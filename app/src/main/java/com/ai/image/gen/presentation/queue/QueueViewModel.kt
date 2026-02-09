package com.ai.image.gen.presentation.queue

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ai.image.gen.data.local.RequestDao
import com.ai.image.gen.data.local.entity.GenerationRequestEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class QueueViewModel(
    private val dao: RequestDao
) : ViewModel() {

    // 1. Live count for the Home Screen Badge
    val activeCount: StateFlow<Int> = dao.observeActiveCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // 2. Active Tab Data (Queued/Running)
    val activeRequests: StateFlow<List<GenerationRequestEntity>> = dao.observeActiveRequests()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 3. History Tab Data (Success/Failed)
    val completedRequests: StateFlow<List<GenerationRequestEntity>> = dao.observeCompletedRequests()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}