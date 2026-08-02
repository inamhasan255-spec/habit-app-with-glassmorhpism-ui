package com.example.data

import android.content.Context
import android.media.AudioFocusRequest
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Build
import kotlin.concurrent.thread
import kotlin.math.exp
import kotlin.math.sin
import kotlin.math.PI

/**
 * A sleek, high-quality audio tone synthesizer for timer alerts.
 * Generates pure mathematical sine waves with custom exponential decay envelopes
 * to avoid hardcoded static asset files or low-quality beeps.
 */
object FocusSoundPlayer {

    private data class Note(
        val frequency: Float,
        val durationSec: Float,
        val startDelaySec: Float,
        val volume: Float = 0.25f,
        val decayFactor: Float = 5.0f
    )

    fun playStartSound(soundPack: String = "Minimalist", context: Context? = null) {
        val notes = when (soundPack) {
            "Nature" -> listOf(
                // Hollow organic wood block double tap
                Note(600.00f, 0.08f, 0.00f, 0.25f, 12.0f),
                Note(750.00f, 0.10f, 0.08f, 0.22f, 12.0f)
            )
            "Mechanical" -> listOf(
                // Double crisp typewriter lever clicks
                Note(2200.00f, 0.03f, 0.00f, 0.30f, 30.0f),
                Note(1900.00f, 0.03f, 0.04f, 0.30f, 30.0f)
            )
            else -> listOf(
                // Minimalist pure chime C5 -> E5
                Note(523.25f, 0.25f, 0.0f, 0.20f, 5.0f),
                Note(659.25f, 0.35f, 0.12f, 0.20f, 5.0f)
            )
        }
        playNotes(notes, context)
    }

    fun playPauseSound(soundPack: String = "Minimalist", context: Context? = null) {
        val notes = when (soundPack) {
            "Nature" -> listOf(
                // Gentle, soft water drops/woodblocks falling in pitch
                Note(550.00f, 0.15f, 0.00f, 0.20f, 8.0f),
                Note(400.00f, 0.20f, 0.12f, 0.18f, 8.0f)
            )
            "Mechanical" -> listOf(
                // Mechanical heavy gear clutch release
                Note(180.00f, 0.08f, 0.00f, 0.25f, 15.0f), // Low mechanical clunk
                Note(2800.00f, 0.02f, 0.01f, 0.30f, 35.0f) // Sharp snap
            )
            else -> listOf(
                // Minimalist soft descending chime D5 -> A4
                Note(587.33f, 0.25f, 0.0f, 0.20f, 5.0f),
                Note(440.00f, 0.40f, 0.12f, 0.20f, 5.0f)
            )
        }
        playNotes(notes, context)
    }

    fun playCompleteSound(soundPack: String = "Minimalist", context: Context? = null) {
        val notes = when (soundPack) {
            "Nature" -> listOf(
                // Multi-toned peaceful forest pentatonic chime: E5 -> G5 -> A5 -> C6
                Note(659.25f, 0.45f, 0.00f, 0.15f, 4.0f),
                Note(783.99f, 0.45f, 0.12f, 0.15f, 4.0f),
                Note(880.00f, 0.45f, 0.24f, 0.15f, 4.0f),
                Note(1046.50f, 0.60f, 0.36f, 0.20f, 4.0f)
            )
            "Mechanical" -> listOf(
                // High mechanical desk bell ring ("Ding!")
                Note(2093.00f, 0.90f, 0.00f, 0.15f, 3.0f), // High C7 ring-out
                Note(2500.00f, 0.70f, 0.00f, 0.10f, 4.0f), // Resonance overtone
                Note(3200.00f, 0.50f, 0.00f, 0.05f, 5.0f)  // High shimmer
            )
            else -> listOf(
                // Minimalist ascending majestic chime: C5 -> E5 -> G5 -> C6
                Note(523.25f, 0.35f, 0.0f, 0.15f, 5.0f),
                Note(659.25f, 0.35f, 0.12f, 0.15f, 5.0f),
                Note(783.99f, 0.35f, 0.24f, 0.15f, 5.0f),
                Note(1046.50f, 0.60f, 0.36f, 0.20f, 5.0f)
            )
        }
        playNotes(notes, context)
    }

