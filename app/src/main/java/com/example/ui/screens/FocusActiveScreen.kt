package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.ui.platform.testTag
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FocusViewModel
import com.example.ui.components.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import com.example.ui.components.GlassSurface

@Composable
fun FocusActiveScreen(viewModel: FocusViewModel) {
    val accentColor = Color(android.graphics.Color.parseColor(viewModel.accentColorHex))

    // Formatted minutes & seconds string
    val mins = viewModel.timerSecondsLeft / 60
    val secs = viewModel.timerSecondsLeft % 60
    val timerStr = String.format("%02d:%02d", mins, secs)

    // Rotating pulse animation for central clock cube
    val infiniteTransition = rememberInfiniteTransition(label = "timer_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(viewModel.themeBg)
            .systemBarsPadding()
    ) {
        MicroCubeBackground(viewModel)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Info
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 24.dp)) {
                Text(
                    text = viewModel.timerLabel.uppercase(),
                    color = accentColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (viewModel.isTimerPaused) "SESSION PAUSED" else "DEEP WORK ACTIVE",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Central rotating & pulsing Clock Cube Widget
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .graphicsLayer {
                        scaleX = pulseScale
                        scaleY = pulseScale
                    },
                contentAlignment = Alignment.Center
            ) {
                // Beautiful custom GlassProgressRing with glow and backdrop blur
                GlassProgressRing(
                    progress = viewModel.timerProgress,
                    accentColor = accentColor,
                    strokeWidth = 10.dp,
                    modifier = Modifier.fillMaxSize()
                )

                // Core 3D Cube centered inside
                RotatingBook3D(
                    bookSize = 150.dp,
                    accentColor = accentColor,
                    viewModel = viewModel
                )
            }

            // Timer Digital Counter in JetBrains Mono / monospace style font
            Text(
                text = timerStr,
                color = Color.White,
                fontSize = 54.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-1).sp
            )

            // Sleek Linear GlassProgressBar with aura glow and backdrop blur
            GlassProgressBar(
                progress = viewModel.timerProgress,
                accentColor = accentColor,
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .padding(vertical = 8.dp),
                showPercentage = true,
                textColor = Color.White
            )

            // Ambient White Noise Sound Switcher
            GlassSurface(
                colors = CardDefaults.cardColors(
                    containerColor = if (viewModel.isWhiteNoiseEnabled) 
                        accentColor.copy(alpha = 0.12f) 
                    else 
                        Color(0x08FFFFFF)
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .clickable { viewModel.toggleWhiteNoise() }
                    .border(
                        1.dp, 
                        if (viewModel.isWhiteNoiseEnabled) accentColor.copy(alpha = 0.35f) else Color(0x0EFFFFFF), 
                        RoundedCornerShape(20.dp)
                    )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = if (viewModel.isWhiteNoiseEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                        contentDescription = "White Noise",
                        tint = if (viewModel.isWhiteNoiseEnabled) accentColor else Color(0xFF7A7A9A),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = if (viewModel.isWhiteNoiseEnabled) "Ambient Noise Active" else "Ambient Noise Muted",
                        color = if (viewModel.isWhiteNoiseEnabled) Color.White else Color(0xFF7A7A9A),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.3.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    GlassToggle(
                        checked = viewModel.isWhiteNoiseEnabled,
                        onCheckedChange = { viewModel.toggleWhiteNoise() },
                        modifier = Modifier.scale(0.8f)
                    )
                }
            }

            if (viewModel.isWhiteNoiseEnabled) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 8.dp)
                ) {
                    val sounds = listOf("Brown Noise", "White Noise", "Rain", "Forest", "Cafe", "Lo-Fi Beats", "Comfort Chain")
                    sounds.forEach { sound ->
                        val isSelected = viewModel.selectedAmbientSound == sound
                        Box(
                            modifier = Modifier
                                .testTag("ambient_sound_${sound.lowercase().replace(" ", "_")}")
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) accentColor.copy(alpha = 0.25f)
                                    else Color(0x0AFFFFFF)
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) accentColor else Color(0x15FFFFFF),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    viewModel.selectAmbientSound(sound)
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = sound,
                                color = if (isSelected) Color.White else Color(0xFF9A9A9A),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Control Buttons Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Button 1: End session early
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color(0x0EFFFFFF))
                        .clickable { viewModel.stopTimerEarly() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Stop",
                        tint = AccentPink,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Button 2: Pause / Resume Play
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .background(accentColor)
                        .clickable {
                            if (viewModel.isTimerPaused) {
                                viewModel.resumeTimer()
                            } else {
                                viewModel.pauseTimer()
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (viewModel.isTimerPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                        contentDescription = if (viewModel.isTimerPaused) "Resume" else "Pause",
                        tint = if (viewModel.isDarkMode) Color.Black else Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Button 3: Add 5 Minutes (+5m)
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color(0x0EFFFFFF))
                        .clickable { viewModel.addFiveMinutes() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add 5m",
                        tint = AccentYellow,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
