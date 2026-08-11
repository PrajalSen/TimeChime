package com.example.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

class AudioBellPlayer {

    private var currentAudioTrack: AudioTrack? = null
    private var currentMediaPlayer: MediaPlayer? = null
    private var activeJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)
    private val lock = Any()

    fun setVolume(volumePercent: Int) {
        synchronized(lock) {
            val vol = (volumePercent / 100f).coerceIn(0f, 1f)
            try {
                if (currentAudioTrack?.state == AudioTrack.STATE_INITIALIZED) {
                    currentAudioTrack?.setVolume(vol)
                }
                currentMediaPlayer?.setVolume(vol, vol)
            } catch (e: Exception) {
                Log.e("AudioBellPlayer", "Error updating volume on active track/media player", e)
            }
        }
    }

    fun playBell(bellName: String, volumePercent: Int = 80, context: Context? = null) {
        stopCleanly()

        activeJob = scope.launch {
            val isCustomUri = bellName.startsWith("content://") || bellName.startsWith("file://") || bellName.contains("://")
            
            if (isCustomUri && context != null) {
                var isReadable = false
                try {
                    val uri = Uri.parse(bellName)
                    context.contentResolver.openInputStream(uri)?.use {
                        isReadable = true
                    }
                } catch (e: Exception) {
                    isReadable = false
                }

                if (isReadable) {
                    try {
                        val uri = Uri.parse(bellName)
                        val mediaPlayer = MediaPlayer().apply {
                            setAudioAttributes(
                                AudioAttributes.Builder()
                                    .setUsage(AudioAttributes.USAGE_MEDIA)
                                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                    .build()
                            )
                            setDataSource(context, uri)
                            val vol = (volumePercent / 100f).coerceIn(0f, 1f)
                            setVolume(vol, vol)
                            prepare()
                            start()
                        }
                        synchronized(lock) {
                            currentMediaPlayer = mediaPlayer
                        }
                        return@launch
                    } catch (e: Exception) {
                        Log.e("AudioBellPlayer", "Error playing custom bell URI", e)
                    }
                }

                // Fallback for custom sound missing / unreadable
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(context, "Custom sound file missing, using default bell.", Toast.LENGTH_SHORT).show()
                }
            }

            // Built-in synthesized bell playback (or fallback for unreadable custom bell)
            try {
                val effectiveBellName = if (isCustomUri) "Classic Chime" else bellName
                val sampleRate = 44100
                val durationMs = when (effectiveBellName) {
                    "Tower Bell" -> 2000
                    "Zen Bowl" -> 2400
                    "Digital Chime" -> 800
                    "Grandfather Clock" -> 1600
                    "Soft Bell" -> 1500
                    else -> 1200 // Classic Chime
                }
                val numSamples = sampleRate * durationMs / 1000
                val buffer = ShortArray(numSamples)

                val vol = (volumePercent / 100f).coerceIn(0f, 1f)

                val attackSamples = (sampleRate * 0.05).toInt()
                val releaseSamples = (sampleRate * 0.10).toInt()

                for (i in 0 until numSamples) {
                    val t = i.toDouble() / sampleRate
                    var envelope = exp(-3.2 * t / (durationMs / 1000.0))

                    // Anti-click attack and release smooth fade
                    if (i < attackSamples) {
                        envelope *= (i.toDouble() / attackSamples)
                    }
                    if (i > numSamples - releaseSamples) {
                        envelope *= ((numSamples - i).toDouble() / releaseSamples)
                    }

                    val sampleValue = when (effectiveBellName) {
                        "Soft Bell" -> {
                            val baseFreq = 349.23 // F4
                            0.85 * sin(2.0 * PI * baseFreq * t) +
                            0.15 * sin(2.0 * PI * (baseFreq * 2.0) * t)
                        }
                        "Digital Chime" -> {
                            val freq = if (t < 0.12) 880.0 else 1108.73 // A5 -> C#6
                            sin(2.0 * PI * freq * t)
                        }
                        "Tower Bell" -> {
                            val baseFreq = 174.61 // Deep F3
                            0.5 * sin(2.0 * PI * baseFreq * t) +
                            0.3 * sin(2.0 * PI * (baseFreq * 2.0) * t) +
                            0.2 * sin(2.0 * PI * (baseFreq * 3.0) * t)
                        }
                        "Zen Bowl" -> {
                            val baseFreq = 216.0
                            0.7 * sin(2.0 * PI * baseFreq * t + 0.15 * sin(2.0 * PI * 2.0 * t)) +
                            0.3 * sin(2.0 * PI * (baseFreq * 1.5) * t)
                        }
                        "Grandfather Clock" -> {
                            val freq = if (t < 0.35) 329.63 else 440.0 // E4 -> A4
                            0.7 * sin(2.0 * PI * freq * t) +
                            0.3 * sin(2.0 * PI * (freq * 2.0) * t)
                        }
                        else -> { // Classic Chime
                            val baseFreq = 523.25 // C5
                            0.5 * sin(2.0 * PI * baseFreq * t) +
                            0.35 * sin(2.0 * PI * (baseFreq * 2.0) * t) +
                            0.15 * sin(2.0 * PI * (baseFreq * 3.0) * t)
                        }
                    }

                    val pcmValue = (sampleValue * envelope * vol * Short.MAX_VALUE).toInt()
                    buffer[i] = pcmValue.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }

                val minBufferSize = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )

                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(maxOf(minBufferSize, buffer.size * 2))
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                audioTrack.write(buffer, 0, buffer.size)

                synchronized(lock) {
                    currentAudioTrack = audioTrack
                    audioTrack.setVolume(vol)
                    audioTrack.play()
                }
            } catch (e: Exception) {
                Log.e("AudioBellPlayer", "Error playing bell sound", e)
            }
        }
    }

    fun stop() {
        stopCleanly()
    }

    private fun stopCleanly() {
        synchronized(lock) {
            activeJob?.cancel()
            activeJob = null
            try {
                val player = currentMediaPlayer
                if (player != null) {
                    try {
                        if (player.isPlaying) {
                            player.stop()
                        }
                    } catch (_: Exception) {}
                    player.release()
                }
            } catch (e: Exception) {
                Log.e("AudioBellPlayer", "Error stopping media player", e)
            } finally {
                currentMediaPlayer = null
            }

            try {
                val track = currentAudioTrack
                if (track != null) {
                    if (track.playState == AudioTrack.PLAYSTATE_PLAYING) {
                        try {
                            // 150ms smooth fade-out step to prevent pop/click
                            for (step in 10 downTo 0) {
                                track.setVolume(step / 10f)
                                Thread.sleep(12)
                            }
                            track.stop()
                        } catch (_: Exception) {}
                    }
                    track.release()
                }
            } catch (e: Exception) {
                Log.e("AudioBellPlayer", "Error stopping audio track", e)
            } finally {
                currentAudioTrack = null
            }
        }
    }
}
