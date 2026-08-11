package com.example.tts

data class VoiceOption(
    val id: String,
    val displayName: String,
    val category: String, // "System", "Installed Voice"
    val isAvailable: Boolean = true,
    val isComingSoon: Boolean = false,
    val description: String = "",
    val languageGroup: String = "System Default",
    val gender: String? = null, // "Male", "Female", or null
    val isNetworkRequired: Boolean = false,
    val qualityLabel: String = ""
)

interface TimeSpeechManager {
    fun speak(text: String, speed: Float = 1.0f, pitch: Float = 1.0f, voiceName: String? = null)
    fun stop()
    fun shutdown()
    fun getAvailableVoices(): List<VoiceOption>
    fun setVoice(voiceId: String)
}

