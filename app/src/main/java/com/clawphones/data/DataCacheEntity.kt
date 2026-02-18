package com.clawphones.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cache")
data class DataCacheEntity(
    @PrimaryKey val key: String,
    val value: String
)
