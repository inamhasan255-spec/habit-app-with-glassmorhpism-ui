package com.focuscube.ui.dashboard

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import android.os.Build
import androidx.compose.ui.graphics.asComposeRenderEffect

@Composable
fun AnimatedMeshBackground(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "mesh")
    val offset1 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(10000, easing = LinearEasing), RepeatMode.Reverse),
        label = "offset1"
    )
    val offset2 by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(12000, easing = LinearEasing), RepeatMode.Reverse),
        label = "offset2"
    )

    Canvas(modifier = modifier
        .fillMaxSize()
        .graphicsLayer {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                renderEffect = android.graphics.RenderEffect
                    .createBlurEffect(150f, 150f, android.graphics.Shader.TileMode.MIRROR)
                    .asComposeRenderEffect()
            }
        }
    ) {
        val w = size.width
        val h = size.height

        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFF1E1E2E), Color(0xFF12121A)),
                start = Offset(0f, 0f),
                end = Offset(w, h)
            )
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF7E57C2).copy(alpha = 0.5f), Color.Transparent),
                center = Offset(w * offset1, h * 0.3f),
                radius = w * 0.8f
            ),
            radius = w * 0.8f,
            center = Offset(w * offset1, h * 0.3f)
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF00E5FF).copy(alpha = 0.4f), Color.Transparent),
                center = Offset(w * offset2, h * 0.7f),
                radius = w * 0.7f
            ),
            radius = w * 0.7f,
            center = Offset(w * offset2, h * 0.7f)
        )
    }
}
