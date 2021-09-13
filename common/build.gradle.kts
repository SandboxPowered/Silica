import com.google.protobuf.gradle.*
import org.jetbrains.kotlin.config.KotlinCompilerVersion

plugins {
    id("com.google.protobuf") version "0.8.17"
}

apply(plugin = "idea")

val jomlVersion = "1.9.25"

group = "org.sandboxpowered"
version = "1.0-SNAPSHOT"

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.17.3"
    }
    plugins {
        id("kotlin")
    }
    generateProtoTasks {
        all().forEach {
            it.builtins {
                kotlin {}
                java {}
            }
            it.plugins {
                id("kotlin")
            }
        }
    }
}

tasks.configureEach {
    if (name.matches(Regex("^extractInclude([A-Za-z]+)TestProto$"))) {
        enabled = false
    }
}

dependencies {
    api("com.github.zafarkhaja:java-semver:0.9.0")

    api("org.jetbrains:annotations:22.0.0")

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

    val akkaVersion = "2.6.10"
    val alpakkaVersion = "2.0.2"
    val scalaBinVersion = "2.13"
    api("com.typesafe.akka:akka-actor-typed_$scalaBinVersion:$akkaVersion")
    api("com.typesafe.akka:akka-stream-typed_$scalaBinVersion:$akkaVersion")
    api("com.lightbend.akka:akka-stream-alpakka-udp_$scalaBinVersion:$alpakkaVersion")

    api("net.sf.jopt-simple:jopt-simple:5.0.4")

    api("com.electronwill.night-config:core:3.6.3")
    api("com.electronwill.night-config:json:3.6.3")
    api("com.electronwill.night-config:toml:3.6.3")

    api("org.joml:joml:${jomlVersion}")
    api("org.apache.logging.log4j:log4j-api:2.14.0")
    runtimeOnly("org.apache.logging.log4j:log4j-core:2.14.0")
    runtimeOnly("org.apache.logging.log4j:log4j-slf4j-impl:2.14.0")
    api("com.google.guava:guava:30.1-jre")
    api(kotlin("stdlib", KotlinCompilerVersion.VERSION))
    api("it.unimi.dsi:fastutil:8.4.4")
    api("com.mojang:authlib:1.5.21")

    api("io.netty:netty-all:4.1.56.Final")
    api("com.google.protobuf:protobuf-java:3.17.3")
    api("com.google.protobuf:protobuf-kotlin:3.17.3")
}


fun artemisContrib(module: String): String {
    return "net.mostlyoriginal.artemis-odb:contrib-$module:2.5.0"
}
