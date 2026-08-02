package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.LinearGradientShader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RainbowButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    val rainbowColors = listOf(
        Color(0xFFFF5252), // hsl(0, 100%, 63%)
        Color(0xFFB352FF), // hsl(270, 100%, 63%)
        Color(0xFF52A8FF), // hsl(210, 100%, 63%)
        Color(0xFF52E0FF), // hsl(195, 100%, 63%)
        Color(0xFF8BFF52), // hsl(90, 100%, 63%)
    )

    val infiniteTransition = rememberInfiniteTransition(label = "rainbow")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rainbow_progress"
    )

    val isDark = isSystemInDarkTheme()
    val borderWidth = 1.3.dp
    val cornerRadius = 12.dp

    // Animated rainbow brush using a repeating linear gradient shader
    val animatedBrush = remember(progress) {
        ShaderBrush(
            LinearGradientShader(
                from = Offset(-progress * 400f, 0f),
                to = Offset((1f - progress) * 400f, 0f),
                colors = rainbowColors + rainbowColors.first(),
                tileMode = TileMode.Repeated
            )
        )
    }
    
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Box(
        modifier = modifier
            .alpha(if (enabled) 1f else 0.5f)
            .focusable(interactionSource = interactionSource, enabled = enabled)
            .border(
                width = if (isFocused) 2.dp else 0.dp,
                color = if (isFocused) Color.White.copy(alpha = 0.5f) else Color.Transparent,
                shape = RoundedCornerShape(cornerRadius + 2.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        // Glow effect behind the button (simulates CSS ::before)
        if (enabled) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = 6.dp)
                    .width(120.dp)
                    .height(8.dp)
                    .graphicsLayer { alpha = 0.4f }
                    .background(
                        brush = animatedBrush,
                        shape = RoundedCornerShape(percent = 50)
                    )
            )
        }

        // Main button: gradient border + solid inner background
        Box(
            modifier = Modifier
                .height(44.dp)
                .clip(RoundedCornerShape(cornerRadius))
                .background(animatedBrush)
                .padding(borderWidth)
                .clip(RoundedCornerShape(cornerRadius - borderWidth))
                .background(if (isDark) Color.White else Color(0xFF121213))
                .clickable(
                    interactionSource = interactionSource,
                    indication = androidx.compose.foundation.LocalIndication.current,
                    enabled = enabled,
                    role = Role.Button,
                    onClick = onClick
                )
                .padding(horizontal = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            ProvideTextStyle(
                value = TextStyle(
                    color = if (isDark) Color(0xFF121213) else Color.White,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                )
            ) {
                content()
            }
        }
    }
}

// ---- Preview ----

@Preview(showBackground = true)
@Composable
fun RainbowButtonPreview() {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Surface(color = Color.White) {
            RainbowButton(onClick = {}) {
                Text("Light Mode")
            }
        }
        Surface(color = Color(0xFF121213)) {
            RainbowButton(onClick = {}) {
                Text("Dark Mode")
            }
        }
    }
}
