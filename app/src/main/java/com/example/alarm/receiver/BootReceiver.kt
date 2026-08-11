package com.example.alarm.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.TimeChimeApplication
import com.example.alarm.TimeChimeAlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED && context != null) {
            Log.d("BootReceiver", "Boot completed, rescheduling active time chimes...")
            val app = context.applicationContext as? TimeChimeApplication ?: return
            val container = app.container
            val scheduler = TimeChimeAlarmScheduler(context)

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val announcements = container.announcementRepository.getAllAnnouncements().first()
                    announcements.filter { it.enabled }.forEach {
                        scheduler.schedule(it)
                    }
                    Log.d("BootReceiver", "Successfully rescheduled ${announcements.size} chimes on boot.")
                } catch (e: Exception) {
                    Log.e("BootReceiver", "Failed to reschedule chimes on boot", e)
                }
            }
        }
    }
}
