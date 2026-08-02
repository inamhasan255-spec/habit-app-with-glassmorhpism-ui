package com.example.ui.components

import com.example.data.FocusViewModel
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import com.example.ui.components.GlassSurface

// Theme Colors
val DeepBg = Color(0xFF0A0A12)
val SurfaceBg = Color(0xFF12121F)
val GlassFill = Color(0x0EFFFFFF)
val GlassBorder = Color(0x1BFFFFFF)

// Accents
val AccentTeal = Color(0xFF2DD4BF)
val AccentPink = Color(0xFFF472B6)
val AccentYellow = Color(0xFFFACC15)
val AccentLavender = Color(0xFFC9B8FF)
val AccentPeach = Color(0xFFFFD0B5)

/**
 * Reusable AntiGravityWrapper that oscillates up and down slowly.
 */
@Composable
fun AntiGravityWrapper(
    modifier: Modifier = Modifier,
    durationMillis: Int = 4000,
    delayMillis: Int = 0,
    offsetRange: Float = 12f,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "anti_gravity")
    val offsetY by infiniteTransition.animateFloat(
        initialValue = -offsetRange,
        targetValue = offsetRange,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, delayMillis = delayMillis, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offset"
    )

    Box(
        modifier = modifier.graphicsLayer {
            translationY = offsetY
        }
    ) {
        content()
    }
}

/**
 * Reusable GlassCard replicating a high-end glassmorphism look.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    borderColor: Color = GlassBorder,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "press_scale"
    )

    val cardModifier = if (onClick != null) {
        modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(24.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    } else {
        modifier.clip(RoundedCornerShape(24.dp))
    }

    Box(
        modifier = cardModifier
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0x14FFFFFF),
                        Color(0x06FFFFFF)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        borderColor,
                        borderColor.copy(alpha = 0.1f)
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(1.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}

/**
 * Press-squish button mimicking high tactile feedback.
 */
@Composable
fun PressSquishButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = AccentTeal,
    contentColor: Color = Color(0xFF003829),
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium),
        label = "press_scale"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(18.dp))
            .background(containerColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 24.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}

data class Voxel(
    val x: Float,
    val y: Float,
    val z: Float,
    val isDrifting: Boolean,
    val driftSpeed: Float,
    val driftSeed: Float,
    val colorT: Float,
    val sparkleSeed: Float
)

class ProjectedVoxel {
    var px: Float = 0f
    var py: Float = 0f
    var depth: Float = 0f
    var color: Color = Color.Unspecified
}

/**
 * Custom Painter based 3D Isometric Cube replaced with a Purple/Magenta Voxel Particle Cube.
 */
