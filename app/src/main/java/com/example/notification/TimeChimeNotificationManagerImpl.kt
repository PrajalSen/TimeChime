package com.example.notification

import android.content.Context

class TimeChimeNotificationManagerImpl(
    private val context: Context
) : TimeChimeNotificationManager {

    override fun createNotificationChannels() {
        // Foundation channel setup
    }

    override fun showAnnouncementNotification(title: String, message: String) {
        // Foundation notification display
    }
}
