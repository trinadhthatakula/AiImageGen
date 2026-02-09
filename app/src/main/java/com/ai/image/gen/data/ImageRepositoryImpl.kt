package com.ai.image.gen.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.ai.image.gen.BuildConfig
import com.ai.image.gen.data.local.RequestDao
import com.ai.image.gen.data.local.entity.GenerationRequestEntity
import com.ai.image.gen.data.local.entity.RequestStatus
import com.ai.image.gen.data.local.entity.RequestType
import com.ai.image.gen.data.worker.ImageEditWorker
import com.ai.image.gen.domain.ImageGenerationResult
import com.ai.image.gen.domain.ImageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID
import kotlin.time.Clock

class ImageRepositoryImpl(
    private val context: Context,
    private val api: HuggingFaceApi,
    private val workManager: WorkManager,
    private val dao: RequestDao // Inject DAO
) : ImageRepository {

    override fun generateImage(prompt: String): Flow<ImageGenerationResult> = flow {
        emit(ImageGenerationResult.Loading)

        // 1. Create Request ID & Entity
        val requestId = UUID.randomUUID().toString()
        val requestEntity = GenerationRequestEntity(
            id = requestId,
            prompt = prompt,
            type = RequestType.TEXT_TO_IMAGE,
            status = RequestStatus.RUNNING, // T2I starts immediately
            createdAt = Clock.System.now()
        )

        try {
            // 2. Insert into DB
            dao.insertRequest(requestEntity)

            val jsonBody = JSONObject().put("inputs", prompt).toString()
            val requestBody = jsonBody.toRequestBody("application/json".toMediaTypeOrNull())

            val responseBody = api.generateImage(
                token = "Bearer ${BuildConfig.HF_TOKEN}",
                inputs = requestBody
            )

            val bytes = responseBody.bytes()
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

            if (bitmap != null) {
                // 3. Save to Disk (Required for History Tab)
                val savedPath = saveT2IToStorage(bitmap)

                // 4. Update DB Success
                dao.markSuccess(requestId, savedPath)
                emit(ImageGenerationResult.Success(bitmap))
            } else {
                val errorMsg = String(bytes)
                dao.markFailed(requestId, errorMsg)
                emit(ImageGenerationResult.Error("Failed to decode image. $errorMsg"))
            }

        } catch (e: Exception) {
            val errorMessage = when (e) {
                is IOException -> "Network Error"
                is retrofit2.HttpException -> "Server Error: ${e.code()}"
                else -> e.localizedMessage ?: "Unknown Error"
            }
            // 5. Update DB Failure
            dao.markFailed(requestId, errorMessage)
            emit(ImageGenerationResult.Error(errorMessage, e))
        }
    }.flowOn(Dispatchers.IO)

    override fun scheduleImageEdit(prompt: String, imageUri: String) {
        // 1. Create Request ID
        val requestId = UUID.randomUUID().toString()

        // 2. Create Entity
        val requestEntity = GenerationRequestEntity(
            id = requestId,
            prompt = prompt,
            type = RequestType.IMAGE_TO_IMAGE,
            status = RequestStatus.QUEUED,
            createdAt = Clock.System.now()
        )

        kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
            dao.insertRequest(requestEntity)

            val inputData = workDataOf(
                ImageEditWorker.KEY_REQUEST_ID to requestId, // Pass ID
                ImageEditWorker.KEY_PROMPT to prompt,
                ImageEditWorker.KEY_IMAGE_URI to imageUri
            )

            val editWorkRequest = OneTimeWorkRequestBuilder<ImageEditWorker>()
                .setInputData(inputData)
                .build()

            workManager.enqueue(editWorkRequest)
        }
    }

    private fun saveT2IToStorage(bitmap: Bitmap): String {
        val filename = "generated_${System.currentTimeMillis()}.jpg"
        val file = File(context.filesDir, filename)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
        }
        return file.absolutePath
    }
}