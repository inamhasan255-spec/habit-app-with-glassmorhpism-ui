package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LuminaTheme

/**
 * A linear Glass ProgressBar utilizing a premium white glass aesthetic.
 * Features:
 * - Semi-transparent white glass container
 * - Backdrop-blur simulation
 * - Outer soft glow emitting from behind the progress track
 * - High-transparency white borders for refractive glass look
 * - Sleek glass-shine horizontal highlights
 */
@Composable
fun GlassProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    accentColor: Color = Color(0xFF3B82F6),
    height: Dp = 16.dp,
    showPercentage: Boolean = false,
    textColor: Color = Color.White
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(
            durationMillis = 1000,
            easing = LinearEasing
        ),
        label = "glass_progress_anim"
    )

    val colors = LuminaTheme.colors

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (showPercentage) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "${(animatedProgress * 100).toInt()}%",
                    color = textColor.copy(alpha = 0.9f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height + 12.dp), // Extra spacing for glow
            contentAlignment = Alignment.Center
        ) {
            // 1. Aura Glow Layer (placed behind, heavily blurred, color-matched to accent)
            if (animatedProgress > 0.01f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(height + 10.dp)
                        .blur(radius = 12.dp)
                        .padding(horizontal = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(animatedProgress)
                            .clip(RoundedCornerShape(height / 2))
                            .background(accentColor.copy(alpha = 0.45f))
                    )
                }
            }

            // 2. White Glass Container Track
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height)
                    .clip(RoundedCornerShape(height / 2))
                    .background(colors.glassFill.copy(alpha = 0.15f))
                    .border(
                        1.dp,
                        colors.glassBorder,
                        RoundedCornerShape(height / 2)
                    )
            ) {
                // Backdrop-blurred overlay within the track
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .blur(radius = 6.dp)
                        .background(colors.glassFill.copy(alpha = 0.1f))
                )

                // 3. Progress Filled Bar
                if (animatedProgress > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(animatedProgress)
                            .padding(2.dp)
                            .clip(RoundedCornerShape((height - 4.dp) / 2))
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        accentColor.copy(alpha = 0.75f),
                                        accentColor,
                                        accentColor.copy(alpha = 0.95f)
                                    )
                                )
                            )
                    ) {
                        // Gloss/Shine Highlight Line on progress fill
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(height / 5)
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            colors.text.copy(alpha = 0.45f),
                                            Color.Transparent
                                        )
                                    )
                                )
                                .align(Alignment.TopStart)
                        )
                    }
                }
            }
        }
    }
}

/**
 * A circular Glass ProgressRing (circular progress indicator) utilizing a premium white glass aesthetic.
 * Perfect for centering inside clock views or timers.
 * Features:
 * - Glassmorphic ring background with soft inner translucent outline
 * - Accent-colored progress ring with high-intensity glowing aura behind it
 * - Rounded end caps for modern design feel
 */
