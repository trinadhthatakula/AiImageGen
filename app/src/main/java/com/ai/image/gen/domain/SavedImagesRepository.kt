package com.ai.image.gen.domain

import java.io.File

interface SavedImagesRepository {
    suspend fun getSavedImages(): List<File>
    suspend fun deleteImage(file: File)
}
