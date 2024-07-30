package monster.wukong.client.aws.lambda

import aws.sdk.kotlin.services.iam.IamClient
import aws.sdk.kotlin.services.iam.model.AttachRolePolicyRequest
import aws.sdk.kotlin.services.iam.model.CreateRoleRequest
import aws.sdk.kotlin.services.iam.model.DeleteRoleRequest
import aws.sdk.kotlin.services.iam.model.GetRoleRequest
import aws.smithy.kotlin.runtime.auth.awscredentials.CredentialsProvider

context(CredentialsProvider)
suspend fun createLambdaRole(roleNameVal: String): String {
    IamClient {
        region = "AWS_GLOBAL"
        credentialsProvider = this@CredentialsProvider
    }.use { iamClient ->
        runCatching {
            iamClient.getRole(GetRoleRequest {
                roleName = roleNameVal
            })
        }.onSuccess {
            return it.role!!.arn
        }.onFailure {
            println("Role not found. Creating a new role.")
        }

        val policyDocument = """
            {
                "Version": "2012-10-17",
                "Statement": [
                    {
                        "Effect": "Allow",
                        "Principal": {
                            "Service": "lambda.amazonaws.com"
                        },
                        "Action": "sts:AssumeRole"
                    }
                ]
            }
        """.trimIndent()

        val createRoleResponse = iamClient.createRole(CreateRoleRequest {
            roleName = roleNameVal
            description = "create lambda role for load test"
            assumeRolePolicyDocument = policyDocument
        })
        if (createRoleResponse.role == null) {
            throw IllegalStateException("Failed to create role")
        }

        val policyArn = "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole"
        val policyRequest =
            AttachRolePolicyRequest {
                roleName = roleNameVal
                this.policyArn = policyArn
            }
        iamClient.attachRolePolicy(policyRequest)

        return createRoleResponse.role!!.arn
    }
}

context(CredentialsProvider)
suspend fun deleteRole(roleNameVal: String = "load-test-lambda") {
    IamClient {
        region = "AWS_GLOBAL"
        credentialsProvider = this@CredentialsProvider
    }.use { iamClient ->
        runCatching {
            iamClient.deleteRole(DeleteRoleRequest {
                roleName = roleNameVal
            })
        }.onFailure {
            println("Fail to delete role: $roleNameVal. ${it.message}")
        }
    }
}
