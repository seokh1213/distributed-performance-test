package monster.wukong.client.aws

import aws.sdk.kotlin.runtime.auth.credentials.StaticCredentialsProvider
import monster.wukong.client.aws.model.HttpRequest
import monster.wukong.client.aws.model.LoadConfiguration
import monster.wukong.client.aws.model.LoadContext
import monster.wukong.client.aws.lambda.createLambdaRole
import monster.wukong.client.aws.lambda.createLoadTestFunction
import monster.wukong.client.aws.lambda.deleteFunction
import monster.wukong.client.aws.lambda.deleteRole
import monster.wukong.client.aws.lambda.invokeFunction
import monster.wukong.client.aws.lambda.listFunctions
import kotlin.time.Duration.Companion.seconds


suspend fun main() {
    val credentials = StaticCredentialsProvider {
        accessKeyId = ""
        secretAccessKey = ""
    }

    with(credentials) {
        val roleName = "load-test-lambda"
        val roleArn = createLambdaRole(roleName)

        val functionName = "testLoadFunction"
        val arn = createLoadTestFunction("testLoadFunction", roleArn)
        println("Function ($functionName) ARN: $arn")
        println("---created---")

        listFunctions().takeIf { it.isNotEmpty() }
            ?.forEach { println(it) }
            ?: run { println("No functions found") }

        invokeFunction(
            functionName, LoadContext(
                httpRequest = HttpRequest(
                    url = "https://www.google.com",
                    method = "GET",
                    headers = mapOf("Content-Type" to "application/json"),
                    body = "Hello, World!",
                    timeoutMillis = 5000
                ),
                configuration = LoadConfiguration(
                    loadDurationMillis = 1000,
                    nodeCount = 1,
                    threadCount = 1
                )
            )
        )

        deleteFunction(functionName)

        println("---deleted---")
        listFunctions().takeIf { it.isNotEmpty() }
            ?.forEach { println(it) }
            ?: run { println("No functions found") }

        deleteRole(roleName)
    }

}
