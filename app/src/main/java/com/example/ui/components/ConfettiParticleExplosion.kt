package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.testTag
import com.example.data.FocusViewModel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Shape types for particles in the confetti / particle explosion.
 */
enum class ParticleShape {
    CIRCLE,
    RECTANGLE,
    STAR,
    DIAMOND
}

private data class Particle(
    val x0: Float,
    val y0: Float,
    val vx: Float,
    val vy: Float,
    val gravity: Float,
    val color: Color,
    val shape: ParticleShape,
    val size: Float,
    val rotationSpeed: Float,
    val initialRotation: Float
)

/**
 * High-performance, satisfying particle explosion and confetti animation
 * triggered via state transitions when a task or habit is completed.
 */
@Composable
fun ConfettiParticleExplosion(
    triggerKey: Long,
    modifier: Modifier = Modifier,
    particleCount: Int = 60,
    burstOriginRatio: Offset = Offset(0.5f, 0.5f)
) {
    if (triggerKey <= 0L) return

    val progress = remember(triggerKey) { Animatable(0f) }
    val particles = remember(triggerKey) {
        val colors = listOf(
            Color(0xFFFFD700), // Gold
            Color(0xFF10B981), // Emerald Green
            Color(0xFF06B6D4), // Cyan
            Color(0xFF8B5CF6), // Purple
            Color(0xFFEC4899), // Pink
            Color(0xFFF97316), // Orange
            Color(0xFF3B82F6)  // Vibrant Blue
        )
        val shapes = ParticleShape.values()

        List(particleCount) {
            val angle = Random.nextFloat() * 2f * PI.toFloat()
            // Radial burst velocity with some upward bias
            val speed = Random.nextFloat() * 1200f + 400f
            val vx = speed * cos(angle) * Random.nextFloat()
            val vy = speed * sin(angle) - Random.nextFloat() * 600f // upward boost
            val gravity = Random.nextFloat() * 900f + 700f

            Particle(
                x0 = 0f,
                y0 = 0f,
                vx = vx,
                vy = vy,
                gravity = gravity,
                color = colors[Random.nextInt(colors.size)],
                shape = shapes[Random.nextInt(shapes.size)],
                size = Random.nextFloat() * 18f + 10f,
                rotationSpeed = (Random.nextFloat() - 0.5f) * 1080f,
                initialRotation = Random.nextFloat() * 360f
            )
        }
    }

    LaunchedEffect(triggerKey) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1400, easing = LinearEasing)
        )
    }

    val currentProgress = progress.value
    if (currentProgress >= 1f) return

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .testTag("confetti_particle_canvas")
    ) {
        val centerX = size.width * burstOriginRatio.x
        val centerY = size.height * burstOriginRatio.y
        val t = currentProgress * 1.3f // normalized time factor
        val alpha = (1f - (currentProgress * currentProgress)).coerceIn(0f, 1f)

        particles.forEach { p ->
            val curX = centerX + p.vx * t
            val curY = centerY + p.vy * t + 0.5f * p.gravity * t * t
            val curRotation = p.initialRotation + p.rotationSpeed * t

            val particleAlpha = alpha * p.color.alpha

            withTransform({
                translate(left = curX, top = curY)
                rotate(degrees = curRotation, pivot = Offset.Zero)
            }) {
                val drawColor = p.color.copy(alpha = particleAlpha)
                when (p.shape) {
                    ParticleShape.CIRCLE -> {
                        drawCircle(
                            color = drawColor,
                            radius = p.size / 2f,
                            center = Offset.Zero
                        )
                    }
                    ParticleShape.RECTANGLE -> {
                        drawRect(
                            color = drawColor,
                            topLeft = Offset(-p.size / 2f, -p.size / 4f),
                            size = Size(p.size, p.size / 2f)
                        )
                    }
                    ParticleShape.DIAMOND -> {
                        val path = Path().apply {
                            moveTo(0f, -p.size / 2f)
                            lineTo(p.size / 2f, 0f)
                            lineTo(0f, p.size / 2f)
                            lineTo(-p.size / 2f, 0f)
                            close()
                        }
                        drawPath(path, color = drawColor)
                    }
                    ParticleShape.STAR -> {
                        drawStar(p.size, drawColor)
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawStar(size: Float, color: Color) {
    val outerRadius = size / 2f
    val innerRadius = outerRadius * 0.4f
    val path = Path()
    val numPoints = 5

    for (i in 0 until numPoints * 2) {
        val r = if (i % 2 == 0) outerRadius else innerRadius
        val angle = (i * PI / numPoints).toFloat() - (PI / 2).toFloat()
        val x = r * cos(angle)
        val y = r * sin(angle)
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    drawPath(path, color = color)
}

/**
 * Global confetti overlay helper connected directly to FocusViewModel's
 * confetti trigger timestamp.
 */
@Composable
fun ViewModelConfettiOverlay(viewModel: FocusViewModel) {
    val triggerKey by viewModel.confettiTrigger.collectAsState(initial = 0L)
    Box(modifier = Modifier.fillMaxSize()) {
        ConfettiParticleExplosion(
            triggerKey = triggerKey,
            modifier = Modifier.fillMaxSize(),
            particleCount = 70
        )
    }
}
