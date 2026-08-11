package com.example.domain.usecase

import com.example.alarm.TimeChimeAlarmScheduler
import com.example.domain.repository.AnnouncementRepository

class ToggleAnnouncementUseCase(
    private val repository: AnnouncementRepository,
    private val alarmScheduler: TimeChimeAlarmScheduler? = null
) {
    suspend operator fun invoke(id: Long, enabled: Boolean) {
        repository.toggleAnnouncementEnabled(id, enabled)
        val announcement = repository.getAnnouncementById(id)
        if (announcement != null) {
            if (enabled) {
                alarmScheduler?.schedule(announcement)
            } else {
                alarmScheduler?.cancel(announcement)
            }
        }
    }
}
