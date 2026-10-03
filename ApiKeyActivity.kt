package com.melvin.predictor.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.melvin.predictor.api.ApiClient
import com.melvin.predictor.databinding.ActivityApiKeyBinding
import com.melvin.predictor.utils.QuotaPreferences

class ApiKeyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityApiKeyBinding
    private lateinit var quotaPrefs: QuotaPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding      = ActivityApiKeyBinding.inflate(layoutInflater)
        quotaPrefs   = QuotaPreferences(this)
        setContentView(binding.root)

        setupUI()
    }

    private fun setupUI() {

        // 📖 Load existing key if already saved
        val existingKey = quotaPrefs.getApiKey()
        if (existingKey.isNotEmpty()) {
            binding.etApiKey.setText(existingKey)
        }

        // ✅ Save button clicked
        binding.btnSaveKey.setOnClickListener {
            val enteredKey = binding.etApiKey.text.toString().trim()

            when {
                // ❌ Empty key
                enteredKey.isEmpty() -> {
                    binding.tilApiKey.error = "Please enter your API key!"
                }

                // ❌ Key too short
                enteredKey.length < 20 -> {
                    binding.tilApiKey.error = "API key looks too short!"
                }

                // ✅ Valid key!
                else -> {
                    saveAndProceed(enteredKey)
                }
            }
        }

        // 🔗 Get API Key link
        binding.tvGetApiKey.setOnClickListener {
            val intent = Intent(
                android.content.Intent.ACTION_VIEW,
                android.net.Uri.parse("https://the-odds-api.com/")
            )
            startActivity(intent)
        }

        // 🗑️ Clear key button
        binding.btnClearKey.setOnClickListener {
            binding.etApiKey.setText("")
            quotaPrefs.clearApiKey()
            binding.tilApiKey.error = null
        }
    }

    private fun saveAndProceed(apiKey: String) {
        // 💾 Save to SharedPreferences
        quotaPrefs.saveApiKey(apiKey)

        // 🔑 Set in ApiClient immediately
        ApiClient.setApiKey(apiKey)

        // ✅ Show success briefly
        binding.tilApiKey.error = null
        binding.tilApiKey.helperText = "✅ Key saved successfully!"

        // 🚀 Go to Main screen!
        startActivity(
            Intent(this, MainActivity::class.java)
        )
        finish() // ← Cant go back to key screen
    }
}
