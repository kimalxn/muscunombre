package com.bodyland.muscunombre.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

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
    val activity: String = "Workout",
    val loggedAt: Long = System.currentTimeMillis(),
    val note: String = "",
    val confirmed: Boolean = true
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
    
    @Query("UPDATE gym_sessions SET note = :note WHERE date = :date")
    suspend fun updateNoteForDate(date: LocalDate, note: String)
    
    @Query("SELECT note FROM gym_sessions WHERE date = :date LIMIT 1")
    suspend fun getNoteForDate(date: LocalDate): String?
    
    @Query("UPDATE gym_sessions SET confirmed = 1 WHERE date = :date AND activity = :activity")
    suspend fun confirmSession(date: LocalDate, activity: String)
    
    @Query("SELECT * FROM gym_sessions WHERE date = :date AND confirmed = 0")
    suspend fun getUnconfirmedSessionsByDate(date: LocalDate): List<GymSession>
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

@Database(entities = [GymSession::class], version = 6, exportSchema = false)
@TypeConverters(Converters::class)
abstract class GymDatabase : RoomDatabase() {
    abstract fun gymSessionDao(): GymSessionDao
    
    companion object {
        @Volatile
        private var INSTANCE: GymDatabase? = null
        
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE gym_sessions ADD COLUMN loggedAt INTEGER NOT NULL DEFAULT 0")
            }
        }
        
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE gym_sessions ADD COLUMN note TEXT NOT NULL DEFAULT ''")
            }
        }
        
        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE gym_sessions ADD COLUMN confirmed INTEGER NOT NULL DEFAULT 1")
            }
        }
        
        fun getDatabase(context: android.content.Context): GymDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GymDatabase::class.java,
                    "gym_database"
                )
                .addMigrations(MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
