plugins {
    kotlin("jvm") version "1.9.22"
    id("maven-publish")
}

val appName = "api-path-manager"
val appVersion = "1.0.3"
val appGroupId = "me.javahere:$appName"

group = appGroupId
version = appVersion

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = appGroupId
            artifactId = appName
            version = "1.0.2"

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