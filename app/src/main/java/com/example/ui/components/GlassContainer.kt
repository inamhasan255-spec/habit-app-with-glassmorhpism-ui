package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

@Composable
fun GlassContainer(
    modifier: Modifier = Modifier,
    backgroundColor: Color? = null,
    borderColor: Color? = null,
    shape: Shape = RoundedCornerShape(22.dp),
    contentAlignment: Alignment = Alignment.TopStart,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val defaultBg = if (isDark) Color(0xFF141419).copy(alpha = 0.5f) else Color.White.copy(alpha = 0.5f)
    val defaultBorder = if (isDark) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.6f)

    Box(
        modifier = modifier
            .clip(shape)
            .background(backgroundColor ?: defaultBg)
            .border(1.dp, borderColor ?: defaultBorder, shape),
        contentAlignment = contentAlignment
    ) {
        content()
    }
}
