package com.ai.image.gen.data

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface HuggingFaceApi {

    // Text to Image
    @POST("{provider}/models/stabilityai/stable-diffusion-xl-base-1.0")
    suspend fun generateImage(
        @Header("Authorization") token: String,
        @Body inputs: RequestBody,
        @Path("provider") provider: String = "hf-inference"
    ): ResponseBody

    // Image Editing
    @POST("{provider}/models/stabilityai/stable-diffusion-xl-base-1.0")
    suspend fun editImage(
        @Header("Authorization") token: String,
        @Body inputs: RequestBody, // JSON containing "inputs" (prompt) and "image" (base64)
        @Path("provider") provider: String = "hf-inference"
    ): ResponseBody

    // Image Editing for paid users only
    /*@POST("fal-ai/fast-sdxl")
    suspend fun editImage(
        @Header("Authorization") token: String,
        @Body inputs: RequestBody
    ): ResponseBody*/

}