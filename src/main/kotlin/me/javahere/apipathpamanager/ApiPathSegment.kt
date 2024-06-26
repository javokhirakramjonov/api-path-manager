package me.javahere.apipathpamanager

internal class ApiPathSegment(
    val name: String,
    val isDynamic: Boolean = false,
    val shouldStop: Boolean = false,
    val nestedSegments: MutableList<ApiPathSegment> = mutableListOf(),
    val queryParams: MutableList<String> = mutableListOf(),
) {
    val formattedSegmentName = if (isDynamic) "{$name}" else name
}
