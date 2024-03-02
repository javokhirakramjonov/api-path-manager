package me.javahere.api_path_manager

private const val CAMEL_CASE_DELIMITER = "_"

private val camelCaseIgnoredCharacterRegex = Regex("[^A-Za-z0-9_]")

val String.Companion.EMPTY: String
	get() = ""

fun String.toCamelCase(): String {
	val trimmedString = this.trim()

	val cleanedString = trimmedString.replace(camelCaseIgnoredCharacterRegex, CAMEL_CASE_DELIMITER)

	val parts =
		cleanedString
			.split(CAMEL_CASE_DELIMITER)
			.mapIndexed { index, part ->
				if (index == 0) {
					part.replaceFirstChar(Char::lowercaseChar)
				} else {
					part.replaceFirstChar(Char::uppercaseChar)
				}
			}

	return parts.joinToString("")
}