@Composable
fun GlassProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    accentColor: Color = Color(0xFF3B82F6),
    strokeWidth: Dp = 10.dp,
    glowRadius: Dp = 14.dp
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "glass_ring_anim"
    )
    
    val colors = LuminaTheme.colors
    val infiniteTransition = rememberInfiniteTransition(label = "ring_gradient_pulse")
    
    // Smooth fluid rotation for the liquid gradient
    val gradientRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "gradient_rotation"
    )

    // Pulsing alpha for the glowing aura
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    // Physics fluid bubble simulation variables
    val fluidOffset1 by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(animation = tween(2000, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse),
        label = "fluid_1"
    )
    
    val fluidOffset2 by infiniteTransition.animateFloat(
        initialValue = 3f,
        targetValue = -3f,
        animationSpec = infiniteRepeatable(animation = tween(2500, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse),
        label = "fluid_2"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // 1. Soft Glow Aura (blurred copy behind the main ring)
        if (animatedProgress > 0.01f) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .blur(radius = glowRadius)
            ) {
                val sizePx = size.minDimension
                val halfStroke = strokeWidth.toPx()
                val radius = (sizePx - halfStroke * 2) / 2f
                val centerOffset = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height / 2f)

                val gradientBrush = Brush.sweepGradient(
                    colors = listOf(
                        accentColor,
                        colors.text.copy(alpha = 0.8f),
                        accentColor,
                        accentColor.copy(alpha = 0.5f),
                        accentColor
                    ),
                    center = centerOffset
                )

                rotate(degrees = gradientRotation, pivot = centerOffset) {
                    drawArc(
                        brush = gradientBrush,
                        startAngle = -90f - gradientRotation,
                        sweepAngle = 360f * animatedProgress,
                        useCenter = false,
                        topLeft = androidx.compose.ui.geometry.Offset(centerOffset.x - radius, centerOffset.y - radius),
                        size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                        style = Stroke(width = halfStroke * 1.5f, cap = StrokeCap.Round),
                        alpha = pulseAlpha
                    )
                }
            }
        }

        // 2. White Glass Base Ring & Front Animated Fluid Ring
        Canvas(modifier = Modifier.fillMaxSize()) {
            val sizePx = size.minDimension
            val strokePx = strokeWidth.toPx()
            val radius = (sizePx - strokePx) / 2f
            val centerOffset = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height / 2f)

            // Backdrop white glass base ring
            drawCircle(
                color = colors.glassFill.copy(alpha = 0.15f),
                radius = radius,
                style = Stroke(width = strokePx)
            )

            
            // Calculate active color with slight tinting based on progress
            val activeColor = accentColor
            
            // Fluid dynamic brush
            val gradientBrush = Brush.sweepGradient(
                colors = listOf(
                    activeColor,
                    colors.text.copy(alpha = 0.95f), // Bright highlight
                    activeColor,
                    activeColor.copy(alpha = 0.6f),
                    activeColor
                ),
                center = centerOffset
            )

            // Dynamic accent progress arc on top
            rotate(degrees = gradientRotation, pivot = centerOffset) {
                // Main thick track
                drawArc(
                    brush = gradientBrush,
                    startAngle = -90f - gradientRotation,
                    sweepAngle = 360f * animatedProgress,
                    useCenter = false,
                    topLeft = androidx.compose.ui.geometry.Offset(centerOffset.x - radius, centerOffset.y - radius),
                    size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
                
                // Add some inner 'fluid bubbles' when running
                if (animatedProgress > 0f && animatedProgress < 1f) {
                     drawCircle(
                        color = Color.White.copy(alpha = 0.3f), // reduced opacity
                        radius = strokePx / 5, // smaller bubbles
                        center = androidx.compose.ui.geometry.Offset(centerOffset.x + fluidOffset1, centerOffset.y - radius + fluidOffset2)
                     )
                     drawCircle(
                        color = Color.White.copy(alpha = 0.2f),
                        radius = strokePx / 6,
                        center = androidx.compose.ui.geometry.Offset(centerOffset.x - fluidOffset2, centerOffset.y - radius + fluidOffset1)
                     )
                }
            }
            
            // Draw head bubble (fluid indicator)
            if (animatedProgress > 0f && animatedProgress < 1f) {
                // Calculate position of the end cap
                val endAngle = -90f + (360f * animatedProgress)
                val endAngleRad = Math.toRadians(endAngle.toDouble())
                
                // Add slight fluid offset to head position
                val headX = centerOffset.x + (radius + fluidOffset2) * Math.cos(endAngleRad).toFloat()
                val headY = centerOffset.y + (radius + fluidOffset1) * Math.sin(endAngleRad).toFloat()
                
                drawCircle(
                    color = Color.White,
                    radius = strokePx * 0.6f, // reduced size slightly
                    center = androidx.compose.ui.geometry.Offset(headX, headY)
                )
                
                // Inner highlight for head bubble
                drawCircle(
                    color = activeColor,
                    radius = strokePx * 0.25f,
                    center = androidx.compose.ui.geometry.Offset(headX, headY)
                )
            }
            // Subtle frosted-inner overlay stroke to make it look glassy
            drawCircle(
                color = colors.glassBorder.copy(alpha = 0.4f),
                radius = radius - strokePx / 2f,
                style = Stroke(width = 1.dp.toPx())
            )
            drawCircle(
                color = colors.glassBorder.copy(alpha = 0.4f),
                radius = radius + strokePx / 2f,
                style = Stroke(width = 1.dp.toPx())
            )
        }
    }
}
