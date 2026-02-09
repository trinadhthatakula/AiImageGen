package com.ai.image.gen.data.local

import androidx.room.TypeConverter
import com.ai.image.gen.data.local.entity.RequestStatus
import com.ai.image.gen.data.local.entity.RequestType
import kotlin.time.Instant

class RoomConverters {

    // --- DateTime ---
    @TypeConverter
    fun fromTimestamp(value: Long?): Instant? {
        return value?.let { Instant.fromEpochMilliseconds(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Instant?): Long? {
        return date?.toEpochMilliseconds()
    }

    // --- Status ---
    @TypeConverter
    fun fromStatus(value: String): RequestStatus {
        return try {
            RequestStatus.valueOf(value)
        } catch (e: IllegalArgumentException) {
            RequestStatus.FAILED // Fallback
        }
    }

    @TypeConverter
    fun statusToString(status: RequestStatus): String {
        return status.name
    }

    // --- Type ---
    @TypeConverter
    fun fromType(value: String): RequestType {
        return RequestType.valueOf(value)
    }

    @TypeConverter
    fun typeToString(type: RequestType): String {
        return type.name
    }
}