@Composable
fun IsometricCube3D(
    modifier: Modifier = Modifier,
    cubeSize: Dp = 120.dp,
    accentColor: Color = Color(0xFFA855F7),
    faceTextTop: String = "HABITS",
    faceTextLeft: String = "SHIELD",
    faceTextRight: String = "STATS",
    shieldActive: Boolean = false,
    onFaceTapped: (face: String) -> Unit = {},
    onClick: () -> Unit = {},
    expandFraction: Float = 0f
) {
    val context = LocalContext.current
    var tiltX by remember { mutableStateOf(0f) }
    var tiltY by remember { mutableStateOf(0f) }

    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event != null && event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                    val rawX = event.values[0]
                    val rawY = event.values[1]
                    tiltX = tiltX * 0.9f + rawX * 0.1f
                    tiltY = tiltY * 0.9f + rawY * 0.1f
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        if (accelerometer != null) {
            sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_UI)
        }
        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    // Continuous auto-rotation animation
    val infiniteTransition = rememberInfiniteTransition(label = "voxel_cube_rotation")
    val autoAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(14000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "auto_angle"
    )

    // Gentle breathing scale pulse
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = if (shieldActive) 0.93f else 0.96f,
        targetValue = if (shieldActive) 1.07f else 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (shieldActive) 2200 else 4500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cube_pulse"
    )

    // Drifting phase
    val driftTime by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = PI.toFloat() * 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "drift_time"
    )

    // Sparkle phase
    val sparklePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = PI.toFloat() * 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sparkle_phase"
    )

    // Deterministic voxel grid with radial density falloff
    val voxels = remember {
        val list = mutableListOf<Voxel>()
        val random = java.util.Random(1337)
        for (gx in -2..2) {
            for (gy in -2..2) {
                for (gz in -2..2) {
                    val dist = kotlin.math.sqrt((gx * gx + gy * gy + gz * gz).toFloat())
                    val maxDist = kotlin.math.sqrt(12f)
                    
                    // Radial density falloff (density is high at center, lower at edges)
                    val keepProb = 1.0f - (dist / (maxDist * 1.05f))
                    if (random.nextFloat() < keepProb) {
                        val isDrifting = random.nextFloat() < 0.18f // 18% of voxels drift
                        list.add(
                            Voxel(
                                x = gx.toFloat(),
                                y = gy.toFloat(),
                                z = gz.toFloat(),
                                isDrifting = isDrifting,
                                driftSpeed = 0.5f + random.nextFloat() * 1.5f,
                                driftSeed = random.nextFloat() * PI.toFloat() * 2f,
                                colorT = dist / maxDist,
                                sparkleSeed = random.nextFloat()
                            )
                        )
                    }
                }
            }
        }
        list
    }

    // Pre-allocated list for high-performance zero-allocation rendering optimization
    val projectedList = remember(voxels) {
        val list = ArrayList<ProjectedVoxel>(voxels.size)
        repeat(voxels.size) { list.add(ProjectedVoxel()) }
        list
    }
    val depthComparator = remember { Comparator<ProjectedVoxel> { a, b -> a.depth.compareTo(b.depth) } }
    val edgeHighlightPath = remember { Path() }

    Box(
        modifier = modifier
            .size(cubeSize)
            .graphicsLayer {
                scaleX = pulseScale
                scaleY = pulseScale
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    val cx = size.width / 2f
                    val cy = size.height / 2f
                    
                    // Soft ambient drop shadow beneath the cube on a light lavender backdrop glow
                    val shadowRadius = size.width * 0.35f
                    
                    // Background light lavender backdrop radial glow
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFD8B4FE).copy(alpha = 0.22f), 
                                Color(0xFFEC4899).copy(alpha = 0.08f), 
                                Color.Transparent
                            ),
                            center = Offset(cx, cy),
                            radius = size.width * 0.55f
                        ),
                        radius = size.width * 0.55f,
                        center = Offset(cx, cy)
                    )

                    // Soft ambient drop shadow oval beneath the cube
                    drawOval(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFF1E1B4B).copy(alpha = 0.35f), Color.Transparent),
                            center = Offset(cx, cy + size.height * 0.42f),
                            radius = shadowRadius
                        ),
                        topLeft = Offset(cx - shadowRadius, cy + size.height * 0.38f),
                        size = androidx.compose.ui.geometry.Size(shadowRadius * 2, shadowRadius * 0.15f)
                    )

                    // Trailing particle effect behind the Purple Voxel Cube when moving during expansion
                    if (expandFraction > 0.01f) {
                        val particleCount = 20
                        for (i in 0 until particleCount) {
                            val angle = i * (2f * PI.toFloat() / particleCount) + (autoAngle * 2f * PI.toFloat() / 360f)
                            
                            // Spiral progress staggered by phase for dynamic trailing movement
                            val phase = (i % 4) * 0.25f
                            val pProgress = (expandFraction + phase) % 1.0f
                            
                            val baseRadius = size.width * 0.12f
                            val maxRadius = size.width * 0.52f
                            val radius = baseRadius + (maxRadius - baseRadius) * pProgress
                            
                            val px = cx + cos(angle) * radius
                            val py = cy + sin(angle) * radius
                            
                            // Alpha transitions to peak mid-flight, enhancing trail/assembling look
                            val pAlpha = (sin(pProgress * PI.toFloat()) * 0.65f * expandFraction).coerceIn(0f, 1f)
                            val pSize = size.width * 0.024f * (1f - pProgress * 0.4f)
                            
                            drawCircle(
                                color = accentColor.copy(alpha = pAlpha),
                                radius = pSize,
                                center = Offset(px, py)
                            )
                            
                            // Outer subtle glowing halo for each assembler particle
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(accentColor.copy(alpha = pAlpha * 0.35f), Color.Transparent),
                                    center = Offset(px, py),
                                    radius = pSize * 2.5f
                                ),
                                radius = pSize * 2.5f,
                                center = Offset(px, py)
                            )
                        }
                    }

                    try {
                        // Voxel spacing based on canvas dimensions
                        val spacing = size.width * 0.13f
                        
                        // Color definition stops
                        val cViolet = Color(0xFFA855F7)
                        val cPink = Color(0xFFEC4899)
                        val cLavender = Color(0xFFD8B4FE)

                        // Angles for rotation
                        val tiltAngleY = (-tiltX * 2.5f) * PI.toFloat() / 180f
                        val tiltAngleX = (tiltY * 2.5f) * PI.toFloat() / 180f
                        
                        val angleY = autoAngle * PI.toFloat() / 180f + tiltAngleY
                        val angleX = 20f * PI.toFloat() / 180f + tiltAngleX

                        // Project voxels into pre-allocated objects safely
                        val limit = minOf(voxels.size, projectedList.size)
                        for (i in 0 until limit) {
                            val voxel = voxels[i]
                            val projected = projectedList[i]
                            
                            var vx = voxel.x * spacing
                            var vy = voxel.y * spacing
                            var vz = voxel.z * spacing

                            // If drifting, apply scattering offset
                            var alphaFactor = 1.0f
                            if (voxel.isDrifting) {
                                val driftOffset = sin(driftTime * voxel.driftSpeed + voxel.driftSeed) * (spacing * 0.6f)
                                vx += driftOffset * cos(voxel.driftSeed)
                                vy += driftOffset * sin(voxel.driftSeed)
                                vz += driftOffset * sin(voxel.driftSeed * 1.5f)
                                alphaFactor = 0.5f // reduced alpha for scatter effect
                            }

                            // 3D rotation around Y
                            val cosY = cos(angleY)
                            val sinY = sin(angleY)
                            val x1 = vx * cosY - vz * sinY
                            val z1 = vx * sinY + vz * cosY

                            // 3D rotation around X
                            val cosX = cos(angleX)
                            val sinX = sin(angleX)
                            val y1 = vy * cosX - z1 * sinX
                            val z2 = vy * sinX + z1 * cosX // Depth

                            // Voxel sparkle variation
                            val sparkle = sin(sparklePhase * 1.5f + voxel.sparkleSeed * 10f) * 0.15f
                            
                            // Color interpolation: violet -> pink -> light lavender
                            val baseColor = if (voxel.colorT < 0.5f) {
                                val frac = voxel.colorT * 2f
                                Color(
                                    red = (cViolet.red + (cPink.red - cViolet.red) * frac).coerceIn(0f, 1f),
                                    green = (cViolet.green + (cPink.green - cViolet.green) * frac).coerceIn(0f, 1f),
                                    blue = (cViolet.blue + (cPink.blue - cViolet.blue) * frac).coerceIn(0f, 1f)
                                )
                            } else {
                                val frac = (voxel.colorT - 0.5f) * 2f
                                Color(
                                    red = (cPink.red + (cLavender.red - cPink.red) * frac).coerceIn(0f, 1f),
                                    green = (cPink.green + (cLavender.green - cPink.green) * frac).coerceIn(0f, 1f),
                                    blue = (cPink.blue + (cLavender.blue - cPink.blue) * frac).coerceIn(0f, 1f)
                                )
                            }

                            val finalColor = baseColor.copy(
                                red = (baseColor.red + sparkle).coerceIn(0f, 1f),
                                green = (baseColor.green + sparkle).coerceIn(0f, 1f),
                                blue = (baseColor.blue + sparkle).coerceIn(0f, 1f),
                                alpha = (0.85f * alphaFactor).coerceIn(0f, 1f)
                            )

                            // Projected 2D Point
                            projected.px = cx + x1
                            projected.py = cy + y1
                            projected.depth = z2
                            projected.color = finalColor
                        }

                        // Sort voxels in-place by depth ascending (painter's algorithm)
                        projectedList.sortWith(depthComparator)

                        // Clear and prepare batched path for highlights to optimize draw operations
                        edgeHighlightPath.reset()

                        // Draw voxels from back to front
                        for (i in projectedList.indices) {
                            val projected = projectedList[i]
                            val px = projected.px
                            val py = projected.py
                            val color = projected.color
                            val voxelSize = size.width * 0.12f
                            
                            drawRoundRect(
                                color = color,
                                topLeft = Offset(px - voxelSize / 2f, py - voxelSize / 2f),
                                size = Size(voxelSize, voxelSize),
                                cornerRadius = CornerRadius(voxelSize * 0.25f)
                            )

                            // Batch subtle edge highlight on each voxel using addRoundRect
                            edgeHighlightPath.addRoundRect(
                                RoundRect(
                                    rect = Rect(
                                        offset = Offset(px - voxelSize / 2f, py - voxelSize / 2f),
                                        size = Size(voxelSize, voxelSize)
                                    ),
                                    cornerRadius = CornerRadius(voxelSize * 0.25f)
                                )
                            )
                        }

                        // Perform single high-performance batched draw call for all wireframe lines/highlights
                        drawPath(
                            path = edgeHighlightPath,
                            color = Color.White.copy(alpha = 0.25f),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1f)
                        )
                    } catch (e: Exception) {
                        // Safe Fallback rendering / error boundary to prevent dashboard crashes
                        val fallbackSize = size.width * 0.35f
                        
                        // Outer glowing aura
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(accentColor.copy(alpha = 0.5f), Color.Transparent),
                                center = Offset(cx, cy),
                                radius = fallbackSize * 2f
                            ),
                            radius = fallbackSize * 2f,
                            center = Offset(cx, cy)
                        )
                        
                        // Main fallback sphere
                        drawCircle(
                            color = accentColor,
                            radius = fallbackSize,
                            center = Offset(cx, cy)
                        )
                        
                        // Bright inner core
                        drawCircle(
                            color = Color.White.copy(alpha = 0.8f),
                            radius = fallbackSize * 0.4f,
                            center = Offset(cx - fallbackSize * 0.2f, cy - fallbackSize * 0.2f)
                        )
                    }
                }
        )
    }
}

