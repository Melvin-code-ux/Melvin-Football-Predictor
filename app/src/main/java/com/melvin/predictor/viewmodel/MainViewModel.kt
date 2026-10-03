package com.melvin.predictor.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.melvin.predictor.model.ApiQuota
import com.melvin.predictor.model.Match
import com.melvin.predictor.model.QuotaStatus
import com.melvin.predictor.repository.OddsRepository
import com.melvin.predictor.repository.ResultState
import com.melvin.predictor.utils.QuotaPreferences
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val quotaPrefs = QuotaPreferences(application)
    private val repository = OddsRepository(quotaPrefs)

    private val _matches            = MutableLiveData<List<Match>>()
    val matches: LiveData<List<Match>> = _matches

    private val _quota              = MutableLiveData<ApiQuota>()
    val quota: LiveData<ApiQuota>   = _quota

    private val _isLoading          = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage       = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _isQuotaExhausted   = MutableLiveData<Boolean>()
    val isQuotaExhausted: LiveData<Boolean> = _isQuotaExhausted

    private val _lastRefreshed      = MutableLiveData<String>()
    val lastRefreshed: LiveData<String> = _lastRefreshed

    private val _isRefreshEnabled   = MutableLiveData(true)
    val isRefreshEnabled: LiveData<Boolean> = _isRefreshEnabled

    init {
        loadCachedQuota()
        fetchOdds()
    }

    private fun loadCachedQuota() {
        repository.getCachedQuota()?.let {
            _quota.value = it
            _isRefreshEnabled.value = it.requestsRemaining > 0
        }
    }

    fun fetchOdds() {
        viewModelScope.launch {
            _isLoading.value    = true
            _errorMessage.value = null

            when (val result = repository.getFootballOdds()) {
                is ResultState.Success -> {
                    _matches.value          = result.data
                    _quota.value            = result.quota
                    _isQuotaExhausted.value = false
                    _lastRefreshed.value    = getCurrentTime()
                    _isRefreshEnabled.value = result.quota.requestsRemaining > 0
                }
                is ResultState.Error -> {
                    _errorMessage.value = result.message
                }
                is ResultState.QuotaExhausted -> {
                    _isQuotaExhausted.value = true
                    _isRefreshEnabled.value = false
                    _errorMessage.value     = "⛔ Quota exhausted! Resets next month."
                }
                is ResultState.Loading -> Unit
            }
            _isLoading.value = false
        }
    }

    fun onRefreshClicked() {
        if (_isRefreshEnabled.value == true && _isLoading.value == false) {
            fetchOdds()
        }
    }

    fun clearError() { _errorMessage.value = null }

    fun getQuotaColorRes(): Int {
        return when (_quota.value?.statusLevel) {
            QuotaStatus.GOOD      -> android.R.color.holo_green_light
            QuotaStatus.WARNING   -> android.R.color.holo_orange_light
            QuotaStatus.CRITICAL  -> android.R.color.holo_red_light
            QuotaStatus.EXHAUSTED -> android.R.color.holo_red_dark
            null                  -> android.R.color.holo_green_light
        }
    }

    private fun getCurrentTime(): String {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return "Last updated: ${sdf.format(Date())}"
    }
}
