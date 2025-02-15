val lwjglVersion = "3.3.6"
val lwjglNatives = "natives-windows"

plugins {
    kotlin("jvm") version "2.1.10"
}

group = "com.reasure"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform("org.lwjgl:lwjgl-bom:$lwjglVersion"))

    implementation("org.lwjgl", "lwjgl")
    implementation("org.lwjgl", "lwjgl-freetype")
    implementation("org.lwjgl", "lwjgl-glfw")
    implementation("org.lwjgl", "lwjgl-jemalloc")
    implementation("org.lwjgl", "lwjgl-openal")
    implementation("org.lwjgl", "lwjgl-opengl")
    implementation("org.lwjgl", "lwjgl-stb")
    implementation("org.lwjgl", "lwjgl-tinyfd")
    runtimeOnly("org.lwjgl", "lwjgl", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-freetype", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-glfw", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-jemalloc", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-openal", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-opengl", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-stb", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-tinyfd", classifier = lwjglNatives)

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}