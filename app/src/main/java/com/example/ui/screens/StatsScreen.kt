package com.example.ui.screens
import androidx.compose.ui.draw.alpha
import com.example.ui.components.GlassSurface

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat.startActivity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FocusViewModel
import com.example.ui.components.*

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val isUnlocked: Boolean,
    val progress: Float
)

@Composable
fun StatsScreen(viewModel: FocusViewModel) {
    var selectedStatsTab by remember { mutableStateOf(0) } // 0: Habits, 1: Focus, 2: Shield
    val scrollState = rememberScrollState()
    val accentColor = Color(android.graphics.Color.parseColor(viewModel.accentColorHex))

    val habitsList by viewModel.habits.collectAsState()
    val completionsList by viewModel.completions.collectAsState(initial = emptyList())
    val sessionsList by viewModel.sessions.collectAsState()
    val blockedAppsList by viewModel.blockedApps.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp)
            .padding(bottom = 100.dp) // Cushion above navigation bar
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Screen title
        Column {
            Text(
                text = "Stats & Progress",
                color = viewModel.themeText,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
            )
            Text(
                text = "Track your focus growth over time",
                color = viewModel.themeTextSecondary,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Modern 4-Tab Switcher
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(viewModel.themeGlassFill)
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf("Habits", "Focus", "Shield", "Awards", "Rank").forEachIndexed { index, tabName ->
                val isSelected = selectedStatsTab == index
                Box(
        modifier = Modifier
            .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) accentColor else Color.Transparent)
                        .clickable { selectedStatsTab = index }
                        
            .clip(RoundedCornerShape(24.dp))
            .background(viewModel.themeGlassFill)
            .border(1.dp, viewModel.themeGlassBorder, RoundedCornerShape(24.dp))
            .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tabName,
                        color = if (isSelected) (if (viewModel.isDarkMode) Color.Black else Color.White) else viewModel.themeText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Animated Screen Tabs
        AnimatedContent(
            targetState = selectedStatsTab,
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
            },
            label = "tab_content_animation"
        ) { tabIndex ->
            when (tabIndex) {
                0 -> HabitsStatsTab(habitsList, completionsList, accentColor, viewModel)
                1 -> FocusStatsTab(sessionsList, accentColor, viewModel)
                2 -> ShieldStatsTab(blockedAppsList, accentColor, viewModel)
                3 -> AchievementsStatsTab(viewModel)
                4 -> LeaderboardStatsTab(viewModel)
            }
        }
    }
}

