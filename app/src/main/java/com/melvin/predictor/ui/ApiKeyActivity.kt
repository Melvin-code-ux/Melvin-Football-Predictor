package com.melvin.predictor.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.melvin.predictor.api.ApiClient
import com.melvin.predictor.databinding.ActivityApiKeyBinding
import com.melvin.predictor.utils.QuotaPreferences

class ApiKeyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityApiKeyBinding
    private lateinit var quotaPrefs: QuotaPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding     = ActivityApiKeyBinding.inflate(layoutInflater)
        quotaPrefs  = QuotaPreferences(this)
        setContentView(binding.root)

        // Load existing key
        val existingKey = quotaPrefs.getApiKey()
        if (existingKey.isNotEmpty()) {
            binding.etApiKey.setText(existingKey)
        }

        binding.btnSaveKey.setOnClickListener {
            val key = binding.etApiKey.text.toString().trim()
            when {
                key.isEmpty()    -> binding.tilApiKey.error = "Please enter your API key!"
                key.length < 10  -> binding.tilApiKey.error = "API key looks invalid!"
                else             -> saveAndProceed(key)
            }
        }

        binding.btnClearKey.setOnClickListener {
            binding.etApiKey.setText("")
            quotaPrefs.clearApiKey()
            binding.tilApiKey.error = null
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

    private fun saveAndProceed(apiKey: String) {
        quotaPrefs.saveApiKey(apiKey)
        ApiClient.setApiKey(apiKey)
        binding.tilApiKey.error      = null
        binding.tilApiKey.helperText = "✅ Key saved!"
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
