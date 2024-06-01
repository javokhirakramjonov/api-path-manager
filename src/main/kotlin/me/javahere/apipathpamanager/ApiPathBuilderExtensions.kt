package me.javahere.apipathpamanager

internal fun ApiPathSegment.printPaths() {
    println("All possible endpoints:")

    printSegments(String.EMPTY, this)

    println("Finish.")
}

private fun printSegments(
    currentPath: String,
    apiPathSegment: ApiPathSegment,
) {
    val path =
        if (currentPath.isEmpty()) {
            apiPathSegment.formattedSegmentName
        } else {
            "$currentPath/${apiPathSegment.formattedSegmentName}"
        }

    if (apiPathSegment.nestedSegments.isEmpty() || apiPathSegment.shouldStop) {
        println(path)
    }

    for (nested in apiPathSegment.nestedSegments) {
        printSegments(path, nested)
    }
}
