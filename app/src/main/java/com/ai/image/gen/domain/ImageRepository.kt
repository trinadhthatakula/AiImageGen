package com.ai.image.gen.domain

import kotlinx.coroutines.flow.Flow

interface ImageRepository {
    /**
     * Generates an image from a text prompt.
     * Returns a Flow to emit Loading -> Success/Error states over time.
     */
    fun generateImage(prompt: String): Flow<ImageGenerationResult>

    fun scheduleImageEdit(prompt: String, imageUri: String)

}