package com.ai.image.gen.presentation.saved


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ai.image.gen.domain.SavedImagesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class SavedImagesViewModel(
    private val repository: SavedImagesRepository
) : ViewModel() {

    private val _images = MutableStateFlow<List<File>>(emptyList())
    val images: StateFlow<List<File>> = _images.asStateFlow()

    init {
        loadImages()
    }

    fun loadImages() {
        viewModelScope.launch {
            _images.value = repository.getSavedImages()
        }
    }

    // Optional: Delete functionality
    fun deleteImage(file: File) {
        viewModelScope.launch {
            repository.deleteImage(file)
            loadImages() // Refresh list
        }
    }
}