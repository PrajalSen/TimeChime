package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.di.AppContainer
import com.example.domain.model.Announcement
import com.example.domain.model.UserPreferences
import com.example.domain.usecase.DeleteAnnouncementUseCase
import com.example.domain.usecase.GetAnnouncementsUseCase
import com.example.domain.usecase.GetUserPreferencesUseCase
import com.example.domain.usecase.SaveAnnouncementUseCase
import com.example.domain.usecase.ToggleAnnouncementUseCase
import com.example.utils.GreetingUtils
import com.example.utils.TimeUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val greeting: String = GreetingUtils.getGreetingMessage(),
    val nextAnnouncementFormattedTime: String = "--:--",
    val nextAnnouncementCountdown: String = "No upcoming announcements",
    val nextAnnouncement: Announcement? = null,
    val totalAnnouncementsCount: Int = 0,
    val activeAnnouncementsCount: Int = 0,
    val disabledAnnouncementsCount: Int = 0,
    val todaysAnnouncementsCount: Int = 0,
    val todaysAnnouncements: List<Announcement> = emptyList(),
    val userPreferences: UserPreferences = UserPreferences(),
    val userMessage: String? = null
)

class HomeViewModel(
    getAnnouncementsUseCase: GetAnnouncementsUseCase,
    getUserPreferencesUseCase: GetUserPreferencesUseCase,
    private val toggleAnnouncementUseCase: ToggleAnnouncementUseCase,
    private val saveAnnouncementUseCase: SaveAnnouncementUseCase,
    private val deleteAnnouncementUseCase: DeleteAnnouncementUseCase
) : ViewModel() {

    private val _userMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<HomeUiState> = combine(
        getAnnouncementsUseCase(),
        getUserPreferencesUseCase(),
        _userMessage
    ) { announcements, prefs, userMsg ->
        val enabledAnnouncements = announcements.filter { it.enabled }
        val nowLocalTime = java.time.LocalTime.now()
        val currentMinutes = nowLocalTime.hour * 60 + nowLocalTime.minute

        val todaysList = announcements.filter { TimeUtils.isScheduledToday(it.repeatDays) }

        val upcomingToday = enabledAnnouncements.filter {
            TimeUtils.isScheduledToday(it.repeatDays) && (it.hour * 60 + it.minute) >= currentMinutes
        }.minByOrNull { it.hour * 60 + it.minute }

        val next = upcomingToday ?: enabledAnnouncements.minByOrNull { it.hour * 60 + it.minute }

        val nextFormatted = if (next != null) {
            TimeUtils.formatTime(next.hour, next.minute, prefs.is24HourFormat)
        } else {
            "--:--"
        }

        val countdown = if (next != null) {
            TimeUtils.calculateNextAnnouncementTime(next.hour, next.minute)
        } else {
            "No announcements scheduled"
        }

        HomeUiState(
            greeting = GreetingUtils.getGreetingMessage(),
            nextAnnouncementFormattedTime = nextFormatted,
            nextAnnouncementCountdown = countdown,
            nextAnnouncement = next,
            totalAnnouncementsCount = announcements.size,
            activeAnnouncementsCount = enabledAnnouncements.size,
            disabledAnnouncementsCount = announcements.size - enabledAnnouncements.size,
            todaysAnnouncementsCount = todaysList.size,
            todaysAnnouncements = todaysList,
            userPreferences = prefs,
            userMessage = userMsg
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

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

    fun clearUserMessage() {
        _userMessage.value = null
    }

    companion object {
        fun factory(container: AppContainer) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return HomeViewModel(
                    container.getAnnouncementsUseCase,
                    container.getUserPreferencesUseCase,
                    container.toggleAnnouncementUseCase,
                    container.saveAnnouncementUseCase,
                    container.deleteAnnouncementUseCase
                ) as T
            }
        }
    }
}

