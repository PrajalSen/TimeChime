package com.example.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.alarm.receiver.TimeChimeAlarmReceiver
import com.example.domain.model.Announcement
import java.util.Calendar

class TimeChimeAlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    fun schedule(announcement: Announcement) {
        if (!announcement.enabled || announcement.repeatDays.isEmpty()) {
            cancel(announcement)
            return
        }

        val triggerAtMillis = calculateNextTriggerMillis(announcement)
        if (triggerAtMillis <= 0) return

        val intent = Intent(context, TimeChimeAlarmReceiver::class.java).apply {
            action = ACTION_ANNOUNCE_TIME
            putExtra(EXTRA_ANNOUNCEMENT_ID, announcement.id)
            putExtra(EXTRA_HOUR, announcement.hour)
            putExtra(EXTRA_MINUTE, announcement.minute)
            putExtra(EXTRA_BELL_SOUND, announcement.bellSound)
            putExtra(EXTRA_VOLUME, announcement.volume)
            putExtra(EXTRA_VOICE_ENABLED, announcement.voiceEnabled)
            putExtra(EXTRA_CUSTOM_MESSAGE, announcement.customMessage)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            announcement.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (canScheduleExactAlarms()) {
                val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerAtMillis, pendingIntent)
                alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
            Log.d("AlarmScheduler", "Scheduled exact alarm clock for id ${announcement.id} at $triggerAtMillis")
        } catch (e: Exception) {
            try {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } catch (e2: Exception) {
                Log.e("AlarmScheduler", "Failed to schedule alarm for announcement ${announcement.id}", e2)
            }
        }
    }

    fun cancel(announcement: Announcement) {
        val intent = Intent(context, TimeChimeAlarmReceiver::class.java).apply {
            action = ACTION_ANNOUNCE_TIME
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            announcement.id.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            Log.d("AlarmScheduler", "Cancelled alarm for id ${announcement.id}")
        }
    }

    fun scheduleAll(announcements: List<Announcement>) {
        announcements.forEach { schedule(it) }
        scheduleHourlyChime()
    }

    fun scheduleHourlyChime() {
        val now = Calendar.getInstance()
        val nextHourCal = Calendar.getInstance().apply {
            add(Calendar.HOUR_OF_DAY, 1)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val triggerAtMillis = nextHourCal.timeInMillis

        val intent = Intent(context, TimeChimeAlarmReceiver::class.java).apply {
            action = ACTION_HOURLY_CHIME
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            HOURLY_CHIME_REQ_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (canScheduleExactAlarms()) {
                val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerAtMillis, pendingIntent)
                alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
            Log.d("AlarmScheduler", "Scheduled hourly chime check for $triggerAtMillis")
        } catch (e: Exception) {
            try {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } catch (e2: Exception) {
                Log.e("AlarmScheduler", "Failed to schedule hourly chime", e2)
            }
        }
    }

    private fun calculateNextTriggerMillis(announcement: Announcement): Long {

        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, announcement.hour)
            set(Calendar.MINUTE, announcement.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // Repeat days map: 1=Mon .. 7=Sun -> Calendar: Sun=1, Mon=2 ... Sat=7
        fun calendarDayFromIso(isoDay: Int): Int {
            return when (isoDay) {
                7 -> Calendar.SUNDAY
                else -> isoDay + 1
            }
        }

        val allowedCalendarDays = announcement.repeatDays.map { calendarDayFromIso(it) }.toSet()

        for (dayOffset in 0..7) {
            val candidate = target.clone() as Calendar
            candidate.add(Calendar.DAY_OF_YEAR, dayOffset)

            val dayOfWeek = candidate.get(Calendar.DAY_OF_WEEK)
            if (allowedCalendarDays.contains(dayOfWeek)) {
                if (candidate.timeInMillis > now.timeInMillis) {
                    return candidate.timeInMillis
                }
            }
        }

        // Default fallback: 24h from now
        target.add(Calendar.DAY_OF_YEAR, 1)
        return target.timeInMillis
    }

    companion object {
        const val ACTION_ANNOUNCE_TIME = "com.example.timechime.ANNOUNCE_TIME"
        const val ACTION_HOURLY_CHIME = "com.example.timechime.HOURLY_CHIME"
        const val HOURLY_CHIME_REQ_CODE = 999999
        const val EXTRA_ANNOUNCEMENT_ID = "EXTRA_ANNOUNCEMENT_ID"
        const val EXTRA_HOUR = "EXTRA_HOUR"
        const val EXTRA_MINUTE = "EXTRA_MINUTE"
        const val EXTRA_BELL_SOUND = "EXTRA_BELL_SOUND"
        const val EXTRA_VOLUME = "EXTRA_VOLUME"
        const val EXTRA_VOICE_ENABLED = "EXTRA_VOICE_ENABLED"
        const val EXTRA_CUSTOM_MESSAGE = "EXTRA_CUSTOM_MESSAGE"
    }

}
