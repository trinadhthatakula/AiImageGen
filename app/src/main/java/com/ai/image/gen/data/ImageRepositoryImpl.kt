package com.ai.image.gen.data

import android.graphics.BitmapFactory
import com.ai.image.gen.domain.ImageGenerationResult
import com.ai.image.gen.domain.ImageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

import com.ai.image.gen.BuildConfig

class ImageRepositoryImpl(
    private val api: HuggingFaceApi
) : ImageRepository {

    override fun generateImage(prompt: String): Flow<ImageGenerationResult> = flow {
        emit(ImageGenerationResult.Loading)

        try {
            // 1. Prepare the raw JSON body manually to avoid complex DTOs for a single string
            val jsonBody = JSONObject().put("inputs", prompt).toString()
            val requestBody = jsonBody.toRequestBody("application/json".toMediaTypeOrNull())

            // 2. Make the call using the Token from BuildConfig
            val responseBody = api.generateImage(
                token = "Bearer ${BuildConfig.HF_TOKEN}",
                inputs = requestBody
            )

            // 3. Convert Bytes to Bitmap
            val bytes = responseBody.bytes()
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

            if (bitmap != null) {
                emit(ImageGenerationResult.Success(bitmap))
            } else {
                // Sometimes the API returns a JSON error (like 503) but Retrofit treats it as "Success"
                // if the code is 200-299. If decoding fails, it might be text data.
                val errorResponse = String(bytes)
                emit(ImageGenerationResult.Error("Failed to decode image. Response: $errorResponse"))
            }

        } catch (e: Exception) {
            // Handle HTTP errors (401, 503, etc.)
            val errorMessage = when (e) {
                is IOException -> "Network Error: Check your internet connection."
                is retrofit2.HttpException -> {
                    when (e.code()) {
                        503 -> "Model is loading (Cold Start). Please try again in a moment."
                        401 -> "Unauthorized. Check your HF_TOKEN."
                        else -> "Server Error: ${e.code()}"
                    }
                }
                else -> e.localizedMessage ?: "Unknown Error"
            }
            emit(ImageGenerationResult.Error(errorMessage, e))
        }
    }.flowOn(Dispatchers.IO) // ALWAYS ensure this runs on IO thread
}