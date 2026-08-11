package com.example.domain.usecase

import com.example.alarm.TimeChimeAlarmScheduler
import com.example.domain.repository.AnnouncementRepository

class BulkDeleteAnnouncementsUseCase(
    private val repository: AnnouncementRepository,
    private val alarmScheduler: TimeChimeAlarmScheduler? = null
) {
    suspend operator fun invoke(ids: List<Long>) {
        ids.forEach { id ->
            val item = repository.getAnnouncementById(id)
            if (item != null) {
                alarmScheduler?.cancel(item)
            }
        }
        repository.deleteAnnouncements(ids)
    }
}
