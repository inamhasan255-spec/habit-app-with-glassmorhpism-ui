

package com.focuscube.ui.dashboard
import com.example.ui.components.GlassToggle


import androidx.compose.foundation.background

import androidx.compose.foundation.border

import androidx.compose.foundation.clickable

import androidx.compose.foundation.layout.*

import androidx.compose.foundation.lazy.LazyColumn

import androidx.compose.foundation.lazy.items

import androidx.compose.foundation.shape.CircleShape

import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material.icons.Icons

import androidx.compose.material.icons.filled.*

import androidx.compose.material3.*

import androidx.compose.runtime.*

import androidx.compose.ui.Alignment

import androidx.compose.ui.Modifier

import androidx.compose.ui.draw.clip

import androidx.compose.ui.graphics.Color

import androidx.compose.ui.text.font.FontWeight

import androidx.compose.ui.unit.dp

import androidx.compose.ui.unit.sp

import androidx.lifecycle.viewmodel.compose.viewModel

import com.focuscube.data.model.AssignmentEntity

import com.focuscube.audio.StereoBinauralEngine

import java.util.Calendar


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = viewModel()
) {
    val isLocked by viewModel.isSessionLocked.collectAsState()
    val assignmentsWithCushions by viewModel.assignmentsWithCushions.collectAsState()
    val academicTasks by viewModel.academicTasks.collectAsState()
    val availabilityBlocks by viewModel.availabilityBlocks.collectAsState()
    val blockState by viewModel.blockState.collectAsState()
    val todos by viewModel.todos.collectAsState()
    val calendarEvents by viewModel.calendarEvents.collectAsState()
    
    val context = androidx.compose.ui.platform.LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.loadCalendarEvents(context)
    }
    
    var isAudioPlaying by remember { mutableStateOf(StereoBinauralEngine.isCurrentlyPlaying()) }

    var showAddDialog by remember { mutableStateOf(false) }

    // Calculate if any task is in deficit
    val hasDeficit = assignmentsWithCushions.any { it.second < 0.0 } || blockState == com.focuscube.data.model.BlockState.STRICT_LOCK
    
    var timerProgress by remember { mutableStateOf(0.75f) }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedMeshBackground()
        
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Focus Cube Guard",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Focus Cube Guard",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                actions = {
                    com.example.ui.components.GlassIconButton(
                        onClick = { viewModel.clearAllAssignments() },
                        contentDescription = "Clear All Assignments",
                        tooltipText = "Clear All"
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteForever,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Assignment")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Focus Guard Active State Banner
            item {
                val strictModeActive = blockState == com.focuscube.data.model.BlockState.STRICT_LOCK
                val guardBg = if (strictModeActive) {
                    MaterialTheme.colorScheme.errorContainer
                } else {
                    MaterialTheme.colorScheme.primaryContainer
                }
                val guardContentColor = if (strictModeActive) {
                    MaterialTheme.colorScheme.onErrorContainer
                } else {
                    MaterialTheme.colorScheme.onPrimaryContainer
                }
                
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    containerColor = guardBg.copy(alpha = 0.3f)
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(guardContentColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (strictModeActive) Icons.Default.SecurityUpdateWarning else Icons.Default.Shield,
                                contentDescription = "Engine Status",
                                tint = guardContentColor,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (strictModeActive) "STRICT LOCK ENFORCED" else "NORMAL CAPACITY",
                                fontWeight = FontWeight.Bold,
                                color = guardContentColor,
                                fontSize = 16.sp
                            )
                            Text(
                                text = if (strictModeActive) {
                                    "Time Cushion < 0 for high-priority task! Targeted apps are strictly locked."
                                } else {
                                    "All high-priority cushions stable. Blocker operates on session schedule."
                                },
                                color = guardContentColor.copy(alpha = 0.8f),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // Interactive 15-Minute Availability Blocks Planner
            item {
                var selectedDay by remember { mutableStateOf<Int>(Calendar.MONDAY) }
                var showBlocksPlanner by remember { mutableStateOf(false) }

                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp).copy(alpha = 0.3f)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Discrete Availability",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        text = "15-minute intervals configuration",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            com.example.ui.components.GlassButton(variant = com.example.ui.components.GlassButtonVariant.Secondary, onClick = { showBlocksPlanner = !showBlocksPlanner }) {
                                Text(if (showBlocksPlanner) "Collapse" else "Configure")
                            }
                        }

                        if (showBlocksPlanner) {
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Day Selector Tab Row
                            val days = listOf<Pair<String, Int>>(
                                "Sun" to Calendar.SUNDAY,
                                "Mon" to Calendar.MONDAY,
                                "Tue" to Calendar.TUESDAY,
                                "Wed" to Calendar.WEDNESDAY,
                                "Thu" to Calendar.THURSDAY,
                                "Fri" to Calendar.FRIDAY,
                                "Sat" to Calendar.SATURDAY
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                days.forEach { pair ->
                                    val label = pair.first
                                    val dayConst = pair.second
                                    val isSel = selectedDay == dayConst
                                    val btnBg = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                                    val btnColor = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(horizontal = 2.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(btnBg)
                                            .clickable { selectedDay = dayConst }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = btnColor)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Quick 15-min Slot Presets:",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            val presets = listOf<Triple<String, IntRange, String>>(
                                Triple("Morning (08:00 - 12:00)", 32..48, "Focus blocks 32-48"),
                                Triple("Afternoon (14:00 - 18:00)", 56..72, "Focus blocks 56-72"),
                                Triple("Evening (19:00 - 22:00)", 76..88, "Focus blocks 76-88"),
                                Triple("Late Night (22:00 - 00:00)", 88..95, "Focus blocks 88-95")
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                presets.forEach { triple ->
                                    val title = triple.first
                                    val range = triple.second
                                    val desc = triple.third
                                    val activeInDay = availabilityBlocks.filter { it.dayOfWeek == selectedDay && it.slotIndex in range }
                                    val isActive = activeInDay.isNotEmpty() && activeInDay.all { it.isAvailable }

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                            .clickable {
                                                range.forEach { slot ->
                                                    viewModel.toggleAvailabilityBlock(selectedDay, slot)
                                                }
                                            }
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                            Text(text = desc, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Checkbox(
                                            checked = isActive,
                                            onCheckedChange = {
                                                range.forEach { slot ->
                                                    viewModel.toggleAvailabilityBlock(selectedDay, slot)
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Dynamic Warning Banner changing to stark error color if T_C drops into deficit
            item {
                val bannerBg = if (hasDeficit) {
                    MaterialTheme.colorScheme.errorContainer
                } else {
                    MaterialTheme.colorScheme.secondaryContainer
                }
                val bannerContentColor = if (hasDeficit) {
                    MaterialTheme.colorScheme.onErrorContainer
                } else {
                    MaterialTheme.colorScheme.onSecondaryContainer
                }
                val bannerIcon = if (hasDeficit) Icons.Default.Warning else Icons.Default.CheckCircle

                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    containerColor = bannerBg.copy(alpha = 0.3f)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = bannerIcon,
                            contentDescription = null,
                            tint = bannerContentColor,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (hasDeficit) "Time Cushion Deficit Alert!" else "Time Cushion Health Stable",
                                fontWeight = FontWeight.Bold,
                                color = bannerContentColor,
                                fontSize = 16.sp
                            )
                            Text(
                                text = if (hasDeficit) {
                                    "Your study hours are insufficient! Focus locks and accessibility blocks are strictly enforced."
                                } else {
                                    "Your current plan maintains a positive study hour buffer. Keep up the high-focus streak!"
                                },
                                color = bannerContentColor.copy(alpha = 0.85f),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // Active Session Lock Status Card
            item {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            if (isLocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                            RoundedCornerShape(24.dp)
                        ),
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp).copy(alpha = 0.3f),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (isLocked) "Deep Focus Locked" else "Focus Shield Ready",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isLocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (isLocked) "Accessibility blocker is strictly active" else "Session settings are unlocked",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            GlassToggle(
                                checked = isLocked,
                                onCheckedChange = { viewModel.toggleSessionLock() }
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                        // Cognitive Audio Controls (Stereo Binaural Beats)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Hearing,
                                    contentDescription = "Binaural Beats",
                                    tint = if (isAudioPlaying) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Binaural Beats Synthesis",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "15Hz Beta Focus (200Hz / 215Hz)",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            com.example.ui.components.GlassButton(
                                onClick = {
                                    if (isAudioPlaying) {
                                        StereoBinauralEngine.stop(context)
                                        isAudioPlaying = false
                                    } else {
                                        StereoBinauralEngine.start(context)
                                        isAudioPlaying = true
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isAudioPlaying) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(if (isAudioPlaying) "Stop" else "Synthesize")
                            }
                        }
                    }
                }
            }
            
            // Todo List Section
            // EXTERNAL CALENDAR EVENTS
            item {
                if (calendarEvents.isNotEmpty()) {
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        shape = RoundedCornerShape(24.dp),
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(bottom = 12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.DateRange,
                                    contentDescription = "Calendar",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Synced Calendar Events",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            calendarEvents.forEach { event ->
                                val startTime = java.text.SimpleDateFormat("MMM dd, h:mm a", java.util.Locale.getDefault()).format(java.util.Date(event.startTime))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(event.color).copy(alpha = 0.2f))
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(Color(event.color))
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = event.title,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = startTime,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                InteractiveTodoList(
                    todos = todos,
                    onAddTodo = { viewModel.addTodo(it) },
                    onToggle = { viewModel.toggleTodoCompletion(it) },
                    onDelete = { viewModel.deleteTodo(it) },
                    onReorder = { from, to -> viewModel.reorderTodos(from, to) }
                )
            }
            
            // Focus Session Ring
            item {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    shape = RoundedCornerShape(24.dp),
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp).copy(alpha = 0.3f)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Focus Session",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Box(contentAlignment = Alignment.Center) {
                            FluidFocusRing(progress = timerProgress)
                            
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${(timerProgress * 100).toInt()}%",
                                    style = MaterialTheme.typography.displayMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Remaining",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            com.example.ui.components.GlassButton(onClick = { timerProgress = (timerProgress - 0.1f).coerceAtLeast(0f) }) {
                                Text("-10%")
                            }
                            com.example.ui.components.GlassButton(onClick = { timerProgress = (timerProgress + 0.1f).coerceAtMost(1f) }) {
                                Text("+10%")
                            }
                        }
                    }
                }
            }

            // Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Capacity Budget & Planner",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${assignmentsWithCushions.size} tasks",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (assignmentsWithCushions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Assignment,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No academic assignments added yet.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            } else {
                items(assignmentsWithCushions, key = { it.first.id }) { (assignment, cushion) ->
                    val hasSurplus = cushion >= 0.0
                    val indicatorColor = if (hasSurplus) Color(0xFF4CAF50) else Color(0xFFF44336)
                    val indicatorText = if (hasSurplus) "Surplus: +${String.format("%.1f", cushion)}h" else "Deficit: ${String.format("%.1f", cushion)}h"

                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp).copy(alpha = 0.3f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(indicatorColor)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = assignment.courseTitle,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Weight: ${assignment.gradingWeight}%",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = assignment.title,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.HourglassEmpty,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "${assignment.estimatedDurationHours} hrs",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.CalendarToday,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = assignment.rawDueDate,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Badge(
                                    containerColor = indicatorColor.copy(alpha = 0.15f),
                                    contentColor = indicatorColor,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                ) {
                                    Text(
                                        text = indicatorText,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                com.example.ui.components.GlassIconButton(
                                    onClick = { viewModel.deleteAssignment(assignment.id) },
                                    contentDescription = "Delete assignment ${assignment.title}",
                                    tooltipText = "Delete Assignment"
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var title by remember { mutableStateOf("") }
        var course by remember { mutableStateOf("") }
        var dueDate by remember { mutableStateOf("") }
        var durationStr by remember { mutableStateOf("") }
        var weightStr by remember { mutableStateOf("") }
        var isHighPriority by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Academic Task") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Assignment Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = course,
                        onValueChange = { course = it },
                        label = { Text("Course / Subject") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = dueDate,
                        onValueChange = { dueDate = it },
                        label = { Text("Due Date (YYYY-MM-DD)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = durationStr,
                            onValueChange = { durationStr = it },
                            label = { Text("Duration (Hrs)") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = weightStr,
                            onValueChange = { weightStr = it },
                            label = { Text("Weight (%)") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("High Priority Task", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Triggers strict system block if Time Cushion falls into deficit.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        GlassToggle(
                            checked = isHighPriority,
                            onCheckedChange = { isHighPriority = it }
                        )
                    }
                }
            },
            confirmButton = {
                com.example.ui.components.GlassButton(
                    onClick = {
                        val duration = durationStr.toDoubleOrNull() ?: 1.0
                        val weight = weightStr.toDoubleOrNull() ?: 10.0
                        viewModel.addAssignment(
                            title = title,
                            rawDueDate = dueDate,
                            estimatedHours = duration,
                            weight = weight,
                            course = course
                        )
                        viewModel.addAcademicTask(
                            title = title,
                            rawDueDate = dueDate,
                            estimatedHours = duration,
                            weight = weight,
                            course = course,
                            isHighPriority = isHighPriority
                        )
                        showAddDialog = false
                    },
                    enabled = title.isNotBlank() && course.isNotBlank() && dueDate.isNotBlank()
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                com.example.ui.components.GlassButton(variant = com.example.ui.components.GlassButtonVariant.Secondary, onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
}