    private fun playNotes(notes: List<Note>, context: Context?) {
        thread(start = true, name = "FocusSoundPlayerThread") {
            var audioManager: AudioManager? = null
            var focusRequest: AudioFocusRequest? = null
            val focusListener = AudioManager.OnAudioFocusChangeListener { }

            context?.let { ctx ->
                try {
                    audioManager = ctx.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
                    audioManager?.let { am ->
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            val playbackAttributes = AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .build()
                            val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                                .setAudioAttributes(playbackAttributes)
                                .setOnAudioFocusChangeListener(focusListener)
                                .build()
                            focusRequest = request
                            am.requestAudioFocus(request)
                        } else {
                            @Suppress("DEPRECATION")
                            am.requestAudioFocus(
                                focusListener,
                                AudioManager.STREAM_MUSIC,
                                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
                            )
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            try {
                val sampleRate = 44100
                // Calculate total duration in seconds
                var maxDuration = 0f
                for (note in notes) {
                    val end = note.startDelaySec + note.durationSec
                    if (end > maxDuration) {
                        maxDuration = end
                    }
                }
                
                // Pad slightly at the end for smooth ring-out decay
                val totalDurationSec = maxDuration + 0.1f
                val totalSamples = (sampleRate * totalDurationSec).toInt()
                val shortBuffer = ShortArray(totalSamples)

                for (s in 0 until totalSamples) {
                    val t = s.toFloat() / sampleRate.toFloat()
                    var mixedSample = 0.0f

                    for (note in notes) {
                        if (t >= note.startDelaySec) {
                            val dt = t - note.startDelaySec
                            if (dt <= note.durationSec) {
                                // Exponential decay: start strong, fade smoothly
                                val envelope = exp(-note.decayFactor * dt / note.durationSec)
                                val sine = sin(2.0f * PI.toFloat() * note.frequency * dt)
                                mixedSample += sine * envelope * note.volume
                            }
                        }
                    }

                    // Convert to float sample and clamp to short boundaries
                    shortBuffer[s] = (mixedSample * 32767.0f).toInt().coerceIn(-32768, 32767).toShort()
                }

                // Play the synthesized PCM array
                val minBufferSize = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )
                
                // We want the buffer size to fit our complete tone
                val bufferSize = shortBuffer.size * 2 // in bytes
                val actualBufferSize = bufferSize.coerceAtLeast(minBufferSize)

                 val track = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    AudioTrack.Builder()
                        .setAudioAttributes(
                            AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .build()
                        )
                        .setAudioFormat(
                            AudioFormat.Builder()
                                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                                .setSampleRate(sampleRate)
                                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                                .build()
                        )
                        .setBufferSizeInBytes(actualBufferSize)
                        .setTransferMode(AudioTrack.MODE_STREAM)
                        .build()
                } else {
                    @Suppress("DEPRECATION")
                    AudioTrack(
                        AudioManager.STREAM_MUSIC,
                        sampleRate,
                        AudioFormat.CHANNEL_OUT_MONO,
                        AudioFormat.ENCODING_PCM_16BIT,
                        actualBufferSize,
                        AudioTrack.MODE_STREAM
                    )
                }

                track.play()
                try {
                    track.write(shortBuffer, 0, shortBuffer.size)
                    
                    // Give it a little time to finish playing before stopping/releasing
                    val sleepTime = (totalDurationSec * 1000).toLong()
                    Thread.sleep(sleepTime)
                } finally {
                    try {
                        track.stop()
                    } catch (e: Exception) {}
                    try {
                        track.release()
                    } catch (e: Exception) {}
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                try {
                    audioManager?.let { am ->
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            focusRequest?.let { am.abandonAudioFocusRequest(it) }
                        } else {
                            @Suppress("DEPRECATION")
                            am.abandonAudioFocus(focusListener)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}
