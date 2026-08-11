package com.example.domain.repository

import com.example.domain.model.Announcement
import kotlinx.coroutines.flow.Flow

interface AnnouncementRepository {
    fun getAllAnnouncements(): Flow<List<Announcement>>
    fun getEnabledAnnouncements(): Flow<List<Announcement>>
    suspend fun getAnnouncementById(id: Long): Announcement?
    suspend fun insertAnnouncement(announcement: Announcement): Long
    suspend fun updateAnnouncement(announcement: Announcement)
    suspend fun deleteAnnouncement(announcement: Announcement)
    suspend fun deleteAnnouncements(ids: List<Long>)
    suspend fun toggleAnnouncementEnabled(id: Long, enabled: Boolean)
    suspend fun duplicateAnnouncement(announcement: Announcement): Long
    suspend fun importAnnouncements(announcements: List<Announcement>)
    suspend fun clearAllAnnouncements()
}
