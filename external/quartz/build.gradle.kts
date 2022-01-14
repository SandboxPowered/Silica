import org.gradle.internal.os.OperatingSystem.*
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.lwjgl.Lwjgl
import org.lwjgl.Lwjgl.Module.*
import org.gradle.internal.os.OperatingSystem.current as currentOS

plugins {
    id("org.lwjgl.plugin") version "0.0.20"
}

val lwjglVersion = "3.3.1-SNAPSHOT"
val jomlVersion = "1.10.2"
val steamworks4jVersion = "1.8.0"

val lwjglNatives = when (currentOS()) {
    LINUX -> "natives-linux"
    MAC_OS -> "natives-macos"
    WINDOWS -> "natives-windows"
    else -> throw Error("Unrecognized or unsupported Operating system. Please set \"lwjglNatives\" manually")
}

dependencies {
    api(project(":utilities"))

    Lwjgl {
        version = lwjglVersion
        implementation(core, glfw, nfd, openal, opengl, stb, vulkan, shaderc)
    }

    implementation("io.github.spair:imgui-java-binding:1.86.0")
    implementation("io.github.spair:imgui-java-lwjgl3:1.86.0")
    implementation("io.github.spair:imgui-java-$lwjglNatives:1.86.0")
}