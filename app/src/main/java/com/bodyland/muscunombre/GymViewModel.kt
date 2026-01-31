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

// Tiers de gamification avec couleurs
data class GamificationTier(
    val tier: Int,
    val name: String,
    val emoji: String,
    val minSessions: Int,
    val maxSessions: Int,
    val description: String,
    val colorHex: Long // Couleur du banner
)

val TIERS = listOf(
    GamificationTier(1, "Vieux Rongeur", "🐀💤", 0, 10, "Tu débutes... continue !", 0xFF6B7280), // Gris
    GamificationTier(2, "Mini Mouse", "🐭🍼", 11, 25, "Tu prends le rythme !", 0xFF60A5FA), // Bleu clair
    GamificationTier(3, "Knight Mouse", "🐭⚔️", 26, 50, "Un vrai guerrier !", 0xFFC0C0C0), // Argent
    GamificationTier(4, "King Rat", "🐀👑", 51, 100, "Respect, ta majesté !", 0xFF8B5CF6), // Violet
    GamificationTier(5, "Oonga Bouna", "🦍🔥", 101, 175, "MODE BÊTE ACTIVÉ !", 0xFFF97316), // Orange
    GamificationTier(6, "Meep Meep", "🏃💨", 176, 250, "BIP BIP ! Impossible à rattraper !", 0xFF06B6D4), // Cyan
    GamificationTier(7, "Légende", "⭐", 251, Int.MAX_VALUE, "Tu es une LÉGENDE !", 0xFFFFD700) // Doré
)

fun getTierForSessions(count: Int): GamificationTier {
    return TIERS.find { count >= it.minSessions && count <= it.maxSessions } ?: TIERS.first()
}

fun getProgressInTier(count: Int, tier: GamificationTier): Float {
    if (tier.tier == 7) return 1f // Légende = 100%
    val range = tier.maxSessions - tier.minSessions + 1
    val progress = count - tier.minSessions + 1
    return (progress.toFloat() / range).coerceIn(0f, 1f)
}

class GymViewModel(private val context: Context) : ViewModel() {
    
    companion object {
        // Prix par catégorie
        private val GYMLIB_PRICE_KEY = doublePreferencesKey("gymlib_price")
        private val RUNNING_PRICE_KEY = doublePreferencesKey("running_price")
        private val WORKOUT_PRICE_KEY = doublePreferencesKey("workout_price")
        
        private val START_DATE_KEY = stringPreferencesKey("start_date")
        private val END_DATE_KEY = stringPreferencesKey("end_date")
        private val ONBOARDING_COMPLETED_KEY = booleanPreferencesKey("onboarding_completed")
    }
    
    private val database = GymDatabase.getDatabase(context)
    private val sessionDao = database.gymSessionDao()
    
    // Prix par catégorie
    private val _gymlibPrice = MutableStateFlow(0.0)
    val gymlibPrice: StateFlow<Double> = _gymlibPrice.asStateFlow()
    
    private val _runningPrice = MutableStateFlow(0.0)
    val runningPrice: StateFlow<Double> = _runningPrice.asStateFlow()
    
    private val _workoutPrice = MutableStateFlow(0.0)
    val workoutPrice: StateFlow<Double> = _workoutPrice.asStateFlow()
    
