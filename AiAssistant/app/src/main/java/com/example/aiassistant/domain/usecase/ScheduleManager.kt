package com.example.aiassistant.domain.usecase

import android.content.Context
import android.icu.util.Calendar
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.aiassistant.domain.model.ScheduledTask
import com.example.aiassistant.domain.repository.ScheduledTaskRepository
import com.example.aiassistant.domain.workers.AIBriefingWorker
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class ScheduleManager(private val repository: ScheduledTaskRepository, private val context: Context) {


}