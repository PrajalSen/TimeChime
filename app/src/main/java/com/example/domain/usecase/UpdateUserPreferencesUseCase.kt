package com.example.domain.usecase

import com.example.domain.model.AppThemeMode
import com.example.domain.model.HourlyChimeMode
import com.example.domain.repository.UserPreferencesRepository

class UpdateUserPreferencesUseCase(
    private val repository: UserPreferencesRepository
) {
    suspend fun setSpeechSpeed(speed: Float) {
        repository.setSpeechSpeed(speed)
    }

    suspend fun setSpeechPitch(pitch: Float) {
        repository.setSpeechPitch(pitch)
    }

    suspend fun setDefaultBell(bellSound: String) {
        repository.setDefaultBell(bellSound)
    }

    suspend fun set24HourFormat(is24Hour: Boolean) {
        repository.set24HourFormat(is24Hour)
    }

    suspend fun setThemeMode(mode: AppThemeMode) {
        repository.setThemeMode(mode)
    }

    suspend fun setDynamicColors(enabled: Boolean) {
        repository.setDynamicColors(enabled)
    }

    suspend fun setSelectedVoiceName(voiceName: String) {
        repository.setSelectedVoiceName(voiceName)
    }

    suspend fun addCustomBell(name: String, uri: String) {
        repository.addCustomBell(name, uri)
    }

    suspend fun renameCustomBell(uri: String, newName: String) {
        repository.renameCustomBell(uri, newName)
    }

    suspend fun removeCustomBell(uri: String) {
        repository.removeCustomBell(uri)
    }

    suspend fun setHourlyChimeConfig(mode: HourlyChimeMode, startHour: Int, endHour: Int, days: List<Int>) {
        repository.setHourlyChimeConfig(mode, startHour, endHour, days)
    }

    suspend fun setHourlyChimeVoiceEnabled(enabled: Boolean) {
        repository.setHourlyChimeVoiceEnabled(enabled)
    }

    suspend fun setFocusTimeConfig(enabled: Boolean, startHour: Int, startMinute: Int, endHour: Int, endMinute: Int, days: List<Int>, allowImportant: Boolean) {
        repository.setFocusTimeConfig(enabled, startHour, startMinute, endHour, endMinute, days, allowImportant)
    }
}

