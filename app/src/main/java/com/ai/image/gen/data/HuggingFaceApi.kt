package com.ai.image.gen.data

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

interface HuggingFaceApi {

    // We point to the specific model's endpoint
    @POST("models/Tongyi-MAI/Z-Image-Turbo")
    // Ensure we don't crash trying to parse JSON when the success response is a JPG
    suspend fun generateImage(
        @Header("Authorization") token: String,
        @Body inputs: RequestBody,
        // We can force the provider via query param as per your Python example
        @Query("provider") provider: String = "fal-ai"
    ): ResponseBody
}