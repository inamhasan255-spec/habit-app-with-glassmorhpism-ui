package com.focuscube.ui.dashboard

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.focuscube.data.model.TodoTask

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InteractiveTodoList(
    todos: List<TodoTask>,
    onAddTodo: (String) -> Unit,
    onToggle: (TodoTask) -> Unit,
    onDelete: (Int) -> Unit,
    onReorder: (Int, Int) -> Unit
) {
    var newTaskTitle by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    var draggedIndex by remember { mutableStateOf<Int?>(null) }
    var draggedOffset by remember { mutableStateOf(0f) }
    val view = LocalView.current

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Interactive Task List",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Add New Task Input
        OutlinedTextField(
            value = newTaskTitle,
            onValueChange = { newTaskTitle = it },
            placeholder = { Text("Add a new task...") },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                com.example.ui.components.GlassIconButton(
                    onClick = {
                        if (newTaskTitle.isNotBlank()) {
                            onAddTodo(newTaskTitle)
                            newTaskTitle = ""
                        }
                    }
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add new task")
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        if (todos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No tasks added. You're all caught up!", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
                    .pointerInput(todos) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = { offset ->
                                listState.layoutInfo.visibleItemsInfo
                                    .firstOrNull { offset.y.toInt() in it.offset..(it.offset + it.size) }
                                    ?.also {
                                        draggedIndex = it.index
                                        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                                    }
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                draggedOffset += dragAmount.y
                                val currentDraggedIndex = draggedIndex ?: return@detectDragGesturesAfterLongPress
                                
                                val itemSize = listState.layoutInfo.visibleItemsInfo.firstOrNull()?.size ?: return@detectDragGesturesAfterLongPress
                                
                                val targetIndex = (currentDraggedIndex + (draggedOffset / itemSize).toInt())
                                    .coerceIn(0, todos.size - 1)
                                
                                if (targetIndex != currentDraggedIndex) {
                                    onReorder(currentDraggedIndex, targetIndex)
                                    draggedIndex = targetIndex
                                    draggedOffset = 0f
                                }
                            },
                            onDragEnd = {
                                draggedIndex = null
                                draggedOffset = 0f
                            },
                            onDragCancel = {
                                draggedIndex = null
                                draggedOffset = 0f
                            }
                        )
                    },
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(todos, key = { _, todo -> todo.id }) { index, todo ->
                    val isDragged = index == draggedIndex
                    val elevation by animateDpAsState(if (isDragged) 8.dp else 1.dp)
                    val alpha by animateFloatAsState(if (isDragged) 0.8f else 1f)

                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .zIndex(if (isDragged) 1f else 0f)
                            .graphicsLayer {
                                this.alpha = alpha
                                this.translationY = if (isDragged) draggedOffset else 0f
                            },
                        shape = RoundedCornerShape(12.dp),
                        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(elevation).copy(alpha = 0.3f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            com.example.ui.components.GlassIconButton(onClick = {}) {
                                Icon(
                                    imageVector = Icons.Default.DragHandle,
                                    contentDescription = "Reorder task '${todo.title}'",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            com.example.ui.components.GlassIconButton(
                                onClick = {
                                    view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                                    onToggle(todo)
                                }
                            ) {
                                Icon(
                                    imageVector = if (todo.isCompleted) Icons.Default.CheckCircle else Icons.Outlined.Circle,
                                    contentDescription = if (todo.isCompleted) "Mark task '${todo.title}' as incomplete" else "Mark task '${todo.title}' as complete",
                                    tint = if (todo.isCompleted) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Text(
                                text = todo.title,
                                modifier = Modifier.weight(1f),
                                fontSize = 16.sp,
                                color = if (todo.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                                textDecoration = if (todo.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                            )
                            
                            com.example.ui.components.GlassIconButton(onClick = { onDelete(todo.id) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete task '${todo.title}'",
                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
