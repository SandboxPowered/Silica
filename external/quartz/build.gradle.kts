import org.lwjgl.Lwjgl
import org.lwjgl.Lwjgl.Module.*

plugins {
    alias(frontendLibs.plugins.lwjgl)
}

dependencies {
    api(project(":utilities"))

    Lwjgl {
        version = frontendLibs.versions.lwjgl.asProvider().get()
        implementation(core, glfw, nfd, openal, opengl, stb, vulkan, shaderc)
    }

    implementation(frontendLibs.bundles.imgui)
    /*runtimeOnly(frontendLibs.bundles.imgui) {
        artifact {
            classifier = "native"
        }
    }*/
}