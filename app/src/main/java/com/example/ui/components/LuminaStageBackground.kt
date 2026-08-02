package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.R
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun LuminaStageBackground(
    isDarkMode: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "lumina_bg_anim")

    // Animating floating offsets for organic blobs
    val float1Val by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * kotlin.math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(18000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "blob1_rotation"
    )

    val float2Val by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * kotlin.math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(22000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "blob2_rotation"
    )

    val float3Val by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * kotlin.math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(26000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "blob3_rotation"
    )

    val blob1Offset = Offset(
        x = cos(float1Val) * 50f - 20f,
        y = sin(float1Val) * 40f - 30f
    )

    val blob2Offset = Offset(
        x = sin(float2Val) * 60f + 30f,
        y = cos(float2Val) * 50f + 20f
    )

    val blob3Offset = Offset(
        x = cos(float3Val) * 40f + 10f,
        y = sin(float3Val) * 60f - 10f
    )

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Base whole-app background image (sunset torii gate cherry blossom image)
        Image(
            painter = painterResource(id = R.drawable.img_sunset_background_1782636423915),
            contentDescription = "Whole App Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Overlay ambient vignette tint
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = if (isDarkMode) {
                            listOf(
                                Color(0x66070512),
                                Color(0x880A0817),
                                Color(0xCC070512)
                            )
                        } else {
                            listOf(
                                Color(0x11000000),
                                Color(0x22000000),
                                Color(0x55000000)
                            )
                        }
                    )
                )
        )

        // High-performance canvas for ambient radial spots and animated float blobs
        Box(
            modifier = Modifier
                .fillMaxSize()
                .blur(80.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawWithContent {
                        // Drawing static top-right violet-indigo glow
                        drawRect(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF2A1A5E).copy(alpha = if (isDarkMode) 0.35f else 0.15f),
                                    Color.Transparent
                                ),
                                center = Offset(size.width * 0.8f, size.height * -0.1f),
                                radius = size.minDimension * 1.5f
                            )
                        )

                        // Drawing static bottom-left dark-teal glow
                        drawRect(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF0D3A4A).copy(alpha = if (isDarkMode) 0.35f else 0.15f),
                                    Color.Transparent
                                ),
                                center = Offset(size.width * -0.1f, size.height * 1.1f),
                                radius = size.minDimension * 1.3f
                            )
                        )

                        // Animated Blob 1: Violet/Indigo sphere
                        val b1Center = Offset(
                            x = size.width * -0.08f + blob1Offset.x * (size.width / 360f),
                            y = size.height * -0.12f + blob1Offset.y * (size.height / 360f)
                        )
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF7B5CFF).copy(alpha = if (isDarkMode) 0.35f else 0.15f),
                                    Color.Transparent
                                ),
                                center = b1Center,
                                radius = size.minDimension * 0.6f
                            ),
                            center = b1Center,
                            radius = size.minDimension * 0.6f
                        )

                        // Animated Blob 2: Fuchsia/Pink sphere
                        val b2Center = Offset(
                            x = size.width * 1.06f + blob2Offset.x * (size.width / 360f),
                            y = size.height * 0.28f + blob2Offset.y * (size.height / 360f)
                        )
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFFF7AB6).copy(alpha = if (isDarkMode) 0.35f else 0.15f),
                                    Color.Transparent
                                ),
                                center = b2Center,
                                radius = size.minDimension * 0.5f
                            ),
                            center = b2Center,
                            radius = size.minDimension * 0.5f
                        )

                        // Animated Blob 3: Mint/Teal sphere
                        val b3Center = Offset(
                            x = size.width * 0.45f + blob3Offset.x * (size.width / 360f),
                            y = size.height * 0.95f + blob3Offset.y * (size.height / 360f)
                        )
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF34E0C8).copy(alpha = if (isDarkMode) 0.25f else 0.10f),
                                    Color.Transparent
                                ),
                                center = b3Center,
                                radius = size.minDimension * 0.7f
                            ),
                            center = b3Center,
                            radius = size.minDimension * 0.7f
                        )
                    }
            )
        }
    }
}

