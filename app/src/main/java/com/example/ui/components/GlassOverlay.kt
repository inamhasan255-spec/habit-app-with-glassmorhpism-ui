package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FocusViewModel

/**
 * A premium backdrop-blurred, semi-transparent white loading overlay that unifies the Lumina design.
 * Features a high-transparency white glassmorphism effect (backdrop-blur: 20dp) with a subtle pulsing glowing orb.
 */
@Composable
fun GlassOverlay(
    isLoading: Boolean,
    viewModel: FocusViewModel,
    modifier: Modifier = Modifier,
    loadingText: String = "Fetching data..."
) {
    AnimatedVisibility(
        visible = isLoading,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .blur(radius = 20.dp) // High-transparency backdrop-blur: 20px
                .background(Color.White.copy(alpha = 0.15f)) // Semi-transparent white
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Premium pulsing glowing orb visual
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .background(Color.White.copy(alpha = 0.45f))
                        .blur(8.dp)
                ) {
                    // Core pulsing orb with brand colors
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(36.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                if (viewModel.isDarkMode) Color(0xFF38BDF8).copy(alpha = 0.85f) else Color(0xFF4F46E5).copy(alpha = 0.85f)
                            )
                    )
                }
                
                Spacer(modifier = Modifier.height(18.dp))
                
                Text(
                    text = loadingText,
                    color = viewModel.themeText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}
