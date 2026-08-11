package com.example.domain.repository

import com.example.domain.model.AppThemeMode
import com.example.domain.model.HourlyChimeMode
import com.example.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    fun getUserPreferences(): Flow<UserPreferences>
    suspend fun setThemeMode(mode: AppThemeMode)
    suspend fun setSpeechSpeed(speed: Float)
    suspend fun setSpeechPitch(pitch: Float)
    suspend fun setDefaultBell(bell: String)
    suspend fun set24HourFormat(is24Hour: Boolean)
    suspend fun setDynamicColors(enabled: Boolean)
    suspend fun setSelectedVoiceName(voiceName: String)
    suspend fun addCustomBell(name: String, uri: String)
    suspend fun renameCustomBell(uri: String, newName: String)
    suspend fun removeCustomBell(uri: String)
    suspend fun setHourlyChimeConfig(mode: HourlyChimeMode, startHour: Int, endHour: Int, days: List<Int>)
    suspend fun setHourlyChimeVoiceEnabled(enabled: Boolean)
    suspend fun setFocusTimeConfig(enabled: Boolean, startHour: Int, startMinute: Int, endHour: Int, endMinute: Int, days: List<Int>, allowImportant: Boolean)
}

