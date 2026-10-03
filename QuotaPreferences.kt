package com.melvin.predictor.utils

import android.content.Context
import com.melvin.predictor.model.ApiQuota

class QuotaPreferences(context: Context) {

    private val prefs = context.getSharedPreferences(
        "melvin_quota_prefs",
        Context.MODE_PRIVATE
    )

    companion object {
        private const val KEY_REMAINING    = "requests_remaining"
        private const val KEY_USED         = "requests_used"
        private const val KEY_LAST_COST    = "last_request_cost"
        private const val KEY_LAST_CHECKED = "last_checked"

        // 🔑 NEW! Store API key after user enters it
        private const val KEY_API_KEY      = "odds_api_key"
    }

    // ─────────────────────────────────────
    // 🔑 API KEY FUNCTIONS
    // ─────────────────────────────────────

    // 💾 Save API key entered by user
    fun saveApiKey(apiKey: String) {
        prefs.edit().putString(KEY_API_KEY, apiKey).apply()
    }

    // 📖 Load saved API key
    fun getApiKey(): String {
        return prefs.getString(KEY_API_KEY, "") ?: ""
    }

    // ✅ Check if user has entered a key
    fun hasApiKey(): Boolean {
        return getApiKey().isNotEmpty()
    }

    // 🗑️ Clear API key (reset)
    fun clearApiKey() {
        prefs.edit().remove(KEY_API_KEY).apply()
    }

    // ─────────────────────────────────────
    // 📊 QUOTA FUNCTIONS (same as before)
    // ─────────────────────────────────────
    fun saveQuota(quota: ApiQuota) {
        prefs.edit().apply {
            putInt(KEY_REMAINING,    quota.requestsRemaining)
            putInt(KEY_USED,         quota.requestsUsed)
            putInt(KEY_LAST_COST,    quota.lastRequestCost)
            putLong(KEY_LAST_CHECKED, System.currentTimeMillis())
            apply()
        }
    }

    fun loadQuota(): ApiQuota? {
        val remaining = prefs.getInt(KEY_REMAINING, -1)
        if (remaining == -1) return null
        return ApiQuota(
            requestsRemaining = remaining,
            requestsUsed      = prefs.getInt(KEY_USED, 0),
            lastRequestCost   = prefs.getInt(KEY_LAST_COST, 0),
            lastChecked       = prefs.getLong(KEY_LAST_CHECKED, 0L)
        )
    }

    fun isQuotaExhausted(): Boolean {
        val remaining = prefs.getInt(KEY_REMAINING, 999)
        return remaining <= 0
    }

    fun isQuotaCritical(): Boolean {
        val remaining = prefs.getInt(KEY_REMAINING, 999)
        return remaining in 1..10
    }
}
