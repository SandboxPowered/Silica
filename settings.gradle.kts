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

include("server")

// Modules, Eventually move these to separate repositories

include("modules:vanilla:common")
include("modules:vanilla:1-18:content")
include("modules:vanilla:1-18:network")
include("modules:vanilla:1-18:world")
include("modules:plugin:test")

// Things that will eventually be moved to external libs

include("api")

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("./versionCatalogues/libs.versions.toml"))
        }
        create("testLibs") {
            from(files("./versionCatalogues/testLibs.versions.toml"))
        }
    }
}
