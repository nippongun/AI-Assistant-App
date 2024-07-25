package com.example.aiassistant.domain.repository

import android.content.Context
import com.aallam.openai.api.chat.ChatCompletion
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.http.Timeout
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.example.aiassistant.BuildConfig
import com.example.aiassistant.domain.model.Prompt
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.seconds

class APIRepository {
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
    suspend fun fetchBriefing(context: Context) : String {
        return withContext(dispatcher){
            val apiKey = BuildConfig.OPENAI_API_KEY
            val projectKey = BuildConfig.PROJECT_KEY
            val organizationKey = BuildConfig.ORGANIZATION_KEY

            val prompts: List<Prompt> = JSONRepository.deserializePrompts(context)
            var request: String = ""
            var result = ""
            if (prompts.isNotEmpty()) {
                request = JSONRepository.generatePrompt(prompts[0])
                val openAI = OpenAI(
                    token = apiKey,
                    timeout = Timeout(socket = 60.seconds),
                    organization = organizationKey
                )
                val chatCompletionRequest = ChatCompletionRequest(
                    model = ModelId("gpt-4-turbo"),
                    messages = listOf(
                        ChatMessage(
                            role = ChatRole.System,
                            content = "You read the users daily briefing. Search the internet. Don't return asterisks"
                        ),
                        ChatMessage(
                            role = ChatRole.User,
                            content = request
                        )
                    )
                )
                val completion: ChatCompletion = openAI.chatCompletion(chatCompletionRequest)
                result = completion.choices[0].message.content.toString()
            }
            result
        }
    }
}