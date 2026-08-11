package com.example.ui.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.CustomBell
import com.example.sound.AudioBellPlayer
import com.example.ui.components.SectionHeader
import com.example.ui.components.TimeChimeTopBar
import com.example.ui.viewmodel.SettingsViewModel

private val BELL_SUBTITLES = mapOf(
    "Classic Chime" to "Clear digital notification",
    "Soft Bell" to "Gentle calming tone",
    "Grandfather Clock" to "Traditional clock strike",
    "Digital Chime" to "Crisp modern chime",
    "Zen Bowl" to "Soothing singing bowl",
    "Tower Bell" to "Deep resonant chime"
)

@Composable
fun BellSettingsScreen(
    viewModel: SettingsViewModel,
    audioBellPlayer: AudioBellPlayer? = null,
    onNavigateBack: () -> Unit
) {
    val prefs by viewModel.userPreferences.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current

    var customBellToRename by remember { mutableStateOf<CustomBell?>(null) }
    var renameText by remember { mutableStateOf("") }
    var customBellToDelete by remember { mutableStateOf<CustomBell?>(null) }

    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { pickedUri ->
            try {
                try {
                    context.contentResolver.takePersistableUriPermission(
                        pickedUri,
                        android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (e: Exception) {
                    android.util.Log.e("BellSettingsScreen", "Failed to take persistable URI permission", e)
                }

                var fileSize = -1L
                try {
                    context.contentResolver.openFileDescriptor(pickedUri, "r")?.use { pfd ->
                        fileSize = pfd.statSize
                    }
                } catch (_: Exception) {}

                if (fileSize > 2 * 1024 * 1024) {
                    android.widget.Toast.makeText(context, "Audio file exceeds 2 MB size limit.", android.widget.Toast.LENGTH_LONG).show()
                    return@let
                }

                var durationMs = 0L
                val retriever = android.media.MediaMetadataRetriever()
                try {
                    retriever.setDataSource(context, pickedUri)
                    val durStr = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION)
                    durationMs = durStr?.toLongOrNull() ?: 0L
                } catch (_: Exception) {
                } finally {
                    try { retriever.release() } catch (_: Exception) {}
                }

                if (durationMs > 10_000L) {
                    android.widget.Toast.makeText(context, "Audio length exceeds 10 seconds limit.", android.widget.Toast.LENGTH_LONG).show()
                    return@let
                }

                val cleanName = com.example.utils.TimeUtils.getDisplayNameFromUri(context, pickedUri)

                viewModel.addCustomBell(name = cleanName, uri = pickedUri.toString())
                viewModel.setDefaultBell(pickedUri.toString())
                audioBellPlayer?.playBell(pickedUri.toString(), prefs.defaultVolume, context)
            } catch (e: Exception) {
                android.widget.Toast.makeText(context, "Error loading custom bell file.", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TimeChimeTopBar(
                title = "Bell Settings",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .testTag("bell_settings_content"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // MY CUSTOM BELLS CARD OVERVIEW
            item {
                SectionHeader(title = "My Custom Bells")
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "🎵 My Custom Bells",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Personalize your announcements with your favorite notification sounds.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Supported: MP3 • WAV • OGG • M4A",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Maximum: 10 seconds • 2 MB",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Button(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                audioPickerLauncher.launch("audio/*")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(vertical = 12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add Custom Bell")
                        }
                    }
                }
            }

            if (prefs.customBells.isNotEmpty()) {
                items(prefs.customBells, key = { it.uri }) { customBell ->
                    val isSelected = prefs.defaultBell == customBell.uri
                    val isReadable = remember(customBell.uri) {
                        try {
                            val parsedUri = Uri.parse(customBell.uri)
                            context.contentResolver.openInputStream(parsedUri)?.use { true } ?: false
                        } catch (e: Exception) {
                            false
                        }
                    }

                    val scaleAnimated by animateFloatAsState(
                        targetValue = if (isSelected) 1.02f else 1.0f,
                        animationSpec = tween(durationMillis = 250),
                        label = "cardScale"
                    )

                    val containerColorAnimated by animateColorAsState(
                        targetValue = if (isSelected)
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                        else
                            MaterialTheme.colorScheme.surface,
                        animationSpec = tween(durationMillis = 250),
                        label = "cardBg"
                    )

                    val borderColorAnimated by animateColorAsState(
                        targetValue = if (!isReadable)
                            MaterialTheme.colorScheme.error
                        else if (isSelected)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.outlineVariant,
                        animationSpec = tween(durationMillis = 250),
                        label = "cardBorder"
                    )

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .scale(scaleAnimated)
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                if (isReadable) {
                                    viewModel.setDefaultBell(customBell.uri)
                                    audioBellPlayer?.playBell(customBell.uri, prefs.defaultVolume, context)
                                } else {
                                    android.widget.Toast.makeText(context, "This custom bell is no longer available.", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            }
                            .testTag("custom_bell_${customBell.name}"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = containerColorAnimated),
                        border = BorderStroke(width = if (isSelected) 2.dp else 1.dp, color = borderColorAnimated)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.MusicNote,
                                        contentDescription = null,
                                        tint = if (!isReadable) MaterialTheme.colorScheme.error else if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Column(modifier = Modifier.padding(start = 12.dp)) {
                                        Text(
                                            text = "🎵 ${customBell.name}",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                            )
                                        )
                                        if (!isReadable) {
                                            Text(
                                                text = "This custom bell is no longer available.",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            if (isReadable) {
                                                audioBellPlayer?.playBell(customBell.uri, prefs.defaultVolume, context)
                                            } else {
                                                android.widget.Toast.makeText(context, "This custom bell is no longer available.", android.widget.Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.PlayArrow,
                                            contentDescription = "Preview sound",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            customBellToRename = customBell
                                            renameText = customBell.name
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Edit,
                                            contentDescription = "Rename custom sound",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    IconButton(
                                        onClick = { customBellToDelete = customBell }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Delete,
                                            contentDescription = "Remove custom sound",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            if (isReadable) {
                                                viewModel.setDefaultBell(customBell.uri)
                                                audioBellPlayer?.playBell(customBell.uri, prefs.defaultVolume, context)
                                            } else {
                                                android.widget.Toast.makeText(context, "This custom bell is no longer available.", android.widget.Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    )
                                }
                            }

                            if (!isReadable) {
                                OutlinedButton(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        audioPickerLauncher.launch("audio/*")
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Choose Another Bell")
                                }
                            }
                        }
                    }
                }
            }

            item {
                SectionHeader(title = "Built-in Chime Collection")
            }

            items(BELL_SOUND_OPTIONS) { bellName ->
                val isSelected = prefs.defaultBell == bellName

                val scaleAnimated by animateFloatAsState(
                    targetValue = if (isSelected) 1.02f else 1.0f,
                    animationSpec = tween(durationMillis = 250),
                    label = "builtinCardScale"
                )

                val containerColorAnimated by animateColorAsState(
                    targetValue = if (isSelected)
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                    else
                        MaterialTheme.colorScheme.surface,
                    animationSpec = tween(durationMillis = 250),
                    label = "builtinCardBg"
                )

                val borderColorAnimated by animateColorAsState(
                    targetValue = if (isSelected)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.outlineVariant,
                    animationSpec = tween(durationMillis = 250),
                    label = "builtinCardBorder"
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .scale(scaleAnimated)
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.setDefaultBell(bellName)
                            audioBellPlayer?.playBell(bellName, prefs.defaultVolume, context)
                        }
                        .testTag("bell_option_$bellName"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = containerColorAnimated),
                    border = BorderStroke(width = if (isSelected) 2.dp else 1.dp, color = borderColorAnimated)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.VolumeUp,
                                contentDescription = null,
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Column(modifier = Modifier.padding(start = 12.dp)) {
                                Text(
                                    text = bellName,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                )
                                Text(
                                    text = BELL_SUBTITLES[bellName] ?: "Chime notification",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        RadioButton(
                            selected = isSelected,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                viewModel.setDefaultBell(bellName)
                                audioBellPlayer?.playBell(bellName, prefs.defaultVolume, context)
                            }
                        )
                    }
                }
            }
        }
    }

    // Rename Custom Bell Dialog
    customBellToRename?.let { bell ->
        AlertDialog(
            onDismissRequest = { customBellToRename = null },
            title = { Text("Rename Custom Bell") },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    label = { Text("Bell Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (renameText.isNotBlank()) {
                            viewModel.renameCustomBell(bell.uri, renameText.trim())
                        }
                        customBellToRename = null
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { customBellToRename = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Custom Bell Dialog
    customBellToDelete?.let { bell ->
        AlertDialog(
            onDismissRequest = { customBellToDelete = null },
            title = { Text("Remove Custom Bell?") },
            text = { Text("Are you sure you want to remove '${bell.name}' from your custom sounds?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.removeCustomBell(bell.uri)
                        if (prefs.defaultBell == bell.uri) {
                            viewModel.setDefaultBell("Classic Chime")
                        }
                        customBellToDelete = null
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Remove")
                }
            },
            dismissButton = {
                TextButton(onClick = { customBellToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

