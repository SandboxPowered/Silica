dependencies {
    runtimeOnly(project(":content"))

    api(platform("org.sandboxpowered.api:api:rework-SNAPSHOT"))
    api("org.sandboxpowered.api:base")
    api("org.sandboxpowered.api:rendering")
    api("org.sandboxpowered.api:resources")

    api("com.github.zafarkhaja:java-semver:0.9.0")
    api("org.sandboxpowered:SimpleEventHandler:2.0.3")

    api("commons-io:commons-io:2.8.0")
    api("org.apache.commons:commons-lang3:3.11")
    api("com.google.code.gson:gson:2.8.6")
    api("com.google.inject:guice:5.0.0-BETA-1")

    api("com.mojang:brigadier:1.0.17")

    api("net.onedaybeard.artemis:artemis-odb:2.3.0")

    api(artemisContrib("core"))
    api(artemisContrib("eventbus"))
    api(artemisContrib("plugin-singleton"))
    api(artemisContrib("plugin-operations"))

    val akkaVersion: String by project
    val alpakkaVersion: String by project
    val scalaBinVersion: String by project
    val jomlVersion: String by project
    api("com.typesafe.akka:akka-actor-typed_$scalaBinVersion:$akkaVersion")
    api("com.typesafe.akka:akka-stream-typed_$scalaBinVersion:$akkaVersion")
    api("com.lightbend.akka:akka-stream-alpakka-udp_$scalaBinVersion:$alpakkaVersion")

    api("net.sf.jopt-simple:jopt-simple:5.0.4")

    api("com.electronwill.night-config:core:3.6.3")
    api("com.electronwill.night-config:json:3.6.3")
    api("com.electronwill.night-config:toml:3.6.3")

    api("org.joml:joml:$jomlVersion")
    api("org.apache.logging.log4j:log4j-api:2.14.0")
    runtimeOnly("org.apache.logging.log4j:log4j-core:2.14.0")
    runtimeOnly("org.apache.logging.log4j:log4j-slf4j-impl:2.14.0")
    api("com.google.guava:guava:30.1-jre")
    api("it.unimi.dsi:fastutil:8.4.4")
    api("com.mojang:authlib:1.5.21")

    api("io.netty:netty-all:4.1.56.Final")
}

fun artemisContrib(module: String) = "net.mostlyoriginal.artemis-odb:contrib-$module:2.5.0"
