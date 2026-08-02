package com.example.data

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Build
import java.util.Random
import kotlin.concurrent.thread
import kotlin.math.sin
import kotlin.math.cos
import kotlin.math.PI
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * A dynamic, offline-first audio generator that uses Android's AudioTrack
 * to synthesize soothing ambient soundscapes ('Brown Noise', 'Rain', 'Forest', 'Cafe') on-the-fly.
 * 
 * No massive asset downloads required! 100% synthesized locally with absolute purity.
 */
object WhiteNoisePlayer {
    private var audioTrack: AudioTrack? = null
    @Volatile
    private var isPlaying = false
    private var playbackThread: Thread? = null

    private val _visualizerFlow = MutableStateFlow(FloatArray(8) { 0f })
    val visualizerFlow: StateFlow<FloatArray> = _visualizerFlow

    @Volatile
    var currentSoundType = "Brown Noise"

    @Volatile
    var comfortChainVolume = 0.6f

    private var appContextRef: java.lang.ref.WeakReference<android.content.Context>? = null

    private var audioFocusRequest: android.media.AudioFocusRequest? = null
    private val audioFocusListener = AudioManager.OnAudioFocusChangeListener { }

    private fun requestFocus(context: android.content.Context): Boolean {
        val audioManager = context.getSystemService(android.content.Context.AUDIO_SERVICE) as? AudioManager ?: return false
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

    private fun abandonFocus(context: android.content.Context) {
        val audioManager = context.getSystemService(android.content.Context.AUDIO_SERVICE) as? AudioManager ?: return
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

    fun start(soundType: String = "Brown Noise", context: android.content.Context? = null) {
        currentSoundType = soundType
        context?.let {
            appContextRef = java.lang.ref.WeakReference(it.applicationContext)
        }
        if (isPlaying) return
        
        isPlaying = true

        // Start background playback foreground service and request Audio Focus
        context?.let { ctx ->
            try {
                requestFocus(ctx)
                val serviceIntent = android.content.Intent(ctx, AudioPlaybackService::class.java).apply {
                    action = AudioPlaybackService.ACTION_START
                }
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    ctx.startForegroundService(serviceIntent)
                } else {
                    ctx.startService(serviceIntent)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        playbackThread = thread(start = true, name = "WhiteNoisePlaybackThread") {
            val sampleRate = 44100
            val bufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )

            val track = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                try {
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
                        .setBufferSizeInBytes(bufferSize)
                        .setTransferMode(AudioTrack.MODE_STREAM)
                        .build()
                } catch (e: Exception) {
                    null
                }
            } else {
                @Suppress("DEPRECATION")
                try {
                    AudioTrack(
                        AudioManager.STREAM_MUSIC,
                        sampleRate,
                        AudioFormat.CHANNEL_OUT_MONO,
                        AudioFormat.ENCODING_PCM_16BIT,
                        bufferSize,
                        AudioTrack.MODE_STREAM
                    )
                } catch (e: Exception) {
                    null
                }
            }

            if (track == null) {
                isPlaying = false
                return@thread
            }

            audioTrack = track
            val buffer = ShortArray(bufferSize)
            val random = Random()

            try {
                try {
                    track.play()
                } catch (e: Exception) {
                    e.printStackTrace()
                    isPlaying = false
                    return@thread
                }

            // Shared synthesis state variables
            var lastValue = 0.0f
            
            // Rain states
            var rainClickActive = false
            var rainClickCount = 0
            var rainClickPhase = 0f
            var rainClickFreq = 0f

            // Forest states
            var forestWindPhase = 0f
            var forestChirpActive = false
            var forestChirpDuration = 0
            var forestChirpPhase = 0f
            var forestChirpFreqStart = 0f
            var forestChirpFreqEnd = 0f

            // Cafe states
            var cafeBabblePhase1 = 0f
            var cafeBabblePhase2 = 0f
            var cafeClinkActive = false
            var cafeClinkDuration = 0
            var cafeClinkPhase = 0f
            var cafeClinkFreq = 0f

            // Binaural states
            var binauralPhase1 = 0f
            var binauralPhase2 = 0f

            // Lo-Fi states
            var sampleCount = 0L
            var lofiVinylFilter = 0f
            var lofiPadFilter = 0f

            // Comfort Chain states
            var comfortSampleCount = 0L
            var comfortChordPhase1 = 0f
            var comfortChordPhase2 = 0f
            var comfortChordPhase3 = 0f
            var comfortChordPhase4 = 0f
            var comfortLeadPhase = 0f
            var comfortLeadCurrentFreq = 261.63f // Middle C default

            while (isPlaying) {
                val soundTypeLocal = currentSoundType

                for (i in buffer.indices) {
                    var sample = 0.0f

                    when (soundTypeLocal) {
                        "Comfort Chain", "Comfort Chain (Silent)", "Instupendo - Comfort Chain" -> {
                            // Synthesize a beautiful, slow-swelling nostalgic ambient progression inspired by Instupendo - Comfort Chain
                            comfortSampleCount++
                            
                            // A chord change every 4 seconds (176400 samples at 44.1kHz)
                            val chordProgress = (comfortSampleCount % 176400) / 176400f
                            val chordIdx = ((comfortSampleCount / 176400) % 4).toInt()
                            
                            // Frequencies for major/minor 7th chords:
                            // Chord 0: Dbmaj7 (Db3: 138.59, F3: 174.61, Ab3: 207.65, C4: 261.63)
                            // Chord 1: Fm7    (F3: 174.61, Ab3: 207.65, C4: 261.63, Eb4: 311.13)
                            // Chord 2: Bbm7   (Bb2: 116.54, Db3: 138.59, F3: 174.61, Ab3: 207.65)
                            // Chord 3: Gbmaj7 (Gb2: 98.00, Bb2: 116.54, Db3: 138.59, F3: 174.61)
                            val (f1, f2, f3, f4) = when (chordIdx) {
                                0 -> listOf(138.59f, 174.61f, 207.65f, 261.63f)
                                1 -> listOf(174.61f, 207.65f, 261.63f, 311.13f)
                                2 -> listOf(116.54f, 138.59f, 174.61f, 207.65f)
                                else -> listOf(98.00f, 116.54f, 138.59f, 174.61f)
                            }
                            
                            // Smoothly swell the volume in and out (attack & release)
                            val swell = if (chordProgress < 0.2f) {
                                chordProgress / 0.2f
                            } else if (chordProgress > 0.8f) {
                                (1f - chordProgress) / 0.2f
                            } else {
                                1.0f
                            }
                            
                            // Increment pad phases
                            comfortChordPhase1 += (2f * PI.toFloat() * f1) / 44100f
                            comfortChordPhase2 += (2f * PI.toFloat() * f2) / 44100f
                            comfortChordPhase3 += (2f * PI.toFloat() * f3) / 44100f
                            comfortChordPhase4 += (2f * PI.toFloat() * f4) / 44100f
                            
                            // Normalize phases to avoid precision loss
                            val twoPi = 2f * PI.toFloat()
                            if (comfortChordPhase1 > twoPi) comfortChordPhase1 -= twoPi
                            if (comfortChordPhase2 > twoPi) comfortChordPhase2 -= twoPi
                            if (comfortChordPhase3 > twoPi) comfortChordPhase3 -= twoPi
                            if (comfortChordPhase4 > twoPi) comfortChordPhase4 -= twoPi
                            
                            val padSample = (sin(comfortChordPhase1) + sin(comfortChordPhase2) + 
                                            sin(comfortChordPhase3) + sin(comfortChordPhase4)) * 0.045f
                            
                            // Play slow nostalgic gliding lead notes (change note every 2 seconds)
                            val noteIdx = ((comfortSampleCount / 88200) % 8).toInt()
                            val melodyNotes = listOf(
                                523.25f, // C5
                                587.33f, // D5
                                659.25f, // E5
                                783.99f, // G5
                                880.00f, // A5
                                783.99f, // G5
                                659.25f, // E5
                                523.25f  // C5
                            )
                            val leadTargetFreq = melodyNotes[noteIdx]
                            // Dynamic gliding (portamento)
                            comfortLeadCurrentFreq = comfortLeadCurrentFreq * 0.9992f + leadTargetFreq * 0.0008f
                            
                            comfortLeadPhase += (2f * PI.toFloat() * comfortLeadCurrentFreq) / 44100f
                            if (comfortLeadPhase > twoPi) comfortLeadPhase -= twoPi
                            
                            // Pure cute lead with subtle vibrato
                            val vibrato = 1.0f + 0.012f * sin(comfortSampleCount * 0.0006f)
                            val leadSample = sin(comfortLeadPhase * vibrato) * 0.035f * swell
                            
                            sample = (padSample + leadSample) * 0.55f * comfortChainVolume
                        }
                        "Rain" -> {
                            // Deep ambient rain rumble
                            val white = random.nextFloat() * 2.0f - 1.0f
                            lastValue = 0.95f * lastValue + 0.05f * white
                            sample = lastValue * 0.12f

                            // Random rain droplet click triggers
                            if (!rainClickActive && random.nextFloat() < 0.0003f) {
                                rainClickActive = true
                                rainClickCount = random.nextInt(35) + 10
                                rainClickPhase = 0f
                                rainClickFreq = random.nextFloat() * 0.12f + 0.06f
                            }

                            if (rainClickActive) {
                                val envelope = rainClickCount.toFloat() / 45f
                                sample += sin(rainClickPhase) * envelope * 0.14f
                                rainClickPhase += rainClickFreq
                                rainClickCount--
                                if (rainClickCount <= 0) rainClickActive = false
                            }
                        }
                        "Forest" -> {
                            // Slow periodic wind gusts
                            forestWindPhase += 0.00003f
                            if (forestWindPhase > 2 * PI) forestWindPhase = 0f
                            val windMod = (sin(forestWindPhase) * 0.35f + 0.65f)

                            val white = random.nextFloat() * 2.0f - 1.0f
                            lastValue = 0.90f * lastValue + 0.10f * white
                            sample = lastValue * 0.13f * windMod

                            // Random bird/cricket chirps
                            if (!forestChirpActive && random.nextFloat() < 0.000025f) {
                                forestChirpActive = true
                                forestChirpDuration = random.nextInt(3500) + 1500
                                forestChirpPhase = 0f
                                forestChirpFreqStart = random.nextFloat() * 0.12f + 0.15f
                                forestChirpFreqEnd = forestChirpFreqStart + 0.04f
                            }

                            if (forestChirpActive) {
                                val progress = forestChirpDuration.toFloat() / 5000f
                                val currentFreq = forestChirpFreqStart + (1f - progress) * (forestChirpFreqEnd - forestChirpFreqStart)
                                val amp = sin(progress * PI.toFloat()) * (0.4f + 0.6f * sin(progress * 35f))
                                sample += sin(forestChirpPhase) * amp * 0.025f
                                forestChirpPhase += currentFreq
                                forestChirpDuration--
                                if (forestChirpDuration <= 0) forestChirpActive = false
                            }
                        }
                        "Cafe" -> {
                            // Room chatter rumble (very muffled voice frequencies)
                            val white = random.nextFloat() * 2.0f - 1.0f
                            lastValue = 0.93f * lastValue + 0.07f * white

                            cafeBabblePhase1 += 0.0008f
                            cafeBabblePhase2 += 0.0013f
                            val babble = sin(cafeBabblePhase1) * 0.016f + sin(cafeBabblePhase2) * 0.012f
                            sample = lastValue * 0.11f + babble

                            // Occasional porcelain coffee cup/saucer clinks
                            if (!cafeClinkActive && random.nextFloat() < 0.000035f) {
                                cafeClinkActive = true
                                cafeClinkDuration = random.nextInt(750) + 300
                                cafeClinkPhase = 0f
                                cafeClinkFreq = random.nextFloat() * 0.15f + 0.38f
                            }

                            if (cafeClinkActive) {
                                val progress = cafeClinkDuration.toFloat() / 1050f
                                val envelope = progress * progress
                                sample += sin(cafeClinkPhase) * envelope * 0.045f
                                cafeClinkPhase += cafeClinkFreq
                                cafeClinkDuration--
                                if (cafeClinkDuration <= 0) cafeClinkActive = false
                            }
                        }
                        "Binaural" -> {
                            // Synthesize rhythmic Binaural Beat (100Hz and 105Hz waves offset to create 5Hz delta)
                            binauralPhase1 += 0.0142f // ~100Hz
                            binauralPhase2 += 0.0149f // ~105Hz (5Hz delta)
                            sample = (sin(binauralPhase1) + sin(binauralPhase2)) * 0.06f
                        }
                        "White Noise" -> {
                            // Classic white/pink comforting noise
                            val white = random.nextFloat() * 2.0f - 1.0f
                            lastValue = 0.85f * lastValue + 0.15f * white
                            sample = lastValue * 0.12f
                        }
                        "Lo-Fi Beats" -> {
                            // 1. Vinyl Hiss and Crackle
                            val vinylHiss = (random.nextFloat() * 2f - 1f) * 0.012f
                            var vinylPop = 0f
                            if (random.nextFloat() < 0.00004f) {
                                vinylPop = (random.nextFloat() * 2f - 1f) * 0.12f
                            }
                            lofiVinylFilter = 0.85f * lofiVinylFilter + 0.15f * (vinylHiss + vinylPop)

                            // 2. Swelling Rhodes-like Chords
                            val barPosition = (sampleCount % 132300).toInt()
                            val barProgress = barPosition.toFloat() / 132300f
                            val swell = when {
                                barProgress < 0.15f -> barProgress / 0.15f
                                barProgress > 0.85f -> (1.0f - barProgress) / 0.15f
                                else -> 1.0f
                            }
                            val flutter = 1.0f + 0.002f * sin(2.0 * PI * 4.5 * sampleCount / 44100.0).toFloat()
                            val currentBar = ((sampleCount / 132300) % 4).toInt()
                            val freqs = when (currentBar) {
                                0 -> floatArrayOf(110.0f, 220.0f, 261.63f, 329.63f, 392.00f) // Am9 / Am7
                                1 -> floatArrayOf(146.83f, 174.61f, 220.0f, 261.63f, 329.63f) // Dm9 / Dm7
                                2 -> floatArrayOf(98.00f, 196.00f, 246.94f, 293.66f, 349.23f, 440.00f) // G13
                                else -> floatArrayOf(130.81f, 164.81f, 196.00f, 246.94f, 293.66f) // Cmaj9 / Cmaj7
                            }
                            var padSample = 0.0f
                            for (f in freqs) {
                                val phaseAngle = 2.0 * PI * f * (sampleCount.toDouble() / 44100.0) * flutter
                                val ratio = ((phaseAngle / (2.0 * PI)) % 1.0).toFloat()
                                val tri = 2f * kotlin.math.abs(2f * ratio - 1f) - 1f
                                padSample += tri
                            }
                            padSample /= freqs.size
                            lofiPadFilter = 0.7f * lofiPadFilter + 0.3f * padSample
                            val padSound = lofiPadFilter * swell * 0.08f

                            // 3. Boom-Bap Drum Loops
                            val beatPosition = (sampleCount % 33075).toInt()
                            val currentBeatInBar = ((sampleCount % 132300) / 33075).toInt()

                            // Kick triggers on beat 1, late-beat 2, and beat 3
                            val kickAge = when {
                                currentBeatInBar == 0 -> beatPosition
                                currentBeatInBar == 1 && beatPosition > 28000 -> beatPosition - 28000
                                currentBeatInBar == 2 -> beatPosition
                                else -> -1
                            }
                            var kickSound = 0f
                            if (kickAge >= 0 && kickAge < 6000) {
                                val tKick = kickAge.toFloat() / 44100f
                                val freqSweep = 110f * kotlin.math.exp(-tKick * 45.0).toFloat() + 45f
                                val env = kotlin.math.exp(-tKick * 20.0).toFloat()
                                kickSound = sin(2f * PI.toFloat() * freqSweep * tKick) * env * 0.22f
                            }

                            // Snare triggers on beat 2 and beat 4
                            val snareAge = when {
                                currentBeatInBar == 1 -> beatPosition
                                currentBeatInBar == 3 -> beatPosition
                                else -> -1
                            }
                            var snareSound = 0f
                            if (snareAge >= 0 && snareAge < 6000) {
                                val tSnare = snareAge.toFloat() / 44100f
                                val toneEnv = kotlin.math.exp(-tSnare * 40.0).toFloat()
                                val noiseEnv = kotlin.math.exp(-tSnare * 18.0).toFloat()
                                val snareTone = sin(2f * PI.toFloat() * 175f * tSnare) * toneEnv * 0.12f
                                val snareNoise = (random.nextFloat() * 2f - 1f) * noiseEnv * 0.14f
                                snareSound = snareTone + snareNoise
                            }

                            // Hi-Hat ticks on eighth notes (every half-beat)
                            val hatAge = beatPosition % 16537
                            var hatSound = 0f
                            if (hatAge < 1200) {
                                val tHat = hatAge.toFloat() / 1200f
                                val hatEnv = (1f - tHat) * (1f - tHat)
                                val noise = random.nextFloat() * 2f - 1f
                                hatSound = noise * hatEnv * 0.018f
                            }

                            sample = padSound + kickSound + snareSound + hatSound + lofiVinylFilter
                        }
                        else -> {
                            // "Brown Noise" / Default (Warm, soft pinkish-brown rumble)
                            val white = random.nextFloat() * 2.0f - 1.0f
                            lastValue = 0.88f * lastValue + 0.12f * white
                            sample = lastValue * 0.15f
                        }
                    }

                    // Write sound to short PCM-16bit buffer
                    buffer[i] = (sample * 32767.0f).toInt().coerceIn(-32768, 32767).toShort()
                    sampleCount++
                }
                track.write(buffer, 0, buffer.size)

                // Calculate visualizer data from current active sound to feed the real-time UI
                val visData = FloatArray(8)
                val soundTypeLocalVal = currentSoundType
                if (isPlaying) {
                    when (soundTypeLocalVal) {
                        "Comfort Chain", "Comfort Chain (Silent)", "Instupendo - Comfort Chain" -> {
                            val chordProgress = (comfortSampleCount % 176400) / 176400f
                            val chordIdx = ((comfortSampleCount / 176400) % 4).toInt()
                            val noteIdx = ((comfortSampleCount / 88200) % 8).toInt()
                            val swell = if (chordProgress < 0.2f) {
                                chordProgress / 0.2f
                            } else if (chordProgress > 0.8f) {
                                (1f - chordProgress) / 0.2f
                            } else {
                                1.0f
                            }
                            
                            visData[0] = swell * (0.4f + 0.15f * sin(comfortSampleCount * 0.0003f).toFloat()) * comfortChainVolume
                            visData[1] = swell * (0.35f + 0.2f * cos(comfortSampleCount * 0.0004f).toFloat()) * comfortChainVolume
                            visData[2] = swell * (0.45f + 0.12f * sin(comfortSampleCount * 0.00025f).toFloat()) * comfortChainVolume
                            visData[3] = swell * (0.22f + 0.08f * chordIdx) * comfortChainVolume
                            visData[4] = swell * (0.18f + 0.1f * (3 - chordIdx)) * comfortChainVolume
                            visData[5] = (if (noteIdx % 3 == 0) swell * 0.85f else swell * 0.3f) * comfortChainVolume
                            visData[6] = (if (noteIdx % 3 == 1) swell * 0.75f else swell * 0.25f) * comfortChainVolume
                            visData[7] = (if (noteIdx % 3 == 2) swell * 0.65f else swell * 0.2f) * comfortChainVolume
                        }
                        "Rain" -> {
                            for (idx in 0..7) {
                                val base = if (idx < 3) 0.6f else 0.2f
                                val noise = random.nextFloat() * 0.2f
                                visData[idx] = (base + noise).coerceIn(0f, 1f)
                            }
                        }
                        "Forest" -> {
                            val windVal = 0.3f + 0.25f * sin(forestWindPhase).toFloat()
                            for (idx in 0..7) {
                                if (idx < 2) {
                                    visData[idx] = windVal
                                } else if (forestChirpActive && idx in 5..6) {
                                    visData[idx] = 0.7f + random.nextFloat() * 0.2f
                                } else {
                                    visData[idx] = 0.1f + random.nextFloat() * 0.1f
                                }
                            }
                        }
                        "Cafe" -> {
                            for (idx in 0..7) {
                                val b = if (idx in 2..5) 0.55f else 0.25f
                                visData[idx] = (b + random.nextFloat() * 0.18f).coerceIn(0f, 1f)
                            }
                        }
                        "Binaural" -> {
                            val beatPulse = 0.5f + 0.4f * sin(binauralPhase2 - binauralPhase1).toFloat()
                            for (idx in 0..7) {
                                visData[idx] = if (idx < 3) beatPulse else 0.08f
                            }
                        }
                        "Lo-Fi Beats" -> {
                            val tLo = (sampleCount % 44100).toFloat() / 44100f
                            val kickPulse = if (tLo < 0.15f) 0.8f else 0.3f
                            for (idx in 0..7) {
                                if (idx < 2) {
                                    visData[idx] = kickPulse
                                } else {
                                    visData[idx] = 0.25f + 0.2f * sin(sampleCount * 0.0001f + idx).toFloat()
                                }
                            }
                        }
                        else -> {
                            for (idx in 0..7) {
                                visData[idx] = (0.55f - idx * 0.06f + random.nextFloat() * 0.1f).coerceIn(0.05f, 1f)
                            }
                        }
                    }
                }
                _visualizerFlow.value = visData
            }
            } finally {
                _visualizerFlow.value = FloatArray(8) { 0f }
                try {
                    track.stop()
                } catch (e: Exception) {}
                try {
                    track.release()
                } catch (e: Exception) {}
                val ctx = context ?: appContextRef?.get()
                ctx?.let {
                    abandonFocus(it)
                }
            }
        }
    }

    fun setSoundType(soundType: String) {
        currentSoundType = soundType
    }

    fun stop(context: android.content.Context? = null) {
        isPlaying = false
        audioTrack = null
        playbackThread = null
        _visualizerFlow.value = FloatArray(8) { 0f }

        val ctx = context ?: appContextRef?.get()
        ctx?.let {
            abandonFocus(it)
            checkAndStopService(it)
        }
    }

    private fun checkAndStopService(context: android.content.Context) {
        val isBinauralPlaying = try {
            com.focuscube.audio.StereoBinauralEngine.isCurrentlyPlaying()
        } catch (e: Exception) {
            false
        }

        if (!isPlaying && !isBinauralPlaying) {
            try {
                val serviceIntent = android.content.Intent(context, AudioPlaybackService::class.java).apply {
                    action = AudioPlaybackService.ACTION_STOP
                }
                context.stopService(serviceIntent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
