import org.gradle.internal.os.OperatingSystem

val os: OperatingSystem = OperatingSystem.current()
val lwjglNatives = when {
    os.isLinux -> {
        val osArch = System.getProperty("os.arch")
        if (osArch.startsWith("arm") || osArch.startsWith("aarch64")) {
            "natives-linux-arm64"
        } else {
            "natives-linux"
        }
    }
    os.isMacOsX -> "natives-macos"
    os.isWindows -> "natives-windows"
    else -> ""
}

subprojects {
    dependencies {
        implementation(project(":client"))
    }
}

dependencies {
    val lwjglVersion: String by project

    api(project(":common"))

    api(platform("org.lwjgl:lwjgl-bom:$lwjglVersion"))
    api("org.lwjgl:lwjgl")
    api("org.lwjgl:lwjgl-assimp")
    api("org.lwjgl:lwjgl-bullet")
    api("org.lwjgl:lwjgl-glfw")
    api("org.lwjgl:lwjgl-nfd")
    api("org.lwjgl:lwjgl-openal")
    api("org.lwjgl:lwjgl-openvr")
    api("org.lwjgl:lwjgl-opus")
    api("org.lwjgl:lwjgl-shaderc")
    api("org.lwjgl:lwjgl-stb")
    api("org.lwjgl:lwjgl-tinyexr")

    runtimeOnly("org.lwjgl:lwjgl::$lwjglNatives")
    runtimeOnly("org.lwjgl:lwjgl-assimp::$lwjglNatives")
    runtimeOnly("org.lwjgl:lwjgl-bullet::$lwjglNatives")
    runtimeOnly("org.lwjgl:lwjgl-glfw::$lwjglNatives")
    runtimeOnly("org.lwjgl:lwjgl-nfd::$lwjglNatives")
    runtimeOnly("org.lwjgl:lwjgl-openal::$lwjglNatives")
    runtimeOnly("org.lwjgl:lwjgl-openvr::$lwjglNatives")
    runtimeOnly("org.lwjgl:lwjgl-opus::$lwjglNatives")
    runtimeOnly("org.lwjgl:lwjgl-shaderc::$lwjglNatives")
    runtimeOnly("org.lwjgl:lwjgl-stb::$lwjglNatives")
    runtimeOnly("org.lwjgl:lwjgl-tinyexr::$lwjglNatives")
}

project(":client:opengl").dependencies {
    api("org.lwjgl:lwjgl-opengl")
    runtimeOnly("org.lwjgl:lwjgl-opengl::$lwjglNatives")
}

project(":client:vulkan").dependencies {
    implementation("org.lwjgl:lwjgl-vulkan")
    if (os.isMacOsX) {
        runtimeOnly("org.lwjgl:lwjgl-vulkan::$lwjglNatives")
    }
}