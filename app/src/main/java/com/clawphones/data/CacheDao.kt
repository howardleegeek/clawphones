package com.clawphones.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CacheDao {
  @Query("SELECT value FROM cache WHERE key = :key LIMIT 1")
  suspend fun getValue(key: String): String?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(entry: DataCacheEntity)
}
