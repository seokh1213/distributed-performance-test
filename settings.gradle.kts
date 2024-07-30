plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "performance-test"
include("client:aws:aws-lambda")
include("client:aws:aws-lambda-js")
include("presentation:cli")
