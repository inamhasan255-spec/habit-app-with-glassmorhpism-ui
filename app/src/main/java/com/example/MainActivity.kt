package com.example

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.os.Bundle
import android.os.Build
import androidx.core.app.ActivityCompat
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.shadow
import com.example.ui.components.FloatingAmbientSoundscapeController
import com.example.ui.theme.softGlassShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.platform.LocalConfiguration
import android.content.res.Configuration
import androidx.compose.ui.graphics.Brush
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import com.example.data.*
import com.example.ui.components.*
import com.example.ui.screens.*
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

class MainActivity : ComponentActivity() {
    private val viewModel: FocusViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Create native focus notification channels
        FocusNotificationManager.createNotificationChannels(this)

        // Request POST_NOTIFICATIONS permission for Android 13+ (API 33+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionCheck = ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    101
                )
            }
        }

        handleIntent(intent)

        setContent {
            com.example.ui.theme.LuminaThemeProvider(isDarkMode = viewModel.isDarkMode, glassOpacity = viewModel.glassOpacity) {
                FocusCubeApp(viewModel)
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    override fun onStop() {
        super.onStop()
        viewModel.onAppBackgrounded()
    }

    private fun handleIntent(intent: android.content.Intent?) {
        if (intent?.action == "START_FOCUS_SESSION") {
            viewModel.startFocusSession("Widget Session", 25)
        } else if (intent?.getBooleanExtra("blocked_app_triggered", false) == true) {
            val packageName = intent.getStringExtra("blocked_package_name") ?: ""
            val appName = intent.getStringExtra("blocked_app_name") ?: "Restricted App"
            viewModel.triggerFocusBlockOverlay(packageName, appName)
        }
    }
}

@Composable
fun FocusCubeApp(viewModel: FocusViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val accentColor = Color(android.graphics.Color.parseColor(viewModel.accentColorHex))

    var showLaunchPermissionsDialog by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(currentScreen) {
        if (currentScreen == AppScreen.MAIN || currentScreen == AppScreen.AUTH) {
            val hasUsage = try {
                val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as android.app.AppOpsManager
                val mode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    appOps.unsafeCheckOpNoThrow(
                        android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
                        android.os.Process.myUid(),
                        context.packageName
                    )
                } else {
                    appOps.checkOpNoThrow(
                        android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
                        android.os.Process.myUid(),
                        context.packageName
                    )
                }
                mode == android.app.AppOpsManager.MODE_ALLOWED
            } catch (e: Exception) {
                false
            }

            val hasNotifications = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                androidx.core.content.ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            } else {
                true
            }

            if (!hasUsage || !hasNotifications) {
                val prefs = context.getSharedPreferences("focus_cube_prefs", Context.MODE_PRIVATE)
                val alreadyAsked = prefs.getBoolean("permission_asked_on_opening", false)
                if (!alreadyAsked) {
                    showLaunchPermissionsDialog = true
                }
            }
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {        if (viewModel.isBackgroundEnabled) {
            if (viewModel.backgroundImageUrl.isNotEmpty()) {
                coil.compose.AsyncImage(
                    model = viewModel.backgroundImageUrl,
                    contentDescription = "Background",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            if (viewModel.isDarkMode) androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.5f)
                            else androidx.compose.ui.graphics.Color.White.copy(alpha = 0.2f)
                        )
                )
            } else {
                // Immersive premium glassmorphism background stage with animated floating color blobs and tinting layer
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .glassmorphismBackgroundEffect(isDarkMode = viewModel.isDarkMode)
                ) {
                    LuminaStageBackground(isDarkMode = viewModel.isDarkMode)
                }
            }
        } else {
            // Pure clean solid background depending on theme choice (white or dark)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(viewModel.themeBg)
            )
        }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Transparent
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    val springSpec = spring<Float>(
                        dampingRatio = 0.82f,
                        stiffness = 380f
                    )
                    
                    fadeIn(animationSpec = tween(300)) +
                        scaleIn(
                            initialScale = 0.94f,
                            animationSpec = springSpec
                        ) togetherWith
                        fadeOut(animationSpec = tween(250)) +
                        scaleOut(
                            targetScale = 0.98f,
                            animationSpec = tween(250)
                        )
                },
                label = "screen_flow"
            ) { screen ->
                when (screen) {
                    AppScreen.SPLASH -> SplashScreen(viewModel)
                    AppScreen.ONBOARDING -> OnboardingScreen(viewModel)
                    AppScreen.AUTH -> AuthScreen(viewModel)
                    AppScreen.MAIN -> MainAppContainer(viewModel, accentColor)
                    AppScreen.FOCUS_ACTIVE -> FocusActiveScreen(viewModel)
                    AppScreen.FOCUS_COMPLETE -> FocusCompleteScreen(viewModel)
                }
            }
        }

        // Floating Ambient Soundscape Controller (Unobtrusive bottom-right positioning)
        val showFloatingController = currentScreen != AppScreen.SPLASH && currentScreen != AppScreen.AUTH && currentScreen != AppScreen.ONBOARDING
        
        AnimatedVisibility(
            visible = showFloatingController,
            enter = fadeIn(animationSpec = tween(800, delayMillis = 300)) + 
                    slideInVertically(
                        initialOffsetY = { 80 }, 
                        animationSpec = spring(dampingRatio = 0.8f, stiffness = 200f)
                    ),
            exit = fadeOut(animationSpec = tween(400)) + slideOutVertically(targetOffsetY = { 80 }),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(bottom = 24.dp, end = 24.dp)
        ) {
            FloatingAmbientSoundscapeController(
                viewModel = viewModel
            )
        }

        // Full-screen focus blocker overlay on top of everything
        if (viewModel.activeBlockedAppTriggered) {
            BlockedAppOverlayScreen(viewModel)
        }

        if (showLaunchPermissionsDialog) {
            Dialog(
                onDismissRequest = { 
                    val prefs = context.getSharedPreferences("focus_cube_prefs", Context.MODE_PRIVATE)
                    prefs.edit().putBoolean("permission_asked_on_opening", true).apply()
                    showLaunchPermissionsDialog = false 
                },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .wrapContentHeight()
                        .softGlassShadow(
                            elevation = 16.dp,
                            shape = RoundedCornerShape(24.dp),
                            isDark = viewModel.isDarkMode
                        )
                        .clip(RoundedCornerShape(24.dp))
                        .background(viewModel.themeGlassFill)
                        .border(
                            width = 1.dp,
                            color = viewModel.themeGlassBorder,
                            shape = RoundedCornerShape(24.dp)
                        )
                        .padding(24.dp)
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Title
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = "Shield",
                                tint = accentColor,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Enable Focus Cube Guard",
                                color = viewModel.themeText,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }

                        // Text (content)
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = "To protect your deep work and send gentle reminders, Focus Cube needs the following permissions enabled on your device:",
                                color = viewModel.themeTextSecondary,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                            
                            // Notification Permission Item
                            Card(
                                colors = CardDefaults.cardColors(containerColor = viewModel.themeGlassFill.copy(alpha = 0.35f)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, viewModel.themeGlassBorder),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Notification",
                                        tint = accentColor,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "Gentle Reminders",
                                            color = viewModel.themeText,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Allows habit streak updates and active session timing notifications.",
                                            color = viewModel.themeTextSecondary,
                                            fontSize = 11.sp,
                                            lineHeight = 15.sp
                                        )
                                    }
                                }
                            }
                            
                            // Usage Stats Permission Item
                            Card(
                                colors = CardDefaults.cardColors(containerColor = viewModel.themeGlassFill.copy(alpha = 0.35f)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, viewModel.themeGlassBorder),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Shield,
                                        contentDescription = "App Blocking",
                                        tint = AccentPink,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "Focus Shield (App Blocker)",
                                            color = viewModel.themeText,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Required to detect and block distracting applications when focus timer is active.",
                                            color = viewModel.themeTextSecondary,
                                            fontSize = 11.sp,
                                            lineHeight = 15.sp
                                        )
                                    }
                                }
                            }

                            // Overlay Permission Item
                            Card(
                                colors = CardDefaults.cardColors(containerColor = viewModel.themeGlassFill.copy(alpha = 0.35f)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, viewModel.themeGlassBorder),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.DarkMode,
                                        contentDescription = "Overlay Blocking Screen",
                                        tint = AccentTeal,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "System Overlay (Immediate Interruption)",
                                            color = viewModel.themeText,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Displays a beautiful motivational lock screen immediately over blocked apps.",
                                            color = viewModel.themeTextSecondary,
                                            fontSize = 11.sp,
                                            lineHeight = 15.sp
                                        )
                                    }
                                }
                            }
                        }

                        // Buttons Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            com.example.ui.components.GlassButton(
                                variant = com.example.ui.components.GlassButtonVariant.Secondary, 
                                onClick = {
                                    val prefs = context.getSharedPreferences("focus_cube_prefs", Context.MODE_PRIVATE)
                                    prefs.edit().putBoolean("permission_asked_on_opening", true).apply()
                                    showLaunchPermissionsDialog = false
                                }
                            ) {
                                Text(
                                    text = "Maybe Later",
                                    color = viewModel.themeTextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(8.dp))

                            com.example.ui.components.GlassButton(
                                onClick = {
                                    val prefs = context.getSharedPreferences("focus_cube_prefs", Context.MODE_PRIVATE)
                                    prefs.edit().putBoolean("permission_asked_on_opening", true).apply()
                                    showLaunchPermissionsDialog = false
                                    
                                    // Trigger system permission flows
                                    // 1. Notification Permission request (standard)
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        ActivityCompat.requestPermissions(
                                            context as android.app.Activity,
                                            arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                                            101
                                        )
                                    }
                                    // 2. Direct user to Usage Access Settings
                                    try {
                                        val usageIntent = Intent(android.provider.Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                        }
                                        context.startActivity(usageIntent)
                                    } catch (e: Exception) {
                                        try {
                                            val generalIntent = Intent(android.provider.Settings.ACTION_SETTINGS).apply {
                                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                            }
                                            context.startActivity(generalIntent)
                                        } catch (ex: Exception) {
                                            ex.printStackTrace()
                                        }
                                    }

                                    // 3. Direct user to Draw Over Other Apps Settings (Overlay)
                                    try {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
                                            val overlayIntent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}")).apply {
                                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                            }
                                            context.startActivity(overlayIntent)
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = "Grant Now",
                                    color = if (viewModel.isDarkMode) Color.Black else Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SplashScreen(viewModel: FocusViewModel) {
    val infiniteTransition = rememberInfiniteTransition(label = "splash_spin")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spin"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(viewModel.themeBg),
        contentAlignment = Alignment.Center
    ) {
        MicroCubeBackground(viewModel)

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.size(160.dp),
                contentAlignment = Alignment.Center
            ) {
                RotatingBook3D(
                    bookSize = 135.dp,
                    accentColor = Color(android.graphics.Color.parseColor(viewModel.accentColorHex)),
                    viewModel = viewModel
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "Lumina",
                color = viewModel.themeText,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-1).sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "STACK YOUR MIND",
                color = Color(android.graphics.Color.parseColor(viewModel.accentColorHex)),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContainer(viewModel: FocusViewModel, accentColor: Color) {
    val currentTab by viewModel.currentTab.collectAsState()

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val isWideScreen = configuration.screenWidthDp >= 600 || isLandscape
    
    val galaxyVisible by viewModel.galaxyVisible.collectAsState()
    val galaxySpeed by viewModel.galaxyStarSpeed.collectAsState()
    val galaxyHue by viewModel.galaxyHueShift.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(viewModel.themeBg)) {
        androidx.compose.animation.AnimatedVisibility(
            visible = galaxyVisible,
            enter = androidx.compose.animation.fadeIn(
                animationSpec = androidx.compose.animation.core.spring(
                    dampingRatio = androidx.compose.animation.core.Spring.DampingRatioNoBouncy,
                    stiffness = androidx.compose.animation.core.Spring.StiffnessLow
                )
            ),
            exit = androidx.compose.animation.fadeOut(
                animationSpec = androidx.compose.animation.core.spring(
                    stiffness = androidx.compose.animation.core.Spring.StiffnessLow
                )
            )
        ) {
            com.example.ui.components.GalaxyBackground(
                isVisible = true,
                starSpeed = galaxySpeed,
                hueShift = galaxyHue,
                modifier = Modifier.fillMaxSize()
            )
        }

        Row(modifier = Modifier.fillMaxSize()) {
            if (isWideScreen) {
                CustomNavigationSidebar(
                    viewModel = viewModel,
                    currentTab = currentTab,
                    accentColor = accentColor,
                    onTabSelected = { viewModel.setTab(it) }
                )
            }
    
            Scaffold(
                topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(accentColor)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Lumina",
                                color = viewModel.themeText,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.5).sp
                            )
                        }
                    },
                    actions = {
                        // Dark Mode Toggle Button
                        com.example.ui.components.GlassIconButton(
                            onClick = { viewModel.toggleDarkMode() },
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Icon(
                                imageVector = if (viewModel.isDarkMode) {
                                    Icons.Default.LightMode
                                } else {
                                    Icons.Default.DarkMode
                                },
                                contentDescription = "Toggle Theme",
                                tint = accentColor
                            )
                        }

                        // Quick stats indicators
                        Box(
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(viewModel.themeGlassFill)
                                .border(1.dp, viewModel.themeGlassBorder, RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "${viewModel.getStreakCount()} day streak 🔥",
                                color = accentColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = viewModel.themeText
                    ),
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                        .softGlassShadow(elevation = 10.dp, shape = RoundedCornerShape(20.dp), isDark = viewModel.isDarkMode)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White.copy(alpha = 0.15f)) // Premium high-transparency white glassmorphism effect
                        .border(1.dp, Color.White.copy(alpha = 0.28f), RoundedCornerShape(20.dp))
                )
            },
            bottomBar = {
                if (!isWideScreen) {
                    CustomBottomNavigationBar(
                        viewModel = viewModel,
                        currentTab = currentTab,
                        accentColor = accentColor,
                        onTabSelected = { viewModel.setTab(it) }
                    )
                }
            },
            containerColor = Color.Transparent,
            modifier = Modifier.weight(1f)
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                MicroCubeBackground(viewModel)

                AnimatedContent(
                    targetState = currentTab,
                    transitionSpec = {
                        val springSpec = spring<Float>(
                            dampingRatio = 0.85f,
                            stiffness = 400f
                        )
                        
                        fadeIn(animationSpec = tween(250)) +
                            scaleIn(
                                initialScale = 0.95f,
                                animationSpec = springSpec
                            ) togetherWith
                            fadeOut(animationSpec = tween(200)) +
                            scaleOut(
                                targetScale = 0.98f,
                                animationSpec = tween(200)
                            )
                    },
                    label = "tab_transitions"
                ) { tab ->
                    when (tab) {
                        AppTab.HOME -> HomeDashboardScreen(viewModel)
                        AppTab.HABITS -> HabitsScreen(viewModel)
                        AppTab.SHIELD -> FocusShieldScreen(viewModel)
                        AppTab.STATS -> StatsScreen(viewModel)
                        AppTab.ACADEMIC -> com.example.ui.screens.TimetableScreen(viewModel)
                        AppTab.COMMUNITY -> com.example.ui.screens.CommunityScreen(viewModel)
                        AppTab.TIMETABLE -> com.focuscube.ui.screens.timetable.TimetableScreen(
                            viewModel = androidx.lifecycle.viewmodel.compose.viewModel<com.focuscube.ui.viewmodel.TimetableViewModel>(),
                            onStartFocus = { weekNumber ->
                                viewModel.startFocusSession("Focus Session", 25, "Deep Work", weekNumber)
                            }
                        )
                        AppTab.SETTINGS -> SettingsScreen(viewModel)
                    }
                }
            }
        }
    }
}
}

