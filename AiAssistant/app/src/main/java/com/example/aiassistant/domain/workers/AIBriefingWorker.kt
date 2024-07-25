package com.example.aiassistant.domain.workers

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.aiassistant.domain.usecase.ExecuteAITaskUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume

class AIBriefingWorker(
    context: Context,
    params: WorkerParameters,
    private val executeAITaskUseCase: ExecuteAITaskUseCase
) : CoroutineWorker(context, params) {
    private lateinit var tts: TextToSpeech

    override suspend fun doWork(): Result = withContext(Dispatchers.Main) {

        val context = applicationContext

        val ttsInitialized = suspendCancellableCoroutine<Boolean> { continuation ->
            tts = TextToSpeech(context) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    prepareTTS()
                    continuation.resume(true)
                } else {
                    Log.e("AITaskWorker", "TTS initialization failed")
                    continuation.resume(false)
                }
            }
        }

        if (!ttsInitialized) {
            return@withContext Result.failure()
        }

        try {
            val result = executeAITaskUseCase(context)
            convertTextToSpeech(result)
            Result.success()
        } catch (e: Exception) {
            Log.e("AITaskWorker", "Task execution failed", e)
            Result.failure()
        } finally {
            tts.shutdown()
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
            voice.locale == desiredLocale && !voice.isNetworkConnectionRequired && !voice.features.contains(
                TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED
            )
        } ?: voices.firstOrNull { voice ->
            voice.locale.language == desiredLocale.language && !voice.isNetworkConnectionRequired && !voice.features.contains(
                TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED
            )
        }
    }

    private fun convertTextToSpeech(text: String) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }
}