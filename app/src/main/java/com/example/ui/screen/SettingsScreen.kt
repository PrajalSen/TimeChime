package com.example.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.RecordVoiceOver
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.components.SectionHeader
import com.example.ui.components.SettingCard
import com.example.ui.components.TimeChimeTopBar

import androidx.compose.material.icons.outlined.Bedtime

@Composable
fun SettingsScreen(
    onNavigateToSpeechSettings: () -> Unit,
    onNavigateToBellSettings: () -> Unit,
    onNavigateToAppearance: () -> Unit,
    onNavigateToPermissions: () -> Unit,
    onNavigateToHourlyChimeFocus: () -> Unit,
    onNavigateToAbout: () -> Unit
) {
    Scaffold(
        topBar = {
            TimeChimeTopBar(
                title = "Settings"
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .testTag("settings_screen_content"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SectionHeader(title = "Audio & Speech")
            }

            item {
                SettingCard(
                    title = "Speech Settings",
                    subtitle = "Voice, speech speed, and pitch customization",
                    icon = Icons.Outlined.RecordVoiceOver,
                    onClick = onNavigateToSpeechSettings,
                    testTag = "setting_speech"
                )
            }

            item {
                SettingCard(
                    title = "Bell & Chime Sounds",
                    subtitle = "Default chime tones, volumes, and custom bells",
                    icon = Icons.Outlined.GraphicEq,
                    onClick = onNavigateToBellSettings,
                    testTag = "setting_bell"
                )
            }

            item {
                SectionHeader(title = "Schedules & Quiet Hours")
            }

            item {
                SettingCard(
                    title = "Hourly Chime & Focus Time",
                    subtitle = "Automatic hourly announcements and quiet hours",
                    icon = Icons.Outlined.Bedtime,
                    onClick = onNavigateToHourlyChimeFocus,
                    testTag = "setting_hourly_focus"
                )
            }

            item {
                SectionHeader(title = "App Preferences")
            }

            item {
                SettingCard(
                    title = "Appearance",
                    subtitle = "Dark Mode, Midnight Indigo, Rose Mist, and 24h format",
                    icon = Icons.Outlined.Palette,
                    onClick = onNavigateToAppearance,
                    testTag = "setting_appearance"
                )
            }

            item {
                SettingCard(
                    title = "Permissions",
                    subtitle = "Alarms, notifications, and battery optimization",
                    icon = Icons.Outlined.Security,
                    onClick = onNavigateToPermissions,
                    testTag = "setting_permissions"
                )
            }

            item {
                SectionHeader(title = "Information")
            }

            item {
                SettingCard(
                    title = "About TimeChime",
                    subtitle = "Version 1.0.0, terms, and system information",
                    icon = Icons.Outlined.Info,
                    onClick = onNavigateToAbout,
                    testTag = "setting_about"
                )
            }
        }
    }
}

