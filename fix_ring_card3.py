with open("app/src/main/java/com/example/ui/screens/HomeDashboardScreen.kt", "r") as f:
    text = f.read()

import re

new_func = """
@Composable
fun LuminaFocusProgressRingCard(
    viewModel: com.example.data.FocusViewModel,
    accentColor: Color
) {
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    
    // Connect to actual viewmodel timer state
    val isTimerRunning = viewModel.focusSessionState == com.example.data.Models.FocusSessionState.RUNNING || 
                         viewModel.focusSessionState == com.example.data.Models.FocusSessionState.PAUSED
                         
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
"""

# Regex replacing the old component - since there's no nested functions inside
text = re.sub(r'@Composable\nfun LuminaFocusProgressRingCard\(.*?\n\}\n\}', new_func, text, flags=re.DOTALL)

with open("app/src/main/java/com/example/ui/screens/HomeDashboardScreen.kt", "w") as f:
    f.write(text)

