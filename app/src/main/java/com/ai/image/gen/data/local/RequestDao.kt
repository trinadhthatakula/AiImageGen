package com.ai.image.gen.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ai.image.gen.data.local.entity.GenerationRequestEntity
import com.ai.image.gen.data.local.entity.RequestStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface RequestDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequest(request: GenerationRequestEntity)

    // Update status to RUNNING
    @Query("UPDATE generation_requests SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: RequestStatus)

    // Update status to SUCCESS with file path
    @Query("UPDATE generation_requests SET status = 'SUCCESS', resultPath = :path WHERE id = :id")
    suspend fun markSuccess(id: String, path: String)

    // Update status to FAILED with error message
    @Query("UPDATE generation_requests SET status = 'FAILED', errorMessage = :error WHERE id = :id")
    suspend fun markFailed(id: String, error: String)

    // Active Tab: Queued or Running, sorted by newest first
    @Query("SELECT * FROM generation_requests WHERE status IN ('QUEUED', 'RUNNING') ORDER BY createdAt DESC")
    fun observeActiveRequests(): Flow<List<GenerationRequestEntity>>

    // History Tab: Success or Failed, sorted by newest first
    @Query("SELECT * FROM generation_requests WHERE status IN ('SUCCESS', 'FAILED') ORDER BY createdAt DESC")
    fun observeCompletedRequests(): Flow<List<GenerationRequestEntity>>

    // For Badge Count
    @Query("SELECT COUNT(*) FROM generation_requests WHERE status IN ('QUEUED', 'RUNNING')")
    fun observeActiveCount(): Flow<Int>
}