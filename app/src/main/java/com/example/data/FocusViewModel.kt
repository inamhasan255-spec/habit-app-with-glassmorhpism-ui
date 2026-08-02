package com.example.data

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.first

enum class AppScreen {
    SPLASH,
    ONBOARDING,
    AUTH,
    MAIN,
    FOCUS_ACTIVE,
    FOCUS_COMPLETE


}
enum class AppTab {
    HOME,
    HABITS,
    SHIELD,
    STATS,
    ACADEMIC,
    TIMETABLE,
    COMMUNITY,
    SETTINGS
}

enum class AppMode {
    FOCUS,
    JOURNAL,
    READING,
    TRACKING
}

sealed class AuthState {
    object Loading : AuthState()
    data class Authenticated(val email: String, val uid: String) : AuthState()
    object Unauthenticated : AuthState()
    data class Error(val message: String) : AuthState()
}
class FocusViewModel(application: Application) : AndroidViewModel(application) {
    private val blockPreferences = com.focuscube.core.datastore.BlockPreferences(application)

    val repository: FocusRepository
    private var prefsListener: SharedPreferences.OnSharedPreferenceChangeListener? = null

    // Navigation States
    private val _currentScreen = MutableStateFlow(AppScreen.SPLASH)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _currentTab = MutableStateFlow(AppTab.HOME)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    val whiteNoiseVisualizerFlow: StateFlow<FloatArray> = WhiteNoisePlayer.visualizerFlow

    // Confetti / Particle Explosion trigger for task/habit completions
    private val _confettiTrigger = MutableStateFlow<Long>(0L)
    val confettiTrigger: StateFlow<Long> = _confettiTrigger.asStateFlow()

    fun triggerConfettiBurst() {
        _confettiTrigger.value = System.currentTimeMillis()
    }

    // Mode States
    private val _currentMode = MutableStateFlow(AppMode.FOCUS)
    val currentMode: StateFlow<AppMode> = _currentMode.asStateFlow()

    private var readingEntryTime: Long = System.currentTimeMillis()
    private var journalingEntryTime: Long = System.currentTimeMillis()
    var isBackgroundAmbientEnabled by mutableStateOf(true)
    var dashboardError by mutableStateOf<String?>(null)

    fun reportDashboardError(message: String) {
        dashboardError = message
    }

    fun clearDashboardError() {
        dashboardError = null
    }

