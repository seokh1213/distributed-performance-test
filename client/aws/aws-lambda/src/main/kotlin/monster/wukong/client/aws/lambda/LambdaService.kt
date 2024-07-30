package monster.wukong.client.aws.lambda

import aws.sdk.kotlin.services.lambda.LambdaClient
import aws.sdk.kotlin.services.lambda.model.CreateFunctionRequest
import aws.sdk.kotlin.services.lambda.model.DeleteFunctionRequest
import aws.sdk.kotlin.services.lambda.model.FunctionCode
import aws.sdk.kotlin.services.lambda.model.FunctionConfiguration
import aws.sdk.kotlin.services.lambda.model.GetFunctionRequest
import aws.sdk.kotlin.services.lambda.model.InvokeRequest
import aws.sdk.kotlin.services.lambda.model.ListFunctionsRequest
import aws.sdk.kotlin.services.lambda.model.LogType
import aws.sdk.kotlin.services.lambda.model.Runtime
import aws.sdk.kotlin.services.lambda.waiters.waitUntilFunctionActive
import aws.smithy.kotlin.runtime.auth.awscredentials.CredentialsProvider
import aws.smithy.kotlin.runtime.text.encoding.decodeBase64
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import monster.wukong.client.aws.model.LoadContext
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

private fun getFunctionCodeZip(): ByteArray {
    val resourceUrl = Thread.currentThread().contextClassLoader.getResource("js")
        ?: throw IllegalStateException("Resource not found. resources/js")
    val sourceDir = File(resourceUrl.toURI())

    val file = File.createTempFile("function", ".zip").also { it.deleteOnExit() }
    ZipOutputStream(FileOutputStream(file)).use { zipOut ->
        sourceDir.walkTopDown().filter { it.isFile }.forEach { file ->
            val entryName = sourceDir.toPath().relativize(file.toPath()).toString()
            zipOut.putNextEntry(ZipEntry(entryName))
            file.inputStream().use { it.copyTo(zipOut) }
            zipOut.closeEntry()
        }
    }
    return file.readBytes()
}

context(CredentialsProvider)
suspend fun createLoadTestFunction(
    myFunctionName: String,
    role: String,
    region: String = "ap-northeast-2"
): String {
    val functionCode = FunctionCode { zipFile = getFunctionCodeZip() }

    val request =
        CreateFunctionRequest {
            functionName = myFunctionName
            code = functionCode
            description = "LoadTest function"
            handler = "aws-lambda-js.handler"
            runtime = Runtime.Nodejs18X
            this.role = role
        }

    // Create a Lambda function using a waiter
    LambdaClient {
        this.region = region
        credentialsProvider = this@CredentialsProvider
    }.use { awsLambda ->
        val functionResponse = awsLambda.createFunction(request)
        awsLambda.waitUntilFunctionActive {
            functionName = myFunctionName
        }
        return functionResponse.functionArn.toString()
    }
}

context(CredentialsProvider)
suspend fun getFunction(functionNameVal: String, region: String = "ap-northeast-2") {
    val functionRequest =
        GetFunctionRequest {
            functionName = functionNameVal
        }

    LambdaClient {
        this.region = region
        credentialsProvider = this@CredentialsProvider
    }.use { awsLambda ->
        val response = awsLambda.getFunction(functionRequest)
        println("The runtime of this Lambda function is ${response.configuration?.runtime}")
    }
}

context(CredentialsProvider)
@OptIn(ExperimentalCoroutinesApi::class)
suspend fun listFunctions(region: String = "ap-northeast-2"): List<FunctionConfiguration> {
    LambdaClient {
        this.region = region
        credentialsProvider = this@CredentialsProvider
    }.use { awsLambda ->
        return flow {
            var nextMarker: String? = null
            do {
                val response = awsLambda.listFunctions(ListFunctionsRequest {
                    maxItems = 50
                    marker = nextMarker
                })

                response.functions?.let { emit(it) }
                nextMarker = response.nextMarker
            } while (nextMarker != null)
        }.flatMapConcat { it.asFlow() }.toList()
    }
}

context(CredentialsProvider)
suspend fun invokeFunction(functionName: String, context: LoadContext, region: String = "ap-northeast-2") {
    val json = Json.encodeToString(context)
    val byteArray = json.trimIndent().encodeToByteArray()
    val request =
        InvokeRequest {
            this.functionName = functionName
            payload = byteArray
            logType = LogType.Tail
        }

    LambdaClient {
        this.region = region
        credentialsProvider = this@CredentialsProvider
    }.use { awsLambda ->
        val res = awsLambda.invoke(request)
        println("The function payload is ${res.payload?.toString(Charsets.UTF_8)}")
        println("response: ${res.logResult?.decodeBase64()}")
    }
}


context(CredentialsProvider)
suspend fun deleteFunction(myFunctionName: String, region: String = "ap-northeast-2") {
    val request =
        DeleteFunctionRequest {
            functionName = myFunctionName
        }

    LambdaClient {
        this.region = region
        credentialsProvider = this@CredentialsProvider
    }.use { awsLambda ->
        awsLambda.deleteFunction(request)
        println("$myFunctionName was deleted")
    }
}