/**
 * Floating drifting particles background simulating micro-cubes.
 */
@Composable
fun MicroCubeBackground(viewModel: FocusViewModel, modifier: Modifier = Modifier) {
    val particles = remember {
        List(25) {
            ParticleState(
                initialX = (0..1000).random().toFloat() / 1000f,
                initialY = (0..1000).random().toFloat() / 1000f,
                speed = (10..30).random().toFloat() / 100000f,
                size = (4..12).random().dp,
                alpha = (2..8).random().toFloat() / 10f,
                color = listOf(AccentTeal, AccentPink, AccentYellow, AccentLavender, AccentPeach).random()
            )
        }
    }

    // Ticker to animate positions
    var ticker by remember { mutableStateOf(0L) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(32)
            ticker++
            for (p in particles) {
                p.y -= p.speed
                if (p.y < 0f) {
                    p.y = 1f
                    p.x = (0..1000).random().toFloat() / 1000f
                }
            }
        }
    }

    val gridColor = if (viewModel.isDarkMode) Color(0x06FFFFFF) else Color(0x0A000000)

    Box(
        modifier = modifier
            .fillMaxSize()
            .drawBehind {
                // Background technical grid network lines
                val cols = 8
                val rows = 12
                val colWidth = size.width / cols
                val rowHeight = size.height / rows
                
                // Draw sophisticated subtle grid lines
                for (i in 0..cols) {
                    drawLine(
                        color = gridColor,
                        start = androidx.compose.ui.geometry.Offset(i * colWidth, 0f),
                        end = androidx.compose.ui.geometry.Offset(i * colWidth, size.height),
                        strokeWidth = 1f
                    )
                }
                for (j in 0..rows) {
                    drawLine(
                        color = gridColor,
                        start = androidx.compose.ui.geometry.Offset(0f, j * rowHeight),
                        end = androidx.compose.ui.geometry.Offset(size.width, j * rowHeight),
                        strokeWidth = 1f
                    )
                }

                // Draw micro particles
                for (p in particles) {
                    val x = p.x * size.width
                    val y = p.y * size.height
                    val sz = p.size.toPx()
                    
                    // Draw tiny isometric wireframe diamond representation using rotation of canvas (highly optimized, zero-allocations)
                    rotate(degrees = 45f, pivot = Offset(x, y)) {
                        drawRoundRect(
                            color = p.color.copy(alpha = p.alpha * 0.25f),
                            topLeft = Offset(x - sz / 2f, y - sz / 2f),
                            size = Size(sz, sz),
                            cornerRadius = CornerRadius(sz * 0.15f)
                        )
                    }
                }
            }
    )
}

