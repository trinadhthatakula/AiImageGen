package com.ai.image.gen.presentation.t2i

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ai.image.gen.domain.GenerateImageUseCase
import com.ai.image.gen.domain.ImageGenerationResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class ImageViewModel(
    private val generateImageUseCase: GenerateImageUseCase
) : ViewModel() {

    // Backing property to avoid exposing mutable state
    private val _state = MutableStateFlow<ImageGenerationResult?>(null)
    val state: StateFlow<ImageGenerationResult?> = _state.asStateFlow()

    fun generateImage(prompt: String) {
        if (prompt.isBlank()) return

        viewModelScope.launch {
            generateImageUseCase(prompt)
                .onEach { result ->
                    _state.value = result
                }
                .catch { e ->
                    // This catch block handles unexpected crashes in the flow itself
                    // that weren't caught by the Repository or UseCase
                    _state.value = ImageGenerationResult.Error("Unexpected Error: ${e.message}")
                }
                .launchIn(this)
        }
    }

    // Optional: Allow UI to clear state (e.g., after navigating away)
    fun clearState() {
        _state.value = null
    }
}