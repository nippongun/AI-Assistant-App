package com.example.aiassistant

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.aallam.openai.api.chat.ChatCompletion
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.http.Timeout
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class TestActivity : AppCompatActivity() {
    private lateinit var viewModel: TaskViewModel
    private lateinit var resultsText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)
        val fetchBriefingButton: Button = findViewById<Button>(R.id.fetchBriefing)
        resultsText = findViewById<TextView>(R.id.resultsText)
        displayPrompts()

        val repo = APIRepository()
        viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return TaskViewModel(repo) as T
            }
        }).get(TaskViewModel::class.java)

        viewModel.taskResult.observe(this) { result ->
            updateUI(result)
        }

        fetchBriefingButton.setOnClickListener {
            viewModel.executeTask(this)
        }
    }

    private fun updateUI(result: String){
        resultsText.text = result
    }

    private fun displayPrompts(){
        val text = Utils.getPrompts(this) ?: "Error loading prompts"
        val promptsTextView: TextView = findViewById<TextView>(R.id.promptsTextView)
        promptsTextView.text = text
    }
}