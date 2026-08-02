package com.example.ui.widget

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.zIndex
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FocusViewModel
import com.example.ui.theme.softGlassShadow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import com.example.ui.components.GlassSurface

enum class TaskColumnStatus(val label: String, val icon: String, val color: Color) {
    TODO("To Do", "📋", Color(0xFF8B5CF6)),
    IN_PROGRESS("In Progress", "⚡", Color(0xFF3B82F6)),
    DONE("Completed", "✅", Color(0xFF10B981))
}

data class DragDropTaskItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val priority: String = "Medium", // High, Medium, Low
    var status: TaskColumnStatus = TaskColumnStatus.TODO,
    val timeLabel: String = "Today"
)

object DragDropTaskWidgetSpec : WidgetSpec {
    override val id = "drag_drop_tasks"
    override val title = "Drag & Drop Task Matrix"
    override val description = "Interactive glassmorphic Kanban task board with hover glow"
    override val category = "UNIVERSAL"
    override val iconName = "TouchApp"

    @Composable
    override fun Render(
        viewModel: FocusViewModel,
        accentColor: Color,
        scope: CoroutineScope,
        selectedDateStr: String
    ) {
        val isDark = viewModel.isDarkMode

        // Sample interactive task list
        var taskList by remember {
            mutableStateOf(
                listOf(
                    DragDropTaskItem(title = "Review Quantum Physics notes", priority = "High", status = TaskColumnStatus.TODO),
                    DragDropTaskItem(title = "Complete Calculus III problem set", priority = "High", status = TaskColumnStatus.IN_PROGRESS),
                    DragDropTaskItem(title = "Draft essay introduction", priority = "Medium", status = TaskColumnStatus.TODO),
                    DragDropTaskItem(title = "Morning mindfulness session", priority = "Low", status = TaskColumnStatus.DONE),
                    DragDropTaskItem(title = "Submit lab report PDF", priority = "Medium", status = TaskColumnStatus.IN_PROGRESS)
                )
            )
        }

        var activeFilterStatus by remember { mutableStateOf<TaskColumnStatus?>(null) }
        var newTaskTitle by remember { mutableStateOf("") }
        var isAddingTask by remember { mutableStateOf(false) }

        // Currently dragged item id and position
        var draggingItemId by remember { mutableStateOf<String?>(null) }
        var hoverTargetStatus by remember { mutableStateOf<TaskColumnStatus?>(null) }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            // Header bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF8B5CF6), Color(0xFFEC4899))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ViewKanban,
                            contentDescription = "Kanban",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Kanban Task Board",
                            color = viewModel.themeText,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Drag cards across status columns • Hover for glow",
                            color = viewModel.themeTextSecondary,
                            fontSize = 10.sp
                        )
                    }
                }

                IconButton(
                    onClick = { isAddingTask = !isAddingTask },
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (isDark) Color(0x33FFFFFF) else Color(0x1F000000))
                ) {
                    Icon(
                        imageVector = if (isAddingTask) Icons.Default.Close else Icons.Default.Add,
                        contentDescription = "Add Task",
                        tint = viewModel.themeText,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Quick task input expander
            AnimatedVisibility(visible = isAddingTask) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newTaskTitle,
                        onValueChange = { newTaskTitle = it },
                        placeholder = {
                            Text(
                                "Enter new task name...",
                                color = viewModel.themeTextSecondary,
                                fontSize = 12.sp
                            )
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accentColor,
                            unfocusedBorderColor = if (isDark) Color(0x33FFFFFF) else Color(0x22000000),
                            focusedContainerColor = if (isDark) Color(0x22FFFFFF) else Color(0x11000000),
                            unfocusedContainerColor = Color.Transparent,
                            focusedTextColor = viewModel.themeText,
                            unfocusedTextColor = viewModel.themeText
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            if (newTaskTitle.isNotBlank()) {
                                taskList = taskList + DragDropTaskItem(title = newTaskTitle.trim())
                                newTaskTitle = ""
                                isAddingTask = false
                            }
                        }),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (newTaskTitle.isNotBlank()) {
                                taskList = taskList + DragDropTaskItem(title = newTaskTitle.trim())
                                newTaskTitle = ""
                                isAddingTask = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Text("Add", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Column Status Chips Filter
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    val isSelected = activeFilterStatus == null
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (isSelected) accentColor.copy(alpha = 0.25f)
                                else if (isDark) Color(0x22FFFFFF) else Color(0x0F000000)
                            )
                            .border(
                                1.dp,
                                if (isSelected) accentColor else Color.Transparent,
                                RoundedCornerShape(20.dp)
                            )
                            .clickable { activeFilterStatus = null }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "All Tasks (${taskList.size})",
                            color = if (isSelected) accentColor else viewModel.themeTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                items(TaskColumnStatus.values()) { status ->
                    val count = taskList.count { it.status == status }
                    val isSelected = activeFilterStatus == status
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (isSelected) status.color.copy(alpha = 0.25f)
                                else if (isDark) Color(0x22FFFFFF) else Color(0x0F000000)
                            )
                            .border(
                                1.dp,
                                if (isSelected) status.color else Color.Transparent,
                                RoundedCornerShape(20.dp)
                            )
                            .clickable {
                                activeFilterStatus = if (isSelected) null else status
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = status.icon, fontSize = 10.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${status.label} ($count)",
                                color = if (isSelected) status.color else viewModel.themeTextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Kanban Columns Display
            val displayedColumns = if (activeFilterStatus != null) {
                listOf(activeFilterStatus!!)
            } else {
                TaskColumnStatus.values().toList()
            }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 320.dp)
            ) {
                items(displayedColumns) { colStatus ->
                    val colTasks = taskList.filter { it.status == colStatus }
                    val isHoveredTarget = hoverTargetStatus == colStatus

                    ColumnDropZone(
                        status = colStatus,
                        tasks = colTasks,
                        isHoveredTarget = isHoveredTarget,
                        isDark = isDark,
                        viewModel = viewModel,
                        draggingItemId = draggingItemId,
                        onStatusChange = { itemId, newStatus ->
                            taskList = taskList.map { item ->
                                if (item.id == itemId) item.copy(status = newStatus) else item
                            }
                        },
                        onDeleteTask = { itemId ->
                            taskList = taskList.filterNot { it.id == itemId }
                        },
                        onStartDrag = { id -> draggingItemId = id },
                        onEndDrag = { draggingItemId = null },
                        onUpdateHoverTarget = { target -> hoverTargetStatus = target }
                    )
                }
            }
        }
    }
}

