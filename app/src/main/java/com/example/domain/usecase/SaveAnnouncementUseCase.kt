package com.example.domain.usecase

import com.example.alarm.TimeChimeAlarmScheduler
import com.example.domain.model.Announcement
import com.example.domain.repository.AnnouncementRepository

class SaveAnnouncementUseCase(
    private val repository: AnnouncementRepository,
    private val alarmScheduler: TimeChimeAlarmScheduler? = null
) {
    suspend operator fun invoke(announcement: Announcement): Long {
        val id = if (announcement.id == 0L) {
            repository.insertAnnouncement(announcement)
        } else {
            repository.updateAnnouncement(announcement)
            announcement.id
        }
        val saved = if (announcement.id == 0L) announcement.copy(id = id) else announcement
        alarmScheduler?.schedule(saved)
        return id
    }
}
