package com.example.aiassistant

data class PromptData(
    val prompts: List<Prompt>
)

data class Prompt(
    val name: String,
    val location: String,
    val stocks: List<String>,
    val news: List<String>,
    val rhetoric: String
)
