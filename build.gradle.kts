plugins {
    kotlin("jvm") version "1.9.22"
    id("maven-publish")
}

group = "me.javahere:api_path_manager"
version = "1.0.1"

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