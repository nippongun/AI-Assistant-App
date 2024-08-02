package com.example.aiassistant.domain.repository

import android.content.Context
import android.content.SharedPreferences
import android.icu.util.Calendar
import android.os.Looper
import android.util.Log
import android.util.Printer
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkQuery
import androidx.work.workDataOf
import com.example.aiassistant.domain.model.ScheduledTask
import com.example.aiassistant.domain.workers.AIBriefingWorker
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class ScheduledTaskRepository(private val context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("SchedulePrefs", Context.MODE_PRIVATE)

    fun saveScheduledTask(task: ScheduledTask) {
        val tasks = getAllScheduledTasks().toMutableList()
        tasks.add(task)
        val jsonArray = JSONArray()
        tasks.forEach { jsonArray.put(it.toJson()) }
        sharedPreferences.edit().putString("scheduled_tasks", jsonArray.toString()).apply()
    }

    fun getAllScheduledTasks(): List<ScheduledTask> {
        val json = sharedPreferences.getString("scheduled_tasks", null)
        return if (json != null) {
            val jsonArray = JSONArray(json)
            List(jsonArray.length()) { index ->
                jsonArray.getJSONObject(index).toScheduledTask()
            }
        } else {
            emptyList()
        }
    }

    fun getScheduledTaskById(id: String): ScheduledTask? {
        return getAllScheduledTasks().find { it.id == id }
    }

    fun updateScheduledTask(task: ScheduledTask) {
        val tasks = getAllScheduledTasks().toMutableList()
        val index = tasks.indexOfFirst { it.id == task.id }
        if (index != -1) {
            tasks[index] = task
            val jsonArray = JSONArray()
            tasks.forEach { jsonArray.put(it.toJson()) }
            Log.d("ScheduledTaskRepository", tasks.size.toString())
            sharedPreferences.edit().putString("scheduled_tasks", jsonArray.toString()).apply()
        }
    }

    fun deleteScheduledTask(id: String) {
        val tasks = getAllScheduledTasks().toMutableList()
        tasks.removeAll { it.id == id }
        val jsonArray = JSONArray()
        tasks.forEach { jsonArray.put(it.toJson()) }
        sharedPreferences.edit().putString("scheduled_tasks", jsonArray.toString()).apply()
    }

    private fun ScheduledTask.toJson(): JSONObject {
        return JSONObject().apply {
            put("id", id)
            put("hour", hour)
            put("minute", minute)
            put("promptName", promptName)
            put("promptLocation", promptLocation)
            put("promptStocks", JSONArray(promptStocks))
            put("promptNews", JSONArray(promptNews))
            put("promptRhetoric", promptRhetoric)
            put("active", active)
        }
    }

    fun toggleTaskActive(taskId: String, active: Boolean) {
        val task = getScheduledTaskById(taskId)
        if (task != null) {
            val updatedTask = task.copy(active = active)
            updateScheduledTask(updatedTask)
            if (active) {
                scheduleTask(updatedTask)
            } else {
                cancelScheduledTask(taskId)
            }
        }
        debugPrintEnqueuedWorks(context)
    }

    private fun JSONObject.toScheduledTask(): ScheduledTask {
        return ScheduledTask(
            id = getString("id"),
            hour = getInt("hour"),
            minute = getInt("minute"),
            promptName = getString("promptName"),
            promptLocation = getString("promptLocation"),
            promptStocks = List(getJSONArray("promptStocks").length()) { i ->
                getJSONArray("promptStocks").getString(i)
            },
            promptNews = List(getJSONArray("promptNews").length()) { i ->
                getJSONArray("promptNews").getString(i)
            },
            promptRhetoric = getString("promptRhetoric"),
            active = getBoolean("active")
        )
    }

    fun scheduleTask(task: ScheduledTask){

        val currentTime = Calendar.getInstance()
        val scheduledTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, task.hour)
            set(Calendar.MINUTE, task.minute)
            set(Calendar.SECOND, 0)
        }

        if(scheduledTime.before(currentTime)){
            scheduledTime.add(Calendar.DAY_OF_YEAR,1)
        }

        val delay = scheduledTime.timeInMillis - currentTime.timeInMillis

        val workRequest = OneTimeWorkRequestBuilder<AIBriefingWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(workDataOf("task" to task.toString()))
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            task.id,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    fun cancelScheduledTask(taskId: String) {
        WorkManager.getInstance(context).cancelUniqueWork(taskId)
        //deleteScheduledTask(taskId)
    }

    fun debugPrintEnqueuedWorks(context: Context) {
        val workManager = WorkManager.getInstance(context)

        val workQuery = WorkQuery.Builder
            .fromStates(listOf(WorkInfo.State.ENQUEUED, WorkInfo.State.RUNNING))
            .build()

        val workInfos = workManager.getWorkInfos(workQuery).get()

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        println("Enqueued and Running Works:")
        workInfos.forEach { workInfo ->
            val currentTime = dateFormat.format(Date())
            val scheduledTime = dateFormat.format(workInfo.nextScheduleTimeMillis)
            println("Current Time: $currentTime")
            println("ID: ${workInfo.id}")
            println("State: ${workInfo.state}")
            println("Tags: ${workInfo.tags}")
            println("Run Attempt Count: ${workInfo.runAttemptCount}")
            println("Next scheduled time: $scheduledTime")
            println("Periodicity ${workInfo.periodicityInfo}")
            // Print input data if any
            println("--------------------")
        }
    }
}