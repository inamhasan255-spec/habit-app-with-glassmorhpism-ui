package com.focuscube.ui.screens.timetable.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassProgressRing
import com.example.ui.theme.LuminaTheme
import com.example.ui.theme.softGlassShadow
import com.focuscube.data.entity.TimetableHabit

@Composable
fun WeekCard(
    habit: TimetableHabit,
    onToggleDone: (Boolean) -> Unit,
    onStartFocus: () -> Unit,
    modifier: Modifier = Modifier
) {
    val phaseColor = Color(android.graphics.Color.parseColor(habit.phaseColor))
    val colors = LuminaTheme.colors

    Box(
        modifier = modifier
            .fillMaxWidth()
            .softGlassShadow(elevation = 8.dp, shape = RoundedCornerShape(20.dp), isDark = colors.isDark)
            .clip(RoundedCornerShape(20.dp))
            .background(colors.glassFill)
            .padding(16.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                    GlassProgressRing(
                        progress = habit.progress,
                        accentColor = phaseColor,
                        strokeWidth = 5.dp,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Week ${habit.weekNumber} • ${habit.dates}",
                        color = colors.text.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = habit.chapter,
                        color = colors.text,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(phaseColor.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = habit.tag,
                                color = phaseColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = habit.focus,
                color = colors.text.copy(alpha = 0.8f),
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val hoursCompleted = habit.completedMinutes / 60
                val minsCompleted = habit.completedMinutes % 60
                val hoursTarget = habit.targetMinutes / 60

                Text(
                    text = "${hoursCompleted}h ${minsCompleted}m / ${hoursTarget}h target",
                    color = colors.text.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                Row {
                    IconButton(onClick = { onToggleDone(!habit.isDone) }) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = if (habit.isDone) "Mark as incomplete" else "Mark as complete",
                            tint = if (habit.isDone) phaseColor else colors.text.copy(alpha = 0.3f)
                        )
                    }
                    Button(
                        onClick = onStartFocus,
                        colors = ButtonDefaults.buttonColors(containerColor = phaseColor.copy(alpha = 0.2f)),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = phaseColor
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Focus", color = phaseColor, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
