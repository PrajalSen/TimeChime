package com.example.data.repository

import com.example.data.database.dao.AnnouncementDao
import com.example.data.database.entity.AnnouncementEntity
import com.example.domain.model.Announcement
import com.example.domain.repository.AnnouncementRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AnnouncementRepositoryImpl(
    private val announcementDao: AnnouncementDao
) : AnnouncementRepository {

    override fun getAllAnnouncements(): Flow<List<Announcement>> {
        return announcementDao.getAllAnnouncements().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getEnabledAnnouncements(): Flow<List<Announcement>> {
        return announcementDao.getEnabledAnnouncements().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getAnnouncementById(id: Long): Announcement? {
        return announcementDao.getAnnouncementById(id)?.toDomain()
    }

    override suspend fun insertAnnouncement(announcement: Announcement): Long {
        val now = System.currentTimeMillis()
        val entity = AnnouncementEntity.fromDomain(
            announcement.copy(
                createdAt = if (announcement.createdAt == 0L) now else announcement.createdAt,
                updatedAt = now
            )
        )
        return announcementDao.insert(entity)
    }

    override suspend fun updateAnnouncement(announcement: Announcement) {
        val now = System.currentTimeMillis()
        val entity = AnnouncementEntity.fromDomain(announcement.copy(updatedAt = now))
        announcementDao.update(entity)
    }

    override suspend fun toggleAnnouncementEnabled(id: Long, enabled: Boolean) {
        announcementDao.updateEnabled(id, enabled, System.currentTimeMillis())
    }

    override suspend fun deleteAnnouncement(announcement: Announcement) {
        announcementDao.delete(AnnouncementEntity.fromDomain(announcement))
    }

    override suspend fun deleteAnnouncements(ids: List<Long>) {
        if (ids.isNotEmpty()) {
            announcementDao.deleteByIds(ids)
        }
    }

    override suspend fun duplicateAnnouncement(announcement: Announcement): Long {
        val now = System.currentTimeMillis()
        val duplicate = announcement.copy(
            id = 0L,
            createdAt = now,
            updatedAt = now
        )
        return announcementDao.insert(AnnouncementEntity.fromDomain(duplicate))
    }

    override suspend fun importAnnouncements(announcements: List<Announcement>) {
        val now = System.currentTimeMillis()
        val entities = announcements.map {
            AnnouncementEntity.fromDomain(
                it.copy(
                    id = 0L,
                    createdAt = now,
                    updatedAt = now
                )
            )
        }
        announcementDao.insertAll(entities)
    }

    override suspend fun clearAllAnnouncements() {
        announcementDao.deleteAll()
    }
}
