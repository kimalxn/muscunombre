package com.bodyland.muscunombre.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

// Liste des activités disponibles
val ACTIVITIES = listOf("Dynamo", "Circuit Training 1", "Circuit Training 2", "Cardio Boxing", "Workout", "Running")

@Entity(tableName = "gym_sessions")
data class GymSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: LocalDate,
    val activity: String = "Workout" // Type d'activité
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
    
    @Query("SELECT * FROM gym_sessions WHERE date = :date LIMIT 1")
    suspend fun getSessionByDate(date: LocalDate): GymSession?
    
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

@Database(entities = [GymSession::class], version = 2, exportSchema = false)
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
