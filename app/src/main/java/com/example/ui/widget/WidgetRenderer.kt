package com.example.ui.widget

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.components.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Represents a logged component-level crash captured by the Widget registry middleware.
 */
data class WidgetCrashReport(
    val id: String = java.util.UUID.randomUUID().toString(),
    val widgetId: String,
    val title: String,
    val errorMessage: String,
    val timestamp: Long = System.currentTimeMillis(),
    val stackTrace: String
)

/**
 * Unified middleware for the WidgetRegistry that logs component-level crashes and maintains telemetry logs.
 */
object WidgetErrorMiddleware {
    private val _crashLogs = mutableStateListOf<WidgetCrashReport>()
    val crashLogs: List<WidgetCrashReport> get() = _crashLogs

    fun logCrash(widgetId: String, title: String, throwable: Throwable) {
        val report = WidgetCrashReport(
            widgetId = widgetId,
            title = title,
            errorMessage = throwable.localizedMessage ?: throwable.toString(),
            stackTrace = throwable.stackTraceToString()
        )
        _crashLogs.add(0, report)
        android.util.Log.e("WidgetMiddleware", "Captured crash in widget '$widgetId' [$title]:", throwable)
    }

    fun clearLogs() {
        _crashLogs.clear()
    }
}

/**
 * Composition local to report widget runtime API or asynchronous errors
 */
val LocalWidgetErrorHandler = staticCompositionLocalOf<(Throwable) -> Unit> {
    { _ -> }
}

