package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FocusViewModel
import com.example.ui.components.AntiGravityWrapper

@Composable
fun BlockedAppOverlayScreen(viewModel: FocusViewModel) {
    val accentColor = Color(android.graphics.Color.parseColor(viewModel.accentColorHex))

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (viewModel.isDarkMode) Color(0xFB0A0A12) else Color(0xFCFFFFFF)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AntiGravityWrapper {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f))
                        .border(2.dp, accentColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Shield Guard",
                        tint = accentColor,
                        modifier = Modifier.size(54.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "FOCUS SHIELD ACTIVATED",
                color = viewModel.themeText,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Access to \"${viewModel.activeBlockedAppName}\" has been suspended because a Deep Work focus session is actively running.",
                color = viewModel.themeTextSecondary,
                fontSize = 14.sp,
                lineHeight = 22.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            com.example.ui.components.GlassButton(
                onClick = {
                    viewModel.dismissFocusBlockOverlay()
                },
                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(
                    text = "Return to Focus",
                    color = if (viewModel.isDarkMode) Color.Black else Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            com.example.ui.components.GlassButton(variant = com.example.ui.components.GlassButtonVariant.Secondary, 
                onClick = {
                    // Emergency bypass stops the timer early and clears the overlay
                    viewModel.stopTimerEarly()
                    viewModel.dismissFocusBlockOverlay()
                }
            ) {
                Text(
                    text = "Emergency Bypass (End Focus Block)",
                    color = Color(0xFFEF4444),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
