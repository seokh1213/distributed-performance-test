package model

import kotlinx.serialization.Serializable
import kotlin.time.Duration

@Serializable
data class LoadConfiguration(
    val loadDurationMillis: Int,
    val nodeCount: Int,
    val threadCount: Int
)
