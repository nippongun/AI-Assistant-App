package com.example.aiassistant

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.aiassistant.domain.repository.APIRepository
import com.example.aiassistant.domain.viewmodels.AIBriefingTaskViewModel
import com.example.aiassistant.domain.repository.JSONRepository
import com.example.aiassistant.domain.usecase.ExecuteAITaskUseCase
import com.example.aiassistant.domain.viewmodels.AIBriefingTaskViewModelFactory
import com.example.aiassistant.domain.workers.AIBriefingWorker
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

class TestActivity : AppCompatActivity() {
    private lateinit var viewModel: AIBriefingTaskViewModel
    private lateinit var resultsText: TextView
    private lateinit var tts: TextToSpeech

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)

        val repository = APIRepository()
        val executeAITaskUseCase = ExecuteAITaskUseCase(repository)
        val viewModelFactory = AIBriefingTaskViewModelFactory(executeAITaskUseCase)

        viewModel = ViewModelProvider(this, viewModelFactory)[AIBriefingTaskViewModel::class.java]

        initTTS()
        observeViewModel()

        val fetchBriefingButton: Button = findViewById<Button>(R.id.fetchBriefing)
        resultsText = findViewById<TextView>(R.id.resultsText)
        displayPrompts()

        fetchBriefingButton.setOnClickListener {
            viewModel.executeTask(this)
        }

        val scheduleDialog = ScheduleDialogFragment.newInstance()
        scheduleDialog.show(supportFragmentManager, ScheduleDialogFragment.TAG)
    }

    override fun onDestroy() {
        super.onDestroy()
        tts.stop()
        tts.shutdown()
    }

    private fun initTTS(){
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                prepareTTS()
            } else {
                Log.e("TestActivity", "TTS initialization failed")
            }
        }
    }

    private fun scheduleAIBriefing(){

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val dailyWorkRequest = PeriodicWorkRequestBuilder<AIBriefingWorker>(1,TimeUnit.DAYS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork("dailyAIBriefing",ExistingPeriodicWorkPolicy.KEEP, dailyWorkRequest)

    }

    private fun observeViewModel(){
        viewModel.taskResult.observe(this) { result ->
            convertTextToSpeech(result)
            updateUI(result)
        }
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
        val text = JSONRepository.getPrompts(this) ?: "Error loading prompts"
        val promptsTextView: TextView = findViewById<TextView>(R.id.promptsTextView)
        promptsTextView.text = text
    }
}