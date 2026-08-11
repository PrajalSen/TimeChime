package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.di.AppContainer
import com.example.domain.model.Announcement
import com.example.domain.model.UserPreferences
import com.example.domain.usecase.BulkDeleteAnnouncementsUseCase
import com.example.domain.usecase.DeleteAnnouncementUseCase
import com.example.domain.usecase.DuplicateAnnouncementUseCase
import com.example.domain.usecase.GetAnnouncementsUseCase
import com.example.domain.usecase.GetUserPreferencesUseCase
import com.example.domain.usecase.SaveAnnouncementUseCase
import com.example.domain.usecase.ToggleAnnouncementUseCase
import com.example.utils.TimePeriod
import com.example.utils.TimeUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ScheduleFilter(val label: String) {
    ALL("All"),
    ENABLED("Enabled"),
    DISABLED("Disabled"),
    MORNING("Morning"),
    AFTERNOON("Afternoon"),
    EVENING("Evening"),
    NIGHT("Night")
}

enum class ScheduleSort(val label: String) {
    TIME_ASC("Time (Earliest)"),
    TIME_DESC("Time (Latest)"),
    CREATED_DESC("Newest First"),
    BELL_NAME("Bell Sound")
}

data class ScheduleUiState(
    val searchQuery: String = "",
    val activeFilter: ScheduleFilter = ScheduleFilter.ALL,
    val activeSort: ScheduleSort = ScheduleSort.TIME_ASC,
    val isGroupedByPeriod: Boolean = true,
    val announcements: List<Announcement> = emptyList(),
    val groupedAnnouncements: Map<TimePeriod, List<Announcement>> = emptyMap(),
    val selectedIds: Set<Long> = emptySet(),
    val isSelectionMode: Boolean = false,
    val userPreferences: UserPreferences = UserPreferences(),
    val userMessage: String? = null
)

private data class FilterAndOptions(
    val query: String,
    val filter: ScheduleFilter,
    val sort: ScheduleSort,
    val isGrouped: Boolean,
    val selected: Set<Long>,
    val userMessage: String?
)

