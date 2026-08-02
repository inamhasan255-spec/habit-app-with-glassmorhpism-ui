package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.withTransform
import kotlin.random.Random
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

data class Star(
    var x: Float,
    var y: Float,
    val size: Float,
    val speedBase: Float,
    val angle: Float,
    val hueOffset: Float,
    val twinkleSpeed: Float
)

@Composable
fun GalaxyBackground(
    isVisible: Boolean = true,
    starSpeed: Float = 1.0f,
    hueShift: Float = 0.0f,
    isDarkTheme: Boolean = true,
    modifier: Modifier = Modifier
) {
    if (!isVisible) return

    val starCount = 150
    val stars = remember {
        List(starCount) {
            Star(
                x = Random.nextFloat(), // 0.0 to 1.0 relative
                y = Random.nextFloat(), // 0.0 to 1.0 relative
                size = Random.nextFloat() * 4f + 1f,
                speedBase = Random.nextFloat() * 0.002f + 0.0005f,
                angle = Random.nextFloat() * 360f,
                hueOffset = Random.nextFloat() * 60f,
                twinkleSpeed = Random.nextFloat() * 0.05f + 0.01f
            )
        }
    }

    var time by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(starSpeed) {
        var lastTime = withFrameMillis { it }
        while (isActive) {
            val currentTime = withFrameMillis { it }
            val delta = (currentTime - lastTime) / 1000f
            lastTime = currentTime
            time += delta * starSpeed
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        
        // Base hue (e.g. standard purple/blue)
        val baseHue = (240f + hueShift) % 360f

        stars.forEach { star ->
            // Move star slightly based on time and speed
            val offsetX = Math.cos(star.angle.toDouble()).toFloat() * star.speedBase * time * width
            val offsetY = Math.sin(star.angle.toDouble()).toFloat() * star.speedBase * time * height
            
            var drawX = (star.x * width + offsetX) % width
            var drawY = (star.y * height + offsetY) % height
            
            if (drawX < 0) drawX += width
            if (drawY < 0) drawY += height

            // Twinkle effect
            val twinkle = (Math.sin((time * star.twinkleSpeed).toDouble()).toFloat() + 1f) / 2f
            val currentSize = star.size * (0.5f + 0.5f * twinkle)

            // Convert hue to RGB (roughly, using standard Color utilities or just hardcoded HSL approximation)
            // For simplicity, we just tint white with some blue/purple/hueShift
            // In Android Compose, Color.hsv is available
            val finalHue = (baseHue + star.hueOffset) % 360f
            val starColor = if (isDarkTheme) {
                androidx.compose.ui.graphics.Color.hsv(
                    hue = finalHue,
                    saturation = 0.6f,
                    value = 1f,
                    alpha = 0.3f + 0.7f * twinkle
                )
            } else {
                androidx.compose.ui.graphics.Color.hsv(
                    hue = finalHue,
                    saturation = 0.8f,
                    value = 0.4f,
                    alpha = 0.4f + 0.6f * twinkle
                )
            }

            drawCircle(
                color = starColor,
                radius = currentSize,
                center = Offset(drawX, drawY)
            )
        }
    }
}
