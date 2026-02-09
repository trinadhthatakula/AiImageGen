package com.ai.image.gen.data

import android.content.Context
import com.ai.image.gen.domain.SavedImagesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class SavedImagesRepositoryImpl(
    private val context: Context
) : SavedImagesRepository {

    override suspend fun getSavedImages(): List<File> = withContext(Dispatchers.IO) {
        val directory = context.filesDir
        // Filter for files starting with "edited_" or "generated_" and ending in .jpg
        // Sort by last modified (newest first)
        directory.listFiles()
            ?.filter { it.isFile && it.name.endsWith(".jpg") }
            ?.sortedByDescending { it.lastModified() }
            ?: emptyList()
    }

    override suspend fun deleteImage(file: File) = withContext(Dispatchers.IO) {
        if (file.exists()) {
            file.delete()
        }
        Unit
    }
}