package com.github.javokhirakramjonov.api_path_manager

import com.squareup.kotlinpoet.*
import java.nio.file.Path
import java.util.LinkedList
import java.util.Queue

private const val BASE_URL_NAME = "baseUrl"
private const val DEFAULT_PREFIX_NAME = "prefix"
private const val CURRENT_PATH_NAME = "currentPathName"
private const val DYNAMIC_VALUE_NAME = "dynamicValue"
private const val BUILD_FUNCTION_NAME = "build"
private const val BUILD_WILDCARD_FUNCTION_NAME = "buildWildcard"

private val rootSegmentName = generateClassName(1, 1)

internal fun generateApiPaths(
	packageName: String,
	apiFileName: String,
	rootSegment: ApiPathSegment,
) {
	val fileSpec = FileSpec.builder(packageName, apiFileName)

	val rootObject = TypeSpec.objectBuilder(apiFileName).apply {
		addFunction(
			FunSpec.builder("startWithBaseUrl").addCode("return $rootSegmentName(\"${rootSegment.name}\")")
				.returns(ClassName(String.EMPTY, rootSegmentName)).build(),
		)
		addFunction(
			FunSpec.builder("startWithoutBaseUrl").addCode("return $rootSegmentName(\"\")")
				.returns(ClassName(String.EMPTY, rootSegmentName)).build(),
		)
	}

	val segmentTypes = generateSegmentTypes(rootSegment)

	segmentTypes.forEach(rootObject::addType)

	fileSpec.addType(rootObject.build())

	fileSpec.build().writeTo(Path.of("generated-api"))
}

private fun generateSegmentTypes(rootSegment: ApiPathSegment): List<TypeSpec> {
	val types = mutableListOf<TypeSpec>()

	val queue: Queue<ApiPathSegment> = LinkedList()

	var step = 1

	queue.add(rootSegment)

	while (queue.isNotEmpty()) {
		val size = queue.size

		var innerChildCounter = 0

		repeat(size) {
			val currentSegment = queue.poll()

			val className = generateClassName(step, it + 1)

			val isRoot = className == rootSegmentName

			val type = TypeSpec.classBuilder(className)

			generateBuildWildcardFunction(type, isRoot)

			if (!isRoot) {
				addCurrentPathNameProperty(type, currentSegment)
			}

			generateConstructorForSegment(type, currentSegment.isDynamic, isRoot)

			if (currentSegment.nestedSegments.isEmpty() || currentSegment.shouldStop) {
				generateBuildFunction(
					type,
					currentSegment,
					isRoot,
				)
			}

			innerChildCounter = addNestedSegments(
				queue, isRoot, step, currentSegment, type, innerChildCounter
			)

			types += type.build()
		}

		step++
	}

	return types.toList()
}

private fun addNestedSegments(
	queue: Queue<ApiPathSegment>,
	isRoot: Boolean,
	currentSegmentOrder: Int,
	currentSegment: ApiPathSegment,
	segmentType: TypeSpec.Builder,
	childStartOrder: Int
): Int {
	var childCounter = childStartOrder

	currentSegment.nestedSegments.forEach { child ->
		val edgeName = child.name.toCamelCase()
		val childClassName = generateClassName(currentSegmentOrder + 1, childCounter++ + 1)

		val edgeFun = generateEdgeToChild(
			child,
			edgeName,
			childClassName,
			isRoot,
		)

		segmentType.addFunction(edgeFun)

		queue += child
	}

	return childCounter
}

private fun addCurrentPathNameProperty(type: TypeSpec.Builder, currentSegment: ApiPathSegment) {
	type.addProperty(
		PropertySpec.builder(
			name = CURRENT_PATH_NAME,
			type = String::class,
			modifiers = arrayOf(KModifier.PRIVATE),
		).initializer(
			if (currentSegment.isDynamic) {
				"\"\$$DEFAULT_PREFIX_NAME/\$$DYNAMIC_VALUE_NAME\""
			} else {
				"\"\$$DEFAULT_PREFIX_NAME/${currentSegment.name}\""
			},
		).build(),
	)
}