@Composable
fun CustomBottomNavigationBar(
    viewModel: FocusViewModel,
    currentTab: AppTab,
    accentColor: Color,
    onTabSelected: (AppTab) -> Unit
) {
    // Elegant floating bottom glassmorphism bar
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(if (viewModel.isDarkMode) Color(0xD912121F) else Color(0xF2FFFFFF))
            .border(1.dp, viewModel.themeGlassBorder, RoundedCornerShape(22.dp))
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf(
                Triple(AppTab.HOME, Icons.Default.Home, "home_tab"),
                Triple(AppTab.HABITS, Icons.Default.CheckCircle, "habits_tab"),
                Triple(AppTab.SHIELD, Icons.Default.Shield, "shield_tab"),
                Triple(AppTab.STATS, Icons.Default.Analytics, "stats_tab"),
                Triple(AppTab.ACADEMIC, Icons.Default.School, "academic_tab"),
                Triple(AppTab.COMMUNITY, Icons.Default.Person, "community_tab"),
                Triple(AppTab.TIMETABLE, Icons.Default.DateRange, "timetable_tab"),
                Triple(AppTab.SETTINGS, Icons.Default.Settings, "settings_tab")
            ).forEach { (tab, icon, testTagStr) ->
                val isSelected = currentTab == tab
                
                Box(
                    modifier = Modifier
                        .testTag(testTagStr)
                        .clip(CircleShape)
                        .clickable { onTabSelected(tab) }
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = tab.name,
                        tint = if (isSelected) accentColor else viewModel.themeTextSecondary,
                        modifier = Modifier.size(if (isSelected) 26.dp else 22.dp)
                    )
                }
            }
        }
    }
}

@OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)
@Composable
fun CustomNavigationSidebar(
    viewModel: FocusViewModel,
    currentTab: AppTab,
    accentColor: Color,
    onTabSelected: (AppTab) -> Unit
) {
    var isHovered by remember { mutableStateOf(false) }
    
    // Animate width between 64.dp (collapsed, icon-only) and 195.dp (expanded on hover)
    val sidebarWidth by animateDpAsState(
        targetValue = if (isHovered) 195.dp else 64.dp,
        animationSpec = spring(dampingRatio = 0.82f, stiffness = Spring.StiffnessLow),
        label = "sidebar_width"
    )

    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(sidebarWidth)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        when (event.type) {
                            PointerEventType.Enter -> isHovered = true
                            PointerEventType.Exit -> isHovered = false
                        }
                    }
                }
            }
            .background(if (viewModel.isDarkMode) Color(0xD912121F) else Color(0xF2FFFFFF))
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(viewModel.themeGlassBorder, Color.Transparent)
                ),
                shape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
            )
            .padding(vertical = 24.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            // Sidebar Header Branding
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.15f))
                    .border(1.dp, accentColor.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Widgets,
                    contentDescription = "FocusCube Suite Logo",
                    tint = accentColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Navigation Links
            val navigationItems = listOf(
                Triple(AppTab.HOME, Icons.Default.Home, "Home"),
                Triple(AppTab.HABITS, Icons.Default.CheckCircle, "Habits"),
                Triple(AppTab.SHIELD, Icons.Default.Shield, "Focus Shield"),
                Triple(AppTab.STATS, Icons.Default.Analytics, "Stats"),
                Triple(AppTab.ACADEMIC, Icons.Default.School, "Timetable"),
                Triple(AppTab.COMMUNITY, Icons.Default.Person, "Community"),
                Triple(AppTab.SETTINGS, Icons.Default.Settings, "Settings")
            )
            
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 10.dp)
            ) {
                navigationItems.forEach { (tab, icon, label) ->
                    val isSelected = currentTab == tab
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onTabSelected(tab) }
                            .background(
                                if (isSelected) accentColor.copy(alpha = 0.12f) else Color.Transparent
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) accentColor.copy(alpha = 0.3f) else Color.Transparent,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = if (isSelected) accentColor else viewModel.themeTextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        
                        AnimatedVisibility(
                            visible = isHovered,
                            enter = fadeIn(animationSpec = tween(180)) + expandHorizontally(),
                            exit = fadeOut(animationSpec = tween(120)) + shrinkHorizontally()
                        ) {
                            Row {
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = label,
                                    color = if (isSelected) accentColor else viewModel.themeText,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Subtle, feedback chevron indicator
            Icon(
                imageVector = if (isHovered) Icons.Default.KeyboardArrowLeft else Icons.Default.KeyboardArrowRight,
                contentDescription = "Inactivity indicator",
                tint = viewModel.themeTextSecondary.copy(alpha = 0.35f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
