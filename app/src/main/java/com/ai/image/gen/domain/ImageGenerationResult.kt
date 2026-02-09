package com.ai.image.gen.domain

import android.graphics.Bitmap

/**
 * A sealed interface representing the strict states of our generation process.
 * We do not use "null" for errors; we use explicit types.
 */
sealed interface ImageGenerationResult {
    data object Loading : ImageGenerationResult
    data class Success(val image: Bitmap) : ImageGenerationResult
    data class Error(val message: String, val cause: Throwable? = null) : ImageGenerationResult
}