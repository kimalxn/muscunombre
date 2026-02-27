package com.bodyland.muscunombre.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

// Définition d'une activité (nom, emoji, prix annuel)
data class ActivityDefinition(
    val name: String,
    val emoji: String,
    val price: Double = 0.0
)

// Activités par défaut
val DEFAULT_ACTIVITIES = listOf(
    ActivityDefinition("Dynamo", "🚴", 0.0),
    ActivityDefinition("Circuit Training", "💪", 0.0),
    ActivityDefinition("Cardio Boxing", "🥊", 0.0),
    ActivityDefinition("Workout", "🏋️", 0.0),
    ActivityDefinition("Running", "👟", 0.0),
    ActivityDefinition("Autres", "➕", 0.0)
)

fun getActivityEmoji(activity: String, activities: List<ActivityDefinition> = emptyList()): String {
    return activities.find { it.name == activity }?.emoji
        ?: DEFAULT_ACTIVITIES.find { it.name == activity }?.emoji
        ?: "💪"
}

// Chaque entrée = 1 activité sur 1 date (permet plusieurs activités par jour)
@Entity(tableName = "gym_sessions")
data class GymSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: LocalDate,
    val activity: String = "Workout"
)

@Dao
interface GymSessionDao {
    @Query("SELECT * FROM gym_sessions ORDER BY date DESC")
    fun getAllSessions(): Flow<List<GymSession>>
    
    @Query("SELECT * FROM gym_sessions WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun getSessionsInPeriod(startDate: LocalDate, endDate: LocalDate): Flow<List<GymSession>>
    
    @Query("SELECT COUNT(*) FROM gym_sessions")
    fun getSessionCount(): Flow<Int>
    
    @Query("SELECT COUNT(*) FROM gym_sessions WHERE date BETWEEN :startDate AND :endDate")
    fun getSessionCountInPeriod(startDate: LocalDate, endDate: LocalDate): Flow<Int>
    
    @Query("SELECT * FROM gym_sessions WHERE date = :date")
    fun getSessionsByDate(date: LocalDate): Flow<List<GymSession>>
    
    @Query("SELECT * FROM gym_sessions WHERE date = :date")
    suspend fun getSessionsByDateSync(date: LocalDate): List<GymSession>
    
    @Query("SELECT * FROM gym_sessions WHERE date = :date AND activity = :activity LIMIT 1")
    suspend fun getSessionByDateAndActivity(date: LocalDate, activity: String): GymSession?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: GymSession)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSessions(sessions: List<GymSession>)
    
    @Delete
    suspend fun deleteSession(session: GymSession)
    
    @Query("DELETE FROM gym_sessions")
    suspend fun deleteAllSessions()
    
    @Query("DELETE FROM gym_sessions WHERE date = :date")
    suspend fun deleteSessionByDate(date: LocalDate)
    
    @Query("DELETE FROM gym_sessions WHERE date = :date AND activity = :activity")
    suspend fun deleteSessionByDateAndActivity(date: LocalDate, activity: String)
}

class Converters {
    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? {
        return date?.toString()
    }
    
    @TypeConverter
    fun toLocalDate(dateString: String?): LocalDate? {
        return dateString?.let { LocalDate.parse(it) }
    }
}

@Database(entities = [GymSession::class], version = 3, exportSchema = false)
@TypeConverters(Converters::class)
abstract class GymDatabase : RoomDatabase() {
    abstract fun gymSessionDao(): GymSessionDao
    
    companion object {
        @Volatile
        private var INSTANCE: GymDatabase? = null
        
        fun getDatabase(context: android.content.Context): GymDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GymDatabase::class.java,
                    "gym_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
