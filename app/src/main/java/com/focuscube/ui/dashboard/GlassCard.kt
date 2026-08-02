package com.focuscube.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    borderWidth: Dp = 1.dp,
    containerColor: Color = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
    content: @Composable BoxScope.() -> Unit
) {
    Box(modifier = modifier) {
        // Blurred background effect using blur modifier for the glassmorphism aesthetic
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(shape)
                .background(containerColor)
                .blur(16.dp)
                .border(
                    width = borderWidth,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
                    shape = shape
                )
        )
        // Content sizes the outer Box
        Box(
            contentAlignment = Alignment.TopStart,
            content = content
        )
    }
}
