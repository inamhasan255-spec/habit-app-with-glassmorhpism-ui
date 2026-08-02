package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.withTransform
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun CelebrationBurst(
    isVisible: Boolean,
    onAnimationEnd: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (!isVisible) return

    val particleCount = 60
    val particles = remember {
        List(particleCount) {
            CelebrationParticle(
                color = listOf(
                    Color(0xFF8B5CF6), Color(0xFFEC4899),
                    Color(0xFF10B981), Color(0xFFF59E0B),
                    Color(0xFF06B6D4)
                ).random(),
                angle = Random.nextFloat() * 2 * PI.toFloat(),
                speed = Random.nextFloat() * 1200f + 600f,
                size = Random.nextFloat() * 20f + 10f,
                rotationSpeed = Random.nextFloat() * 720f - 360f,
                shapeType = Random.nextInt(3) // 0=Rect, 1=Circle, 2=Triangle
            )
        }
    }

    val progress = remember { Animatable(0f) }

    LaunchedEffect(isVisible) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 2000, easing = FastOutSlowInEasing)
        )
        onAnimationEnd()
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val center = Offset(size.width / 2, size.height / 2)

        particles.forEach { particle ->
            val distance = particle.speed * progress.value * (1f - progress.value * 0.3f)
            val currentX = center.x + cos(particle.angle) * distance
            val currentY = center.y + sin(particle.angle) * distance
            
            // Add gravity effect pulling down
            val gravityOffset = 800f * progress.value * progress.value
            val finalY = currentY + gravityOffset

            // Fade out at the end
            val alpha = if (progress.value > 0.7f) {
                (1f - progress.value) / 0.3f
            } else 1f

            val rotation = particle.rotationSpeed * progress.value

            withTransform({
                translate(left = currentX, top = finalY)
                rotate(rotation)
                scale(scaleX = alpha, scaleY = alpha)
            }) {
                when (particle.shapeType) {
                    0 -> drawRect(
                        color = particle.color,
                        topLeft = Offset(-particle.size / 2, -particle.size / 2),
                        size = Size(particle.size, particle.size)
                    )
                    1 -> drawCircle(
                        color = particle.color,
                        radius = particle.size / 2,
                        center = Offset.Zero
                    )
                    2 -> {
                        val path = Path().apply {
                            moveTo(0f, -particle.size / 2)
                            lineTo(particle.size / 2, particle.size / 2)
                            lineTo(-particle.size / 2, particle.size / 2)
                            close()
                        }
                        drawPath(path = path, color = particle.color)
                    }
                }
            }
        }
    }
}

private data class CelebrationParticle(
    val color: Color,
    val angle: Float,
    val speed: Float,
    val size: Float,
    val rotationSpeed: Float,
    val shapeType: Int
)