    fun toggleBackgroundAmbient() {
        isBackgroundAmbientEnabled = !isBackgroundAmbientEnabled
        val prefs = getApplication<Application>().getSharedPreferences("focus_cube_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("is_background_ambient_enabled", isBackgroundAmbientEnabled).apply()
        updateBackgroundAmbientForMode(_currentMode.value)
    }

    fun setMode(mode: AppMode) {
        val oldMode = _currentMode.value
        if (oldMode != mode) {
            handleModeTransitionOut(oldMode)
            handleModeTransitionIn(mode)
            _currentMode.value = mode
            updateBackgroundAmbientForMode(mode)
        }
    }

    fun updateBackgroundAmbientForMode(mode: AppMode) {
        if (!isBackgroundAmbientEnabled) {
            WhiteNoisePlayer.stop()
            return
        }
        val soundType = when (mode) {
            AppMode.FOCUS -> "White Noise"
            AppMode.READING -> "Forest"
            AppMode.JOURNAL -> "Rain"
            AppMode.TRACKING -> "Cafe"
        }
        WhiteNoisePlayer.start(soundType, getApplication())
    }

    private fun handleModeTransitionIn(mode: AppMode) {
        when (mode) {
            AppMode.READING -> {
                readingEntryTime = System.currentTimeMillis()
            }
            AppMode.JOURNAL -> {
                journalingEntryTime = System.currentTimeMillis()
            }
            else -> {}
        }
    }

    private fun handleModeTransitionOut(mode: AppMode) {
        val now = System.currentTimeMillis()
        when (mode) {
            AppMode.READING -> {
                val durationMs = now - readingEntryTime
                val durationSeconds = durationMs / 1000
                if (durationSeconds >= 2) {
                    val durationMinutes = (durationSeconds / 60).toInt().coerceAtLeast(1)
                    logAutoHabitSession("Reading", durationMinutes, "Learn", "#10B981")
                }
            }
            AppMode.JOURNAL -> {
                val durationMs = now - journalingEntryTime
                val durationSeconds = durationMs / 1000
                if (durationSeconds >= 2) {
                    val durationMinutes = (durationSeconds / 60).toInt().coerceAtLeast(1)
                    logAutoHabitSession("Journaling", durationMinutes, "Mindful", "#3B82F6")
                }
            }
            else -> {}
        }
    }

    fun logAutoHabitSession(habitName: String, durationMinutes: Int, category: String, colorHex: String) {
        viewModelScope.launch {
            val existingHabit = habits.value.find { it.name.equals(habitName, ignoreCase = true) }
            val today = getTodayDateString()
            
            val habitToLog = if (existingHabit != null) {
                existingHabit
            } else {
                val newHabit = Habit(
                    name = habitName,
                    category = category,
                    colorHex = colorHex,
                    targetValue = if (habitName == "Reading") 20 else 10,
                    unit = "mins"
                )
                val newId = repository.insertHabit(newHabit)
                newHabit.copy(id = newId.toInt())
            }

            val existingCompletion = completions.value.find { it.habitId == habitToLog.id && it.date == today }
            val currentVal = existingCompletion?.value ?: 0
            val newVal = currentVal + durationMinutes
            
            repository.updateHabitProgress(habitToLog, today, newVal)
            
            if (newVal >= habitToLog.targetValue && currentVal < habitToLog.targetValue) {
                triggerHapticPattern("SUCCESS")
            } else {
                triggerHapticPattern("START")
            }
            syncDataToFirebase()
        }
    }

    fun triggerHapticPattern(patternType: String) {
        try {
            val vibrator = getApplication<Application>().getSystemService(Context.VIBRATOR_SERVICE) as? android.os.Vibrator
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val effect = when (patternType) {
                        "SUCCESS" -> {
                            android.os.VibrationEffect.createWaveform(
                                longArrayOf(0, 80, 120, 80, 50, 180),
                                intArrayOf(0, 255, 0, 180, 0, 255),
                                -1
                            )
                        }
                        "START" -> {
                            android.os.VibrationEffect.createOneShot(120, 220)
                        }
                        "STOP" -> {
                            android.os.VibrationEffect.createWaveform(
                                longArrayOf(0, 150, 80, 50),
                                intArrayOf(0, 255, 0, 120),
                                -1
                            )
                        }
                        else -> {
                            android.os.VibrationEffect.createOneShot(50, android.os.VibrationEffect.DEFAULT_AMPLITUDE)
                        }
                    }
                    vibrator.vibrate(effect)
                } else {
                    @Suppress("DEPRECATION")
                    when (patternType) {
                        "SUCCESS" -> vibrator.vibrate(longArrayOf(0, 80, 120, 80, 50, 180), -1)
                        "START" -> vibrator.vibrate(120)
                        "STOP" -> vibrator.vibrate(longArrayOf(0, 150, 80, 50), -1)
                        else -> vibrator.vibrate(50)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Database Streams
    val habits: StateFlow<List<Habit>>
    val completions: StateFlow<List<HabitCompletion>>
    val sessions: StateFlow<List<WorkSession>>
    val allFocusSessions: StateFlow<List<FocusSession>>
    val blockedApps: StateFlow<List<BlockedApp>>
    val essentialItems: StateFlow<List<EssentialItem>>

    // New Mode Database Streams
    // Gamification
    val userStats: StateFlow<UserStats?>
    val allBadges: StateFlow<List<Badge>>

    val journalEntries: StateFlow<List<JournalEntry>>
    val books: StateFlow<List<ReadingBook>>
    val chatMessages: StateFlow<List<ChatMessage>>

    // Academic Mode Streams
    val academicSubjects: StateFlow<List<AcademicSubject>>
    val timetableEntries: StateFlow<List<TimetableEntry>>
    val academicTasks: StateFlow<List<AcademicTask>>
    val academicProfile: StateFlow<AcademicProfile?>
    val voiceNotes: StateFlow<List<VoiceNote>>
    val academicChatThreads: StateFlow<List<AcademicChatThread>>
    val activeAcademicThreadId = MutableStateFlow<String?>(null)
    private val _activeAcademicMessages = MutableStateFlow<List<AcademicChatMessage>>(emptyList())
    val activeAcademicMessages: StateFlow<List<AcademicChatMessage>> = _activeAcademicMessages.asStateFlow()
    var isAcademicChatLoading by mutableStateOf(false)

    // DataStore Preferences Manager
    private val dataStoreManager = DataStoreManager(application)
    val whitelistedApps: StateFlow<Set<String>>
    
    val galaxyVisible: StateFlow<Boolean> = dataStoreManager.galaxyVisible.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )
    val galaxyStarSpeed: StateFlow<Float> = dataStoreManager.galaxyStarSpeed.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 1.0f
    )
    val galaxyHueShift: StateFlow<Float> = dataStoreManager.galaxyHueShift.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0f
    )

    fun updateGalaxyPreferences(visible: Boolean, speed: Float, hue: Float) {
        viewModelScope.launch {
            dataStoreManager.saveGalaxyPreferences(visible, speed, hue)
        }
    }

    private val commandExecutor = object : CommandExecutor {
        override fun execute(commandName: String, args: Map<String, Any>): String {
            return when (commandName) {
                "startFocusSession" -> {
                    val label = args["label"]?.toString() ?: "Focus Block"
                    val duration = (args["durationMinutes"] as? Number)?.toInt() ?: 25
                    val mode = args["mode"]?.toString() ?: "General"
                    startFocusSession(label, duration, mode)
                    "Started focus block: '$label' ($duration mins)"
                }
                "addTask" -> {
                    val title = args["title"]?.toString() ?: "New Task"
                    val subject = args["subject"]?.toString() ?: "General"
                    val dueInDays = (args["dueInDays"] as? Number)?.toInt() ?: 2
                    val priority = args["priority"]?.toString() ?: "Medium"
                    val calendar = java.util.Calendar.getInstance()
                    calendar.add(java.util.Calendar.DAY_OF_YEAR, dueInDays)
                    addAcademicTask(title, subject, calendar.timeInMillis, priority, "Added via AI assistant")
                    "Added academic task: '$title' for subject '$subject'"
                }
                "addHabit" -> {
                    val name = args["name"]?.toString() ?: "New Habit"
                    val category = args["category"]?.toString() ?: "Health"
                    val colorHex = args["colorHex"]?.toString() ?: "#FF5722"
                    val targetValue = (args["targetValue"] as? Number)?.toInt() ?: 1
                    val unit = args["unit"]?.toString() ?: "times"
                    addHabit(name, category, colorHex, obsidianUri = "", targetValue = targetValue, unit = unit)
                    "Created new Habit: '$name' under category '$category'"
                }
                "switchTab", "navigate" -> {
                    val tabName = args["tabName"]?.toString() ?: args["screen"]?.toString() ?: "HOME"
                    try {
                        val matchedTab = AppTab.valueOf(tabName.uppercase())
                        setTab(matchedTab)
                        "Successfully switched dashboard tab to: $matchedTab"
                    } catch (e: Exception) {
                        "Failed to switch tab. Available tabs: HOME, HABITS, SHIELD, STATS, ACADEMIC, SETTINGS"
                    }
                }
                "toggleDarkMode" -> {
                    toggleDarkMode()
                    "Toggled dark mode. Active isDarkMode: $isDarkMode"
                }
                "addTimetableEntry", "addClass" -> {
                    val subjectName = args["subjectName"]?.toString() ?: args["subject"]?.toString() ?: "General Class"
                    val dayOfWeek = (args["dayOfWeek"] as? Number)?.toInt() ?: (args["day"] as? Number)?.toInt() ?: 1
                    val weekRotation = args["weekRotation"]?.toString() ?: "All Weeks"
                    val startTime = args["startTime"]?.toString() ?: args["start"]?.toString() ?: "09:00"
                    val endTime = args["endTime"]?.toString() ?: args["end"]?.toString() ?: "10:00"
                    val instructor = args["instructor"]?.toString() ?: ""
                    val room = args["room"]?.toString() ?: ""
                    val colorHex = args["colorHex"]?.toString() ?: "#3B82F6"
                    addTimetableEntry(subjectName, dayOfWeek, weekRotation, startTime, endTime, instructor, room, colorHex)
                    "Added timetable class entry: '$subjectName' on Day $dayOfWeek ($startTime - $endTime)"
                }
                else -> "Unknown command"
            }
        }
    }

    // Device apps list (for Focus Shield customization)
    private val _deviceApps = MutableStateFlow<List<DeviceAppInfo>>(emptyList())
    val deviceApps: StateFlow<List<DeviceAppInfo>> = _deviceApps.asStateFlow()

    // Timer States
    var timerLabel by mutableStateOf("Deep Work Block")
    var timerTotalMinutes by mutableStateOf(25)
    var timerSecondsLeft by mutableStateOf(25 * 60)
    var isTimerRunning by mutableStateOf(false)
    var isTimerPaused by mutableStateOf(false)
    var timerProgress by mutableStateOf(1f) // 0f to 1f

    // Active Focus Shield Blocker Overlay State
    var activeBlockedAppTriggered by mutableStateOf(false)
    var activeBlockedAppName by mutableStateOf("")
    var activeBlockedAppPackage by mutableStateOf("")

    fun triggerFocusBlockOverlay(packageName: String, appName: String) {
        activeBlockedAppTriggered = true
        activeBlockedAppPackage = packageName
        activeBlockedAppName = appName
        if (focusSessionState == FocusSessionState.RUNNING) {
            sessionInterruptionCount++
        }
    }

    fun dismissFocusBlockOverlay() {
        activeBlockedAppTriggered = false
        activeBlockedAppPackage = ""
        activeBlockedAppName = ""
    }

    private var timerJob: Job? = null
    private var sessionStartTime: Long = 0

    // Focus Session Engine State
    var focusSessionState by mutableStateOf(FocusSessionState.IDLE)
    var focusSessionMode by mutableStateOf("Deep Work") // "Deep Work", "Pomodoro", "Custom"
    var sessionInterruptionCount by mutableStateOf(0)

    fun onAppBackgrounded() {
        if (focusSessionState == FocusSessionState.RUNNING) {
            sessionInterruptionCount++
        }
    }

    // Settings
    var accentColorHex by mutableStateOf("#5adcb3") // Default Teal
    var soundsEnabled by mutableStateOf(true) // Sound toggle in settings
    var notificationsEnabled by mutableStateOf(true)
    var shieldActive by mutableStateOf(true)
    var profileName by mutableStateOf("")
    var profileTitle by mutableStateOf("Ready to Focus")
    var userRole by mutableStateOf("STUDENT") // "STUDENT" or "ADULT"
    var isDarkMode by mutableStateOf(false)
    var luminaAesthetic by mutableStateOf("light") // "light" or "dark" (Lumina aesthetic preference)
    var isWhiteNoiseEnabled by mutableStateOf(false)
    var isWhiteNoisePlayingAnytime by mutableStateOf(false)
    var selectedAmbientSound by mutableStateOf("White Noise") // default to White Noise
    var comfortChainVolume by mutableStateOf(0.6f) // Volume for Comfort Chain, defaults to 0.6
    var selectedSoundPack by mutableStateOf("Minimalist")
    var selectedCategory by mutableStateOf("Deep Work") // Pomodoro category selection

    // Reading Timer & Shelf States
    var readingTimerSecondsLeft by mutableStateOf(25 * 60)
    var isReadingTimerRunning by mutableStateOf(false)
    var activeReadingBook by mutableStateOf<ReadingBook?>(null)
    private var readingTimerJob: Job? = null

    // Private Coach Chat State
    var isCoachLoading by mutableStateOf(false)
    private val chatRepo = ChatRepository()

    // Post-Session Reflection / Journal Reflection States
    var showReflectionTrigger by mutableStateOf(false)
    var reflectionSessionMins by mutableStateOf(0)
    var reflectionSessionCategory by mutableStateOf("Deep Work")
    var isBackgroundEnabled by mutableStateOf(true)
    var backgroundImageUrl by mutableStateOf("")
    var useWhiteUi by mutableStateOf(false)
    var lessTransparency by mutableStateOf(true)

    // Widget Personalization States
    var widgetBackgroundType by mutableStateOf("solid_color") // "photo", "animated_lottie", "solid_color"
    var widgetPhotoBgUri by mutableStateOf("https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=500") 
    var widgetLottieTheme by mutableStateOf("Aurora Lights") // "Focus Bubble", "Cosmic Wave", "Aurora Lights"
    var widgetSolidColorHex by mutableStateOf("#111827") 

    // Dynamic Theme Glassmorphism properties
    var darkGlassBlur by mutableStateOf(16f)
    var darkGlassOpacity by mutableStateOf(0.85f)
    var lightGlassBlur by mutableStateOf(10f)
    var lightGlassOpacity by mutableStateOf(0.65f)

    val glassBlurIntensity: Float get() {
        return if (isDarkMode) darkGlassBlur else lightGlassBlur
    }

    val glassOpacity: Float get() {
        return if (isDarkMode) darkGlassOpacity else lightGlassOpacity
    }

    fun updateGlassmorphismProperties(blur: Float, opacity: Float) {
        val prefs = getApplication<Application>().getSharedPreferences("focus_cube_prefs", Context.MODE_PRIVATE)
        if (isDarkMode) {
            darkGlassBlur = blur
            darkGlassOpacity = opacity
            prefs.edit().apply {
                putFloat("dark_glass_blur", darkGlassBlur)
                putFloat("dark_glass_opacity", darkGlassOpacity)
                apply()
            }
        } else {
            lightGlassBlur = blur
            lightGlassOpacity = opacity
            prefs.edit().apply {
                putFloat("light_glass_blur", lightGlassBlur)
                putFloat("light_glass_opacity", lightGlassOpacity)
                apply()
            }
        }
    }

    // Calendar & Work Scheduler
    var scheduledWorkList by mutableStateOf<List<ScheduledWork>>(emptyList())
        private set

    fun loadSchedulesFromPrefs() {
        val prefs = getApplication<Application>().getSharedPreferences("focus_cube_prefs", Context.MODE_PRIVATE)
        val jsonStr = prefs.getString("scheduled_work_list", null)
        if (!jsonStr.isNullOrEmpty()) {
            try {
                val array = org.json.JSONArray(jsonStr)
                val list = mutableListOf<ScheduledWork>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        ScheduledWork(
                            id = obj.getString("id"),
                            title = obj.getString("title"),
                            date = obj.getString("date"),
                            durationMinutes = obj.getInt("durationMinutes"),
                            category = obj.getString("category"),
                            isGoogleSynced = obj.optBoolean("isGoogleSynced", false)
                        )
                    )
                }
                scheduledWorkList = list
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun saveSchedulesToPrefs() {
        val prefs = getApplication<Application>().getSharedPreferences("focus_cube_prefs", Context.MODE_PRIVATE)
        val array = org.json.JSONArray()
        for (work in scheduledWorkList) {
            val obj = org.json.JSONObject()
            obj.put("id", work.id)
            obj.put("title", work.title)
            obj.put("date", work.date)
            obj.put("durationMinutes", work.durationMinutes)
            obj.put("category", work.category)
            obj.put("isGoogleSynced", work.isGoogleSynced)
            array.put(obj)
        }
        prefs.edit().putString("scheduled_work_list", array.toString()).apply()
    }

    fun saveProfile(name: String, title: String) {
        profileName = name
        profileTitle = title
        val prefs = getApplication<Application>().getSharedPreferences("focus_cube_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("profile_name", name)
            .putString("profile_title", title)
            .apply()

        val user = mAuth?.currentUser
        if (user != null) {
            saveUserProfileToFirestore(user.uid, name, title)
        }
    }

    fun scheduleWork(title: String, dateString: String, durationMinutes: Int, category: String) {
        val work = ScheduledWork(
            id = java.util.UUID.randomUUID().toString(),
            title = title,
            date = dateString,
            durationMinutes = durationMinutes,
            category = category,
            isGoogleSynced = false
        )
        scheduledWorkList = scheduledWorkList + work
        saveSchedulesToPrefs()

        viewModelScope.launch {
            val success = addEventToGoogleCalendar(work)
            if (success) {
                scheduledWorkList = scheduledWorkList.map {
                    if (it.id == work.id) it.copy(isGoogleSynced = true) else it
                }
                saveSchedulesToPrefs()
            }
        }
    }

    suspend fun addEventToGoogleCalendar(work: ScheduledWork): Boolean {
        val client = okhttp3.OkHttpClient()
        val context = getApplication<Application>()
        val googleAccount = com.google.android.gms.auth.api.signin.GoogleSignIn.getLastSignedInAccount(context)
        val token = googleAccount?.idToken ?: return false

        val json = """
            {
              "summary": "${work.title}",
              "description": "Focus session scheduled via FocusCube Glassmorphism Habit Tracker.",
              "start": {
                "dateTime": "${work.date}T09:00:00Z",
                "timeZone": "UTC"
              },
              "end": {
                "dateTime": "${work.date}T10:00:00Z",
                "timeZone": "UTC"
              }
            }
        """.trimIndent()

        val mediaType = "application/json".toMediaTypeOrNull()
        val body = okhttp3.RequestBody.Companion.create(mediaType, json)
        val request = okhttp3.Request.Builder()
            .url("https://www.googleapis.com/calendar/v3/calendars/primary/events")
            .addHeader("Authorization", "Bearer $token")
            .post(body)
            .build()

        return withContext(Dispatchers.IO) {
            try {
                val response = client.newCall(request).execute()
                val code = response.code
                android.util.Log.d("GoogleCalendarSync", "HTTP $code when adding calendar event")
                code == 200 || code == 201
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    // Dynamic Theme Colors - Support for Premium Light-Based White Glass Theme and Dark Theme
    val themeBg: Color get() {
        return if (isDarkMode) Color(0xFF070512) else Color(0xFFF1F5F9)
    }
    
    val themeSurface: Color get() {
        return if (isDarkMode) Color(0xD912121F) else Color(0xB2FFFFFF)
    }
    
    val themeText: Color get() {
        return if (isDarkMode) Color(0xFFFFFFFF) else Color(0xFF0F172A)
    }
    
    val themeTextSecondary: Color get() {
        return if (isDarkMode) Color(0xFF7A7A9A) else Color(0xFF475569)
    }
    
    val themeBorder: Color get() {
        return if (isDarkMode) Color(0x1BFFFFFF) else Color(0x1F000000)
    }
    
    val themeGlassFill: Color get() {
        val alpha = glassOpacity
        return if (isDarkMode) {
            Color(0xFF0F0F1A).copy(alpha = alpha.coerceIn(0.6f, 0.9f))
        } else {
            Color(0xFFFFFFFF).copy(alpha = alpha.coerceIn(0.5f, 0.85f))
        }
    }
    
    val themeGlassBorder: Color get() {
        return if (isDarkMode) {
            val borderAlpha = (glassOpacity * 0.5f).coerceIn(0.2f, 0.8f)
            Color(0xFFFFFFFF).copy(alpha = borderAlpha)
        } else {
            val borderAlpha = (glassOpacity * 0.35f).coerceIn(0.08f, 0.3f)
            Color(0xFF000000).copy(alpha = borderAlpha)
        }
    }
    
    val themeDivider: Color get() {
        return if (isDarkMode) Color(0x0DFFFFFF) else Color(0x14000000)
    }

    fun setBackgroundImage(url: String) {
        backgroundImageUrl = url
        val prefs = getApplication<Application>().getSharedPreferences("focus_cube_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("background_image_url", url).apply()
    }

    fun toggleBackground() {
        isBackgroundEnabled = !isBackgroundEnabled
        val prefs = getApplication<Application>().getSharedPreferences("focus_cube_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("is_background_enabled", isBackgroundEnabled).apply()
    }

    fun updateWidgetBackgroundType(type: String) {
        widgetBackgroundType = type
        val prefs = getApplication<Application>().getSharedPreferences("focus_cube_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("widget_background_type", type).apply()
    }

    fun updateWidgetPhotoBgUri(uri: String) {
        widgetPhotoBgUri = uri
        val prefs = getApplication<Application>().getSharedPreferences("focus_cube_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("widget_photo_bg_uri", uri).apply()
    }

    fun updateWidgetLottieTheme(theme: String) {
        widgetLottieTheme = theme
        val prefs = getApplication<Application>().getSharedPreferences("focus_cube_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("widget_lottie_theme", theme).apply()
    }

    fun updateWidgetSolidColorHex(hex: String) {
        widgetSolidColorHex = hex
        val prefs = getApplication<Application>().getSharedPreferences("focus_cube_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("widget_solid_color_hex", hex).apply()
    }

    fun toggleWhiteUi() {
        useWhiteUi = !useWhiteUi
        if (useWhiteUi) {
            isDarkMode = false
        }
        val prefs = getApplication<Application>().getSharedPreferences("focus_cube_prefs", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putBoolean("use_white_ui", useWhiteUi)
            putBoolean("is_dark_mode", isDarkMode)
            apply()
        }
    }

    fun toggleLessTransparency() {
        lessTransparency = !lessTransparency
        val prefs = getApplication<Application>().getSharedPreferences("focus_cube_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("less_transparency", lessTransparency).apply()
    }

    fun updateServicePreferences() {
        val prefs = getApplication<Application>().getSharedPreferences("focus_cube_prefs", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putBoolean("is_timer_running_for_service", isTimerRunning)
            putBoolean("is_shield_active_for_service", shieldActive)
            putInt("timer_seconds_left_for_service", timerSecondsLeft)
            putInt("timer_total_minutes_for_service", timerTotalMinutes)
            putString("timer_label_for_service", timerLabel)
            putBoolean("is_dark_mode_for_service", isDarkMode)
            putString("accent_color_hex_for_service", accentColorHex)
            putLong("session_start_time_for_service", sessionStartTime)
            putBoolean("is_timer_paused_for_service", isTimerPaused)
            apply()
        }
        
        viewModelScope.launch {
            blockPreferences.setSessionLock(shieldActive)
        }
        
        val context = getApplication<Application>()
        val serviceIntent = Intent(context, FocusShieldService::class.java)
        
        if (shieldActive) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            try {
                context.stopService(serviceIntent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun toggleDarkMode() {
        isDarkMode = !isDarkMode
        luminaAesthetic = if (isDarkMode) "dark" else "light"
        val prefs = getApplication<Application>().getSharedPreferences("focus_cube_prefs", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putBoolean("is_dark_mode", isDarkMode)
            putString("lumina_aesthetic", luminaAesthetic)
            apply()
        }
    }

    fun toggleWhiteNoise() {
        isWhiteNoiseEnabled = !isWhiteNoiseEnabled
        val prefs = getApplication<Application>().getSharedPreferences("focus_cube_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("is_white_noise_enabled", isWhiteNoiseEnabled).apply()
        
        // If focus session is actively running and not paused, start/stop sound
        if (isTimerRunning && !isTimerPaused) {
            if (isWhiteNoiseEnabled) {
                WhiteNoisePlayer.start(selectedAmbientSound, getApplication())
            } else {
                WhiteNoisePlayer.stop()
            }
        }
    }

    fun toggleWhiteNoisePlayingAnytime() {
        isWhiteNoisePlayingAnytime = !isWhiteNoisePlayingAnytime
        if (isWhiteNoisePlayingAnytime) {
            WhiteNoisePlayer.start(selectedAmbientSound, getApplication())
        } else {
            WhiteNoisePlayer.stop()
        }
    }

    fun selectAmbientSound(soundType: String) {
        selectedAmbientSound = soundType
        WhiteNoisePlayer.setSoundType(soundType)
        val prefs = getApplication<Application>().getSharedPreferences("focus_cube_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("selected_ambient_sound", soundType).apply()
        
        // If playing, update it instantly
        if ((isTimerRunning && !isTimerPaused && isWhiteNoiseEnabled) || isWhiteNoisePlayingAnytime) {
            WhiteNoisePlayer.start(soundType, getApplication())
        }
    }

    fun setComfortChainVolumeValue(volume: Float) {
        comfortChainVolume = volume
        WhiteNoisePlayer.comfortChainVolume = volume
        val prefs = getApplication<Application>().getSharedPreferences("focus_cube_prefs", Context.MODE_PRIVATE)
        prefs.edit().putFloat("comfort_chain_volume", volume).apply()
    }

    fun selectSoundPack(pack: String) {
        selectedSoundPack = pack
        val prefs = getApplication<Application>().getSharedPreferences("focus_cube_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("selected_sound_pack", pack).apply()
        
        // Preview start sound for the selected sound pack
        FocusSoundPlayer.playStartSound(pack, getApplication<Application>())
    }

    // Firebase Properties (wrapped inside try-catch block for extreme robustness)
    private fun ensureFirebaseInitialized() {
        val context = getApplication<Application>()
        if (com.google.firebase.FirebaseApp.getApps(context).isEmpty()) {
            try {
                val apiKey = com.example.BuildConfig.FIREBASE_API_KEY
                val appId = com.example.BuildConfig.FIREBASE_APP_ID
                val projectId = com.example.BuildConfig.FIREBASE_PROJECT_ID
                val senderId = com.example.BuildConfig.FIREBASE_MESSAGING_SENDER_ID
                
                if (apiKey.isNotEmpty() && apiKey != "YOUR_FIREBASE_API_KEY" &&
                    appId.isNotEmpty() && appId != "YOUR_FIREBASE_APP_ID" &&
                    projectId.isNotEmpty() && projectId != "YOUR_FIREBASE_PROJECT_ID") {
                    
                    val options = com.google.firebase.FirebaseOptions.Builder()
                        .setApiKey(apiKey)
                        .setApplicationId(appId)
                        .setProjectId(projectId)
                        .setGcmSenderId(if (senderId.isNotEmpty() && senderId != "YOUR_FIREBASE_MESSAGING_SENDER_ID") senderId else null)
                        .build()
                    com.google.firebase.FirebaseApp.initializeApp(context, options)
                    android.util.Log.d("FocusViewModel", "Firebase programmatically initialized successfully!")
                } else {
                    android.util.Log.w("FocusViewModel", "Firebase environment variables are not configured in Secrets panel.")
                }
            } catch (e: Exception) {
                android.util.Log.e("FocusViewModel", "Error programmatically initializing Firebase", e)
            }
        }
    }

    val mAuth: FirebaseAuth? by lazy {
        try {
            ensureFirebaseInitialized()
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    val mFirestore: FirebaseFirestore? by lazy {
        try {
            ensureFirebaseInitialized()
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    var authState by mutableStateOf<AuthState>(AuthState.Loading)
        private set

    var isSyncing by mutableStateOf(false)

    // Onboarding Completeness check
    
    init {
        ensureFirebaseInitialized()
        val database = FocusDatabase.getDatabase(application)
        repository = FocusRepository(database)

        userStats = repository.userStats.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = null
        )
        allBadges = repository.allBadges.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = emptyList()
        )

        viewModelScope.launch {
            // Initialize basic stats if not present
            if (repository.userStats.first() == null) {
                repository.insertUserStats(UserStats())
            }
            // Initialize badges
            val initialBadges = listOf(
                Badge("streak_3", "3-Day Streak", "Complete a habit for 3 consecutive days.", "🔥"),
                Badge("streak_10", "10-Day Streak", "Complete a habit for 10 consecutive days.", "🌟"),
                Badge("focus_novice", "Focus Novice", "Complete 5 focus sessions.", "⏱️"),
                Badge("master_focus", "Master of Focus", "Accumulate 1000 focus points.", "👑")
            )
            repository.insertBadges(initialBadges)
        }

        habits = repository.allHabits.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        completions = repository.allCompletions.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        sessions = repository.allSessions.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allFocusSessions = repository.allFocusSessions.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        blockedApps = repository.allBlockedApps.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        essentialItems = repository.allEssentialItems.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        journalEntries = repository.allJournalEntries.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        books = repository.allBooks.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        chatMessages = repository.allMessages.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        academicSubjects = repository.allSubjects.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        timetableEntries = repository.allEntries.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        academicTasks = repository.allTasks.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        academicProfile = repository.academicProfile.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

        voiceNotes = repository.allVoiceNotes.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        academicChatThreads = repository.allAcademicChatThreads.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        var messagesCollectJob: Job? = null
        viewModelScope.launch {
            activeAcademicThreadId.collect { threadId ->
                messagesCollectJob?.cancel()
                if (threadId != null) {
                    messagesCollectJob = launch {
                        repository.getMessagesForThread(threadId).collect { msgs ->
                            _activeAcademicMessages.value = msgs
                        }
                    }
                } else {
                    _activeAcademicMessages.value = emptyList()
                }
            }
        }

        whitelistedApps = dataStoreManager.whitelistedApps.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptySet()
        )

        // Sync DataStore whitelistedApps to Room essential_items table so service gets updates instantly
        viewModelScope.launch {
            whitelistedApps.collect { appSet ->
                val current = repository.allEssentialItems.first()
                current.filter { it.type == "App" }.forEach { repository.deleteEssentialItem(it) }
                appSet.forEach { pkg ->
                    repository.insertEssentialItem(EssentialItem(name = pkg, type = "App", identifier = pkg))
                }
            }
        }

        mAuth?.addAuthStateListener { auth ->
            val user = auth.currentUser
            if (user != null) {
                authState = AuthState.Authenticated(user.email ?: "", user.uid)
                syncDataFromFirebase()
            } else {
                authState = AuthState.Unauthenticated
            }
        } ?: run {
            authState = AuthState.Unauthenticated
        }

        viewModelScope.launch {
            // Prepopulate habits & blocked apps on first run
            // repository.prepopulateDefaultHabitsIfEmpty() // Comment out to keep habits and work session completely empty as requested
            repository.prepopulateDefaultAppsIfEmpty()
            
            // Check if onboarding completed before
            val prefs = application.getSharedPreferences("focus_cube_prefs", Context.MODE_PRIVATE)
            val isOnboardingDone = prefs.getBoolean("onboarding_complete", false)
            userRole = prefs.getString("user_role", "STUDENT") ?: "STUDENT"
            isDarkMode = prefs.getBoolean("is_dark_mode", false)
            luminaAesthetic = prefs.getString("lumina_aesthetic", if (isDarkMode) "dark" else "light") ?: "light"
            isDarkMode = (luminaAesthetic == "dark")
            isWhiteNoiseEnabled = prefs.getBoolean("is_white_noise_enabled", false)
            selectedAmbientSound = prefs.getString("selected_ambient_sound", "Brown Noise") ?: "Brown Noise"
            selectedSoundPack = prefs.getString("selected_sound_pack", "Minimalist") ?: "Minimalist"
            WhiteNoisePlayer.setSoundType(selectedAmbientSound)
            comfortChainVolume = prefs.getFloat("comfort_chain_volume", 0.6f)
            WhiteNoisePlayer.comfortChainVolume = comfortChainVolume
            isBackgroundEnabled = prefs.getBoolean("is_background_enabled", true)
            backgroundImageUrl = prefs.getString("background_image_url", "") ?: ""
            isBackgroundAmbientEnabled = prefs.getBoolean("is_background_ambient_enabled", true)
            
            // Load Widget Personalization
            widgetBackgroundType = prefs.getString("widget_background_type", "solid_color") ?: "solid_color"
            widgetPhotoBgUri = prefs.getString("widget_photo_bg_uri", "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=500") ?: "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=500"
            widgetLottieTheme = prefs.getString("widget_lottie_theme", "Aurora Lights") ?: "Aurora Lights"
            widgetSolidColorHex = prefs.getString("widget_solid_color_hex", "#111827") ?: "#111827"
            updateBackgroundAmbientForMode(_currentMode.value)
            useWhiteUi = prefs.getBoolean("use_white_ui", false)
            lessTransparency = prefs.getBoolean("less_transparency", true)
            
            darkGlassBlur = prefs.getFloat("dark_glass_blur", 16f)
            darkGlassOpacity = prefs.getFloat("dark_glass_opacity", 0.85f)
            lightGlassBlur = prefs.getFloat("light_glass_blur", 10f)
            lightGlassOpacity = prefs.getFloat("light_glass_opacity", 0.65f)
            profileName = prefs.getString("profile_name", "") ?: ""
            profileTitle = prefs.getString("profile_title", "Ready to Focus") ?: "Ready to Focus"
            loadSchedulesFromPrefs()
            
            // Listen to SharedPreferences changes from service overlay Emergency Bypass
            prefsListener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPrefs: SharedPreferences?, key: String? ->
                if (key == "is_timer_running_for_service" && sharedPrefs != null) {
                    val running = sharedPrefs.getBoolean(key, isTimerRunning)
                    if (!running && isTimerRunning) {
                        viewModelScope.launch(Dispatchers.Main) {
                            stopTimerEarly()
                        }
                    }
                }
            }
            prefs.registerOnSharedPreferenceChangeListener(prefsListener)
            
            // Sync with foreground service state on app startup
            updateServicePreferences()
            loadWidgetsFromPrefs()
            
            delay(1500) // Beautiful splash screen delay
            if (isOnboardingDone) {
                val user = mAuth?.currentUser
                if (user != null) {
                    val isTimerRunningForService = prefs.getBoolean("is_timer_running_for_service", false)
                    if (isTimerRunningForService) {
                        val savedLabel = prefs.getString("timer_label_for_service", "Focus Block") ?: "Focus Block"
                        val savedTotalMinutes = prefs.getInt("timer_total_minutes_for_service", 25)
                        val savedStartTime = prefs.getLong("session_start_time_for_service", System.currentTimeMillis())
                        val isPaused = prefs.getBoolean("is_timer_paused_for_service", false)
                        
                        // Calculate elapsed time to prevent drift
                        val elapsedSeconds = if (isPaused) {
                            // If paused, we don't increment elapsed time while closed
                            ((System.currentTimeMillis() - savedStartTime) / 1000).toInt() // Fallback
                        } else {
                            ((System.currentTimeMillis() - savedStartTime) / 1000).toInt()
                        }
                        
                        val totalSeconds = savedTotalMinutes * 60
                        val secondsRemaining = totalSeconds - elapsedSeconds
                        
                        if (secondsRemaining > 0) {
                            timerLabel = savedLabel
                            timerTotalMinutes = savedTotalMinutes
                            timerSecondsLeft = secondsRemaining
                            sessionStartTime = savedStartTime
                            isTimerRunning = true
                            isTimerPaused = isPaused
                            timerProgress = timerSecondsLeft.toFloat() / totalSeconds.toFloat()
                            focusSessionState = if (isPaused) FocusSessionState.PAUSED else FocusSessionState.RUNNING
                            shieldActive = true
                            _currentScreen.value = AppScreen.FOCUS_ACTIVE
                            startTimerJob()
                        } else {
                            // Session completed while app process was dead
                            isTimerRunning = false
                            focusSessionState = FocusSessionState.COMPLETED
                            shieldActive = false
                            _currentScreen.value = AppScreen.MAIN
                            
                            viewModelScope.launch {
                                repository.insertFocusSession(
                                    FocusSession(
                                        label = savedLabel,
                                        startTime = savedStartTime,
                                        endTime = savedStartTime + (totalSeconds * 1000),
                                        mode = "Deep Work",
                                        plannedMinutes = savedTotalMinutes,
                                        actualMinutes = savedTotalMinutes,
                                        interruptionCount = 0,
                                        completed = true
                                    )
                                )
                                repository.insertSession(
                                    WorkSession(
                                        label = savedLabel,
                                        durationMinutes = savedTotalMinutes,
                                        focusTimeMinutes = savedTotalMinutes,
                                        timestamp = System.currentTimeMillis(),
                                        category = "Work"
                                    )
                                )
                                syncDataToFirebase()
                            }
                        }
                    } else {
                        _currentScreen.value = AppScreen.MAIN
                    }
                    syncDataFromFirebase()
                    loadWidgetsFromFirestore()
                } else {
                    _currentScreen.value = AppScreen.AUTH
                }
            } else {
                _currentScreen.value = AppScreen.ONBOARDING
            }

            // Load device installed apps
            loadDeviceApps()
        }
    }

    var activeWidgets by mutableStateOf<List<String>>(emptyList())

    fun initializeDefaultWidgetsForRole(role: String) {
        val defaultList = com.example.ui.widget.WidgetRegistry.getWidgetsForRole(role)
        activeWidgets = defaultList
        saveWidgetsToPrefs(defaultList)
    }

    fun saveWidgetsToPrefs(widgets: List<String>) {
        val prefs = getApplication<Application>().getSharedPreferences("focus_cube_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("active_widgets_csv", widgets.joinToString(",")).apply()
        syncWidgetsToFirestore()
    }

    fun loadWidgetsFromPrefs() {
        val prefs = getApplication<Application>().getSharedPreferences("focus_cube_prefs", Context.MODE_PRIVATE)
        val csv = prefs.getString("active_widgets_csv", "")
        if (!csv.isNullOrEmpty()) {
            val list = csv.split(",").toMutableList()
            if (!list.contains("drag_drop_tasks")) {
                list.add(0, "drag_drop_tasks")
            }
            activeWidgets = list
        } else {
            initializeDefaultWidgetsForRole(userRole)
        }
    }

    private fun syncWidgetsToFirestore() {
        val uid = mAuth?.currentUser?.uid ?: return
        val db = mFirestore ?: return
        val map = mapOf("activeWidgets" to activeWidgets)
        db.collection("users").document(uid).collection("data").document("widgets").set(map)
            .addOnFailureListener { e -> e.printStackTrace() }
    }

    fun loadWidgetsFromFirestore() {
        val uid = mAuth?.currentUser?.uid ?: return
        val db = mFirestore ?: return
        db.collection("users").document(uid).collection("data").document("widgets").get()
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {
                    val list = doc.get("activeWidgets") as? List<String>
                    if (list != null) {
                        activeWidgets = list
                        val prefs = getApplication<Application>().getSharedPreferences("focus_cube_prefs", Context.MODE_PRIVATE)
                        prefs.edit().putString("active_widgets_csv", list.joinToString(",")).apply()
                    }
                }
            }
    }

    fun completeOnboarding(role: String) {
        viewModelScope.launch {
            dataStoreManager.completeOnboarding(role)
        }
        val prefs = getApplication<Application>().getSharedPreferences("focus_cube_prefs", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putBoolean("onboarding_complete", true)
            putString("user_role", role)
            apply()
        }
        userRole = role
        initializeDefaultWidgetsForRole(role)
        
        val user = mAuth?.currentUser
        if (user != null) {
            _currentScreen.value = AppScreen.MAIN
            syncDataFromFirebase()
        } else {
            _currentScreen.value = AppScreen.AUTH
        }
    }

    fun replayOnboarding() {
        val prefs = getApplication<Application>().getSharedPreferences("focus_cube_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("onboarding_complete", false).apply()
        _currentScreen.value = AppScreen.ONBOARDING
    }

    fun signUpWithEmail(
        email: String,
        password: String,
        displayName: String,
        titleProfile: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val auth = mAuth ?: run {
            onError("Firebase Authentication is not initialized.")
            return
        }
        
        authState = AuthState.Loading
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = task.result?.user
                    if (user != null) {
                        profileName = displayName
                        profileTitle = titleProfile
                        saveUserProfileToFirestore(user.uid, displayName, titleProfile)
                        authState = AuthState.Authenticated(user.email ?: "", user.uid)
                        _currentScreen.value = AppScreen.MAIN
                        onSuccess()
                    } else {
                        authState = AuthState.Error("Sign up failed: User is null.")
                        onError("User is null.")
                    }
                } else {
                    val errMsg = task.exception?.localizedMessage ?: "Unknown error"
                    authState = AuthState.Error(errMsg)
                    onError(errMsg)
                }
            }
    }

    fun signInWithEmail(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val auth = mAuth ?: run {
            onError("Firebase Authentication is not initialized.")
            return
        }

        authState = AuthState.Loading
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = task.result?.user
                    if (user != null) {
                        authState = AuthState.Authenticated(user.email ?: "", user.uid)
                        _currentScreen.value = AppScreen.MAIN
                        syncDataFromFirebase()
                        onSuccess()
                    } else {
                        authState = AuthState.Error("Sign in failed.")
                        onError("User is null.")
                    }
                } else {
                    val errMsg = task.exception?.localizedMessage ?: "Unknown error"
                    authState = AuthState.Error(errMsg)
                    onError(errMsg)
                }
            }
    }

    fun signInWithGoogle(
        idToken: String,
        deviceId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val auth = mAuth ?: run {
            onError("Firebase Authentication is not initialized.")
            return
        }

        authState = AuthState.Loading
        val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = task.result?.user
                    if (user != null) {
                        profileName = user.displayName ?: "Google User"
                        profileTitle = "Sync Active"
                        saveUserProfileToFirestore(user.uid, profileName, profileTitle, deviceId)
                        authState = AuthState.Authenticated(user.email ?: "", user.uid)
                        _currentScreen.value = AppScreen.MAIN
                        syncDataFromFirebase()
                        onSuccess()
                    } else {
                        authState = AuthState.Error("Google Sign-In failed.")
                        onError("User is null.")
                    }
                } else {
                    val errMsg = task.exception?.localizedMessage ?: "Unknown error"
                    authState = AuthState.Error(errMsg)
                    onError(errMsg)
                }
            }
    }

    fun signOut() {
        mAuth?.signOut()
        authState = AuthState.Unauthenticated
        _currentScreen.value = AppScreen.AUTH
    }

    private fun saveUserProfileToFirestore(uid: String, name: String, title: String, deviceId: String? = null) {
        val db = mFirestore ?: return
        val profileData = hashMapOf(
            "profileName" to name,
            "profileTitle" to title,
            "accentColorHex" to accentColorHex,
            "notificationsEnabled" to notificationsEnabled,
            "shieldActive" to shieldActive,
            "updatedAt" to System.currentTimeMillis()
        )
        if (deviceId != null) {
            profileData["deviceId"] = deviceId
        }
        db.collection("users").document(uid).set(profileData)
            .addOnFailureListener { e -> e.printStackTrace() }
    }

    fun syncDataToFirebase() {
        val uid = mAuth?.currentUser?.uid ?: return
        val db = mFirestore ?: return
        if (isSyncing) return
        isSyncing = true

        viewModelScope.launch {
            try {
                saveUserProfileToFirestore(uid, profileName, profileTitle)

                val currentHabits = repository.allHabits.first()
                val currentCompletions = repository.allCompletions.first()
                val currentSessions = repository.allSessions.first()
                val currentBlockedApps = repository.allBlockedApps.first()
                val currentFocusSessions = repository.allFocusSessions.first()

                val habitsMap = currentHabits.map {
                    mapOf(
                        "name" to it.name,
                        "category" to it.category,
                        "colorHex" to it.colorHex,
                        "isObsidianLinked" to it.isObsidianLinked,
                        "obsidianUri" to it.obsidianUri,
                        "streakCount" to it.streakCount,
                        "lastCompletedDate" to it.lastCompletedDate
                    )
                }

                val completionsMap = currentCompletions.map {
                    mapOf(
                        "habitId" to it.habitId,
                        "date" to it.date
                    )
                }

                val sessionsMap = currentSessions.map {
                    mapOf(
                        "label" to it.label,
                        "durationMinutes" to it.durationMinutes,
                        "focusTimeMinutes" to it.focusTimeMinutes,
                        "timestamp" to it.timestamp,
                        "category" to it.category
                    )
                }

                val focusSessionsMap = currentFocusSessions.map {
                    mapOf(
                        "label" to it.label,
                        "startTime" to it.startTime,
                        "endTime" to it.endTime,
                        "mode" to it.mode,
                        "plannedMinutes" to it.plannedMinutes,
                        "actualMinutes" to it.actualMinutes,
                        "interruptionCount" to it.interruptionCount,
                        "completed" to it.completed
                    )
                }

                val blockedAppsMap = currentBlockedApps.map {
                    mapOf(
                        "packageName" to it.packageName,
                        "appName" to it.appName,
                        "isBlocked" to it.isBlocked,
                        "category" to it.category,
                        "blockMode" to it.blockMode
                    )
                }

                val currentSubjects = repository.allSubjects.first()
                val currentEntries = repository.allEntries.first()
                val currentTasks = repository.allTasks.first()
                val currentProfile = repository.academicProfile.first()

                val subjectsMap = currentSubjects.map {
                    mapOf(
                        "name" to it.name,
                        "instructor" to it.instructor,
                        "room" to it.room,
                        "colorHex" to it.colorHex
                    )
                }

                val entriesMap = currentEntries.map {
                    mapOf(
                        "subjectName" to it.subjectName,
                        "dayOfWeek" to it.dayOfWeek,
                        "weekRotation" to it.weekRotation,
                        "startTime" to it.startTime,
                        "endTime" to it.endTime,
                        "instructor" to it.instructor,
                        "room" to it.room,
                        "colorHex" to it.colorHex
                    )
                }

                val tasksMap = currentTasks.map {
                    mapOf(
                        "title" to it.title,
                        "subjectName" to it.subjectName,
                        "dueDate" to it.dueDate,
                        "priority" to it.priority,
                        "status" to it.status,
                        "notes" to it.notes,
                        "subtasksJson" to it.subtasksJson
                    )
                }

                val profileMap = currentProfile?.let {
                    mapOf(
                        "role" to it.role,
                        "customBgType" to it.customBgType,
                        "customBgValue" to it.customBgValue,
                        "opacity" to it.opacity
                    )
                }

                val syncPayload = hashMapOf(
                    "habits" to habitsMap,
                    "completions" to completionsMap,
                    "sessions" to sessionsMap,
                    "focusSessions" to focusSessionsMap,
                    "blockedApps" to blockedAppsMap,
                    "academicSubjects" to subjectsMap,
                    "timetableEntries" to entriesMap,
                    "academicTasks" to tasksMap,
                    "academicProfile" to profileMap,
                    "lastSyncedAt" to System.currentTimeMillis()
                )

                db.collection("users").document(uid)
                    .collection("data").document("sync")
                    .set(syncPayload)
                    .addOnCompleteListener { isSyncing = false }
            } catch (e: Exception) {
                e.printStackTrace()
                isSyncing = false
            }
        }
    }

    fun syncDataFromFirebase() {
        val uid = mAuth?.currentUser?.uid ?: return
        val db = mFirestore ?: return
        if (isSyncing) return
        isSyncing = true

        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {
                    profileName = doc.getString("profileName") ?: profileName
                    profileTitle = doc.getString("profileTitle") ?: profileTitle
                    accentColorHex = doc.getString("accentColorHex") ?: accentColorHex
                    notificationsEnabled = doc.getBoolean("notificationsEnabled") ?: notificationsEnabled
                    shieldActive = doc.getBoolean("shieldActive") ?: shieldActive
                }
            }

        db.collection("users").document(uid)
            .collection("data").document("sync").get()
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {
                    val habitsList = doc.get("habits") as? List<Map<String, Any>>
                    val completionsList = doc.get("completions") as? List<Map<String, Any>>
                    val sessionsList = doc.get("sessions") as? List<Map<String, Any>>
                    val focusSessionsList = doc.get("focusSessions") as? List<Map<String, Any>>
                    val blockedAppsList = doc.get("blockedApps") as? List<Map<String, Any>>
                    val subjectsList = doc.get("academicSubjects") as? List<Map<String, Any>>
                    val entriesList = doc.get("timetableEntries") as? List<Map<String, Any>>
                    val tasksList = doc.get("academicTasks") as? List<Map<String, Any>>
                    val profileMap = doc.get("academicProfile") as? Map<String, Any>

                    if (habitsList != null || completionsList != null || sessionsList != null || focusSessionsList != null || blockedAppsList != null ||
                        subjectsList != null || entriesList != null || tasksList != null || profileMap != null) {
                        viewModelScope.launch {
                            try {
                                val databaseObj = FocusDatabase.getDatabase(getApplication())
                                withContext(Dispatchers.IO) {
                                    databaseObj.clearAllTables()
                                }

                                if (habitsList != null) {
                                    for (hMap in habitsList) {
                                        val name = hMap["name"] as? String ?: ""
                                        val category = hMap["category"] as? String ?: ""
                                        val colorHex = hMap["colorHex"] as? String ?: ""
                                        val isObsidianLinked = hMap["isObsidianLinked"] as? Boolean ?: false
                                        val obsidianUri = hMap["obsidianUri"] as? String ?: ""
                                        val streakCount = (hMap["streakCount"] as? Long)?.toInt() ?: 0
                                        val lastCompletedDate = hMap["lastCompletedDate"] as? String ?: ""
                                        
                                        repository.insertHabit(
                                            Habit(
                                                name = name,
                                                category = category,
                                                colorHex = colorHex,
                                                isObsidianLinked = isObsidianLinked,
                                                obsidianUri = obsidianUri,
                                                streakCount = streakCount,
                                                lastCompletedDate = lastCompletedDate
                                            )
                                        )
                                    }
                                }

                                if (completionsList != null) {
                                    for (cMap in completionsList) {
                                        val habitId = (cMap["habitId"] as? Long)?.toInt() ?: 0
                                        val date = cMap["date"] as? String ?: ""
                                        databaseObj.habitDao().insertCompletion(
                                            HabitCompletion(habitId = habitId, date = date)
                                        )
                                    }
                                }

                                if (sessionsList != null) {
                                    for (sMap in sessionsList) {
                                        val label = sMap["label"] as? String ?: ""
                                        val durationMinutes = (sMap["durationMinutes"] as? Long)?.toInt() ?: 0
                                        val focusTimeMinutes = (sMap["focusTimeMinutes"] as? Long)?.toInt() ?: 0
                                        val timestamp = sMap["timestamp"] as? Long ?: System.currentTimeMillis()
                                        val category = sMap["category"] as? String ?: "Work"
                                        repository.insertSession(
                                            WorkSession(
                                                label = label,
                                                durationMinutes = durationMinutes,
                                                focusTimeMinutes = focusTimeMinutes,
                                                timestamp = timestamp,
                                                category = category
                                            )
                                        )
                                    }
                                }

                                if (focusSessionsList != null) {
                                    for (fsMap in focusSessionsList) {
                                        val label = fsMap["label"] as? String ?: ""
                                        val startTime = fsMap["startTime"] as? Long ?: 0L
                                        val endTime = fsMap["endTime"] as? Long ?: 0L
                                        val mode = fsMap["mode"] as? String ?: "Deep Work"
                                        val plannedMinutes = (fsMap["plannedMinutes"] as? Long)?.toInt() ?: 0
                                        val actualMinutes = (fsMap["actualMinutes"] as? Long)?.toInt() ?: 0
                                        val interruptionCount = (fsMap["interruptionCount"] as? Long)?.toInt() ?: 0
                                        val completed = fsMap["completed"] as? Boolean ?: false
                                        repository.insertFocusSession(
                                            FocusSession(
                                                label = label,
                                                startTime = startTime,
                                                endTime = endTime,
                                                mode = mode,
                                                plannedMinutes = plannedMinutes,
                                                actualMinutes = actualMinutes,
                                                interruptionCount = interruptionCount,
                                                completed = completed
                                            )
                                        )
                                    }
                                }

                                if (blockedAppsList != null) {
                                    for (bMap in blockedAppsList) {
                                        val packageName = bMap["packageName"] as? String ?: ""
                                        val appName = bMap["appName"] as? String ?: ""
                                        val isBlocked = bMap["isBlocked"] as? Boolean ?: false
                                        val category = bMap["category"] as? String ?: ""
                                        val blockMode = bMap["blockMode"] as? String ?: "Session only"
                                        repository.insertBlockedApp(
                                            BlockedApp(
                                                packageName = packageName,
                                                appName = appName,
                                                isBlocked = isBlocked,
                                                category = category,
                                                blockMode = blockMode
                                            )
                                        )
                                    }
                                }

                                if (subjectsList != null) {
                                    for (sMap in subjectsList) {
                                        val name = sMap["name"] as? String ?: ""
                                        val instructor = sMap["instructor"] as? String ?: ""
                                        val room = sMap["room"] as? String ?: ""
                                        val colorHex = sMap["colorHex"] as? String ?: "#3B82F6"
                                        repository.insertSubject(
                                            AcademicSubject(
                                                name = name,
                                                instructor = instructor,
                                                room = room,
                                                colorHex = colorHex
                                            )
                                        )
                                    }
                                }

                                if (entriesList != null) {
                                    for (eMap in entriesList) {
                                        val subjectName = eMap["subjectName"] as? String ?: ""
                                        val dayOfWeek = (eMap["dayOfWeek"] as? Long)?.toInt() ?: 1
                                        val weekRotation = eMap["weekRotation"] as? String ?: "All"
                                        val startTime = eMap["startTime"] as? String ?: "09:00"
                                        val endTime = eMap["endTime"] as? String ?: "10:00"
                                        val instructor = eMap["instructor"] as? String ?: ""
                                        val room = eMap["room"] as? String ?: ""
                                        val colorHex = eMap["colorHex"] as? String ?: "#3B82F6"
                                        repository.insertEntry(
                                            TimetableEntry(
                                                subjectName = subjectName,
                                                dayOfWeek = dayOfWeek,
                                                weekRotation = weekRotation,
                                                startTime = startTime,
                                                endTime = endTime,
                                                instructor = instructor,
                                                room = room,
                                                colorHex = colorHex
                                            )
                                        )
                                    }
                                }

                                if (tasksList != null) {
                                    for (tMap in tasksList) {
                                        val title = tMap["title"] as? String ?: ""
                                        val subjectName = tMap["subjectName"] as? String ?: ""
                                        val dueDate = tMap["dueDate"] as? Long ?: System.currentTimeMillis()
                                        val priority = tMap["priority"] as? String ?: "Medium"
                                        val status = tMap["status"] as? String ?: "Pending"
                                        val notes = tMap["notes"] as? String ?: ""
                                        val subtasksJson = tMap["subtasksJson"] as? String ?: "[]"
                                        repository.insertTask(
                                            AcademicTask(
                                                title = title,
                                                subjectName = subjectName,
                                                dueDate = dueDate,
                                                priority = priority,
                                                status = status,
                                                notes = notes,
                                                subtasksJson = subtasksJson
                                            )
                                        )
                                    }
                                }

                                if (profileMap != null) {
                                    val role = profileMap["role"] as? String ?: "Student"
                                    val customBgType = profileMap["customBgType"] as? String ?: "Color"
                                    val customBgValue = profileMap["customBgValue"] as? String ?: "#0B0F19"
                                    val opacity = (profileMap["opacity"] as? Double)?.toFloat() ?: 1.0f
                                    repository.insertProfile(
                                        AcademicProfile(
                                            role = role,
                                            customBgType = customBgType,
                                            customBgValue = customBgValue,
                                            opacity = opacity
                                        )
                                    )
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            } finally {
                                isSyncing = false
                            }
                        }
                    } else {
                        isSyncing = false
                    }
                } else {
                    isSyncing = false
                }
            }
            .addOnFailureListener {
                isSyncing = false
            }
    }

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun setTab(tab: AppTab) {
        _currentTab.value = tab
    }

    // Habits Logic
    fun addHabit(name: String, category: String, colorHex: String, obsidianUri: String = "", targetValue: Int = 1, unit: String = "times") {
        viewModelScope.launch {
            val insertedId = repository.insertHabit(
                Habit(
                    name = name,
                    category = category,
                    colorHex = colorHex,
                    isObsidianLinked = false,
                    obsidianUri = "",
                    targetValue = targetValue,
                    unit = unit
                )
            )
            if (notificationsEnabled) {
                // Schedule reminder at 9:00 AM by default
                FocusNotificationManager.scheduleHabitReminder(
                    getApplication(),
                    insertedId.toInt(),
                    name,
                    9,
                    0
                )
            }
            syncDataToFirebase()
        }
    }

    fun updateHabitProgress(habit: Habit, date: String, newValue: Int) {
        viewModelScope.launch {
            val target = habit.targetValue
            val previousValue = completions.value.find { it.habitId == habit.id && it.date == date }?.value ?: 0
            repository.updateHabitProgress(habit, date, newValue)
            if (newValue >= target && previousValue < target) {
                triggerHapticPattern("SUCCESS")
                triggerConfettiBurst()
            }
            syncDataToFirebase()
        }
    }

    fun toggleHabitCompletion(habit: Habit, date: String) {
        viewModelScope.launch {
            val isCompleted = completions.value.any { it.habitId == habit.id && it.date == date }
            if (isCompleted) {
                repository.uncompleteHabit(habit, date)
            } else {
                repository.completeHabit(habit, date)
                triggerHapticPattern("SUCCESS")
                triggerConfettiBurst()
                addPoints(50)
                // Check if streak increased
                val newStreak = habit.streakCount + 1
                checkStreakBadges(newStreak)
            }
            syncDataToFirebase()
        }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            repository.deleteHabit(habit)
            FocusNotificationManager.cancelHabitReminder(getApplication(), habit.id)
            syncDataToFirebase()
        }
    }

    var currentTimetableWeekNumber by mutableStateOf<Int?>(null)

    val timetableWeeks: StateFlow<List<com.focuscube.data.entity.TimetableHabit>> = com.focuscube.data.local.FocusCubeDatabase.getDatabase(application).timetableDao().getAllWeeks()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Timer Logic
    fun startFocusSession(label: String, minutes: Int, mode: String = "Deep Work", weekNumber: Int? = null) {
        timerLabel = label
        timerTotalMinutes = minutes
        timerSecondsLeft = minutes * 60
        isTimerRunning = true
        isTimerPaused = false
        timerProgress = 1f
        sessionStartTime = System.currentTimeMillis()
        focusSessionState = FocusSessionState.RUNNING
        focusSessionMode = mode
        sessionInterruptionCount = 0
        shieldActive = true // Automatically engage Focus Shield!
        currentTimetableWeekNumber = weekNumber
        
        _currentScreen.value = AppScreen.FOCUS_ACTIVE
        if (isWhiteNoiseEnabled) {
            WhiteNoisePlayer.start(selectedAmbientSound, getApplication())
        }
        FocusSoundPlayer.playStartSound(selectedSoundPack, getApplication())
        triggerHapticPattern("START")
        updateServicePreferences()
        startTimerJob()
    }

    private fun startTimerJob() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (timerSecondsLeft > 0 && isTimerRunning) {
                delay(1000)
                if (!isTimerPaused) {
                    timerSecondsLeft--
                    timerProgress = timerSecondsLeft.toFloat() / (timerTotalMinutes * 60).toFloat()
                    
                    // Keep SharedPreferences in sync for foreground service overlay
                    val prefs = getApplication<Application>().getSharedPreferences("focus_cube_prefs", Context.MODE_PRIVATE)
                    prefs.edit().putInt("timer_seconds_left_for_service", timerSecondsLeft).apply()
                }
            }
            if (timerSecondsLeft == 0) {
                finishFocusSession()
            }
        }
    }

    fun pauseTimer() {
        isTimerPaused = true
        focusSessionState = FocusSessionState.PAUSED
        WhiteNoisePlayer.stop()
        FocusSoundPlayer.playPauseSound(selectedSoundPack, getApplication())
        updateServicePreferences()
    }

    fun resumeTimer() {
        isTimerPaused = false
        focusSessionState = FocusSessionState.RUNNING
        if (isWhiteNoiseEnabled) {
            WhiteNoisePlayer.start(selectedAmbientSound, getApplication())
        }
        FocusSoundPlayer.playStartSound(selectedSoundPack, getApplication())
        updateServicePreferences()
    }

    fun addFiveMinutes() {
        timerTotalMinutes += 5
        timerSecondsLeft += 5 * 60
        timerProgress = timerSecondsLeft.toFloat() / (timerTotalMinutes * 60).toFloat()
    }

    fun stopTimerEarly() {
        timerJob?.cancel()
        isTimerRunning = false
        focusSessionState = FocusSessionState.INTERRUPTED
        shieldActive = false // Cleanly disengage Focus Shield!
        WhiteNoisePlayer.stop()
        triggerHapticPattern("STOP")
        updateServicePreferences()
        _currentScreen.value = AppScreen.MAIN
        
        val elapsedMinutes = timerTotalMinutes - (timerSecondsLeft / 60)
        viewModelScope.launch {
            repository.insertFocusSession(
                FocusSession(
                    label = timerLabel,
                    startTime = sessionStartTime,
                    endTime = System.currentTimeMillis(),
                    mode = focusSessionMode,
                    plannedMinutes = timerTotalMinutes,
                    actualMinutes = elapsedMinutes,
                    interruptionCount = sessionInterruptionCount,
                    completed = false
                )
            )
            syncDataToFirebase()
        }
    }

    private fun finishFocusSession() {
        isTimerRunning = false
        focusSessionState = FocusSessionState.COMPLETED
        shieldActive = false // Cleanly disengage Focus Shield!
        WhiteNoisePlayer.stop()
        FocusSoundPlayer.playCompleteSound(selectedSoundPack, getApplication())
        triggerHapticPattern("SUCCESS")
        updateServicePreferences()
        viewModelScope.launch {
            // Record session in database
            val elapsedMinutes = timerTotalMinutes
            repository.insertFocusSession(
                FocusSession(
                    label = timerLabel,
                    startTime = sessionStartTime,
                    endTime = System.currentTimeMillis(),
                    mode = focusSessionMode,
                    plannedMinutes = timerTotalMinutes,
                    actualMinutes = elapsedMinutes,
                    interruptionCount = sessionInterruptionCount,
                    completed = true
                )
            )
            currentTimetableWeekNumber?.let { weekNum ->
                val dao = com.focuscube.data.local.FocusCubeDatabase.getDatabase(getApplication()).timetableDao()
                dao.addMinutes(weekNum, elapsedMinutes)
                currentTimetableWeekNumber = null
            }
            addPoints(elapsedMinutes * 10)
            repository.insertSession(
                WorkSession(
                    label = timerLabel,
                    durationMinutes = timerTotalMinutes,
                    focusTimeMinutes = elapsedMinutes,
                    timestamp = System.currentTimeMillis(),
                    category = "Work"
                )
            )
            syncDataToFirebase()
            if (notificationsEnabled) {
                FocusNotificationManager.showTimerCompletedNotification(
                    getApplication(),
                    timerLabel,
                    timerTotalMinutes
                )
            }
            _currentScreen.value = AppScreen.FOCUS_COMPLETE
        }
    }

    fun completeAndSaveSession() {
        _currentScreen.value = AppScreen.MAIN
        _currentTab.value = AppTab.HOME
    }

    // Blocked Apps Logic
    fun toggleAppBlocked(app: BlockedApp) {
        viewModelScope.launch {
            repository.updateBlockedApp(app.copy(isBlocked = !app.isBlocked))
            syncDataToFirebase()
        }
    }

    fun updateAppBlockMode(app: BlockedApp, mode: String) {
        viewModelScope.launch {
            repository.updateBlockedApp(app.copy(blockMode = mode))
            syncDataToFirebase()
        }
    }

    fun updateBlockedAppDetails(app: BlockedApp, isBlocked: Boolean, blockMode: String, category: String) {
        viewModelScope.launch {
            repository.updateBlockedApp(app.copy(isBlocked = isBlocked, blockMode = blockMode, category = category))
            syncDataToFirebase()
        }
    }

    fun deleteBlockedApp(app: BlockedApp) {
        viewModelScope.launch {
            repository.deleteBlockedApp(app)
            syncDataToFirebase()
        }
    }

    fun addCustomBlockedApp(packageName: String, appName: String, category: String) {
        viewModelScope.launch {
            val existing = repository.getBlockedAppsByPackageName(packageName)
            if (existing.isNotEmpty()) {
                existing.forEach { app ->
                    repository.updateBlockedApp(app.copy(isBlocked = true, category = category.uppercase()))
                }
            } else {
                repository.insertBlockedApp(
                    BlockedApp(
                        packageName = packageName,
                        appName = appName,
                        isBlocked = true,
                        category = category.uppercase(),
                        blockMode = "Session only"
                    )
                )
            }
            syncDataToFirebase()
        }
    }

    // Helper to query actual device applications
    private fun loadDeviceApps() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val app = getApplication<Application>()
                val filtered = FocusShieldService.getDiscoverableApps(app)
                _deviceApps.value = filtered
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getTodayDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    fun getStreakCount(): Int {
        val currentHabits = habits.value
        if (currentHabits.isEmpty()) return 0
        return currentHabits.maxOfOrNull { it.streakCount } ?: 0
    }

    fun getFocusTimeHours(): Double {
        val totalSecs = sessions.value.sumOf { it.focusTimeMinutes }
        if (totalSecs == 0) return 0.0
        return totalSecs.toDouble() / 60.0
    }

    fun getBlockedAppsCount(): Int {
        return blockedApps.value.count { it.isBlocked }
    }

    fun addEssentialItem(name: String, type: String, identifier: String) {
        viewModelScope.launch {
            repository.insertEssentialItem(
                EssentialItem(
                    name = name,
                    type = type,
                    identifier = identifier
                )
            )
        }
    }

    fun deleteEssentialItem(item: EssentialItem) {
        viewModelScope.launch {
            repository.deleteEssentialItem(item)
        }
    }

    fun resetData() {
        viewModelScope.launch {
            // Delete all habits, sessions, blocked apps and reseed
            val db = FocusDatabase.getDatabase(getApplication())
            withContext(Dispatchers.IO) {
                db.clearAllTables()
            }
            // Do not reseed habits to keep the workspace entirely new as requested
            repository.prepopulateDefaultAppsIfEmpty()
            scheduledWorkList = emptyList()
            saveSchedulesToPrefs()
        }
    }

    // --- Book tracking methods ---
    fun selectBookForReading(book: ReadingBook?) {
        activeReadingBook = book
    }

    fun startReadingSession(minutes: Int) {
        readingTimerSecondsLeft = minutes * 60
        isReadingTimerRunning = true
        isTimerRunning = true
        
        // Save focus shield status to SharedPrefs for service
        val prefs = getApplication<Application>().getSharedPreferences("focus_cube_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("is_timer_running_for_service", true).apply()
        
        // Start FocusShieldService
        val serviceIntent = Intent(getApplication(), FocusShieldService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getApplication<Application>().startForegroundService(serviceIntent)
        } else {
            getApplication<Application>().startService(serviceIntent)
        }

        readingTimerJob?.cancel()
        readingTimerJob = viewModelScope.launch {
            while (readingTimerSecondsLeft > 0 && isReadingTimerRunning) {
                delay(1000)
                readingTimerSecondsLeft--
            }
            if (readingTimerSecondsLeft <= 0) {
                completeReadingSession()
            }
        }
    }

    fun pauseReadingSession() {
        isReadingTimerRunning = false
        readingTimerJob?.cancel()
    }

    fun stopReadingSession() {
        isReadingTimerRunning = false
        readingTimerJob?.cancel()
        val prefs = getApplication<Application>().getSharedPreferences("focus_cube_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("is_timer_running_for_service", false).apply()
        val serviceIntent = Intent(getApplication(), FocusShieldService::class.java)
        getApplication<Application>().stopService(serviceIntent)
    }

    fun completeReadingSession() {
        isReadingTimerRunning = false
        readingTimerJob?.cancel()
        val prefs = getApplication<Application>().getSharedPreferences("focus_cube_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("is_timer_running_for_service", false).apply()
        val serviceIntent = Intent(getApplication(), FocusShieldService::class.java)
        getApplication<Application>().stopService(serviceIntent)

        val durationMins = 25
        viewModelScope.launch {
            val label = activeReadingBook?.title ?: "Reading Session"
            repository.insertSession(
                WorkSession(
                    label = label,
                    durationMinutes = durationMins,
                    focusTimeMinutes = durationMins,
                    timestamp = System.currentTimeMillis(),
                    category = "Reading"
                )
            )
            // Trigger Post-Session Reflection bottom sheet
            showReflectionTrigger = true
            reflectionSessionMins = durationMins
            reflectionSessionCategory = "Reading"
        }
    }

    fun addBook(title: String, author: String, pages: Int) {
        viewModelScope.launch {
            repository.insertBook(
                ReadingBook(
                    title = title,
                    author = author,
                    totalPages = pages,
                    currentPage = 0
                )
            )
        }
    }

    fun updateBookProgress(book: ReadingBook, newPage: Int) {
        viewModelScope.launch {
            val isFinished = newPage >= book.totalPages
            repository.insertBook(
                book.copy(
                    currentPage = newPage.coerceAtMost(book.totalPages),
                    isFinished = isFinished,
                    lastReadTimestamp = System.currentTimeMillis()
                )
            )
        }
    }

    fun deleteBook(book: ReadingBook) {
        viewModelScope.launch {
            repository.deleteBook(book)
        }
    }

    // --- Journal methods ---
    fun addJournalEntry(mood: Int, gratitudePrompt: String, textEntry: String) {
        viewModelScope.launch {
            repository.insertJournalEntry(
                JournalEntry(
                    mood = mood,
                    gratitudePrompt = gratitudePrompt,
                    textEntry = textEntry,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    fun deleteJournalEntry(entry: JournalEntry) {
        viewModelScope.launch {
            repository.deleteJournalEntry(entry)
        }
    }

    fun saveReflectionToJournal(mood: Int, distractedBy: String, feedback: String) {
        viewModelScope.launch {
            repository.insertJournalEntry(
                JournalEntry(
                    mood = mood,
                    gratitudePrompt = "Post-Session Reflection",
                    textEntry = "Distracted by: $distractedBy. Reflection: $feedback",
                    timestamp = System.currentTimeMillis(),
                    isSessionReflection = true,
                    focusMinutes = reflectionSessionMins,
                    focusCategory = reflectionSessionCategory
                )
            )
            showReflectionTrigger = false
        }
    }

    // --- Private Coach Chat methods ---
    fun sendMessageToCoach(userMessage: String) {
        viewModelScope.launch {
            // Insert user chat message
            repository.insertMessage(
                ChatMessage(role = "user", message = userMessage)
            )
            isCoachLoading = true
            try {
                val response = chatRepo.generateCoachResponse(
                    userMessage = userMessage,
                    chatHistory = chatMessages.value,
                    streak = getStreakCount(),
                    lastSessionMinutes = sessions.value.firstOrNull()?.focusTimeMinutes ?: 0,
                    blockedAppsCount = getBlockedAppsCount(),
                    commandExecutor = commandExecutor
                )
                repository.insertMessage(
                    ChatMessage(role = "model", message = response)
                )
            } catch (e: Exception) {
                repository.insertMessage(
                    ChatMessage(role = "model", message = "I encountered an issue connecting. Let's keep focusing!")
                )
            } finally {
                isCoachLoading = false
            }
        }
    }

    fun clearChatHistory() {
        viewModelScope.launch {
            repository.clearAllMessages()
        }
    }

    // --- Academic Wrapper Methods ---
    fun addSubject(name: String, instructor: String = "", room: String = "", colorHex: String = "#3B82F6") {
        viewModelScope.launch {
            repository.insertSubject(AcademicSubject(name = name, instructor = instructor, room = room, colorHex = colorHex))
            syncDataToFirebase()
        }
    }

    fun deleteSubject(subject: AcademicSubject) {
        viewModelScope.launch {
            repository.deleteSubject(subject)
            syncDataToFirebase()
        }
    }

    fun addTimetableEntry(subjectName: String, dayOfWeek: Int, weekRotation: String, startTime: String, endTime: String, instructor: String = "", room: String = "", colorHex: String = "#3B82F6") {
        viewModelScope.launch {
            repository.insertEntry(
                TimetableEntry(
                    subjectName = subjectName,
                    dayOfWeek = dayOfWeek,
                    weekRotation = weekRotation,
                    startTime = startTime,
                    endTime = endTime,
                    instructor = instructor,
                    room = room,
                    colorHex = colorHex
                )
            )
            syncDataToFirebase()
        }
    }

    fun updateTimetableEntry(entry: TimetableEntry) {
        viewModelScope.launch {
            repository.updateEntry(entry)
            syncDataToFirebase()
        }
    }

    fun deleteTimetableEntry(entry: TimetableEntry) {
        viewModelScope.launch {
            repository.deleteEntry(entry)
            syncDataToFirebase()
        }
    }

    fun addAcademicTask(title: String, subjectName: String, dueDate: Long, priority: String, notes: String = "", subtasksJson: String = "[]") {
        viewModelScope.launch {
            val currentMaxSort = academicTasks.value.maxOfOrNull { it.sortOrder } ?: 0
            repository.insertTask(
                AcademicTask(
                    title = title,
                    subjectName = subjectName,
                    dueDate = dueDate,
                    priority = priority,
                    status = "Pending",
                    notes = notes,
                    subtasksJson = subtasksJson,
                    sortOrder = currentMaxSort + 1
                )
            )
            syncDataToFirebase()
        }
    }

    fun getFocusSessionsByDate(startOfDay: Long, endOfDay: Long): kotlinx.coroutines.flow.Flow<List<FocusSession>> {
        return repository.getFocusSessionsByDate(startOfDay, endOfDay)
    }

    fun getDailyInterruptionCount(startOfDay: Long, endOfDay: Long): kotlinx.coroutines.flow.Flow<Int?> {
        return repository.getDailyInterruptionCount(startOfDay, endOfDay)
    }

    fun updateAcademicTask(task: AcademicTask) {
        viewModelScope.launch {
            if (task.status.equals("Completed", ignoreCase = true)) {
                triggerHapticPattern("SUCCESS")
                triggerConfettiBurst()
            }
            repository.updateTask(task)
            syncDataToFirebase()
        }
    }

    fun deleteAcademicTask(task: AcademicTask) {
        viewModelScope.launch {
            repository.deleteTask(task)
            syncDataToFirebase()
        }
    }

    fun updateAcademicProfile(role: String, customBgType: String, customBgValue: String, opacity: Float) {
        viewModelScope.launch {
            repository.insertProfile(
                AcademicProfile(
                    role = role,
                    customBgType = customBgType,
                    customBgValue = customBgValue,
                    opacity = opacity
                )
            )
            syncDataToFirebase()
        }
    }

    fun updateAcademicTasksOrder(reorderedTasks: List<AcademicTask>) {
        viewModelScope.launch {
            reorderedTasks.forEachIndexed { index, task ->
                repository.updateTask(task.copy(sortOrder = index))
            }
            syncDataToFirebase()
        }
    }

    fun addVoiceNote(title: String, filePath: String, durationSec: Int) {
        viewModelScope.launch {
            repository.insertVoiceNote(
                VoiceNote(
                    title = title,
                    filePath = filePath,
                    durationSec = durationSec
                )
            )
        }
    }

    fun deleteVoiceNote(note: VoiceNote) {
        viewModelScope.launch {
            repository.deleteVoiceNote(note)
            try {
                val file = java.io.File(note.filePath)
                if (file.exists()) {
                    file.delete()
                }
            } catch (e: Exception) {}
        }
    }

    // --- Academic AI Assistant Methods ---
    fun createAcademicChatThread(title: String) {
        viewModelScope.launch {
            val newId = "thread_" + java.util.UUID.randomUUID().toString().take(8)
            val thread = AcademicChatThread(id = newId, title = title)
            repository.insertAcademicChatThread(thread)
            activeAcademicThreadId.value = newId
        }
    }

    fun selectAcademicChatThread(threadId: String) {
        activeAcademicThreadId.value = threadId
    }

    fun deleteAcademicChatThread(threadId: String) {
        viewModelScope.launch {
            repository.deleteAcademicChatThread(threadId)
            if (activeAcademicThreadId.value == threadId) {
                activeAcademicThreadId.value = null
            }
        }
    }

    fun sendAcademicChatMessage(messageText: String, imageUri: String? = null, onTtsRequired: (String) -> Unit = {}) {
        val threadId = activeAcademicThreadId.value ?: return
        viewModelScope.launch {
            // 1. Save user's message locally
            val userMsg = AcademicChatMessage(
                threadId = threadId,
                role = "user",
                message = messageText,
                imageUri = imageUri
            )
            repository.insertAcademicChatMessage(userMsg)

            // 2. Set loading state
            isAcademicChatLoading = true

            try {
                // 3. Build system prompt with schedule + homework context
                val currentRole = academicProfile.value?.role ?: "Student"
                val timetableContext = timetableEntries.value.joinToString("\n") {
                    val dayStr = when(it.dayOfWeek) {
                        1 -> "Monday"; 2 -> "Tuesday"; 3 -> "Wednesday"; 4 -> "Thursday"; 5 -> "Friday"; 6 -> "Saturday"; 7 -> "Sunday"
                        else -> "Unknown"
                    }
                    "- ${it.subjectName} on $dayStr at ${it.startTime}-${it.endTime} (Instructor: ${it.instructor}, Room: ${it.room})"
                }
                val tasksContext = academicTasks.value.joinToString("\n") {
                    val formattedDate = try {
                        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
                        sdf.format(java.util.Date(it.dueDate))
                    } catch (e: Exception) { "Unknown" }
                    "- Task: ${it.title} | Subject: ${it.subjectName} | Status: ${it.status} | Priority: ${it.priority} | Due: $formattedDate"
                }

                val systemPrompt = """
                    You are the Academic AI Assistant for the user's smart scheduling portal.
                    
                    User Category/Role: $currentRole
                    
                    Current Schedule/Timetable:
                    $timetableContext
                    
                    Current Tasks/Homework:
                    $tasksContext
                    
                    INSTRUCTIONS:
                    - You are helper-focused. Offer step-by-step reasoning rather than just short final answers.
                    - Be extremely context-aware. If the user asks what classes they have or what homework is due, consult the schedule and tasks provided above.
                    - Action proposals: If the user requests to create, delete, or reschedule a class or task, DO NOT apply it silently. Instead, provide a structured proposal tag at the end of your text message (always use a single separate line):
                      [PROPOSAL_ADD_TASK: title="Task Title", subject="SubjectName", dueInDays=2, priority="Medium", notes="Auto-suggested notes"]
                      or [PROPOSAL_ADD_CLASS: subject="SubjectName", day=1, start="HH:mm", end="HH:mm", instructor="Instructor Name", room="Room Code"]
                      This will allow the user to approve the change inside the chat dialog before it is executed.
                    - If you suggest adding a task, make sure 'subject' matches one of the known subjects or 'General'.
                    - Keep your responses structured, clear, and encouraging.
                """.trimIndent()

                // 4. Load base64 image if uri is provided
                val base64Image = getBase64FromUri(imageUri)

                // 5. Query Gemini API
                val currentHistory = activeAcademicMessages.value
                val aiResponse = chatRepo.generateAcademicResponse(
                    userMessage = messageText,
                    chatHistory = currentHistory,
                    systemPrompt = systemPrompt,
                    imageBytesBase64 = base64Image,
                    commandExecutor = commandExecutor
                )

                // 6. Save AI's response locally with high-fidelity streaming/typewriter effect
                val placeholderMsg = AcademicChatMessage(
                    threadId = threadId,
                    role = "model",
                    message = ""
                )
                val messageId = repository.insertAcademicChatMessage(placeholderMsg)

                // Split response and stream it progressively
                val words = aiResponse.split(" ")
                val sb = java.lang.StringBuilder()
                for (i in words.indices) {
                    sb.append(words[i])
                    if (i < words.size - 1) sb.append(" ")
                    repository.updateAcademicChatMessage(messageId.toInt(), sb.toString())
                    kotlinx.coroutines.delay(20) // silky smooth progressive emission
                }

                // 7. Trigger TextToSpeech callback if TTS is active
                onTtsRequired(aiResponse)

            } catch (e: Exception) {
                repository.insertAcademicChatMessage(
                    AcademicChatMessage(
                        threadId = threadId,
                        role = "model",
                        message = "I encountered an error connecting to my knowledge base: ${e.localizedMessage}. Please verify your connection."
                    )
                )
            } finally {
                isAcademicChatLoading = false
            }
        }
    }

    private fun getBase64FromUri(uriString: String?): String? {
        if (uriString.isNullOrBlank()) return null
        return try {
            val contentResolver = getApplication<android.app.Application>().contentResolver
            val uri = android.net.Uri.parse(uriString)
            val inputStream = contentResolver.openInputStream(uri)
            val bytes = inputStream?.readBytes()
            inputStream?.close()
            if (bytes != null) {
                android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun parseScheduleImageWithGemini(imageBytesBase64: String): String {
        return chatRepo.parseScheduleImageWithGemini(imageBytesBase64)
    }

    fun analyzeImageWithGemini(
        prompt: String,
        imageUriStr: String,
        onResult: (String) -> Unit
    ) {
        viewModelScope.launch {
            val base64 = getBase64FromUri(imageUriStr) ?: ""
            if (base64.isEmpty()) {
                onResult("Unable to load or read selected image file.")
                return@launch
            }
            val result = chatRepo.analyzeImageWithGemini(prompt, base64)
            onResult(result)
        }
    }

    fun summarizeTextFast(text: String, onResult: (String) -> Unit) {
        viewModelScope.launch {
            val result = chatRepo.summarizeTextFast(text)
            onResult(result)
        }
    }

    fun analyzeContentGeneral(content: String, prompt: String, onResult: (String) -> Unit) {
        viewModelScope.launch {
            val result = chatRepo.analyzeContentGeneral(content, prompt)
            onResult(result)
        }
    }

    fun addPoints(points: Int) {
        viewModelScope.launch {
            val stats = repository.userStats.first() ?: UserStats()
            val newPoints = stats.points + points
            val newLevel = (newPoints / 100) + 1
            repository.updateUserStats(stats.copy(points = newPoints, level = newLevel))
            checkBadges(newPoints)
        }
    }

    private suspend fun checkBadges(currentPoints: Int) {
        val badges = repository.allBadges.first()
        if (currentPoints >= 1000) {
            val masterBadge = badges.find { it.id == "master_focus" }
            if (masterBadge?.unlocked == false) {
                repository.updateBadge(masterBadge.copy(unlocked = true, unlockedTimestamp = System.currentTimeMillis()))
            }
        }
    }

    fun checkStreakBadges(streak: Int) {
        viewModelScope.launch {
            val badges = repository.allBadges.first()
            if (streak >= 3) {
                val badge3 = badges.find { it.id == "streak_3" }
                if (badge3?.unlocked == false) {
                    repository.updateBadge(badge3.copy(unlocked = true, unlockedTimestamp = System.currentTimeMillis()))
                }
            }
            if (streak >= 10) {
                val badge10 = badges.find { it.id == "streak_10" }
                if (badge10?.unlocked == false) {
                    repository.updateBadge(badge10.copy(unlocked = true, unlockedTimestamp = System.currentTimeMillis()))
                }
            }
        }
    }
}
data class DeviceAppInfo(
    val packageName: String,
    val appName: String
)
