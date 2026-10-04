package com.melvin.predictor.repository

import com.melvin.predictor.api.ApiClient
import com.melvin.predictor.model.ApiQuota
import com.melvin.predictor.model.Match
import com.melvin.predictor.utils.QuotaPreferences

sealed class ResultState<out T> {
    data class Success<T>(
        val data: T,
        val quota: ApiQuota
    ) : ResultState<T>()
    data class Error(
        val message: String
    ) : ResultState<Nothing>()
    object Loading       : ResultState<Nothing>()
    object QuotaExhausted: ResultState<Nothing>()
}

class OddsRepository(
    private val quotaPrefs: QuotaPreferences
) {
    private val service = ApiClient.oddsService

    suspend fun getOddsForLeague(
        sportKey: String
    ): ResultState<List<Match>> {

        if (quotaPrefs.isQuotaExhausted()) {
            return ResultState.QuotaExhausted
        }

        return try {
            val response = service.getOddsForSport(
                sport    = sportKey,
                markets  = "h2h,totals,spreads,btts",
                regions  = "uk,us,eu,au"
            )

            if (response.isSuccessful) {
                val quota = extractQuota(response.headers())
                quota?.let { quotaPrefs.saveQuota(it) }
                ResultState.Success(
                    data  = response.body() ?: emptyList(),
                    quota = quota ?: defaultQuota()
                )
            } else {
                ResultState.Error(parseError(response.code()))
            }

        } catch (e: java.net.UnknownHostException) {
            ResultState.Error("📡 No internet!")
        } catch (e: java.net.SocketTimeoutException) {
            ResultState.Error("⏱️ Timed out!")
        } catch (e: Exception) {
            ResultState.Error("❌ ${e.localizedMessage}")
        }
    }

    private fun extractQuota(
        headers: okhttp3.Headers
    ): ApiQuota? {
        return try {
            val remaining = headers["x-requests-remaining"]
                ?.toIntOrNull() ?: return null
            val used      = headers["x-requests-used"]
                ?.toIntOrNull() ?: 0
            val lastCost  = headers["x-requests-last"]
                ?.toIntOrNull() ?: 1
            ApiQuota(
                requestsRemaining = remaining,
                requestsUsed      = used,
                lastRequestCost   = lastCost
            )
        } catch (e: Exception) { null }
    }

    fun getCachedQuota(): ApiQuota? =
        quotaPrefs.loadQuota()

    private fun defaultQuota() = ApiQuota(
        requestsRemaining = 0,
        requestsUsed      = 500,
        lastRequestCost   = 0
    )

    private fun parseError(code: Int): String =
        when (code) {
            401  -> "🔑 Invalid API Key!"
            403  -> "🚫 Access forbidden!"
            429  -> "🔴 Too many requests!"
            500  -> "🔥 Server error!"
            else -> "❌ Error: $code"
        }
}
