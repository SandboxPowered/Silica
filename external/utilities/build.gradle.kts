private val String.version get() = project.extra["${this}_version"] as String

dependencies {
    api(libs.commons.io)
    api(libs.commons.lang)
    api(libs.gson)
    api(libs.guice)
    api(libs.fastutil)

    api(libs.bundles.nightconfig)

    api(libs.joml)

    api(libs.log4j)
}
