package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Shape
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke

import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.unit.IntOffset
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.material3.CircularProgressIndicator

@Composable
fun LiquidMetalButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    tooltipText: String? = null,
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isHovered by interactionSource.collectIsHoveredAsState()
    val scale = if (isPressed) 0.98f else 1f

    val baseGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF202020),
            Color(0xFF000000)
        )
    )

    val borderGradient = Brush.sweepGradient(
        colors = listOf(
            Color(0xFF999999), // Top right bright
            Color(0xFF333333),
            Color(0xFF111111), // Bottom right dark
            Color(0xFF333333),
            Color(0xFF999999), // Bottom left bright
            Color(0xFF333333),
            Color(0xFF111111), // Top left dark
            Color(0xFF999999)
        )
    )

    HoverTooltip(
        text = tooltipText ?: "",
        enabled = tooltipText != null,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .defaultMinSize(minWidth = 142.dp, minHeight = 46.dp)
                .scale(scale)
                .clip(RoundedCornerShape(100.dp))
                .background(Color.Transparent)
                .shadow(
                    elevation = if (isPressed) 1.dp else if (isHovered) 12.dp else 8.dp,
                    shape = RoundedCornerShape(100.dp),
                    ambientColor = Color.Black.copy(alpha = 0.5f),
                    spotColor = Color.Black.copy(alpha = 0.5f)
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    enabled = enabled && !isLoading,
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            // Simulated shader/rim layer
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(100.dp))
                .border(
                    width = 1.dp,
                    color = Color(0x40000000), // outer dark ring
                    shape = RoundedCornerShape(100.dp)
                )
                .padding(1.dp)
                .border(
                    width = 1.dp,
                    brush = borderGradient,
                    shape = RoundedCornerShape(100.dp)
                )
        )

        // Inner layer
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(2.dp)
                .clip(RoundedCornerShape(100.dp))
                .background(baseGradient)
                .drawWithContent {
                    drawContent()
                    if (isPressed) {
                        drawRoundRect(
                            color = Color.Black.copy(alpha = 0.6f),
                            size = size,
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(100.dp.toPx()),
                            style = Stroke(width = 4.dp.toPx())
                        )
                    }
                }
        )
        
        Row(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            CompositionLocalProvider(
                LocalContentColor provides Color(0xFF888888) // Matches React #666666 approx with slightly more brightness for readability
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color(0xFF888888),
                        strokeWidth = 2.dp
                    )
                } else {
                    content()
                }
            }
        }
    }
}
}

@Composable
fun LuminaDepthButton(
    onClick: () -> Unit,
    text: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
    isDark: Boolean = true
) {
    LiquidMetalButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Text(
            text = text,
            color = Color(0xFF666666),
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            style = TextStyle(
                shadow = Shadow(
                    color = Color.Black.copy(alpha = 0.5f),
                    offset = Offset(0f, 1f),
                    blurRadius = 2f
                )
            )
        )
    }
}

@Composable
fun DepthButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    tooltipText: String? = null,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    shape: Shape = RoundedCornerShape(100.dp),
    content: @Composable RowScope.() -> Unit
) {
    // Just delegate to LiquidMetalButton for this app to apply the requested visual style everywhere
    LiquidMetalButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        isLoading = isLoading,
        tooltipText = tooltipText,
        content = content
    )
}

@Composable
fun DepthIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    contentDescription: String? = null,
    tooltipText: String? = null,
    content: @Composable () -> Unit
) {
    val effectiveTooltip = tooltipText ?: contentDescription
    LiquidMetalButton(
        onClick = onClick,
        modifier = modifier.size(46.dp),
        enabled = enabled,
        isLoading = isLoading,
        tooltipText = effectiveTooltip
    ) {
        CompositionLocalProvider(
            LocalContentColor provides Color(0xFF666666)
        ) {
            content()
        }
    }
}

@Composable
fun HoverTooltip(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    if (!enabled) {
        content()
        return
    }

    var isHovered by remember { mutableStateOf(false) }
    var pointerOffset by remember { mutableStateOf(Offset.Zero) }
    
    val transitionState = remember { MutableTransitionState(false) }
    transitionState.targetState = isHovered

    Box(
        modifier = modifier.pointerInput(Unit) {
            awaitPointerEventScope {
                while (true) {
                    val event = awaitPointerEvent(PointerEventPass.Main)
                    when (event.type) {
                        PointerEventType.Enter, PointerEventType.Move -> {
                            isHovered = true
                            if (event.changes.isNotEmpty()) {
                                pointerOffset = event.changes.first().position
                            }
                        }
                        PointerEventType.Exit -> {
                            isHovered = false
                        }
                    }
                }
            }
        }
    ) {
        content()

        if (transitionState.currentState || transitionState.targetState) {
            Popup(
                offset = IntOffset(
                    x = (pointerOffset.x).toInt() - 100,
                    y = (pointerOffset.y).toInt() - 120
                ),
                properties = PopupProperties(focusable = false)
            ) {
                AnimatedVisibility(
                    visibleState = transitionState,
                    enter = fadeIn(tween(200)) + scaleIn(tween(200), initialScale = 0.8f),
                    exit = fadeOut(tween(150)) + scaleOut(tween(150), targetScale = 0.8f)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xEE111111))
                            .border(1.dp, Color(0xFF333333), RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = text,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
