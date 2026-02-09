package com.ai.image.gen.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.time.Instant

@Entity(tableName = "generation_requests")
data class GenerationRequestEntity(
    @PrimaryKey val id: String, // UUID passed to WorkManager
    val prompt: String,
    val type: RequestType,
    val status: RequestStatus,
    val resultPath: String? = null, // Null until SUCCESS
    val createdAt: Instant,
    val errorMessage: String? = null // To show why it failed
)