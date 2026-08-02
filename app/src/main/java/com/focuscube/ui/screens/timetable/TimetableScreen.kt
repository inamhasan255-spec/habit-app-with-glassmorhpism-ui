package com.focuscube.ui.screens.timetable

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassProgressRing
import com.focuscube.ui.screens.timetable.components.PhaseHeader
import com.focuscube.ui.screens.timetable.components.WeekCard
import com.focuscube.ui.viewmodel.TimetableViewModel

@Composable
fun TimetableScreen(viewModel: TimetableViewModel, onStartFocus: (Int) -> Unit) {
    val weeks by viewModel.weeks.collectAsState()
    val completedWeeksCount by viewModel.completedWeeksCount.collectAsState()
    val totalMinutes by viewModel.totalCompletedMinutes.collectAsState()

    val phases = listOf("calculus", "algebra", "geometry", "revision")
    val phaseColors = mapOf(
        "calculus" to "#EF4444",
        "algebra" to "#8B5CF6",
        "geometry" to "#06B6D4",
        "revision" to "#10B981"
    )

    var expandedPhase by remember { mutableStateOf<String?>("calculus") }

    val totalProgress = if (weeks.isNotEmpty()) weeks.map { it.progress }.average().toFloat() else 0f

    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(modifier = Modifier.size(120.dp), contentAlignment = Alignment.Center) {
                GlassProgressRing(
                    progress = totalProgress,
                    accentColor = Color.White,
                    strokeWidth = 8.dp,
                    modifier = Modifier.fillMaxSize()
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${(totalProgress * 100).toInt()}%",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "JEE 2027",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            phases.forEach { phase ->
                val phaseWeeks = weeks.filter { it.phase == phase }
                val phaseProgress = if (phaseWeeks.isNotEmpty()) phaseWeeks.map { it.progress }.average().toFloat() else 0f
                val isExpanded = expandedPhase == phase

                item {
                    PhaseHeader(
                        title = phase,
                        progress = phaseProgress,
                        isExpanded = isExpanded,
                        onToggle = { expandedPhase = if (isExpanded) null else phase },
                        phaseColorHex = phaseColors[phase] ?: "#94A3B8"
                    )
                }

                if (isExpanded) {
                    items(phaseWeeks) { week ->
                        WeekCard(
                            habit = week,
                            onToggleDone = { viewModel.toggleWeekDone(week.weekNumber, it) },
                            onStartFocus = {
                                onStartFocus(week.weekNumber)
                            },
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }
                }
            }
        }
    }
}
