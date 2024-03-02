plugins {
    kotlin("jvm") version "1.9.22"
    id("maven-publish")
}

val appGroup = "me.javahere"
val appName = "api-path-manager"
val appVersion = "1.0"

group = "$appGroup:$appName"
version = appVersion

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = appGroup
            artifactId = appName
            version = "1.0"

            from(components["java"])
        }
    }
}

repositories {
    mavenCentral()
}

dependencies {
    val squareUpVersion = "1.16.0"
    implementation("com.squareup:kotlinpoet:$squareUpVersion")
}

kotlin {
    jvmToolchain(21)
}