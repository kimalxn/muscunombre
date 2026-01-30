package com.bodyland.muscunombre

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.bodyland.muscunombre.data.GymDatabase
import com.bodyland.muscunombre.data.GymSession
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "gym_tracker_prefs")

// Tiers de gamification
data class GamificationTier(
    val tier: Int,
    val name: String,
    val emoji: String,
    val minSessions: Int,
    val maxSessions: Int,
    val description: String
)

val TIERS = listOf(
    GamificationTier(1, "Vieux Rongeur", "🐀", 0, 10, "Tu débutes... continue !"),
    GamificationTier(2, "Mini Mouse", "🐭", 11, 25, "Tu prends le rythme !"),
    GamificationTier(3, "Knight Mouse", "🐭⚔️", 26, 50, "Un vrai guerrier !"),
    GamificationTier(4, "King Rat", "👑🐀", 51, 100, "Respect, ta majesté !"),
    GamificationTier(5, "Oonga Boonga", "🦍", 101, 200, "MODE BÊTE ACTIVÉ !"),
    GamificationTier(6, "Légende", "🏆✨", 201, Int.MAX_VALUE, "Tu es une LÉGENDE !")
)

fun getTierForSessions(count: Int): GamificationTier {
    return TIERS.find { count >= it.minSessions && count <= it.maxSessions } ?: TIERS.first()
}

fun getProgressInTier(count: Int, tier: GamificationTier): Float {
    if (tier.tier == 6) return 1f // Légende = 100%
    val range = tier.maxSessions - tier.minSessions + 1
    val progress = count - tier.minSessions + 1
    return (progress.toFloat() / range).coerceIn(0f, 1f)
}

class GymViewModel(private val context: Context) : ViewModel() {
    
    companion object {
        private val SUBSCRIPTION_PRICE_KEY = doublePreferencesKey("subscription_price")
        private val START_DATE_KEY = stringPreferencesKey("start_date")
        private val END_DATE_KEY = stringPreferencesKey("end_date")
        private val ONBOARDING_COMPLETED_KEY = booleanPreferencesKey("onboarding_completed")
    }
    
    private val database = GymDatabase.getDatabase(context)
    private val sessionDao = database.gymSessionDao()
    
    private val _subscriptionPrice = MutableStateFlow(0.0)
    val subscriptionPrice: StateFlow<Double> = _subscriptionPrice.asStateFlow()
    
    private val _startDate = MutableStateFlow<LocalDate?>(null)
    val startDate: StateFlow<LocalDate?> = _startDate.asStateFlow()
    
    private val _endDate = MutableStateFlow<LocalDate?>(null)
    val endDate: StateFlow<LocalDate?> = _endDate.asStateFlow()
    
    private val _onboardingCompleted = MutableStateFlow(false)
    val onboardingCompleted: StateFlow<Boolean> = _onboardingCompleted.asStateFlow()
    
    // Sessions from Room
    val allSessions: StateFlow<List<GymSession>> = sessionDao.getAllSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    val sessionCount: StateFlow<Int> = sessionDao.getSessionCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    
    // Sessions dans la période
    val sessionsInPeriod: StateFlow<List<GymSession>> = combine(
        _startDate, _endDate
    ) { start, end -> Pair(start, end) }
        .flatMapLatest { (start, end) ->
            if (start != null && end != null) {
                sessionDao.getSessionsInPeriod(start, end)
            } else {
                kotlinx.coroutines.flow.flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    init {
        loadPreferences()
    }
    
    private fun loadPreferences() {
        viewModelScope.launch {
            context.dataStore.data.collect { preferences ->
                _subscriptionPrice.value = preferences[SUBSCRIPTION_PRICE_KEY] ?: 0.0
                _onboardingCompleted.value = preferences[ONBOARDING_COMPLETED_KEY] ?: false
                
                val startDateStr = preferences[START_DATE_KEY]
                _startDate.value = startDateStr?.let { LocalDate.parse(it) }
                
                val endDateStr = preferences[END_DATE_KEY]
                _endDate.value = endDateStr?.let { LocalDate.parse(it) }
            }
        }
    }
    
    // Compléter l'onboarding (date de fin = date de début + 365 jours)
    fun completeOnboarding(startDate: LocalDate) {
        val endDate = startDate.plusDays(365)
        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                preferences[ONBOARDING_COMPLETED_KEY] = true
                preferences[START_DATE_KEY] = startDate.toString()
                preferences[END_DATE_KEY] = endDate.toString()
            }
        }
    }
    
    // Mettre à jour la date de début (recalcule automatiquement la date de fin)
    fun updateStartDateWithAutoEnd(date: LocalDate) {
        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                preferences[START_DATE_KEY] = date.toString()
                preferences[END_DATE_KEY] = date.plusDays(365).toString()
            }
        }
    }
    
    // Ajouter la séance du jour avec activité
    fun addTodaySession(activity: String = "Workout") {
        viewModelScope.launch {
            val today = LocalDate.now()
            val existingSession = sessionDao.getSessionByDate(today)
            if (existingSession == null) {
                sessionDao.insertSession(GymSession(date = today, activity = activity))
            }
        }
    }
    
    // Ajouter une séance à une date spécifique avec activité
    fun addSessionOnDate(date: LocalDate, activity: String = "Workout") {
        viewModelScope.launch {
            val existingSession = sessionDao.getSessionByDate(date)
            if (existingSession == null) {
                sessionDao.insertSession(GymSession(date = date, activity = activity))
            }
        }
    }
    
    // Supprimer une séance
    fun removeSessionOnDate(date: LocalDate) {
        viewModelScope.launch {
            sessionDao.deleteSessionByDate(date)
        }
    }
    
    // Toggle séance (ajoute si absente, supprime si présente)
    fun toggleSessionOnDate(date: LocalDate, activity: String = "Workout") {
        viewModelScope.launch {
            val existingSession = sessionDao.getSessionByDate(date)
            if (existingSession != null) {
                sessionDao.deleteSession(existingSession)
            } else {
                sessionDao.insertSession(GymSession(date = date, activity = activity))
            }
        }
    }
    
    // RESET COMPLET : vide la base de données
    fun resetSessions() {
        viewModelScope.launch {
            sessionDao.deleteAllSessions()
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
