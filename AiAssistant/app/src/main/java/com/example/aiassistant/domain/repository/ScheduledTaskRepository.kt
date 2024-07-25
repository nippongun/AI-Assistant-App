package com.example.aiassistant.domain.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.aiassistant.domain.model.ScheduledTask
import org.json.JSONArray
import org.json.JSONObject

class ScheduledTaskRepository(context: Context) {
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
        }
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
            promptRhetoric = getString("promptRhetoric")
        )
    }
}