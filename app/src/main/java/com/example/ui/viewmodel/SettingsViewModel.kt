package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.di.AppContainer
import com.example.domain.model.AppThemeMode
import com.example.domain.model.UserPreferences
import com.example.domain.usecase.GetUserPreferencesUseCase
import com.example.domain.usecase.UpdateUserPreferencesUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    getUserPreferencesUseCase: GetUserPreferencesUseCase,
    private val updateUserPreferencesUseCase: UpdateUserPreferencesUseCase
) : ViewModel() {

    val userPreferences: StateFlow<UserPreferences> = getUserPreferencesUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferences()
        )

    fun setThemeMode(mode: AppThemeMode) {
        viewModelScope.launch {
            updateUserPreferencesUseCase.setThemeMode(mode)
        }
    }

    fun setSpeechSpeed(speed: Float) {
        viewModelScope.launch {
            updateUserPreferencesUseCase.setSpeechSpeed(speed)
        }
    }

    fun setSpeechPitch(pitch: Float) {
        viewModelScope.launch {
            updateUserPreferencesUseCase.setSpeechPitch(pitch)
        }
    }

    fun setDefaultBell(bell: String) {
        viewModelScope.launch {
            updateUserPreferencesUseCase.setDefaultBell(bell)
        }
    }

    fun set24HourFormat(is24Hour: Boolean) {
        viewModelScope.launch {
            updateUserPreferencesUseCase.set24HourFormat(is24Hour)
        }
    }

    fun setDynamicColors(enabled: Boolean) {
        viewModelScope.launch {
            updateUserPreferencesUseCase.setDynamicColors(enabled)
        }
    }

    fun setSelectedVoiceName(voiceName: String) {
        viewModelScope.launch {
            updateUserPreferencesUseCase.setSelectedVoiceName(voiceName)
        }
    }

    fun addCustomBell(name: String, uri: String) {
        viewModelScope.launch {
            updateUserPreferencesUseCase.addCustomBell(name, uri)
        }
    }

    fun renameCustomBell(uri: String, newName: String) {
        viewModelScope.launch {
            updateUserPreferencesUseCase.renameCustomBell(uri, newName)
        }
    }

    fun removeCustomBell(uri: String) {
        viewModelScope.launch {
            updateUserPreferencesUseCase.removeCustomBell(uri)
        }
    }

    fun setHourlyChimeConfig(mode: com.example.domain.model.HourlyChimeMode, startHour: Int, endHour: Int, days: List<Int>) {
        viewModelScope.launch {
            updateUserPreferencesUseCase.setHourlyChimeConfig(mode, startHour, endHour, days)
        }
    }

    fun setHourlyChimeVoiceEnabled(enabled: Boolean) {
        viewModelScope.launch {
            updateUserPreferencesUseCase.setHourlyChimeVoiceEnabled(enabled)
        }
    }

    fun setFocusTimeConfig(enabled: Boolean, startHour: Int, startMinute: Int, endHour: Int, endMinute: Int, days: List<Int>, allowImportant: Boolean) {
        viewModelScope.launch {
            updateUserPreferencesUseCase.setFocusTimeConfig(enabled, startHour, startMinute, endHour, endMinute, days, allowImportant)
        }
    }


    companion object {
        fun factory(container: AppContainer) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SettingsViewModel(
                    container.getUserPreferencesUseCase,
                    container.updateUserPreferencesUseCase
                ) as T
            }
        }
    }
}
