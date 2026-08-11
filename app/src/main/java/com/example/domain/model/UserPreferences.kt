package com.example.domain.model

enum class AppThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
    AURORA_DARK,
    MIDNIGHT_INDIGO,
    AURORA_BLOOM,
    ROSE_MIST
}

enum class HourlyChimeMode {
    OFF,
    EVERY_HOUR,
    CUSTOM_RANGE
}

data class CustomBell(
    val name: String,
    val uri: String
)

data class UserPreferences(
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val speechSpeed: Float = 1.0f,
    val speechPitch: Float = 1.0f,
    val defaultBell: String = "Classic Chime",
    val defaultVolume: Int = 80,
    val defaultVoiceEnabled: Boolean = true,
    val is24HourFormat: Boolean = false,
    val dynamicColors: Boolean = true,
    val selectedVoiceName: String = "System Default",
    val customBells: List<CustomBell> = emptyList(),

    // Hourly Chime
    val hourlyChimeMode: HourlyChimeMode = HourlyChimeMode.OFF,
    val hourlyChimeStartHour: Int = 8,
    val hourlyChimeEndHour: Int = 22,
    val hourlyChimeDays: List<Int> = listOf(1, 2, 3, 4, 5, 6, 7), // 1=Mon .. 7=Sun
    val hourlyChimeVoiceEnabled: Boolean = false,

    // Focus Time (Quiet Hours)
    val focusTimeEnabled: Boolean = false,
    val focusStartHour: Int = 22,
    val focusStartMinute: Int = 0,
    val focusEndHour: Int = 7,
    val focusEndMinute: Int = 0,
    val focusDays: List<Int> = listOf(1, 2, 3, 4, 5, 6, 7),
    val focusAllowImportantAlarms: Boolean = false
)

