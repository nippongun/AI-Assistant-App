package com.example.aiassistant.domain.model

import java.util.UUID

data class ScheduledTask(
    val id: String = UUID.randomUUID().toString(),
    val hour: Int,
    val minute: Int,
    val promptName: String,
    val promptLocation: String,
    val promptStocks: List<String>,
    val promptNews: List<String>,
    val promptRhetoric: String,
    val active: Boolean
)