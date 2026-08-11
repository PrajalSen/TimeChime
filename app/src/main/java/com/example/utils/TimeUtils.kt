package com.example.utils

import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class TimePeriod(val displayName: String) {
    MORNING("Morning"),
    AFTERNOON("Afternoon"),
    EVENING("Evening"),
    NIGHT("Night");

    companion object {
        fun fromHour(hour: Int): TimePeriod {
            return when (hour) {
                in 5..11 -> MORNING
                in 12..16 -> AFTERNOON
                in 17..20 -> EVENING
                else -> NIGHT
            }
        }
    }
}

object TimeUtils {

    fun formatTime(hour: Int, minute: Int, is24HourFormat: Boolean = false): String {
        val time = LocalTime.of(hour, minute)
        val pattern = if (is24HourFormat) "HH:mm" else "h:mm a"
        return time.format(DateTimeFormatter.ofPattern(pattern, Locale.getDefault()))
    }

    fun calculateNextAnnouncementTime(hour: Int, minute: Int): String {
        val now = LocalTime.now()
        val target = LocalTime.of(hour, minute)

        val diffMinutes = if (target.isAfter(now)) {
            java.time.Duration.between(now, target).toMinutes()
        } else {
            java.time.Duration.between(now, target).plusDays(1).toMinutes()
        }

        val hours = diffMinutes / 60
        val mins = diffMinutes % 60

        return when {
            hours == 0L && mins == 0L -> "Due now"
            hours == 0L -> "In $mins min"
            mins == 0L -> "In $hours hr"
            else -> "In ${hours}h ${mins}m"
        }
    }

    fun formatRepeatDays(repeatDays: List<Int>): String {
        if (repeatDays.isEmpty() || repeatDays.size == 7) {
            return "Every day"
        }
        val sorted = repeatDays.sorted()
        if (sorted == listOf(1, 2, 3, 4, 5)) {
            return "Weekdays"
        }
        if (sorted == listOf(6, 7)) {
            return "Weekends"
        }

        val dayNames = mapOf(
            1 to "Mon", 2 to "Tue", 3 to "Wed", 4 to "Thu",
            5 to "Fri", 6 to "Sat", 7 to "Sun"
        )
        return sorted.mapNotNull { dayNames[it] }.joinToString(", ")
    }

    fun formatBellDisplayName(
        bellSound: String,
        customBells: List<com.example.domain.model.CustomBell> = emptyList()
    ): String {
        val matchingCustom = customBells.firstOrNull { it.uri == bellSound }
        if (matchingCustom != null && matchingCustom.name.isNotBlank()) {
            return matchingCustom.name
        }
        if (bellSound.contains("://") || bellSound.contains("/")) {
            val path = try {
                val uri = android.net.Uri.parse(bellSound)
                uri.lastPathSegment ?: bellSound
            } catch (_: Exception) {
                bellSound
            }
            val decoded = try { java.net.URLDecoder.decode(path, "UTF-8") } catch (_: Exception) { path }
            val name = decoded.substringAfterLast('/').substringAfterLast(':')
            val cleanName = if (name.contains('.')) name.substringBeforeLast('.') else name
            return cleanName.ifBlank { "Custom Bell" }
        }
        return bellSound
    }

    fun getDisplayNameFromUri(context: android.content.Context, uri: android.net.Uri): String {
        if (uri.scheme == "content") {
            try {
                context.contentResolver.query(
                    uri,
                    arrayOf(android.provider.OpenableColumns.DISPLAY_NAME),
                    null,
                    null,
                    null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                        if (nameIndex != -1) {
                            val name = cursor.getString(nameIndex)
                            if (!name.isNullOrBlank()) {
                                return if (name.contains('.')) name.substringBeforeLast('.') else name
                            }
                        }
                    }
                }
            } catch (_: Exception) {}
        }
        val path = uri.path ?: uri.toString()
        val decoded = try { java.net.URLDecoder.decode(path, "UTF-8") } catch (_: Exception) { path }
        val name = decoded.substringAfterLast('/').substringAfterLast(':')
        val cleanName = if (name.contains('.')) name.substringBeforeLast('.') else name
        return cleanName.ifBlank { "Custom Sound" }
    }

    fun isScheduledToday(repeatDays: List<Int>): Boolean {
        if (repeatDays.isEmpty() || repeatDays.size == 7) return true
        val todayValue = LocalDate.now().dayOfWeek.value // 1=Mon .. 7=Sun
        return repeatDays.contains(todayValue)
    }

    fun toSpokenWords(hour: Int, minute: Int, customMessage: String? = null): String {
        val amPm = if (hour >= 12) "PM" else "AM"
        val displayHour = when (val h = hour % 12) {
            0 -> 12
            else -> h
        }

        val hourWords = arrayOf(
            "twelve", "one", "two", "three", "four", "five",
            "six", "seven", "eight", "nine", "ten", "eleven", "twelve"
        )[displayHour]

        val minuteWords = when (minute) {
            0 -> "o'clock"
            in 1..9 -> "o' $minute"
            10 -> "ten"
            11 -> "eleven"
            12 -> "twelve"
            13 -> "thirteen"
            14 -> "fourteen"
            15 -> "fifteen"
            16 -> "sixteen"
            17 -> "seventeen"
            18 -> "eighteen"
            19 -> "nineteen"
            20 -> "twenty"
            30 -> "thirty"
            40 -> "forty"
            50 -> "fifty"
            else -> {
                val tens = minute / 10
                val ones = minute % 10
                val tensWord = when (tens) {
                    2 -> "twenty"
                    3 -> "thirty"
                    4 -> "forty"
                    5 -> "fifty"
                    else -> ""
                }
                val onesWord = arrayOf("", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine")[ones]
                "$tensWord $onesWord"
            }
        }

        val baseSpeech = if (minute == 0) {
            "It is $hourWords $amPm."
        } else {
            "It is $hourWords $minuteWords $amPm."
        }

        return if (!customMessage.isNullOrBlank()) {
            "$baseSpeech $customMessage"
        } else {
            baseSpeech
        }
    }
}