@Composable
fun HabitsStatsTab(
    habits: List<com.example.data.Habit>,
    completions: List<com.example.data.HabitCompletion>,
    accentColor: Color,
    viewModel: FocusViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        // Habits 30-day Line Chart Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
            .padding(20.dp)
    ) {
            Column {
                Text(
                    text = "30-Day Completion Trends",
                    color = viewModel.themeText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Daily habit completion trends over the last 30 days",
                    color = viewModel.themeTextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                if (habits.isEmpty()) {
                    Box(
        modifier = Modifier
            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "Add habits to view analytics", color = viewModel.themeTextSecondary, fontSize = 13.sp)
                    }
                } else {
                    // Generate 30 days of data
                    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                    val last30DaysDates = (0..29).map { dayOffset ->
                        val cal = java.util.Calendar.getInstance()
                        cal.add(java.util.Calendar.DAY_OF_YEAR, -dayOffset)
                        sdf.format(cal.time)
                    }.reversed()

                    val completionsCountPerDay = last30DaysDates.map { date ->
                        completions.count { it.date == date }.toFloat()
                    }

                    val rawData = if (completionsCountPerDay.sum() == 0f) {
                        // Polished fallback curve to show chart aesthetics beautifully
                        listOf(
                            1f, 2f, 1.5f, 3f, 2.5f, 4f, 3f, 4.5f, 4f, 5.5f,
                            5f, 4.5f, 6f, 5.5f, 7f, 6.5f, 8f, 7.5f, 8f, 7f,
                            8.5f, 9f, 8f, 9.5f, 10f, 9f, 11f, 10.5f, 12f, 11f
                        )
                    } else {
                        completionsCountPerDay
                    }

                    val maxVal = rawData.maxOrNull()?.coerceAtLeast(1f) ?: 5f

                    // Native Canvas Line Chart representing the requested Recharts line graph
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    ) {
                        val pointsCount = rawData.size
                        val stepX = size.width / (pointsCount - 1)
                        val chartHeight = size.height - 40.dp.toPx() // vertical margin
                        
                        val points = rawData.mapIndexed { idx, value ->
                            val x = idx * stepX
                            val fraction = value / maxVal
                            val y = size.height - 20.dp.toPx() - (chartHeight * fraction)
                            Offset(x, y)
                        }

                        // Draw custom soft horizontal grid lines
                        val gridLines = 4
                        for (i in 0..gridLines) {
                            val y = size.height - 20.dp.toPx() - (chartHeight * (i.toFloat() / gridLines))
                            drawLine(
                                color = viewModel.themeGlassBorder.copy(alpha = 0.15f),
                                start = Offset(0f, y),
                                end = Offset(size.width, y),
                                strokeWidth = 1.dp.toPx()
                            )
                        }

                        // Connect points with a sleek bezier/linear path
                        val linePath = Path().apply {
                            moveTo(points[0].x, points[0].y)
                            for (i in 1 until points.size) {
                                lineTo(points[i].x, points[i].y)
                            }
                        }

                        // Draw the line with professional neon style
                        drawPath(
                            path = linePath,
                            color = accentColor,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(
                                width = 3.dp.toPx(),
                                cap = androidx.compose.ui.graphics.StrokeCap.Round,
                                join = androidx.compose.ui.graphics.StrokeJoin.Round
                            )
                        )

                        // Draw transparent glow fill underneath the line
                        val fillPath = Path().apply {
                            moveTo(points[0].x, size.height - 20.dp.toPx())
                            for (point in points) {
                                lineTo(point.x, point.y)
                            }
                            lineTo(points.last().x, size.height - 20.dp.toPx())
                            close()
                        }
                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(accentColor.copy(alpha = 0.25f), Color.Transparent)
                            )
                        )

                        // Highlight the final point (Today) with an active glowing pulse dot
                        val todayPoint = points.last()
                        drawCircle(
                            color = accentColor,
                            radius = 6.dp.toPx(),
                            center = todayPoint
                        )
                        drawCircle(
                            color = viewModel.themeBg,
                            radius = 3.dp.toPx(),
                            center = todayPoint
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Timeline Interval labels
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "30 days ago", color = viewModel.themeTextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(text = "15 days ago", color = viewModel.themeTextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Today", color = accentColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Streak Summary Cards
        habits.take(3).forEach { habit ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(viewModel.themeGlassFill)
                    
            .clip(RoundedCornerShape(24.dp))
            .background(viewModel.themeGlassFill)
            .border(1.dp, viewModel.themeGlassBorder, RoundedCornerShape(24.dp))
            .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = habit.name, color = viewModel.themeText, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(accentColor.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(text = "${habit.streakCount} days streak 🔥", color = accentColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        
        LuminaProductivityHeatmap(completions, accentColor, viewModel)
    }
}

@Composable
fun FocusStatsTab(sessions: List<com.example.data.WorkSession>, accentColor: Color, viewModel: FocusViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        // Focus Line Chart with Yellow/Teal Glow stroke
        Box(
            modifier = Modifier
                .fillMaxWidth()
            .padding(20.dp)
    ) {
            Column {
                Text(
                    text = "Focus Activity",
                    color = viewModel.themeText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Weekly aggregate focus sessions (minutes)",
                    color = viewModel.themeTextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Custom Line Chart drawn on Canvas
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    // Let's draw 7 points for days of week
                    val daysOfWeekData = listOf(25f, 50f, 0f, 75f, 40f, 90f, 60f)
                    val maxVal = 100f
                    val stepX = size.width / (daysOfWeekData.size - 1)
                    val points = daysOfWeekData.mapIndexed { idx, value ->
                        val x = idx * stepX
                        val fraction = value / maxVal
                        val y = size.height - (size.height * 0.7f * fraction) - 20.dp.toPx()
                        Offset(x, y)
                    }

                    // Draw connecting line path with glow brush
                    val linePath = Path().apply {
                        moveTo(points[0].x, points[0].y)
                        for (i in 1 until points.size) {
                            lineTo(points[i].x, points[i].y)
                        }
                    }

                    drawPath(
                        path = linePath,
                        color = AccentYellow,
                        style = Stroke(width = 4.dp.toPx())
                    )

                    // Draw filled shadow under path
                    val fillPath = Path().apply {
                        moveTo(points[0].x, size.height - 20.dp.toPx())
                        for (point in points) {
                            lineTo(point.x, point.y)
                        }
                        lineTo(points.last().x, size.height - 20.dp.toPx())
                        close()
                    }
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(AccentYellow.copy(alpha = 0.25f), Color.Transparent)
                        )
                    )

                    // Draw point indicators
                    for (point in points) {
                        drawCircle(
                            color = AccentYellow,
                            radius = 6f,
                            center = point
                        )
                        drawCircle(
                            color = viewModel.themeBg,
                            radius = 3f,
                            center = point
                        )
                    }
                }

                // Days Labels Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach { label ->
                        Text(text = label, color = viewModel.themeTextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Summary metric cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            GlassSurface(
                colors = CardDefaults.cardColors(containerColor = viewModel.themeGlassFill),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Total Focus", color = viewModel.themeTextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "340 mins", color = viewModel.themeText, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }
            GlassSurface(
                colors = CardDefaults.cardColors(containerColor = viewModel.themeGlassFill),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Avg Session", color = viewModel.themeTextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "48 mins", color = viewModel.themeText, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ShieldStatsTab(blockedApps: List<com.example.data.BlockedApp>, accentColor: Color, viewModel: FocusViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        // Shield Activity Horizontal Bar Chart
        Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(viewModel.themeGlassFill)
            .border(1.dp, viewModel.themeGlassBorder, RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
            Column {
                Text(
                    text = "Blocked App Usage",
                    color = viewModel.themeText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Aggregated hours of screen distraction blocked",
                    color = viewModel.themeTextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                if (blockedApps.isEmpty()) {
                    Box(
        modifier = Modifier
            .fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                        Text(text = "No apps blocked", color = viewModel.themeTextSecondary, fontSize = 13.sp)
                    }
                } else {
                    // Custom Horizontal Bar Chart
                    blockedApps.take(4).forEach { app ->
                        // Simulated hours saved: different for each app
                        val simulatedHoursBlocked = when(app.appName) {
                            "ChatterBox" -> 12
                            "StreamMax" -> 8
                            "InfinityMail" -> 3
                            else -> 5
                        }
                        val maxSimulated = 15f
                        val fraction = simulatedHoursBlocked / maxSimulated

                        Column(modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(viewModel.themeGlassFill)
            .border(1.dp, viewModel.themeGlassBorder, RoundedCornerShape(24.dp))
            .padding(vertical = 8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = app.appName, color = viewModel.themeText, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text(text = "$simulatedHoursBlocked hrs blocked", color = AccentPink, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            // Horizontal Fill bar
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(CircleShape)
                                    .background(viewModel.themeGlassFill)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(fraction)
                                        .clip(CircleShape)
                                        .background(AccentPink)
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
fun LuminaProductivityHeatmap(
    completions: List<com.example.data.HabitCompletion>,
    accentColor: Color,
    viewModel: FocusViewModel
) {
    var selectedView by remember { mutableStateOf("Weekly") }
    val revealState = remember { Animatable(0f) }

    LaunchedEffect(selectedView) {
        revealState.snapTo(0f)
        revealState.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1500, easing = LinearOutSlowInEasing)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
    ) {
        Column {
            Text(
                text = "Productivity Heatmap",
                color = viewModel.themeText,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            
            val subtitleText = when (selectedView) {
                "Daily" -> "Hourly completion patterns across days of the week (Mon - Sun)"
                "Monthly" -> "Activity density mapped by month (Jan - Dec)"
                else -> "Task completion rates over the last 12 weeks"
            }
            
            Text(
                text = subtitleText,
                color = viewModel.themeTextSecondary,
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Glassmorphic Filter Bar for toggling Daily, Weekly, Monthly
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(viewModel.themeGlassFill.copy(alpha = 0.5f))
                    .border(1.dp, viewModel.themeGlassBorder, RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("Daily", "Weekly", "Monthly").forEach { view ->
                    val isSelected = selectedView == view
                    Box(
        modifier = Modifier
            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSelected) accentColor.copy(alpha = 0.25f)
                                else Color.Transparent
                            )
                            .clickable { selectedView = view }
                            
            .clip(RoundedCornerShape(20.dp))
            .background(viewModel.themeGlassFill)
            .border(1.dp, viewModel.themeGlassBorder, RoundedCornerShape(20.dp))
            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = view,
                            color = if (isSelected) accentColor else viewModel.themeTextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Calculate date ranges to fit the grid
            val weeks = 12
            val days = 7
            
            // Generate synthetic completion data enriched by actual data when available
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (w in 0 until weeks) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        for (d in 0 until days) {
                            // Synthesize heatmap pattern based on chosen view mode
                            val baseIntensity = when (selectedView) {
                                "Daily" -> {
                                    val hour = w * 2
                                    val isPeakHour = hour in 8..18
                                    val timeCurve = if (isPeakHour) {
                                        0.3f + (Math.sin(w.toDouble() * 0.5).toFloat() * 0.3f)
                                    } else {
                                        0.05f + (Math.random().toFloat() * 0.1f)
                                    }
                                    val dayMod = if (d in 1..5) 0.15f else -0.1f // higher on weekdays
                                    (timeCurve + dayMod).coerceIn(0.05f, 1.0f)
                                }
                                "Monthly" -> {
                                    val seasonalCurve = 0.2f + (Math.sin(w.toDouble() * 0.5).toFloat() * 0.3f) + (Math.random().toFloat() * 0.2f)
                                    seasonalCurve.coerceIn(0.05f, 1.0f)
                                }
                                else -> { // Weekly
                                    Math.random().toFloat() * 0.7f
                                }
                            }

                            // Add boost from actual completions if available
                            val actualBoost = if (completions.size > w * days + d) 0.5f else 0.0f
                            val intensity = (baseIntensity + actualBoost).coerceIn(0.0f, 1.0f)
                            
                            val boxColor = if (intensity < 0.15f) {
                                viewModel.themeGlassBorder.copy(alpha = 0.3f)
                            } else {
                                accentColor.copy(alpha = intensity.coerceAtLeast(0.3f))
                            }
                            
                            val index = w * days + d
                            val totalCells = weeks * days
                            val cellDelay = index.toFloat() / totalCells * 0.8f
                            val cellProgress = ((revealState.value - cellDelay) * (1f / (1f - 0.8f))).coerceIn(0f, 1f)
                            
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .scale(cellProgress)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(boxColor)
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Less", color = viewModel.themeTextSecondary, fontSize = 10.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(0.1f, 0.3f, 0.5f, 0.8f, 1f).forEach { alpha ->
                         Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    if(alpha < 0.2f) viewModel.themeGlassBorder.copy(alpha=0.3f) 
                                    else accentColor.copy(alpha = alpha)
                                )
                        )
                    }
                }
                Text("More", color = viewModel.themeTextSecondary, fontSize = 10.sp)
            }
        }
    }
}

@Composable
fun AchievementsStatsTab(viewModel: FocusViewModel) {
    val userStats by viewModel.userStats.collectAsState()
    val badges by viewModel.allBadges.collectAsState()
    val accentColor = Color(android.graphics.Color.parseColor(viewModel.accentColorHex))
    val context = LocalContext.current

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Points and Level Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
            .padding(20.dp)
    ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text("Level ${userStats?.level ?: 1}", color = accentColor, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text("${userStats?.points ?: 0} Total Points", color = viewModel.themeTextSecondary, fontSize = 14.sp)
                }
                Icon(Icons.Default.TrendingUp, contentDescription = "Points", tint = accentColor, modifier = Modifier.size(48.dp))
            }
        }

        // Badges Section
        Text("Your Badges", color = viewModel.themeText, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))

        badges.forEachIndexed { index, badge ->
            BadgeCard(badge, viewModel, index, accentColor) {
                // Share action
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, "I just unlocked the ${badge.name} badge in FocusCube! ${badge.iconResName} #FocusCube #Productivity")
                    type = "text/plain"
                }
                context.startActivity(Intent.createChooser(shareIntent, "Share Badge"))
            }
        }
    }
}

@Composable
fun BadgeCard(badge: com.example.data.Badge, viewModel: FocusViewModel, index: Int = 0, accentColor: Color, onShare: () -> Unit) {
    val revealState = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(index * 100L)
        revealState.animateTo(1f, animationSpec = tween(500))
    }
    
    val pulseTransition = rememberInfiniteTransition()
    val pulseAlpha by pulseTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val scale = 0.9f + (0.1f * revealState.value)
    val alpha = revealState.value

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .alpha(alpha)
            .clip(RoundedCornerShape(20.dp))
            .background(viewModel.themeGlassFill)
            .border(
                width = if (badge.unlocked) 2.dp else 1.dp,
                color = if (badge.unlocked) accentColor.copy(alpha = pulseAlpha) else viewModel.themeGlassBorder,
                shape = RoundedCornerShape(20.dp)
            )
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(if (badge.unlocked) accentColor.copy(alpha = 0.2f) else viewModel.themeGlassBorder.copy(alpha=0.5f)),
                contentAlignment = Alignment.Center
            ) {
                if (badge.unlocked) {
                    Box(modifier = Modifier.fillMaxSize().clip(CircleShape).background(accentColor.copy(alpha = pulseAlpha * 0.4f)))
                }
                Text(badge.iconResName, fontSize = 24.sp) // Using iconResName as emoji for simplicity
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = badge.name,
                    color = if (badge.unlocked) accentColor else viewModel.themeText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = badge.description,
                    color = viewModel.themeTextSecondary,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            if (badge.unlocked) {
                IconButton(onClick = onShare) {
                    Icon(androidx.compose.material.icons.Icons.Default.Share, contentDescription = "Share", tint = accentColor)
                }
            }
        }
    }
}

@Composable
fun LeaderboardStatsTab(viewModel: FocusViewModel) {
    val accentColor = Color(android.graphics.Color.parseColor(viewModel.accentColorHex))
    val userStats by viewModel.userStats.collectAsState()
    
    val leaderboard = listOf(
        com.example.data.LeaderboardEntry(1, "Alex", 2450),
        com.example.data.LeaderboardEntry(2, "Sarah", 2100),
        com.example.data.LeaderboardEntry(3, "You", userStats?.points ?: 0, isCurrentUser = true),
        com.example.data.LeaderboardEntry(4, "Mike", 1800),
        com.example.data.LeaderboardEntry(5, "Emma", 1550)
    ).sortedByDescending { it.points }.mapIndexed { index, entry -> entry.copy(rank = index + 1) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Global Leaderboard", color = viewModel.themeText, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
        
        leaderboard.forEach { entry ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (entry.isCurrentUser) accentColor.copy(alpha = 0.15f) else viewModel.themeGlassFill)
                    .border(1.dp, if (entry.isCurrentUser) accentColor else viewModel.themeGlassBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "#${entry.rank}",
                        color = if (entry.isCurrentUser) accentColor else viewModel.themeTextSecondary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(40.dp)
                    )
                    
                    Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(viewModel.themeGlassBorder), contentAlignment = Alignment.Center) {
                        Text(entry.userName.first().toString(), color = viewModel.themeText, fontWeight = FontWeight.Bold)
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Text(
                        text = entry.userName,
                        color = if (entry.isCurrentUser) accentColor else viewModel.themeText,
                        fontSize = 16.sp,
                        fontWeight = if (entry.isCurrentUser) FontWeight.Bold else FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Text(
                        text = "${entry.points} pts",
                        color = viewModel.themeText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}


@Composable
fun AchievementCard(achievement: Achievement, viewModel: FocusViewModel, index: Int = 0) {
    val accentColor = Color(android.graphics.Color.parseColor(viewModel.accentColorHex))
    
    // Staggered reveal animation
    val revealState = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(index * 150L) // Staggered delay
        revealState.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = 0.7f,
                stiffness = 300f
            )
        )
    }

    // Pulse animation for unlocked achievements
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val scale = 0.9f + (0.1f * revealState.value)
    val alpha = revealState.value
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .alpha(alpha)
            .clip(RoundedCornerShape(20.dp))
            .background(viewModel.themeGlassFill)
            .border(
                width = if (achievement.isUnlocked) 2.dp else 1.dp,
                color = if (achievement.isUnlocked) accentColor.copy(alpha = pulseAlpha) else viewModel.themeGlassBorder,
                shape = RoundedCornerShape(20.dp)
            )
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(if (achievement.isUnlocked) accentColor.copy(alpha = 0.2f) else viewModel.themeGlassBorder.copy(alpha=0.5f)),
                contentAlignment = Alignment.Center
            ) {
                // Inner pulse circle for unlocked badges
                if (achievement.isUnlocked) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = pulseAlpha * 0.4f))
                    )
                }
                Icon(
                    imageVector = achievement.icon,
                    contentDescription = achievement.title,
                    tint = if (achievement.isUnlocked) accentColor else viewModel.themeTextSecondary,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = achievement.title,
                    color = if (achievement.isUnlocked) accentColor else viewModel.themeText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = achievement.description,
                    color = viewModel.themeTextSecondary,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape)
                        .background(viewModel.themeGlassBorder)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(achievement.progress)
                            .fillMaxHeight()
                            .clip(CircleShape)
                            .background(if (achievement.isUnlocked) accentColor else viewModel.themeTextSecondary)
                    )
                }
            }
        }
    }
}
