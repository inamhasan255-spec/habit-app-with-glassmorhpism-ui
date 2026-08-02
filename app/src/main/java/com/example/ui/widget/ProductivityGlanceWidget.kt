package com.example.ui.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.data.FocusDatabase
import kotlinx.coroutines.flow.firstOrNull
import java.util.Calendar

class ProductivityGlanceWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val database = try {
            FocusDatabase.getDatabase(context)
        } catch (e: Exception) {
            null
        }
        
        var todaysFocusMinutes = 0
        var topBlockedApps: List<String> = emptyList()
        
        try {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                // Calculate today's focus time
                val workSessions = database?.workSessionDao()?.getAllSessions()?.firstOrNull() ?: emptyList()
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val startOfDay = calendar.timeInMillis
                
                todaysFocusMinutes = workSessions
                    .filter { it.timestamp >= startOfDay }
                    .sumOf { it.focusTimeMinutes }
                
                // Get most blocked apps
                val blockedApps = database?.blockedAppDao()?.getAllBlockedApps()?.firstOrNull() ?: emptyList()
                topBlockedApps = blockedApps
                    .filter { it.isBlocked }
                    .take(3)
                    .map { it.appName }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        provideContent {
            WidgetContent(
                focusMinutes = todaysFocusMinutes,
                blockedApps = topBlockedApps
            )
        }
    }
    
    @Composable
    private fun WidgetContent(focusMinutes: Int, blockedApps: List<String>) {
        val bgColor = ColorProvider(Color(0xFF1E1E24))
        val titleColor = ColorProvider(Color(0xFF6C63FF))
        val textColor = ColorProvider(Color.White)
        val subtitleColor = ColorProvider(Color(0xFFB0BEC5))
        
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(bgColor)
                .padding(16.dp),
            verticalAlignment = Alignment.Top,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Productivity Insights",
                style = TextStyle(color = titleColor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            )
            Spacer(modifier = GlanceModifier.height(12.dp))
            
            Text(
                text = "Today's Focus Time",
                style = TextStyle(color = subtitleColor, fontSize = 12.sp)
            )
            Text(
                text = "${focusMinutes / 60}h ${focusMinutes % 60}m",
                style = TextStyle(color = textColor, fontWeight = FontWeight.Bold, fontSize = 24.sp)
            )
            
            Spacer(modifier = GlanceModifier.height(16.dp))
            
            Text(
                text = "Top Distractions Blocked",
                style = TextStyle(color = subtitleColor, fontSize = 12.sp)
            )
            Spacer(modifier = GlanceModifier.height(4.dp))
            
            if (blockedApps.isEmpty()) {
                Text(
                    text = "No apps blocked yet",
                    style = TextStyle(color = textColor, fontSize = 14.sp)
                )
            } else {
                blockedApps.forEach { appName ->
                    Text(
                        text = "• $appName",
                        style = TextStyle(color = textColor, fontSize = 14.sp)
                    )
                }
            }
        }
    }
}

class ProductivityGlanceWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ProductivityGlanceWidget()
}
