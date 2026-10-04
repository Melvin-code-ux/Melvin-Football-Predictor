package com.melvin.predictor.ui

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
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

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var matchAdapter: MatchAdapter
    private var isSpinnerReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupRecyclerView()
        setupButtons()
        setupLeagueSpinner()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        matchAdapter = MatchAdapter()
        binding.rvMatches.apply {
            adapter = matchAdapter
            layoutManager = LinearLayoutManager(
                this@MainActivity
            )
            setHasFixedSize(false)
        }
    }

    private fun setupButtons() {
        binding.btnRefresh.setOnClickListener {
            viewModel.onRefreshClicked()
        }
    }

    private fun setupLeagueSpinner() {
        viewModel.leagueNames.observe(this) { names ->
            val adapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item,
                names
            ).apply {
                setDropDownViewResource(
                    android.R.layout.simple_spinner_dropdown_item
                )
            }
            binding.spinnerLeague.adapter = adapter
            binding.spinnerLeague
                .onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        if (isSpinnerReady) {
                            viewModel.onLeagueSelected(
                                position
                            )
                        }
                        isSpinnerReady = true
                    }
                    override fun onNothingSelected(
                        parent: AdapterView<*>?
                    ) {}
                }
        }
    }

    private fun observeViewModel() {
        viewModel.matches.observe(this) { matches ->
            matchAdapter.submitList(matches)
            binding.tvEmptyState.visibility =
                if (matches.isEmpty()) View.VISIBLE
                else View.GONE
            binding.rvMatches.visibility =
                if (matches.isEmpty()) View.GONE
                else View.VISIBLE
        }
        viewModel.quota.observe(this) { quota ->
            updateQuotaUI(quota)
        }
        viewModel.isLoading.observe(this) { isLoading ->
            binding.loadingLayout.visibility =
                if (isLoading) View.VISIBLE else View.GONE
            binding.btnRefresh.text =
                if (isLoading) "⏳ Loading..."
                else "🔄 Refresh Matches"
        }
        viewModel.errorMessage.observe(this) { message ->
            message?.let {
                Snackbar.make(
                    binding.root,
                    it,
                    Snackbar.LENGTH_LONG
                ).setBackgroundTint(
                    ContextCompat.getColor(
                        this,
                        R.color.quota_critical
                    )
                ).show()
                viewModel.clearError()
            }
        }
        viewModel.isRefreshEnabled.observe(this) { enabled ->
            binding.btnRefresh.isEnabled = enabled
            binding.btnRefresh.alpha =
                if (enabled) 1.0f else 0.5f
        }
        viewModel.lastRefreshed.observe(this) { time ->
            binding.tvLastUpdated.text = time
        }
    }

    private fun updateQuotaUI(quota: ApiQuota) {
        binding.tvRequestsRemaining.text =
            quota.requestsRemaining.toString()
        binding.tvRequestsUsed.text =
            quota.requestsUsed.toString()
        binding.tvLastCost.text =
            quota.lastRequestCost.toString()
        binding.quotaProgressBar.progress =
            quota.usagePercent
        val colorRes = when (quota.statusLevel) {
            QuotaStatus.GOOD      -> R.color.quota_good
            QuotaStatus.WARNING   -> R.color.quota_warning
            QuotaStatus.CRITICAL  -> R.color.quota_critical
            QuotaStatus.EXHAUSTED -> R.color.quota_exhausted
        }
        val color = ContextCompat.getColor(this, colorRes)
        binding.tvRequestsRemaining.setTextColor(color)
        binding.quotaProgressBar.progressTintList =
            android.content.res.ColorStateList.valueOf(color)
        binding.tvQuotaStatus.text = quota.statusMessage
        binding.tvQuotaStatus.setTextColor(color)
    }
}
