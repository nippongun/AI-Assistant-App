package com.example.aiassistant

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class TaskViewModel(private val repo: APIRepository) : ViewModel()
{
    private val _taskResult = MutableLiveData<String>()
    val taskResult: LiveData<String> = _taskResult

    fun executeTask(context: Context) {
        viewModelScope.launch {
            try {
                val result = repo.fetchBriefing(context)
                _taskResult.value = result
            } catch (e: Exception) {
                // Handle errors
                _taskResult.value = "Error: ${e.message}"
            }
        }
    }
}