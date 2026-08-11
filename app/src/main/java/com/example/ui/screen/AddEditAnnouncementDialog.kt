package com.example.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.model.Announcement
import com.example.domain.model.UserPreferences
import com.example.sound.AudioBellPlayer
import com.example.tts.TimeSpeechManager
import com.example.ui.components.ReusableDialog
import com.example.utils.TimeUtils

val DAYS_MAP = listOf(
    1 to "Mon",
    2 to "Tue",
    3 to "Wed",
    4 to "Thu",
    5 to "Fri",
    6 to "Sat",
    7 to "Sun"
)

val BELL_SOUND_OPTIONS = listOf(
    "Classic Chime",
    "Soft Bell",
    "Grandfather Clock",
    "Digital Chime",
    "Zen Bowl",
    "Tower Bell"
)

val SUGGESTED_TAGS = listOf(
    "Study", "Work", "Medicine", "Workout", "Meeting",
    "Break", "Lunch", "Reading", "Sleep", "Travel"
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditAnnouncementDialog(
    initialAnnouncement: Announcement? = null,
    userPreferences: UserPreferences = UserPreferences(),
    existingAnnouncements: List<Announcement> = emptyList(),
    is24Hour: Boolean = false,
    audioBellPlayer: AudioBellPlayer? = null,
    timeSpeechManager: TimeSpeechManager? = null,
    onDismiss: () -> Unit,
    onSave: (Announcement) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val initialHour = initialAnnouncement?.hour ?: 12
    val initialMinute = initialAnnouncement?.minute ?: 0
    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = is24Hour
    )

    var selectedBell by remember {
        mutableStateOf(
            initialAnnouncement?.bellSound ?: userPreferences.defaultBell
        )
    }
    var bellDropdownExpanded by remember { mutableStateOf(false) }
    var voiceEnabled by remember {
        mutableStateOf(
            initialAnnouncement?.voiceEnabled ?: userPreferences.defaultVoiceEnabled
        )
    }
    var customMessage by remember { mutableStateOf(initialAnnouncement?.customMessage ?: "") }
    var tag by remember { mutableStateOf(initialAnnouncement?.tag ?: "") }
    var volume by remember {
        mutableStateOf(
            initialAnnouncement?.volume ?: userPreferences.defaultVolume
        )
    }
    var repeatDays by remember {
        mutableStateOf(
            initialAnnouncement?.repeatDays ?: listOf(1, 2, 3, 4, 5, 6, 7)
        )
    }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var warningMessage by remember { mutableStateOf<String?>(null) }
    var testMessage by remember { mutableStateOf<String?>(null) }

    val context = androidx.compose.ui.platform.LocalContext.current

    // Priority 2: Soft Haptic Ticks for Time Picker Wheels
    var isFirstPickerState by remember { mutableStateOf(true) }
    LaunchedEffect(timePickerState.hour, timePickerState.minute, timePickerState.isAfternoon) {
        if (!isFirstPickerState) {
            try {
                val vibrator = context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as? android.os.Vibrator
                if (vibrator != null && vibrator.hasVibrator()) {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                        vibrator.vibrate(android.os.VibrationEffect.createPredefined(android.os.VibrationEffect.EFFECT_TICK))
                    } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        vibrator.vibrate(android.os.VibrationEffect.createOneShot(5, 30))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(5)
                    }
                }
            } catch (e: Exception) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
        } else {
            isFirstPickerState = false
        }
    }

    // Live Spoken Preview calculation
    val liveSpokenWords = remember(timePickerState.hour, timePickerState.minute, customMessage, voiceEnabled) {
        if (voiceEnabled) {
            TimeUtils.toSpokenWords(
                timePickerState.hour,
                timePickerState.minute,
                customMessage.ifBlank { null }
            )
        } else {
            "Bell chime only"
        }
    }

    ReusableDialog(
        title = if (initialAnnouncement == null) "Add Announcement" else "Edit Announcement",
        onDismissRequest = onDismiss,
        confirmButtonText = "Save",
        onConfirm = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            if (repeatDays.isEmpty()) {
                errorMessage = "Please select at least one day for repeat schedule."
                return@ReusableDialog
            }

            val isDuplicateTime = existingAnnouncements.any {
                it.id != (initialAnnouncement?.id ?: 0L) &&
                        it.hour == timePickerState.hour &&
                        it.minute == timePickerState.minute
            }

            if (isDuplicateTime && warningMessage == null) {
                warningMessage = "An announcement is already set for this exact time. Tap Save again to proceed."
                return@ReusableDialog
            }

            val announcement = Announcement(
                id = initialAnnouncement?.id ?: 0L,
                hour = timePickerState.hour,
                minute = timePickerState.minute,
                enabled = initialAnnouncement?.enabled ?: true,
                repeatDays = repeatDays,
                bellSound = selectedBell,
                volume = volume,
                voiceEnabled = voiceEnabled,
                customMessage = customMessage.ifBlank { null },
                tag = tag.ifBlank { null },
                createdAt = initialAnnouncement?.createdAt ?: System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            onSave(announcement)
            onDismiss()
        },
        dismissButtonText = "Cancel",
        onDismiss = onDismiss,
        testTag = "add_edit_announcement_dialog"
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            if (warningMessage != null) {
                Text(
                    text = warningMessage!!,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            if (testMessage != null) {
                Text(
                    text = testMessage!!,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            // Time Picker
            TimePicker(
                state = timePickerState,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .testTag("dialog_time_picker")
            )

            // Live Preview Card (Compact Material 3 Card - Priority 4)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("live_speech_preview_card"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "🔔 ${TimeUtils.formatBellDisplayName(selectedBell, userPreferences.customBells)}",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "\"$liveSpokenWords\"",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                    OutlinedButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            audioBellPlayer?.playBell(selectedBell, volume)
                            if (voiceEnabled) {
                                timeSpeechManager?.speak(
                                    liveSpokenWords,
                                    userPreferences.speechSpeed,
                                    userPreferences.speechPitch
                                )
                            }
                            testMessage = "Playing test preview..."
                        },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("test_announcement_button")
                    ) {
                        Icon(Icons.Outlined.PlayArrow, contentDescription = "Test", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Preview", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            // Repeat Days Section
            Column {
                Text(
                    text = "Repeat Days",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Spacer(modifier = Modifier.height(6.dp))

                // Quick preset buttons
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    InputChip(
                        selected = repeatDays.size == 7,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            repeatDays = listOf(1, 2, 3, 4, 5, 6, 7)
                        },
                        label = { Text("Daily", softWrap = false) },
                        modifier = Modifier.testTag("chip_daily")
                    )
                    InputChip(
                        selected = repeatDays.sorted() == listOf(1, 2, 3, 4, 5),
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            repeatDays = listOf(1, 2, 3, 4, 5)
                        },
                        label = { Text("Weekdays", softWrap = false) },
                        modifier = Modifier.testTag("chip_weekdays")
                    )
                    InputChip(
                        selected = repeatDays.sorted() == listOf(6, 7),
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            repeatDays = listOf(6, 7)
                        },
                        label = { Text("Weekends", softWrap = false) },
                        modifier = Modifier.testTag("chip_weekends")
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Day Selection Chips
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    DAYS_MAP.forEach { (dayInt, dayLabel) ->
                        val isSelected = repeatDays.contains(dayInt)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                repeatDays = if (isSelected) {
                                    repeatDays - dayInt
                                } else {
                                    (repeatDays + dayInt).sorted()
                                }
                                errorMessage = null
                            },
                            label = { Text(dayLabel, fontWeight = FontWeight.Bold, softWrap = false) },
                            modifier = Modifier.testTag("chip_day_$dayInt")
                        )
                    }
                }
            }

            // Bell Sound Selection
            val selectedBellDisplayName = remember(selectedBell, userPreferences.customBells) {
                TimeUtils.formatBellDisplayName(selectedBell, userPreferences.customBells)
            }

            ExposedDropdownMenuBox(
                expanded = bellDropdownExpanded,
                onExpandedChange = { bellDropdownExpanded = !bellDropdownExpanded }
            ) {
                OutlinedTextField(
                    value = selectedBellDisplayName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Bell Sound") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = bellDropdownExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                        .testTag("bell_sound_dropdown"),
                    shape = RoundedCornerShape(12.dp)
                )

                ExposedDropdownMenu(
                    expanded = bellDropdownExpanded,
                    onDismissRequest = { bellDropdownExpanded = false }
                ) {
                    userPreferences.customBells.forEach { customBell ->
                        DropdownMenuItem(
                            text = { Text("🎵 ${customBell.name}") },
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                selectedBell = customBell.uri
                                bellDropdownExpanded = false
                                audioBellPlayer?.playBell(customBell.uri, volume, context)
                            }
                        )
                    }

                    BELL_SOUND_OPTIONS.forEach { bell ->
                        DropdownMenuItem(
                            text = { Text(bell) },
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                selectedBell = bell
                                bellDropdownExpanded = false
                                audioBellPlayer?.playBell(bell, volume, context)
                            }
                        )
                    }
                }
            }

            // Speech Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Voice Announcement",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        text = "Speak current time after bell chime",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = voiceEnabled,
                    onCheckedChange = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        voiceEnabled = it
                    },
                    modifier = Modifier.testTag("dialog_voice_switch")
                )
            }

            // Custom Speech Message
            if (voiceEnabled) {
                OutlinedTextField(
                    value = customMessage,
                    onValueChange = { customMessage = it },
                    label = { Text("Custom Message (Optional)") },
                    placeholder = { Text("e.g. Time for afternoon break!") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_custom_message_input"),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Tag / Label Section
            Column {
                Text(
                    text = "Tag / Label (Optional)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SUGGESTED_TAGS.forEach { tagOption ->
                        val isSelected = tag.equals(tagOption, ignoreCase = true)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                tag = if (isSelected) "" else tagOption
                            },
                            label = { Text(tagOption, style = MaterialTheme.typography.labelMedium, softWrap = false) },
                            modifier = Modifier.testTag("tag_chip_$tagOption")
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = tag,
                    onValueChange = { tag = it },
                    label = { Text("Custom Tag") },
                    placeholder = { Text("e.g. Study, Work, Medicine") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_tag_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            // Volume Slider
            Column {
                Text(
                    text = "Chime Volume ($volume%)",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                )
                Slider(
                    value = volume.toFloat(),
                    onValueChange = {
                        volume = it.toInt()
                        audioBellPlayer?.setVolume(it.toInt())
                    },
                    onValueChangeFinished = {
                        audioBellPlayer?.playBell(selectedBell, volume)
                    },
                    valueRange = 0f..100f,
                    steps = 100,
                    modifier = Modifier.testTag("dialog_volume_slider")
                )
            }
        }
    }
}
