package com.example.ui.screens

import java.text.SimpleDateFormat
import java.util.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.animation.*
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.ui.viewinterop.AndroidView
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.testTag
import com.example.data.AppTab
import com.example.data.FocusViewModel
import com.example.ui.components.*
import com.example.data.AppMode
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Book
import android.media.MediaRecorder
import android.media.MediaPlayer
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlin.math.cos
import kotlin.math.sin
import com.example.data.VoiceNote
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.SmartToy
import com.example.ui.widget.WidgetRenderer
import com.example.ui.widget.WidgetRegistry
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.animation.core.animateDpAsState
import kotlinx.coroutines.CoroutineScope
import com.example.ui.components.GlassSurface

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun HomeDashboardScreen(viewModel: FocusViewModel) {
    DashboardErrorBoundary(viewModel = viewModel) {
        val currentMode by viewModel.currentMode.collectAsState()
        var showChatSheet by remember { mutableStateOf(false) }
        var showNotesSheet by remember { mutableStateOf(false) }
        var showWidgetCustomizer by remember { mutableStateOf(false) }
        var showQuickVoiceDialog by remember { mutableStateOf(false) }

    // Subtle screen-wide color tint transition to reflect each state's mood
    val isDark = viewModel.isDarkMode
    val targetTint = when (currentMode) {
        AppMode.FOCUS -> if (isDark) Color(0x19A855F7) else Color(0x0FA855F7)
        AppMode.JOURNAL -> if (isDark) Color(0x193B82F6) else Color(0x0F3B82F6)
        AppMode.READING -> if (isDark) Color(0x1910B981) else Color(0x0F10B981)
        AppMode.TRACKING -> if (isDark) Color(0x19F59E0B) else Color(0x0FF59E0B)
    }
    
    val animatedTint by animateColorAsState(
        targetValue = targetTint,
        animationSpec = tween(600),
        label = "mode_color_tint"
    )

    // Subtle background radial gradient pulse
    val bgEaseInOutSine = Easing { fraction -> (1f - kotlin.math.cos(fraction * kotlin.math.PI.toFloat())) / 2f }
    val bgPulseTransition = rememberInfiniteTransition(label = "bg_radial_pulse")
    val bgPulseRadiusScale by bgPulseTransition.animateFloat(
        initialValue = 0.82f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 7500, easing = bgEaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bg_pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                val radius = size.minDimension * 0.82f * bgPulseRadiusScale
                val baseColor = animatedTint.copy(alpha = if (isDark) 0.32f else 0.22f)
                val outerColor = Color.Transparent
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(baseColor, outerColor),
                        center = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height * 0.42f),
                        radius = radius
                    )
                )
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
        ) {
            // Mode toggle sits fixed at the top of the Home Dashboard Screen
            ModeToggle(
                currentMode = currentMode,
                onModeSelected = { viewModel.setMode(it) },
                modifier = Modifier.statusBarsPadding()
            )

            Box(
        modifier = Modifier
            .weight(1f)
                    .fillMaxWidth()
            ) {
                androidx.compose.animation.SharedTransitionLayout {
                    AnimatedContent(
                        targetState = currentMode,
                        transitionSpec = {
                        val springSpec = spring<Float>(
                            dampingRatio = 0.85f,
                            stiffness = 300f
                        )
                        (fadeIn(animationSpec = tween(250)) +
                        scaleIn(initialScale = 0.92f, animationSpec = springSpec)) togetherWith
                        (fadeOut(animationSpec = tween(200)) +
                        scaleOut(targetScale = 0.96f, animationSpec = tween(200)))
                    },
                    label = "mode_transitions",
                        modifier = Modifier.fillMaxSize()
                    ) { mode ->
                    when (mode) {
                        AppMode.FOCUS -> FocusHomeDashboard(viewModel)
                        AppMode.JOURNAL -> JournalScreen(viewModel = viewModel)
                        AppMode.READING -> ReadingScreen(viewModel = viewModel)
                        AppMode.TRACKING -> TrackingScreen(viewModel = viewModel)
                    }
                }
                }
                // Floating Action Buttons Group (Bottom End)
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        
            .clip(RoundedCornerShape(20.dp))
            .background(viewModel.themeGlassFill)
            .border(1.dp, viewModel.themeGlassBorder, RoundedCornerShape(20.dp))
            .padding(bottom = 24.dp, end = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    FloatingActionButton(
                        onClick = { showNotesSheet = true },
                        containerColor = viewModel.themeGlassFill,
                        contentColor = viewModel.themeText,
                        elevation = FloatingActionButtonDefaults.elevation(0.dp)
                    ) {
                        Icon(Icons.Default.Description, contentDescription = "Markdown Notes")
                    }

                    FloatingActionButton(
                        onClick = { showQuickVoiceDialog = true },
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.testTag("floating_quick_voice_button")
                    ) {
                        Icon(Icons.Default.Mic, contentDescription = "Quick Voice Entry", tint = MaterialTheme.colorScheme.primary)
                    }

                    FloatingActionButton(
                        onClick = { showChatSheet = true },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.testTag("floating_cube_coach_button")
                    ) {
                        Icon(com.example.ui.components.CubeIcon, contentDescription = "Gemini", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                }


            }
        }
    }

    com.example.ui.components.ViewModelConfettiOverlay(viewModel)

    if (showQuickVoiceDialog) {
        com.example.ui.components.QuickVoiceEntryDialog(
            viewModel = viewModel,
            onDismiss = { showQuickVoiceDialog = false },
            onOpenChatWithPrompt = { prompt ->
                showQuickVoiceDialog = false
                showChatSheet = true
                viewModel.sendMessageToCoach("Voice goal/task: $prompt")
            }
        )
    }

    if (showNotesSheet) {
        LuminaMarkdownNotesSheet(
            viewModel = viewModel,
            onDismiss = { showNotesSheet = false }
        )
    }

    if (showChatSheet) {
        ModalBottomSheet(
            onDismissRequest = { showChatSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            ChatScreen(
                viewModel = viewModel,
                onDismiss = { showChatSheet = false },
                modifier = Modifier.fillMaxHeight(0.85f)
            )
        }
    }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusHomeDashboard(viewModel: FocusViewModel) {
    val haptic = LocalHapticFeedback.current
    val scrollState = rememberScrollState()
    val habitsList by viewModel.habits.collectAsState()
    val sessionsList by viewModel.sessions.collectAsState()
    val accentColor = Color(android.graphics.Color.parseColor(viewModel.accentColorHex))
    var showWidgetCustomizer by remember { mutableStateOf(false) }
    var showTagSheet by remember { mutableStateOf(false) }
    val timetableWeeks by viewModel.timetableWeeks.collectAsState()

    // Dynamic specular highlight tilt trackers
    val context = androidx.compose.ui.platform.LocalContext.current
    var tiltX by remember { mutableStateOf(0f) }
    var tiltY by remember { mutableStateOf(0f) }

    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(android.content.Context.SENSOR_SERVICE) as android.hardware.SensorManager
        val accelerometer = sensorManager?.getDefaultSensor(android.hardware.Sensor.TYPE_ACCELEROMETER)
        val listener = object : android.hardware.SensorEventListener {
            override fun onSensorChanged(event: android.hardware.SensorEvent?) {
                if (event != null && event.sensor.type == android.hardware.Sensor.TYPE_ACCELEROMETER) {
                    val rawX = event.values[0]
                    val rawY = event.values[1]
                    tiltX = tiltX * 0.9f + rawX * 0.1f
                    tiltY = tiltY * 0.9f + rawY * 0.1f
                }
            }
            override fun onAccuracyChanged(sensor: android.hardware.Sensor?, accuracy: Int) {}
        }
        if (accelerometer != null) {
            sensorManager.registerListener(listener, accelerometer, android.hardware.SensorManager.SENSOR_DELAY_UI)
        }
        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    // Selected date state for the interactive calendar
    var selectedDateStr by remember { mutableStateOf(viewModel.getTodayDateString()) }

    var showGameStatusDialog by remember { mutableStateOf(false) }
    val academicTasks by viewModel.academicTasks.collectAsState()
    val voiceNotesList by viewModel.voiceNotes.collectAsState()

    // Scheduler form state
    var showScheduleForm by remember { mutableStateOf(false) }
    var scheduleTitle by remember { mutableStateOf("") }
    var scheduleDuration by remember { mutableStateOf(25) }
    var scheduleCategory by remember { mutableStateOf("Work") }
    var showCelebration by remember { mutableStateOf(false) }

    val displayName = if (viewModel.profileName.isBlank()) "Focus User" else viewModel.profileName

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
                .padding(bottom = 120.dp) // Cushion above navigation bar
        ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Focus Shield Glass Header on Top
        val blockedAppsCount = viewModel.blockedApps.collectAsState().value.count { it.isBlocked }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(100.dp))
                .border(1.dp, viewModel.themeGlassBorder, RoundedCornerShape(100.dp))
                .clickable { viewModel.setTab(AppTab.SHIELD) }
        ) {
            // Real-time background fill layer
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(viewModel.themeGlassFill)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(if (viewModel.shieldActive) AccentTeal.copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Shield Icon",
                            tint = if (viewModel.shieldActive) AccentTeal else Color.Gray,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (viewModel.shieldActive) "SHIELD ACTIVE" else "SHIELD STANDBY",
                        color = viewModel.themeText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (viewModel.shieldActive) "$blockedAppsCount Apps Protected" else "Standby",
                        color = if (viewModel.shieldActive) AccentTeal else viewModel.themeTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Configure Shield",
                        tint = viewModel.themeTextSecondary.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Top Status & Controls Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "LUMINA",
                    color = viewModel.themeText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Focus · Refined",
                    color = viewModel.themeTextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            // Top Right Controls (Theme Toggle + User Avatar)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ThemeToggle(viewModel = viewModel)

                // User Avatar with Animated Gradient Border
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.sweepGradient(
                                colors = listOf(accentColor, AccentPink, AccentYellow, accentColor)
                            )
                        )
                        .padding(2.5.dp)
                        .clip(CircleShape)
                        .background(viewModel.themeSurface)
                        .clickable { showGameStatusDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = displayName.firstOrNull()?.toString()?.uppercase() ?: "F",
                        color = viewModel.themeText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Lumina Premium Hero Banner
        LuminaHeroSection(
            viewModel = viewModel,
            displayName = displayName,
            accentColor = accentColor,
            onStartFocus = { showTagSheet = true }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Hero Cube Section (3D isometric interactive widget - Floating & Expandable Radial Mode Menu)
        var isExpanded by remember { mutableStateOf(false) }
        val expandFraction by animateFloatAsState(
            targetValue = if (isExpanded) 1f else 0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessMedium
            ),
            label = "expand_fraction"
        )

        // Floating offset animation
        val EaseInOutSine = Easing { fraction -> (1f - kotlin.math.cos(fraction * kotlin.math.PI.toFloat())) / 2f }
        val infiniteTransition = rememberInfiniteTransition(label = "cube_float")
        val floatOffsetY by infiniteTransition.animateFloat(
            initialValue = -8f,
            targetValue = 8f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 2500, easing = EaseInOutSine),
                repeatMode = RepeatMode.Reverse
            ),
            label = "cube_float_y"
        )

        class ModeBtn(
            val mode: AppMode,
            val label: String,
            val icon: androidx.compose.ui.graphics.vector.ImageVector,
            val color: Color
        )

        LiquidGlassmorphismContainer(
            isDarkMode = viewModel.isDarkMode,
            modifier = Modifier
                .fillMaxWidth()
                .height(340.dp)
                .clip(RoundedCornerShape(32.dp))
                .border(1.dp, viewModel.themeGlassBorder, RoundedCornerShape(32.dp))
        ) {
            // Background glow that intensifies when expanded
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                accentColor.copy(alpha = 0.15f * expandFraction + 0.05f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // 1. Central Isometric Cube (Drawn FIRST so it is behind buttons)
            Box(
                modifier = Modifier
                    .size(130.dp) // Tight physical boundary size to prevent click encroachment
                    .offset(y = floatOffsetY.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        isExpanded = !isExpanded
                    },
                contentAlignment = Alignment.Center
            ) {
                AntiGravityWrapper {
                    IsometricCube3D(
                        cubeSize = 120.dp,
                        accentColor = accentColor,
                        faceTextTop = "HABITS",
                        faceTextLeft = "SHIELD",
                        faceTextRight = "STATS",
                        shieldActive = viewModel.shieldActive,
                        onFaceTapped = { face ->
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            isExpanded = !isExpanded
                        },
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            isExpanded = !isExpanded
                        },
                        expandFraction = expandFraction
                    )
                }
            }

            // 2. Surround 4 mode-selection buttons in a radial diagonal cluster (Drawn AFTER so they are physically on top and clickable)
            val buttons = listOf(
                ModeBtn(AppMode.FOCUS, "Focus", Icons.Default.Timer, Color(0xFFA855F7)),
                ModeBtn(AppMode.JOURNAL, "Journal", Icons.Default.Edit, Color(0xFF3B82F6)),
                ModeBtn(AppMode.READING, "Reading", Icons.Default.Book, Color(0xFF10B981)),
                ModeBtn(AppMode.TRACKING, "Tracking", Icons.Default.TrendingUp, Color(0xFFF59E0B))
            )

            buttons.forEachIndexed { index, btn ->
                // Position offsets in dp
                val targetX = when (index) {
                    0 -> (-100).dp // Top-Left
                    1 -> 100.dp  // Top-Right
                    2 -> (-100).dp // Bottom-Left
                    else -> 100.dp // Bottom-Right
                }
                val targetY = when (index) {
                    0 -> (-90).dp
                    1 -> (-90).dp
                    2 -> 90.dp
                    else -> 90.dp
                }

                val currentX = targetX * expandFraction
                val currentY = targetY * expandFraction
                val alphaVal = expandFraction
                val scaleVal = 0.5f + 0.5f * expandFraction

                if (expandFraction > 0.01f) {
                    Box(
                        modifier = Modifier
                            .offset(x = currentX, y = currentY)
                            .graphicsLayer {
                                alpha = alphaVal
                                scaleX = scaleVal
                                scaleY = scaleVal
                            }
                            .clip(RoundedCornerShape(20.dp))
                            .border(
                                width = 1.dp,
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.45f),
                                        Color.White.copy(alpha = 0.05f)
                                    ),
                                    start = androidx.compose.ui.geometry.Offset(
                                        x = 120f * (0.5f - tiltX / 9.81f),
                                        y = 120f * (0.5f + tiltY / 9.81f)
                                    ),
                                    end = androidx.compose.ui.geometry.Offset(
                                        x = 120f * (0.5f + tiltX / 9.81f),
                                        y = 120f * (0.5f - tiltY / 9.81f)
                                    )
                                ),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.setMode(btn.mode)
                                isExpanded = false
                            }
                            .testTag("mode_button_${btn.mode.name.lowercase()}"),
                        contentAlignment = Alignment.Center
                    ) {
                        // Frosted real-time backdrop fill layer
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            if (viewModel.isDarkMode) Color.White.copy(alpha = 0.14f) else Color.White.copy(alpha = 0.55f),
                                            if (viewModel.isDarkMode) Color.White.copy(alpha = 0.06f) else Color.White.copy(alpha = 0.3f)
                                        )
                                    )
                                )
                        )

                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = btn.icon,
                                contentDescription = btn.label,
                                tint = btn.color,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = btn.label,
                                color = viewModel.themeText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Expanded status hint indicator text
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp)
            ) {
                Text(
                    text = if (isExpanded) "Tap cube to collapse modes" else "Tap cube to expand focus modes",
                    color = viewModel.themeTextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.5.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Quick Stats Pill Row (Now starts correctly empty/zeroed)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            listOf(
                Triple("Streak", "${viewModel.getStreakCount()} 🔥", AccentTeal),
                Triple("Focus Hr", String.format("%.1f ⚡", viewModel.getFocusTimeHours()), AccentYellow),
                Triple("Blocked", "${viewModel.getBlockedAppsCount()} 🛡️", AccentPink)
            ).forEach { (label, value, valueColor) ->
                val tab = when (label) {
                    "Streak" -> AppTab.HABITS
                    "Focus Hr" -> AppTab.STATS
                    else -> AppTab.SHIELD
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(18.dp))
                        .border(1.dp, viewModel.themeGlassBorder, RoundedCornerShape(18.dp))
                        .clickable { viewModel.setTab(tab) }
                ) {
                    // Real-time background fill layer
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(viewModel.themeGlassFill)
                    )
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    ) {
                        Text(text = label, color = viewModel.themeTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = value, color = valueColor, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // LUMINA REFINE DESIGN SYSTEM RESPONSIVE COMPOSABLES
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val isWide = maxWidth > 600.dp
            if (isWide) {
                // Wide tablet/landscape layout: 3 elegant side-by-side columns!
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        LuminaNowPlayingCard(viewModel = viewModel, accentColor = accentColor)
                        LuminaAmbientSoundCard(viewModel = viewModel, accentColor = accentColor)
                    }
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        LuminaInsightsCard(viewModel = viewModel, accentColor = accentColor)
                        LuminaFlowChecklistCard(viewModel = viewModel, accentColor = accentColor)
                        LuminaQuoteCard(viewModel = viewModel)
                    }
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        LuminaFocusProgressRingCard(viewModel = viewModel, accentColor = accentColor)
                    }
                }
            } else {
                // Mobile compact stacked layout: beautiful vertical card list
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    LuminaNowPlayingCard(viewModel = viewModel, accentColor = accentColor)
                    LuminaFlowChecklistCard(viewModel = viewModel, accentColor = accentColor)
                    LuminaFocusProgressRingCard(viewModel = viewModel, accentColor = accentColor)
                    LuminaAmbientSoundCard(viewModel = viewModel, accentColor = accentColor)
                    LuminaInsightsCard(viewModel = viewModel, accentColor = accentColor)
                    LuminaQuoteCard(viewModel = viewModel)
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Customizable Adaptive Widget Workspace Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "Workspace",
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Personal Workspace",
                    color = viewModel.themeText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            com.example.ui.components.GlassButton(variant = com.example.ui.components.GlassButtonVariant.Secondary, 
                onClick = { showWidgetCustomizer = true }
            ) {
                Text(
                    text = "EDIT WIDGETS",
                    color = accentColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        val dashboardScope = rememberCoroutineScope()
        val filteredActiveWidgets = viewModel.activeWidgets.filter { wId ->
            val w = WidgetRegistry.widgets.firstOrNull { it.id == wId }
            w != null && (w.category == "UNIVERSAL" || w.category == viewModel.userRole.uppercase())
        }

        // Dynamic customizable widgets loop in a LazyVerticalGrid
        if (filteredActiveWidgets.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
            .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Your Workspace is empty", color = viewModel.themeText, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "Tap EDIT WIDGETS above to add cards!", color = viewModel.themeTextSecondary, fontSize = 12.sp, textAlign = TextAlign.Center)
                }
            }
        } else {
            // Draggable LazyVerticalGrid mimicking Framer Motion spring physics
            var draggedIndex by remember { mutableStateOf<Int?>(null) }
            var dragOffset by remember { mutableStateOf(Offset.Zero) }
            var dragTargetIndex by remember { mutableStateOf<Int?>(null) }

            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 320.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 8000.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                userScrollEnabled = false
            ) {
                itemsIndexed(filteredActiveWidgets, key = { _, id -> id }) { index, widgetId ->
                    val isDragging = draggedIndex == index
                    
                    // Framer Motion style spring animation
                    val translationX by animateFloatAsState(
                        targetValue = if (isDragging) dragOffset.x else 0f,
                        animationSpec = spring(dampingRatio = 0.82f, stiffness = Spring.StiffnessLow)
                    )
                    val translationY by animateFloatAsState(
                        targetValue = if (isDragging) dragOffset.y else 0f,
                        animationSpec = spring(dampingRatio = 0.82f, stiffness = Spring.StiffnessLow)
                    )
                    val scale by animateFloatAsState(
                        targetValue = if (isDragging) 1.04f else 1f,
                        animationSpec = spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessMedium)
                    )
                    val elevation by animateDpAsState(
                        targetValue = if (isDragging) 12.dp else 0.dp,
                        animationSpec = spring()
                    )

                    Box(
                        modifier = Modifier
                            .animateItem()
                            .graphicsLayer {
                                this.translationX = translationX
                                this.translationY = translationY
                                this.scaleX = scale
                                this.scaleY = scale
                                this.shadowElevation = elevation.toPx()
                            }
                            .pointerInput(filteredActiveWidgets) {
                                detectDragGesturesAfterLongPress(
                                    onDragStart = { _ ->
                                        draggedIndex = index
                                        dragOffset = Offset.Zero
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        dragOffset += dragAmount
                                        
                                        // Swap calculation based on drag height
                                        val rowOffset = (dragOffset.y / 240f).toInt()
                                        val newIndex = (index + rowOffset).coerceIn(0, filteredActiveWidgets.size - 1)
                                        if (newIndex != index) {
                                            dragTargetIndex = newIndex
                                        }
                                    },
                                    onDragEnd = {
                                        val target = dragTargetIndex
                                        if (target != null && target != index && target in filteredActiveWidgets.indices) {
                                            val fullList = viewModel.activeWidgets.toMutableList()
                                            val sourceId = filteredActiveWidgets[index]
                                            val targetId = filteredActiveWidgets[target]
                                            val srcIdx = fullList.indexOf(sourceId)
                                            val tgtIdx = fullList.indexOf(targetId)
                                            if (srcIdx != -1 && tgtIdx != -1) {
                                                val removed = fullList.removeAt(srcIdx)
                                                fullList.add(tgtIdx, removed)
                                                viewModel.saveWidgetsToPrefs(fullList)
                                            }
                                        }
                                        draggedIndex = null
                                        dragOffset = Offset.Zero
                                        dragTargetIndex = null
                                    },
                                    onDragCancel = {
                                        draggedIndex = null
                                        dragOffset = Offset.Zero
                                        dragTargetIndex = null
                                    }
                                )
                            }
                    ) {
                        WidgetRenderer(
                            widgetId = widgetId,
                            viewModel = viewModel,
                            accentColor = accentColor,
                            scope = dashboardScope,
                            selectedDateStr = selectedDateStr
                        )
                    }
                }
            }
        }

        // Widget Personalizer Bottom Dialog
        if (showWidgetCustomizer) {
            AlertDialog(
                onDismissRequest = { showWidgetCustomizer = false },
                confirmButton = {
                    com.example.ui.components.GlassButton(variant = com.example.ui.components.GlassButtonVariant.Secondary, onClick = { showWidgetCustomizer = false }) {
                        Text("DONE", color = accentColor, fontWeight = FontWeight.Bold)
                    }
                },
                title = {
                    Text(
                        text = "Customize Dashboard",
                        color = viewModel.themeText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(360.dp)
                            .verticalScroll(rememberScrollState())
    ) {
                        Text(
                            text = "ACTIVE TILES",
                            color = accentColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        if (viewModel.activeWidgets.isEmpty()) {
                            Text("No widgets active on the dashboard.", color = viewModel.themeTextSecondary, fontSize = 12.sp)
                        } else {
                            viewModel.activeWidgets.forEachIndexed { index, wId ->
                                val widget = WidgetRegistry.widgets.firstOrNull { it.id == wId }
                                if (widget != null) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(viewModel.themeSurface)
                                            .padding(8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                            Icon(
                                                imageVector = com.example.ui.widget.getWidgetIcon(widget.iconName),
                                                contentDescription = null,
                                                tint = accentColor,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(widget.title, color = viewModel.themeText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                        
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            // Reorder Up
                                            if (index > 0) {
                                                com.example.ui.components.GlassIconButton(
                                                    onClick = {
                                                        val newList = viewModel.activeWidgets.toMutableList()
                                                        val temp = newList[index]
                                                        newList[index] = newList[index - 1]
                                                        newList[index - 1] = temp
                                                        viewModel.saveWidgetsToPrefs(newList)
                                                    },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(Icons.Default.ArrowUpward, contentDescription = "Move Up", tint = viewModel.themeTextSecondary, modifier = Modifier.size(14.dp))
                                                }
                                            }
                                            // Reorder Down
                                            if (index < viewModel.activeWidgets.size - 1) {
                                                com.example.ui.components.GlassIconButton(
                                                    onClick = {
                                                        val newList = viewModel.activeWidgets.toMutableList()
                                                        val temp = newList[index]
                                                        newList[index] = newList[index + 1]
                                                        newList[index + 1] = temp
                                                        viewModel.saveWidgetsToPrefs(newList)
                                                    },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(Icons.Default.ArrowDownward, contentDescription = "Move Down", tint = viewModel.themeTextSecondary, modifier = Modifier.size(14.dp))
                                                }
                                            }
                                            // Remove
                                            com.example.ui.components.GlassIconButton(
                                                onClick = {
                                                    val newList = viewModel.activeWidgets.toMutableList()
                                                    newList.removeAt(index)
                                                    viewModel.saveWidgetsToPrefs(newList)
                                                },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Color.Red, modifier = Modifier.size(14.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "AVAILABLE TILES",
                            color = accentColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        val availableWidgets = WidgetRegistry.widgets.filter { 
                            it.id !in viewModel.activeWidgets && (it.category == "UNIVERSAL" || it.category == viewModel.userRole.uppercase())
                        }
                        if (availableWidgets.isEmpty()) {
                            Text("All widgets active.", color = viewModel.themeTextSecondary, fontSize = 12.sp)
                        } else {
                            availableWidgets.forEach { widget ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(viewModel.themeSurface)
                                        .clickable {
                                            val newList = viewModel.activeWidgets.toMutableList()
                                            newList.add(widget.id)
                                            viewModel.saveWidgetsToPrefs(newList)
                                        }
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = com.example.ui.widget.getWidgetIcon(widget.iconName),
                                            contentDescription = null,
                                            tint = viewModel.themeTextSecondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(widget.title, color = viewModel.themeText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            Text(widget.description, color = viewModel.themeTextSecondary, fontSize = 10.sp)
                                        }
                                    }
                                    Icon(Icons.Default.Add, contentDescription = "Add", tint = accentColor, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                },
                containerColor = viewModel.themeBg,
                shape = RoundedCornerShape(28.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // INTERACTIVE GLASS CALENDAR CONTROLLER
        Column(
            modifier = Modifier
                .fillMaxWidth()
            .padding(18.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(viewModel.themeGlassFill)
            .border(1.dp, viewModel.themeGlassBorder, RoundedCornerShape(24.dp))
    ) {
            val calendar = remember { java.util.Calendar.getInstance() }
            val currentMonthName = remember { SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(calendar.time) }
            val daysInMonth = remember { calendar.getActualMaximum(java.util.Calendar.DAY_OF_MONTH) }
            val firstDayOfWeek = remember {
                val cal = java.util.Calendar.getInstance()
                cal.set(java.util.Calendar.DAY_OF_MONTH, 1)
                (cal.get(java.util.Calendar.DAY_OF_WEEK) + 5) % 7 // Monday-indexed (0 to 6)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.DateRange,
                        contentDescription = "Calendar",
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Work Scheduler",
                        color = viewModel.themeText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = currentMonthName,
                    color = accentColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Weekday labels
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                listOf("M", "T", "W", "T", "F", "S", "S").forEach { day ->
                    Text(
                        text = day,
                        color = viewModel.themeTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Days Grid calculation & looping (100% standard and compatible)
            val totalSlots = daysInMonth + firstDayOfWeek
            val rows = (totalSlots + 6) / 7
            for (r in 0 until rows) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    for (c in 0 until 7) {
                        val index = r * 7 + c
                        val dayNum = index - firstDayOfWeek + 1
                        if (dayNum in 1..daysInMonth) {
                            val dateStr = String.format("%04d-%02d-%02d", calendar.get(java.util.Calendar.YEAR), calendar.get(java.util.Calendar.MONTH) + 1, dayNum)
                            val isSelected = dateStr == selectedDateStr
                            val isToday = dateStr == viewModel.getTodayDateString()

                            Box(
        modifier = Modifier
            .weight(1f)
                                    .aspectRatio(1f)
                                    
            .clip(RoundedCornerShape(20.dp))
            .background(viewModel.themeGlassFill)
            .border(1.dp, viewModel.themeGlassBorder, RoundedCornerShape(20.dp))
            .padding(2.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isSelected -> accentColor
                                            isToday -> accentColor.copy(alpha = 0.15f)
                                            else -> Color.Transparent
                                        }
                                    )
                                    .clickable { selectedDateStr = dateStr }
                                    .wrapContentSize(Alignment.Center),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = dayNum.toString(),
                                    color = when {
                                        isSelected -> if (viewModel.isDarkMode) Color.Black else Color.White
                                        isToday -> accentColor
                                        else -> viewModel.themeText
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Focus Schedule List for selected date
            val schedulesForDate = viewModel.scheduledWorkList.filter { it.date == selectedDateStr }
            Text(
                text = "Sessions on ${selectedDateStr.substringAfterLast("-")}:",
                color = viewModel.themeTextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (schedulesForDate.isEmpty()) {
                Text(
                    text = "No focus sessions scheduled for this date.",
                    color = viewModel.themeTextSecondary.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            } else {
                schedulesForDate.forEach { work ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(viewModel.themeSurface.copy(alpha = 0.5f))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = work.title, color = viewModel.themeText, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(text = "${work.durationMinutes}m | ${work.category}", color = viewModel.themeTextSecondary, fontSize = 11.sp)
                        }
                        // Synced status pill
                        val isGoogleUser = viewModel.mAuth?.currentUser != null
                        Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (work.isGoogleSynced) AccentTeal.copy(alpha = 0.15f)
                                    else if (isGoogleUser) AccentPink.copy(alpha = 0.1f)
                                    else viewModel.themeTextSecondary.copy(alpha = 0.1f)
                                )
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = if (work.isGoogleSynced) "Synced" else if (isGoogleUser) "Pending Google Sync" else "Local Only",
                                color = if (work.isGoogleSynced) AccentTeal else if (isGoogleUser) AccentPink else viewModel.themeTextSecondary,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Expandable Scheduler Form Button
            if (!showScheduleForm) {
                com.example.ui.components.GlassButton(
                    onClick = { showScheduleForm = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Add,
                        contentDescription = "Schedule",
                        tint = if (viewModel.isDarkMode) Color.Black else Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Schedule Focus Block",
                        color = if (viewModel.isDarkMode) Color.Black else Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                // Inline translucent scheduling sheet
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(viewModel.themeSurface.copy(alpha = 0.4f))
                        .padding(12.dp)
                ) {
                    Text(text = "NEW SCHEDULE BLOCK", color = accentColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = scheduleTitle,
                        onValueChange = { scheduleTitle = it },
                        label = { Text("Task / Block Title", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accentColor,
                            unfocusedBorderColor = viewModel.themeGlassBorder,
                            focusedTextColor = viewModel.themeText,
                            unfocusedTextColor = viewModel.themeText
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Duration pickers
                        listOf(25, 45, 60).forEach { mins ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (scheduleDuration == mins) accentColor else viewModel.themeSurface.copy(alpha = 0.3f)
                                    )
                                    .border(1.dp, viewModel.themeGlassBorder, RoundedCornerShape(10.dp))
                                    .clickable { scheduleDuration = mins }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${mins}m",
                                    color = if (scheduleDuration == mins) (if (viewModel.isDarkMode) Color.Black else Color.White) else viewModel.themeText,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Category choices
                        listOf("Work", "Learn", "Life", "Health").forEach { cat ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (scheduleCategory == cat) accentColor.copy(alpha = 0.25f) else Color.Transparent
                                    )
                                    .border(
                                        1.dp,
                                        if (scheduleCategory == cat) accentColor else viewModel.themeGlassBorder,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable { scheduleCategory = cat }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = cat,
                                    color = if (scheduleCategory == cat) accentColor else viewModel.themeTextSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        com.example.ui.components.GlassButton(variant = com.example.ui.components.GlassButtonVariant.Secondary, 
                            onClick = { showScheduleForm = false },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = viewModel.themeTextSecondary)
                        ) {
                            Text("Cancel", fontSize = 11.sp)
                        }

                        com.example.ui.components.GlassButton(
                            onClick = {
                                if (scheduleTitle.isNotBlank()) {
                                    viewModel.scheduleWork(
                                        title = scheduleTitle,
                                        dateString = selectedDateStr,
                                        durationMinutes = scheduleDuration,
                                        category = scheduleCategory
                                    )
                                    scheduleTitle = ""
                                    showScheduleForm = false
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                        ) {
                            Text("Schedule", fontSize = 11.sp, color = if (viewModel.isDarkMode) Color.Black else Color.White)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Upcoming Habits Strip (Starts beautifully empty)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Upcoming Habits",
                color = viewModel.themeText,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "VIEW ALL",
                color = AccentTeal,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                modifier = Modifier.clickable { viewModel.setTab(AppTab.HABITS) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (habitsList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
            .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Your habit stacks are empty.", color = viewModel.themeText, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Create your first habits in Habits tab!", color = viewModel.themeTextSecondary, fontSize = 12.sp)
                }
            }
        } else {
            val completionsList by viewModel.completions.collectAsState()
            habitsList.forEach { habit ->
                val itemColor = Color(android.graphics.Color.parseColor(habit.colorHex))
                val currentProgress = completionsList.find { it.habitId == habit.id && it.date == selectedDateStr }?.value ?: 0
                val isCompleted = currentProgress >= habit.targetValue

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
            .padding(16.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(viewModel.themeGlassFill)
            .border(1.dp, viewModel.themeGlassBorder, RoundedCornerShape(20.dp))
    ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
        modifier = Modifier
            .width(4.dp)
                                    .height(34.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(itemColor)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(text = habit.name, color = viewModel.themeText, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    text = habit.category,
                                    color = viewModel.themeTextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // Right Side: Streak indicator OR toggle controls
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(itemColor.copy(alpha = 0.15f))
                                    
            .clip(RoundedCornerShape(24.dp))
            .background(viewModel.themeGlassFill)
            .border(1.dp, viewModel.themeGlassBorder, RoundedCornerShape(24.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${habit.streakCount} 🔥",
                                    color = itemColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (habit.targetValue > 1) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    // Minus Button
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(viewModel.themeGlassFill)
                                            .border(1.dp, viewModel.themeGlassBorder, RoundedCornerShape(8.dp))
                                            .clickable {
                                                viewModel.updateHabitProgress(habit, selectedDateStr, currentProgress - 1)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("-", color = viewModel.themeText, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    }

                                    // Plus Button
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(itemColor.copy(alpha = 0.2f))
                                            .border(1.dp, itemColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                            .clickable {
                                                if (!isCompleted && currentProgress + 1 >= habit.targetValue) {
                                                    showCelebration = true
                                                }
                                                viewModel.updateHabitProgress(habit, selectedDateStr, currentProgress + 1)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("+", color = itemColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            } else {
                                // Toggle complete checkbox
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isCompleted) itemColor else viewModel.themeGlassFill)
                                        .border(
                                            width = 1.dp,
                                            color = if (isCompleted) itemColor else viewModel.themeGlassBorder,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable {
                                            if (!isCompleted) {
                                                showCelebration = true
                                            }
                                            viewModel.toggleHabitCompletion(habit, selectedDateStr)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isCompleted) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Completed",
                                            tint = if (viewModel.isDarkMode) Color.Black else Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Progress indicators
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "$currentProgress / ${habit.targetValue} ${habit.unit}",
                            color = viewModel.themeTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        LinearProgressIndicator(
                            progress = { (currentProgress.toFloat() / habit.targetValue.toFloat()).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .weight(1f)
                                .height(5.dp)
                                .clip(RoundedCornerShape(2.5.dp)),
                                color = itemColor,
                                trackColor = viewModel.themeGlassBorder
                            )
                        }
                    }
                }
            }

        Spacer(modifier = Modifier.height(24.dp))

        // Voice Notes & Audio Saves Section
        VoiceNotesSection(
            viewModel = viewModel,
            voiceNotes = voiceNotesList,
            accentColor = accentColor
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Lumina Bottom Sliding Text Marquee Footer
        LuminaScrollingMarqueeFooter(viewModel = viewModel)
    }

    CelebrationBurst(
        isVisible = showCelebration,
        onAnimationEnd = { showCelebration = false }
    )

    if (showGameStatusDialog) {
        GameStatusDialog(
            onDismiss = { showGameStatusDialog = false },
            viewModel = viewModel,
            sessions = sessionsList,
            tasks = academicTasks,
            habits = habitsList
        )
    }

    if (showTagSheet) {
        com.focuscube.ui.screens.focus.TagSessionBottomSheet(
            weeks = timetableWeeks,
            onDismiss = { showTagSheet = false },
            onStartSession = { weekNumber ->
                showTagSheet = false
                viewModel.startFocusSession("Focus Session", 25, "Deep Work", weekNumber)
            }
        )
    }
}
}

@Composable
fun VoiceNotesSection(
    viewModel: FocusViewModel,
    voiceNotes: List<VoiceNote>,
    accentColor: Color
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var isRecording by remember { mutableStateOf(false) }
    var mediaRecorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var audioFile by remember { mutableStateOf<java.io.File?>(null) }
    var recordSeconds by remember { mutableStateOf(0) }
    
    // Live timer
    LaunchedEffect(isRecording) {
        if (isRecording) {
            recordSeconds = 0
            while (isRecording) {
                kotlinx.coroutines.delay(1000L)
                recordSeconds++
            }
        }
    }

    // Media Player State
    var activePlayingId by remember { mutableStateOf<Int?>(null) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    // Clean up on dispose
    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.release()
            mediaRecorder?.release()
        }
    }

    val voicePermissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            android.widget.Toast.makeText(context, "Microphone permission granted!", android.widget.Toast.LENGTH_SHORT).show()
        } else {
            android.widget.Toast.makeText(context, "Microphone permission required for voice notes.", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    fun startRecording() {
        val permissionCheck = androidx.core.content.ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.RECORD_AUDIO
        )
        if (permissionCheck != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            voicePermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
            return
        }

        try {
            val file = java.io.File(context.filesDir, "voice_note_${System.currentTimeMillis()}.mp4")
            audioFile = file

            val recorder = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }

            mediaRecorder = recorder
            isRecording = true
        } catch (e: Exception) {
            android.widget.Toast.makeText(context, "Error starting recording: ${e.localizedMessage}", android.widget.Toast.LENGTH_LONG).show()
        }
    }

    fun stopRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            isRecording = false

            val file = audioFile
            if (file != null && file.exists()) {
                val duration = if (recordSeconds > 0) recordSeconds else 1
                viewModel.addVoiceNote(
                    title = "Voice Note #${voiceNotes.size + 1}",
                    filePath = file.absolutePath,
                    durationSec = duration
                )
                android.widget.Toast.makeText(context, "Voice note saved!", android.widget.Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            android.widget.Toast.makeText(context, "Recording failed: Note too short", android.widget.Toast.LENGTH_SHORT).show()
            isRecording = false
            mediaRecorder = null
        }
    }

    fun playAudio(note: VoiceNote) {
        try {
            if (activePlayingId == note.id) {
                mediaPlayer?.stop()
                mediaPlayer?.release()
                mediaPlayer = null
                activePlayingId = null
            } else {
                mediaPlayer?.stop()
                mediaPlayer?.release()

                val player = MediaPlayer().apply {
                    setDataSource(note.filePath)
                    prepare()
                    start()
                    setOnCompletionListener { mp ->
                        try {
                            mp.stop()
                        } catch (e: Exception) {}
                        try {
                            mp.release()
                        } catch (e: Exception) {}
                        activePlayingId = null
                        mediaPlayer = null
                    }
                }
                mediaPlayer = player
                activePlayingId = note.id
            }
        } catch (e: Exception) {
            android.widget.Toast.makeText(context, "Error playing audio: ${e.localizedMessage}", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    // Card UI
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
        modifier = Modifier
            .size(36.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Voice Notes",
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Voice Notes & Audio Saves",
                        color = viewModel.themeText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Save thoughts, tasks, and notes",
                        color = viewModel.themeTextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            if (isRecording) {
                val infiniteTransition = rememberInfiniteTransition(label = "Pulse")
                val alpha by infiniteTransition.animateFloat(
                    initialValue = 0.2f,
                    targetValue = 0.8f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(800),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "PulseAlpha"
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Red.copy(alpha = alpha))
                        
            .clip(RoundedCornerShape(16.dp))
            .background(viewModel.themeGlassFill)
            .border(1.dp, viewModel.themeGlassBorder, RoundedCornerShape(16.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "REC ${recordSeconds}s",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val buttonBg = if (isRecording) Color.Red else accentColor
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(buttonBg.copy(alpha = 0.15f))
                        .border(1.dp, buttonBg, CircleShape)
                        .clickable {
                            if (isRecording) stopRecording() else startRecording()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = "Record Trigger",
                        tint = buttonBg,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isRecording) "Tap to stop & save" else "Tap microphone to record",
                    color = viewModel.themeTextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        if (voiceNotes.isNotEmpty()) {
            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = viewModel.themeGlassBorder)
            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                voiceNotes.forEach { note ->
                    val isPlaying = activePlayingId == note.id
                    val sdf = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault())
                    val formattedDate = sdf.format(java.util.Date(note.timestamp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(viewModel.themeGlassFill.copy(alpha = 0.5f))
                            .border(1.dp, viewModel.themeGlassBorder, RoundedCornerShape(16.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            com.example.ui.components.GlassIconButton(
                                onClick = { playAudio(note) },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(accentColor.copy(alpha = 0.1f))
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "Playback",
                                    tint = accentColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = note.title,
                                    color = viewModel.themeText,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "$formattedDate • ${note.durationSec}s",
                                    color = viewModel.themeTextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        com.example.ui.components.GlassIconButton(
                            onClick = {
                                if (isPlaying) {
                                    mediaPlayer?.stop()
                                    mediaPlayer?.release()
                                    mediaPlayer = null
                                    activePlayingId = null
                                }
                                viewModel.deleteVoiceNote(note)
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete note",
                                tint = Color.Red.copy(alpha = 0.7f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GameStatusDialog(
    onDismiss: () -> Unit,
    viewModel: FocusViewModel,
    sessions: List<com.example.data.WorkSession>,
    tasks: List<com.example.data.AcademicTask>,
    habits: List<com.example.data.Habit>
) {
    val completedSessions = sessions.size
    val completedTasks = tasks.count { it.status == "Completed" }
    val totalJournalEntries = viewModel.journalEntries.value.size
    val totalHabitStreaks = habits.sumOf { it.streakCount }

    val totalXPoints = completedSessions * 35 + completedTasks * 60 + totalJournalEntries * 25 + totalHabitStreaks * 15
    val currentLevel = (totalXPoints / 120).coerceAtLeast(1)
    val nextLevelXp = (currentLevel + 1) * 120
    val prevLevelXp = currentLevel * 120
    val levelXpGained = totalXPoints - prevLevelXp
    val xpProgressFraction = if (nextLevelXp > prevLevelXp) {
        (levelXpGained.toFloat() / 120f).coerceIn(0f, 1f)
    } else {
        0f
    }

    val classTitle = when {
        currentLevel >= 15 -> "Time Warden Grandmaster"
        currentLevel >= 10 -> "Chronos Spellweaver"
        currentLevel >= 6 -> "Deep Work Wizard"
        currentLevel >= 3 -> "Focus Knight"
        else -> "Deep Work Apprentice"
    }

    val strStat = (completedSessions * 8 + 15).coerceIn(15, 100)
    val agiStat = (totalHabitStreaks * 6 + 15).coerceIn(15, 100)
    val intStat = (completedTasks * 10 + 15).coerceIn(15, 100)
    val vitStat = (totalJournalEntries * 12 + 15).coerceIn(15, 100)
    val lukStat = ((System.currentTimeMillis() / 86400000L % 40) + 50).toInt()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.75f))
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            GlassSurface(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .clickable(enabled = false) {},
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = viewModel.themeSurface),
                border = BorderStroke(1.dp, viewModel.themeGlassBorder)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.SportsEsports,
                                contentDescription = "RPG HUD",
                                tint = AccentTeal,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "FOCUS HERO STATS",
                                color = viewModel.themeText,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Brush.sweepGradient(listOf(AccentTeal, AccentPink, AccentYellow, AccentTeal)))
                                .padding(2.dp)
                                .clip(CircleShape)
                                .background(viewModel.themeSurface),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "🧙",
                                fontSize = 28.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)
    ) {
                            val nameText = if (viewModel.profileName.isBlank()) "Focus User" else viewModel.profileName
                            Text(
                                text = nameText,
                                color = viewModel.themeText,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = classTitle,
                                color = AccentTeal,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Level $currentLevel",
                                color = viewModel.themeText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "XP Progress",
                                color = viewModel.themeTextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "$levelXpGained / 120 XP",
                                color = viewModel.themeText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { xpProgressFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = AccentTeal,
                            trackColor = viewModel.themeGlassBorder
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "FOCUS DISCIPLINE RADAR",
                        color = viewModel.themeText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .align(Alignment.CenterHorizontally),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val centerX = size.width / 2f
                            val centerY = size.height / 2f
                            val maxRadius = size.width / 2f - 20f

                            val angles = List(5) { i -> i * 2 * Math.PI / 5 - Math.PI / 2 }

                            val gridFractions = listOf(0.2f, 0.4f, 0.6f, 0.8f, 1.0f)
                            gridFractions.forEach { frac ->
                                val r = maxRadius * frac
                                val path = Path().apply {
                                    val x0 = centerX + r * cos(angles[0]).toFloat()
                                    val y0 = centerY + r * sin(angles[0]).toFloat()
                                    moveTo(x0, y0)
                                    for (i in 1..4) {
                                        val x = centerX + r * cos(angles[i]).toFloat()
                                        val y = centerY + r * sin(angles[i]).toFloat()
                                        lineTo(x, y)
                                    }
                                    close()
                                }
                                drawPath(
                                    path = path,
                                    color = viewModel.themeGlassBorder,
                                    style = Stroke(width = 1.dp.toPx())
                                )
                            }

                            angles.forEach { angle ->
                                drawLine(
                                    color = viewModel.themeGlassBorder,
                                    start = Offset(centerX, centerY),
                                    end = Offset(
                                        centerX + maxRadius * cos(angle).toFloat(),
                                        centerY + maxRadius * sin(angle).toFloat()
                                    ),
                                    strokeWidth = 1.dp.toPx()
                                )
                            }

                            val statValues = listOf(strStat, agiStat, intStat, vitStat, lukStat)
                            val statPath = Path().apply {
                                val r0 = maxRadius * (statValues[0].toFloat() / 100f)
                                moveTo(
                                    centerX + r0 * cos(angles[0]).toFloat(),
                                    centerY + r0 * sin(angles[0]).toFloat()
                                )
                                for (i in 1..4) {
                                    val r = maxRadius * (statValues[i].toFloat() / 100f)
                                    lineTo(
                                        centerX + r * cos(angles[i]).toFloat(),
                                        centerY + r * sin(angles[i]).toFloat()
                                    )
                                }
                                close()
                            }
                            drawPath(
                                path = statPath,
                                color = AccentTeal.copy(alpha = 0.25f)
                            )
                            drawPath(
                                path = statPath,
                                color = AccentTeal,
                                style = Stroke(width = 2.5.dp.toPx())
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val statList = listOf(
                            Triple("STR (Discipline)", strStat, "Focus Sessions completing"),
                            Triple("AGI (Consistency)", agiStat, "Habit completion streaks"),
                            Triple("INT (Academic)", intStat, "Homework tasks completed"),
                            Triple("VIT (Wellbeing)", vitStat, "Journaling & Reflection"),
                            Triple("LUK (Inspiration)", lukStat, "Daily focus luck factor")
                        )

                        statList.forEach { (label, value, desc) ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(viewModel.themeGlassFill)
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = label,
                                        color = viewModel.themeText,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                    )
                                    Text(
                                        text = "$value/100",
                                        color = AccentTeal,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = desc,
                                    color = viewModel.themeTextSecondary,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


fun getD3ChartHtml(data: List<Map<String, Any>>, viewModel: FocusViewModel, accentColor: Color): String {
    val accentHex = String.format("#%06X", 0xFFFFFF and accentColor.value.toInt())
    val isDark = viewModel.isDarkMode
    val textColorHex = if (isDark) "#FFFFFF" else "#111827"
    val textSecondaryColorHex = if (isDark) "#9CA3AF" else "#4B5563"
    val gridColorHex = if (isDark) "#374151" else "#E5E7EB"
    val bgColorHex = if (isDark) "#0A0A12" else "#FFFFFF"
    val tooltipBgHex = if (isDark) "#1F2937" else "#FFFFFF"
    val tooltipBorderHex = if (isDark) "#374151" else "#D1D5DB"

    val jsonArray = org.json.JSONArray()
    for (item in data) {
        val obj = org.json.JSONObject()
        obj.put("date", item["date"])
        obj.put("value", item["value"])
        jsonArray.put(obj)
    }
    val jsonString = jsonArray.toString()

    return """
        <!DOCTYPE html>
        <html>
        <head>
          <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
          <script src="https://d3js.org/d3.v7.min.js"></script>
          <style>
            body {
              margin: 0;
              padding: 0;
              background-color: transparent;
              color: $textColorHex;
              font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
              overflow: hidden;
              user-select: none;
            }
            #chart {
              width: 100%;
              height: 180px;
            }
            .line {
              fill: none;
              stroke: $accentHex;
              stroke-width: 3;
              stroke-linecap: round;
            }
            .area {
              fill: url(#area-gradient);
            }
            .grid line {
              stroke: $gridColorHex;
              stroke-opacity: 0.25;
              shape-rendering: crispEdges;
            }
            .grid path {
              stroke-width: 0;
            }
            .axis-label {
              font-size: 9px;
              fill: $textSecondaryColorHex;
              font-weight: 500;
            }
            .domain {
              stroke: $gridColorHex;
              stroke-opacity: 0.3;
            }
            .tick line {
              stroke: $gridColorHex;
              stroke-opacity: 0.3;
            }
            .tooltip {
              position: absolute;
              background: $tooltipBgHex;
              border: 1px solid $tooltipBorderHex;
              padding: 4px 8px;
              border-radius: 6px;
              font-size: 10px;
              font-weight: bold;
              color: $textColorHex;
              pointer-events: none;
              opacity: 0;
              transition: opacity 0.1s ease-in-out;
              box-shadow: 0 2px 8px rgba(0,0,0,0.15);
            }
            .dot {
              fill: $accentHex;
              stroke: $bgColorHex;
              stroke-width: 1.5px;
            }
          </style>
        </head>
        <body>
          <div id="chart"></div>
          <div id="tooltip" class="tooltip"></div>

          <script>
            try {
                const data = $jsonString;
                
                const margin = {top: 10, right: 10, bottom: 25, left: 30};
                const width = document.getElementById('chart').clientWidth;
                const height = 180;
                const chartWidth = width - margin.left - margin.right;
                const chartHeight = height - margin.top - margin.bottom;

                const svg = d3.select("#chart")
                  .append("svg")
                  .attr("width", width)
                  .attr("height", height)
                  .append("g")
                  .attr("transform", `translate(${'$'}{margin.left},${'$'}{margin.top})`);

                const defs = svg.append("defs");
                
                const areaGradient = defs.append("linearGradient")
                  .attr("id", "area-gradient")
                  .attr("x1", "0%")
                  .attr("y1", "0%")
                  .attr("x2", "0%")
                  .attr("y2", "100%");
                  
                areaGradient.append("stop")
                  .attr("offset", "0%")
                  .attr("stop-color", "$accentHex")
                  .attr("stop-opacity", 0.35);
                  
                areaGradient.append("stop")
                  .attr("offset", "100%")
                  .attr("stop-color", "$accentHex")
                  .attr("stop-opacity", 0.0);

                const x = d3.scalePoint()
                  .domain(data.map(d => d.date))
                  .range([0, chartWidth])
                  .padding(0.4);

                const maxVal = d3.max(data, d => d.value) || 10;
                const y = d3.scaleLinear()
                  .domain([0, maxVal * 1.15])
                  .range([chartHeight, 0]);

                svg.append("g")
                  .attr("class", "grid")
                  .call(d3.axisLeft(y)
                    .ticks(4)
                    .tickSize(-chartWidth)
                    .tickFormat("")
                  );

                const area = d3.area()
                  .x(d => x(d.date))
                  .y0(chartHeight)
                  .y1(d => y(d.value))
                  .curve(d3.curveMonotoneX);

                svg.append("path")
                  .datum(data)
                  .attr("class", "area")
                  .attr("d", area);

                const line = d3.line()
                  .x(d => x(d.date))
                  .y(d => y(d.value))
                  .curve(d3.curveMonotoneX);

                svg.append("path")
                  .datum(data)
                  .attr("class", "line")
                  .attr("d", line);

                const totalPoints = data.length;
                const tickInterval = Math.max(1, Math.floor(totalPoints / 5));
                const xTicks = data.filter((d, i) => i % tickInterval === 0 || i === totalPoints - 1).map(d => d.date);

                svg.append("g")
                  .attr("transform", `translate(0,${'$'}{chartHeight})`)
                  .call(d3.axisBottom(x)
                    .tickValues(xTicks)
                  )
                  .selectAll("text")
                  .attr("class", "axis-label")
                  .attr("dy", "10px");

                svg.append("g")
                  .call(d3.axisLeft(y).ticks(4))
                  .selectAll("text")
                  .attr("class", "axis-label");

                const tooltip = d3.select("#tooltip");

                svg.selectAll(".interaction-dot")
                  .data(data.filter(d => d.value > 0))
                  .enter()
                  .append("circle")
                  .attr("class", "dot")
                  .attr("cx", d => x(d.date))
                  .attr("cy", d => y(d.value))
                  .attr("r", 3.5)
                  .on("mouseover", function(event, d) {
                    d3.select(this).transition().duration(80).attr("r", 5.5);
                    tooltip.style("opacity", 1)
                      .html(`Date: ${'$'}{d.date}<br/>Focus: ${'$'}{d.value}m`)
                      .style("left", (event.clientX + 8) + "px")
                      .style("top", (event.clientY - 30) + "px");
                  })
                  .on("mousemove", function(event) {
                    tooltip.style("left", (event.clientX + 8) + "px")
                      .style("top", (event.clientY - 30) + "px");
                  })
                  .on("mouseout", function() {
                    d3.select(this).transition().duration(80).attr("r", 3.5);
                    tooltip.style("opacity", 0);
                  });
            } catch (err) {
                document.body.innerHTML = "<div style='color:red;padding:10px;font-size:12px;'>Error rendering chart: " + err.message + "</div>";
            }
          </script>
        </body>
        </html>
    """.trimIndent()
}

@Composable
fun DashboardErrorBoundary(
    viewModel: FocusViewModel,
    content: @Composable () -> Unit
) {
    val errorMsg = viewModel.dashboardError

    if (errorMsg != null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(viewModel.themeBg)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            GlassSurface(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0x26FF9800)
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .border(
                        width = 1.dp,
                        color = Color(0x40FF9800),
                        shape = RoundedCornerShape(24.dp)
                    )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Error Boundary Catch",
                        tint = Color(0xFFFF9800),
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Dashboard Shield Intercepted an Error",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "A rendering, canvas draw, or custom physics animation threw an exception, but your Focus Shield intercepted it to prevent a crash. All your session progress remains safe!",
                        color = Color(0xFFB3B0CD),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    com.example.ui.components.LiquidNotification(
                        title = "Physics / Canvas Error Intercepted",
                        message = errorMsg,
                        time = "Just now",
                        icon = { Icon(Icons.Default.Warning, contentDescription = "Error", tint = Color(0xFFFF8888)) }
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    com.example.ui.components.GlassButton(
                        onClick = {
                            viewModel.clearDashboardError()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF9800),
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Autorenew,
                                contentDescription = "Recover",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Re-initialize Dashboard",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    } else {
        content()
    }
}

// ==========================================
// LUMINA GLASSMORPHISM DESIGN SYSTEM CARDS
// ==========================================

@Composable
fun LuminaHeroSection(
    viewModel: com.example.data.FocusViewModel,
    displayName: String,
    accentColor: Color,
    onStartFocus: () -> Unit
) {
    val calendar = java.util.Calendar.getInstance()
    val dayFormat = java.text.SimpleDateFormat("EEEE", java.util.Locale.getDefault())
    val timeFormat = java.text.SimpleDateFormat("H:mm", java.util.Locale.getDefault())
    val dayOfWeek = dayFormat.format(calendar.time)
    val timeOfDay = timeFormat.format(calendar.time)

    var rescheduleActive by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(viewModel.themeGlassFill)
            .border(1.dp, viewModel.themeGlassBorder, RoundedCornerShape(32.dp))
            .padding(28.dp)
    ) {
        // Soft glowing backlight neon sphere behind the heading
        Box(
            modifier = Modifier
                .size(160.dp)
                .offset(x = (-30).dp, y = (-20).dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.12f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column {
            Text(
                text = "$dayOfWeek · $timeOfDay".uppercase(),
                color = viewModel.themeTextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            
            Spacer(modifier = Modifier.height(10.dp))
            
            Text(
                text = "Good morning,\n$displayName.",
                color = viewModel.themeText,
                fontSize = 34.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 42.sp,
                letterSpacing = (-1).sp
            )
            
            Spacer(modifier = Modifier.height(14.dp))
            
            Text(
                text = if (rescheduleActive) {
                    "Your day is now optimized. Walk with your phone free at 5pm to recharge."
                } else {
                    "You have 3 deep sessions planned and a quiet hour at 2pm. Let's protect it."
                },
                color = viewModel.themeTextSecondary,
                fontSize = 14.sp,
                lineHeight = 22.sp
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Primary Start Focus Button with ripple
                com.example.ui.components.GlassButton(
                    onClick = onStartFocus,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accentColor,
                        contentColor = if (viewModel.isDarkMode) Color.Black else Color.White
                    ),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                    modifier = Modifier.weight(1.2f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Start Focus",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Start focus",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Reschedule Day Button
                com.example.ui.components.GlassButton(
                    onClick = { rescheduleActive = !rescheduleActive },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.07f),
                        contentColor = viewModel.themeText
                    ),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, viewModel.themeGlassBorder, RoundedCornerShape(16.dp))
                ) {
                    Text(
                        text = if (rescheduleActive) "Reset Day" else "Reschedule day",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun LuminaNowPlayingCard(
    viewModel: com.example.data.FocusViewModel,
    accentColor: Color
) {
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    val soundList = listOf("Rain", "Forest", "Cafe", "Binaural", "White Noise", "Lo-Fi Beats", "Brown Noise", "Comfort Chain")
    
    // Simulating progress
    var simulatedProgress by remember { mutableStateOf(84) } // 1m 24s out of 3m 42s (222 seconds total)
    val isPlaying = viewModel.isWhiteNoisePlayingAnytime

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (true) {
                kotlinx.coroutines.delay(1000)
                simulatedProgress = (simulatedProgress + 1) % 222
            }
        }
    }

    val minutesLeft = simulatedProgress / 60
    val secondsLeft = simulatedProgress % 60
    val timeLabel = String.format("%d:%02d", minutesLeft, secondsLeft)

    GlassSurface(
        modifier = Modifier.fillMaxWidth(),
        fillColor = viewModel.themeGlassFill,
        borderHighlight = viewModel.themeGlassBorder,
        contentPadding = PaddingValues(20.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Album Cover (Coil mock or nice canvas)
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(accentColor.copy(alpha = 0.8f), Color(0xFFFF7AB6))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "Cover Art",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(14.dp))
                    
                    Column {
                        val displayTrackName = if (viewModel.selectedAmbientSound == "Comfort Chain") "Comfort Chain" else if (isPlaying) "${viewModel.selectedAmbientSound} Synth" else "Ambient Soundscape"
                        val displayArtist = if (viewModel.selectedAmbientSound == "Comfort Chain") "Instupendo" else "Lumina — Focus Refined"
                        Text(
                            text = displayTrackName,
                            color = viewModel.themeText,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = displayArtist,
                            color = viewModel.themeTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                // Exquisite Real-Time Audio Equalizer Animation
                Row(
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier.height(28.dp)
                ) {
                    repeat(5) { index ->
                        val infiniteTransition = rememberInfiniteTransition(label = "eq_bar_$index")
                        val heightMultiplier by if (isPlaying) {
                            infiniteTransition.animateFloat(
                                initialValue = 0.2f,
                                targetValue = 1f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(
                                        durationMillis = listOf(400, 650, 480, 580, 720)[index],
                                        easing = androidx.compose.animation.core.FastOutSlowInEasing
                                    ),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "eq_anim_$index"
                            )
                        } else {
                            remember { mutableStateOf(0.15f) }
                        }

                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height((heightMultiplier * 22).dp)
                                .clip(RoundedCornerShape(100.dp))
                                .background(accentColor)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Progress Slider Bar
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = timeLabel, color = viewModel.themeTextSecondary, fontSize = 11.sp)
                    Text(text = "3:42", color = viewModel.themeTextSecondary, fontSize = 11.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                // Progress Line
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(100.dp))
                        .background(viewModel.themeBorder)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = simulatedProgress.toFloat() / 222f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(100.dp))
                            .background(accentColor)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Music Controls Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Previous Button
                com.example.ui.components.GlassIconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        val currentIndex = soundList.indexOf(viewModel.selectedAmbientSound)
                        val prevIndex = if (currentIndex <= 0) soundList.size - 1 else currentIndex - 1
                        viewModel.selectAmbientSound(soundList[prevIndex])
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Previous Track",
                        tint = viewModel.themeText,
                        modifier = Modifier
                            .size(28.dp)
                            .graphicsLayer { rotationZ = 180f }
                    )
                }

                Spacer(modifier = Modifier.width(18.dp))

                // Play/Pause Button
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(accentColor)
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.toggleWhiteNoisePlayingAnytime()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = if (viewModel.isDarkMode) Color.Black else Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(18.dp))

                // Next Button
                com.example.ui.components.GlassIconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        val currentIndex = soundList.indexOf(viewModel.selectedAmbientSound)
                        val nextIndex = (currentIndex + 1) % soundList.size
                        viewModel.selectAmbientSound(soundList[nextIndex])
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Next Track",
                        tint = viewModel.themeText,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun LuminaFlowChecklistCard(
    viewModel: com.example.data.FocusViewModel,
    accentColor: Color
) {
    GlassSurface(
        modifier = Modifier.fillMaxWidth(),
        fillColor = viewModel.themeGlassFill,
        borderHighlight = viewModel.themeGlassBorder,
        contentPadding = PaddingValues(20.dp)
    ) {
        com.example.ui.components.TaskFlowList(
            modifier = Modifier.fillMaxWidth(),
            accentColor = accentColor,
            title = "Today's Flow"
        )
    }
}

@Composable
fun LuminaFocusProgressRingCard(
    viewModel: com.example.data.FocusViewModel,
    accentColor: Color
) {
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    
    // Connect to actual viewmodel timer state
    val isTimerRunning = viewModel.focusSessionState == com.example.data.FocusSessionState.RUNNING || 
                         viewModel.focusSessionState == com.example.data.FocusSessionState.PAUSED
                         
    val timerTotal = viewModel.timerTotalMinutes * 60f
    val timerRemaining = viewModel.timerSecondsLeft.toFloat()
    
    // Default to the daily focus display if no timer is running
    var focusMinutesDone = viewModel.getFocusTimeHours() * 60f
    val focusMinutesGoal = 240f // 4 hours daily goal

    // Dynamic color based on intensity
    val intensityColor = if (isTimerRunning) {
        val intensity = 1f - (timerRemaining / timerTotal)
        Color(
            red = (accentColor.red * (1f - intensity) + 1f * intensity).coerceIn(0f, 1f),
            green = (accentColor.green * (1f - intensity) + 0.3f * intensity).coerceIn(0f, 1f),
            blue = (accentColor.blue * (1f - intensity) + 0.3f * intensity).coerceIn(0f, 1f),
            alpha = 1f
        )
    } else accentColor

    val targetProgress = if (isTimerRunning) {
        if (timerTotal > 0) (timerTotal - timerRemaining) / timerTotal else 0f
    } else {
        (focusMinutesDone / focusMinutesGoal).toFloat()
    }
    
    val animatedPercent by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessLow)
    )
    
    val displayPercent = (animatedPercent * 100).toInt().coerceIn(0, 100)

    GlassSurface(
        modifier = Modifier.fillMaxWidth(),
        fillColor = viewModel.themeGlassFill,
        borderHighlight = viewModel.themeGlassBorder,
        contentPadding = PaddingValues(20.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isTimerRunning) "Active Session" else "Daily Focus",
                    color = viewModel.themeText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = if (isTimerRunning) viewModel.timerLabel else "Goal: 4 hr",
                    color = viewModel.themeTextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Box(
                modifier = Modifier.size(140.dp),
                contentAlignment = Alignment.Center
            ) {
                // Beautiful Custom GlassProgressRing with glow and backdrop blur
                com.example.ui.components.GlassProgressRing(
                    progress = animatedPercent,
                    accentColor = intensityColor,
                    strokeWidth = 12.dp,
                    modifier = Modifier.fillMaxSize()
                )

                // Inner Text labels
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$displayPercent%",
                        color = viewModel.themeText,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = if (isTimerRunning) "completed" else "of goal",
                        color = viewModel.themeTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            
            if (isTimerRunning) {
                 val m = viewModel.timerSecondsLeft / 60
                 val s = viewModel.timerSecondsLeft % 60
                 Text(
                    text = String.format("%02d:%02d", m, s),
                    color = viewModel.themeText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Text(
                    text = "${focusMinutesDone.toInt()}m / 240m",
                    color = viewModel.themeText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun LuminaTrackInfoDisplay(
    viewModel: com.example.data.FocusViewModel,
    accentColor: Color
) {
    val activeSound = viewModel.selectedAmbientSound
    val isPlaying = viewModel.isWhiteNoisePlayingAnytime

    val trackName = if (activeSound == "Comfort Chain") "Comfort Chain" else "$activeSound Synth"
    val artistName = if (activeSound == "Comfort Chain") "Instupendo" else "Lumina Focus"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(viewModel.themeGlassFill.copy(alpha = 0.45f))
            .border(1.dp, viewModel.themeGlassBorder, RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Track visualizer pulsing circle
                    val infiniteTransition = rememberInfiniteTransition(label = "track_pulse")
                    val pulseAlpha by if (isPlaying) {
                        infiniteTransition.animateFloat(
                            initialValue = 0.3f,
                            targetValue = 1.0f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1200, easing = androidx.compose.animation.core.FastOutSlowInEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "pulse_anim"
                        )
                    } else {
                        remember { mutableStateOf(0.4f) }
                    }

                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = if (isPlaying) 0.15f else 0.05f))
                            .border(1.dp, accentColor.copy(alpha = pulseAlpha * 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = "Playback State Icon",
                            tint = if (isPlaying) accentColor else viewModel.themeTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = trackName,
                            color = viewModel.themeText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            text = artistName,
                            color = viewModel.themeTextSecondary,
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }
                }

                // Play/Pause pill indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(if (isPlaying) accentColor.copy(alpha = 0.15f) else viewModel.themeGlassBorder.copy(alpha = 0.25f))
                        .clickable { viewModel.toggleWhiteNoisePlayingAnytime() }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (isPlaying) accentColor else viewModel.themeTextSecondary)
                    )
                    Text(
                        text = if (isPlaying) "Playing" else "Paused",
                        color = if (isPlaying) accentColor else viewModel.themeTextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(10.dp))
            
            // Subtle, D3.js-style real-time audio visualizer
            D3StyleAudioVisualizer(
                viewModel = viewModel,
                accentColor = accentColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(8.dp))
            )

            if (activeSound == "Comfort Chain") {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(viewModel.themeGlassFill.copy(alpha = 0.2f))
                        .border(1.dp, viewModel.themeGlassBorder.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = if (viewModel.comfortChainVolume > 0.6f) Icons.Default.VolumeUp else if (viewModel.comfortChainVolume > 0f) Icons.Default.VolumeDown else Icons.Default.VolumeOff,
                        contentDescription = "Comfort Chain Volume Icon",
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Volume",
                        color = viewModel.themeTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Slider(
                        value = viewModel.comfortChainVolume,
                        onValueChange = { viewModel.setComfortChainVolumeValue(it) },
                        valueRange = 0f..1f,
                        colors = SliderDefaults.colors(
                            activeTrackColor = accentColor,
                            inactiveTrackColor = viewModel.themeGlassBorder.copy(alpha = 0.3f),
                            thumbColor = accentColor,
                            activeTickColor = Color.Transparent,
                            inactiveTickColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(18.dp)
                            .testTag("comfort_chain_volume_slider")
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${(viewModel.comfortChainVolume * 100).toInt()}%",
                        color = viewModel.themeText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(32.dp),
                        textAlign = TextAlign.End
                    )
                }
            }
        }
    }
}

@Composable
fun D3StyleAudioVisualizer(
    viewModel: com.example.data.FocusViewModel,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val visualizerDataState = viewModel.whiteNoiseVisualizerFlow.collectAsState()
    val visualizerData = visualizerDataState.value
    
    // Smooth each of the 8 bins using animateFloatAsState
    val animatedBins = List(8) { index ->
        val targetVal = if (index < visualizerData.size) visualizerData[index] else 0f
        animateFloatAsState(
            targetValue = targetVal,
            animationSpec = spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessLow),
            label = "vis_bin_$index"
        )
    }
    
    val isPlaying = viewModel.isWhiteNoisePlayingAnytime

    androidx.compose.foundation.Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val pointsCount = 10 // 8 bins + 2 padding anchors
        
        if (width <= 0f || height <= 0f) return@Canvas
        
        val dx = width / (pointsCount - 1)
        val splinePoints = ArrayList<androidx.compose.ui.geometry.Offset>()
        
        // Left padding anchor anchored at vertical middle
        splinePoints.add(androidx.compose.ui.geometry.Offset(0f, height * 0.7f))
        
        for (i in 0 until 8) {
            val amplitude = animatedBins[i].value
            // Add ultra subtle micro-fluctuation to appear organic
            val activeFluctuation = if (isPlaying) {
                (Math.sin(System.currentTimeMillis() * 0.003 + i * 1.5).toFloat() * 0.03f)
            } else {
                0f
            }
            val normValue = (amplitude + activeFluctuation).coerceIn(0f, 1f)
            val py = height * 0.7f - (normValue * height * 0.65f)
            splinePoints.add(androidx.compose.ui.geometry.Offset((i + 1) * dx, py))
        }
        
        // Right padding anchor anchored at vertical middle
        splinePoints.add(androidx.compose.ui.geometry.Offset(width, height * 0.7f))
        
        val path = androidx.compose.ui.graphics.Path()
        val fillPath = androidx.compose.ui.graphics.Path()
        
        path.moveTo(splinePoints[0].x, splinePoints[0].y)
        fillPath.moveTo(splinePoints[0].x, height)
        fillPath.lineTo(splinePoints[0].x, splinePoints[0].y)
        
        // D3-style basis spline generation using cubic Beziers
        for (i in 0 until splinePoints.size - 1) {
            val p0 = splinePoints[i]
            val p1 = splinePoints[i + 1]
            val controlX1 = p0.x + dx / 2f
            val controlY1 = p0.y
            val controlX2 = p1.x - dx / 2f
            val controlY2 = p1.y
            
            path.cubicTo(controlX1, controlY1, controlX2, controlY2, p1.x, p1.y)
            fillPath.cubicTo(controlX1, controlY1, controlX2, controlY2, p1.x, p1.y)
        }
        
        fillPath.lineTo(width, height)
        fillPath.close()
        
        // Glassmorphic neon glowing area fill
        val brush = androidx.compose.ui.graphics.Brush.verticalGradient(
            colors = listOf(
                accentColor.copy(alpha = 0.35f),
                accentColor.copy(alpha = 0.05f),
                Color.Transparent
            ),
            startY = height * 0.1f,
            endY = height
        )
        
        drawPath(fillPath, brush = brush)
        
        // High-contrast clean path stroke
        drawPath(
            path = path,
            color = accentColor.copy(alpha = 0.85f),
            style = androidx.compose.ui.graphics.drawscope.Stroke(
                width = 2.dp.toPx(),
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
        )
        
        // Delicate peak anchor dots
        if (isPlaying) {
            for (i in 1..8) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.45f),
                    radius = 2.dp.toPx(),
                    center = splinePoints[i]
                )
            }
        }
    }
}

@Composable
fun LuminaAmbientSoundCard(
    viewModel: com.example.data.FocusViewModel,
    accentColor: Color
) {
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    val activeSound = viewModel.selectedAmbientSound
    val isPlaying = viewModel.isWhiteNoisePlayingAnytime

    GlassSurface(
        modifier = Modifier.fillMaxWidth(),
        fillColor = viewModel.themeGlassFill,
        borderHighlight = viewModel.themeGlassBorder,
        contentPadding = PaddingValues(20.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Ambient Sound",
                        color = viewModel.themeText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isPlaying) "Active · $activeSound" else "Standby",
                        color = viewModel.themeTextSecondary,
                        fontSize = 11.sp
                    )
                }

                // Custom iOS-style Switch Toggle
                Box(
                    modifier = Modifier
                        .width(44.dp)
                        .height(24.dp)
                        .clip(RoundedCornerShape(100.dp))
                        .background(if (isPlaying) accentColor else viewModel.themeBorder)
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.toggleWhiteNoisePlayingAnytime()
                        }
                        .padding(2.dp),
                    contentAlignment = if (isPlaying) Alignment.CenterEnd else Alignment.CenterStart
                ) {
                    // Switch Thumb
                    val thumbOffset by animateDpAsState(
                        targetValue = if (isPlaying) 20.dp else 0.dp,
                        animationSpec = spring(stiffness = Spring.StiffnessHigh)
                    )
                    Box(
                        modifier = Modifier
                            .offset(x = thumbOffset)
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(if (viewModel.isDarkMode && isPlaying) Color.Black else Color.White)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LuminaTrackInfoDisplay(viewModel = viewModel, accentColor = accentColor)

            Spacer(modifier = Modifier.height(16.dp))

            // Rain, Waves (Lo-Fi beats!), Forest buttons grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "Rain" to "🌧️ Rain",
                    "Lo-Fi Beats" to "🎵 Lo-Fi",
                    "Forest" to "🌲 Forest",
                    "Comfort Chain" to "🔗 Comfort"
                ).forEach { (soundKey, soundLabel) ->
                    val isSelected = activeSound == soundKey
                    
                    com.example.ui.components.GlassButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.selectAmbientSound(soundKey)
                            if (!isPlaying) {
                                viewModel.toggleWhiteNoisePlayingAnytime()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) accentColor else Color.White.copy(alpha = 0.05f),
                            contentColor = if (isSelected) {
                                if (viewModel.isDarkMode) Color.Black else Color.White
                            } else viewModel.themeText
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) accentColor else viewModel.themeGlassBorder,
                                shape = RoundedCornerShape(14.dp)
                            ),
                        contentPadding = PaddingValues(vertical = 10.dp)
                    ) {
                        Text(
                            text = soundLabel,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LuminaQuoteCard(
    viewModel: com.example.data.FocusViewModel
) {
    GlassSurface(
        modifier = Modifier.fillMaxWidth(),
        fillColor = viewModel.themeGlassFill,
        borderHighlight = viewModel.themeGlassBorder,
        contentPadding = PaddingValues(24.dp)
    ) {
        Column {
            Text(
                text = "“",
                color = Color(0xFFC9B8FF).copy(alpha = 0.7f),
                fontSize = 44.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.height(24.dp)
            )
            
            Text(
                text = "The quieter you become, the more you can hear.",
                color = viewModel.themeText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                lineHeight = 22.sp
            )
            
            Spacer(modifier = Modifier.height(10.dp))
            
            Text(
                text = "— Rumi · daily card",
                color = viewModel.themeTextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun LuminaScrollingMarqueeFooter(
    viewModel: com.example.data.FocusViewModel
) {
    val scrollText = "DEEP WORK · 90 MIN   ·   INBOX ZERO AT 4PM   ·   NO MEETINGS BEFORE 11   ·   WALK WITHOUT YOUR PHONE   ·   PROTECT YOUR FOCUS   ·   "
    val infiniteTransition = rememberInfiniteTransition(label = "marquee")
    val translationX by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(28000, easing = androidx.compose.animation.core.LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "marquee_anim"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(34.dp)
            .background(Color.White.copy(alpha = 0.03f))
            .border(1.dp, viewModel.themeGlassBorder.copy(alpha = 0.1f), RoundedCornerShape(0.dp)),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .offset(x = translationX.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(3) {
                Text(
                    text = scrollText,
                    color = viewModel.themeTextSecondary.copy(alpha = 0.35f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}



@Composable
fun LuminaInsightsCard(
    viewModel: com.example.data.FocusViewModel,
    accentColor: Color
) {
    var insights by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val sessions by viewModel.sessions.collectAsState()

    GlassSurface(
        modifier = Modifier.fillMaxWidth(),
        fillColor = viewModel.themeGlassFill,
        borderHighlight = viewModel.themeGlassBorder,
        contentPadding = PaddingValues(24.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI Insights",
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Productivity Insights",
                        color = viewModel.themeText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                if (!isLoading && insights == null) {
                    com.example.ui.components.GlassButton(
                        onClick = {
                            if (sessions.isEmpty()) {
                                insights = "No focus sessions logged yet to analyze."
                                return@GlassButton
                            }
                            isLoading = true
                            
                            // Format the sessions into a string for Gemini
                            val oneWeekAgo = System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000
                            val recentSessions = sessions.filter { it.timestamp > oneWeekAgo }
                            
                            val sessionData = recentSessions.joinToString(separator = "\n") {
                                val date = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date(it.timestamp))
                                "- [${date}] ${it.label} (${it.category}): ${it.focusTimeMinutes} min focus / ${it.durationMinutes} min total"
                            }
                            
                            val prompt = "Analyze these focus session logs from the past 7 days and provide a highly concise, encouraging productivity summary (2-3 short bullet points) with one tailored actionable suggestion to improve focus. Avoid generic advice.\n\nLogs:\n$sessionData"
                            
                            viewModel.analyzeContentGeneral(sessionData, prompt) { result ->
                                insights = result
                                isLoading = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = viewModel.themeSurface),
                        modifier = Modifier.height(32.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Analyze",
                            fontSize = 12.sp,
                            color = viewModel.themeText,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (isLoading) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = accentColor,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Gemini is analyzing your focus patterns...",
                        color = viewModel.themeTextSecondary,
                        fontSize = 13.sp,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            } else if (insights != null) {
                Text(
                    text = insights ?: "",
                    color = viewModel.themeText,
                    fontSize = 14.sp,
                    lineHeight = 22.sp
                )
            } else {
                Text(
                    text = "Tap 'Analyze' to get personalized productivity insights and suggestions based on your recent focus sessions.",
                    color = viewModel.themeTextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 20.sp
                )
            }
        }
    }
}
