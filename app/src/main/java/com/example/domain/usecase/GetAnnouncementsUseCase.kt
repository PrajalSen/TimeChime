package com.example.domain.usecase

import com.example.domain.model.Announcement
import com.example.domain.repository.AnnouncementRepository
import kotlinx.coroutines.flow.Flow

class GetAnnouncementsUseCase(
    private val repository: AnnouncementRepository
) {
    operator fun invoke(): Flow<List<Announcement>> {
        return repository.getAllAnnouncements()
    }
}
