package com.example.data.repository

import com.example.data.preferences.DataStoreManager
import com.example.domain.model.AppThemeMode
import com.example.domain.model.HourlyChimeMode
import com.example.domain.model.UserPreferences
import com.example.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow

class UserPreferencesRepositoryImpl(
    private val dataStoreManager: DataStoreManager
) : UserPreferencesRepository {

    override fun getUserPreferences(): Flow<UserPreferences> {
        return dataStoreManager.userPreferencesFlow
    }

    override suspend fun setSpeechSpeed(speed: Float) {
        dataStoreManager.setSpeechSpeed(speed)
    }

    override suspend fun setSpeechPitch(pitch: Float) {
        dataStoreManager.setSpeechPitch(pitch)
    }

    override suspend fun setDefaultBell(bellSound: String) {
        dataStoreManager.setDefaultBell(bellSound)
    }

    override suspend fun set24HourFormat(is24Hour: Boolean) {
        dataStoreManager.set24HourFormat(is24Hour)
    }

    override suspend fun setThemeMode(mode: AppThemeMode) {
        dataStoreManager.setThemeMode(mode)
    }

    override suspend fun setDynamicColors(enabled: Boolean) {
        dataStoreManager.setDynamicColors(enabled)
    }

    override suspend fun setSelectedVoiceName(voiceName: String) {
        dataStoreManager.setSelectedVoiceName(voiceName)
    }

    override suspend fun addCustomBell(name: String, uri: String) {
        dataStoreManager.addCustomBell(name, uri)
    }

    override suspend fun renameCustomBell(uri: String, newName: String) {
        dataStoreManager.renameCustomBell(uri, newName)
    }

    override suspend fun removeCustomBell(uri: String) {
        dataStoreManager.removeCustomBell(uri)
    }

    override suspend fun setHourlyChimeConfig(
        mode: HourlyChimeMode,
        startHour: Int,
        endHour: Int,
        days: List<Int>
    ) {
        dataStoreManager.setHourlyChimeConfig(mode, startHour, endHour, days)
    }

    override suspend fun setHourlyChimeVoiceEnabled(enabled: Boolean) {
        dataStoreManager.setHourlyChimeVoiceEnabled(enabled)
    }

    override suspend fun setFocusTimeConfig(
        enabled: Boolean,
        startHour: Int,
        startMinute: Int,
        endHour: Int,
        endMinute: Int,
        days: List<Int>,
        allowImportant: Boolean
    ) {
        dataStoreManager.setFocusTimeConfig(enabled, startHour, startMinute, endHour, endMinute, days, allowImportant)
    }
}

