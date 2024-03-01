package com.github.javokhirakramjonov.api_path_manager

/**
 * Api Path Builder Dsl
 * @param baseUrl base url for api set
 * @sample
 */
class ApiPathBuilderDsl(baseUrl: String) {
	internal val baseSegment = ApiPathSegment(baseUrl)

	private var currentSegment = baseSegment

	/**
	 * Adds nested route to current route.
	 */
	fun route(
		name: String,
		isDynamic: Boolean = false,
		shouldStop: Boolean = false,
		builderBlock: ApiPathBuilderDsl.() -> Unit = {},
	) {
		val prev = currentSegment

		currentSegment = ApiPathSegment(name, isDynamic, shouldStop)

		builderBlock()

		prev.nestedSegments.add(currentSegment)

		currentSegment = prev
	}

	/**
	 * Adds query parameters if and only if current segment is endpoint.
	 */
	fun queryParams(vararg params: String) {
		currentSegment.queryParams.addAll(params)
	}
}

/**
 * Generates api path segment classes to build and manage easily.
 * @param baseUrl base url for api path set.
 * @param apiFileName filename which generated code will locate.
 * @param printAllEndpoints if it is true then it will print all possible endpoints in the console.
 * @param builderBlock it is builder block to build your api paths.
 */
fun buildApiPath(
	baseUrl: String,
	apiFileName: String,
	printAllEndpoints: Boolean = true,
	builderBlock: ApiPathBuilderDsl.() -> Unit,
) {
	val packageName = String.EMPTY

	ApiPathBuilderDsl(baseUrl).apply {
		builderBlock()

		if (printAllEndpoints) baseSegment.printPaths()

		generateApiPaths(packageName, apiFileName, baseSegment)
	}
}
