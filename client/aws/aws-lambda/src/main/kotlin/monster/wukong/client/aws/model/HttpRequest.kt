package monster.wukong.client.aws.model

import kotlinx.serialization.Serializable

@Serializable
data class HttpRequest(
    val url: String,
    val method: String,
    val headers: Map<String, String>,
    val body: String,
    val timeoutMillis: Int,
)
