package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import androidx.core.content.ContextCompat.startActivity
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FocusViewModel
import com.example.ui.components.*
import kotlinx.coroutines.delay

@Composable
fun FocusCompleteScreen(viewModel: FocusViewModel) {
    val accentColor = Color(android.graphics.Color.parseColor(viewModel.accentColorHex))

    // Infinite spinning animation for the unlock cube icon
    val infiniteTransition = rememberInfiniteTransition(label = "spinning_complete")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Animated vertical confetti simulation on Canvas
    val confettiList = remember {
        List(40) {
            ConfettiParticle(
                x = (0..100).random() / 100f,
                y = (0..100).random() / 100f,
                speed = (15..35).random() / 1000f,
                color = listOf(AccentTeal, AccentPink, AccentYellow, AccentLavender, AccentPeach).random(),
                size = (5..14).random().dp
            )
        }
    }

    var ticker by remember { mutableStateOf(0L) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(32)
            ticker++
            for (c in confettiList) {
                c.y += c.speed
                if (c.y > 1f) {
                    c.y = 0f
                    c.x = (0..100).random() / 100f
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(viewModel.themeBg)
            .drawBehind {
                // Draw falling vertical confetti particles
                for (c in confettiList) {
                    val xPx = c.x * size.width
                    val yPx = c.y * size.height
                    val sz = c.size.toPx()
                    
                    val path = Path().apply {
                        moveTo(xPx, yPx - sz/2)
                        lineTo(xPx + sz/2, yPx)
                        lineTo(xPx, yPx + sz/2)
                        lineTo(xPx - sz/2, yPx)
                        close()
                    }
                    drawPath(
                        path = path,
                        color = c.color.copy(alpha = 0.8f)
                    )
                }
            }
            .systemBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                
            .clip(RoundedCornerShape(24.dp))
            .background(viewModel.themeGlassFill)
            .border(1.dp, viewModel.themeGlassBorder, RoundedCornerShape(24.dp))
            .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Checkmark
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Session Saved",
                    tint = accentColor,
                    modifier = Modifier.size(54.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "FOCUS BLOCK SECURED",
                    color = accentColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
            }

            // Spinning Unlock Hero Cube
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .graphicsLayer {
                        rotationZ = rotation
                    },
                contentAlignment = Alignment.Center
            ) {
                RotatingBook3D(
                    bookSize = 140.dp,
                    accentColor = accentColor,
                    viewModel = viewModel
                )
            }

            // Session completion stats card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
            .padding(24.dp)
    ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Ecosystem Sync Complete",
                        color = viewModel.themeText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Your completed session is logged under the ${viewModel.timerLabel} stack.",
                        color = viewModel.themeTextSecondary,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "DURATION", color = viewModel.themeTextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(text = "${viewModel.timerTotalMinutes}m", color = viewModel.themeText, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "STREAK", color = viewModel.themeTextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(text = "${viewModel.getStreakCount()} 🔥", color = accentColor, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "ECOSYSTEM", color = viewModel.themeTextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(text = "SECURED", color = AccentPink, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                }
            }

            val context = LocalContext.current

            // Save & Continue CTA
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                com.example.ui.components.RainbowButton(
                    onClick = {
                        val shareIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "I just completed a ${viewModel.timerTotalMinutes}-minute focus session on FocusCube! Current Streak: ${viewModel.getStreakCount()}🔥 #FocusCube #Productivity")
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Session"))
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(16.dp))
                        Text(
                            text = "SHARE WORKOUT",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }

                com.example.ui.components.GlassButton(
                    onClick = { viewModel.completeAndSaveSession() },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = "SAVE & DISMISS CUBE",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = viewModel.themeText
                    )
                }
            }
        }
        
        CelebrationBurst(isVisible = true)
    }
}

private class ConfettiParticle(
    var x: Float,
    var y: Float,
    val speed: Float,
    val color: Color,
    val size: androidx.compose.ui.unit.Dp
)
