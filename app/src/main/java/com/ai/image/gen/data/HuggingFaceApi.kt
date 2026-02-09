package com.ai.image.gen.data

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface HuggingFaceApi {

    // We point to the specific model's endpoint
    @POST("{provider}/models/stabilityai/stable-diffusion-xl-base-1.0")
    suspend fun generateImage(
        @Header("Authorization") token: String,
        @Body inputs: RequestBody,
        @Path("provider") provider: String = "hf-inference"
    ): ResponseBody
}