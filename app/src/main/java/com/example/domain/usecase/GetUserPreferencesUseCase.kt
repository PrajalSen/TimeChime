package com.example.domain.usecase

import com.example.domain.model.UserPreferences
import com.example.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow

class GetUserPreferencesUseCase(
    private val repository: UserPreferencesRepository
) {
    operator fun invoke(): Flow<UserPreferences> {
        return repository.getUserPreferences()
    }
}
