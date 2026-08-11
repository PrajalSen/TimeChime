package com.example.di

import android.content.Context
import androidx.room.Room
import com.example.data.database.AppDatabase
import com.example.data.preferences.DataStoreManager
import com.example.data.repository.AnnouncementRepositoryImpl
import com.example.data.repository.UserPreferencesRepositoryImpl
import com.example.domain.repository.AnnouncementRepository
import com.example.domain.repository.UserPreferencesRepository
import com.example.domain.usecase.BulkDeleteAnnouncementsUseCase
import com.example.domain.usecase.DeleteAnnouncementUseCase
import com.example.domain.usecase.DuplicateAnnouncementUseCase
import com.example.domain.usecase.GetAnnouncementsUseCase
import com.example.domain.usecase.GetUserPreferencesUseCase
import com.example.domain.usecase.SaveAnnouncementUseCase
import com.example.domain.usecase.ToggleAnnouncementUseCase
import com.example.domain.usecase.UpdateUserPreferencesUseCase
import com.example.domain.usecase.ValidateAnnouncementUseCase
import com.example.sound.AudioBellPlayer
import com.example.tts.TimeSpeechManager
import com.example.tts.TimeSpeechManagerImpl

class AppContainer(private val context: Context) {

    val audioBellPlayer by lazy { AudioBellPlayer() }

    val timeSpeechManager: TimeSpeechManager by lazy {
        TimeSpeechManagerImpl(context.applicationContext)
    }

    val database: AppDatabase by lazy {
        Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    val announcementDao by lazy { database.announcementDao() }

    val announcementRepository: AnnouncementRepository by lazy {
        AnnouncementRepositoryImpl(announcementDao)
    }

    val dataStoreManager by lazy {
        DataStoreManager(context.applicationContext)
    }

    val userPreferencesRepository: UserPreferencesRepository by lazy {
        UserPreferencesRepositoryImpl(dataStoreManager)
    }

    val getAnnouncementsUseCase by lazy {
        GetAnnouncementsUseCase(announcementRepository)
    }

    val alarmScheduler by lazy {
        com.example.alarm.TimeChimeAlarmScheduler(context.applicationContext)
    }

    val toggleAnnouncementUseCase by lazy {
        ToggleAnnouncementUseCase(announcementRepository, alarmScheduler)
    }

    val saveAnnouncementUseCase by lazy {
        SaveAnnouncementUseCase(announcementRepository, alarmScheduler)
    }

    val deleteAnnouncementUseCase by lazy {
        DeleteAnnouncementUseCase(announcementRepository, alarmScheduler)
    }

    val duplicateAnnouncementUseCase by lazy {
        DuplicateAnnouncementUseCase(announcementRepository)
    }

    val bulkDeleteAnnouncementsUseCase by lazy {
        BulkDeleteAnnouncementsUseCase(announcementRepository, alarmScheduler)
    }

    val validateAnnouncementUseCase by lazy {
        ValidateAnnouncementUseCase()
    }

    val getUserPreferencesUseCase by lazy {
        GetUserPreferencesUseCase(userPreferencesRepository)
    }

    val updateUserPreferencesUseCase by lazy {
        UpdateUserPreferencesUseCase(userPreferencesRepository)
    }
}
