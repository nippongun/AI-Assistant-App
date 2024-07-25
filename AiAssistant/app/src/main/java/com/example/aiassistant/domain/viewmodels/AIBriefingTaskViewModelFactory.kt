package com.example.aiassistant.domain.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.aiassistant.domain.usecase.ExecuteAITaskUseCase

class AIBriefingTaskViewModelFactory(private val executeAITaskUseCase: ExecuteAITaskUseCase) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AIBriefingTaskViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AIBriefingTaskViewModel(executeAITaskUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}