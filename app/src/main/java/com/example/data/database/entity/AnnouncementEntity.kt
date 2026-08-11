package com.example.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.Announcement

@Entity(tableName = "announcements")
data class AnnouncementEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val hour: Int,
    val minute: Int,
    val enabled: Boolean = true,
    val repeatDays: String = "1,2,3,4,5,6,7", // Comma-separated day integers "1,2,3,4,5,6,7"
    val bellSound: String = "Classic Chime",
    val volume: Int = 80,
    val voiceEnabled: Boolean = true,
    val customMessage: String? = null,
    val tag: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): Announcement {
        val daysList = if (repeatDays.isBlank()) emptyList() else repeatDays.split(",").mapNotNull { it.trim().toIntOrNull() }
        return Announcement(
            id = id,
            hour = hour,
            minute = minute,
            enabled = enabled,
            repeatDays = daysList,
            bellSound = bellSound,
            volume = volume,
            voiceEnabled = voiceEnabled,
            customMessage = customMessage,
            tag = tag,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    companion object {
        fun fromDomain(domain: Announcement): AnnouncementEntity {
            return AnnouncementEntity(
                id = domain.id,
                hour = domain.hour,
                minute = domain.minute,
                enabled = domain.enabled,
                repeatDays = domain.repeatDays.joinToString(","),
                bellSound = domain.bellSound,
                volume = domain.volume,
                voiceEnabled = domain.voiceEnabled,
                customMessage = domain.customMessage,
                tag = domain.tag,
                createdAt = domain.createdAt,
                updatedAt = domain.updatedAt
            )
        }
    }
}
