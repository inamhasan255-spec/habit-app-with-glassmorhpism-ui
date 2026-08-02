package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.sin

enum class VisualizerState {
    IDLE,
    LISTENING,
    THINKING,
    SPEAKING
}

@Composable
fun AudioVisualizer(
    state: VisualizerState,
    modifier: Modifier = Modifier,
    accentColor: Color = Color(0xFF10B981) // Emerald Green default
) {
    val infiniteTransition = rememberInfiniteTransition(label = "audio_visualizer")
    
    // Core animation values
    val phaseShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase_shift"
    )

    val scalePulse by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val thinkingRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Canvas(modifier = modifier.size(100.dp)) {
        val width = size.width
        val height = size.height
        val centerX = width / 2
        val centerY = height / 2
        val maxRadius = (width.coerceAtMost(height) / 2) * scalePulse

        when (state) {
            VisualizerState.IDLE -> {
                // --- IDLE STATE ---
                // Clean breathing outer aura and soft center dot
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(accentColor.copy(alpha = 0.25f), Color.Transparent),
                        center = Offset(centerX, centerY),
                        radius = maxRadius
                    )
                )
                drawCircle(
                    color = accentColor,
                    radius = maxRadius * 0.3f,
                    center = Offset(centerX, centerY)
                )
            }
            VisualizerState.LISTENING -> {
                // --- LISTENING STATE (Reactive Waves) ---
                // Multi-layered pulsing orbs with sine-wave overlays
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFEF4444).copy(alpha = 0.2f), Color.Transparent),
                        center = Offset(centerX, centerY),
                        radius = maxRadius * 1.2f
                    )
                )

                // Render 3 animated overlapping orbital sine waves
                for (layer in 0..2) {
                    val alpha = 0.7f - (layer * 0.2f)
                    val strokeWidth = (3 - layer).dp.toPx()
                    val waveColor = Color(0xFFEF4444).copy(alpha = alpha)
                    val frequency = 2f + layer
                    val amplitude = 12.dp.toPx() * (1f - layer * 0.25f)
                    val layerPhase = phaseShift + (layer * Math.PI.toFloat() / 3f)

                    val path = Path()
                    path.moveTo(0f, centerY)

                    var x = 0f
                    while (x <= width) {
                        // Blend wave into circular orb bounds using a bell-curve envelope
                        val envelope = sin((x / width) * Math.PI.toFloat())
                        val y = centerY + sin((x / width) * frequency * Math.PI.toFloat() + layerPhase) * amplitude * envelope
                        if (x == 0f) {
                            path.moveTo(x, y)
                        } else {
                            path.lineTo(x, y)
                        }
                        x += 4f
                    }
                    drawPath(path = path, color = waveColor, style = Stroke(width = strokeWidth))
                }

                // Center glowing recording core
                drawCircle(
                    color = Color(0xFFEF4444),
                    radius = maxRadius * 0.35f,
                    center = Offset(centerX, centerY)
                )
            }
            VisualizerState.THINKING -> {
                // --- THINKING STATE (Swirling Aurora/Gemini Gradient) ---
                // Siri/Gemini style beautiful spinning gradient ring
                val gradientBrush = Brush.sweepGradient(
                    colors = listOf(
                        Color(0xFF3B82F6), // Blue
                        Color(0xFF8B5CF6), // Purple
                        Color(0xFFEC4899), // Pink
                        Color(0xFF3B82F6)  // Wrap around Blue
                    ),
                    center = Offset(centerX, centerY)
                )

                // Swirling outer ring
                drawCircle(
                    brush = gradientBrush,
                    radius = maxRadius * 0.85f,
                    center = Offset(centerX, centerY),
                    style = Stroke(width = 5.dp.toPx())
                )

                // Soft inner spinning nebula
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF8B5CF6).copy(alpha = 0.35f), Color.Transparent),
                        center = Offset(centerX, centerY),
                        radius = maxRadius * 0.7f
                    )
                )
            }
            VisualizerState.SPEAKING -> {
                // --- SPEAKING STATE (Vibrant EQ Waves) ---
                // 3 pulsing waveforms intersecting in an orb
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF10B981).copy(alpha = 0.2f), Color.Transparent),
                        center = Offset(centerX, centerY),
                        radius = maxRadius * 1.1f
                    )
                )

                for (layer in 0..2) {
                    val alpha = 0.8f - (layer * 0.2f)
                    val waveColor = Color(0xFF10B981).copy(alpha = alpha)
                    val freq = 3.5f + (layer * 1.5f)
                    val amp = 24.dp.toPx() * (1f - layer * 0.3f)
                    val layerPhase = phaseShift * 1.5f - (layer * Math.PI.toFloat() / 4f)

                    val path = Path()
                    var x = 0f
                    while (x <= width) {
                        val envelope = sin((x / width) * Math.PI.toFloat())
                        val y = centerY + sin((x / width) * freq * Math.PI.toFloat() + layerPhase) * amp * envelope
                        if (x == 0f) {
                            path.moveTo(x, y)
                        } else {
                            path.lineTo(x, y)
                        }
                        x += 4f
                    }
                    drawPath(path = path, color = waveColor, style = Stroke(width = 3.dp.toPx()))
                }

                // Center green speaker core
                drawCircle(
                    color = Color(0xFF10B981),
                    radius = maxRadius * 0.3f,
                    center = Offset(centerX, centerY)
                )
            }
        }
    }
}
