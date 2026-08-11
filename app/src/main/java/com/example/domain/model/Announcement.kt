package com.example.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Announcement(
    val id: Long = 0,
    val hour: Int,
    val minute: Int,
    val enabled: Boolean = true,
    val repeatDays: List<Int> = listOf(1, 2, 3, 4, 5, 6, 7), // 1=Mon, 2=Tue, ..., 7=Sun
    val bellSound: String = "Classic Chime",
    val volume: Int = 80, // 0 to 100
    val voiceEnabled: Boolean = true,
    val customMessage: String? = null,
    val tag: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
