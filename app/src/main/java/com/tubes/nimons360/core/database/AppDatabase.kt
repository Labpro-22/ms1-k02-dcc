package com.tubes.nimons360.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Delete
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.lifecycle.LiveData

// ==========================================
// KODE MILIK PERSON B (Pinned Family)
// ==========================================
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

// ==========================================
// KODE MILIK PERSON C (Favorite Location - Task C4)
// ==========================================
@Entity(tableName = "favorite_locations")
data class FavoriteLocationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val label: String,
    val latitude: Double,
    val longitude: Double,
    val savedAt: Long = System.currentTimeMillis()
)

@Dao
interface FavoriteLocationDao {
    @Query("SELECT * FROM favorite_locations ORDER BY savedAt DESC")
    fun getAll(): LiveData<List<FavoriteLocationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(location: FavoriteLocationEntity)

    @Delete
    suspend fun delete(location: FavoriteLocationEntity)

    @Query("DELETE FROM favorite_locations WHERE id = :id")
    suspend fun deleteById(id: Int)
}

// ==========================================
// DATABASE UTAMA
// ==========================================
@Database(
    // Daftarkan kedua Entity di sini
    entities = [PinnedFamilyEntity::class, FavoriteLocationEntity::class],
    version = 2, // Naikkan versi ke 2 karena kita menambah tabel baru
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun pinnedFamilyDao(): PinnedFamilyDao

    // Tambahkan akses ke Dao milik Person C
    abstract fun favoriteLocationDao(): FavoriteLocationDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext, AppDatabase::class.java, "nimons_db"
                )
                    .fallbackToDestructiveMigration() // Penting: Mencegah crash jika struktur tabel berubah
                    .build().also { INSTANCE = it }
            }
    }
}