private class ParticleState(
    var initialX: Float,
    var initialY: Float,
    val speed: Float,
    val size: Dp,
    val alpha: Float,
    val color: Color
) {
    var x by mutableStateOf(initialX)
    var y by mutableStateOf(initialY)
}

/**
 * A beautiful, highly-polished 3D/2.5D Rotating Book with flipping pages
 * inspired by Perplexity AI's geometric page-turning animations.
 */
@Composable
fun RotatingBook3D(
    modifier: Modifier = Modifier,
    bookSize: Dp = 120.dp,
    accentColor: Color = AccentTeal,
    viewModel: FocusViewModel
) {
    val infiniteTransition = rememberInfiniteTransition(label = "book_flip")
    
    // Page flip progress from 0.0 (fully right) to 1.0 (fully left)
    val pageProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "page_progress"
    )

    // Base book spin around Y-axis for full 3D feel
    val baseSpinAngle by infiniteTransition.animateFloat(
        initialValue = -12f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "base_spin"
    )

    val isDark = viewModel.isDarkMode
    val pageColor = if (isDark) Color(0xFF1E1E2C) else Color(0xFFFFFFFF)
    val coverColor = accentColor.copy(alpha = 0.85f)
    val spineColor = accentColor
    val textColor = (if (isDark) Color.White else Color.Black).copy(alpha = 0.15f)
    val wFactor = 0.42f
    val hFactor = 0.55f

    Box(
        modifier = modifier
            .size(bookSize)
            .graphicsLayer {
                rotationY = baseSpinAngle
                cameraDistance = 8 * density
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            
            val w = size.width * wFactor
            val h = size.height * hFactor
            
            // Draw Book Cover Shadow
            drawRoundRect(
                color = Color.Black.copy(alpha = 0.22f),
                topLeft = androidx.compose.ui.geometry.Offset(cx - w - 8f, cy - h / 2f + 12f),
                size = androidx.compose.ui.geometry.Size(w * 2f + 16f, h + 12f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f)
            )

            // 1. Draw Left and Right Covers
            // Left Cover
            val leftCoverPath = Path().apply {
                moveTo(cx, cy + h / 2f + 6f)
                cubicTo(
                    cx - w * 0.5f, cy + h / 2f + 8f,
                    cx - w, cy + h / 2f + 4f,
                    cx - w - 6f, cy + h / 2f - 4f
                )
                lineTo(cx - w - 6f, cy - h / 2f - 4f)
                cubicTo(
                    cx - w, cy - h / 2f + 4f,
                    cx - w * 0.5f, cy - h / 2f + 8f,
                    cx, cy - h / 2f + 6f
                )
                close()
            }
            drawPath(path = leftCoverPath, color = coverColor)

            // Right Cover
            val rightCoverPath = Path().apply {
                moveTo(cx, cy + h / 2f + 6f)
                cubicTo(
                    cx + w * 0.5f, cy + h / 2f + 8f,
                    cx + w, cy + h / 2f + 4f,
                    cx + w + 6f, cy + h / 2f - 4f
                )
                lineTo(cx + w + 6f, cy - h / 2f - 4f)
                cubicTo(
                    cx + w, cy - h / 2f + 4f,
                    cx + w * 0.5f, cy - h / 2f + 8f,
                    cx, cy - h / 2f + 6f
                )
                close()
            }
            drawPath(path = rightCoverPath, color = coverColor)

            // 2. Draw Stacked Page Thickness (depth effect)
            for (i in 1..3) {
                val offsetFactor = i * 2.5f
                val leftThicknessPath = Path().apply {
                    moveTo(cx, cy + h / 2f + 2f - offsetFactor)
                    cubicTo(
                        cx - w * 0.5f, cy + h / 2f + 4f - offsetFactor,
                        cx - w, cy + h / 2f - offsetFactor,
                        cx - w, cy + h / 2f - 4f - offsetFactor
                    )
                    lineTo(cx - w, cy - h / 2f - 4f - offsetFactor)
                    cubicTo(
                        cx - w, cy - h / 2f - offsetFactor,
                        cx - w * 0.5f, cy - h / 2f + 4f - offsetFactor,
                        cx, cy - h / 2f + 2f - offsetFactor
                    )
                    close()
                }
                drawPath(path = leftThicknessPath, color = pageColor.copy(alpha = 0.5f + (i * 0.1f)))

                val rightThicknessPath = Path().apply {
                    moveTo(cx, cy + h / 2f + 2f - offsetFactor)
                    cubicTo(
                        cx + w * 0.5f, cy + h / 2f + 4f - offsetFactor,
                        cx + w, cy + h / 2f - offsetFactor,
                        cx + w, cy + h / 2f - 4f - offsetFactor
                    )
                    lineTo(cx + w, cy - h / 2f - 4f - offsetFactor)
                    cubicTo(
                        cx + w, cy - h / 2f - offsetFactor,
                        cx + w * 0.5f, cy - h / 2f + 4f - offsetFactor,
                        cx, cy - h / 2f + 2f - offsetFactor
                    )
                    close()
                }
                drawPath(path = rightThicknessPath, color = pageColor.copy(alpha = 0.5f + (i * 0.1f)))
            }

            // 3. Draw Top Left Page
            val leftPagePath = Path().apply {
                moveTo(cx, cy + h / 2f)
                cubicTo(
                    cx - w * 0.5f, cy + h / 2f + 3f,
                    cx - w + 2f, cy + h / 2f - 2f,
                    cx - w + 2f, cy + h / 2f - 8f
                )
                lineTo(cx - w + 2f, cy - h / 2f - 8f)
                cubicTo(
                    cx - w + 2f, cy - h / 2f - 2f,
                    cx - w * 0.5f, cy - h / 2f + 3f,
                    cx, cy - h / 2f
                )
                close()
            }
            drawPath(path = leftPagePath, color = pageColor)

            // 4. Draw Top Right Page
            val rightPagePath = Path().apply {
                moveTo(cx, cy + h / 2f)
                cubicTo(
                    cx + w * 0.5f, cy + h / 2f + 3f,
                    cx + w - 2f, cy + h / 2f - 2f,
                    cx + w - 2f, cy + h / 2f - 8f
                )
                lineTo(cx + w - 2f, cy - h / 2f - 8f)
                cubicTo(
                    cx + w - 2f, cy - h / 2f - 2f,
                    cx + w * 0.5f, cy - h / 2f + 3f,
                    cx, cy - h / 2f
                )
                close()
            }
            drawPath(path = rightPagePath, color = pageColor)

            // 5. Draw Book Spine (center fold line)
            drawLine(
                color = spineColor.copy(alpha = 0.7f),
                start = androidx.compose.ui.geometry.Offset(cx, cy - h / 2f - 2f),
                end = androidx.compose.ui.geometry.Offset(cx, cy + h / 2f + 4f),
                strokeWidth = 3f
            )

            // 6. Draw Simulated Text lines on left and right pages
            // Left page lines
            for (lineIdx in 0..4) {
                val lineY = cy - h / 2f + 25f + (lineIdx * 16f)
                val lineStart = cx - w * 0.8f
                val lineEnd = cx - w * 0.2f
                drawLine(
                    color = textColor,
                    start = androidx.compose.ui.geometry.Offset(lineStart, lineY),
                    end = androidx.compose.ui.geometry.Offset(lineEnd, lineY),
                    strokeWidth = 2.5f
                )
            }
            // Right page lines
            for (lineIdx in 0..4) {
                val lineY = cy - h / 2f + 25f + (lineIdx * 16f)
                val lineStart = cx + w * 0.2f
                val lineEnd = cx + w * 0.8f
                drawLine(
                    color = textColor,
                    start = androidx.compose.ui.geometry.Offset(lineStart, lineY),
                    end = androidx.compose.ui.geometry.Offset(lineEnd, lineY),
                    strokeWidth = 2.5f
                )
            }

            // 7. Draw Flipping/Turning Page (inspired by Perplexity AI)
            val p = pageProgress
            val angleRad = p * PI // From 0 (right) to PI (left)
            
            val liftHeight = sin(angleRad).toFloat() * (h * 0.22f)
            val warpDepth = sin(angleRad).toFloat() * (w * 0.15f)

            val fOuterX = cx + w * cos(angleRad).toFloat()
            val fTopOuterY = cy - h / 2f - 8f - liftHeight
            val fBottomOuterY = cy + h / 2f - 8f - liftHeight

            val spineTopX = cx
            val spineTopY = cy - h / 2f
            val spineBottomX = cx
            val spineBottomY = cy + h / 2f

            // Create Path for the flipping page
            val flippingPagePath = Path().apply {
                moveTo(spineTopX, spineTopY)
                
                // Curve from top spine to top outer corner
                val ctrlTopX = (spineTopX + fOuterX) / 2f + (if (p < 0.5f) warpDepth else -warpDepth)
                val ctrlTopY = (spineTopY + fTopOuterY) / 2f - liftHeight * 0.5f
                quadraticTo(ctrlTopX, ctrlTopY, fOuterX, fTopOuterY)
                
                // Straight edge down
                lineTo(fOuterX, fBottomOuterY)
                
                // Curve from bottom outer corner back to bottom spine
                val ctrlBottomX = (spineBottomX + fOuterX) / 2f + (if (p < 0.5f) warpDepth else -warpDepth)
                val ctrlBottomY = (spineBottomY + fBottomOuterY) / 2f - liftHeight * 0.5f
                quadraticTo(ctrlBottomX, ctrlBottomY, spineBottomX, spineBottomY)
                
                close()
            }

            // Draw shadow under the turning page
            val shadowPath = Path().apply {
                moveTo(spineTopX, spineTopY)
                val sOuterX = cx + w * 0.85f * cos(angleRad).toFloat()
                val sTopY = cy - h / 2f + 10f
                val sBottomY = cy + h / 2f + 10f
                lineTo(sOuterX, sTopY)
                lineTo(sOuterX, sBottomY)
                lineTo(spineBottomX, spineBottomY)
                close()
            }
            drawPath(
                path = shadowPath,
                color = Color.Black.copy(alpha = 0.12f * sin(angleRad).toFloat())
            )

            // Draw turning page with gradient
            val flippingGradient = Brush.horizontalGradient(
                colors = listOf(
                    pageColor.copy(alpha = 0.95f),
                    pageColor.copy(alpha = 0.85f),
                    pageColor.copy(alpha = 0.95f)
                ),
                startX = cx - w,
                endX = cx + w
            )
            drawPath(path = flippingPagePath, color = pageColor)
            drawPath(path = flippingPagePath, brush = flippingGradient)

            // Add thin border highlight on the turning page
            val borderHighlightColor = if (p < 0.5f) {
                accentColor.copy(alpha = 0.3f * (1f - p))
            } else {
                accentColor.copy(alpha = 0.3f * p)
            }
            
            drawPath(
                path = flippingPagePath,
                color = borderHighlightColor,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5f)
            )

            // Draw text lines on the flipping page
            if (sin(angleRad) > 0.2f) {
                for (lineIdx in 1..3) {
                    val pageHeightFraction = 0.25f + (lineIdx * 0.15f)
                    val pSpineY = spineTopY + (spineBottomY - spineTopY) * pageHeightFraction
                    val pOuterY = fTopOuterY + (fBottomOuterY - fTopOuterY) * pageHeightFraction
                    
                    val textLinePath = Path().apply {
                        moveTo(spineTopX, pSpineY)
                        val ctrlX = (spineTopX + fOuterX) / 2f + (if (p < 0.5f) warpDepth else -warpDepth)
                        val ctrlY = (pSpineY + pOuterY) / 2f - liftHeight * 0.5f
                        quadraticTo(ctrlX, ctrlY, fOuterX, pOuterY)
                    }
                    drawPath(
                        path = textLinePath,
                        color = accentColor.copy(alpha = 0.22f * sin(angleRad).toFloat()),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
                    )
                }
            }
        }
    }
}

