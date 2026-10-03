package com.melvin.predictor.ui

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.melvin.predictor.R
import com.melvin.predictor.databinding.ActivityMainBinding
import com.melvin.predictor.model.ApiQuota
import com.melvin.predictor.model.QuotaStatus
import com.melvin.predictor.viewmodel.MainViewModel

class MainActivity : AppCompatActivity() {

    // 🔗 ViewBinding
    private lateinit var binding: ActivityMainBinding

    // 🧠 ViewModel
    private val viewModel: MainViewModel by viewModels()

    // 📋 Adapter
    private lateinit var matchAdapter: MatchAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupRefreshButton()
        observeViewModel()
    }

    // ─────────────────────────────────────
    // 📋 Setup RecyclerView
    // ─────────────────────────────────────
    private fun setupRecyclerView() {
        matchAdapter = MatchAdapter()
        binding.rvMatches.apply {
            adapter       = matchAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
            setHasFixedSize(false)
        }
    }

    // ─────────────────────────────────────
    // 🔄 Setup Refresh Button
    // ─────────────────────────────────────
    private fun setupRefreshButton() {
        binding.btnRefresh.setOnClickListener {
            viewModel.onRefreshClicked()
        }
    }

    // ─────────────────────────────────────
    // 👁️ Observe ViewModel LiveData
    // ─────────────────────────────────────
    private fun observeViewModel() {

        // 📋 Matches
        viewModel.matches.observe(this) { matches ->
            matchAdapter.submitList(matches)
            binding.tvEmptyState.visibility =
                if (matches.isEmpty()) View.VISIBLE else View.GONE
            binding.rvMatches.visibility =
                if (matches.isEmpty()) View.GONE else View.VISIBLE
        }

        // 📊 Quota
        viewModel.quota.observe(this) { quota ->
            updateQuotaUI(quota)
        }

        // ⏳ Loading
        viewModel.isLoading.observe(this) { isLoading ->
            binding.loadingLayout.visibility =
                if (isLoading) View.VISIBLE else View.GONE
            binding.btnRefresh.text =
                if (isLoading) getString(R.string.btn_refreshing)
                else getString(R.string.btn_refresh)
        }

        // ❌ Errors
        viewModel.errorMessage.observe(this) { message ->
            message?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG)
                    .setBackgroundTint(
                        ContextCompat.getColor(this, R.color.quota_critical)
                    )
                    .show()
                viewModel.clearError()
            }
        }

        // 🔄 Refresh button state
        viewModel.isRefreshEnabled.observe(this) { enabled ->
            binding.btnRefresh.isEnabled = enabled
            binding.btnRefresh.alpha     = if (enabled) 1.0f else 0.5f
        }

        // 🕐 Last refreshed
        viewModel.lastRefreshed.observe(this) { time ->
            binding.tvLastUpdated.text = time
        }
    }

    // ─────────────────────────────────────
    // 📊 Update Quota UI
    // ─────────────────────────────────────
    private fun updateQuotaUI(quota: ApiQuota) {

        // 🔢 Numbers
        binding.tvRequestsRemaining.text = quota.requestsRemaining.toString()
        binding.tvRequestsUsed.text      = quota.requestsUsed.toString()
        binding.tvLastCost.text          = quota.lastRequestCost.toString()

        // 📊 Progress Bar
        binding.quotaProgressBar.progress = quota.usagePercent

        // 🎨 Color based on status
        val statusColor = when (quota.statusLevel) {
            QuotaStatus.GOOD      -> R.color.quota_good
            QuotaStatus.WARNING   -> R.color.quota_warning
            QuotaStatus.CRITICAL  -> R.color.quota_critical
            QuotaStatus.EXHAUSTED -> R.color.quota_exhausted
        }

        val colorInt = ContextCompat.getColor(this, statusColor)

        // Apply color to remaining text + progress
        binding.tvRequestsRemaining.setTextColor(colorInt)
        binding.quotaProgressBar.progressTintList =
            android.content.res.ColorStateList.valueOf(colorInt)

        // 💬 Status message
        binding.tvQuotaStatus.text     = quota.statusMessage
        binding.tvQuotaStatus.setTextColor(colorInt)
    }
}
