package com.example.aiassistant.domain.usecase

import android.content.Context
import com.example.aiassistant.domain.repository.APIRepository

class ExecuteAITaskUseCase(private val repository: APIRepository) {
    suspend operator fun invoke(context: Context): String {
        return repository.fetchBriefing(context)
    }
}