@Composable
fun LiquidGlassmorphismContainer(
    modifier: Modifier = Modifier,
    isDarkMode: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "liquid_glass_morphism")
    
    // Slow, organic movement parameters for 4 distinct fluid blobs
    val blob1X by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blob1_x"
    )
    val blob1Y by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(11000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blob1_y"
    )
    
    val blob2X by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(9500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blob2_x"
    )
    val blob2Y by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(7500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blob2_y"
    )

    val blob3X by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(13000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blob3_x"
    )
    val blob3Y by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(9000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blob3_y"
    )
    
    val blob4X by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(10500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blob4_x"
    )
    val blob4Y by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blob4_y"
    )

    Box(
        modifier = modifier
    ) {
        // Draw the moving liquid blobs
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height
            
            // Draw background dark slate/indigo void
            drawRect(
                color = if (isDarkMode) Color(0xFF0F0E1D) else Color(0xFFF3F1FA)
            )
            
            // Fluid Blob 1: Deep Violet/Indigo
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF8B5CF6).copy(alpha = if (isDarkMode) 0.32f else 0.45f),
                        Color.Transparent
                    ),
                    center = Offset(w * blob1X, h * blob1Y),
                    radius = w * 0.55f
                ),
                radius = w * 0.55f,
                center = Offset(w * blob1X, h * blob1Y)
            )

            // Fluid Blob 2: Hot Magenta/Pink
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFEC4899).copy(alpha = if (isDarkMode) 0.26f else 0.4f),
                        Color.Transparent
                    ),
                    center = Offset(w * blob2X, h * blob2Y),
                    radius = w * 0.48f
                ),
                radius = w * 0.48f,
                center = Offset(w * blob2X, h * blob2Y)
            )

            // Fluid Blob 3: Neon Cyan/Blue
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF06B6D4).copy(alpha = if (isDarkMode) 0.22f else 0.35f),
                        Color.Transparent
                    ),
                    center = Offset(w * blob3X, h * blob3Y),
                    radius = w * 0.52f
                ),
                radius = w * 0.52f,
                center = Offset(w * blob3X, h * blob3Y)
            )

            // Fluid Blob 4: Warm Tangerine/Gold
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFF59E0B).copy(alpha = if (isDarkMode) 0.16f else 0.3f),
                        Color.Transparent
                    ),
                    center = Offset(w * blob4X, h * blob4Y),
                    radius = w * 0.45f
                ),
                radius = w * 0.45f,
                center = Offset(w * blob4X, h * blob4Y)
            )
        }
        
        // Premium Real-time Frosted Glassmorphism Layer overlay
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    if (isDarkMode) Color(0x73111024) else Color(0x99FFFFFF)
                )
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = if (isDarkMode) 0.08f else 0.3f),
                            Color.White.copy(alpha = if (isDarkMode) 0.02f else 0.1f)
                        )
                    )
                )
        )
        
        // Inner Glow specular highlights and custom glass edge reflections
        Box(
            modifier = Modifier
                .matchParentSize()
                .drawBehind {
                    // Thin top-down high-contrast glass specular edge
                    drawLine(
                        color = Color.White.copy(alpha = if (isDarkMode) 0.22f else 0.55f),
                        start = Offset(0f, 0f),
                        end = Offset(size.width, 0f),
                        strokeWidth = 2f
                    )
                    // Soft specular side lights
                    drawLine(
                        color = Color.White.copy(alpha = if (isDarkMode) 0.08f else 0.25f),
                        start = Offset(0f, 0f),
                        end = Offset(0f, size.height),
                        strokeWidth = 1f
                    )
                }
        )
        
        // Content overlay
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}