    // Prix total (somme des abonnements > 0€ seulement)
    val subscriptionPrice: StateFlow<Double> = combine(
        _gymlibPrice, _runningPrice, _workoutPrice
    ) { gymlib, running, workout -> 
        (if (gymlib > 0) gymlib else 0.0) + 
        (if (running > 0) running else 0.0) + 
        (if (workout > 0) workout else 0.0)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
    
    private val _startDate = MutableStateFlow<LocalDate?>(null)
    val startDate: StateFlow<LocalDate?> = _startDate.asStateFlow()
    
    private val _endDate = MutableStateFlow<LocalDate?>(null)
    val endDate: StateFlow<LocalDate?> = _endDate.asStateFlow()
    
    private val _onboardingCompleted = MutableStateFlow(false)
    val onboardingCompleted: StateFlow<Boolean> = _onboardingCompleted.asStateFlow()
    
    // Toutes les sessions
    val allSessions: StateFlow<List<GymSession>> = sessionDao.getAllSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    // Sessions dans la période (recalculé automatiquement quand les dates changent)
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val sessionsInPeriod: StateFlow<List<GymSession>> = combine(
        _startDate, _endDate
    ) { start, end -> Pair(start, end) }
        .flatMapLatest { (start, end) ->
            if (start != null && end != null) {
                sessionDao.getSessionsInPeriod(start, end)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    // Nombre total de séances (pour les tiers)
    val sessionCount: StateFlow<Int> = allSessions
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    
    init {
        loadPreferences()
    }
    
    private fun loadPreferences() {
        viewModelScope.launch {
            context.dataStore.data.collect { preferences ->
                _gymlibPrice.value = preferences[GYMLIB_PRICE_KEY] ?: 0.0
                _runningPrice.value = preferences[RUNNING_PRICE_KEY] ?: 0.0
                _workoutPrice.value = preferences[WORKOUT_PRICE_KEY] ?: 0.0
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
    
    // Ajouter une activité pour aujourd'hui
    fun addTodaySession(activity: String) {
        viewModelScope.launch {
            val today = LocalDate.now()
            val existing = sessionDao.getSessionByDateAndActivity(today, activity)
            if (existing == null) {
                sessionDao.insertSession(GymSession(date = today, activity = activity))
            }
        }
    }
    
    // Ajouter une activité à une date spécifique
    fun addSessionOnDate(date: LocalDate, activity: String) {
        viewModelScope.launch {
            val existing = sessionDao.getSessionByDateAndActivity(date, activity)
            if (existing == null) {
                sessionDao.insertSession(GymSession(date = date, activity = activity))
            }
        }
    }
    
    // Supprimer une activité spécifique d'une date
    fun removeActivityOnDate(date: LocalDate, activity: String) {
        viewModelScope.launch {
            sessionDao.deleteSessionByDateAndActivity(date, activity)
        }
    }
    
    // Supprimer toutes les séances d'une date
    fun removeSessionOnDate(date: LocalDate) {
        viewModelScope.launch {
            sessionDao.deleteSessionByDate(date)
        }
    }
    
    // Toggle une activité pour une date
    fun toggleActivityOnDate(date: LocalDate, activity: String) {
        viewModelScope.launch {
            val existing = sessionDao.getSessionByDateAndActivity(date, activity)
            if (existing != null) {
                sessionDao.deleteSession(existing)
            } else {
                sessionDao.insertSession(GymSession(date = date, activity = activity))
            }
        }
    }
    
    // Vérifier si une activité est déjà pointée pour une date
    suspend fun isActivityLoggedForDate(date: LocalDate, activity: String): Boolean {
        return sessionDao.getSessionByDateAndActivity(date, activity) != null
    }
    
    // Obtenir les activités d'une date
    suspend fun getActivitiesForDate(date: LocalDate): List<String> {
        return sessionDao.getSessionsByDateSync(date).map { it.activity }
    }
    
    // RESET COMPLET : vide la base de données
    fun resetSessions() {
        viewModelScope.launch {
            sessionDao.deleteAllSessions()
        }
    }
    
    // RESET COMPLET avec prix
    fun resetAllData() {
        viewModelScope.launch {
            sessionDao.deleteAllSessions()
            context.dataStore.edit { preferences ->
                preferences[GYMLIB_PRICE_KEY] = 0.0
                preferences[RUNNING_PRICE_KEY] = 0.0
                preferences[WORKOUT_PRICE_KEY] = 0.0
            }
        }
    }
    
    // Mettre à jour les prix
    fun updateGymlibPrice(price: Double) {
        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                preferences[GYMLIB_PRICE_KEY] = price
            }
        }
    }
    
    fun updateRunningPrice(price: Double) {
        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                preferences[RUNNING_PRICE_KEY] = price
            }
        }
    }
    
    fun updateWorkoutPrice(price: Double) {
        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                preferences[WORKOUT_PRICE_KEY] = price
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
