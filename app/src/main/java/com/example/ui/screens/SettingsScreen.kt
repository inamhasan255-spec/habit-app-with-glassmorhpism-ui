
package com.example.ui.screens
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.components.*
import coil.compose.AsyncImage
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.geometry.Offset
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.ui.platform.testTag
import kotlin.math.sin
import com.example.ui.components.GlassSurface

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: FocusViewModel) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val accentColor = Color(android.graphics.Color.parseColor(viewModel.accentColorHex))
    
    val calendarPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                Toast.makeText(context, "Calendar Sync Enabled. Events will show on Dashboard.", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "Calendar Permission Denied.", Toast.LENGTH_SHORT).show()
            }
        }
    )

    // Profile input states
    var nameInput by remember { mutableStateOf(viewModel.profileName) }
    var titleInput by remember { mutableStateOf(viewModel.profileTitle) }
    var showResetConfirm by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp)
            .padding(bottom = 100.dp) // Leave cushion above bottom navbar
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Screen title
        Column {
            Text(
                text = "Settings",
                color = viewModel.themeText,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
            )
            Text(
                text = "Customize your cube productivity ecosystem",
                color = viewModel.themeTextSecondary,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // SECTION 0: Cloud Sync and Account
        Text(text = "CLOUD SECURITY & SYNC", color = accentColor, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(12.dp))

        Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(viewModel.themeGlassFill)
            .border(1.dp, viewModel.themeGlassBorder, RoundedCornerShape(22.dp))
            .padding(18.dp)
    ) {
            Column {
                val currentUser = viewModel.mAuth?.currentUser
                if (currentUser != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
        modifier = Modifier
            .size(48.dp)
                                .clip(CircleShape)
                                .background(accentColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudQueue,
                                contentDescription = "Authenticated User",
                                tint = accentColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "Synced Account",
                                color = viewModel.themeText,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = currentUser.email ?: "no-email@firebase.com",
                                color = viewModel.themeTextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Your FocusCube stats, custom habits, and work sessions are continuously synced to Firebase.",
                        color = viewModel.themeTextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        modifier = Modifier
            .clip(RoundedCornerShape(22.dp))
            .background(viewModel.themeGlassFill)
            .border(1.dp, viewModel.themeGlassBorder, RoundedCornerShape(22.dp))
            .padding(bottom = 16.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        com.example.ui.components.GlassButton(
                            onClick = {
                                viewModel.syncDataToFirebase()
                                Toast.makeText(context, "Syncing to Cloud...", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0x0DFFFFFF), contentColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x14FFFFFF)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f),
                            enabled = !viewModel.isSyncing
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Force Sync", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        com.example.ui.components.GlassButton(
                            onClick = {
                                viewModel.signOut()
                                Toast.makeText(context, "Signed out of sync ecosystem.", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentPink.copy(alpha = 0.15f), contentColor = AccentPink),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AccentPink.copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Sign Out", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(AccentPink.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudOff,
                                contentDescription = "Offline Mode",
                                tint = AccentPink,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "Local Sandbox Mode",
                                color = viewModel.themeText,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Data is saved locally only",
                                color = viewModel.themeTextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Sign up or log in to enable cloud backup, stats recovery, and cross-device sync features.",
                        color = viewModel.themeTextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    com.example.ui.components.GlassButton(
                        onClick = {
                            viewModel.navigateTo(AppScreen.AUTH)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor, contentColor = if (viewModel.isDarkMode) Color.Black else Color.White),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.CloudQueue, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Enable Cloud Sync", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = viewModel.themeGlassBorder)
                Spacer(modifier = Modifier.height(12.dp))

                // Firebase/google-services config notice
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Info",
                        tint = viewModel.themeTextSecondary,
                        modifier = Modifier.size(14.dp).padding(top = 1.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Ecosystem Sync Notice: For Google Sign-In and cloud backup to connect successfully, the administrator must place a valid 'google-services.json' in the /app directory.",
                        color = viewModel.themeTextSecondary,
                        fontSize = 10.sp,
                        lineHeight = 13.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // WIDGET PERSONALIZATION SECTION
        WidgetPersonalizationCard(viewModel = viewModel, accentColor = accentColor)

        Spacer(modifier = Modifier.height(24.dp))

        // EXTERNAL INTEGRATIONS SECTION
        Text(text = "EXTERNAL INTEGRATIONS", color = accentColor, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
            .padding(18.dp)
    ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth().clickable {
                        calendarPermissionLauncher.launch(Manifest.permission.READ_CALENDAR)
                    },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Connect External Calendars",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Google Calendar, Microsoft Outlook & Exchange",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 13.sp
                        )
                    }
                    Icon(
                        imageVector = Icons.Filled.DateRange,
                        contentDescription = "Sync Calendars",
                        tint = accentColor
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // BACKDROP & VISUAL APPEARANCE SECTION
        Text(text = "BACKDROP & VISUAL APPEARANCE", color = accentColor, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(12.dp))

        Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(viewModel.themeGlassFill)
            .border(1.dp, viewModel.themeGlassBorder, RoundedCornerShape(22.dp))
            .padding(18.dp)
    ) {
            Column {
                // Row 1: Sunset Backdrop Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Sunset Backdrop",
                            color = viewModel.themeText,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Render immersive ambient sunset illustration background.",
                            color = viewModel.themeTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                    GlassToggle(
                        checked = viewModel.isBackgroundEnabled,
                        onCheckedChange = { viewModel.toggleBackground() },
                        checkedTrackColor = accentColor
                    )
                }

                Divider(
                    modifier = Modifier.padding(vertical = 14.dp),
                    color = viewModel.themeDivider
                )

                // Row 1.5: Lumina Glass Aesthetic Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Lumina Glass Aesthetic",
                            color = viewModel.themeText,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Switch between Premium White Glass and Midnight Dark Glass themes.",
                            color = viewModel.themeTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                    GlassToggle(
                        checked = viewModel.isDarkMode,
                        onCheckedChange = { viewModel.toggleDarkMode() },
                        checkedTrackColor = accentColor
                    )
                }

                Divider(
                    modifier = Modifier.padding(vertical = 14.dp),
                    color = viewModel.themeDivider
                )

                // Row 2: Pure White Theme Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Pure Solid White UI",
                            color = viewModel.themeText,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Force high-contrast non-translucent solid white theme interface.",
                            color = viewModel.themeTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                    GlassToggle(
                        checked = viewModel.useWhiteUi,
                        onCheckedChange = { viewModel.toggleWhiteUi() },
                        checkedTrackColor = accentColor
                    )
                }

                Divider(
                    modifier = Modifier.padding(vertical = 14.dp),
                    color = viewModel.themeDivider
                )

                // Row 3: Minimize Transparency Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Solid Glass Panels",
                            color = viewModel.themeText,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Make background glass blocks solid and opaque for readability.",
                            color = viewModel.themeTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                    GlassToggle(
                        checked = viewModel.lessTransparency,
                        onCheckedChange = { viewModel.toggleLessTransparency() },
                        checkedTrackColor = accentColor
                    )
                }

                Divider(
                    modifier = Modifier.padding(vertical = 14.dp),
                    color = viewModel.themeDivider
                )

                // DYNAMIC GLASSMORPHISM CONFIGURATION SLIDERS
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = "Glass Engine",
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Dynamic Glass Engine (${if (viewModel.isDarkMode) "Dark Theme" else "Light Theme"})",
                        color = viewModel.themeText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Blur intensity control
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Blur Intensity",
                            color = viewModel.themeText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${viewModel.glassBlurIntensity.toInt()} dp",
                            color = accentColor,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Slider(
                        value = viewModel.glassBlurIntensity,
                        onValueChange = { newVal ->
                            viewModel.updateGlassmorphismProperties(newVal, viewModel.glassOpacity)
                        },
                        valueRange = 0f..30f,
                        colors = SliderDefaults.colors(
                            thumbColor = accentColor,
                            activeTrackColor = accentColor,
                            inactiveTrackColor = viewModel.themeDivider
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Opacity control
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Glass Opacity",
                            color = viewModel.themeText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${(viewModel.glassOpacity * 100).toInt()}%",
                            color = accentColor,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Slider(
                        value = viewModel.glassOpacity,
                        onValueChange = { newVal ->
                            viewModel.updateGlassmorphismProperties(viewModel.glassBlurIntensity, newVal)
                        },
                        valueRange = 0.1f..1.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = accentColor,
                            activeTrackColor = accentColor,
                            inactiveTrackColor = viewModel.themeDivider
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Mode-aware presets row
                Text(
                    text = "Presets",
                    color = viewModel.themeTextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val presets = listOf(
                        Triple("Frosted", 22f, 0.88f),
                        Triple("Glassy", 12f, 0.50f),
                        Triple("Mist", 6f, 0.25f)
                    )
                    presets.forEach { preset ->
                        Box(
        modifier = Modifier
            .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(viewModel.themeDivider)
                                .clickable {
                                    viewModel.updateGlassmorphismProperties(preset.second, preset.third)
                                }
                                
            .clip(RoundedCornerShape(22.dp))
            .background(viewModel.themeGlassFill)
            .border(1.dp, viewModel.themeGlassBorder, RoundedCornerShape(22.dp))
            .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = preset.first,
                                color = viewModel.themeText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // GALAXY BACKGROUND EFFECTS SECTION
        Text(text = "GALAXY BACKGROUND EFFECTS", color = accentColor, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
            .padding(18.dp)
    ) {
            val galaxyVisible by viewModel.galaxyVisible.collectAsState()
            val galaxySpeed by viewModel.galaxyStarSpeed.collectAsState()
            val galaxyHue by viewModel.galaxyHueShift.collectAsState()

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Enable Galaxy Background",
                            color = viewModel.themeText,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Show beautiful animated starry background",
                            color = viewModel.themeTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                    GlassToggle(
                        checked = galaxyVisible,
                        onCheckedChange = { viewModel.updateGalaxyPreferences(it, galaxySpeed, galaxyHue) },
                        checkedTrackColor = accentColor
                    )
                }

                if (galaxyVisible) {
                    Divider(
                        modifier = Modifier.padding(vertical = 14.dp),
                        color = viewModel.themeDivider
                    )
                    
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Star Speed",
                                color = viewModel.themeText,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = String.format("%.1fx", galaxySpeed),
                                color = accentColor,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Slider(
                            value = galaxySpeed,
                            onValueChange = { newVal ->
                                viewModel.updateGalaxyPreferences(galaxyVisible, newVal, galaxyHue)
                            },
                            valueRange = 0.1f..5.0f,
                            colors = SliderDefaults.colors(
                                thumbColor = accentColor,
                                activeTrackColor = accentColor,
                                inactiveTrackColor = viewModel.themeDivider
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Hue Shift",
                                color = viewModel.themeText,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Slider(
                            value = galaxyHue,
                            onValueChange = { newVal ->
                                viewModel.updateGalaxyPreferences(galaxyVisible, galaxySpeed, newVal)
                            },
                            valueRange = 0f..360f,
                            colors = SliderDefaults.colors(
                                thumbColor = accentColor,
                                activeTrackColor = accentColor,
                                inactiveTrackColor = viewModel.themeDivider
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        // NOTIFICATION SOUND SYSTEM SECTION
        Text(text = "NOTIFICATION SOUND SYSTEM", color = accentColor, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(12.dp))

        Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(viewModel.themeGlassFill)
            .border(1.dp, viewModel.themeGlassBorder, RoundedCornerShape(22.dp))
            .padding(18.dp)
    ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Sound Pack",
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Focus Session Sound Pack",
                            color = viewModel.themeText,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Synthesized real-time tones for timer alerts",
                            color = viewModel.themeTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val soundPacks = listOf(
                        "Minimalist" to "Pure crystalline electronic chimes",
                        "Nature" to "Warm woodblocks & pentatonic forest tones",
                        "Mechanical" to "Sharp typewriter clicks & high desk bells"
                    )
                    soundPacks.forEach { (pack, description) ->
                        val isSelected = viewModel.selectedSoundPack == pack
                        Box(
        modifier = Modifier
            .weight(1f)
                                .height(82.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) accentColor.copy(alpha = 0.15f) else viewModel.themeDivider)
                                .border(
                                    width = 1.5.dp,
                                    color = if (isSelected) accentColor else Color.Transparent,
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .clickable {
                                    viewModel.selectSoundPack(pack)
                                    Toast.makeText(context, "Sound Pack: $pack selected", Toast.LENGTH_SHORT).show()
                                }
                                
            .clip(RoundedCornerShape(22.dp))
            .background(viewModel.themeGlassFill)
            .border(1.dp, viewModel.themeGlassBorder, RoundedCornerShape(22.dp))
            .padding(10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = when(pack) {
                                        "Nature" -> Icons.Default.Star
                                        "Mechanical" -> Icons.Default.Settings
                                        else -> Icons.Default.Notifications
                                    },
                                    contentDescription = pack,
                                    tint = if (isSelected) accentColor else viewModel.themeTextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = pack,
                                    color = if (isSelected) accentColor else viewModel.themeText,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = when(pack) {
                                        "Nature" -> "Forest Tap"
                                        "Mechanical" -> "Retro Ding"
                                        else -> "Clear Bell"
                                    },
                                    color = viewModel.themeTextSecondary.copy(alpha = 0.8f),
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = viewModel.themeGlassBorder)
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Play notification sound previews:",
                        color = viewModel.themeTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(
                            "Start" to { FocusSoundPlayer.playStartSound(viewModel.selectedSoundPack) },
                            "Pause" to { FocusSoundPlayer.playPauseSound(viewModel.selectedSoundPack) },
                            "Done" to { FocusSoundPlayer.playCompleteSound(viewModel.selectedSoundPack) }
                        ).forEach { (label, action) ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(viewModel.themeDivider)
                                    .clickable { action() }
                                    .padding(horizontal = 8.dp, vertical = 5.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = viewModel.themeText,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = viewModel.themeGlassBorder)
                Spacer(modifier = Modifier.height(14.dp))

                // Mode Ambient Soundscapes Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Mode Ambient Soundscapes",
                            color = viewModel.themeText,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Play looping ambient audio corresponding to your active app mode (e.g. Forest for Reading, Rain for Journal).",
                            color = viewModel.themeTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                    GlassToggle(
                        checked = viewModel.isBackgroundAmbientEnabled,
                        onCheckedChange = { viewModel.toggleBackgroundAmbient() },
                        checkedTrackColor = accentColor
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // SECTION 1: Accent Color Cube Picker
        Text(text = "ACCENT CUBE COLOR", color = accentColor, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(12.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
            .padding(18.dp)
    ) {
            Column {
                Text(
                    text = "Engine Accent",
                    color = viewModel.themeText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Select an interactive color-face to colorize system components",
                    color = viewModel.themeTextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val colorOptions = listOf(
                        Pair("#5adcb3", "Teal"),
                        Pair("#fcb2c5", "Pink"),
                        Pair("#f7e299", "Yellow"),
                        Pair("#C9B8FF", "Lavender"),
                        Pair("#FFD0B5", "Peach")
                    )

                    colorOptions.forEach { (hex, name) ->
                        val itemColor = Color(android.graphics.Color.parseColor(hex))
                        val isSelected = viewModel.accentColorHex == hex

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
        modifier = Modifier
            .size(42.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(itemColor)
                                    .border(
                                        width = if (isSelected) 3.dp else 0.dp,
                                        color = if (isSelected) Color.White else Color.Transparent,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable {
                                        viewModel.accentColorHex = hex
                                    }
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = name,
                                color = if (isSelected) viewModel.themeText else viewModel.themeTextSecondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // SECTION 2: Profile Customization
        Text(text = "PROFILE IDENTIFIER", color = accentColor, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(viewModel.themeGlassFill)
            .border(1.dp, viewModel.themeGlassBorder, RoundedCornerShape(22.dp))
            .padding(18.dp)
    ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("Crafter Name", color = viewModel.themeTextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentColor,
                        unfocusedBorderColor = viewModel.themeGlassBorder,
                        focusedTextColor = viewModel.themeText,
                        unfocusedTextColor = viewModel.themeText
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = titleInput,
                    onValueChange = { titleInput = it },
                    label = { Text("Profile Subtitle", color = viewModel.themeTextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentColor,
                        unfocusedBorderColor = viewModel.themeGlassBorder,
                        focusedTextColor = viewModel.themeText,
                        unfocusedTextColor = viewModel.themeText
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                com.example.ui.components.GlassButton(
                    onClick = {
                        viewModel.profileName = nameInput
                        viewModel.profileTitle = titleInput
                        Toast.makeText(context, "Profile updated!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor, contentColor = if (viewModel.isDarkMode) Color.Black else Color.White),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Icon(imageVector = Icons.Default.Save, contentDescription = "Save", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Save Identity", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // SECTION 3: Essential Apps & Websites (Bypass list)
        Text(text = "ESSENTIAL BYPASS (WHITELIST)", color = accentColor, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(12.dp))

        Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(viewModel.themeGlassFill)
            .border(1.dp, viewModel.themeGlassBorder, RoundedCornerShape(22.dp))
            .padding(18.dp)
    ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "Whitelisted apps and websites bypass active focus shielding, ensuring your core productivity tools are never blocked.",
                    color = viewModel.themeTextSecondary,
                    fontSize = 12.sp
                )

                var itemType by remember { mutableStateOf("App") }
                var itemName by remember { mutableStateOf("") }
                var itemIdentifier by remember { mutableStateOf("") }

                // Tab-like Selection for App/Website
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(viewModel.themeGlassBorder.copy(alpha = 0.2f))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("App", "Website").forEach { type ->
                        val isSelected = itemType == type
                        Box(
        modifier = Modifier
            .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) accentColor else Color.Transparent)
                                .clickable { itemType = type }
                                
            .clip(RoundedCornerShape(22.dp))
            .background(viewModel.themeGlassFill)
            .border(1.dp, viewModel.themeGlassBorder, RoundedCornerShape(22.dp))
            .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = type,
                                color = if (isSelected) (if (viewModel.isDarkMode) Color.Black else Color.White) else viewModel.themeTextSecondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                // Input fields
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = itemName,
                        onValueChange = { itemName = it },
                        label = { Text("Display Name (e.g. Gmail, Wikipedia)", color = viewModel.themeTextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accentColor,
                            unfocusedBorderColor = viewModel.themeGlassBorder,
                            focusedTextColor = viewModel.themeText,
                            unfocusedTextColor = viewModel.themeText
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = itemIdentifier,
                        onValueChange = { itemIdentifier = it },
                        label = { 
                            Text(
                                text = if (itemType == "App") "Package Name (e.g. com.google.android.gm)" else "Website Domain (e.g. wikipedia.org)", 
                                color = viewModel.themeTextSecondary
                            ) 
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accentColor,
                            unfocusedBorderColor = viewModel.themeGlassBorder,
                            focusedTextColor = viewModel.themeText,
                            unfocusedTextColor = viewModel.themeText
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Quick Preset Button to easily add common tools
                    Text(
                        text = "Quick Presets",
                        color = accentColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .clickable {
                                // Fill out fields with a template of common useful items
                                if (itemType == "App") {
                                    itemName = "Gmail"
                                    itemIdentifier = "com.google.android.gm"
                                } else {
                                    itemName = "Wikipedia"
                                    itemIdentifier = "wikipedia.org"
                                }
                            }
                            .padding(vertical = 4.dp)
                    )

                    com.example.ui.components.GlassButton(
                        onClick = {
                            if (itemName.isNotBlank() && itemIdentifier.isNotBlank()) {
                                viewModel.addEssentialItem(itemName.trim(), itemType, itemIdentifier.trim().lowercase())
                                itemName = ""
                                itemIdentifier = ""
                                Toast.makeText(context, "Added to Whitelist!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor, contentColor = if (viewModel.isDarkMode) Color.Black else Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Add Whitelist", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                Divider(color = viewModel.themeGlassBorder, thickness = 1.dp)

                val essentialList by viewModel.essentialItems.collectAsState(initial = emptyList())

                if (essentialList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No exceptions defined yet.",
                            color = viewModel.themeTextSecondary.copy(alpha = 0.6f),
                            fontSize = 12.sp
                        )
                    }
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp) // constraint list height so it doesn't expand settings page infinitely
                    ) {
                        essentialList.forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(viewModel.themeGlassBorder.copy(alpha = 0.1f))
                                    .border(1.dp, viewModel.themeGlassBorder.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Colored Badge indicating App vs Website
                                    val badgeBg = if (item.type == "App") AccentTeal.copy(alpha = 0.15f) else AccentPink.copy(alpha = 0.15f)
                                    val badgeText = if (item.type == "App") AccentTeal else AccentPink
                                    
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(badgeBg)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = item.type.uppercase(),
                                            color = badgeText,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Column {
                                        Text(
                                            text = item.name,
                                            color = viewModel.themeText,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = item.identifier,
                                            color = viewModel.themeTextSecondary,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                com.example.ui.components.GlassIconButton(
                                    onClick = { viewModel.deleteEssentialItem(item) },
                                    contentDescription = "Delete essential item",
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = AccentPink,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // SECTION 3.5: Replay Onboarding Tutorial
        Text(text = "ONBOARDING TUTORIAL", color = accentColor, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
            .padding(18.dp)
    ) {
            Column {
                Text(
                    text = "Replay Tutorial",
                    color = viewModel.themeText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Review the focus mechanics, Focus Shield, and assistants of FocusCube.",
                    color = viewModel.themeTextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                com.example.ui.components.RainbowButton(
                    onClick = {
                        viewModel.replayOnboarding()
                    },
                    modifier = Modifier.fillMaxWidth().testTag("replay_tutorial_button")
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Replay")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "REPLAY TUTORIAL", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // SECTION 4: Data Reset
        Text(text = "ECOSYSTEM RESET", color = AccentPink, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(12.dp))

        Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(viewModel.themeGlassFill)
            .border(1.dp, viewModel.themeGlassBorder, RoundedCornerShape(22.dp))
            .padding(18.dp)
    ) {
            Column {
                Text(
                    text = "Clear and Reseed FocusCube",
                    color = viewModel.themeText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Erase all habits, logs, session times, and re-populate clean initial nodes",
                    color = viewModel.themeTextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                com.example.ui.components.RainbowButton(
                    onClick = {
                        showResetConfirm = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Reset")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "FULL ECOSYSTEM RESET", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }

    if (showResetConfirm) {
        com.example.ui.components.GlassModal(
            onDismissRequest = { showResetConfirm = false },
            title = "Full Ecosystem Reset?",
            isDarkMode = viewModel.isDarkMode,
            themeTextColor = viewModel.themeText
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Are you absolutely sure you want to perform a full reset? This will erase all your custom habits, whitelist exceptions, and your entire focus session history permanently. This action cannot be undone.",
                    color = viewModel.themeTextSecondary,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    com.example.ui.components.GlassButton(
                        variant = com.example.ui.components.GlassButtonVariant.Secondary,
                        onClick = { showResetConfirm = false },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel", color = viewModel.themeTextSecondary, fontWeight = FontWeight.Bold)
                    }

                    com.example.ui.components.GlassButton(
                        onClick = {
                            viewModel.resetData()
                            showResetConfirm = false
                            Toast.makeText(context, "Ecosystem database reseeded successfully!", Toast.LENGTH_LONG).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentPink, contentColor = if (viewModel.isDarkMode) Color.Black else Color.White),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1.2f)
                    ) {
                        Text("Erase Everything", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun WidgetPersonalizationCard(viewModel: FocusViewModel, accentColor: Color) {
    val context = LocalContext.current
    
    // Ambient animations for the "animated_lottie" preview
    val infiniteTransition = rememberInfiniteTransition(label = "widget_bg_anim")
    val animOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing), RepeatMode.Restart),
        label = "animOffset"
    )
    
    // Bubble positions for Focus Bubble theme
    val bubbles = remember {
        List(10) {
            Triple(
                (0..100).random() / 100f, // random x fraction
                (0..100).random() / 100f, // random y fraction
                (5..15).random().dp       // random size
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(viewModel.themeGlassFill)
            .border(1.dp, viewModel.themeGlassBorder, RoundedCornerShape(22.dp))
            .padding(18.dp)
            .testTag("widget_personalization_card")
    ) {
        Text(
            text = "WIDGET PERSONALIZATION & PREVIEW",
            color = accentColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Personalize your on-screen widget backdrop frame style",
            color = viewModel.themeTextSecondary,
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 14.dp)
        )

        // LIVE PREVIEW OF THE WIDGET
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(84.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, viewModel.themeGlassBorder, RoundedCornerShape(16.dp))
                .testTag("widget_live_preview")
        ) {
            // RENDERING CORRESPONDING BACKGROUND STYLE
            when (viewModel.widgetBackgroundType) {
                "solid_color" -> {
                    val color = try {
                        Color(android.graphics.Color.parseColor(viewModel.widgetSolidColorHex))
                    } catch (e: Exception) {
                        Color(0xFF111827)
                    }
                    Box(modifier = Modifier.fillMaxSize().background(color))
                }
                "photo" -> {
                    AsyncImage(
                        model = viewModel.widgetPhotoBgUri,
                        contentDescription = "Widget Background Photo",
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    // Dimming overlay for text readability
                    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.35f)))
                }
                "animated_lottie" -> {
                    // Custom fluid drawing depending on chosen theme
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val width = size.width
                        val height = size.height
                        
                        when (viewModel.widgetLottieTheme) {
                            "Aurora Lights" -> {
                                // Sweeping circular aurora gradient
                                drawRect(
                                    brush = Brush.sweepGradient(
                                        colors = listOf(
                                            Color(0xFF0F172A),
                                            Color(0xFF0D9488),
                                            Color(0xFF7C3AED),
                                            Color(0xFF0F172A)
                                        ),
                                        center = Offset(width / 2, height / 2)
                                    )
                                )
                            }
                            "Cosmic Wave" -> {
                                // Draw background deep indigo
                                drawRect(color = Color(0xFF1E1B4B))
                                // Pulsing pink/purple wave
                                val wavePath = Path()
                                wavePath.moveTo(0f, height / 2)
                                for (x in 0..width.toInt() step 5) {
                                    val waveY = height / 2 + sin((x / width) * 2 * Math.PI + animOffset * 2 * Math.PI).toFloat() * 15f
                                    wavePath.lineTo(x.toFloat(), waveY)
                                }
                                wavePath.lineTo(width, height)
                                wavePath.lineTo(0f, height)
                                wavePath.close()
                                drawPath(
                                    path = wavePath,
                                    brush = Brush.verticalGradient(
                                        colors = listOf(Color(0xFFEC4899).copy(alpha = 0.4f), Color.Transparent)
                                    )
                                )
                            }
                            "Focus Bubble" -> {
                                // Draw golden slate background
                                drawRect(color = Color(0xFF1F2937))
                                // Render rising circles
                                bubbles.forEach { (xF, yF, radius) ->
                                    val x = xF * width
                                    val y = ((yF - animOffset + 1f) % 1f) * height
                                    drawCircle(
                                        color = Color(0xFFF59E0B).copy(alpha = 0.25f),
                                        radius = radius.toPx(),
                                        center = Offset(x, y)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // OVERLAY TEXT & BRANDING (Mirroring the RemoteViews structure)
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Widget Alarm Icon
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = "Widget Icon",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "FocusCube",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = when (viewModel.widgetBackgroundType) {
                            "animated_lottie" -> "Theme: ${viewModel.widgetLottieTheme}"
                            "photo" -> "Static Backdrop"
                            else -> "Solid Accent Frame"
                        },
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 11.sp
                    )
                }

                com.example.ui.components.GlassButton(
                    onClick = {
                        Toast.makeText(context, "Initiating Focus Session...", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accentColor,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text(text = "START", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // SELECTION TABS: SOLID, PHOTO, ANIMATED
        Text(
            text = "CHOOSE BACKGROUND TYPE",
            color = viewModel.themeTextSecondary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val bgTypes = listOf(
                "solid_color" to "Solid",
                "photo" to "Photo",
                "animated_lottie" to "Animated"
            )
            bgTypes.forEach { (type, label) ->
                val isSelected = viewModel.widgetBackgroundType == type
                com.example.ui.components.GlassButton(
                    onClick = { viewModel.updateWidgetBackgroundType(type) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) accentColor else viewModel.themeDivider,
                        contentColor = if (isSelected) Color.White else viewModel.themeText
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).height(36.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // DYNAMIC PARAMETER SELECTION PANELS
        when (viewModel.widgetBackgroundType) {
            "solid_color" -> {
                Text(
                    text = "SELECT SOLID ACCENT COLOR",
                    color = viewModel.themeTextSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val colors = listOf(
                        "#111827" to "Midnight",
                        "#1E1B4B" to "Cosmic",
                        "#064E3B" to "Emerald",
                        "#7C2D12" to "Sunset",
                        "#4C0519" to "Crimson"
                    )
                    colors.forEach { (hex, name) ->
                        val isSelected = viewModel.widgetSolidColorHex == hex
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(android.graphics.Color.parseColor(hex)))
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) Color.White else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { viewModel.updateWidgetSolidColorHex(hex) }
                        )
                    }
                }
            }
            "photo" -> {
                Text(
                    text = "SELECT PHOTO BACKDROP",
                    color = viewModel.themeTextSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val photos = listOf(
                        "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=200" to "Beach",
                        "https://images.unsplash.com/photo-1441974231531-c6227db76b6e?w=200" to "Forest",
                        "https://images.unsplash.com/photo-1506318137071-a8e063b4bec0?w=200" to "Galaxy",
                        "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=200" to "Mountain"
                    )
                    photos.forEach { (url, label) ->
                        val isSelected = viewModel.widgetPhotoBgUri == url
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) accentColor else Color.Transparent,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { viewModel.updateWidgetPhotoBgUri(url) }
                        ) {
                            AsyncImage(
                                model = url,
                                contentDescription = label,
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Box(
                                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = label, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
            "animated_lottie" -> {
                Text(
                    text = "SELECT ANIMATED THEME EFFECT",
                    color = viewModel.themeTextSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val themes = listOf("Aurora Lights", "Cosmic Wave", "Focus Bubble")
                    themes.forEach { theme ->
                        val isSelected = viewModel.widgetLottieTheme == theme
                        com.example.ui.components.GlassButton(
                            onClick = { viewModel.updateWidgetLottieTheme(theme) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) accentColor.copy(alpha = 0.2f) else viewModel.themeDivider,
                                contentColor = if (isSelected) accentColor else viewModel.themeText
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                width = 1.dp,
                                color = if (isSelected) accentColor else Color.Transparent
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).height(38.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(text = theme, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BackgroundCarousel(viewModel: com.example.data.FocusViewModel, accentColor: Color) {
    val demoCards = listOf(
        "" to "Default Animated Blobs",
        "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=400&h=700&fit=crop" to "Mountain landscape",
        "https://images.unsplash.com/photo-1511765224389-37f0e77cf0eb?w=400&h=700&fit=crop" to "City night",
        "https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?w=400&h=700&fit=crop" to "Foggy forest",
        "https://images.unsplash.com/photo-1441974231531-c6227db76b6e?w=400&h=700&fit=crop" to "Sunlit woods",
        "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=400&h=700&fit=crop" to "Tropical beach",
        "https://images.unsplash.com/photo-1519681393784-d120267933ba?w=400&h=700&fit=crop" to "Starry mountain"
    )

    androidx.compose.foundation.lazy.LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(demoCards.size) { index ->
            val (url, alt) = demoCards[index]
            val isSelected = viewModel.backgroundImageUrl == url
            
            Box(
                modifier = Modifier
                    .width(100.dp)
                    .height(140.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(
                        width = if (isSelected) 3.dp else 1.dp,
                        color = if (isSelected) accentColor else viewModel.themeGlassBorder,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { viewModel.setBackgroundImage(url) }
            ) {
                if (url.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                androidx.compose.ui.graphics.Brush.linearGradient(
                                    colors = listOf(Color(0xFF6B4EE6), Color(0xFFE84C7B))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "Default", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    coil.compose.AsyncImage(
                        model = url,
                        contentDescription = alt,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(accentColor.copy(alpha = 0.3f))
                        )
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "Selected",
                            tint = Color.White,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(4.dp)
                                .background(accentColor, CircleShape)
                                .padding(2.dp)
                                .size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
