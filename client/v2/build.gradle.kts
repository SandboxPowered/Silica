import org.gradle.internal.os.OperatingSystem.*
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.lwjgl.Lwjgl
import org.lwjgl.Lwjgl.Module.*
import org.gradle.internal.os.OperatingSystem.current as currentOS

plugins {
    id("org.lwjgl.plugin") version "0.0.20"
}

val lwjglVersion = "3.3.1-SNAPSHOT"

dependencies {
    api(project(":quartz"))

    Lwjgl {
        version = lwjglVersion
        implementation(core, glfw, nfd, openal, opengl, stb, vulkan, shaderc)
    }
}