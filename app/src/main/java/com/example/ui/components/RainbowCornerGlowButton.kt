package com.example.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.max
import java.util.Calendar



enum class GlowTheme {
    Default,
    Dynamic,
    Morning,
    Afternoon,
    Evening,
    Night
}

@Composable
fun RainbowCornerGlowButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    glowColor: Color? = null,
    isBreathing: Boolean = false,
    theme: GlowTheme = GlowTheme.Dynamic
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val pressedAlpha by animateFloatAsState(
        targetValue = if (isPressed) 1f else 0f,
        animationSpec = tween(150),
        label = "glowAlpha"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "breathing")
    val breathingAlpha by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathingAlpha"
    )

    val glowAlpha = if (isPressed) pressedAlpha else if (isBreathing) max(pressedAlpha, breathingAlpha) else pressedAlpha

    val glowSpread = 20.dp
    val cornerRadius = 28.dp

    val currentHour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    val resolvedTheme = if (theme == GlowTheme.Dynamic) {
        when (currentHour) {
            in 5..11 -> GlowTheme.Morning
            in 12..17 -> GlowTheme.Afternoon
            in 18..20 -> GlowTheme.Evening
            else -> GlowTheme.Night
        }
    } else theme

    // Outer box adds padding so the glow doesn't get clipped
    Box(
        modifier = modifier.padding(glowSpread),
        contentAlignment = Alignment.Center
    ) {
        // Glow layer: sits behind the button, same size as the padded box
        Box(
            modifier = Modifier
                .matchParentSize()
                .drawBehind {
                    if (glowAlpha > 0.001f) {
                        val r = cornerRadius.toPx()
                        val spread = glowSpread.toPx()

                        // Centers of the four corner arcs
                        val centers = listOf(
                            Offset(r, r),                                    // Top-left
                            Offset(size.width - r, r),                       // Top-right
                            Offset(r, size.height - r),                      // Bottom-left
                            Offset(size.width - r, size.height - r)          // Bottom-right
                        )

                        val colors = if (glowColor != null) {
                            listOf(glowColor, glowColor, glowColor, glowColor)
                        } else {
                            when (resolvedTheme) {
                                GlowTheme.Morning -> listOf(
                                    Color(0xFFFFB74D), // Warm Orange
                                    Color(0xFFFF8A65), // Coral
                                    Color(0xFF4FC3F7), // Light Blue
                                    Color(0xFF81C784)  // Soft Green
                                )
                                GlowTheme.Afternoon -> listOf(
                                    Color(0xFFFF5252), // Red
                                    Color(0xFFFFAB40), // Orange
                                    Color(0xFF64FFDA), // Teal
                                    Color(0xFF448AFF)  // Blue
                                )
                                GlowTheme.Evening -> listOf(
                                    Color(0xFF9575CD), // Deep Purple
                                    Color(0xFFE57373), // Muted Red
                                    Color(0xFF4DB6AC), // Teal
                                    Color(0xFF7986CB)  // Indigo
                                )
                                GlowTheme.Night -> listOf(
                                    Color(0xFF5E35B1), // Deep Violet
                                    Color(0xFF3949AB), // Deep Blue
                                    Color(0xFF00ACC1), // Cyan
                                    Color(0xFF039BE5)  // Light Blue
                                )
                                else -> listOf(
                                    Color(0xFFFF0040), // Red
                                    Color(0xFFFF8000), // Orange
                                    Color(0xFF00FF80), // Green
                                    Color(0xFF0080FF)  // Blue
                                )
                            }
                        }

                        centers.forEachIndexed { i, center ->
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        colors[i].copy(alpha = 0.85f * glowAlpha),
                                        colors[i].copy(alpha = 0f)
                                    ),
                                    center = center,
                                    radius = r + spread
                                ),
                                center = center,
                                radius = r + spread
                            )
                        }
                    }
                }
        )

        // Button face
        Box(
            modifier = Modifier
                .clickable(
                    interactionSource = interactionSource,
                    indication = null, // Removes default ripple
                    enabled = enabled,
                    onClick = onClick
                )
                .background(
                    color = Color(0xFF141419).copy(alpha = 0.9f),
                    shape = RoundedCornerShape(9999.dp)
                )
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(9999.dp)
                )
                .padding(horizontal = 36.dp, vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = text,
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 16.sp
                )
            }
        }
    }
}
