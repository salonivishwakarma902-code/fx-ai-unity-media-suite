package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PhotoDao {
    @Query("SELECT * FROM photos WHERE isTrashed = 0 ORDER BY timestamp DESC")
    fun getActivePhotos(): Flow<List<PhotoEntity>>

    @Query("SELECT * FROM photos WHERE isTrashed = 1 ORDER BY timestamp DESC")
    fun getTrashedPhotos(): Flow<List<PhotoEntity>>

    @Query("SELECT COUNT(*) FROM photos")
    suspend fun getTotalPhotoCount(): Int

    @Query("SELECT * FROM photos WHERE id = :id LIMIT 1")
    suspend fun getPhotoById(id: String): PhotoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: PhotoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhotos(photos: List<PhotoEntity>)

    @Update
    suspend fun updatePhoto(photo: PhotoEntity)

    @Query("UPDATE photos SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: String, isFavorite: Boolean)

    @Query("UPDATE photos SET isTrashed = :isTrashed WHERE id = :id")
    suspend fun setTrashed(id: String, isTrashed: Boolean)

    @Query("UPDATE photos SET isSyncedToOracle = :synced, oracleRemoteId = :remoteId WHERE id = :id")
    suspend fun setSynced(id: String, synced: Boolean, remoteId: String?)

    @Query("DELETE FROM photos WHERE id = :id")
    suspend fun deletePermanently(id: String)

    @Query("DELETE FROM photos WHERE isTrashed = 1")
    suspend fun emptyTrash()
}

@Database(entities = [PhotoEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun photoDao(): PhotoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "fx_ai_unity_photos.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
