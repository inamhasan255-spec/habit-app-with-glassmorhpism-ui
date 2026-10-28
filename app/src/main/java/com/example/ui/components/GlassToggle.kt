package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.LuminaTheme

@Composable
fun GlassToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentDescription: String? = null,
    width: Dp = 52.dp,
    height: Dp = 32.dp,
    thumbSize: Dp = 24.dp,
    checkedThumbColor: Color = Color(0xFF8B5CF6),
    uncheckedThumbColor: Color = Color.White,
    checkedTrackColor: Color = Color(0xFF8B5CF6).copy(alpha = 0.3f),
    uncheckedTrackColor: Color = LuminaTheme.colors.glassFill,
    borderWidth: Dp = 1.dp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val colors = LuminaTheme.colors

    // Smooth spring animations for position and scale
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) (width - thumbSize - 4.dp) else 4.dp,
        animationSpec = spring(
            dampingRatio = 0.6f, // Slightly bouncy for tactile feel
            stiffness = 400f
        ),
        label = "thumb_offset"
    )

    val thumbScale by animateFloatAsState(
        targetValue = if (checked) 1f else 0.85f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f),
        label = "thumb_scale"
    )

    val trackColor by animateColorAsState(
        targetValue = if (checked) checkedTrackColor else uncheckedTrackColor,
        animationSpec = tween(durationMillis = 200, easing = LinearOutSlowInEasing),
        label = "track_color"
    )

    val thumbColor by animateColorAsState(
        targetValue = if (checked) checkedThumbColor else uncheckedThumbColor,
        animationSpec = tween(durationMillis = 200, easing = LinearOutSlowInEasing),
        label = "thumb_color"
    )
    
    val borderColor by animateColorAsState(
        targetValue = if (checked) checkedThumbColor.copy(alpha = 0.5f) else colors.glassBorder,
        animationSpec = tween(durationMillis = 200),
        label = "border_color"
    )

    val semanticsModifier = if (contentDescription != null) {
        Modifier.semantics {
            this.contentDescription = contentDescription
        }
    } else Modifier

    Box(
        modifier = modifier
            .width(width)
            .height(height)
            .then(semanticsModifier)
            .clip(CircleShape)
            .background(trackColor)
            .border(borderWidth, borderColor, CircleShape)
            .toggleable(
                value = checked,
                interactionSource = interactionSource,
                indication = null, // Disable default ripple for custom tactile feel
                enabled = enabled,
                role = Role.Switch,
                onValueChange = onCheckedChange
            ),
        contentAlignment = Alignment.CenterStart
    ) {
        // Thumb
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(thumbSize)
                .scale(thumbScale)
                .shadow(
                    elevation = if (checked) 4.dp else 2.dp,
                    shape = CircleShape,
                    spotColor = if (checked) checkedThumbColor else Color.Black
                )
                .clip(CircleShape)
                .background(thumbColor)
        )
    }
}
