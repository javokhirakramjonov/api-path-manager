plugins {
    kotlin("jvm") version "1.9.22"
    id("maven-publish")
}

group = "com.github.javokhirakramjonov:api-path-manager"
version = "1.0"

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