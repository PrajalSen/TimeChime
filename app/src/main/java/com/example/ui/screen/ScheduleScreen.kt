package com.example.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SelectAll
import androidx.compose.material.icons.outlined.Sort
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.Announcement
import com.example.sound.AudioBellPlayer
import com.example.tts.TimeSpeechManager
import com.example.ui.components.EmptyState
import com.example.ui.components.ScheduleCard
import com.example.ui.components.SectionHeader
import com.example.ui.components.TimeChimeTopBar
import com.example.ui.viewmodel.ScheduleFilter
import com.example.ui.viewmodel.ScheduleSort
import com.example.ui.viewmodel.ScheduleViewModel
import com.example.utils.TimePeriod

@Composable
fun ScheduleScreen(
    viewModel: ScheduleViewModel,
    audioBellPlayer: AudioBellPlayer? = null,
    timeSpeechManager: TimeSpeechManager? = null,
    onAddClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var editingAnnouncement by remember { mutableStateOf<Announcement?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }
    var isSearchExpanded by remember { mutableStateOf(false) }
    var announcementToDelete by remember { mutableStateOf<Announcement?>(null) }
    var showBulkDeleteConfirm by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()
    var isFabVisible by remember { mutableStateOf(true) }
    var lastIndex by remember { mutableIntStateOf(0) }
    var lastOffset by remember { mutableIntStateOf(0) }

    LaunchedEffect(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset) {
        val currentIndex = listState.firstVisibleItemIndex
        val currentOffset = listState.firstVisibleItemScrollOffset
        if (currentIndex > lastIndex || (currentIndex == lastIndex && currentOffset > lastOffset + 12)) {
            isFabVisible = false
        } else if (currentIndex < lastIndex || (currentIndex == lastIndex && currentOffset < lastOffset - 12)) {
            isFabVisible = true
        }
        lastIndex = currentIndex
        lastOffset = currentOffset
    }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearUserMessage()
        }
    }

    if (announcementToDelete != null) {
        AlertDialog(
            onDismissRequest = { announcementToDelete = null },
            title = { Text("Delete Announcement?", fontWeight = FontWeight.Bold) },
            text = { Text("This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        announcementToDelete?.let { viewModel.deleteAnnouncement(it) }
                        announcementToDelete = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { announcementToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showBulkDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showBulkDeleteConfirm = false },
            title = { Text("Delete Selected Announcements?", fontWeight = FontWeight.Bold) },
            text = { Text("This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteSelectedAnnouncements()
                        showBulkDeleteConfirm = false
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBulkDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            if (uiState.isSelectionMode) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    tonalElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { viewModel.clearSelection() }) {
                                Icon(Icons.Outlined.Clear, contentDescription = "Clear Selection")
                            }
                            Text(
                                text = "${uiState.selectedIds.size} Selected",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        Row {
                            IconButton(onClick = {
                                viewModel.selectAll(uiState.announcements.map { it.id })
                            }) {
                                Icon(Icons.Outlined.SelectAll, contentDescription = "Select All")
                            }
                            IconButton(onClick = {
                                showBulkDeleteConfirm = true
                            }) {
                                Icon(
                                    Icons.Outlined.Delete,
                                    contentDescription = "Delete Selected",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            } else if (isSearchExpanded) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = uiState.searchQuery,
                            onValueChange = { viewModel.onSearchQueryChanged(it) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("schedule_search_input"),
                            placeholder = {
                                Text("Search schedules...")
                            },
                            leadingIcon = {
                                IconButton(onClick = {
                                    isSearchExpanded = false
                                    viewModel.onSearchQueryChanged("")
                                }) {
                                    Icon(
                                        imageVector = Icons.Outlined.Clear,
                                        contentDescription = "Close Search"
                                    )
                                }
                            },
                            trailingIcon = {
                                if (uiState.searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                        Icon(Icons.Outlined.Clear, contentDescription = "Clear text")
                                    }
                                }
                            },
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true
                        )
                    }
                }
            } else {
                TimeChimeTopBar(
                    title = "All Schedules",
                    actions = {
                        IconButton(onClick = { isSearchExpanded = true }) {
                            Icon(Icons.Outlined.Search, contentDescription = "Search Schedules")
                        }
                    }
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            AnimatedVisibility(
                visible = isFabVisible,
                enter = fadeIn(animationSpec = tween(250)) + scaleIn(initialScale = 0.8f, animationSpec = tween(250)),
                exit = fadeOut(animationSpec = tween(250)) + scaleOut(targetScale = 0.8f, animationSpec = tween(250))
            ) {
                ExtendedFloatingActionButton(
                    onClick = onAddClick,
                    icon = { Icon(Icons.Filled.Add, contentDescription = "Add Schedule") },
                    text = { Text("Add Time", fontWeight = FontWeight.Bold) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.testTag("schedule_add_fab")
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .testTag("schedule_screen_content")
        ) {
            // Category Filter Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(ScheduleFilter.values()) { filter ->
                    val isSelected = uiState.activeFilter == filter
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setFilter(filter) },
                        label = { Text(filter.label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.testTag("filter_chip_${filter.name}")
                    )
                }
            }

            // Controls Bar: Sort & Grouping
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Grouping toggle
                FilterChip(
                    selected = uiState.isGroupedByPeriod,
                    onClick = { viewModel.toggleGrouping() },
                    label = { Text(if (uiState.isGroupedByPeriod) "Grouped" else "Flat List") },
                    leadingIcon = { Icon(Icons.Outlined.FilterList, contentDescription = null) },
                    modifier = Modifier.testTag("toggle_grouping_chip")
                )

                // Sort Dropdown Button
                Box {
                    OutlinedButton(
                        onClick = { showSortMenu = true },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Sort,
                            contentDescription = "Sort",
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Text(uiState.activeSort.label)
                    }

                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        ScheduleSort.values().forEach { sort ->
                            DropdownMenuItem(
                                text = { Text(sort.label) },
                                onClick = {
                                    viewModel.setSort(sort)
                                    showSortMenu = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            if (uiState.announcements.isEmpty()) {
                EmptyState(
                    title = if (uiState.searchQuery.isNotBlank()) "No Matching Schedules" else "No Scheduled Announcements",
                    description = if (uiState.searchQuery.isNotBlank())
                        "Try searching for another time or clear your search/filters."
                    else
                        "You haven't set up any time announcements yet. Tap 'Add Time' to schedule one.",
                    icon = Icons.Outlined.CalendarToday,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (uiState.isGroupedByPeriod) {
                        TimePeriod.values().forEach { period ->
                            val periodItems = uiState.groupedAnnouncements[period] ?: emptyList()
                            if (periodItems.isNotEmpty()) {
                                item(key = "header_${period.name}") {
                                    SectionHeader(
                                        title = "${period.displayName} (${periodItems.size})",
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    )
                                }

                                items(
                                    items = periodItems,
                                    key = { it.id }
                                ) { announcement ->
                                    val isSelected = uiState.selectedIds.contains(announcement.id)
                                    ScheduleCard(
                                        announcement = announcement,
                                        is24HourFormat = uiState.userPreferences.is24HourFormat,
                                        isSelected = isSelected,
                                        isSelectionMode = uiState.isSelectionMode,
                                        onToggleEnabled = { enabled ->
                                            viewModel.toggleAnnouncement(announcement.id, enabled)
                                        },
                                        onClick = {
                                            if (uiState.isSelectionMode) {
                                                viewModel.toggleSelection(announcement.id)
                                            } else {
                                                editingAnnouncement = announcement
                                                showEditDialog = true
                                            }
                                        },
                                        onLongClick = {
                                            viewModel.toggleSelection(announcement.id)
                                        },
                                        onDelete = {
                                            announcementToDelete = announcement
                                        }
                                    )
                                }
                            }
                        }
                    } else {
                        items(
                            items = uiState.announcements,
                            key = { it.id }
                        ) { announcement ->
                            val isSelected = uiState.selectedIds.contains(announcement.id)
                            ScheduleCard(
                                announcement = announcement,
                                is24HourFormat = uiState.userPreferences.is24HourFormat,
                                isSelected = isSelected,
                                isSelectionMode = uiState.isSelectionMode,
                                onToggleEnabled = { enabled ->
                                    viewModel.toggleAnnouncement(announcement.id, enabled)
                                },
                                onClick = {
                                    if (uiState.isSelectionMode) {
                                        viewModel.toggleSelection(announcement.id)
                                    } else {
                                        editingAnnouncement = announcement
                                        showEditDialog = true
                                    }
                                },
                                onLongClick = {
                                    viewModel.toggleSelection(announcement.id)
                                },
                                onDelete = {
                                    announcementToDelete = announcement
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showEditDialog) {
        AddEditAnnouncementDialog(
            initialAnnouncement = editingAnnouncement,
            userPreferences = uiState.userPreferences,
            existingAnnouncements = uiState.announcements,
            is24Hour = uiState.userPreferences.is24HourFormat,
            audioBellPlayer = audioBellPlayer,
            timeSpeechManager = timeSpeechManager,
            onDismiss = {
                showEditDialog = false
                editingAnnouncement = null
            },
            onSave = { updated ->
                viewModel.saveAnnouncement(updated)
            }
        )
    }
}
