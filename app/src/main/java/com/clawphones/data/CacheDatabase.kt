package com.clawphones.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [DataCacheEntity::class], version = 1)
abstract class CacheDatabase : RoomDatabase() {
  abstract fun cacheDao(): CacheDao
}
