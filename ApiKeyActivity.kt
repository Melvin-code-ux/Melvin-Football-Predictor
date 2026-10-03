package com.melvin.predictor.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.melvin.predictor.api.ApiClient
import com.melvin.predictor.databinding.ActivityApiKeyBinding
import com.melvin.predictor.utils.QuotaPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class ApiKeyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityApiKeyBinding
    private lateinit var quotaPrefs: QuotaPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding    = ActivityApiKeyBinding.inflate(layoutInflater)
        quotaPrefs = QuotaPreferences(this)
        setContentView(binding.root)

        // Load existing key
        val existingKey = quotaPrefs.getApiKey()
        if (existingKey.isNotEmpty()) {
            binding.etApiKey.setText(existingKey)
        }

        binding.btnSaveKey.setOnClickListener {
            // 🧹 Clean the key thoroughly!
            val key = binding.etApiKey.text
                .toString()
                .trim()
                .replace("\n", "")
                .replace("\r", "")
                .replace(" ", "")

            when {
                key.isEmpty() -> {
                    binding.tilApiKey.error = "❌ Enter your API key!"
                }
                key.length < 5 -> {
                    binding.tilApiKey.error = "❌ Key too short!"
                }
                else -> {
                    testAndSave(key)
                }
            }
        }

        binding.btnClearKey.setOnClickListener {
            binding.etApiKey.setText("")
            quotaPrefs.clearApiKey()
            binding.tilApiKey.error      = null
            binding.tilApiKey.helperText = null
        }

        binding.tvGetApiKey.setOnClickListener {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    android.net.Uri.parse("https://the-odds-api.com/")
                )
            )
        }
    }

    private fun testAndSave(apiKey: String) {
        binding.btnSaveKey.isEnabled  = false
        binding.btnSaveKey.text       = "⏳ Testing..."
        binding.tilApiKey.error       = null
        binding.tilApiKey.helperText  = "🔄 Checking key..."

        lifecycleScope.launch {
            val responseCode = withContext(Dispatchers.IO) {
                checkKey(apiKey)
            }

            binding.btnSaveKey.isEnabled = true
            binding.btnSaveKey.text      = "✅ Save Key and Start"

            // 🔍 Show exact response for debugging!
            when (responseCode) {
                200 -> {
                    binding.tilApiKey.helperText = "✅ Key valid! Loading..."
                    binding.tilApiKey.error      = null
                    saveAndProceed(apiKey)
                }
                401 -> {
                    binding.tilApiKey.error =
                        "❌ Error 401 - Key Invalid! Check dashboard!"
                }
                403 -> {
                    binding.tilApiKey.error =
                        "❌ Error 403 - Access denied!"
                }
                429 -> {
                    // ✅ 429 means key works but too many requests!
                    // Save and proceed anyway!
                    binding.tilApiKey.helperText = "⚠️ Rate limited but key valid!"
                    saveAndProceed(apiKey)
                }
                0 -> {
                    // ⚠️ Network error - save anyway!
                    binding.tilApiKey.helperText = "⚠️ No internet - saved anyway!"
                    saveAndProceed(apiKey)
                }
                else -> {
                    // ⚠️ Unknown - save anyway and try!
                    binding.tilApiKey.helperText =
                        "⚠️ Code $responseCode - Saving anyway!"
                    saveAndProceed(apiKey)
                }
            }
        }
    }

    private fun checkKey(apiKey: String): Int {
        return try {
            val url = URL(
                "https://api.the-odds-api.com/v4/sports/soccer_epl/odds" +
                "?apiKey=$apiKey" +
                "&regions=uk" +
                "&markets=h2h"
            )
            val conn = url.openConnection() as HttpsURLConnection
            conn.connectTimeout = 15000
            conn.readTimeout    = 15000
            conn.requestMethod  = "GET"
            val code = conn.responseCode
            conn.disconnect()
            code
        } catch (e: java.net.UnknownHostException) {
            0  // No internet
        } catch (e: Exception) {
            -1 // Unknown error
        }
    }

    private fun saveAndProceed(apiKey: String) {
        quotaPrefs.saveApiKey(apiKey)
        ApiClient.setApiKey(apiKey)
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
