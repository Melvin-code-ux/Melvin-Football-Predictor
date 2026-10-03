package com.melvin.predictor.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
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

        // 📖 Load existing key if saved
        val existingKey = quotaPrefs.getApiKey()
        if (existingKey.isNotEmpty()) {
            binding.etApiKey.setText(existingKey)
        }

        // ✅ Save button
        binding.btnSaveKey.setOnClickListener {
            // 🧹 Trim ALL spaces & newlines from key!
            val key = binding.etApiKey.text
                .toString()
                .trim()                    // ← removes spaces
                .replace("\n", "")         // ← removes newlines
                .replace("\r", "")         // ← removes returns
                .replace(" ", "")          // ← removes any spaces

            when {
                key.isEmpty() -> {
                    binding.tilApiKey.error = "❌ Please enter your API key!"
                }
                key.length < 10 -> {
                    binding.tilApiKey.error = "❌ Key too short! Check your key!"
                }
                else -> {
                    // ✅ Test key before saving!
                    testAndSaveKey(key)
                }
            }
        }

        // 🗑️ Clear button
        binding.btnClearKey.setOnClickListener {
            binding.etApiKey.setText("")
            quotaPrefs.clearApiKey()
            binding.tilApiKey.error      = null
            binding.tilApiKey.helperText = null
        }

        // 🔗 Get key link
        binding.tvGetApiKey.setOnClickListener {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    android.net.Uri.parse("https://the-odds-api.com/")
                )
            )
        }
    }

    // 🧪 Test key with real API call before saving!
    private fun testAndSaveKey(apiKey: String) {
        // Show loading state
        binding.btnSaveKey.isEnabled = false
        binding.btnSaveKey.text      = "⏳ Testing key..."
        binding.tilApiKey.error      = null
        binding.tilApiKey.helperText = "Testing your API key..."

        lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                testApiKey(apiKey)
            }

            // Reset button
            binding.btnSaveKey.isEnabled = true
            binding.btnSaveKey.text      = "✅ Save Key and Start"

            when (result) {
                // ✅ Key works!
                ApiTestResult.SUCCESS -> {
                    binding.tilApiKey.error      = null
                    binding.tilApiKey.helperText = "✅ Key verified!"
                    saveAndProceed(apiKey)
                }
                // ❌ Invalid key
                ApiTestResult.INVALID_KEY -> {
                    binding.tilApiKey.helperText = null
                    binding.tilApiKey.error      =
                        "❌ Invalid key! Get your key at the-odds-api.com"
                }
                // 📡 No internet
                ApiTestResult.NO_INTERNET -> {
                    binding.tilApiKey.helperText = null
                    binding.tilApiKey.error      =
                        "📡 No internet! Connect and try again!"
                }
                // ⚠️ Unknown error
                ApiTestResult.UNKNOWN_ERROR -> {
                    // Save anyway if unknown error
                    // might be API issue not key issue
                    binding.tilApiKey.error      = null
                    binding.tilApiKey.helperText = "⚠️ Saved! Could not verify."
                    saveAndProceed(apiKey)
                }
            }
        }
    }

    // 🌐 Direct API test
    private fun testApiKey(apiKey: String): ApiTestResult {
        return try {
            val url = URL(
                "https://api.the-odds-api.com/v4/sports" +
                "?apiKey=$apiKey"
            )
            val connection = url.openConnection() as HttpsURLConnection
            connection.connectTimeout = 10000
            connection.readTimeout    = 10000
            connection.requestMethod  = "GET"

            val responseCode = connection.responseCode
            connection.disconnect()

            when (responseCode) {
                200  -> ApiTestResult.SUCCESS
                401  -> ApiTestResult.INVALID_KEY
                403  -> ApiTestResult.INVALID_KEY
                else -> ApiTestResult.UNKNOWN_ERROR
            }
        } catch (e: java.net.UnknownHostException) {
            ApiTestResult.NO_INTERNET
        } catch (e: Exception) {
            ApiTestResult.UNKNOWN_ERROR
        }
    }

    // 💾 Save key and go to main screen
    private fun saveAndProceed(apiKey: String) {
        quotaPrefs.saveApiKey(apiKey)
        ApiClient.setApiKey(apiKey)
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}

// 📊 Test result states
enum class ApiTestResult {
    SUCCESS,
    INVALID_KEY,
    NO_INTERNET,
    UNKNOWN_ERROR
}
