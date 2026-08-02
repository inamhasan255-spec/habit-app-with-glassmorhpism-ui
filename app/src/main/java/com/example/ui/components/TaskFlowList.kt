package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

data class TaskFlowItem(
    val id: String,
    val title: String,
    val subtitle: String = "",
    val category: String = "Deep Work",
    val isCompleted: Boolean = false,
    val priority: String = "Normal" // "High", "Normal", "Low"
)

@Composable
fun TaskFlowList(
    modifier: Modifier = Modifier,
    initialTasks: List<TaskFlowItem> = emptyList(),
    accentColor: Color = Color(0xFF38BDF8),
    title: String = "Today's Flow",
    onTaskCompleteToggle: ((TaskFlowItem) -> Unit)? = null,
    onTaskDelete: ((TaskFlowItem) -> Unit)? = null,
    onTaskAdd: ((TaskFlowItem) -> Unit)? = null
) {
    val isDark = isSystemInDarkTheme()
    val haptic = LocalHapticFeedback.current

    // Default sample tasks if caller passes an empty list
    val defaultSampleTasks = remember {
        listOf(
            TaskFlowItem(
                id = "task_1",
                title = "Morning pages · 15 min",
                subtitle = "Mindfulness journal entry",
                category = "Mindset",
                isCompleted = false,
                priority = "Normal"
            ),
            TaskFlowItem(
                id = "task_2",
                title = "Deep work — Q3 strategy doc",
                subtitle = "Focus block 1 · Do not disturb",
                category = "Deep Work",
                isCompleted = false,
                priority = "High"
            ),
            TaskFlowItem(
                id = "task_3",
                title = "Review design crit with Theo",
                subtitle = "Figma prototypes & feedback",
                category = "Collaboration",
                isCompleted = false,
                priority = "Normal"
            ),
            TaskFlowItem(
                id = "task_4",
                title = "Walk · phone-free · 20 min",
                subtitle = "Outdoor recovery & recharge",
                category = "Wellness",
                isCompleted = true,
                priority = "Low"
            )
        )
    }

    val taskList = remember {
        mutableStateListOf<TaskFlowItem>().apply {
            if (initialTasks.isNotEmpty()) {
                addAll(initialTasks)
            } else {
                addAll(defaultSampleTasks)
            }
        }
    }

    var selectedCategory by remember { mutableStateOf("All") }
    var newTaskTitle by remember { mutableStateOf("") }
    var showAddTaskInput by remember { mutableStateOf(false) }

    val categories = remember(taskList) {
        listOf("All") + taskList.map { it.category }.distinct()
    }

    val filteredTasks = remember(taskList.toList(), selectedCategory) {
        if (selectedCategory == "All") {
            taskList
        } else {
            taskList.filter { it.category == selectedCategory }
        }
    }

    val completedCount = taskList.count { it.isCompleted }
    val totalCount = taskList.size
    val progressFraction = if (totalCount > 0) completedCount.toFloat() / totalCount.toFloat() else 0f

    val textPrimary = if (isDark) Color.White else Color(0xFF1E293B)
    val textSecondary = if (isDark) Color.White.copy(alpha = 0.6f) else Color(0xFF64748B)

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Header Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = title,
                    color = textPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "$completedCount/$totalCount completed · Swipe right to finish",
                    color = textSecondary,
                    fontSize = 12.sp
                )
            }

            // Quick Add Toggle Button
            IconButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    showAddTaskInput = !showAddTaskInput
                },
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.15f))
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Task",
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Glass Progress Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape)
                .background(if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f))
        ) {
            val animatedProgress by animateFloatAsState(
                targetValue = progressFraction,
                animationSpec = tween(600, easing = FastOutSlowInEasing),
                label = "task_progress"
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(accentColor)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Interactive Add Task Input (Toggleable)
        if (showAddTaskInput) {
            GlassContainer(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                shape = RoundedCornerShape(16.dp),
                backgroundColor = if (isDark) Color(0xFF1E1E26).copy(alpha = 0.7f) else Color.White.copy(alpha = 0.8f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newTaskTitle,
                        onValueChange = { newTaskTitle = it },
                        placeholder = {
                            Text(
                                "New task title...",
                                color = textSecondary.copy(alpha = 0.7f),
                                fontSize = 14.sp
                            )
                        },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = textPrimary,
                            unfocusedTextColor = textPrimary
                        )
                    )

                    TextButton(
                        onClick = {
                            if (newTaskTitle.isNotBlank()) {
                                val item = TaskFlowItem(
                                    id = "task_${System.currentTimeMillis()}",
                                    title = newTaskTitle.trim(),
                                    subtitle = "Custom task",
                                    category = "Focus",
                                    isCompleted = false,
                                    priority = "Normal"
                                )
                                taskList.add(0, item)
                                onTaskAdd?.invoke(item)
                                newTaskTitle = ""
                                showAddTaskInput = false
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = accentColor)
                    ) {
                        Text("Add", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Category Filters
        if (categories.size > 2) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { category ->
                    val isSelected = selectedCategory == category
                    val chipBg = if (isSelected) {
                        accentColor.copy(alpha = 0.2f)
                    } else {
                        if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.04f)
                    }
                    val chipBorder = if (isSelected) accentColor else Color.Transparent

                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(chipBg)
                            .border(1.dp, chipBorder, CircleShape)
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                selectedCategory = category
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = category,
                            color = if (isSelected) accentColor else textSecondary,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Tasks List
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            filteredTasks.forEach { task ->
                TaskFlowItemCard(
                    item = task,
                    accentColor = accentColor,
                    onToggleComplete = { toggledItem ->
                        val idx = taskList.indexOfFirst { it.id == toggledItem.id }
                        if (idx != -1) {
                            val updated = toggledItem.copy(isCompleted = !toggledItem.isCompleted)
                            taskList[idx] = updated
                            onTaskCompleteToggle?.invoke(updated)
                        }
                    },
                    onDelete = { deletedItem ->
                        val idx = taskList.indexOfFirst { it.id == deletedItem.id }
                        if (idx != -1) {
                            taskList.removeAt(idx)
                            onTaskDelete?.invoke(deletedItem)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun TaskFlowItemCard(
    item: TaskFlowItem,
    accentColor: Color,
    onToggleComplete: (TaskFlowItem) -> Unit,
    onDelete: (TaskFlowItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    val offsetX = remember { androidx.compose.animation.core.Animatable(0f) }
    val swipeThreshold = 180f

    val textPrimary = if (isDark) Color.White else Color(0xFF1E293B)
    val textSecondary = if (isDark) Color.White.copy(alpha = 0.6f) else Color(0xFF64748B)

    // Background swipe reveal colors
    val completeBg = Color(0xFF10B981) // Emerald Green
    val deleteBg = Color(0xFFEF4444)   // Rose Red

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
    ) {
        // Swipe Reveal Background Layer
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(18.dp))
                .background(
                    when {
                        offsetX.value > 20f -> completeBg.copy(alpha = 0.85f)
                        offsetX.value < -20f -> deleteBg.copy(alpha = 0.85f)
                        else -> Color.Transparent
                    }
                )
                .padding(horizontal = 20.dp),
            contentAlignment = if (offsetX.value > 0) Alignment.CenterStart else Alignment.CenterEnd
        ) {
            if (offsetX.value > 20f) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Complete",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = if (item.isCompleted) "Mark Incomplete" else "Complete Task",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            } else if (offsetX.value < -20f) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Delete",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // Foreground Glass Task Item
        GlassContainer(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .pointerInput(item.id) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            if (offsetX.value > swipeThreshold) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                coroutineScope.launch {
                                    offsetX.animateTo(600f, tween(180))
                                    onToggleComplete(item)
                                    offsetX.snapTo(0f)
                                }
                            } else if (offsetX.value < -swipeThreshold) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                coroutineScope.launch {
                                    offsetX.animateTo(-600f, tween(180))
                                    onDelete(item)
                                }
                            } else {
                                coroutineScope.launch {
                                    offsetX.animateTo(
                                        0f,
                                        spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessMedium
                                        )
                                    )
                                }
                            }
                        },
                        onDragCancel = {
                            coroutineScope.launch {
                                offsetX.animateTo(0f, spring())
                            }
                        }
                    ) { change, dragAmount ->
                        change.consume()
                        coroutineScope.launch {
                            offsetX.snapTo(offsetX.value + dragAmount)
                        }
                    }
                },
            shape = RoundedCornerShape(18.dp),
            backgroundColor = if (item.isCompleted) {
                if (isDark) Color(0xFF14141A).copy(alpha = 0.35f) else Color.White.copy(alpha = 0.4f)
            } else {
                if (isDark) Color(0xFF1E1E28).copy(alpha = 0.6f) else Color.White.copy(alpha = 0.75f)
            },
            borderColor = if (item.isCompleted) {
                if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.04f)
            } else {
                if (isDark) Color.White.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.8f)
            }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Interactive Checkbox
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(7.dp))
                        .background(if (item.isCompleted) accentColor else Color.Transparent)
                        .border(
                            width = 1.5.dp,
                            color = if (item.isCompleted) accentColor else textSecondary.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(7.dp)
                        )
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onToggleComplete(item)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (item.isCompleted) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Checked",
                            tint = if (isDark) Color.Black else Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Task Title, Subtitle, Category & Strike-through animation
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Box(contentAlignment = Alignment.CenterStart) {
                        Text(
                            text = item.title,
                            color = if (item.isCompleted) textSecondary.copy(alpha = 0.5f) else textPrimary,
                            fontSize = 15.sp,
                            fontWeight = if (item.isCompleted) FontWeight.Normal else FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        // Animated Strike-through line for completed tasks
                        val strikeWidth by animateFloatAsState(
                            targetValue = if (item.isCompleted) 1f else 0f,
                            animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
                            label = "strike_through"
                        )
                        if (strikeWidth > 0f) {
                            Canvas(
                                modifier = Modifier
                                    .matchParentSize()
                                    .padding(top = 1.dp)
                            ) {
                                drawLine(
                                    color = textSecondary.copy(alpha = 0.6f),
                                    start = Offset(0f, size.height / 2f),
                                    end = Offset(size.width * strikeWidth, size.height / 2f),
                                    strokeWidth = 2.5f
                                )
                            }
                        }
                    }

                    if (item.subtitle.isNotEmpty() || item.category.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(3.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (item.category.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(accentColor.copy(alpha = 0.12f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = item.category,
                                        color = accentColor,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            if (item.subtitle.isNotEmpty()) {
                                Text(
                                    text = item.subtitle,
                                    color = textSecondary.copy(alpha = 0.75f),
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Priority Indicator / Flag
                val priorityColor = when (item.priority) {
                    "High" -> Color(0xFFEF4444) // Coral red
                    "Normal" -> accentColor
                    "Low" -> Color(0xFF10B981)  // Emerald green
                    else -> textSecondary
                }
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(priorityColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Flag,
                        contentDescription = "Priority ${item.priority}",
                        tint = priorityColor,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