private fun generateClassName(
	segmentNumber: Int,
	childNumber: Int,
): String {
	return "Segment${segmentNumber}Path$childNumber"
}

private fun generateBuildWildcardFunction(
	typeSpec: TypeSpec.Builder,
	isRoot: Boolean,
) {
	val returnPath = if (isRoot) {
		BASE_URL_NAME
	} else {
		CURRENT_PATH_NAME
	}

	val wildcardFun =
		FunSpec.builder(BUILD_WILDCARD_FUNCTION_NAME).addCode("return \"\$$returnPath/**\"").returns(String::class)
			.build()

	typeSpec.addFunction(wildcardFun)
}

private fun generateBuildFunction(
	typeSpec: TypeSpec.Builder,
	apiPathSegment: ApiPathSegment,
	isRoot: Boolean,
) {
	val builderFun = FunSpec.builder(BUILD_FUNCTION_NAME).returns(String::class)

	val queryParams = apiPathSegment.queryParams.map(String::toCamelCase)

	queryParams.forEach {
		builderFun.addParameter(
			ParameterSpec.builder(
				name = it,
				type = String::class.asTypeName().copy(nullable = true),
			).defaultValue("null").build(),
		)
	}

	val returnPath = if (isRoot) {
		BASE_URL_NAME
	} else {
		CURRENT_PATH_NAME
	}

	val funBody = if (queryParams.isNotEmpty()) {
		"""
            val params = listOf(${
			queryParams.joinToString { "\"$it\" to $it" }
		}).filter {
                it.second != null
            }.joinToString(separator = "&") { "${'$'}{it.first}=${'$'}{it.second}" }
            
            return if(params.isNotEmpty())
                "${'$'}$returnPath?${'$'}params"
            else
                $returnPath
            """.trimIndent()
	} else {
		"return $returnPath"
	}

	builderFun.addCode(funBody)

	typeSpec.addFunction(builderFun.build())
}

private fun generateEdgeToChild(
	childApiPathSegment: ApiPathSegment,
	edgeName: String,
	childClassName: String,
	isRoot: Boolean,
): FunSpec {
	val edgeFun = FunSpec.builder(edgeName).returns(ClassName(String.EMPTY, childClassName))

	if (childApiPathSegment.isDynamic) {
		edgeFun.addParameter(
			parameterSpec = ParameterSpec.builder(
				name = DYNAMIC_VALUE_NAME,
				type = String::class,
			).defaultValue("\"${childApiPathSegment.formattedSegmentName}\"").build(),
		)
	}

	val currentPathName = if (isRoot) {
		BASE_URL_NAME
	} else {
		CURRENT_PATH_NAME
	}

	val funBody = if (childApiPathSegment.isDynamic) {
		"return $childClassName($currentPathName, $DYNAMIC_VALUE_NAME)"
	} else {
		"return $childClassName($currentPathName)"
	}

	edgeFun.addCode(funBody)

	return edgeFun.build()
}

private fun generateConstructorForSegment(
	typeSpec: TypeSpec.Builder,
	isDynamic: Boolean,
	isRoot: Boolean,
) {
	val prefixName = if (isRoot) {
		BASE_URL_NAME
	} else {
		DEFAULT_PREFIX_NAME
	}

	val constructor = FunSpec.constructorBuilder().addParameter(
		name = prefixName,
		type = String::class,
	)

	if (isDynamic) {
		constructor.addParameter(
			name = DYNAMIC_VALUE_NAME,
			type = String::class,
		)
	}

	typeSpec.primaryConstructor(constructor.build())

	if (isRoot) {
		val property = PropertySpec.builder(
			name = prefixName,
			type = String::class,
			modifiers = arrayOf(KModifier.PRIVATE),
		).initializer(prefixName).build()

		typeSpec.addProperty(property)
	}
}