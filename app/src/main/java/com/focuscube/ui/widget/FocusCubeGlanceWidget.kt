package com.focuscube.ui.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.actionStartActivity
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.MainActivity
import com.focuscube.data.local.FocusCubeDatabase
import com.focuscube.data.model.TimeCushionCalculator
import com.focuscube.data.model.UserSchedule
import kotlinx.coroutines.flow.firstOrNull
import java.util.Date

class FocusCubeGlanceWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val database = try {
            FocusCubeDatabase.getDatabase(context)
        } catch (e: Exception) {
            null
        }
        val dao = database?.assignmentDao()
        val assignments = try {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                kotlinx.coroutines.withTimeoutOrNull(1000) {
                    dao?.getAllAssignmentsFlow()?.firstOrNull()
                } ?: emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }

        provideContent {
            val cushions = try {
                TimeCushionCalculator.calculateTimeCushions(
                    assignments = assignments,
                    schedule = UserSchedule(),
                    now = Date()
                )
            } catch (e: Exception) {
                emptyList()
            }
            val totalPending = assignments.size
            val remainingHours = assignments.sumOf { it.estimatedDurationHours }
            val hasDeficit = cushions.any { it < 0.0 }

            WidgetContent(
                totalPending = totalPending,
                remainingHours = remainingHours,
                hasDeficit = hasDeficit
            )
        }
    }

    @Composable
    private fun WidgetContent(
        totalPending: Int,
        remainingHours: Double,
        hasDeficit: Boolean
    ) {
        val backgroundColor = if (hasDeficit) {
            ColorProvider(Color(0xFF2D1C1B)) // Dark Red
        } else {
            ColorProvider(Color(0xFF1B2D21)) // Dark Green
        }

        val textColor = ColorProvider(Color.White)
        val subtitleColor = ColorProvider(Color(0xFFB0BEC5))

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(16.dp),
            verticalAlignment = Alignment.Top,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Focus Cube Guard",
                style = TextStyle(
                    color = textColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            )
            Spacer(modifier = GlanceModifier.height(8.dp))

            Row(modifier = GlanceModifier.fillMaxWidth()) {
                Column(modifier = GlanceModifier.defaultWeight()) {
                    Text(
                        text = "Pending",
                        style = TextStyle(color = subtitleColor, fontSize = 12.sp)
                    )
                    Text(
                        text = "$totalPending",
                        style = TextStyle(color = textColor, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    )
                }
                Spacer(modifier = GlanceModifier.width(16.dp))
                Column(modifier = GlanceModifier.defaultWeight()) {
                    Text(
                        text = "Workload",
                        style = TextStyle(color = subtitleColor, fontSize = 12.sp)
                    )
                    Text(
                        text = "${String.format("%.1f", remainingHours)}h",
                        style = TextStyle(color = textColor, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    )
                }
            }

            Spacer(modifier = GlanceModifier.height(12.dp))

            val cushionStatus = if (hasDeficit) {
                "Deficit Active - Locks Locked"
            } else {
                "Surplus Healthy - Shield Active"
            }
            Text(
                text = cushionStatus,
                style = TextStyle(
                    color = if (hasDeficit) ColorProvider(Color(0xFFFF8A80)) else ColorProvider(Color(0xFFB9F6CA)),
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp
                )
            )

            Spacer(modifier = GlanceModifier.height(12.dp))

            Button(
                text = "Open Focus Cube",
                onClick = actionStartActivity<MainActivity>(),
                modifier = GlanceModifier.fillMaxWidth()
            )
        }
    }
}

class FocusCubeGlanceWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = FocusCubeGlanceWidget()
}
