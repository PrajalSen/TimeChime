package com.example.domain.usecase

import com.example.domain.model.Announcement
import com.example.domain.repository.AnnouncementRepository

class DuplicateAnnouncementUseCase(
    private val repository: AnnouncementRepository
) {
    suspend operator fun invoke(announcement: Announcement): Long {
        return repository.duplicateAnnouncement(announcement)
    }
}
