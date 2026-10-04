package com.melvin.predictor.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.melvin.predictor.model.ApiQuota
import com.melvin.predictor.model.FootballLeagues
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

    // ✅ No QuotaPreferences needed for key!
    private val quotaPrefs = QuotaPreferences(application)
    private val repository = OddsRepository(quotaPrefs)

    private val _matches             = MutableLiveData<List<Match>>()
    val matches: LiveData<List<Match>> = _matches

    private val _quota               = MutableLiveData<ApiQuota>()
    val quota: LiveData<ApiQuota>    = _quota

    private val _isLoading           = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage        = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _isQuotaExhausted    = MutableLiveData<Boolean>()
    val isQuotaExhausted: LiveData<Boolean> = _isQuotaExhausted

    private val _lastRefreshed       = MutableLiveData<String>()
    val lastRefreshed: LiveData<String> = _lastRefreshed

    private val _isRefreshEnabled    = MutableLiveData(true)
    val isRefreshEnabled: LiveData<Boolean> = _isRefreshEnabled

    private val _selectedLeague      = MutableLiveData("soccer_epl")
    val selectedLeague: LiveData<String> = _selectedLeague

    private val _leagueNames         = MutableLiveData<List<String>>()
    val leagueNames: LiveData<List<String>> = _leagueNames

    private val _leagueKeys          = MutableLiveData<List<String>>()
    val leagueKeys: LiveData<List<String>> = _leagueKeys

    init {
        loadCachedQuota()
        loadLeagues()
        fetchOdds()
    }

    private fun loadLeagues() {
        _leagueNames.value = FootballLeagues.ALL_LEAGUES.values.toList()
        _leagueKeys.value  = FootballLeagues.ALL_LEAGUES.keys.toList()
    }

    private fun loadCachedQuota() {
        repository.getCachedQuota()?.let {
            _quota.value            = it
            _isRefreshEnabled.value = it.requestsRemaining > 0
        }
    }

    fun onLeagueSelected(position: Int) {
        val keys = _leagueKeys.value ?: return
        if (position < keys.size) {
            _selectedLeague.value = keys[position]
            fetchOdds()
        }
    }

    fun fetchOdds() {
        viewModelScope.launch {
            _isLoading.value    = true
            _errorMessage.value = null

            when (val result = repository.getOddsForLeague(
                _selectedLeague.value ?: "soccer_epl"
            )) {
                is ResultState.Success -> {
                    _matches.value          = result.data
                    _quota.value            = result.quota
                    _isQuotaExhausted.value = false
                    _lastRefreshed.value    = getTime()
                    _isRefreshEnabled.value =
                        result.quota.requestsRemaining > 0
                }
                is ResultState.Error -> {
                    _errorMessage.value = result.message
                }
                is ResultState.QuotaExhausted -> {
                    _isQuotaExhausted.value = true
                    _isRefreshEnabled.value = false
                    _errorMessage.value     = "⛔ Quota exhausted!"
                }
                else -> Unit
            }
            _isLoading.value = false
        }
    }

    fun onRefreshClicked() {
        if (_isRefreshEnabled.value == true &&
            _isLoading.value == false) {
            fetchOdds()
        }
    }

    fun clearError() { _errorMessage.value = null }

    private fun getTime(): String =
        "Last updated: ${
            SimpleDateFormat(
                "HH:mm:ss",
                Locale.getDefault()
            ).format(Date())
        }"
}
