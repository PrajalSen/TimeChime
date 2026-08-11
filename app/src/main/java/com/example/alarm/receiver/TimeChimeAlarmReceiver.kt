package com.example.alarm.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.TimeChimeApplication
import com.example.alarm.TimeChimeAlarmScheduler
import com.example.utils.TimeUtils
import kotlinx.coroutines.launch

import com.example.domain.model.HourlyChimeMode
import com.example.domain.model.UserPreferences
import kotlinx.coroutines.flow.firstOrNull
import java.util.Calendar

class TimeChimeAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        val app = context.applicationContext as? TimeChimeApplication ?: return
        val container = app.container
        val scheduler = TimeChimeAlarmScheduler(context)

        val cal = Calendar.getInstance()
        val currentHour = cal.get(Calendar.HOUR_OF_DAY)
        val currentMinute = cal.get(Calendar.MINUTE)
        // Iso day: 1=Mon .. 7=Sun
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        val currentDayIso = if (dayOfWeek == Calendar.SUNDAY) 7 else dayOfWeek - 1

        kotlinx.coroutines.MainScope().launchSafely {
            val prefs = container.getUserPreferencesUseCase().firstOrNull() ?: UserPreferences()

            val isFocusActive = isFocusTimeActive(currentHour, currentMinute, currentDayIso, prefs)

            if (intent.action == TimeChimeAlarmScheduler.ACTION_HOURLY_CHIME) {
                // Reschedule next hourly chime
                scheduler.scheduleHourlyChime()

                val isHourlyActive = isHourlyChimeActiveForHour(currentHour, currentDayIso, prefs)
                if (isHourlyActive && !isFocusActive) {
                    val bellSound = prefs.defaultBell
                    val volume = prefs.defaultVolume
                    val spokenText = TimeUtils.toSpokenWords(currentHour, 0, null)

                    container.audioBellPlayer.playBell(bellSound, volume, context)
                    if (prefs.hourlyChimeVoiceEnabled) {
                        container.timeSpeechManager.speak(spokenText, prefs.speechSpeed, prefs.speechPitch, prefs.selectedVoiceName)
                    }
                    showNotification(context, bellSound, spokenText)
                } else {
                    Log.d("TimeChimeAlarmReceiver", "Hourly chime skipped (active=$isHourlyActive, focusActive=$isFocusActive)")
                }
                return@launchSafely
            }

            // Standard scheduled announcement
            val announcementId = intent.getLongExtra(TimeChimeAlarmScheduler.EXTRA_ANNOUNCEMENT_ID, -1L)
            val announcement = container.announcementRepository.getAnnouncementById(announcementId)

            if (isFocusActive) {
                val allowImportant = prefs.focusAllowImportantAlarms
                // Focus time active: check if allowed
                if (!allowImportant) {
                    Log.d("TimeChimeAlarmReceiver", "Announcement $announcementId skipped due to Focus Time")
                    if (announcement != null && announcement.enabled) {
                        scheduler.schedule(announcement)
                    }
                    return@launchSafely
                }
            }

            val hour = intent.getIntExtra(TimeChimeAlarmScheduler.EXTRA_HOUR, currentHour)
            val minute = intent.getIntExtra(TimeChimeAlarmScheduler.EXTRA_MINUTE, currentMinute)
            val bellSound = intent.getStringExtra(TimeChimeAlarmScheduler.EXTRA_BELL_SOUND) ?: prefs.defaultBell
            val volume = intent.getIntExtra(TimeChimeAlarmScheduler.EXTRA_VOLUME, prefs.defaultVolume)
            val voiceEnabled = intent.getBooleanExtra(TimeChimeAlarmScheduler.EXTRA_VOICE_ENABLED, prefs.defaultVoiceEnabled)
            val customMessage = intent.getStringExtra(TimeChimeAlarmScheduler.EXTRA_CUSTOM_MESSAGE)

            Log.d("TimeChimeAlarmReceiver", "Chime triggered for id $announcementId at $hour:$minute")

            // 1. Play sound
            container.audioBellPlayer.playBell(bellSound, volume, context)

            // 2. Play TTS speech
            val spokenText = TimeUtils.toSpokenWords(hour, minute, customMessage)
            if (voiceEnabled) {
                container.timeSpeechManager.speak(spokenText, prefs.speechSpeed, prefs.speechPitch, prefs.selectedVoiceName)
            }

            // 3. Post notification
            showNotification(context, bellSound, spokenText)

            // 4. Reschedule next alarm for repeating schedule
            if (announcement != null && announcement.enabled) {
                scheduler.schedule(announcement)
            }
        }
    }

    private fun isFocusTimeActive(
        nowHour: Int,
        nowMinute: Int,
        nowDayIso: Int,
        prefs: UserPreferences
    ): Boolean {
        if (!prefs.focusTimeEnabled) return false
        if (!prefs.focusDays.contains(nowDayIso)) return false

        val nowMins = nowHour * 60 + nowMinute
        val startMins = prefs.focusStartHour * 60 + prefs.focusStartMinute
        val endMins = prefs.focusEndHour * 60 + prefs.focusEndMinute

        return if (startMins <= endMins) {
            nowMins in startMins..endMins
        } else {
            // Range crosses midnight (e.g. 22:00 to 07:00)
            nowMins >= startMins || nowMins <= endMins
        }
    }

    private fun isHourlyChimeActiveForHour(
        hour: Int,
        dayIso: Int,
        prefs: UserPreferences
    ): Boolean {
        if (prefs.hourlyChimeMode == HourlyChimeMode.OFF) return false
        if (!prefs.hourlyChimeDays.contains(dayIso)) return false

        if (prefs.hourlyChimeMode == HourlyChimeMode.EVERY_HOUR) {
            return true
        }

        // CUSTOM_RANGE
        val startHour = prefs.hourlyChimeStartHour
        val endHour = prefs.hourlyChimeEndHour

        return if (startHour <= endHour) {
            hour in startHour..endHour
        } else {
            // Range crosses midnight (e.g. 22 to 6)
            hour >= startHour || hour <= endHour
        }
    }


    private fun showNotification(context: Context, bellSound: String, text: String) {
        val channelId = "timechime_alarm_channel"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "TimeChime Announcements",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifies when a scheduled time chime occurs"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("🔔 TimeChime ($bellSound)")
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun kotlinx.coroutines.CoroutineScope.launchSafely(block: suspend () -> Unit) {
        launch {
            try {
                block()
            } catch (e: Exception) {
                Log.e("TimeChimeAlarmReceiver", "Error in async receiver task", e)
            }
        }
    }
}
