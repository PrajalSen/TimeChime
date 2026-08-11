package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.domain.model.AppThemeMode
import com.example.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

import androidx.datastore.preferences.core.intPreferencesKey

import com.example.domain.model.CustomBell

import com.example.domain.model.HourlyChimeMode

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "timechime_user_preferences")

class DataStoreManager(private val context: Context) {

    private object PreferenceKeys {
        val SPEECH_SPEED = floatPreferencesKey("speech_speed")
        val SPEECH_PITCH = floatPreferencesKey("speech_pitch")
        val DEFAULT_BELL = stringPreferencesKey("default_bell")
        val DEFAULT_VOLUME = intPreferencesKey("default_volume")
        val DEFAULT_VOICE_ENABLED = booleanPreferencesKey("default_voice_enabled")
        val IS_24_HOUR = booleanPreferencesKey("is_24_hour")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DYNAMIC_COLORS = booleanPreferencesKey("dynamic_colors")
        val SELECTED_VOICE = stringPreferencesKey("selected_voice")
        val CUSTOM_BELLS = stringPreferencesKey("custom_bells_list")

        // Hourly Chime
        val HOURLY_CHIME_MODE = stringPreferencesKey("hourly_chime_mode")
        val HOURLY_CHIME_START_HOUR = intPreferencesKey("hourly_chime_start_hour")
        val HOURLY_CHIME_END_HOUR = intPreferencesKey("hourly_chime_end_hour")
        val HOURLY_CHIME_DAYS = stringPreferencesKey("hourly_chime_days")
        val HOURLY_CHIME_VOICE_ENABLED = booleanPreferencesKey("hourly_chime_voice_enabled")

        // Focus Time
        val FOCUS_TIME_ENABLED = booleanPreferencesKey("focus_time_enabled")
        val FOCUS_START_HOUR = intPreferencesKey("focus_start_hour")
        val FOCUS_START_MINUTE = intPreferencesKey("focus_start_minute")
        val FOCUS_END_HOUR = intPreferencesKey("focus_end_hour")
        val FOCUS_END_MINUTE = intPreferencesKey("focus_end_minute")
        val FOCUS_DAYS = stringPreferencesKey("focus_days")
        val FOCUS_ALLOW_IMPORTANT = booleanPreferencesKey("focus_allow_important")
    }

    private fun parseDaysList(raw: String?): List<Int> {
        if (raw.isNullOrEmpty()) return listOf(1, 2, 3, 4, 5, 6, 7)
        return raw.split(",").mapNotNull { it.trim().toIntOrNull() }
    }

    private fun encodeDaysList(days: List<Int>): String {
        return days.joinToString(",")
    }

    private fun parseCustomBells(raw: String?): List<CustomBell> {
        if (raw.isNullOrEmpty()) return emptyList()
        return raw.split(";;").mapNotNull { entry ->
            val parts = entry.split("|||")
            if (parts.size >= 2) CustomBell(name = parts[0], uri = parts[1]) else null
        }
    }

