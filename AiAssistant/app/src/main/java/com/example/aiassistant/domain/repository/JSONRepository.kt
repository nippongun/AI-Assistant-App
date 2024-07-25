package com.example.aiassistant.domain.repository

import android.content.Context
import android.util.Log
import com.example.aiassistant.domain.model.Prompt
import com.example.aiassistant.domain.model.PromptData
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileWriter

class JSONRepository {
    companion object {
        fun getPrompts(context: Context): String? {
            try {
                val file = File(context.getExternalFilesDir(null), "prompts.json")
                val jsonString = file.readText()

                val jsonObject = JSONObject(jsonString)
                val promptsArray = jsonObject.getJSONArray("prompts")
                val stringBuilder = StringBuilder()

                for (i in 0 until promptsArray.length()) {
                    val prompt = promptsArray.getJSONObject(i)

                    stringBuilder.append("Prompt ${i + 1}: \n")
                    stringBuilder.append("Name: ${prompt.getString("name")}\n")
                    stringBuilder.append("Location: ${prompt.getString("location")}\n")

                    val stocks = prompt.getJSONArray("stocks")
                    stringBuilder.append("Stocks: ")

                    for (j in 0 until stocks.length()) {
                        if (j > 0) stringBuilder.append(", ")
                        stringBuilder.append(stocks.getString(j))
                    }
                    stringBuilder.append("\n")

                    val newsCategories = prompt.getJSONArray("news")
                    stringBuilder.append("News: ")

                    for (j in 0 until newsCategories.length()) {
                        if (j > 0) stringBuilder.append(", ")
                        stringBuilder.append(newsCategories.getString(j))
                    }
                    stringBuilder.append("\n")

                    stringBuilder.append("Rhetoric: ${prompt.getString("rhetoric")}\n")
                }
                return stringBuilder.toString()
            } catch (e: Exception) {
                e.printStackTrace()
                Log.d("MainActivity", "Error: $e")
                return null
            }
        }
        fun serializePrompt(prompt: Prompt, context: Context){
            val promptObject = JSONObject()
            promptObject.put("name", prompt.name)
            promptObject.put("location", prompt.location)

            val stocksArray = JSONArray()
            for(stockTicker in prompt.stocks) {
                if(stockTicker.isNotEmpty()){
                    stocksArray.put(stockTicker)
                }
            }
            promptObject.put("stocks", stocksArray)

            val newsCategoriesArray = JSONArray()
            for(newsCategory in prompt.news) {
                if(newsCategory.isNotEmpty()){
                    newsCategoriesArray.put(newsCategory)
                }
            }
            promptObject.put("news", newsCategoriesArray)

            promptObject.put("rhetoric", prompt.rhetoric)

            val promptsArray = JSONArray()
            promptsArray.put(promptObject)

            val jsonObject = JSONObject()
            jsonObject.put("prompts", promptsArray)

            try {
                val file = File(context.getExternalFilesDir(null), "prompts.json")
                FileWriter(file).use { writer ->
                    writer.write(jsonObject.toString(2))
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Log.d("MainActivity", "Error: $e")

            }
        }
        fun deserializePrompts(context: Context): List<Prompt> {
            val file = File(context.getExternalFilesDir(null), "prompts.json")
            if (!file.exists()) {
                return emptyList()
            }

            val jsonString = file.readText()
            val jsonObject = JSONObject(jsonString)
            val promptsArray = jsonObject.getJSONArray("prompts")

            val prompts = mutableListOf<Prompt>()

            for (i in 0 until promptsArray.length()) {
                val promptObject = promptsArray.getJSONObject(i)

                val name = promptObject.getString("name")
                val location = promptObject.getString("location")

                val stocksArray = promptObject.getJSONArray("stocks")
                val stocks = List(stocksArray.length()) { stocksArray.getString(it) }

                val newsArray = promptObject.getJSONArray("news")
                val news = List(newsArray.length()) { newsArray.getString(it) }

                val rhetoric = promptObject.getString("rhetoric")

                prompts.add(Prompt(name, location, stocks, news, rhetoric))
            }

            return prompts
        }
        fun parsePrompts(jsonString: String) : PromptData {
            val jsonObject = JSONObject(jsonString)
            val promptsArray = jsonObject.getJSONArray("prompts")
            val prompts = mutableListOf<Prompt>()

            for (i in 0 until promptsArray.length()) {
                val promptJson = promptsArray.getJSONObject(i)
                val prompt = Prompt(
                    name = promptJson.getString("name"),
                    location = promptJson.getString("location"),
                    stocks = parseJsonArray(promptJson.getJSONArray("stocks")),
                    news = parseJsonArray(promptJson.getJSONArray("news")),
                    rhetoric = promptJson.getString("rhetoric")
                )
                prompts.add(prompt)
            }

            return PromptData(prompts)
        }
        fun appendPromptToJson(context: Context, newPrompt: Prompt) {
            val file = File(context.getExternalFilesDir(null), "prompts.json")

            try {
                // Read existing file content
                val jsonString = if (file.exists()) file.readText() else """{"prompts":[]}"""
                val jsonObject = JSONObject(jsonString)
                val promptsArray = jsonObject.getJSONArray("prompts")

                // Append new prompt
                promptsArray.put(promptToJson(newPrompt))

                // Write updated content back to file
                file.writeText(jsonObject.toString(2)) // 2 is the indentation
            } catch (e: Exception) {
                e.printStackTrace()
                // Handle error (e.g., throw exception or return a result)
            }
        }
        fun removePromptFromJson(context: Context, promptToRemove: Prompt){
            val file = File(context.getExternalFilesDir(null), "prompts.json")
            try {
                val jsonString = file.readText()
                val jsonObject = JSONObject(jsonString)
                val promptsArray = jsonObject.getJSONArray("prompts")

                val updatedPromptsArray = JSONArray()

                for (i in 0 until promptsArray.length()) {
                    val promptJson = promptsArray.getJSONObject(i)
                    val currentPrompt = jsonToPrompt(promptJson)
                    if (currentPrompt != promptToRemove) {
                        updatedPromptsArray.put(promptJson)
                    }
                }
                jsonObject.put("prompts", updatedPromptsArray)

                file.writeText(jsonObject.toString(2))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        fun removePromptFromJsonByIndex(context: Context, indexToRemove: Int) {
            val file = File(context.getExternalFilesDir(null), "prompts.json")
            try {
                val jsonString = file.readText()
                val jsonObject = JSONObject(jsonString)
                val promptsArray = jsonObject.getJSONArray("prompts")

                if (indexToRemove < 0 || indexToRemove >= promptsArray.length()) {
                    throw IllegalArgumentException("Invalid index: $indexToRemove")
                }

                val updatedPromptsArray = JSONArray()

                for (i in 0 until promptsArray.length()) {
                    if (i != indexToRemove) {
                        updatedPromptsArray.put(promptsArray.getJSONObject(i))
                    }
                }

                jsonObject.put("prompts", updatedPromptsArray)

                file.writeText(jsonObject.toString(2)) // 2 is the indentation
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun generatePrompt(prompt: Prompt): String {
            val promptBuilder = StringBuilder()

            promptBuilder.append("Generate daily prompt for ${prompt.name} located in ${prompt.location}. Include:\n")
            promptBuilder.append("1. Weather forecast for ${prompt.location}\n")
            promptBuilder.append("2. Top news headlines in categories: ${prompt.news.joinToString(",")}\n")
            promptBuilder.append("3. Stock prices for ${prompt.stocks.joinToString(",")}\n")
            promptBuilder.append("Provide a concise summary for each section.")
            promptBuilder.append("Search the internet for all categories. Respond with plain text only. No formatting.")
            return promptBuilder.toString()
        }

        private fun parseJsonArray(jsonArray: JSONArray): List<String> {
            val list = mutableListOf<String>()
            for (i in 0 until jsonArray.length()) {
                list.add(jsonArray.getString(i))
            }
            return list
        }
        private fun promptToJson(prompt: Prompt): JSONObject {
            return JSONObject().apply {
                put("name", prompt.name)
                put("location", prompt.location)
                put("stocks", JSONArray(prompt.stocks))
                put("news", JSONArray(prompt.news))
                put("rhetoric", prompt.rhetoric)
            }
        }
        private fun jsonToPrompt(json: JSONObject): Prompt {
            return Prompt(
                name = json.getString("name"),
                location = json.getString("location"),
                stocks = parseJsonArray(json.getJSONArray("stocks")),
                news = parseJsonArray(json.getJSONArray("news")),
                rhetoric = json.getString("rhetoric")
            )
        }
    }
}