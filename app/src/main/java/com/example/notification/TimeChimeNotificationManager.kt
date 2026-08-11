package com.example.notification

interface TimeChimeNotificationManager {
    fun showAnnouncementNotification(title: String, message: String)
    fun createNotificationChannels()
}
