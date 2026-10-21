package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FocusViewModel
import com.example.ui.theme.softGlassShadow
import com.example.ui.theme.LuminaTheme
import kotlin.math.roundToInt

@Composable
fun FloatingAmbientSoundscapeController(
    viewModel: FocusViewModel,
    modifier: Modifier = Modifier
) {
    val colors = LuminaTheme.colors
    var isExpanded by remember { mutableStateOf(false) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    val isPlaying = viewModel.isWhiteNoisePlayingAnytime || (viewModel.isTimerRunning && !viewModel.isTimerPaused && viewModel.isWhiteNoiseEnabled)
    val selectedSound = viewModel.selectedAmbientSound
    val volume = viewModel.comfortChainVolume

    val visualizerData by viewModel.whiteNoiseVisualizerFlow.collectAsState(initial = FloatArray(8) { 0.2f })

    val infiniteTransition = rememberInfiniteTransition(label = "ambient_controller_glow")
    val ambientPulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ambient_glow_pulse"
    )

    Box(
        modifier = modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    offsetX += dragAmount.x
                    offsetY += dragAmount.y
                }
            }
            .testTag("floating_ambient_soundscape_controller")
    ) {
        AnimatedContent(
            targetState = isExpanded,
            transitionSpec = {
                (fadeIn(tween(300)) + scaleIn(initialScale = 0.8f, animationSpec = spring(stiffness = Spring.StiffnessLow))) togetherWith
                        (fadeOut(tween(200)) + scaleOut(targetScale = 0.8f))
            },
            label = "ambient_controller_state"
        ) { expanded ->
            if (!expanded) {
                // Collapsed Compact Glass Pill Controller (Movable / Draggable)
                Box(
                    modifier = Modifier
                        .softGlassShadow(
                            elevation = 12.dp,
                            shape = RoundedCornerShape(28.dp),
                            isDark = viewModel.isDarkMode
                        )
                        .clip(RoundedCornerShape(28.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    colors.glassFill,
                                    colors.glassFill.copy(alpha = 0.85f)
                                )
                            )
                        )
                        .border(
                            width = 1.dp,
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    if (isPlaying) Color(0xFF8B5CF6).copy(alpha = ambientPulseAlpha) else colors.glassBorder,
                                    if (isPlaying) Color(0xFFEC4899).copy(alpha = ambientPulseAlpha) else colors.glassBorder
                                )
                            ),
                            shape = RoundedCornerShape(28.dp)
                        )
                        .clickable { isExpanded = true }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val textColor = LuminaTheme.colors.text
                        val textSecondaryColor = LuminaTheme.colors.textSecondary

                        // Drag handle grip icon
                        Icon(
                            imageVector = Icons.Default.DragHandle,
                            contentDescription = "Drag to move soundscape",
                            tint = textSecondaryColor.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp)
                        )

                        // Icon with glowing ring when playing
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isPlaying) {
                                        Brush.linearGradient(
                                            colors = listOf(Color(0xFF8B5CF6), Color(0xFFEC4899))
                                        )
                                    } else {
                                        Brush.linearGradient(
                                            colors = listOf(textColor.copy(alpha = 0.1f), textColor.copy(alpha = 0.05f))
                                        )
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Rounded.GraphicEq else Icons.Rounded.MusicNote,
                                contentDescription = "Ambient Sound",
                                tint = if (isPlaying) Color.White else textColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Sound Name & Status
                        Column {
                            Text(
                                text = selectedSound,
                                color = textColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isPlaying) "Playing • Drag to move" else "Muted • Drag to move",
                                color = if (isPlaying) Color(0xFF8B5CF6) else textSecondaryColor,
                                fontSize = 10.sp
                            )
                        }

                        // Mini Audio Wave Bar Visualizer
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.height(16.dp)
                        ) {
                            for (i in 0 until 4) {
                                val barVal = if (isPlaying) (visualizerData.getOrElse(i) { 0.3f }).coerceIn(0.2f, 1f) else 0.15f
                                Box(
                                    modifier = Modifier
                                        .width(3.dp)
                                        .fillMaxHeight(barVal)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(
                                            if (isPlaying) Color(0xFFEC4899) else textColor.copy(alpha = 0.3f)
                                        )
                                )
                            }
                        }

                        // Play / Pause Quick Button
                        GlassIconButton(
                            onClick = { viewModel.toggleWhiteNoisePlayingAnytime() },
                            contentDescription = if (isPlaying) "Pause ambient sound" else "Play ambient sound",
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                contentDescription = null,
                                tint = textColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            } else {
                // Expanded Glass Control Card
                GlassSurface(
                    colors = CardDefaults.cardColors(containerColor = colors.glassFill),
                    shape = RoundedCornerShape(24.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, colors.glassBorder),
                    modifier = Modifier
                        .widthIn(max = 340.dp)
                        .softGlassShadow(
                            elevation = 20.dp,
                            shape = RoundedCornerShape(24.dp),
                            isDark = viewModel.isDarkMode
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .padding(18.dp)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        val textColor = LuminaTheme.colors.text
                        val textSecondaryColor = LuminaTheme.colors.textSecondary

                        // Header Row with Drag Indicator & Close
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DragHandle,
                                    contentDescription = "Drag soundscape",
                                    tint = textSecondaryColor.copy(alpha = 0.6f),
                                    modifier = Modifier.size(20.dp)
                                )
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.linearGradient(
                                                colors = listOf(Color(0xFF8B5CF6), Color(0xFFEC4899))
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.GraphicEq,
                                        contentDescription = "Soundscape",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "Ambient Soundscape",
                                        color = textColor,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (isPlaying) "Synthesizing $selectedSound" else "Audio engine ready",
                                        color = if (isPlaying) Color(0xFF8B5CF6) else textSecondaryColor,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                if (offsetX != 0f || offsetY != 0f) {
                                    GlassIconButton(
                                        onClick = {
                                            offsetX = 0f
                                            offsetY = 0f
                                        },
                                        contentDescription = "Reset controller position",
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.RestartAlt,
                                            contentDescription = null,
                                            tint = textColor.copy(alpha = 0.8f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                // Minimize Button
                                GlassIconButton(
                                    onClick = { isExpanded = false },
                                    contentDescription = "Collapse soundscape controller",
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = null,
                                        tint = textColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        // Full Audio Spectrum Visualizer
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(textColor.copy(alpha = 0.05f))
                                .border(1.dp, textColor.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            val activeVisualizerData = if (isPlaying) visualizerData else FloatArray(8) { 0.05f }
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val canvasWidth = size.width
                                val canvasHeight = size.height
                                val segmentWidth = canvasWidth / (activeVisualizerData.size - 1)
                                
                                val path = Path().apply {
                                    val startY = canvasHeight - (activeVisualizerData[0] * canvasHeight * 0.8f)
                                    moveTo(0f, startY)
                                    
                                    for (i in 0 until activeVisualizerData.size - 1) {
                                        val x1 = i * segmentWidth
                                        val y1 = canvasHeight - (activeVisualizerData[i] * canvasHeight * 0.8f)
                                        val x2 = (i + 1) * segmentWidth
                                        val y2 = canvasHeight - (activeVisualizerData[i + 1] * canvasHeight * 0.8f)
                                        
                                        val cx = (x1 + x2) / 2
                                        cubicTo(cx, y1, cx, y2, x2, y2)
                                    }
                                }
                                
                                drawPath(
                                    path = path,
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(Color(0xFF8B5CF6), Color(0xFFEC4899))
                                    ),
                                    style = Stroke(
                                        width = 3.dp.toPx(),
                                        cap = StrokeCap.Round,
                                        join = StrokeJoin.Round
                                    )
                                )
                            }
                        }

                        // Focus Profiles
                        Text(
                            text = "Focus Profiles",
                            color = colors.textSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val profiles = listOf(
                                "Deep Work" to "Brown Noise",
                                "Relaxation" to "Rain",
                                "Energy" to "Lo-Fi Beats"
                            )
                            profiles.forEach { (profileName, soundPreset) ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (selectedSound == soundPreset) Brush.horizontalGradient(listOf(Color(0xFF8B5CF6), Color(0xFFEC4899)))
                                            else Brush.horizontalGradient(listOf(textColor.copy(alpha = 0.05f), textColor.copy(alpha = 0.02f)))
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = if (selectedSound == soundPreset) Color.White.copy(alpha = 0.5f) else textColor.copy(alpha = 0.1f),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable { 
                                            viewModel.selectAmbientSound(soundPreset)
                                            if (!isPlaying) viewModel.toggleWhiteNoisePlayingAnytime()
                                        }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = profileName,
                                        color = if (selectedSound == soundPreset) Color.White else textColor,
                                        fontSize = 11.sp,
                                        fontWeight = if (selectedSound == soundPreset) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }
                        }

                        // Soundscapes Quick Selector Chips
                        val soundPresets = listOf(
                            "Rain" to "🌧️",
                            "Coffee Shop" to "☕",
                            "Forest" to "🌲",
                            "Ocean Waves" to "🌊",
                            "Binaural 40Hz" to "🎧",
                            "Brown Noise" to "🟤",
                            "White Noise" to "⚡"
                        )

                        Text(
                            text = "Choose Soundscape Preset",
                            color = colors.textSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(soundPresets) { (preset, emoji) ->
                                val isPresetSelected = selectedSound.equals(preset, ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(
                                            if (isPresetSelected) {
                                                Brush.horizontalGradient(
                                                    colors = listOf(Color(0xFF8B5CF6), Color(0xFFEC4899))
                                                )
                                            } else {
                                                Brush.horizontalGradient(
                                                    colors = listOf(textColor.copy(alpha = 0.05f), textColor.copy(alpha = 0.02f))
                                                )
                                            }
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = if (isPresetSelected) Color.White.copy(alpha = 0.5f) else textColor.copy(alpha = 0.1f),
                                            shape = RoundedCornerShape(16.dp)
                                        )
                                        .clickable { viewModel.selectAmbientSound(preset) }
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(text = emoji, fontSize = 12.sp)
                                        Text(
                                            text = preset,
                                            color = if (isPresetSelected) Color.White else textColor,
                                            fontSize = 12.sp,
                                            fontWeight = if (isPresetSelected) FontWeight.Bold else FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }

                        // Volume Control Slider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.VolumeUp,
                                contentDescription = "Volume",
                                tint = textColor.copy(alpha = 0.8f),
                                modifier = Modifier.size(18.dp)
                            )
                            GlassSlider(
                                value = volume,
                                onValueChange = { viewModel.setComfortChainVolumeValue(it) },
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "${(volume * 100).toInt()}%",
                                color = textColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Main Toggle Button
                        InteractiveHoverButton(
                            text = if (isPlaying) "Pause Soundscape" else "Play Soundscape",
                            onClick = { viewModel.toggleWhiteNoisePlayingAnytime() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                        )
                    }
                }
            }
        }
    }
}
