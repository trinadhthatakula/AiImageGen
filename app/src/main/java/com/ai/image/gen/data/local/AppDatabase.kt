package com.ai.image.gen.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ai.image.gen.data.local.entity.GenerationRequestEntity

@Database(
    entities = [GenerationRequestEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(RoomConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun requestDao(): RequestDao
}