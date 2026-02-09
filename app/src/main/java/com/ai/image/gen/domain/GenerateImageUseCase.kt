package com.ai.image.gen.domain

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.retryWhen
import retrofit2.HttpException
import java.io.IOException

/**
 * Encapsulates the logic of generating an image, including the
 * specific business rule of handling "Cold Starts" (503 errors)
 * from the Inference API.
 */
class GenerateImageUseCase(
    private val repository: ImageRepository
) {
    operator fun invoke(prompt: String): Flow<ImageGenerationResult> {
        return repository.generateImage(prompt)
            .retryWhen { cause, attempt ->
                // If the model is loading (503), we wait and retry.
                // If it's a network glitch (IOException), we retry once or twice.

                if (attempt >= 3) return@retryWhen false // Give up after 3 retries (approx 15-20s)

                val shouldRetry = when (cause) {
                    is HttpException -> cause.code() == 503 // Model is loading
                    is IOException -> true // Network blip
                    else -> false
                }

                if (shouldRetry) {
                    // Exponential Backoff: 1s, 2s, 4s
                    val delayTime = 1000L * (1L shl attempt.toInt())
                    delay(delayTime)
                    return@retryWhen true
                } else {
                    return@retryWhen false
                }
            }
    }
}