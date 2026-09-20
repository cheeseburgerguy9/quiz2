package com.aistudio.caitlindaily.util

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.util.Calendar
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

object SoundEffectHelper {
    /**
     * Synthesizes and plays an authentic whiskey bottle opening ("pop" / cork pull)
     * followed immediately by the resonant liquid pouring and bubbling into a glass.
     * Uses pure PCM audio synthesis via AudioTrack so it runs offline with zero external assets.
     */
    suspend fun playWhiskeyPopAndPour() = withContext(Dispatchers.Default) {
        try {
            val sampleRate = 44100
            val totalDurationSeconds = 2.4
            val numSamples = (sampleRate * totalDurationSeconds).toInt()
            val buffer = ShortArray(numSamples)

            // 1. Pop Sound (0.0s to 0.25s) - Cork snap and resonant cavity release
            val popDuration = (sampleRate * 0.22).toInt()
            for (i in 0 until popDuration) {
                val t = i.toDouble() / sampleRate
                val freq = 340.0 * exp(-t * 35.0) + 130.0
                val decay = exp(-t * 22.0)
                val tone = sin(2.0 * PI * freq * t) * decay
                val noise = (Random.nextDouble() * 2.0 - 1.0) * exp(-t * 60.0) * 0.4
                val sample = (tone * 0.85 + noise) * 32000.0
                buffer[i] = sample.coerceIn(-32767.0, 32767.0).toInt().toShort()
            }

            // 2. Liquid Pouring & Glug Glug Glug (0.3s to 2.3s)
            val pourStart = (sampleRate * 0.30).toInt()
            val glugTimes = listOf(0.36, 0.68, 1.02, 1.38, 1.76) // Rhythmic glugs
            for (i in pourStart until numSamples) {
                val t = i.toDouble() / sampleRate
                val tRel = t - 0.30
                var sample = 0.0

                // Liquid stream and ambient trickle (filtered noise + dynamic tone)
                val trickleNoise = (Random.nextDouble() * 2.0 - 1.0)
                val trickleEnv = when {
                    tRel < 0.2 -> (tRel / 0.2)
                    t > 2.0 -> ((totalDurationSeconds - t) / 0.4).coerceAtLeast(0.0)
                    else -> 1.0
                }
                val trickleTone = sin(2.0 * PI * (920.0 + sin(t * 18.0) * 220.0) * t) * 0.15
                sample += (trickleNoise * 0.20 + trickleTone) * trickleEnv * 0.45

                // Resonant bubble "glugs" ascending pitch
                for (glugT in glugTimes) {
                    val dt = t - glugT
                    if (dt in 0.0..0.18) {
                        val glugFreq = 380.0 + (dt / 0.18) * 200.0
                        val glugEnv = sin(dt / 0.18 * PI) * exp(-dt * 8.0)
                        sample += sin(2.0 * PI * glugFreq * dt) * glugEnv * 0.75
                    }
                }

                buffer[i] = (sample * 28000.0).coerceIn(-32767.0, 32767.0).toInt().toShort()
            }

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
                .setBufferSizeInBytes(buffer.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(buffer, 0, buffer.size)
            audioTrack.play()
            delay((totalDurationSeconds * 1000).toLong() + 200)
            audioTrack.stop()
            audioTrack.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Calculates the hidden easter egg message for Friday:
     * - If Friday: "congrats today's the day , enjoy ."
     * - Else: "X days till the next friday"
     */
    fun getFridayEasterEggMessage(): String {
        val calendar = Calendar.getInstance()
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        // Calendar: SUNDAY=1, MONDAY=2, TUESDAY=3, WEDNESDAY=4, THURSDAY=5, FRIDAY=6, SATURDAY=7
        return if (dayOfWeek == Calendar.FRIDAY) {
            "congrats today's the day , enjoy ."
        } else {
            val daysUntilFriday = if (dayOfWeek < Calendar.FRIDAY) {
                Calendar.FRIDAY - dayOfWeek
            } else {
                // Saturday (7) -> 6 days until next Friday
                7 - dayOfWeek + Calendar.FRIDAY
            }
            if (daysUntilFriday == 1) {
                "1 day till the next friday"
            } else {
                "$daysUntilFriday days till the next friday"
            }
        }
    }
}
