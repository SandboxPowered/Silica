import org.gradle.internal.os.OperatingSystem

plugins {
    application
    kotlin("jvm") version "1.5.0"
}

allprojects {
    group = "org.sandboxpowered"
    version = "1.0-SNAPSHOT"

    apply(plugin = "kotlin")
    apply(plugin = "application")

    repositories {
        maven("https://nexus.sandboxpowered.org/repository/maven-snapshots/")
        maven("https://nexus.sandboxpowered.org/repository/maven-public/")
        mavenCentral()
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
        maven("https://jitpack.io")
        maven("https://dl.bintray.com/kotlin/kotlin-eap")
        maven("https://libraries.minecraft.net/")
    }
}

subprojects {
    dependencies {
        val junitVersion: String by project
        val mockkVersion: String by project
        testImplementation("org.junit.jupiter", "junit-jupiter-api", junitVersion)
        testImplementation("org.junit.jupiter", "junit-jupiter-params", junitVersion)
        testImplementation("io.mockk", "mockk", mockkVersion)
        testRuntimeOnly("org.junit.jupiter", "junit-jupiter-engine", junitVersion)
    }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(14))
        }
    }

    tasks {
        compileKotlin {
            kotlinOptions {
                jvmTarget = "14"
            }
        }
        compileTestKotlin {
            kotlinOptions {
                jvmTarget = "14"
            }
        }
        test {
            useJUnitPlatform()
        }
    }

    application {
        mainClass.set("org.sandboxpowered.silica.client.main.Main")
        if (OperatingSystem.current().isMacOsX) {
            // Need this to make sure the OS starts our app on thread 0
            applicationDefaultJvmArgs += "-XStartOnFirstThread"
        }
    }
}

extensions.findByName("buildScan")?.withGroovyBuilder {
    setProperty("termsOfServiceUrl", "https://gradle.com/terms-of-service")
    setProperty("termsOfServiceAgree", "yes")
}