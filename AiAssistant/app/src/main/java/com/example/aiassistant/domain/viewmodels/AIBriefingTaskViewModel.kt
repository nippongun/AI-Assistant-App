package com.example.aiassistant.domain.viewmodels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aiassistant.domain.repository.APIRepository
import com.example.aiassistant.domain.usecase.ExecuteAITaskUseCase
import kotlinx.coroutines.launch

class AIBriefingTaskViewModel(private val executeAITaskUseCase: ExecuteAITaskUseCase) : ViewModel()
{
    private val _taskResult = MutableLiveData<String>()
    val taskResult: LiveData<String> = _taskResult

    fun executeTask(context: Context) {
        viewModelScope.launch {
            try {
                val result = executeAITaskUseCase.invoke(context)
                _taskResult.value = result
            } catch (e: Exception) {
                // Handle errors
                _taskResult.value = "Error: ${e.message}"
            }
        }
    }
}