package com.example.data

import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import kotlinx.coroutines.delay
import kotlin.random.Random

class SystemBlockOverlayManager(private val context: Context) {

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var overlayView: ComposeView? = null
    private var lifecycleOwner: MyLifecycleOwner? = null

    class MyLifecycleOwner : LifecycleOwner, SavedStateRegistryOwner, ViewModelStoreOwner {
        private val lifecycleRegistry = LifecycleRegistry(this)
        private val savedStateRegistryController = SavedStateRegistryController.create(this)
        private val store = ViewModelStore()

        override val lifecycle: Lifecycle
            get() = lifecycleRegistry

        override val savedStateRegistry: SavedStateRegistry
            get() = savedStateRegistryController.savedStateRegistry

        override val viewModelStore: ViewModelStore
            get() = store

        fun onCreate() {
            savedStateRegistryController.performRestore(null)
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        }

        fun onResume() {
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        }

        fun onDestroy() {
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            store.clear()
        }
    }

    fun showOverlay(blockedAppName: String, blockedPackage: String) {
        if (overlayView != null) {
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
            return
        }

        val myLifecycleOwner = MyLifecycleOwner().apply {
            onCreate()
            onResume()
        }
        lifecycleOwner = myLifecycleOwner

        val composeView = ComposeView(context).apply {
            setViewTreeLifecycleOwner(myLifecycleOwner)
            setViewTreeSavedStateRegistryOwner(myLifecycleOwner)
            setViewTreeViewModelStoreOwner(myLifecycleOwner)

            setContent {
                MaterialTheme {
                    SystemBlockedOverlayContent(
                        context = context,
                        appName = blockedAppName,
                        onDismiss = {
                            removeOverlay()
                        }
                    )
                }
            }
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_FULLSCREEN or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
        }

        try {
            windowManager.addView(composeView, params)
            overlayView = composeView
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun removeOverlay() {
        overlayView?.let {
            try {
                windowManager.removeView(it)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            overlayView = null
        }
        lifecycleOwner?.onDestroy()
        lifecycleOwner = null
    }
}

val motivationalMessages = listOf(
    "Stay strong. Your future self will thank you.",
    "Deep work is where greatness is forged.",
    "One distraction can ruin hours of flow. Protect your focus.",
    "You have the power to control your attention.",
    "The secret of change is to focus all of your energy on building the new.",
    "Do not let a moment of impulse break a lifetime of discipline.",
    "Focus is a muscle. Keep exercising it."
)

@Composable
fun SystemBlockedOverlayContent(
    context: Context,
    appName: String,
    onDismiss: () -> Unit
) {
    val prefs = remember { context.getSharedPreferences("focus_cube_prefs", Context.MODE_PRIVATE) }
    var secondsLeft by remember { mutableStateOf(prefs.getInt("timer_seconds_left_for_service", 0)) }
    var isTimerRunning by remember { mutableStateOf(prefs.getBoolean("is_timer_running_for_service", false)) }
    var quoteIndex by remember { mutableStateOf(Random.nextInt(motivationalMessages.size)) }

    LaunchedEffect(Unit) {
        while (true) {
            secondsLeft = prefs.getInt("timer_seconds_left_for_service", 0)
            isTimerRunning = prefs.getBoolean("is_timer_running_for_service", false)
            delay(500)
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(15000)
            quoteIndex = (quoteIndex + 1) % motivationalMessages.size
        }
    }

    val mins = secondsLeft / 60
    val secs = secondsLeft % 60
    val timerStr = if (isTimerRunning && secondsLeft > 0) {
        String.format("%02d:%02d", mins, secs)
    } else {
        "00:00"
    }

    val primaryBg = Color(0xFF07070C)
    val cardBg = Color(0xFF12121E)
    val accentColor = Color(0xFFFF2E93)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(primaryBg),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.15f))
                    .border(2.dp, accentColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = "Shield Guard",
                    tint = accentColor,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "FOCUS CUBE SHIELD",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Access to \"$appName\" is suspended during your active Deep Work session.",
                color = Color(0xFF9CA3AF),
                fontSize = 13.sp,
                lineHeight = 20.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(36.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = cardBg),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .wrapContentWidth()
                    .border(1.dp, Color(0xFF2E2E42), RoundedCornerShape(20.dp))
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 32.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "TIME REMAINING",
                        color = Color(0xFF6B7280),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = timerStr,
                        color = accentColor,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            Text(
                text = "\"${motivationalMessages[quoteIndex]}\"",
                color = Color(0xFFECECF1),
                fontSize = 14.sp,
                lineHeight = 22.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            com.example.ui.components.GlassButton(
                onClick = {
                    val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                        addCategory(Intent.CATEGORY_HOME)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(homeIntent)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(
                    text = "Return to Focus",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            com.example.ui.components.GlassButton(variant = com.example.ui.components.GlassButtonVariant.Secondary, 
                onClick = {
                    prefs.edit().putBoolean("is_timer_running_for_service", false).apply()
                    onDismiss()
                }
            ) {
                Text(
                    text = "Emergency Bypass (End Focus Session)",
                    color = Color(0xFFEF4444),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
