package com.example.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.util.Log
import java.util.Locale

class TimeSpeechManagerImpl(
    context: Context
) : TimeSpeechManager {

    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private var selectedVoiceId: String = "System Default"

    private data class PendingSpeakRequest(
        val text: String,
        val speed: Float,
        val pitch: Float,
        val voiceName: String?
    )

    private var pendingSpeakRequest: PendingSpeakRequest? = null

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.getDefault()
                isInitialized = true
                applySelectedVoice(selectedVoiceId)

                pendingSpeakRequest?.let { req ->
                    pendingSpeakRequest = null
                    speak(req.text, req.speed, req.pitch, req.voiceName)
                }
            } else {
                Log.e("TimeSpeechManager", "TTS initialization failed")
            }
        }
    }

    override fun setVoice(voiceId: String) {
        selectedVoiceId = voiceId
        if (isInitialized) {
            applySelectedVoice(voiceId)
        }
    }

    private fun applySelectedVoice(voiceId: String) {
        val ttsRef = tts ?: return
        try {
            if (voiceId == "System Default" || voiceId.isBlank()) {
                ttsRef.language = Locale.getDefault()
                return
            }

            val allVoices = ttsRef.voices
            if (!allVoices.isNullOrEmpty()) {
                val exactMatch = allVoices.firstOrNull { it.name == voiceId }
                if (exactMatch != null) {
                    ttsRef.voice = exactMatch
                    return
                }

                // Legacy fallbacks
                if (voiceId == "System Female") {
                    val femaleVoice = allVoices.firstOrNull {
                        it.name.contains("female", ignoreCase = true) || it.name.contains("-f-")
                    }
                    if (femaleVoice != null) {
                        ttsRef.voice = femaleVoice
                        return
                    }
                } else if (voiceId == "System Male" || voiceId == "Jarvis") {
                    val maleVoice = allVoices.firstOrNull {
                        it.name.contains("male", ignoreCase = true) || it.name.contains("-m-") || it.name.contains("m0")
                    }
                    if (maleVoice != null) {
                        ttsRef.voice = maleVoice
                        return
                    }
                }
            }
            ttsRef.language = Locale.getDefault()
        } catch (e: Exception) {
            Log.e("TimeSpeechManager", "Failed to apply voice $voiceId", e)
        }
    }

    override fun getAvailableVoices(): List<VoiceOption> {
        val list = mutableListOf<VoiceOption>()

        list.add(
            VoiceOption(
                id = "System Default",
                displayName = "System Default Voice",
                category = "System",
                isAvailable = true,
                isComingSoon = false,
                description = "Default speech engine voice from Android system settings",
                languageGroup = "System Default",
                gender = null,
                isNetworkRequired = false,
                qualityLabel = "System"
            )
        )

        val ttsRef = tts
        if (ttsRef != null) {
            val installedVoices = try {
                ttsRef.voices?.filter { voice ->
                    val notInstalled = voice.features?.contains(TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED) == true
                    !notInstalled
                }?.sortedWith(compareBy({ it.locale?.displayName ?: "" }, { it.name })) ?: emptyList()
            } catch (e: Exception) {
                Log.e("TimeSpeechManager", "Error querying installed voices", e)
                emptyList()
            }

            val localeCounts = mutableMapOf<String, Int>()

            installedVoices.forEach { voice ->
                val languageGroup = if (voice.locale != null && voice.locale.displayName.isNotBlank()) {
                    voice.locale.displayName
                } else {
                    voice.locale?.language ?: "System / Other"
                }

                val count = (localeCounts[languageGroup] ?: 0) + 1
                localeCounts[languageGroup] = count

                val isMale = voice.name.contains("male", ignoreCase = true) ||
                        voice.name.contains("-m-") ||
                        voice.name.contains("m0") ||
                        voice.features?.contains("male") == true

                val isFemale = voice.name.contains("female", ignoreCase = true) ||
                        voice.name.contains("-f-") ||
                        voice.name.contains("f0") ||
                        voice.features?.contains("female") == true

                val gender = when {
                    isMale -> "Male"
                    isFemale -> "Female"
                    else -> null
                }

                val friendlyDisplayName = buildString {
                    append("Voice $count")
                    if (gender != null) {
                        append(" ($gender)")
                    }
                }

                val qualityDesc = when (voice.quality) {
                    Voice.QUALITY_VERY_HIGH -> "Very High Quality"
                    Voice.QUALITY_HIGH -> "High Quality"
                    Voice.QUALITY_NORMAL -> "Standard Quality"
                    else -> "Installed Voice"
                }

                val networkDesc = if (voice.isNetworkConnectionRequired) "Network Required" else "Offline"

                list.add(
                    VoiceOption(
                        id = voice.name,
                        displayName = friendlyDisplayName,
                        category = "Installed Voice",
                        isAvailable = true,
                        isComingSoon = false,
                        description = "$qualityDesc • $networkDesc",
                        languageGroup = languageGroup,
                        gender = gender,
                        isNetworkRequired = voice.isNetworkConnectionRequired,
                        qualityLabel = qualityDesc
                    )
                )
            }
        }

        return list
    }

    override fun speak(text: String, speed: Float, pitch: Float, voiceName: String?) {
        if (!isInitialized || tts == null) {
            pendingSpeakRequest = PendingSpeakRequest(text, speed, pitch, voiceName)
            return
        }
        try {
            val activeVoice = voiceName ?: selectedVoiceId
            applySelectedVoice(activeVoice)

            tts?.setSpeechRate(speed.coerceIn(0.5f, 2.0f))
            tts?.setPitch(pitch.coerceIn(0.5f, 2.0f))
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "TimeChimeUtterance_${System.currentTimeMillis()}")
        } catch (e: Exception) {
            Log.e("TimeSpeechManager", "Error speaking text", e)
        }
    }

    override fun stop() {
        try {
            pendingSpeakRequest = null
            tts?.stop()
        } catch (e: Exception) {
            Log.e("TimeSpeechManager", "Error stopping TTS", e)
        }
    }

    override fun shutdown() {
        try {
            pendingSpeakRequest = null
            tts?.stop()
            tts?.shutdown()
            tts = null
            isInitialized = false
        } catch (e: Exception) {
            Log.e("TimeSpeechManager", "Error shutting down TTS", e)
        }
    }
}


