package model

import kotlinx.serialization.Serializable

@Serializable
data class LoadContext(
    val httpRequest: HttpRequest,
    val configuration: LoadConfiguration
)
