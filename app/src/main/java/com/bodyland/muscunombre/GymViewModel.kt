package com.bodyland.muscunombre

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "gym_tracker_prefs")

class GymViewModel(private val context: Context) : ViewModel() {
    
    companion object {
        private val SESSION_COUNT_KEY = intPreferencesKey("session_count")
        private val SUBSCRIPTION_PRICE_KEY = doublePreferencesKey("subscription_price")
        private val START_DATE_KEY = stringPreferencesKey("start_date")
        private val END_DATE_KEY = stringPreferencesKey("end_date")
    }
    
    // Default dates: 1er août 2025 au 30 septembre 2026
    private val defaultStartDate = LocalDate.of(2025, 8, 1)
    private val defaultEndDate = LocalDate.of(2026, 9, 30)
    
    private val _sessionCount = MutableStateFlow(0)
    val sessionCount: StateFlow<Int> = _sessionCount.asStateFlow()
    
    private val _subscriptionPrice = MutableStateFlow(0.0)
    val subscriptionPrice: StateFlow<Double> = _subscriptionPrice.asStateFlow()
    
    private val _startDate = MutableStateFlow(defaultStartDate)
    val startDate: StateFlow<LocalDate> = _startDate.asStateFlow()
    
    private val _endDate = MutableStateFlow(defaultEndDate)
    val endDate: StateFlow<LocalDate> = _endDate.asStateFlow()
    
    init {
        loadPreferences()
    }
    
    private fun loadPreferences() {
        viewModelScope.launch {
            context.dataStore.data.collect { preferences ->
                _sessionCount.value = preferences[SESSION_COUNT_KEY] ?: 0
                _subscriptionPrice.value = preferences[SUBSCRIPTION_PRICE_KEY] ?: 0.0
                
                val startDateStr = preferences[START_DATE_KEY]
                _startDate.value = if (startDateStr != null) {
                    LocalDate.parse(startDateStr)
                } else {
                    defaultStartDate
                }
                
                val endDateStr = preferences[END_DATE_KEY]
                _endDate.value = if (endDateStr != null) {
                    LocalDate.parse(endDateStr)
                } else {
                    defaultEndDate
                }
            }
        }
    }
    
    fun incrementSession() {
        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                val currentCount = preferences[SESSION_COUNT_KEY] ?: 0
                preferences[SESSION_COUNT_KEY] = currentCount + 1
            }
        }
    }
    
    fun resetSessions() {
        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                preferences[SESSION_COUNT_KEY] = 0
            }
        }
    }
    
    fun updateSubscriptionPrice(price: Double) {
        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                preferences[SUBSCRIPTION_PRICE_KEY] = price
            }
        }
    }
    
    fun updateStartDate(date: LocalDate) {
        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                preferences[START_DATE_KEY] = date.toString()
            }
        }
    }
    
    fun updateEndDate(date: LocalDate) {
        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                preferences[END_DATE_KEY] = date.toString()
            }
        }
    }
    
    fun setDefaultPeriod() {
        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                preferences[START_DATE_KEY] = defaultStartDate.toString()
                preferences[END_DATE_KEY] = defaultEndDate.toString()
            }
        }
    }
}

class GymViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GymViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GymViewModel(context.applicationContext) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
