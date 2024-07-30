import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalDistributionDsl

plugins {
    val kotlinVersion = "2.0.0"
    kotlin("multiplatform") version kotlinVersion
    kotlin("plugin.js-plain-objects") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion
}

repositories {
    mavenCentral()
}

val minifiedJsDirectory = layout.buildDirectory.dir("minified-js")

kotlin {

    sourceSets {
        jsMain {
            dependencies {
                implementation(npm("node-fetch", "2.7.0"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.1")
            }
        }
        jsTest {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
    js {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            target.set("es2015")
        }

        nodejs {
            @OptIn(ExperimentalDistributionDsl::class)
            distribution {
                outputDirectory.set(minifiedJsDirectory)
            }

            testTask {
                useKarma()
            }

            binaries.executable()
        }

        browser {
            @OptIn(ExperimentalDistributionDsl::class)
            distribution {
                outputDirectory.set(minifiedJsDirectory)
            }

            testTask {
                useKarma()
            }

            webpackTask {

            }

            binaries.executable()
        }

    }
}

tasks.register<Copy>("copyJsToMainProject") {
    from(minifiedJsDirectory)
    into("../aws-lambda/build/resources/main/js")
    dependsOn(tasks.named("build"))
}
