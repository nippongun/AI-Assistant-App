package com.example.aiassistant

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
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
import java.util.Locale
import kotlin.coroutines.CoroutineContext

class TestActivity : AppCompatActivity() {
    private lateinit var viewModel: TaskViewModel
    private lateinit var resultsText: TextView
    private lateinit var tts: TextToSpeech
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                prepareTTS()
            } else {
                Log.e("TestActivity", "TTS initialization failed")
            }
        }

        setContentView(R.layout.activity_test)
        val fetchBriefingButton: Button = findViewById<Button>(R.id.fetchBriefing)
        resultsText = findViewById<TextView>(R.id.resultsText)
        displayPrompts()

        val repo = APIRepository()
        viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return TaskViewModel(repo) as T
            }
        })[TaskViewModel::class.java]

        viewModel.taskResult.observe(this) { result ->
            convertTextToSpeech(result)
            updateUI(result)
        }

        fetchBriefingButton.setOnClickListener {
            viewModel.executeTask(this)
            //convertTextToSpeech("Hello")
        }


    }

    override fun onDestroy() {
        super.onDestroy()
        tts.stop()
        tts.shutdown()
    }

    private fun prepareTTS() {
        if (tts == null) {
            Log.e("TestActivity", "TTS not initialized")
            return
        }

        val desiredLocale = Locale.US
        tts.setLanguage(desiredLocale)

        val bestVoice = findBestVoice(tts!!, desiredLocale)
        if (bestVoice != null) {
            tts?.voice = bestVoice
            Log.i("TestActivity", "Selected voice: ${bestVoice.name}")
        } else {
            Log.w("TestActivity", "No suitable voice found for locale $desiredLocale")
        }
    }

    private fun findBestVoice(tts: TextToSpeech, desiredLocale: Locale): Voice? {
        val voices = tts.voices ?: return null
        return voices.firstOrNull { voice ->
            voice.locale == desiredLocale && !voice.isNetworkConnectionRequired && !voice.features.contains(TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED)
        } ?: voices.firstOrNull { voice ->
            voice.locale.language == desiredLocale.language && !voice.isNetworkConnectionRequired && !voice.features.contains(TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED)
        }
    }

    private fun convertTextToSpeech(text: String){
        tts.speak(text, TextToSpeech.QUEUE_FLUSH,null,null)
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