    private fun encodeCustomBells(list: List<CustomBell>): String {
        return list.joinToString(";;") { "${it.name}|||${it.uri}" }
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data
        .map { preferences ->
            val themeModeString = preferences[PreferenceKeys.THEME_MODE] ?: AppThemeMode.SYSTEM.name
            val themeMode = try {
                AppThemeMode.valueOf(themeModeString)
            } catch (e: Exception) {
                AppThemeMode.SYSTEM
            }

            val hourlyModeString = preferences[PreferenceKeys.HOURLY_CHIME_MODE] ?: HourlyChimeMode.OFF.name
            val hourlyMode = try {
                HourlyChimeMode.valueOf(hourlyModeString)
            } catch (e: Exception) {
                HourlyChimeMode.OFF
            }

            val customBells = parseCustomBells(preferences[PreferenceKeys.CUSTOM_BELLS])

            UserPreferences(
                speechSpeed = preferences[PreferenceKeys.SPEECH_SPEED] ?: 1.0f,
                speechPitch = preferences[PreferenceKeys.SPEECH_PITCH] ?: 1.0f,
                defaultBell = preferences[PreferenceKeys.DEFAULT_BELL] ?: "Classic Chime",
                defaultVolume = preferences[PreferenceKeys.DEFAULT_VOLUME] ?: 80,
                defaultVoiceEnabled = preferences[PreferenceKeys.DEFAULT_VOICE_ENABLED] ?: true,
                is24HourFormat = preferences[PreferenceKeys.IS_24_HOUR] ?: false,
                themeMode = themeMode,
                dynamicColors = preferences[PreferenceKeys.DYNAMIC_COLORS] ?: true,
                selectedVoiceName = preferences[PreferenceKeys.SELECTED_VOICE] ?: "System Default",
                customBells = customBells,
                hourlyChimeMode = hourlyMode,
                hourlyChimeStartHour = preferences[PreferenceKeys.HOURLY_CHIME_START_HOUR] ?: 8,
                hourlyChimeEndHour = preferences[PreferenceKeys.HOURLY_CHIME_END_HOUR] ?: 22,
                hourlyChimeDays = parseDaysList(preferences[PreferenceKeys.HOURLY_CHIME_DAYS]),
                hourlyChimeVoiceEnabled = preferences[PreferenceKeys.HOURLY_CHIME_VOICE_ENABLED] ?: false,
                focusTimeEnabled = preferences[PreferenceKeys.FOCUS_TIME_ENABLED] ?: false,
                focusStartHour = preferences[PreferenceKeys.FOCUS_START_HOUR] ?: 22,
                focusStartMinute = preferences[PreferenceKeys.FOCUS_START_MINUTE] ?: 0,
                focusEndHour = preferences[PreferenceKeys.FOCUS_END_HOUR] ?: 7,
                focusEndMinute = preferences[PreferenceKeys.FOCUS_END_MINUTE] ?: 0,
                focusDays = parseDaysList(preferences[PreferenceKeys.FOCUS_DAYS]),
                focusAllowImportantAlarms = preferences[PreferenceKeys.FOCUS_ALLOW_IMPORTANT] ?: false
            )
        }

    suspend fun setHourlyChimeConfig(
        mode: HourlyChimeMode,
        startHour: Int,
        endHour: Int,
        days: List<Int>
    ) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.HOURLY_CHIME_MODE] = mode.name
            preferences[PreferenceKeys.HOURLY_CHIME_START_HOUR] = startHour
            preferences[PreferenceKeys.HOURLY_CHIME_END_HOUR] = endHour
            preferences[PreferenceKeys.HOURLY_CHIME_DAYS] = encodeDaysList(days)
        }
    }

    suspend fun setHourlyChimeVoiceEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.HOURLY_CHIME_VOICE_ENABLED] = enabled
        }
    }

    suspend fun setFocusTimeConfig(
        enabled: Boolean,
        startHour: Int,
        startMinute: Int,
        endHour: Int,
        endMinute: Int,
        days: List<Int>,
        allowImportant: Boolean
    ) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.FOCUS_TIME_ENABLED] = enabled
            preferences[PreferenceKeys.FOCUS_START_HOUR] = startHour
            preferences[PreferenceKeys.FOCUS_START_MINUTE] = startMinute
            preferences[PreferenceKeys.FOCUS_END_HOUR] = endHour
            preferences[PreferenceKeys.FOCUS_END_MINUTE] = endMinute
            preferences[PreferenceKeys.FOCUS_DAYS] = encodeDaysList(days)
            preferences[PreferenceKeys.FOCUS_ALLOW_IMPORTANT] = allowImportant
        }
    }

    suspend fun addCustomBell(name: String, uri: String) {
        context.dataStore.edit { preferences ->
            val current = parseCustomBells(preferences[PreferenceKeys.CUSTOM_BELLS]).toMutableList()
            current.removeAll { it.uri == uri || it.name == name }
            current.add(CustomBell(name = name, uri = uri))
            preferences[PreferenceKeys.CUSTOM_BELLS] = encodeCustomBells(current)
        }
    }

    suspend fun renameCustomBell(uri: String, newName: String) {
        context.dataStore.edit { preferences ->
            val current = parseCustomBells(preferences[PreferenceKeys.CUSTOM_BELLS]).map {
                if (it.uri == uri) it.copy(name = newName) else it
            }
            preferences[PreferenceKeys.CUSTOM_BELLS] = encodeCustomBells(current)
        }
    }

    suspend fun removeCustomBell(uri: String) {
        context.dataStore.edit { preferences ->
            val current = parseCustomBells(preferences[PreferenceKeys.CUSTOM_BELLS]).filterNot { it.uri == uri }
            preferences[PreferenceKeys.CUSTOM_BELLS] = encodeCustomBells(current)
        }
    }

    suspend fun setSelectedVoiceName(voiceName: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.SELECTED_VOICE] = voiceName
        }
    }

    suspend fun setSpeechSpeed(speed: Float) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.SPEECH_SPEED] = speed
        }
    }

    suspend fun setSpeechPitch(pitch: Float) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.SPEECH_PITCH] = pitch
        }
    }

    suspend fun setDefaultBell(bellSound: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.DEFAULT_BELL] = bellSound
        }
    }

    suspend fun setDefaultVolume(volume: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.DEFAULT_VOLUME] = volume
        }
    }

    suspend fun setDefaultVoiceEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.DEFAULT_VOICE_ENABLED] = enabled
        }
    }

    suspend fun set24HourFormat(is24Hour: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.IS_24_HOUR] = is24Hour
        }
    }

    suspend fun setThemeMode(mode: AppThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.THEME_MODE] = mode.name
        }
    }

    suspend fun setDynamicColors(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.DYNAMIC_COLORS] = enabled
        }
    }
}