@Composable
private fun ColumnDropZone(
    status: TaskColumnStatus,
    tasks: List<DragDropTaskItem>,
    isHoveredTarget: Boolean,
    isDark: Boolean,
    viewModel: FocusViewModel,
    draggingItemId: String?,
    onStatusChange: (String, TaskColumnStatus) -> Unit,
    onDeleteTask: (String) -> Unit,
    onStartDrag: (String) -> Unit,
    onEndDrag: () -> Unit,
    onUpdateHoverTarget: (TaskColumnStatus?) -> Unit
) {
    val dropZoneBorderAlpha by animateFloatAsState(
        targetValue = if (isHoveredTarget) 0.9f else 0.2f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "drop_zone_alpha"
    )

    val columnWidth = if (tasks.isEmpty()) 160.dp else 220.dp

    Box(
        modifier = Modifier
            .width(columnWidth)
            .fillMaxHeight()
            .zIndex(if (tasks.any { it.id == draggingItemId }) 10f else 0f)
            .softGlassShadow(
                elevation = if (isHoveredTarget) 12.dp else 4.dp,
                shape = RoundedCornerShape(18.dp),
                isDark = isDark
            )
            .clip(RoundedCornerShape(18.dp))
            .background(
                if (isDark) {
                    if (isHoveredTarget) status.color.copy(alpha = 0.18f) else Color(0x33141124)
                } else {
                    if (isHoveredTarget) status.color.copy(alpha = 0.12f) else Color(0x66FFFFFF)
                }
            )
            .border(
                width = if (isHoveredTarget) 2.dp else 1.dp,
                color = if (isHoveredTarget) status.color else status.color.copy(alpha = dropZoneBorderAlpha),
                shape = RoundedCornerShape(18.dp)
            )
            .padding(10.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Column Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = status.icon, fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = status.label,
                        color = status.color,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(status.color.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = tasks.size.toString(),
                        color = status.color,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Divider(
                color = if (isDark) Color(0x1FFFFFFF) else Color(0x10000000),
                thickness = 1.dp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (tasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .drawBehind {
                            drawRoundRect(
                                color = status.color.copy(alpha = 0.4f),
                                style = androidx.compose.ui.graphics.drawscope.Stroke(
                                    width = 2.dp.toPx(),
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)
                                ),
                                cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx())
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Drop tasks here",
                        color = viewModel.themeTextSecondary,
                        fontSize = 11.sp
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(tasks, key = { it.id }) { item ->
                        GlassmorphicHoverTaskCard(
                            item = item,
                            isDark = isDark,
                            viewModel = viewModel,
                            onStatusChange = onStatusChange,
                            onDeleteTask = onDeleteTask,
                            onStartDrag = onStartDrag,
                            onEndDrag = onEndDrag,
                            onUpdateHoverTarget = onUpdateHoverTarget
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GlassmorphicHoverTaskCard(
    item: DragDropTaskItem,
    isDark: Boolean,
    viewModel: FocusViewModel,
    onStatusChange: (String, TaskColumnStatus) -> Unit,
    onDeleteTask: (String) -> Unit,
    onStartDrag: (String) -> Unit,
    onEndDrag: () -> Unit,
    onUpdateHoverTarget: (TaskColumnStatus?) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    var isHovered by remember { mutableStateOf(false) }
    var dragOffsetX by remember { mutableFloatStateOf(0f) }
    var dragOffsetY by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }

    // Hover / Drag Glow animations
    val glowAlpha by animateFloatAsState(
        targetValue = if (isDragging) 0.95f else if (isHovered || isPressed) 0.65f else 0.15f,
        animationSpec = tween(durationMillis = 200),
        label = "hover_glow_alpha"
    )

    val scale by animateFloatAsState(
        targetValue = if (isDragging) 1.05f else if (isHovered || isPressed) 1.02f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "card_scale"
    )

    val priorityColor = when (item.priority) {
        "High" -> Color(0xFFEF4444)
        "Medium" -> Color(0xFFF59E0B)
        else -> Color(0xFF10B981)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .zIndex(if (isDragging) 100f else 0f)
            .offset { IntOffset(dragOffsetX.roundToInt(), dragOffsetY.roundToInt()) }
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                shadowElevation = if (isDragging) 16f else if (isHovered) 8f else 2f
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        isDragging = true
                        onStartDrag(item.id)
                    },
                    onDragEnd = {
                        isDragging = false
                        onEndDrag()

                        // Determine drop column target based on horizontal drag offset
                        if (dragOffsetX > 120f) {
                            val nextStatus = when (item.status) {
                                TaskColumnStatus.TODO -> TaskColumnStatus.IN_PROGRESS
                                TaskColumnStatus.IN_PROGRESS -> TaskColumnStatus.DONE
                                TaskColumnStatus.DONE -> TaskColumnStatus.DONE
                            }
                            onStatusChange(item.id, nextStatus)
                        } else if (dragOffsetX < -120f) {
                            val prevStatus = when (item.status) {
                                TaskColumnStatus.DONE -> TaskColumnStatus.IN_PROGRESS
                                TaskColumnStatus.IN_PROGRESS -> TaskColumnStatus.TODO
                                TaskColumnStatus.TODO -> TaskColumnStatus.TODO
                            }
                            onStatusChange(item.id, prevStatus)
                        }

                        dragOffsetX = 0f
                        dragOffsetY = 0f
                        onUpdateHoverTarget(null)
                    },
                    onDragCancel = {
                        isDragging = false
                        onEndDrag()
                        dragOffsetX = 0f
                        dragOffsetY = 0f
                        onUpdateHoverTarget(null)
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        dragOffsetX += dragAmount.x
                        dragOffsetY += dragAmount.y

                        // Update hover target indicator column
                        if (dragOffsetX > 100f) {
                            onUpdateHoverTarget(
                                when (item.status) {
                                    TaskColumnStatus.TODO -> TaskColumnStatus.IN_PROGRESS
                                    else -> TaskColumnStatus.DONE
                                }
                            )
                        } else if (dragOffsetX < -100f) {
                            onUpdateHoverTarget(
                                when (item.status) {
                                    TaskColumnStatus.DONE -> TaskColumnStatus.IN_PROGRESS
                                    else -> TaskColumnStatus.TODO
                                }
                            )
                        } else {
                            onUpdateHoverTarget(null)
                        }
                    }
                )
            }
            .drawBehind {
                // Radiant subtle hover glow effect
                if (glowAlpha > 0.05f) {
                    drawRoundRect(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                item.status.color.copy(alpha = glowAlpha * 0.4f),
                                Color.Transparent
                            ),
                            center = Offset(size.width / 2, size.height / 2),
                            radius = size.width * 0.8f
                        ),
                        cornerRadius = CornerRadius(14.dp.toPx(), 14.dp.toPx())
                    )
                }
            }
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (isDark) {
                    Color(0x551E1B2E)
                } else {
                    Color(0xCCFFFFFF)
                }
            )
            .border(
                width = if (isHovered || isDragging) 1.5.dp else 1.dp,
                color = if (isDragging) item.status.color else item.status.color.copy(alpha = glowAlpha),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(interactionSource = interactionSource, indication = null) {
                isHovered = !isHovered
            }
            .padding(10.dp)
    ) {
        Column {
            // Top Row: Drag Handle + Priority Badge + Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DragHandle,
                        contentDescription = "Drag",
                        tint = viewModel.themeTextSecondary.copy(alpha = 0.6f),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(priorityColor.copy(alpha = 0.18f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = item.priority,
                            color = priorityColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                IconButton(
                    onClick = { onDeleteTask(item.id) },
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Delete",
                        tint = viewModel.themeTextSecondary.copy(alpha = 0.6f),
                        modifier = Modifier.size(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Task Title
            Text(
                text = item.title,
                color = viewModel.themeText,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Footer row: status selector buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.timeLabel,
                    color = viewModel.themeTextSecondary,
                    fontSize = 9.sp
                )

                // Quick move buttons
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TaskColumnStatus.values().forEach { st ->
                        if (st != item.status) {
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(st.color.copy(alpha = 0.15f))
                                    .clickable { onStatusChange(item.id, st) }
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = st.icon,
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
