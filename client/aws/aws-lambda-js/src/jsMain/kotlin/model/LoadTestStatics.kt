package model

data class LoadTestStatics(
    val requestTotalCount: Int,
    val requestSuccessCount: Int,
    val requestFailureCount: Int,
    val responseTimeMillisMin: Int,
    val responseTimeMillisMean: Int,
    val responseTimeMillisMax: Int,
    val requestPerSecond: Int,
    val durationMillis: Int
)
