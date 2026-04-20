package com.bodyland.muscunombre

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.bodyland.muscunombre.data.ActivityDefinition
import com.bodyland.muscunombre.data.DEFAULT_ACTIVITIES
import com.bodyland.muscunombre.data.GymDatabase
import com.bodyland.muscunombre.data.GymSession
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.roundToInt
import java.time.LocalDate

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "gym_tracker_prefs")

// Niveaux de gamification
data class GamificationTier(
    val tier: Int,
    val name: String,
    val minSessions: Int,
    val maxSessions: Int,
    val description: String,
    val colorHex: Long
)

val GamificationTier.displayLevel: Int get() = 8 - tier
val GamificationTier.displayName: String get() = "Niveau $displayLevel"

// Seuils de référence pour 365 jours (1 an)
private val BASE_PERIOD_DAYS = 365
private val BASE_THRESHOLDS = listOf(
    Triple(0, 10, 0xFF9CA3AF),
    Triple(11, 25, 0xFF60A5FA),
    Triple(26, 50, 0xFF34D399),
    Triple(51, 100, 0xFF818CF8),
    Triple(101, 175, 0xFFF59E0B),
    Triple(176, 250, 0xFFEF4444),
    Triple(251, Int.MAX_VALUE, 0xFF2563EB)
)

// Génère les tiers au pro-rata de la période
// Formule : seuil_ajusté = round(seuil_base * periodDays / 365)
fun getScaledTiers(periodDays: Int): List<GamificationTier> {
    val ratio = periodDays.toDouble() / BASE_PERIOD_DAYS
    return BASE_THRESHOLDS.mapIndexed { index, (baseMin, baseMax, color) ->
        val scaledMin = if (index == 0) 0 else (baseMin * ratio).roundToInt()
        val scaledMax = if (baseMax == Int.MAX_VALUE) Int.MAX_VALUE else (baseMax * ratio).roundToInt()
        val desc = if (scaledMax == Int.MAX_VALUE) "$scaledMin+ séances" else "$scaledMin–$scaledMax séances"
        GamificationTier(
            tier = index + 1,
            name = "Niveau ${7 - index}",
            minSessions = scaledMin,
            maxSessions = scaledMax,
            description = desc,
            colorHex = color
        )
    }
}

// Conserve TIERS comme alias pour 365 jours (rétro-compatibilité)
val TIERS = getScaledTiers(365)

fun getTierForSessions(count: Int, tiers: List<GamificationTier> = TIERS): GamificationTier {
    return tiers.find { count >= it.minSessions && count <= it.maxSessions } ?: tiers.first()
}

fun getProgressInTier(count: Int, tier: GamificationTier): Float {
    if (tier.tier == 7) return 1f // Niveau max = 100%
    val range = tier.maxSessions - tier.minSessions + 1
    val progress = count - tier.minSessions + 1
    return (progress.toFloat() / range).coerceIn(0f, 1f)
}

class GymViewModel(private val context: Context) : ViewModel() {
    
    companion object {
        // Legacy price keys (for migration from v1)
        private val GYMLIB_PRICE_KEY = doublePreferencesKey("gymlib_price")
        private val RUNNING_PRICE_KEY = doublePreferencesKey("running_price")
        private val WORKOUT_PRICE_KEY = doublePreferencesKey("workout_price")
        
        private val ACTIVITIES_KEY = stringPreferencesKey("activities_json")
        private val START_DATE_KEY = stringPreferencesKey("start_date")
        private val END_DATE_KEY = stringPreferencesKey("end_date")
        private val ONBOARDING_COMPLETED_KEY = booleanPreferencesKey("onboarding_completed")
        private val STANDALONE_NOTES_KEY = stringPreferencesKey("standalone_notes_json")
        private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
    }
    
    private val database = GymDatabase.getDatabase(context)
    private val sessionDao = database.gymSessionDao()
    
