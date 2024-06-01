package me.javahere.apipathpamanager.example

import Qirikki
import me.javahere.apipathpamanager.buildApiPath

private fun main() {
//    createApiPath()
    exampleUsage()
}

fun createApiPath() =
    buildApiPath(
        baseUrl = "qirikki.uz",
        apiFileName = "Qirikki",
    ) {
        route("auth") {
            route("login")
            route("register")
        }

        route("courses") {
            route("all")
            route("id", isDynamic = true)
        }

        route("teachers") {
            route("android") {
                route("all")
                route("id", isDynamic = true)
            }
        }
    }

fun exampleUsage() {
    val androidTeacherPath =
        Qirikki
            .startWithBaseUrl()
            .teachers()
            .android()
            .id()
            .build()

    println(androidTeacherPath)
}
