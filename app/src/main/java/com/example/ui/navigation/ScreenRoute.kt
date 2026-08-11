package com.example.ui.navigation

sealed class ScreenRoute(val route: String) {
    object Home : ScreenRoute("home")
    object Schedule : ScreenRoute("schedule")
    object Settings : ScreenRoute("settings")
    object SpeechSettings : ScreenRoute("speech_settings")
    object BellSettings : ScreenRoute("bell_settings")
    object AppearanceSettings : ScreenRoute("appearance_settings")
    object PermissionsSettings : ScreenRoute("permissions_settings")
    object HourlyChimeFocusSettings : ScreenRoute("hourly_chime_focus_settings")
    object About : ScreenRoute("about")

}