    // Activités dynamiques
    private val _activities = MutableStateFlow(DEFAULT_ACTIVITIES)
    val activities: StateFlow<List<ActivityDefinition>> = _activities.asStateFlow()
    
    // Prix total (somme de tous les prix d'activités > 0)
    val subscriptionPrice: StateFlow<Double> = _activities
        .map { list -> list.sumOf { if (it.price > 0) it.price else 0.0 } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
    
    private val _startDate = MutableStateFlow<LocalDate?>(null)
    val startDate: StateFlow<LocalDate?> = _startDate.asStateFlow()
    
    private val _endDate = MutableStateFlow<LocalDate?>(null)
    val endDate: StateFlow<LocalDate?> = _endDate.asStateFlow()
    
    private val _onboardingCompleted = MutableStateFlow(false)
    val onboardingCompleted: StateFlow<Boolean> = _onboardingCompleted.asStateFlow()
    
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // Theme mode: "system", "light", "dark"
    private val _themeMode = MutableStateFlow("system")
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()
    
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

    // Dates ayant une note indépendante (sans séance)
    val datesWithStandaloneNotes: StateFlow<Set<LocalDate>> = context.dataStore.data
        .map { prefs ->
            val json = prefs[STANDALONE_NOTES_KEY] ?: return@map emptySet()
            try {
                val obj = org.json.JSONObject(json)
                obj.keys().asSequence()
                    .filter { obj.getString(it).isNotEmpty() }
                    .mapNotNull { runCatching { LocalDate.parse(it) }.getOrNull() }
                    .toSet()
            } catch (e: Exception) { emptySet() }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    init {
        loadPreferences()
    }
    
    private fun activitiesToJson(activities: List<ActivityDefinition>): String {
        return JSONArray().apply {
            activities.forEach { act ->
                put(JSONObject().apply {
                    put("name", act.name)
                    put("emoji", act.emoji)
                    put("price", act.price)
                })
            }
        }.toString()
    }
    
    private fun jsonToActivities(jsonStr: String): List<ActivityDefinition> {
        val array = JSONArray(jsonStr)
        return (0 until array.length()).map { i ->
            val obj = array.getJSONObject(i)
            ActivityDefinition(
                name = obj.getString("name"),
                emoji = obj.getString("emoji"),
                price = obj.optDouble("price", 0.0)
            )
        }
    }
    
    private fun loadPreferences() {
        viewModelScope.launch {
            context.dataStore.data.collect { preferences ->
                _onboardingCompleted.value = preferences[ONBOARDING_COMPLETED_KEY] ?: false
                _themeMode.value = preferences[THEME_MODE_KEY] ?: "system"
                
                val startDateStr = preferences[START_DATE_KEY]
                _startDate.value = startDateStr?.let { LocalDate.parse(it) }
                
                val endDateStr = preferences[END_DATE_KEY]
                _endDate.value = endDateStr?.let { LocalDate.parse(it) }
                
                // Load activities (with migration from legacy format)
                val activitiesJson = preferences[ACTIVITIES_KEY]
                if (activitiesJson != null) {
                    _activities.value = jsonToActivities(activitiesJson)
                } else {
                    // Migration: check for legacy price keys
                    val gymlibPrice = preferences[GYMLIB_PRICE_KEY] ?: 0.0
                    val workoutPrice = preferences[WORKOUT_PRICE_KEY] ?: 0.0
                    val runningPrice = preferences[RUNNING_PRICE_KEY] ?: 0.0
                    
                    if (gymlibPrice > 0 || workoutPrice > 0 || runningPrice > 0) {
                        val perGymlib = if (gymlibPrice > 0) gymlibPrice / 3.0 else 0.0
                        val migratedActivities = listOf(
                            ActivityDefinition("Dynamo", "🚴", perGymlib),
                            ActivityDefinition("Circuit Training", "💪", perGymlib),
                            ActivityDefinition("Cardio Boxing", "🥊", perGymlib),
                            ActivityDefinition("Workout", "🏋️", workoutPrice),
                            ActivityDefinition("Running", "👟", runningPrice),
                            ActivityDefinition("Autres", "➕", 0.0)
                        )
                        _activities.value = migratedActivities
                        context.dataStore.edit { prefs ->
                            prefs[ACTIVITIES_KEY] = activitiesToJson(migratedActivities)
                        }
                    } else {
                        _activities.value = DEFAULT_ACTIVITIES
                    }
                }
                
                _isLoading.value = false
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

    // Notes indépendantes (sans session associée) — stockées en DataStore
    fun saveStandaloneNote(date: LocalDate, note: String) {
        viewModelScope.launch {
            context.dataStore.edit { prefs ->
                val existing = try { JSONObject(prefs[STANDALONE_NOTES_KEY] ?: "{}") } catch (e: Exception) { JSONObject() }
                if (note.isEmpty()) existing.remove(date.toString()) else existing.put(date.toString(), note)
                prefs[STANDALONE_NOTES_KEY] = existing.toString()
            }
        }
    }

    suspend fun getStandaloneNoteForDate(date: LocalDate): String {
        val json = context.dataStore.data.first()[STANDALONE_NOTES_KEY] ?: return ""
        return try { JSONObject(json).optString(date.toString(), "") } catch (e: Exception) { "" }
    }
    

    // Versions suspend pour usage ordonné dans les coroutines UI
    suspend fun addSessionSuspend(date: LocalDate, activity: String, confirmed: Boolean = true) {
        val existing = sessionDao.getSessionByDateAndActivity(date, activity)
        if (existing == null) {
            sessionDao.insertSession(GymSession(date = date, activity = activity, confirmed = confirmed))
        }
    }

    suspend fun removeActivitySuspend(date: LocalDate, activity: String) {
        sessionDao.deleteSessionByDateAndActivity(date, activity)
    }

    suspend fun confirmSessionSuspend(date: LocalDate, activity: String) {
        sessionDao.confirmSession(date, activity)
    }

    suspend fun getUnconfirmedSessionsForDate(date: LocalDate): List<GymSession> {
        return sessionDao.getUnconfirmedSessionsByDate(date)
    }

    suspend fun updateNoteSuspend(date: LocalDate, note: String) {
        sessionDao.updateNoteForDate(date, note)
    }

    // Obtenir la note pour une date
    suspend fun getNoteForDate(date: LocalDate): String {
        return sessionDao.getNoteForDate(date) ?: ""
    }
    
    // Exporter toutes les données en JSON (v2)
    suspend fun exportDataToJson(): String {
        val sessions = sessionDao.getAllSessions().first()
        
        val json = JSONObject().apply {
            put("version", 2)
            put("exportDate", LocalDate.now().toString())
            
            put("activities", JSONArray().apply {
                _activities.value.forEach { act ->
                    put(JSONObject().apply {
                        put("name", act.name)
                        put("emoji", act.emoji)
                        put("price", act.price)
                    })
                }
            })
            
            put("config", JSONObject().apply {
                put("startDate", _startDate.value?.toString() ?: "")
                put("endDate", _endDate.value?.toString() ?: "")
            })
            
            put("sessions", JSONArray().apply {
                sessions.forEach { session ->
                    put(JSONObject().apply {
                        put("date", session.date.toString())
                        put("activity", session.activity)
                        put("loggedAt", session.loggedAt)
                        put("confirmed", session.confirmed)
                        if (session.note.isNotEmpty()) put("note", session.note)
                    })
                }
            })
        }
        
        return json.toString(2)
    }
    
    // Importer les données depuis un JSON (v1 + v2)
    fun importDataFromJson(jsonString: String) {
        viewModelScope.launch {
            val json = JSONObject(jsonString)
            val version = json.optInt("version", 1)
            
            // Import activities
            if (version >= 2 && json.has("activities")) {
                val activitiesArray = json.getJSONArray("activities")
                val importedActivities = (0 until activitiesArray.length()).map { i ->
                    val obj = activitiesArray.getJSONObject(i)
                    ActivityDefinition(
                        name = obj.getString("name"),
                        emoji = obj.getString("emoji"),
                        price = obj.optDouble("price", 0.0)
                    )
                }
                context.dataStore.edit { it[ACTIVITIES_KEY] = activitiesToJson(importedActivities) }
            } else {
                // v1: reconstruct from legacy price keys
                val config = json.getJSONObject("config")
                val gymlibPrice = config.optDouble("gymlibPrice", 0.0)
                val workoutPrice = config.optDouble("workoutPrice", 0.0)
                val runningPrice = config.optDouble("runningPrice", 0.0)
                val perGymlib = if (gymlibPrice > 0) gymlibPrice / 3.0 else 0.0
                
                val migratedActivities = listOf(
                    ActivityDefinition("Dynamo", "🚴", perGymlib),
                    ActivityDefinition("Circuit Training", "💪", perGymlib),
                    ActivityDefinition("Cardio Boxing", "🥊", perGymlib),
                    ActivityDefinition("Workout", "🏋️", workoutPrice),
                    ActivityDefinition("Running", "👟", runningPrice),
                    ActivityDefinition("Autres", "➕", 0.0)
                )
                context.dataStore.edit { it[ACTIVITIES_KEY] = activitiesToJson(migratedActivities) }
            }
            
            // Import config
            val config = json.getJSONObject("config")
            context.dataStore.edit { preferences ->
                val startDateStr = config.optString("startDate", "")
                if (startDateStr.isNotEmpty()) {
                    preferences[START_DATE_KEY] = startDateStr
                }
                val endDateStr = config.optString("endDate", "")
                if (endDateStr.isNotEmpty()) {
                    preferences[END_DATE_KEY] = endDateStr
                }
                preferences[ONBOARDING_COMPLETED_KEY] = true
            }
            
            // Import sessions
            sessionDao.deleteAllSessions()
            val sessionsArray = json.getJSONArray("sessions")
            val sessions = (0 until sessionsArray.length()).map { i ->
                val sessionObj = sessionsArray.getJSONObject(i)
                GymSession(
                    date = LocalDate.parse(sessionObj.getString("date")),
                    activity = sessionObj.getString("activity"),
                    loggedAt = sessionObj.optLong("loggedAt", System.currentTimeMillis()),
                    note = sessionObj.optString("note", ""),
                    confirmed = sessionObj.optBoolean("confirmed", true)
                )
            }
            sessionDao.insertSessions(sessions)
        }
    }
    
    // --- Activities CRUD ---
    
    fun addActivity(activity: ActivityDefinition) {
        viewModelScope.launch {
            val updated = _activities.value + activity
            context.dataStore.edit { it[ACTIVITIES_KEY] = activitiesToJson(updated) }
        }
    }
    
    fun updateActivity(oldName: String, newActivity: ActivityDefinition) {
        viewModelScope.launch {
            val updated = _activities.value.map { 
                if (it.name == oldName) newActivity else it 
            }
            context.dataStore.edit { it[ACTIVITIES_KEY] = activitiesToJson(updated) }
        }
    }
    
    fun removeActivity(name: String) {
        viewModelScope.launch {
            val updated = _activities.value.filter { it.name != name }
            context.dataStore.edit { it[ACTIVITIES_KEY] = activitiesToJson(updated) }
        }
    }
    
    // RESET COMPLET : sessions + activités
    fun resetAllData() {
        viewModelScope.launch {
            sessionDao.deleteAllSessions()
            context.dataStore.edit { preferences ->
                preferences[ACTIVITIES_KEY] = activitiesToJson(DEFAULT_ACTIVITIES)
            }
        }
    }
    
    fun updateStartDateOnly(date: LocalDate) {
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
    
    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                preferences[THEME_MODE_KEY] = mode
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
