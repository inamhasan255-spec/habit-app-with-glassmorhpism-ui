package com.example.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class GlassButtonVariant {
    Primary, Secondary
}

@Composable
fun GlassButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    variant: GlassButtonVariant = GlassButtonVariant.Primary,
    icon: (@Composable () -> Unit)? = null,
    shape: Shape = RoundedCornerShape(999.dp),
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    elevation: Any? = null,
    border: BorderStroke? = null,
    contentPadding: PaddingValues = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable RowScope.() -> Unit
) {
    val isPressed by interactionSource.collectIsPressedAsState()
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isInteracting = (isPressed || isHovered) && enabled && !isLoading

    val buttonScale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "glass_button_scale"
    )

    // Interactive expansion dot effect
    val dotScale by animateFloatAsState(
        targetValue = if (isInteracting) 3.5f else 0.2f,
        animationSpec = tween(durationMillis = 350),
        label = "interactive_dot_scale"
    )
    val dotAlpha by animateFloatAsState(
        targetValue = if (isInteracting) 0.85f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "interactive_dot_alpha"
    )

    // Sliding text transition parameters matching InteractiveHoverButton specification
    val defaultTextOffset by animateDpAsState(
        targetValue = if (isInteracting) 24.dp else 0.dp,
        animationSpec = tween(durationMillis = 300),
        label = "default_text_offset"
    )
    val defaultTextAlpha by animateFloatAsState(
        targetValue = if (isInteracting) 0f else 1f,
        animationSpec = tween(durationMillis = 250),
        label = "default_text_alpha"
    )

    val hoverTextOffset by animateDpAsState(
        targetValue = if (isInteracting) 0.dp else (-24).dp,
        animationSpec = tween(durationMillis = 300),
        label = "hover_text_offset"
    )
    val hoverTextAlpha by animateFloatAsState(
        targetValue = if (isInteracting) 1f else 0f,
        animationSpec = tween(durationMillis = 250),
        label = "hover_text_alpha"
    )

    val opacity = if (enabled && !isLoading) 1f else 0.6f
    val isSecondary = variant == GlassButtonVariant.Secondary

    Box(
        modifier = modifier
            .scale(buttonScale)
            .alpha(opacity)
            .clip(shape)
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = if (isInteracting) 0.7f else 0.4f),
                        Color.White.copy(alpha = 0.1f),
                        Color.Black.copy(alpha = 0.3f)
                    )
                ),
                shape = shape
            ),
        contentAlignment = Alignment.Center
    ) {
        // Base container color / glass background
        if (!isSecondary) {
            androidx.compose.material3.Button(
                onClick = onClick,
                modifier = Modifier.matchParentSize(),
                enabled = enabled,
                shape = shape,
                colors = colors,
                elevation = null,
                border = null,
                contentPadding = PaddingValues(0.dp),
                interactionSource = interactionSource,
                content = {}
            )
        } else {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.White.copy(alpha = 0.12f))
            )
        }

        // Animated expanding background bubble (InteractiveHoverButton dot expansion)
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(shape)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .align(Alignment.CenterStart)
                    .offset(x = 12.dp)
                    .scale(dotScale)
                    .alpha(dotAlpha)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF8B5CF6), // Primary Purple Accent expansion
                                Color(0xFFEC4899)  // Glowing Pink Edge
                            )
                        )
                    )
            )
        }

        // Frosted glass inner glare overlay
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(1.dp)
                .clip(shape)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.3f),
                            Color.Transparent
                        ),
                        startY = 0f,
                        endY = 50f
                    )
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = androidx.compose.foundation.LocalIndication.current,
                    enabled = enabled && !isLoading,
                    role = Role.Button,
                    onClick = onClick
                )
        )

        // Content Area
        Box(
            modifier = Modifier.padding(contentPadding),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
            } else {
                // Default unhovered content
                Row(
                    modifier = Modifier
                        .offset(x = defaultTextOffset)
                        .alpha(defaultTextAlpha),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (icon != null) {
                        icon()
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    content()
                }

                // Interactive hovered/pressed content (reveals text + arrow right)
                Row(
                    modifier = Modifier
                        .offset(x = hoverTextOffset)
                        .alpha(hoverTextAlpha),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    content()
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Interactive Arrow",
                        modifier = Modifier.size(18.dp),
                        tint = Color.White
                    )
                }
            }
        }
    }
}

/**
 * Dedicated Compose implementation of InteractiveHoverButton matching the React/Tailwind design.
 */
@Composable
fun InteractiveHoverButton(
    text: String = "Button",
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    shape: Shape = RoundedCornerShape(999.dp)
) {
    GlassButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        isLoading = isLoading,
        shape = shape
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
    }
}


@Composable
fun GlassIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    variant: GlassButtonVariant = GlassButtonVariant.Primary,
    contentDescription: String? = null,
    colors: IconButtonColors = IconButtonDefaults.iconButtonColors(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable () -> Unit
) {
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.92f else 1f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow), label = "icon_button_scale")
    val opacity = if (enabled && !isLoading) 1f else 0.6f
    val shape = RoundedCornerShape(14.dp) // Squircle for icons

    val isSecondary = variant == GlassButtonVariant.Secondary

    val semanticsModifier = Modifier.semantics(mergeDescendants = true) {
        if (contentDescription != null) {
            this.contentDescription = contentDescription
        }
        this.role = Role.Button
    }

    Box(
        modifier = modifier
            .scale(scale)
            .alpha(opacity)
            .size(44.dp)
            .then(semanticsModifier)
            .clip(shape)
            .background(if (isSecondary) Color.White.copy(alpha = 0.1f) else colors.containerColor)
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.4f),
                        Color.Transparent,
                        Color.Black.copy(alpha = 0.25f)
                    )
                ),
                shape = shape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = androidx.compose.foundation.LocalIndication.current,
                enabled = enabled && !isLoading,
                role = Role.Button,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        // Inner highlight
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(1.dp)
                .clip(shape)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.35f),
                            Color.Transparent
                        ),
                        startY = 0f,
                        endY = 40f
                    )
                )
        )
        
        Box(contentAlignment = Alignment.Center) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                content()
            }
        }
    }
}
