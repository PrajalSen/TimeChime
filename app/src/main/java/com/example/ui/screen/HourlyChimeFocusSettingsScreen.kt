package com.example.ui.screen

import android.app.TimePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.HourlyChimeMode
import com.example.domain.model.UserPreferences

import com.example.ui.components.SectionHeader
import com.example.ui.components.TimeChimeTopBar
import com.example.ui.viewmodel.SettingsViewModel
import com.example.utils.TimeUtils

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HourlyChimeFocusSettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit
) {
    val prefs by viewModel.userPreferences.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val daysOfWeek = listOf(
        1 to "Mon", 2 to "Tue", 3 to "Wed", 4 to "Thu",
        5 to "Fri", 6 to "Sat", 7 to "Sun"
    )

    Scaffold(
        topBar = {
            TimeChimeTopBar(
                title = "Chime & Quiet Hours",
                onNavigateBack = onNavigateBack
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 1. Hourly Chime Section
            item {
                SectionHeader(title = "Hourly Chime")
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.NotificationsActive,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(end = 12.dp)
                            )
                            Column {
                                Text(
                                    text = "Hourly Time Announcement",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Automatically announce the time every hour without extra setup",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                RadioButton(
                                    selected = prefs.hourlyChimeMode == HourlyChimeMode.OFF,
                                    onClick = {
                                        viewModel.setHourlyChimeConfig(
                                            HourlyChimeMode.OFF,
                                            prefs.hourlyChimeStartHour,
                                            prefs.hourlyChimeEndHour,
                                            prefs.hourlyChimeDays
                                        )
                                    }
                                )
                                Text("Off", style = MaterialTheme.typography.bodyMedium)
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                RadioButton(
                                    selected = prefs.hourlyChimeMode == HourlyChimeMode.EVERY_HOUR,
                                    onClick = {
                                        viewModel.setHourlyChimeConfig(
                                            HourlyChimeMode.EVERY_HOUR,
                                            prefs.hourlyChimeStartHour,
                                            prefs.hourlyChimeEndHour,
                                            prefs.hourlyChimeDays
                                        )
                                    }
                                )
                                Text("Every Hour (24 Hours)", style = MaterialTheme.typography.bodyMedium)
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                RadioButton(
                                    selected = prefs.hourlyChimeMode == HourlyChimeMode.CUSTOM_RANGE,
                                    onClick = {
                                        viewModel.setHourlyChimeConfig(
                                            HourlyChimeMode.CUSTOM_RANGE,
                                            prefs.hourlyChimeStartHour,
                                            prefs.hourlyChimeEndHour,
                                            prefs.hourlyChimeDays
                                        )
                                    }
                                )
                                Text("Custom Hourly Range", style = MaterialTheme.typography.bodyMedium)
                            }
                        }

                        AnimatedVisibility(visible = prefs.hourlyChimeMode == HourlyChimeMode.CUSTOM_RANGE) {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(
                                    text = "Chime Time Window",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        onClick = {
                                            TimePickerDialog(
                                                context,
                                                { _, hourOfDay, _ ->
                                                    viewModel.setHourlyChimeConfig(
                                                        prefs.hourlyChimeMode,
                                                        hourOfDay,
                                                        prefs.hourlyChimeEndHour,
                                                        prefs.hourlyChimeDays
                                                    )
                                                },
                                                prefs.hourlyChimeStartHour,
                                                0,
                                                prefs.is24HourFormat
                                            ).show()
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.AccessTime,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Column {
                                                Text(
                                                    text = "Start Time",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 1,
                                                    softWrap = false
                                                )
                                                Text(
                                                    text = TimeUtils.formatTime(prefs.hourlyChimeStartHour, 0, prefs.is24HourFormat),
                                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                    maxLines = 1,
                                                    softWrap = false
                                                )
                                            }
                                        }
                                    }

                                    Text(
                                        text = "to",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Surface(
                                        onClick = {
                                            TimePickerDialog(
                                                context,
                                                { _, hourOfDay, _ ->
                                                    viewModel.setHourlyChimeConfig(
                                                        prefs.hourlyChimeMode,
                                                        prefs.hourlyChimeStartHour,
                                                        hourOfDay,
                                                        prefs.hourlyChimeDays
                                                    )
                                                },
                                                prefs.hourlyChimeEndHour,
                                                0,
                                                prefs.is24HourFormat
                                            ).show()
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.AccessTime,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Column {
                                                Text(
                                                    text = "End Time",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 1,
                                                    softWrap = false
                                                )
                                                Text(
                                                    text = TimeUtils.formatTime(prefs.hourlyChimeEndHour, 0, prefs.is24HourFormat),
                                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                    maxLines = 1,
                                                    softWrap = false
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (prefs.hourlyChimeMode != HourlyChimeMode.OFF) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "Voice Announcement",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Speaks current time after bell chime (e.g. \"It is 3 PM.\")",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    modifier = Modifier.padding(top = 4.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.clickable { viewModel.setHourlyChimeVoiceEnabled(false) }
                                    ) {
                                        RadioButton(
                                            selected = !prefs.hourlyChimeVoiceEnabled,
                                            onClick = { viewModel.setHourlyChimeVoiceEnabled(false) }
                                        )
                                        Text("Off", style = MaterialTheme.typography.bodyMedium)
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.clickable { viewModel.setHourlyChimeVoiceEnabled(true) }
                                    ) {
                                        RadioButton(
                                            selected = prefs.hourlyChimeVoiceEnabled,
                                            onClick = { viewModel.setHourlyChimeVoiceEnabled(true) }
                                        )
                                        Text("On", style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "Repeat Days",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )

                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    FilterChip(
                                        selected = prefs.hourlyChimeDays.size == 7,
                                        onClick = {
                                            viewModel.setHourlyChimeConfig(
                                                prefs.hourlyChimeMode,
                                                prefs.hourlyChimeStartHour,
                                                prefs.hourlyChimeEndHour,
                                                listOf(1, 2, 3, 4, 5, 6, 7)
                                            )
                                        },
                                        label = { Text("Every Day", softWrap = false) }
                                    )
                                    FilterChip(
                                        selected = prefs.hourlyChimeDays.sorted() == listOf(1, 2, 3, 4, 5),
                                        onClick = {
                                            viewModel.setHourlyChimeConfig(
                                                prefs.hourlyChimeMode,
                                                prefs.hourlyChimeStartHour,
                                                prefs.hourlyChimeEndHour,
                                                listOf(1, 2, 3, 4, 5)
                                            )
                                        },
                                        label = { Text("Weekdays", softWrap = false) }
                                    )
                                    FilterChip(
                                        selected = prefs.hourlyChimeDays.sorted() == listOf(6, 7),
                                        onClick = {
                                            viewModel.setHourlyChimeConfig(
                                                prefs.hourlyChimeMode,
                                                prefs.hourlyChimeStartHour,
                                                prefs.hourlyChimeEndHour,
                                                listOf(6, 7)
                                            )
                                        },
                                        label = { Text("Weekends", softWrap = false) }
                                    )
                                }

                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    daysOfWeek.forEach { (dayNum, label) ->
                                        val isSelected = prefs.hourlyChimeDays.contains(dayNum)
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = {
                                                val currentDays = prefs.hourlyChimeDays.toMutableList()
                                                if (isSelected) {
                                                    if (currentDays.size > 1) currentDays.remove(dayNum)
                                                } else {
                                                    currentDays.add(dayNum)
                                                }
                                                viewModel.setHourlyChimeConfig(
                                                    prefs.hourlyChimeMode,
                                                    prefs.hourlyChimeStartHour,
                                                    prefs.hourlyChimeEndHour,
                                                    currentDays.sorted()
                                                )
                                            },
                                            label = { Text(label, softWrap = false) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 2. Focus Time (Quiet Hours) Section
            item {
                SectionHeader(title = "Focus Time (Quiet Hours)")
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Bedtime,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(end = 12.dp)
                                )
                                Column {
                                    Text(
                                        text = "Enable Focus Time",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Silence all chimes during quiet or sleeping hours",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Switch(
                                checked = prefs.focusTimeEnabled,
                                onCheckedChange = { enabled ->
                                    viewModel.setFocusTimeConfig(
                                        enabled,
                                        prefs.focusStartHour,
                                        prefs.focusStartMinute,
                                        prefs.focusEndHour,
                                        prefs.focusEndMinute,
                                        prefs.focusDays,
                                        prefs.focusAllowImportantAlarms
                                    )
                                }
                            )
                        }

                        AnimatedVisibility(visible = prefs.focusTimeEnabled) {
                            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                Text(
                                    text = "Quiet Window",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        onClick = {
                                            TimePickerDialog(
                                                context,
                                                { _, hourOfDay, minute ->
                                                    viewModel.setFocusTimeConfig(
                                                        prefs.focusTimeEnabled,
                                                        hourOfDay,
                                                        minute,
                                                        prefs.focusEndHour,
                                                        prefs.focusEndMinute,
                                                        prefs.focusDays,
                                                        prefs.focusAllowImportantAlarms
                                                    )
                                                },
                                                prefs.focusStartHour,
                                                prefs.focusStartMinute,
                                                prefs.focusIs24Hour
                                            ).show()
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.AccessTime,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Column {
                                                Text(
                                                    text = "Start Time",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 1,
                                                    softWrap = false
                                                )
                                                Text(
                                                    text = TimeUtils.formatTime(prefs.focusStartHour, prefs.focusStartMinute, prefs.is24HourFormat),
                                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                    maxLines = 1,
                                                    softWrap = false
                                                )
                                            }
                                        }
                                    }

                                    Text(
                                        text = "to",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Surface(
                                        onClick = {
                                            TimePickerDialog(
                                                context,
                                                { _, hourOfDay, minute ->
                                                    viewModel.setFocusTimeConfig(
                                                        prefs.focusTimeEnabled,
                                                        prefs.focusStartHour,
                                                        prefs.focusStartMinute,
                                                        hourOfDay,
                                                        minute,
                                                        prefs.focusDays,
                                                        prefs.focusAllowImportantAlarms
                                                    )
                                                },
                                                prefs.focusEndHour,
                                                prefs.focusEndMinute,
                                                prefs.focusIs24Hour
                                            ).show()
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.AccessTime,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Column {
                                                Text(
                                                    text = "End Time",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 1,
                                                    softWrap = false
                                                )
                                                Text(
                                                    text = TimeUtils.formatTime(prefs.focusEndHour, prefs.focusEndMinute, prefs.is24HourFormat),
                                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                    maxLines = 1,
                                                    softWrap = false
                                                )
                                            }
                                        }
                                    }
                                }

                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = "Active Days",
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary
                                    )

                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        FilterChip(
                                            selected = prefs.focusDays.size == 7,
                                            onClick = {
                                                viewModel.setFocusTimeConfig(
                                                    prefs.focusTimeEnabled,
                                                    prefs.focusStartHour,
                                                    prefs.focusStartMinute,
                                                    prefs.focusEndHour,
                                                    prefs.focusEndMinute,
                                                    listOf(1, 2, 3, 4, 5, 6, 7),
                                                    prefs.focusAllowImportantAlarms
                                                )
                                            },
                                            label = { Text("Every Day", softWrap = false) }
                                        )
                                        FilterChip(
                                            selected = prefs.focusDays.sorted() == listOf(1, 2, 3, 4, 5),
                                            onClick = {
                                                viewModel.setFocusTimeConfig(
                                                    prefs.focusTimeEnabled,
                                                    prefs.focusStartHour,
                                                    prefs.focusStartMinute,
                                                    prefs.focusEndHour,
                                                    prefs.focusEndMinute,
                                                    listOf(1, 2, 3, 4, 5),
                                                    prefs.focusAllowImportantAlarms
                                                )
                                            },
                                            label = { Text("Weekdays", softWrap = false) }
                                        )
                                        FilterChip(
                                            selected = prefs.focusDays.sorted() == listOf(6, 7),
                                            onClick = {
                                                viewModel.setFocusTimeConfig(
                                                    prefs.focusTimeEnabled,
                                                    prefs.focusStartHour,
                                                    prefs.focusStartMinute,
                                                    prefs.focusEndHour,
                                                    prefs.focusEndMinute,
                                                    listOf(6, 7),
                                                    prefs.focusAllowImportantAlarms
                                                )
                                            },
                                            label = { Text("Weekends", softWrap = false) }
                                        )
                                    }

                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                                        verticalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        daysOfWeek.forEach { (dayNum, label) ->
                                            val isSelected = prefs.focusDays.contains(dayNum)
                                            FilterChip(
                                                selected = isSelected,
                                                onClick = {
                                                    val currentDays = prefs.focusDays.toMutableList()
                                                    if (isSelected) {
                                                        if (currentDays.size > 1) currentDays.remove(dayNum)
                                                    } else {
                                                        currentDays.add(dayNum)
                                                    }
                                                    viewModel.setFocusTimeConfig(
                                                        prefs.focusTimeEnabled,
                                                        prefs.focusStartHour,
                                                        prefs.focusStartMinute,
                                                        prefs.focusEndHour,
                                                        prefs.focusEndMinute,
                                                        currentDays.sorted(),
                                                        prefs.focusAllowImportantAlarms
                                                    )
                                                },
                                                label = { Text(label, softWrap = false) },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                                )
                                            )
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Allow Important Alarms",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                        )
                                        Text(
                                            text = "Let designated important chimes play during Focus Time",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Switch(
                                        checked = prefs.focusAllowImportantAlarms,
                                        onCheckedChange = { allow ->
                                            viewModel.setFocusTimeConfig(
                                                prefs.focusTimeEnabled,
                                                prefs.focusStartHour,
                                                prefs.focusStartMinute,
                                                prefs.focusEndHour,
                                                prefs.focusEndMinute,
                                                prefs.focusDays,
                                                allow
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private val UserPreferences.focusIs24Hour get() = this.is24HourFormat
