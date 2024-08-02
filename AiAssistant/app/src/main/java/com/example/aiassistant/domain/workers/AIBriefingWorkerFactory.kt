package com.example.aiassistant.domain.workers

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.example.aiassistant.domain.usecase.ExecuteAITaskUseCase

class AIBriefingWorkerFactory(private val executeAITaskUseCase: ExecuteAITaskUseCase) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return when (workerClassName) {
            AIBriefingWorker::class.java.name ->
                AIBriefingWorker(appContext, workerParameters, executeAITaskUseCase)
            else -> null
        }
    }
}