package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FocusViewModel
import com.example.data.Habit
import com.example.ui.components.*

@Composable
fun HabitsScreen(viewModel: FocusViewModel) {
    val habitsList by viewModel.habits.collectAsState()
    val completionsList by viewModel.completions.collectAsState()
    val accentColor = Color(android.graphics.Color.parseColor(viewModel.accentColorHex))
    val context = LocalContext.current
    val todayDate = viewModel.getTodayDateString()

    var showAddDialog by remember { mutableStateOf(false) }
    var habitToDelete by remember { mutableStateOf<Habit?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(22.dp))
            .background(viewModel.themeGlassFill)
            .border(1.dp, viewModel.themeGlassBorder, RoundedCornerShape(22.dp))
            .padding(bottom = 100.dp) // Leave spacing for navigation bar
    ) {
        ViewModelConfettiOverlay(viewModel)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Title section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Habit Flows",
                        color = viewModel.themeText,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "Build visual compounding stacks",
                        color = viewModel.themeTextSecondary,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // LazyColumn list of habits
            if (habitsList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No habits created yet.",
                            color = viewModel.themeTextSecondary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tap the [+] button to engineer a new stack!",
                            color = viewModel.themeTextSecondary.copy(0.7f),
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(habitsList) { habit ->
                        val currentProgress = completionsList.find { it.habitId == habit.id && it.date == todayDate }?.value ?: 0
                        val isCompleted = currentProgress >= habit.targetValue
                        val habitColor = Color(android.graphics.Color.parseColor(habit.colorHex))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
            .padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Left Side: Habit Details
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Left accent border bar color
                                Box(
                                    modifier = Modifier
                                        .width(4.dp)
                                        .height(if (habit.targetValue > 1) 64.dp else 44.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(habitColor)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = habit.name,
                                            color = viewModel.themeText,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = habit.category,
                                            color = viewModel.themeTextSecondary,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "•  ${habit.streakCount} day streak 🔥",
                                            color = accentColor,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    
                                    if (habit.targetValue > 1) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.fillMaxWidth().padding(end = 12.dp)
    ) {
                                            Text(
                                                text = "$currentProgress / ${habit.targetValue} ${habit.unit}",
                                                color = viewModel.themeText,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            LinearProgressIndicator(
                                                progress = { (currentProgress.toFloat() / habit.targetValue.toFloat()).coerceIn(0f, 1f) },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(6.dp)
                                                    .clip(RoundedCornerShape(3.dp)),
                                                color = habitColor,
                                                trackColor = viewModel.themeGlassBorder
                                            )
                                        }
                                    }
                                }
                            }

                            // Right Side: Checklist box + Delete Icon
                            Row(verticalAlignment = Alignment.CenterVertically) {

                                if (habit.streakCount > 0) {
                                    Icon(
                                        imageVector = androidx.compose.material.icons.Icons.Default.Share,
                                        contentDescription = "Share",
                                        tint = accentColor,
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clickable {
                                                val shareIntent = Intent().apply {
                                                    action = Intent.ACTION_SEND
                                                    putExtra(Intent.EXTRA_TEXT, "I'm on a ${habit.streakCount}-day streak for my habit '${habit.name}'! Building better habits with FocusCube! 🔥 #FocusCube #HabitTracker")
                                                    type = "text/plain"
                                                }
                                                context.startActivity(Intent.createChooser(shareIntent, "Share Habit Streak"))
                                            }
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                }
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = viewModel.themeTextSecondary.copy(0.7f),
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clickable { habitToDelete = habit }
                                )
                                Spacer(modifier = Modifier.width(16.dp))

                                if (habit.targetValue > 1) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        // Minus Button
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(viewModel.themeGlassFill)
                                                .border(1.dp, viewModel.themeGlassBorder, RoundedCornerShape(8.dp))
                                                .clickable {
                                                    viewModel.updateHabitProgress(habit, todayDate, currentProgress - 1)
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("-", color = viewModel.themeText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                        }

                                        // Plus Button
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(habitColor.copy(alpha = 0.2f))
                                                .border(1.dp, habitColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                                .clickable {
                                                    viewModel.updateHabitProgress(habit, todayDate, currentProgress + 1)
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("+", color = habitColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                } else {
                                    // Isometric Styled Checkbox
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (isCompleted) habitColor else viewModel.themeGlassFill)
                                            .border(
                                                width = 1.5.dp,
                                                color = if (isCompleted) habitColor else viewModel.themeGlassBorder,
                                                shape = RoundedCornerShape(10.dp)
                                            )
                                            .clickable {
                                                viewModel.toggleHabitCompletion(habit, todayDate)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isCompleted) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Completed",
                                                tint = if (viewModel.isDarkMode) Color.Black else Color.White,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = { showAddDialog = true },
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
                contentDescription = "Add Habit",
                modifier = Modifier.size(28.dp)
            )
        }
    }

    // Add Habit Bottom Sheet Dialog
    if (showAddDialog) {
        var habitName by remember { mutableStateOf("") }
        var category by remember { mutableStateOf("Work") }
        var selectedColorHex by remember { mutableStateOf(viewModel.accentColorHex) }
        var obsidianUri by remember { mutableStateOf("") }
        var targetValueStr by remember { mutableStateOf("1") }
        var unit by remember { mutableStateOf("times") }
 
        val categories = listOf("Work", "Health", "Learn", "Life")
        val colorOptions = listOf("#5adcb3", "#fcb2c5", "#f7e299", "#C9B8FF", "#FFD0B5")
 
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            containerColor = viewModel.themeSurface,
            title = {
                Text(
                    text = "Engineer Habit Node",
                    color = viewModel.themeText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Habit Name Input
                    OutlinedTextField(
                        value = habitName,
                        onValueChange = { habitName = it },
                        label = { Text("Habit Title", color = viewModel.themeTextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accentColor,
                            unfocusedBorderColor = viewModel.themeGlassBorder,
                            focusedTextColor = viewModel.themeText,
                            unfocusedTextColor = viewModel.themeText
                        ),
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                        modifier = Modifier.fillMaxWidth()
                    )
 
                    // Category Chooser Chips
                    Column {
                        Text(text = "Category", color = viewModel.themeText, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            categories.forEach { cat ->
                                val isSelected = category == cat
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) accentColor else viewModel.themeGlassFill)
                                        .clickable { category = cat }
                                        .padding(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = cat,
                                        color = if (isSelected) (if (viewModel.isDarkMode) Color.Black else Color.White) else viewModel.themeText,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // Numerical Goal Section
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = targetValueStr,
                            onValueChange = { targetValueStr = it.filter { char -> char.isDigit() } },
                            label = { Text("Goal Target", color = viewModel.themeTextSecondary) },
                            placeholder = { Text("e.g. 8", color = viewModel.themeTextSecondary.copy(alpha = 0.5f)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = accentColor,
                                unfocusedBorderColor = viewModel.themeGlassBorder,
                                focusedTextColor = viewModel.themeText,
                                unfocusedTextColor = viewModel.themeText
                            ),
                            keyboardOptions = KeyboardOptions.Default.copy(
                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = unit,
                            onValueChange = { unit = it },
                            label = { Text("Unit", color = viewModel.themeTextSecondary) },
                            placeholder = { Text("e.g. glasses", color = viewModel.themeTextSecondary.copy(alpha = 0.5f)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = accentColor,
                                unfocusedBorderColor = viewModel.themeGlassBorder,
                                focusedTextColor = viewModel.themeText,
                                unfocusedTextColor = viewModel.themeText
                            ),
                            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                            modifier = Modifier.weight(1f)
                        )
                    }
 
                    // Accent Color Picker Dots
                    Column {
                        Text(text = "Accent Color", color = viewModel.themeText, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            colorOptions.forEach { colorStr ->
                                val dotColor = Color(android.graphics.Color.parseColor(colorStr))
                                val isSelected = selectedColorHex == colorStr
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(dotColor)
                                        .border(
                                            width = if (isSelected) 2.5.dp else 0.dp,
                                            color = if (isSelected) viewModel.themeText else Color.Transparent,
                                            shape = CircleShape
                                        )
                                        .clickable { selectedColorHex = colorStr }
                                )
                            }
                        }
                    }
 
                    Spacer(modifier = Modifier.height(4.dp))
                }
            },
            confirmButton = {
                com.example.ui.components.GlassButton(
                    onClick = {
                        if (habitName.isNotEmpty()) {
                            val target = targetValueStr.toIntOrNull() ?: 1
                            viewModel.addHabit(
                                habitName,
                                category,
                                selectedColorHex,
                                obsidianUri,
                                target,
                                unit.ifBlank { "times" }
                            )
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor, contentColor = if (viewModel.isDarkMode) Color.Black else Color.White)
                ) {
                    Text(text = "Create Node", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                com.example.ui.components.GlassButton(variant = com.example.ui.components.GlassButtonVariant.Secondary, onClick = { showAddDialog = false }) {
                    Text(text = "Cancel", color = viewModel.themeTextSecondary)
                }
            }
        )
    }

    if (habitToDelete != null) {
        AlertDialog(
            onDismissRequest = { habitToDelete = null },
            title = {
                Text(
                    text = "Delete Habit?",
                    color = viewModel.themeText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete '${habitToDelete?.name}'? This will permanently erase this habit and all its completions log history.",
                    color = viewModel.themeTextSecondary,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                com.example.ui.components.GlassButton(
                    onClick = {
                        habitToDelete?.let { viewModel.deleteHabit(it) }
                        habitToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentPink, contentColor = if (viewModel.isDarkMode) Color.Black else Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                com.example.ui.components.GlassButton(variant = com.example.ui.components.GlassButtonVariant.Secondary, onClick = { habitToDelete = null }) {
                    Text("Cancel", color = viewModel.themeTextSecondary, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = viewModel.themeGlassFill,
            modifier = Modifier.border(1.dp, viewModel.themeGlassBorder, RoundedCornerShape(28.dp))
        )
    }
}
