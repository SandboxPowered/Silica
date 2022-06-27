@file:Suppress("LocalVariableName", "INACCESSIBLE_TYPE", "UnstableApiUsage")

import org.gradle.internal.os.OperatingSystem

pluginManagement {
    repositories {
        mavenCentral()
        maven {
            url = uri("https://plugins.gradle.org/m2/")
        }
    }
}

rootProject.name = "Silica"

include("client")
include("client:opengl")
include("client:vulkan")

include("common")
include("server")


// Modules, Eventually move these to separate repositories

include("modules:vanilla:common")
include("modules:vanilla:1-18:content")
include("modules:vanilla:1-18:network")
include("modules:vanilla:1-18:world")
include("modules:plugin:test")

// V2 Client

include("v2:client")

project(":v2:client").projectDir = file("client/v2")


// Things that will eventually be moved to external libs

include("api")
include("quartz")
include("utilities")
project(":api").projectDir = file("external/api")
project(":quartz").projectDir = file("external/quartz")
project(":utilities").projectDir = file("external/utilities")

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("./versionCatalogues/commonLibs.versions.toml"))
        }
        create("backendLibs") {
            from(files("./versionCatalogues/backendLibs.versions.toml"))
        }
        create("frontendLibs") {
            from(files("./versionCatalogues/frontendLibs.versions.toml"))

            //region lwjgl
            val osArch = System.getProperty("os.arch")
            val lwjglNatives = when (OperatingSystem.current()) {
                OperatingSystem.LINUX -> "natives-linux${
                    if (osArch.startsWith("arm") || osArch.startsWith("aarch64")) {
                        if ("64" in osArch || osArch.startsWith("armv8")) "-arm64"
                        else "-arm32"
                    } else ""
                }"
                OperatingSystem.MAC_OS -> "natives-macos"
                OperatingSystem.WINDOWS -> if ("64" in osArch) "natives-windows" else "natives-windows-x86"
                else -> throw Error("Unrecognized or unsupported Operating system. Please set \"lwjglNatives\" manually")
            }
            System.setProperty("lwjglNatives", lwjglNatives)
            //endregion

            // region imgui
            val imguiNatives = when (OperatingSystem.current()) {
                OperatingSystem.LINUX -> "natives-linux${if ("64" in osArch) "" else "-x86"}"
                OperatingSystem.MAC_OS -> "natives-macos"
                OperatingSystem.WINDOWS -> "natives-windows${if ("64" in osArch) "" else "-x86"}"
                else -> throw Error("Unrecognized or unsupported Operating system. Please set \"imguiNatives\" manually")
            }
            library(
                "imgui-natives",
                "io.github.spair", "imgui-java-$imguiNatives"
            ).versionRef("imgui")

            bundle("imgui", listOf("imgui-binding", "imgui-lwjgl3", "imgui-natives"))
            // endregion
        }
        create("testLibs") {
            from(files("./versionCatalogues/testLibs.versions.toml"))
        }
    }
}
