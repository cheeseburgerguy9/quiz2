package com.example.util

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.net.Uri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin
import kotlin.random.Random

object SoundEffectHelper {

    /**
     * Synthesizes and plays an authentic whiskey bottle opening (crisp pop/uncork friction)
     * followed by liquid pouring glugs into a glass using PCM AudioTrack audio synthesis.
     */
    fun playWhiskeyPourSound(scope: CoroutineScope = CoroutineScope(Dispatchers.Default)) {
        scope.launch {
            try {
                val sampleRate = 44100
                // Duration: ~2.4 seconds
                val totalDurationSec = 2.4
                val totalSamples = (sampleRate * totalDurationSec).toInt()
                val audioData = ShortArray(totalSamples)

                // Part 1: Cork pop / bottle opening (0.0s - 0.25s)
                val popDurationSec = 0.25
                val popSamples = (sampleRate * popDurationSec).toInt()
                for (i in 0 until popSamples) {
                    val t = i.toDouble() / sampleRate
                    // Pitch drops quickly like a cork unplugging from glass neck (580Hz down to 140Hz)
                    val freq = 580.0 * (1.0 - t / popDurationSec * 0.75)
                    val envelope = (1.0 - t / popDurationSec) * (1.0 - t / popDurationSec)
                    val popTone = sin(2.0 * Math.PI * freq * t)
                    // Add slight friction whoosh/hiss
                    val hiss = (Random.nextDouble() * 2.0 - 1.0) * 0.25 * envelope
                    val sampleVal = ((popTone * 0.8 + hiss) * envelope * Short.MAX_VALUE * 0.9).toInt()
                    audioData[i] = sampleVal.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }

                // Short silence (0.25s - 0.35s)
                val pourStartSample = (sampleRate * 0.35).toInt()

                // Part 2: Whiskey liquid pouring & bubbling glugs (0.35s - 2.4s)
                // Glug intervals: periodic resonant bubbles that pitch upward slightly as glass fills
                val glugInterval = (sampleRate * 0.18).toInt()
                var currentGlugStart = pourStartSample
                var glugIndex = 0

                for (i in pourStartSample until totalSamples) {
                    val t = (i - pourStartSample).toDouble() / sampleRate
                    val relativePourProgress = t / (totalDurationSec - 0.35)

                    // Continuous liquid flow stream hiss filtered around liquid frequencies
                    val streamNoise = (Random.nextDouble() * 2.0 - 1.0) * 0.22

                    // Bubble / glug resonance
                    val glugT = (i - currentGlugStart).toDouble() / sampleRate
                    val baseGlugFreq = 260.0 + (glugIndex * 14.0) // slightly rises as liquid fills
                    // Glug envelope: fast attack, ringing decay
                    val glugEnv = if (glugT in 0.0..0.12) {
                        sin(Math.PI * (glugT / 0.12)) * (1.0 - glugT / 0.12)
                    } else 0.0

                    val glugTone = sin(2.0 * Math.PI * baseGlugFreq * glugT) * glugEnv * 0.85
                    val harmonic = sin(2.0 * Math.PI * (baseGlugFreq * 1.8) * glugT) * glugEnv * 0.3

                    // Overall fade out towards the end
                    val overallEnvelope = (1.0 - relativePourProgress).coerceIn(0.0, 1.0)

                    val combined = (glugTone + harmonic + streamNoise * 0.4) * overallEnvelope
                    val sampleVal = (combined * Short.MAX_VALUE * 0.8).toInt()
                    audioData[i] = sampleVal.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()

                    if (i - currentGlugStart >= glugInterval) {
                        currentGlugStart = i
                        glugIndex++
                    }
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
                    .setBufferSizeInBytes(audioData.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                audioTrack.write(audioData, 0, audioData.size)
                audioTrack.play()
            } catch (e: Exception) {
                // Ignore audio play errors on background synthesis
            }
        }
    }

    fun openFowlersSite(context: Context) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://fowlers.site")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // In case no browser is installed or handling fails
        }
    }
}
