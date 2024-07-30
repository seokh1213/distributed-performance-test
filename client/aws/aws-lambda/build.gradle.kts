plugins {
    val kotlinVersion = "2.0.0"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion
}

group = "monster.wukong"
version = "0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.1")
    implementation("aws.sdk.kotlin:lambda:1.2.52")
    implementation("aws.sdk.kotlin:iam:1.2.52")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
    sourceSets.all {
        languageSettings.enableLanguageFeature("ContextReceivers")
    }
}

sourceSets {
    main {
        resources {
            srcDir("resources/js")
        }
    }
}

tasks.register<Copy>("copyJsFromSubProject") {
    from("../aws-lambda-js/build/minified-js")
    into("build/resources/main/js")
    dependsOn(":client:aws:aws-lambda-js:copyJsToMainProject")
    mustRunAfter("processResources")

    doFirst {
        println("Starting copyJsFromSubProject task")
    }


    doLast {
        println("Finished copyJsFromSubProject task")
    }
}

tasks.named("classes") {
    dependsOn("copyJsFromSubProject")
}
