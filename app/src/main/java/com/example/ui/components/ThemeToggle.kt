package com.example.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.data.FocusViewModel

/**
 * A highly-polished Material 3 Theme Toggle widget with responsive spring transitions
 * and dual sun/moon icons, managing the global application theme state.
 */
@Composable
fun ThemeToggle(
    viewModel: FocusViewModel,
    modifier: Modifier = Modifier
) {
    val isDark = viewModel.isDarkMode
    
    // Spring physics animation for thumb offset and rotation
    val offset by animateDpAsState(
        targetValue = if (isDark) 24.dp else 0.dp,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f),
        label = "toggle_offset"
    )
    
    val rotation by animateFloatAsState(
        targetValue = if (isDark) 180f else 0f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 250f),
        label = "toggle_rotation"
    )

    Box(
        modifier = modifier
            .semantics {
                contentDescription = "Toggle dark mode"
            }
            .width(56.dp)
            .height(32.dp)
            .clip(CircleShape)
            .background(viewModel.themeGlassFill)
            .toggleable(
                value = isDark,
                role = Role.Switch,
                onValueChange = { viewModel.toggleDarkMode() }
            )
            .padding(4.dp)
            .testTag("theme_toggle_widget"),
        contentAlignment = Alignment.CenterStart
    ) {
        // Animated background thumb
        Box(
            modifier = Modifier
                .offset(x = offset)
                .rotate(rotation)
                .size(24.dp)
                .clip(CircleShape)
                .background(
                    if (isDark) Color(0xFF38BDF8) else Color(0xFFFBBF24)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isDark) Icons.Default.DarkMode else Icons.Default.LightMode,
                contentDescription = null,
                tint = if (isDark) Color.Black else Color.White,
                modifier = Modifier
                    .size(15.dp)
                    .scale(if (isDark) 1f else 1.05f)
            )
        }
    }
}
