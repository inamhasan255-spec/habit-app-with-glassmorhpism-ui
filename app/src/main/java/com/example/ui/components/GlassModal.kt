package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.softGlassShadow

/**
 * A beautiful, reusable GlassModal component with a full-page, backdrop-blurred white overlay
 * that provides a consistent, premium dialog experience for user settings or confirmation prompts.
 */
@Composable
fun GlassModal(
    onDismissRequest: () -> Unit,
    title: String? = null,
    isDarkMode: Boolean = false,
    themeTextColor: Color = if (isDarkMode) Color.White else Color.Black,
    properties: DialogProperties = DialogProperties(usePlatformDefaultWidth = false),
    content: @Composable ColumnScope.() -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = properties
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismissRequest
                ),
            contentAlignment = Alignment.Center
        ) {
            // Full-page, backdrop-blurred white overlay (premium frosted glass effect)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .blur(radius = 24.dp)
                    .background(Color.White.copy(alpha = 0.12f))
            )

            // Inner Dialog Content (Glass container)
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.90f)
                    .wrapContentHeight()
                    .softGlassShadow(
                        elevation = 20.dp,
                        shape = RoundedCornerShape(24.dp),
                        isDark = isDarkMode
                    )
                    .clip(RoundedCornerShape(24.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {} // Prevent clicks on container from dismissing
                    )
                    .background(
                        if (isDarkMode) Color(0xCC0D0B1C) else Color(0xF2FFFFFF)
                    )
                    .border(
                        1.dp,
                        if (isDarkMode) Color.White.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.60f),
                        RoundedCornerShape(24.dp)
                    )
                    .padding(24.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Header row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        if (title != null) {
                            Text(
                                text = title,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = themeTextColor,
                                letterSpacing = (-0.5).sp
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }

                        IconButton(
                            onClick = onDismissRequest,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = themeTextColor.copy(alpha = 0.6f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    content()
                }
            }
        }
    }
}
