import org.lwjgl.Lwjgl
import org.lwjgl.Lwjgl.Module.*

plugins {
    alias(frontendLibs.plugins.lwjgl)
}

dependencies {
    api(project(":utilities"))

    Lwjgl {
        version = "3.3.1"
        implementation(core, glfw, nfd, openal, opengl, stb, vulkan, shaderc)
    }

    implementation(frontendLibs.bundles.imgui)
    /*runtimeOnly(frontendLibs.bundles.imgui) {
        artifact {
            classifier = "native"
        }
    }*/
}