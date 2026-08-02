package com.example.data

import android.content.Context
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FocusSessionManager(
    private val context: Context,
    private val repository: FocusRepository,
    private val shieldRepository: ShieldRepository,
    private val coroutineScope: CoroutineScope
) {
    private val _sessionState = MutableStateFlow(FocusSessionState.IDLE)
    val sessionState: StateFlow<FocusSessionState> = _sessionState

    private val _label = MutableStateFlow("")
    val label: StateFlow<String> = _label

    private val _totalMinutes = MutableStateFlow(0)
    val totalMinutes: StateFlow<Int> = _totalMinutes

    private val _secondsLeft = MutableStateFlow(0)
    val secondsLeft: StateFlow<Int> = _secondsLeft

    private val _progress = MutableStateFlow(1f)
    val progress: StateFlow<Float> = _progress

    private val _mode = MutableStateFlow("Deep Work")
    val mode: StateFlow<String> = _mode

    private val _startTime = MutableStateFlow(0L)
    val startTime: StateFlow<Long> = _startTime

    private val _interruptionCount = MutableStateFlow(0)
    val interruptionCount: StateFlow<Int> = _interruptionCount

    private var timerJob: Job? = null

    fun startFocusSession(sessionLabel: String, minutes: Int, sessionMode: String = "Deep Work") {
        _label.value = sessionLabel
        _totalMinutes.value = minutes
        _secondsLeft.value = minutes * 60
        _progress.value = 1f
        _startTime.value = System.currentTimeMillis()
        _mode.value = sessionMode
        _interruptionCount.value = 0
        _sessionState.value = FocusSessionState.RUNNING

        // Integrate with ShieldRepository for automated focus shielding during active sessions
        shieldRepository.setShieldActive(true)

        // Keep SharedPreferences in sync for foreground service overlay
        updateServicePreferences(running = true, seconds = minutes * 60)

        startTimerJob()
    }

    private fun startTimerJob() {
        timerJob?.cancel()
        timerJob = coroutineScope.launch {
            while (_secondsLeft.value > 0 && _sessionState.value == FocusSessionState.RUNNING) {
                delay(1000)
                if (_sessionState.value == FocusSessionState.RUNNING) {
                    _secondsLeft.value -= 1
                    _progress.value = _secondsLeft.value.toFloat() / (_totalMinutes.value * 60).toFloat()
                    
                    updateServicePreferences(running = true, seconds = _secondsLeft.value)
                }
            }
            if (_secondsLeft.value == 0 && _sessionState.value == FocusSessionState.RUNNING) {
                finishFocusSession()
            }
        }
    }

    fun pauseTimer() {
        if (_sessionState.value == FocusSessionState.RUNNING) {
            _sessionState.value = FocusSessionState.PAUSED
            updateServicePreferences(running = false, seconds = _secondsLeft.value)
        }
    }

    fun resumeTimer() {
        if (_sessionState.value == FocusSessionState.PAUSED) {
            _sessionState.value = FocusSessionState.RUNNING
            updateServicePreferences(running = true, seconds = _secondsLeft.value)
            startTimerJob()
        }
    }

    fun addFiveMinutes() {
        _totalMinutes.value += 5
        _secondsLeft.value += 5 * 60
        _progress.value = _secondsLeft.value.toFloat() / (_totalMinutes.value * 60).toFloat()
        updateServicePreferences(running = _sessionState.value == FocusSessionState.RUNNING, seconds = _secondsLeft.value)
    }

    fun incrementInterruption() {
        _interruptionCount.value += 1
    }

    fun stopTimerEarly() {
        timerJob?.cancel()
        val elapsedMinutes = _totalMinutes.value - (_secondsLeft.value / 60)
        _sessionState.value = FocusSessionState.INTERRUPTED

        // Disable Focus Shield when session is cancelled/interrupted
        shieldRepository.setShieldActive(false)
        updateServicePreferences(running = false, seconds = 0)

        coroutineScope.launch {
            repository.insertFocusSession(
                FocusSession(
                    label = _label.value,
                    startTime = _startTime.value,
                    endTime = System.currentTimeMillis(),
                    mode = _mode.value,
                    plannedMinutes = _totalMinutes.value,
                    actualMinutes = elapsedMinutes,
                    interruptionCount = _interruptionCount.value,
                    completed = false
                )
            )
        }
    }

    fun finishFocusSession() {
        timerJob?.cancel()
        _sessionState.value = FocusSessionState.COMPLETED

        // Disable Focus Shield when session completes
        shieldRepository.setShieldActive(false)
        updateServicePreferences(running = false, seconds = 0)

        val elapsedMinutes = _totalMinutes.value
        coroutineScope.launch {
            repository.insertFocusSession(
                FocusSession(
                    label = _label.value,
                    startTime = _startTime.value,
                    endTime = System.currentTimeMillis(),
                    mode = _mode.value,
                    plannedMinutes = _totalMinutes.value,
                    actualMinutes = elapsedMinutes,
                    interruptionCount = _interruptionCount.value,
                    completed = true
                )
            )
            repository.insertSession(
                WorkSession(
                    label = _label.value,
                    durationMinutes = _totalMinutes.value,
                    focusTimeMinutes = elapsedMinutes,
                    timestamp = System.currentTimeMillis(),
                    category = _mode.value
                )
            )
        }
    }

    private fun updateServicePreferences(running: Boolean, seconds: Int) {
        val prefs = context.getSharedPreferences("focus_cube_prefs", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putBoolean("is_timer_running_for_service", running)
            putBoolean("is_shield_active_for_service", shieldRepository.isShieldActive.value)
            putInt("timer_seconds_left_for_service", seconds)
            apply()
        }
    }
}
