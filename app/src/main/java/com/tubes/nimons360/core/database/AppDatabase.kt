package com.tubes.nimons360.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Entity(tableName = "pinned_families")
data class PinnedFamilyEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val iconUrl: String
)

@Dao
interface PinnedFamilyDao {
    @Query("SELECT * FROM pinned_families")
    fun getAll(): List<PinnedFamilyEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(family: PinnedFamilyEntity)
}

@Database(
    entities = [PinnedFamilyEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun pinnedFamilyDao(): PinnedFamilyDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext, AppDatabase::class.java, "nimons_db"
                ).build().also { INSTANCE = it }
            }
    }
}
