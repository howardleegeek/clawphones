package com.clawphones.data

import androidx.room.Database
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase

// Local cache entity for data
@Entity(tableName = "cache")
data class DataCacheEntity(
  @PrimaryKey val key: String,
  val value: String
)

@Dao
interface CacheDao {
  @Query("SELECT value FROM cache WHERE key = :key LIMIT 1")
  suspend fun getValue(key: String): String?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(cache: DataCacheEntity)
}

@Database(entities = [DataCacheEntity::class], version = 1, exportSchema = false)
abstract class RepositoryDatabase : RoomDatabase() {
  abstract fun cacheDao(): CacheDao
}
