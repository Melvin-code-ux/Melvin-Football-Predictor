package com.melvin.predictor.model

data class ApiQuota(
    val requestsRemaining: Int,
    val requestsUsed: Int,
    val lastRequestCost: Int,
    val totalQuota: Int = 500,
    val lastChecked: Long = System.currentTimeMillis()
) {
    val usagePercent: Int
        get() = ((requestsUsed.toFloat() / totalQuota) * 100).toInt()

    val statusLevel: QuotaStatus
        get() = when {
            requestsRemaining > 100 -> QuotaStatus.GOOD
            requestsRemaining > 20  -> QuotaStatus.WARNING
            requestsRemaining > 0   -> QuotaStatus.CRITICAL
            else                    -> QuotaStatus.EXHAUSTED
        }

    val statusMessage: String
        get() = when (statusLevel) {
            QuotaStatus.GOOD      -> "✅ You're good to go!"
            QuotaStatus.WARNING   -> "⚠️ Use wisely!"
            QuotaStatus.CRITICAL  -> "🔴 Almost out!"
            QuotaStatus.EXHAUSTED -> "⛔ Quota exhausted!"
        }
}

enum class QuotaStatus {
    GOOD, WARNING, CRITICAL, EXHAUSTED
}
