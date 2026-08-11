package com.example.domain.usecase

import com.example.domain.model.Announcement

sealed class ValidationResult {
    object Success : ValidationResult()
    data class Error(val message: String) : ValidationResult()
    data class Warning(val message: String) : ValidationResult()
}

class ValidateAnnouncementUseCase {

    fun validate(
        announcement: Announcement,
        existingList: List<Announcement>
    ): ValidationResult {
        if (announcement.hour !in 0..23) {
            return ValidationResult.Error("Hour must be between 0 and 23")
        }
        if (announcement.minute !in 0..59) {
            return ValidationResult.Error("Minute must be between 0 and 59")
        }
        if (announcement.volume !in 0..100) {
            return ValidationResult.Error("Volume must be between 0 and 100")
        }

        val hasExactDuplicateTime = existingList.any {
            it.id != announcement.id && it.hour == announcement.hour && it.minute == announcement.minute
        }

        if (hasExactDuplicateTime) {
            return ValidationResult.Warning("An announcement is already scheduled at this exact time.")
        }

        return ValidationResult.Success
    }
}
