package com.melvin.predictor.utils

import android.content.Context
import com.melvin.predictor.model.ApiQuota
import com.melvin.predictor.model.QuotaStatus

class QuotaPreferences(context: Context) {

    private val prefs = context.getSharedPreferences(
        "melvin_quota_prefs",
        Context.MODE_PRIVATE
    )

    companion object {
        private const val KEY_REMAINING   = "requests_remaining"
        private const val KEY_USED        = "requests_used"
        private const val KEY_LAST_COST   = "last_request_cost"
        private const val KEY_LAST_CHECKED = "last_checked"
    }

    // 💾 Save quota after every API call
    fun saveQuota(quota: ApiQuota) {
        prefs.edit().apply {
            putInt(KEY_REMAINING,    quota.requestsRemaining)
            putInt(KEY_USED,         quota.requestsUsed)
            putInt(KEY_LAST_COST,    quota.lastRequestCost)
            putLong(KEY_LAST_CHECKED, System.currentTimeMillis())
            apply()
        }
    }

    // 📖 Load cached quota instantly on app open
    fun loadQuota(): ApiQuota? {
        val remaining = prefs.getInt(KEY_REMAINING, -1)
        if (remaining == -1) return null  // ← First time, no cache

        return ApiQuota(
            requestsRemaining = remaining,
            requestsUsed      = prefs.getInt(KEY_USED, 0),
            lastRequestCost   = prefs.getInt(KEY_LAST_COST, 0),
            lastChecked       = prefs.getLong(KEY_LAST_CHECKED, 0L)
        )
    }

    // 🚫 Check if we should block API calls
    fun isQuotaExhausted(): Boolean {
        val remaining = prefs.getInt(KEY_REMAINING, 999)
        return remaining <= 0
    }

    // ⚠️ Check if quota is critical
    fun isQuotaCritical(): Boolean {
        val remaining = prefs.getInt(KEY_REMAINING, 999)
        return remaining in 1..10
    }
}
