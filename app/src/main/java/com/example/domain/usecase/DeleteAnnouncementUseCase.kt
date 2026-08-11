package com.example.domain.usecase

import com.example.alarm.TimeChimeAlarmScheduler
import com.example.domain.model.Announcement
import com.example.domain.repository.AnnouncementRepository

class DeleteAnnouncementUseCase(
    private val repository: AnnouncementRepository,
    private val alarmScheduler: TimeChimeAlarmScheduler? = null
) {
    suspend operator fun invoke(announcement: Announcement) {
        alarmScheduler?.cancel(announcement)
        repository.deleteAnnouncement(announcement)
    }
}
