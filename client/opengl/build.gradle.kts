dependencies {
    implementation(frontendLibs.lwjgl.opengl)

    runtimeOnly(frontendLibs.lwjgl.opengl) {
        artifact {
            classifier = System.getProperty("lwjglNatives")
        }
    }
}