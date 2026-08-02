package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
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
import com.example.ui.components.*
import com.example.ui.theme.LuminaTheme
import com.example.ui.theme.glassmorphic
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(viewModel: FocusViewModel) {
    BackHandler(enabled = true) {
        // Explicitly lock hardware/system back gesture during onboarding tutorial to prevent bypass
    }
    val pagerState = rememberPagerState(pageCount = { 5 })
    val coroutineScope = rememberCoroutineScope()
    val accentColor = Color(android.graphics.Color.parseColor(viewModel.accentColorHex))
    var selectedRole by remember { mutableStateOf("STUDENT") } // Default Student

    val colors = LuminaTheme.colors

    Box(modifier = Modifier.fillMaxSize()) {
        LuminaStageBackground(isDarkMode = colors.isDark)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .systemBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header with optional Skip button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(accentColor)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "FocusCube",
                        color = colors.text,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    )
                }

                if (pagerState.currentPage < 4) {
                    GlassButton(
                        variant = GlassButtonVariant.Secondary,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(4)
                            }
                        }
                    ) {
                        Text(
                            text = "SKIP",
                            color = colors.textSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            // Pager content
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { page ->
                when (page) {
                    0 -> OnboardingIntroPage(accentColor)
                    1 -> OnboardingSessionsPage(accentColor)
                    2 -> OnboardingShieldPage(accentColor)
                    3 -> OnboardingAssistantsPage(accentColor)
                    4 -> OnboardingRoleSelectionPage(accentColor, selectedRole) { selectedRole = it }
                }
            }

            // Bottom controls
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Indicators
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 24.dp)
                ) {
                    repeat(5) { index ->
                        val isSelected = pagerState.currentPage == index
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(width = if (isSelected) 24.dp else 8.dp, height = 8.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) accentColor else colors.textSecondary.copy(alpha = 0.3f))
                        )
                    }
                }

                // Call To Action
                GlassButton(
                    onClick = {
                        if (pagerState.currentPage < 4) {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        } else {
                            viewModel.completeOnboarding(selectedRole)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    variant = GlassButtonVariant.Primary
                ) {
                    val label = if (pagerState.currentPage == 4) "GET STARTED" else "CONTINUE"
                    Text(
                        text = label,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = label,
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun OnboardingIntroPage(accentColor: Color) {
    val colors = LuminaTheme.colors
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        GlassSurface(
            modifier = Modifier.size(160.dp),
            shape = RoundedCornerShape(32.dp),
            fillColor = colors.glassFill,
            borderHighlight = colors.glassBorder
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Token,
                    contentDescription = "Cube Voxel",
                    tint = accentColor,
                    modifier = Modifier.size(72.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(36.dp))

        Text(
            text = "Welcome to FocusCube",
            color = colors.text,
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "An offline-first immersive space tailored to help you lock in focus, level up through gamification, and master habits with a floating voxel cube buddy.",
            color = colors.textSecondary,
            fontSize = 15.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 12.dp),
            lineHeight = 22.sp
        )
    }
}

@Composable
fun OnboardingSessionsPage(accentColor: Color) {
    val colors = LuminaTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        GlassSurface(
            modifier = Modifier.size(160.dp),
            shape = RoundedCornerShape(32.dp),
            fillColor = colors.glassFill,
            borderHighlight = colors.glassBorder
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.HourglassTop,
                    contentDescription = "Focus Sessions",
                    tint = accentColor,
                    modifier = Modifier.size(72.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(36.dp))

        Text(
            text = "Adaptive Focus Sessions",
            color = colors.text,
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Choose Pomodoro (25m), Custom Blocks, or Deep Work. Session metrics are tracked securely even if the app closes, giving you full insight.",
            color = colors.textSecondary,
            fontSize = 15.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 12.dp),
            lineHeight = 22.sp
        )
    }
}

@Composable
fun OnboardingShieldPage(accentColor: Color) {
    val colors = LuminaTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        GlassSurface(
            modifier = Modifier.size(160.dp),
            shape = RoundedCornerShape(32.dp),
            fillColor = colors.glassFill,
            borderHighlight = colors.glassBorder
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = "Focus Shield",
                    tint = accentColor,
                    modifier = Modifier.size(72.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(36.dp))

        Text(
            text = "The Focus Shield",
            color = colors.text,
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Auto-engage app blocking when a focus session starts. When distracting apps are launched, the shield redirects you to the cube, counting interruptions.",
            color = colors.textSecondary,
            fontSize = 15.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 12.dp),
            lineHeight = 22.sp
        )
    }
}

@Composable
fun OnboardingAssistantsPage(accentColor: Color) {
    val colors = LuminaTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        GlassSurface(
            modifier = Modifier.size(160.dp),
            shape = RoundedCornerShape(32.dp),
            fillColor = colors.glassFill,
            borderHighlight = colors.glassBorder
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = CubeIcon,
                    contentDescription = "Gemini",
                    tint = accentColor,
                    modifier = Modifier.size(72.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(36.dp))

        Text(
            text = "Gemini AI Inside",
            color = colors.text,
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Powered by Gemini to provide personalized coaching, habit insights, and academic assistance whenever you need it.",
            color = colors.textSecondary,
            fontSize = 15.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 12.dp),
            lineHeight = 22.sp
        )
    }
}

@Composable
fun OnboardingRoleSelectionPage(
    accentColor: Color,
    selectedRole: String,
    onRoleSelected: (String) -> Unit
) {
    val colors = LuminaTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Choose Your Role",
            color = colors.text,
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "We will optimize your dashboard widgets automatically based on your daily flow. You can always customize them later.",
            color = colors.textSecondary,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 12.dp),
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        // STUDENT Card
        GlassSurface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onRoleSelected("STUDENT") },
            shape = RoundedCornerShape(20.dp),
            fillColor = if (selectedRole == "STUDENT") accentColor.copy(alpha = 0.15f) else colors.glassFill,
            borderHighlight = if (selectedRole == "STUDENT") accentColor else colors.glassBorder,
            contentPadding = PaddingValues(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = "Student",
                    tint = if (selectedRole == "STUDENT") accentColor else colors.textSecondary,
                    modifier = Modifier.size(44.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Student Companion",
                        color = colors.text,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Prioritizes timetables, lecture notes, academic subjects, and homework tasks.",
                        color = colors.textSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ADULT Card
        GlassSurface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onRoleSelected("ADULT") },
            shape = RoundedCornerShape(20.dp),
            fillColor = if (selectedRole == "ADULT") accentColor.copy(alpha = 0.15f) else colors.glassFill,
            borderHighlight = if (selectedRole == "ADULT") accentColor else colors.glassBorder,
            contentPadding = PaddingValues(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Work,
                    contentDescription = "Adult/Professional",
                    tint = if (selectedRole == "ADULT") accentColor else colors.textSecondary,
                    modifier = Modifier.size(44.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Professional Toolkit",
                        color = colors.text,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Prioritizes Eisenhower matrix, Obsidian habit log sync, and focus charts.",
                        color = colors.textSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