class ScheduleViewModel(
    getAnnouncementsUseCase: GetAnnouncementsUseCase,
    getUserPreferencesUseCase: GetUserPreferencesUseCase,
    private val toggleAnnouncementUseCase: ToggleAnnouncementUseCase,
    private val saveAnnouncementUseCase: SaveAnnouncementUseCase,
    private val deleteAnnouncementUseCase: DeleteAnnouncementUseCase,
    private val bulkDeleteAnnouncementsUseCase: BulkDeleteAnnouncementsUseCase
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _filter = MutableStateFlow(ScheduleFilter.ALL)
    private val _sort = MutableStateFlow(ScheduleSort.TIME_ASC)
    private val _isGrouped = MutableStateFlow(true)
    private val _selectedIds = MutableStateFlow<Set<Long>>(emptySet())
    private val _userMessage = MutableStateFlow<String?>(null)

    private val _searchAndFilterFlow = combine(_searchQuery, _filter, _sort) { query, filter, sort ->
        Triple(query, filter, sort)
    }

    private val _uiOptionsFlow = combine(_isGrouped, _selectedIds, _userMessage) { isGrouped, selected, msg ->
        Triple(isGrouped, selected, msg)
    }

    private val _optionsFlow = combine(_searchAndFilterFlow, _uiOptionsFlow) { sf, ui ->
        FilterAndOptions(sf.first, sf.second, sf.third, ui.first, ui.second, ui.third)
    }

    val uiState: StateFlow<ScheduleUiState> = combine(
        getAnnouncementsUseCase(),
        getUserPreferencesUseCase(),
        _optionsFlow
    ) { list, prefs, options ->
        // 1. Search Filter
        val searchFiltered = if (options.query.isBlank()) {
            list
        } else {
            list.filter { item ->
                val timeStr = TimeUtils.formatTime(item.hour, item.minute, prefs.is24HourFormat)
                timeStr.contains(options.query, ignoreCase = true) ||
                        item.bellSound.contains(options.query, ignoreCase = true) ||
                        (item.tag?.contains(options.query, ignoreCase = true) == true) ||
                        (item.customMessage?.contains(options.query, ignoreCase = true) == true)
            }
        }

        // 2. Category Filter
        val categoryFiltered = when (options.filter) {
            ScheduleFilter.ALL -> searchFiltered
            ScheduleFilter.ENABLED -> searchFiltered.filter { it.enabled }
            ScheduleFilter.DISABLED -> searchFiltered.filter { !it.enabled }
            ScheduleFilter.MORNING -> searchFiltered.filter { TimePeriod.fromHour(it.hour) == TimePeriod.MORNING }
            ScheduleFilter.AFTERNOON -> searchFiltered.filter { TimePeriod.fromHour(it.hour) == TimePeriod.AFTERNOON }
            ScheduleFilter.EVENING -> searchFiltered.filter { TimePeriod.fromHour(it.hour) == TimePeriod.EVENING }
            ScheduleFilter.NIGHT -> searchFiltered.filter { TimePeriod.fromHour(it.hour) == TimePeriod.NIGHT }
        }

        // 3. Sorting
        val sortedList = when (options.sort) {
            ScheduleSort.TIME_ASC -> categoryFiltered.sortedWith(compareBy({ it.hour }, { it.minute }))
            ScheduleSort.TIME_DESC -> categoryFiltered.sortedWith(compareByDescending<Announcement> { it.hour }.thenByDescending { it.minute })
            ScheduleSort.CREATED_DESC -> categoryFiltered.sortedByDescending { it.createdAt }
            ScheduleSort.BELL_NAME -> categoryFiltered.sortedBy { it.bellSound }
        }

        // 4. Time Period Grouping
        val groupedMap = sortedList.groupBy { TimePeriod.fromHour(it.hour) }

        ScheduleUiState(
            searchQuery = options.query,
            activeFilter = options.filter,
            activeSort = options.sort,
            isGroupedByPeriod = options.isGrouped,
            announcements = sortedList,
            groupedAnnouncements = groupedMap,
            selectedIds = options.selected,
            isSelectionMode = options.selected.isNotEmpty(),
            userPreferences = prefs,
            userMessage = options.userMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ScheduleUiState()
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun setFilter(filter: ScheduleFilter) {
        _filter.value = filter
    }

    fun setSort(sort: ScheduleSort) {
        _sort.value = sort
    }

    fun toggleGrouping() {
        _isGrouped.value = !_isGrouped.value
    }

    fun toggleSelection(id: Long) {
        val current = _selectedIds.value
        _selectedIds.value = if (current.contains(id)) {
            current - id
        } else {
            current + id
        }
    }

    fun selectAll(announcementIds: List<Long>) {
        _selectedIds.value = announcementIds.toSet()
    }

    fun clearSelection() {
        _selectedIds.value = emptySet()
    }

    fun toggleAnnouncement(id: Long, enabled: Boolean) {
        viewModelScope.launch {
            toggleAnnouncementUseCase(id, enabled)
        }
    }

    fun saveAnnouncement(announcement: Announcement) {
        viewModelScope.launch {
            saveAnnouncementUseCase(announcement)
            _userMessage.value = "Announcement saved successfully."
        }
    }

    fun deleteAnnouncement(announcement: Announcement) {
        viewModelScope.launch {
            deleteAnnouncementUseCase(announcement)
            _userMessage.value = "Announcement deleted."
        }
    }

    fun deleteSelectedAnnouncements() {
        val selectedIds = _selectedIds.value
        val ids = selectedIds.toList()
        if (ids.isNotEmpty()) {
            viewModelScope.launch {
                bulkDeleteAnnouncementsUseCase(ids)
                _selectedIds.value = emptySet()
                _userMessage.value = "${ids.size} announcements deleted."
            }
        }
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    companion object {
        fun factory(container: AppContainer) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ScheduleViewModel(
                    container.getAnnouncementsUseCase,
                    container.getUserPreferencesUseCase,
                    container.toggleAnnouncementUseCase,
                    container.saveAnnouncementUseCase,
                    container.deleteAnnouncementUseCase,
                    container.bulkDeleteAnnouncementsUseCase
                ) as T
            }
        }
    }
}
