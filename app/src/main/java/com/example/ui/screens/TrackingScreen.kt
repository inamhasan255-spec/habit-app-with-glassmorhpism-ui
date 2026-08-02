package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FocusViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.ui.components.GlassSurface

@Composable
fun TrackingScreen(
    viewModel: FocusViewModel,
    modifier: Modifier = Modifier
) {
    val sessions by viewModel.sessions.collectAsState()
    val blockedApps by viewModel.blockedApps.collectAsState()

    // Premium White Glass theme palette
    val techBg = viewModel.themeBg
    val techCardBg = viewModel.themeSurface
    val techBorder = viewModel.themeBorder
    val techGreen = Color(0xFF0D9488) // Scholarly premium deep teal
    val techBlue = Color(0xFF3B82F6) // Electric blue
    val techText = viewModel.themeText
    val techTextMuted = viewModel.themeTextSecondary

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(techBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Tech Header ---
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Analytics,
                    contentDescription = "Analytics",
                    tint = techGreen,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "CONTROL MATRIX",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = techGreen,
                        letterSpacing = 4.sp
                    )
                )
                Text(
                    text = "Telemetry of focus density and app blocks.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        color = techTextMuted
                    ),
                    textAlign = TextAlign.Center
                )
            }
        }

        // --- Core Tech Stats Grid (Total hours, Streak, Blocks) ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Total hours card
                GlassSurface(
                    colors = CardDefaults.cardColors(containerColor = techCardBg),
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, techBorder, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Schedule, contentDescription = null, tint = techBlue, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("FOCUS HOURS", style = MaterialTheme.typography.labelSmall.copy(color = techTextMuted, fontFamily = FontFamily.Monospace))
                        }
                        val totalMins = sessions.sumOf { it.focusTimeMinutes }
                        val hrs = totalMins.toFloat() / 60.0f
                        Text(
                            text = String.format("%.1f", hrs),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = techText,
                                fontFamily = FontFamily.Monospace
                            )
                        )
                    }
                }

                // Active blocks card
                GlassSurface(
                    colors = CardDefaults.cardColors(containerColor = techCardBg),
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, techBorder, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Block, contentDescription = null, tint = Color.Red, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("SHIELD LOGS", style = MaterialTheme.typography.labelSmall.copy(color = techTextMuted, fontFamily = FontFamily.Monospace))
                        }
                        val blocksCount = blockedApps.count { it.isBlocked }
                        Text(
                            text = blocksCount.toString(),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = techText,
                                fontFamily = FontFamily.Monospace
                            )
                        )
                    }
                }
            }
        }

        // --- Custom Category Distribution (Deep Work vs Reading Donut) ---
        item {
            GlassSurface(
                colors = CardDefaults.cardColors(containerColor = techCardBg),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, techBorder, RoundedCornerShape(16.dp))
                    .testTag("category_distribution_card")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "focus_category_distribution.log",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = techGreen,
                            fontFamily = FontFamily.Monospace
                        )
                    )

                    val deepWorkCount = sessions.filter { it.category != "Reading" }.sumOf { it.focusTimeMinutes }
                    val readingCount = sessions.filter { it.category == "Reading" }.sumOf { it.focusTimeMinutes }
                    val totalFocus = (deepWorkCount + readingCount).coerceAtLeast(1)

                    val deepWorkPercent = (deepWorkCount.toFloat() / totalFocus.toFloat()) * 100f
                    val readingPercent = (readingCount.toFloat() / totalFocus.toFloat()) * 100f

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        // Custom Drawn Donut Chart
                        Box(
                            modifier = Modifier.size(110.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val strokeWidth = 14.dp.toPx()
                                val diameter = size.minDimension - strokeWidth
                                val rectSize = Size(diameter, diameter)
                                val offset = strokeWidth / 2f
                                val topLeft = Offset(offset, offset)

                                val deepWorkAngle = (deepWorkCount.toFloat() / totalFocus.toFloat()) * 360f
                                val readingAngle = 360f - deepWorkAngle

                                drawArc(
                                    color = techBlue,
                                    startAngle = -90f,
                                    sweepAngle = deepWorkAngle,
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = rectSize,
                                    style = Stroke(width = strokeWidth)
                                )

                                drawArc(
                                    color = techGreen,
                                    startAngle = -90f + deepWorkAngle,
                                    sweepAngle = readingAngle,
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = rectSize,
                                    style = Stroke(width = strokeWidth)
                                )
                            }
                            Text(
                                text = "SUM",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    color = techTextMuted
                                )
                            )
                        }

                        // Labels with legends
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(10.dp).background(techBlue, RoundedCornerShape(2.dp)))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Deep Work (${deepWorkPercent.toInt()}%)",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        color = techText
                                    )
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(10.dp).background(techGreen, RoundedCornerShape(2.dp)))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Reading (${readingPercent.toInt()}%)",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        color = techText
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- Blocked Distraction History Logs ---
        item {
            Text(
                text = "Blocked App Intercepts",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = techText,
                    letterSpacing = 1.sp
                )
            )
        }

        val blockedLogs = blockedApps.filter { it.isBlocked }
        if (blockedLogs.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No intercepts logged. FocusShield is running flawlessly.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = techTextMuted,
                            fontFamily = FontFamily.Monospace
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(blockedLogs) { app ->
                GlassSurface(
                    colors = CardDefaults.cardColors(containerColor = techCardBg),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, techBorder, RoundedCornerShape(10.dp))
                        .testTag("blocked_log_${app.packageName}")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ElectricBolt, contentDescription = "Intercepted", tint = techGreen, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = app.appName,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        color = techText
                                    )
                                )
                                Text(
                                    text = app.packageName,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = techTextMuted,
                                        fontFamily = FontFamily.Monospace
                                    )
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .background(Color.Red.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                .border(1.dp, Color.Red, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "SHIELD ACTIVE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color.Red,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