@Composable
fun WidgetErrorStateWrapper(
    widgetId: String,
    viewModel: FocusViewModel,
    accentColor: Color,
    content: @Composable () -> Unit
) {
    var caughtError by remember(widgetId) { mutableStateOf<Throwable?>(null) }
    var retryTrigger by remember { mutableStateOf(0) }
    var showTelemetry by remember { mutableStateOf(false) }

    val widgetInfo = remember(widgetId) {
        WidgetRegistry.widgets.firstOrNull { it.id == widgetId }
    }
    val widgetTitle = widgetInfo?.title ?: "Unknown Widget"

    if (caughtError != null) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Widget Sync Interrupted",
                tint = Color(0xFFEF5350), // modern error coral-red
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Widget Sync Interrupted",
                color = viewModel.themeText,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = caughtError?.localizedMessage ?: "An unexpected API or runtime error occurred.",
                color = viewModel.themeTextSecondary,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            // Expandable Technical Trace
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .clickable { showTelemetry = !showTelemetry }
                    .padding(vertical = 4.dp, horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (showTelemetry) "Hide Technical Trace ▴" else "Show Technical Trace ▾",
                    color = accentColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (showTelemetry) {
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 120.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(viewModel.themeSurface)
                        .border(1.dp, viewModel.themeGlassBorder, RoundedCornerShape(12.dp))
                        .padding(10.dp)
                ) {
                    androidx.compose.foundation.lazy.LazyColumn {
                        item {
                            Text(
                                text = "Widget: $widgetId\nTime: ${SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date(caughtError?.let { retryTrigger; System.currentTimeMillis() } ?: System.currentTimeMillis()))}\n\n${caughtError?.stackTraceToString()}",
                                color = viewModel.themeTextSecondary,
                                fontSize = 9.sp,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                lineHeight = 12.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            com.example.ui.components.GlassButton(
                onClick = {
                    caughtError = null
                    retryTrigger++
                },
                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                modifier = Modifier
                    .height(32.dp)
                    .testTag("widget_retry_button_${widgetId}"),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Retry",
                    tint = if (viewModel.isDarkMode) Color.Black else Color.White,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "RETRY SYNC",
                    color = if (viewModel.isDarkMode) Color.Black else Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
            }
        }
    } else {
        val errorHandler: (Throwable) -> Unit = remember(widgetId, widgetTitle) {
            { throwable ->
                caughtError = throwable
                WidgetErrorMiddleware.logCrash(widgetId, widgetTitle, throwable)
            }
        }
        CompositionLocalProvider(LocalWidgetErrorHandler provides errorHandler) {
            key(retryTrigger) {
                content()
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun WidgetRenderer(
    widgetId: String,
    viewModel: FocusViewModel,
    accentColor: Color,
    scope: CoroutineScope,
    selectedDateStr: String
) {
    val context = LocalContext.current
    val widgetInfo = WidgetRegistry.widgets.firstOrNull { it.id == widgetId } ?: return

    // Base card styled using our liquid-glass theme token
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
            .clip(RoundedCornerShape(24.dp))
            .background(viewModel.themeGlassFill)
            .border(1.dp, viewModel.themeGlassBorder, RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Widget Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val icon = getWidgetIcon(widgetInfo.iconName)
                    Icon(
                        imageVector = icon,
                        contentDescription = widgetInfo.title,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = widgetInfo.title,
                        color = viewModel.themeText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Audience/Category badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(accentColor.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = widgetInfo.category,
                        color = accentColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Widget Content Body
            val spec = WidgetRegistry.specs[widgetId]
            WidgetErrorStateWrapper(
                widgetId = widgetId,
                viewModel = viewModel,
                accentColor = accentColor
            ) {
                if (spec != null) {
                    spec.Render(viewModel, accentColor, scope, selectedDateStr)
                } else {
                    // Generically render the remaining widgets beautifully
                    var interactionCount by remember { mutableStateOf(0) }
                    Column {
                        Text(
                            text = widgetInfo.description,
                            color = viewModel.themeTextSecondary,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Hits: $interactionCount ⚡",
                                color = accentColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            com.example.ui.components.GlassButton(
                                onClick = { 
                                    interactionCount++
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                                modifier = Modifier.height(32.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Log Tracker",
                                    fontSize = 11.sp,
                                    color = viewModel.themeBg,
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

// Map descriptor string to proper material icon
fun getWidgetIcon(name: String): ImageVector {
    return when (name) {
        "Cube" -> Icons.Default.Token
        "DocumentScanner" -> Icons.Default.DocumentScanner
        "Star" -> Icons.Default.Star
        "PlayArrow" -> Icons.Default.PlayArrow
        "Face" -> Icons.Default.Face
        "BarChart" -> Icons.Default.BarChart
        "History" -> Icons.Default.History
        "CheckCircle" -> Icons.Default.CheckCircle
        "FormatQuote" -> Icons.Default.FormatQuote
        "VolumeUp" -> Icons.Default.VolumeUp
        "Edit" -> Icons.Default.Edit
        "Timer" -> Icons.Default.Timer
        "EmojiEvents" -> Icons.Default.EmojiEvents
        "Air" -> Icons.Default.Air
        "DoNotDisturb" -> Icons.Default.DoNotDisturb
        "DateRange" -> Icons.Default.DateRange
        "StickyNote2" -> Icons.Default.StickyNote2
        "Assignment" -> Icons.Default.Assignment
        "GraphicEq" -> Icons.Default.GraphicEq
        "Calculate" -> Icons.Default.Calculate
        "Groups" -> Icons.Default.Groups
        "Style" -> Icons.Default.Style
        "Alarm" -> Icons.Default.Alarm
        "WbSunny" -> Icons.Default.WbSunny
        "ContactPage" -> Icons.Default.ContactPage
        "AutoAwesome" -> Icons.Default.AutoAwesome
        "CloudUpload" -> Icons.Default.CloudUpload
        "MenuBook" -> Icons.Default.MenuBook
        "GridView" -> Icons.Default.GridView
        "TrendingUp" -> Icons.Default.TrendingUp
        "Sync" -> Icons.Default.Sync
        "Security" -> Icons.Default.Security
        "AttachMoney" -> Icons.Default.AttachMoney
        "Timeline" -> Icons.Default.Timeline
        "Favorite" -> Icons.Default.Favorite
        "AccountBalanceWallet" -> Icons.Default.AccountBalanceWallet
        "CallEnd" -> Icons.Default.CallEnd
        "FactCheck" -> Icons.Default.FactCheck
        "Send" -> Icons.Default.Send
        "LocalCafe" -> Icons.Default.LocalCafe
        "Email" -> Icons.Default.Email
        else -> Icons.Default.Star
    }
}
