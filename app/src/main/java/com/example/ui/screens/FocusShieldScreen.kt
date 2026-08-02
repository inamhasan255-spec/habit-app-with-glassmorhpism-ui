package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.example.data.BlockedApp
import com.example.data.FocusViewModel
import com.example.ui.components.*

@Composable
fun FocusShieldScreen(viewModel: FocusViewModel) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val blockedAppsList by viewModel.blockedApps.collectAsState()
    val deviceAppsList by viewModel.deviceApps.collectAsState()
    val accentColor = Color(android.graphics.Color.parseColor(viewModel.accentColorHex))

    var showAddAppDialog by remember { mutableStateOf(false) }
    var showOptionsDialog by remember { mutableStateOf(false) }
    var selectedAppForOptions by remember { mutableStateOf<BlockedApp?>(null) }
    var hasPermission by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            hasPermission = try {
                val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as android.app.AppOpsManager
                val mode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    appOps.unsafeCheckOpNoThrow(
                        android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
                        android.os.Process.myUid(),
                        context.packageName
                    )
                } else {
                    appOps.checkOpNoThrow(
                        android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
                        android.os.Process.myUid(),
                        context.packageName
                    )
                }
                mode == android.app.AppOpsManager.MODE_ALLOWED
            } catch (e: Exception) {
                false
            }
            kotlinx.coroutines.delay(1000)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(20.dp))
            .background(viewModel.themeGlassFill)
            .border(1.dp, viewModel.themeGlassBorder, RoundedCornerShape(20.dp))
            .padding(bottom = 100.dp) // Cushion above navigation bar
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Header Icon Section
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AntiGravityWrapper {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(viewModel.themeGlassFill)
                            .border(1.dp, viewModel.themeGlassBorder, RoundedCornerShape(20.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Shield",
                            tint = accentColor,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Focus Shield",
                    color = viewModel.themeText,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Distraction apps are currently blocked",
                    color = viewModel.themeTextSecondary,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Active Shield Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(viewModel.themeGlassFill)
                        .border(1.dp, viewModel.themeGlassBorder, RoundedCornerShape(100.dp))
                        .clickable {
                            viewModel.shieldActive = !viewModel.shieldActive
                            viewModel.updateServicePreferences()
                            viewModel.syncDataToFirebase()
                        }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (viewModel.shieldActive) AccentTeal else Color.Gray)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (viewModel.shieldActive) "SHIELD ACTIVE" else "SHIELD STANDBY",
                        color = if (viewModel.shieldActive) AccentTeal else Color.Gray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            if (!hasPermission) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(AccentPink.copy(alpha = 0.15f))
                        .border(1.dp, AccentPink.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Warning",
                                tint = AccentPink,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "SYSTEM ACCESS REQUIRED",
                                color = AccentPink,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "To monitor when restricted apps are launched during your focus blocks, the 'Focus Shield' service requires the Android Usage Stats permission.",
                            color = viewModel.themeTextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        com.example.ui.components.GlassButton(
                            onClick = {
                                try {
                                    val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    val intent = Intent(Settings.ACTION_SETTINGS).apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    }
                                    context.startActivity(intent)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentPink),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Grant Usage Permission",
                                color = if (viewModel.isDarkMode) Color.Black else Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Blocked Applications List
            Text(
                text = "Protected Applications",
                color = viewModel.themeText,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (blockedAppsList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "No apps in focus blocked list.", color = viewModel.themeTextSecondary, fontSize = 13.sp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(blockedAppsList) { app ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
            .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        selectedAppForOptions = app
                                        showOptionsDialog = true
                                    }
                            ) {
                                // Monogram styled placeholder for app icon
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(viewModel.themeGlassFill)
                                        .border(1.dp, viewModel.themeGlassBorder, RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = app.appName.firstOrNull()?.toString()?.uppercase() ?: "A",
                                        color = accentColor,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                Column {
                                    Text(
                                        text = app.appName,
                                        color = viewModel.themeText,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)
    ) {
                                        Text(
                                            text = app.category,
                                            color = viewModel.themeTextSecondary,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.5.sp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "•  ${app.blockMode}",
                                            color = AccentPink,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Remove",
                                    tint = viewModel.themeTextSecondary.copy(0.7f),
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clickable { viewModel.deleteBlockedApp(app) }
                                )
                                Spacer(modifier = Modifier.width(16.dp))

                                GlassToggle(
                                    checked = app.isBlocked,
                                    onCheckedChange = { viewModel.toggleAppBlocked(app) },
                                    /* colors omitted for GlassToggle */
                                )
                            }
                        }
                    }
                }
            }
        }

        // Add App Floating Action Button
        FloatingActionButton(
            onClick = { showAddAppDialog = true },
            containerColor = accentColor,
            contentColor = if (viewModel.isDarkMode) Color.Black else Color.White,
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 24.dp, end = 24.dp)
                .size(60.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Protect App",
                modifier = Modifier.size(28.dp)
            )
        }
    }

    // Add Application dialog showing actual installed device packages!
    if (showAddAppDialog) {
        var selectedCategory by remember { mutableStateOf("SOCIAL MEDIA") }
        var searchQuery by remember { mutableStateOf("") }
        val categories = listOf("SOCIAL MEDIA", "ENTERTAINMENT", "COMMUNICATION")

        val filteredDeviceApps = remember(deviceAppsList, searchQuery) {
            deviceAppsList.filter {
                it.appName.contains(searchQuery, ignoreCase = true) ||
                it.packageName.contains(searchQuery, ignoreCase = true)
            }
        }

        AlertDialog(
            onDismissRequest = { showAddAppDialog = false },
            containerColor = viewModel.themeSurface,
            title = {
                Text(
                    text = "Protect Device Application",
                    color = viewModel.themeText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Category Selection
                    Column {
                        Text(text = "App Category", color = viewModel.themeText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            categories.forEach { cat ->
                                val isSelected = selectedCategory == cat
                                Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) accentColor else viewModel.themeGlassFill)
                                        .clickable { selectedCategory = cat }
                                        
            .clip(RoundedCornerShape(12.dp))
            .background(viewModel.themeGlassFill)
            .border(1.dp, viewModel.themeGlassBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = cat,
                                        color = if (isSelected) (if (viewModel.isDarkMode) Color.Black else Color.White) else viewModel.themeText,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // Search field
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search installed apps...", color = viewModel.themeTextSecondary, fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = viewModel.themeText,
                            unfocusedTextColor = viewModel.themeText,
                            focusedBorderColor = accentColor,
                            unfocusedBorderColor = viewModel.themeGlassBorder,
                            focusedContainerColor = viewModel.themeGlassFill,
                            unfocusedContainerColor = viewModel.themeGlassFill
                        )
                    )

                    // Device apps list
                    Text(text = "Select from Installed Apps", color = viewModel.themeText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
            .padding(8.dp)
    ) {
                        if (filteredDeviceApps.isEmpty()) {
                            Box(
        modifier = Modifier
            .fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    text = if (deviceAppsList.isEmpty()) "Loading installed apps..." else "No apps match your search.",
                                    color = viewModel.themeTextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(filteredDeviceApps) { appInfo ->
                                    val isCurrentApp = appInfo.packageName == context.packageName
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .then(
                                                if (isCurrentApp) {
                                                    Modifier.alpha(0.6f)
                                                } else {
                                                    Modifier.clickable {
                                                        viewModel.addCustomBlockedApp(appInfo.packageName, appInfo.appName, selectedCategory)
                                                        showAddAppDialog = false
                                                    }
                                                }
                                            )
                                            
            .clip(RoundedCornerShape(12.dp))
            .background(viewModel.themeGlassFill)
            .border(1.dp, viewModel.themeGlassBorder, RoundedCornerShape(12.dp))
            .padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Load actual app icon
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(viewModel.themeGlassFill),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            androidx.compose.ui.viewinterop.AndroidView(
                                                factory = { ctx ->
                                                    android.widget.ImageView(ctx).apply {
                                                        val pm = ctx.packageManager
                                                        try {
                                                            setImageDrawable(pm.getApplicationIcon(appInfo.packageName))
                                                        } catch (e: Exception) {
                                                            // Fallback to text inside canvas/box
                                                        }
                                                    }
                                                },
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(text = appInfo.appName, color = viewModel.themeText, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                                if (isCurrentApp) {
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(4.dp))
                                                            .background(accentColor.copy(alpha = 0.15f))
                                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                                    ) {
                                                        Text(
                                                            text = "Immune",
                                                            color = accentColor,
                                                            fontSize = 8.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                }
                                            }
                                            Text(text = appInfo.packageName, color = viewModel.themeTextSecondary, fontSize = 10.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                com.example.ui.components.GlassButton(variant = com.example.ui.components.GlassButtonVariant.Secondary, onClick = { showAddAppDialog = false }) {
                    Text(text = "Close", color = viewModel.themeTextSecondary)
                }
            }
        )
    }

    // Interactive App Options and Blocking Policies Dialog
    if (showOptionsDialog && selectedAppForOptions != null) {
        val app = selectedAppForOptions!!
        var currentBlockMode by remember(app.id) { mutableStateOf(app.blockMode) }
        var currentCategory by remember(app.id) { mutableStateOf(app.category) }
        var isCurrentlyBlocked by remember(app.id) { mutableStateOf(app.isBlocked) }

        val row1Modes = listOf("Session only", "Scheduled", "Always block")
        val row2Modes = listOf("Limit: 15m/day", "Limit: 30m/day", "Limit: 1h/day")

        val row1Categories = listOf("SOCIAL MEDIA", "ENTERTAINMENT")
        val row2Categories = listOf("COMMUNICATION", "GAMES", "UTILITIES")

        AlertDialog(
            onDismissRequest = { showOptionsDialog = false },
            containerColor = viewModel.themeSurface,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(accentColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = app.appName.firstOrNull()?.toString()?.uppercase() ?: "A",
                            color = accentColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = app.appName,
                            color = viewModel.themeText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = app.packageName,
                            color = viewModel.themeTextSecondary,
                            fontSize = 10.sp
                        )
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. Status toggle option
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Shield Blocking Policy", color = viewModel.themeText, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(text = "Enable or bypass restrictions", color = viewModel.themeTextSecondary, fontSize = 11.sp)
                        }
                        GlassToggle(
                            checked = isCurrentlyBlocked,
                            onCheckedChange = { isCurrentlyBlocked = it },
                            /* colors omitted for GlassToggle */
                        )
                    }

                    // 2. Block Mode Options
                    Column {
                        Text(
                            text = "Blocking Policy Options",
                            color = viewModel.themeText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                row1Modes.forEach { mode ->
                                    val isSelected = currentBlockMode == mode
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (isSelected) accentColor else viewModel.themeGlassFill)
                                            .border(1.dp, if (isSelected) accentColor else viewModel.themeGlassBorder, RoundedCornerShape(10.dp))
                                            .clickable { currentBlockMode = mode }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = mode,
                                            color = if (isSelected) (if (viewModel.isDarkMode) Color.Black else Color.White) else viewModel.themeText,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                row2Modes.forEach { mode ->
                                    val isSelected = currentBlockMode == mode
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (isSelected) accentColor else viewModel.themeGlassFill)
                                            .border(1.dp, if (isSelected) accentColor else viewModel.themeGlassBorder, RoundedCornerShape(10.dp))
                                            .clickable { currentBlockMode = mode }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = mode,
                                            color = if (isSelected) (if (viewModel.isDarkMode) Color.Black else Color.White) else viewModel.themeText,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 3. Category Options
                    Column {
                        Text(
                            text = "App Category Type",
                            color = viewModel.themeText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                row1Categories.forEach { cat ->
                                    val isSelected = currentCategory == cat
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (isSelected) accentColor.copy(alpha = 0.2f) else Color.Transparent)
                                            .border(1.dp, if (isSelected) accentColor else viewModel.themeGlassBorder, RoundedCornerShape(10.dp))
                                            .clickable { currentCategory = cat }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = cat,
                                            color = if (isSelected) accentColor else viewModel.themeTextSecondary,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                row2Categories.forEach { cat ->
                                    val isSelected = currentCategory == cat
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (isSelected) accentColor.copy(alpha = 0.2f) else Color.Transparent)
                                            .border(1.dp, if (isSelected) accentColor else viewModel.themeGlassBorder, RoundedCornerShape(10.dp))
                                            .clickable { currentCategory = cat }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = cat,
                                            color = if (isSelected) accentColor else viewModel.themeTextSecondary,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                com.example.ui.components.GlassButton(
                    onClick = {
                        viewModel.updateBlockedAppDetails(
                            app = app,
                            isBlocked = isCurrentlyBlocked,
                            blockMode = currentBlockMode,
                            category = currentCategory
                        )
                        showOptionsDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                ) {
                    Text("Apply Options", color = if (viewModel.isDarkMode) Color.Black else Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                com.example.ui.components.GlassButton(variant = com.example.ui.components.GlassButtonVariant.Secondary, onClick = { showOptionsDialog = false }) {
                    Text("Cancel", color = viewModel.themeTextSecondary, fontSize = 12.sp)
                }
            }
        )
    }
}
