package com.focuscube.audio

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Build
import android.util.Log
import com.example.data.AudioPlaybackService
import com.example.data.WhiteNoisePlayer
import kotlin.concurrent.thread

object StereoBinauralEngine {

    private var playbackThread: Thread? = null
    private val sampleRate = 44100

    @Volatile
    private var isPlaying = false

    private var appContextRef: java.lang.ref.WeakReference<Context>? = null

    private var audioFocusRequest: android.media.AudioFocusRequest? = null
    private val audioFocusListener = AudioManager.OnAudioFocusChangeListener { }

    private fun requestFocus(context: Context): Boolean {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return false
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val playbackAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
            val request = android.media.AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(playbackAttributes)
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener(audioFocusListener)
                .build()
            audioFocusRequest = request
            audioManager.requestAudioFocus(request) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(
                audioFocusListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            ) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        }
    }

    private fun abandonFocus(context: Context) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let {
                audioManager.abandonAudioFocusRequest(it)
                audioFocusRequest = null
            }
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(audioFocusListener)
        }
    }

    fun isCurrentlyPlaying(): Boolean = isPlaying

    fun start(context: Context? = null, leftFreq: Double = 200.0, rightFreq: Double = 215.0) {
        context?.let {
            appContextRef = java.lang.ref.WeakReference(it.applicationContext)
        }
        if (isPlaying) return
        
        isPlaying = true

        // Start background playback foreground service and request Audio Focus
        context?.let { ctx ->
            try {
                requestFocus(ctx)
                val serviceIntent = Intent(ctx, AudioPlaybackService::class.java).apply {
                    action = AudioPlaybackService.ACTION_START
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    ctx.startForegroundService(serviceIntent)
                } else {
                    ctx.startService(serviceIntent)
                }
            } catch (e: Exception) {
                Log.e("StereoBinauralEngine", "Failed to start AudioPlaybackService: ${e.localizedMessage}")
            }
        }

        playbackThread = thread(start = true, name = "BinauralAudioThread") {
            var audioTrack: AudioTrack? = null
            try {
                val bufferSize = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_STEREO,
                    AudioFormat.ENCODING_PCM_16BIT
                ).coerceAtLeast(4096)

                audioTrack = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
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
                                .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                                .build()
                        )
                        .setBufferSizeInBytes(bufferSize)
                        .setTransferMode(AudioTrack.MODE_STREAM)
                        .build()
                } else {
                    @Suppress("DEPRECATION")
                    AudioTrack(
                        AudioManager.STREAM_MUSIC,
                        sampleRate,
                        AudioFormat.CHANNEL_OUT_STEREO,
                        AudioFormat.ENCODING_PCM_16BIT,
                        bufferSize,
                        AudioTrack.MODE_STREAM
                    )
                }

                val writeSize = bufferSize / 2 // in Shorts
                val tempBuffer = ShortArray(writeSize)
                var sampleIndex = 0L

                audioTrack.play()

                while (isPlaying) {
                    for (i in 0 until writeSize step 2) {
                        val leftAngle = 2.0 * Math.PI * leftFreq * (sampleIndex.toDouble() / sampleRate)
                        val rightAngle = 2.0 * Math.PI * rightFreq * (sampleIndex.toDouble() / sampleRate)

                        // 16-bit PCM short synthesis with 0.4 volume scale
                        val leftVal = (Math.sin(leftAngle) * Short.MAX_VALUE * 0.4).toInt().toShort()
                        val rightVal = (Math.sin(rightAngle) * Short.MAX_VALUE * 0.4).toInt().toShort()

                        tempBuffer[i] = leftVal
                        if (i + 1 < writeSize) {
                            tempBuffer[i + 1] = rightVal
                        }
                        sampleIndex++
                    }
                    audioTrack.write(tempBuffer, 0, writeSize)
                }
            } catch (e: Exception) {
                Log.e("StereoBinauralEngine", "Binaural audio thread execution failure: ${e.localizedMessage}")
            } finally {
                isPlaying = false
                try {
                    audioTrack?.stop()
                } catch (e: Exception) {
                    Log.w("StereoBinauralEngine", "Failed to stop AudioTrack safely: ${e.localizedMessage}")
                }
                try {
                    audioTrack?.release()
                } catch (e: Exception) {
                    Log.w("StereoBinauralEngine", "Failed to release AudioTrack safely: ${e.localizedMessage}")
                }

                val ctx = context ?: appContextRef?.get()
                ctx?.let {
                    abandonFocus(it)
                    checkAndStopService(it)
                }
            }
        }
    }

    fun stop(context: Context? = null) {
        isPlaying = false
        playbackThread = null
        val ctx = context ?: appContextRef?.get()
        ctx?.let {
            abandonFocus(it)
            checkAndStopService(it)
        }
    }

    private fun checkAndStopService(context: Context) {
        val isWhiteNoisePlaying = try {
            WhiteNoisePlayer.isCurrentlyPlaying()
        } catch (e: Exception) {
            false
        }

        if (!isPlaying && !isWhiteNoisePlaying) {
            try {
                val serviceIntent = Intent(context, AudioPlaybackService::class.java).apply {
                    action = AudioPlaybackService.ACTION_STOP
                }
                context.stopService(serviceIntent)
            } catch (e: Exception) {
                Log.e("StereoBinauralEngine", "Failed to stop AudioPlaybackService: ${e.localizedMessage}")
            }
        }
    }
}
