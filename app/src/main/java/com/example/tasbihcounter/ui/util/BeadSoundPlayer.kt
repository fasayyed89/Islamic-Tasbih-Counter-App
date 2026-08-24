package com.example.tasbihcounter.ui.util

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * Generates and plays a calm, organic wooden prayer-bead click sound directly via PCM synthesis.
 * Uses a single cached static AudioTrack to prevent audio resource leakage and crashes.
 * Requires 0 KB audio files in the APK assets, maintaining ultra-compact APK size.
 */
object BeadSoundPlayer {

    private const val SAMPLE_RATE = 44100
    private const val DURATION_MS = 24
    private val numSamples = (SAMPLE_RATE * DURATION_MS) / 1000

    private val pcmData: ShortArray by lazy {
        val samples = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            // Fundamental wooden click freq ~800Hz with harmonic ~1600Hz and sharp exponential decay
            val envelope = exp(-t / 0.004)
            val wave = 0.7 * sin(2.0 * PI * 800.0 * t) + 0.3 * sin(2.0 * PI * 1600.0 * t)
            val sampleVal = (wave * envelope * 0.4 * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
            samples[i] = sampleVal.toShort()
        }
        samples
    }

    private var cachedTrack: AudioTrack? = null

    @Synchronized
    fun playClick() {
        try {
            var track = cachedTrack
            if (track == null || track.state != AudioTrack.STATE_INITIALIZED) {
                track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(SAMPLE_RATE)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(pcmData.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                track.write(pcmData, 0, pcmData.size)
                cachedTrack = track
            }

            if (track.state == AudioTrack.STATE_INITIALIZED) {
                track.stop()
                track.reloadStaticData()
                track.play()
            }
        } catch (_: Throwable) {
            // Silently fall back if audio hardware is temporarily unavailable
        }
    }
}
