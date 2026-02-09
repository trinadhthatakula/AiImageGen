package com.ai.image.gen.presentation.i2i

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ai.image.gen.domain.ImageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class ImageEditViewModel(
    private val repository: ImageRepository
) : ViewModel() {

    private val _selectedUri = MutableStateFlow<Uri?>(null)
    val selectedUri: StateFlow<Uri?> = _selectedUri

    private val _selectedStyle = MutableStateFlow<ImageStyle?>(null)
    val selectedStyle: StateFlow<ImageStyle?> = _selectedStyle

    private val _customPrompt = MutableStateFlow("")
    val customPrompt: StateFlow<String> = _customPrompt

    // Derived state: Is the button enabled?
    val isGenerateEnabled: StateFlow<Boolean> = combine(
        _selectedUri, _selectedStyle, _customPrompt
    ) { uri, style, custom ->
        uri != null && (style != null && style != ImageStyle.CUSTOM || (style == ImageStyle.CUSTOM && custom.isNotBlank()))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun onImageSelected(uri: Uri?) {
        _selectedUri.value = uri
    }

    fun onStyleSelected(style: ImageStyle) {
        _selectedStyle.value = style
    }

    fun onCustomPromptChanged(prompt: String) {
        _customPrompt.value = prompt
    }

    fun generateEdit() {
        val uri = _selectedUri.value ?: return
        val style = _selectedStyle.value ?: return

        // Use the custom prompt if selected, otherwise the style's instruction
        val finalPrompt = if (style == ImageStyle.CUSTOM) {
            _customPrompt.value
        } else {
            style.instruction
        }

        if (finalPrompt.isBlank()) return

        // Trigger WorkManager
        repository.scheduleImageEdit(finalPrompt, uri.toString())
    }
}