package com.example.ui.components

import androidx.compose.foundation.border
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.glassmorphismBackgroundEffect(
    isDarkMode: Boolean,
    blurRadius: Dp = 80.dp
): Modifier = this
    .blur(blurRadius)
    .drawWithContent {
        drawContent()
        drawRect(
            color = if (isDarkMode) Color(0xFF141419).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.4f)
        )
    }
    .border(
        width = 1.dp,
        color = if (isDarkMode) Color.White.copy(alpha = 0.08f) else Color.Transparent,
        shape = RectangleShape
    )
