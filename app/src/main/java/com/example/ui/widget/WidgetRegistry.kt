package com.example.ui.widget

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

data class WidgetInfo(
    val id: String,
    val title: String,
    val description: String,
    val category: String, // "STUDENT", "ADULT", "UNIVERSAL"
    val iconName: String // string descriptor for easy icon mapping
)

object WidgetRegistry {
    val widgets = listOf(
        // UNIVERSAL
        WidgetInfo("cube_voxels", "Voxel Focus Cube", "Interactive 3D voxel-particle focus cube", "UNIVERSAL", "Cube"),
        WidgetInfo("xp_gamification", "XP & Level Engine", "Track your level, XP, and unlock achievements", "UNIVERSAL", "Star"),
        WidgetInfo("timer_trigger", "Quick Session Timer", "Launch a Focus Session with one tap", "UNIVERSAL", "PlayArrow"),
        WidgetInfo("ai_assistant_chat", "Gemini Assistant", "Chat with Gemini", "UNIVERSAL", "Face"),
        WidgetInfo("stats_overview", "Focus Stats Summary", "Daily focus hours and streak counter", "UNIVERSAL", "BarChart"),
        WidgetInfo("recent_sessions", "Recent Sessions", "View logs of your completed focus blocks", "UNIVERSAL", "History"),
        WidgetInfo("quick_habits", "Daily Habit Loops", "Mark your daily positive habits as completed", "UNIVERSAL", "CheckCircle"),
        WidgetInfo("daily_inspiration", "Aesthetic Quotes", "Zen productivity quotes to keep you inspired", "UNIVERSAL", "FormatQuote"),
        WidgetInfo("focus_soundscape", "Ambient Sound Pack", "Toggle and customize minimalist audio packs", "UNIVERSAL", "VolumeUp"),
        WidgetInfo("time_logger", "Manual Session Logger", "Manually log offline study or work blocks", "UNIVERSAL", "Edit"),
        WidgetInfo("screen_time", "Digital Well-being Tracker", "Track screen time and app launches", "UNIVERSAL", "Timer"),
        WidgetInfo("achievement_vault", "Achievements Vault", "Browse unlocked titles and badges", "UNIVERSAL", "EmojiEvents"),
        WidgetInfo("breath_coach", "Pranayama Breath Guide", "Paced deep breathing exercises for focus", "UNIVERSAL", "Air"),
        WidgetInfo("zen_mode_toggle", "Extreme Monotasking", "Disable notifications and auto-engage silent mode", "UNIVERSAL", "DoNotDisturb"),
        
        // STUDENT
        WidgetInfo("timetable_schedule", "Timetable Schedule", "College class timetable and week rotations", "STUDENT", "DateRange"),
        WidgetInfo("subject_notes", "Interactive Lecture Notes", "Quick lecture notes and study cards", "STUDENT", "StickyNote2"),
        WidgetInfo("academic_tasks", "Academic Tasks Tracker", "Homework, exam preparation and grades tracking", "STUDENT", "Assignment"),
        WidgetInfo("white_noise", "White Noise Generator", "Play Brown/White/Pink noise to block dorm noise", "STUDENT", "GraphicEq"),
        WidgetInfo("grade_tracker", "GPA & Grade Calculator", "Estimate semester performance and exam weightage", "STUDENT", "Calculate"),
        WidgetInfo("group_study", "Study Group Planner", "Coordinate assignments and shared project deadlines", "STUDENT", "Groups"),
        WidgetInfo("flashcard_booster", "Active Recall Flashcards", "Spaced-repetition flashcard deck manager", "STUDENT", "Style"),
        WidgetInfo("exam_countdown", "Exam Countdowns", "Track exact days left for major midterms/finals", "STUDENT", "Alarm"),
        WidgetInfo("dorm_routine", "Dorm Habit Builder", "Build morning routines and study rituals", "STUDENT", "WbSunny"),
        WidgetInfo("professor_office", "Professor Directory", "Office hours details and lecture halls", "STUDENT", "ContactPage"),
        WidgetInfo("academic_chatbot", "Gemini Academic AI", "Ask homework questions to Gemini", "STUDENT", "AutoAwesome"),
        WidgetInfo("syllabus_analyzer", "Syllabus Weight Scanner", "Upload and list grade breakdowns from syllabi", "STUDENT", "CloudUpload"),
        WidgetInfo("scholar_feed", "ArXiv Research Feed", "Browse top academic papers in your major", "STUDENT", "MenuBook"),

        // ADULT
        WidgetInfo("priority_matrix", "Eisenhower Matrix", "Categorize tasks by Urgency and Importance", "ADULT", "GridView"),
        WidgetInfo("productivity_trends", "Focus Trend Charts", "D3-powered monthly productivity visualizations", "ADULT", "TrendingUp"),
        WidgetInfo("obsidian_sync", "Obsidian Habit Sync", "Sync habit completion logs with Obsidian vault", "ADULT", "Sync"),
        WidgetInfo("app_whitelisting", "Focus Shield Whitelist", "Select essential apps that bypass focus blockers", "ADULT", "Security"),
        WidgetInfo("client_billing", "Client Billable Timer", "Track and export focus time directly for clients", "ADULT", "AttachMoney"),
        WidgetInfo("project_timeline", "Gantt Milestones", "Track long-term career goals and epic milestones", "ADULT", "Timeline"),
        WidgetInfo("burnout_sensor", "Burnout Check-In", "Track mood, fatigue, and get wellness warnings", "ADULT", "Favorite"),
        WidgetInfo("financial_focus", "Monthly Budget Guard", "Track spending against productivity rewards", "ADULT", "AccountBalanceWallet"),
        WidgetInfo("meeting_blocker", "Meeting Focus Guard", "Auto-reject calls and messages during focus hours", "ADULT", "CallEnd"),
        WidgetInfo("weekly_review", "Weekly Retrospective", "Reflect on accomplishments and next week's focus", "ADULT", "FactCheck"),
        WidgetInfo("task_delegation", "Delegation Matrix", "Track tasks delegated to teammates or assistants", "ADULT", "Send"),
        WidgetInfo("coffee_tracker", "Caffeine Intake log", "Log coffee cups and trace caffeine decay curve", "ADULT", "LocalCafe"),
        WidgetInfo("inbox_zero", "Inbox Zero Assistant", "Quick email batch-reply reminder timers", "ADULT", "Email"),
        WidgetInfo("drag_drop_tasks", "Drag & Drop Task Matrix", "Interactive glassmorphic Kanban task board with hover glow", "UNIVERSAL", "TouchApp"),
        WidgetInfo("weather_focus", "Focus Weather Zone", "Syncs ambient focus sounds & theme to your local weather", "UNIVERSAL", "WbSunny"),
        WidgetInfo("image_analyzer", "Analyze images", "Analyze images using Gemini 3.1 Pro", "UNIVERSAL", "DocumentScanner")
    )

    val specs: Map<String, WidgetSpec> = mapOf(
        "drag_drop_tasks" to DragDropTaskWidgetSpec,
        "image_analyzer" to ImageAnalyzerWidget(),
        "cube_voxels" to CubeVoxelsWidgetSpec,
        "xp_gamification" to XpGamificationWidgetSpec,
        "timer_trigger" to TimerTriggerWidgetSpec,
        "ai_assistant_chat" to AiAssistantChatWidgetSpec,
        "stats_overview" to StatsOverviewWidgetSpec,
        "recent_sessions" to RecentSessionsWidgetSpec,
        "quick_habits" to QuickHabitsWidgetSpec,
        "timetable_schedule" to TimetableScheduleWidgetSpec,
        "subject_notes" to SubjectNotesWidgetSpec,
        "academic_tasks" to AcademicTasksWidgetSpec,
        "white_noise" to WhiteNoiseWidgetSpec,
        "priority_matrix" to PriorityMatrixWidgetSpec,
        "productivity_trends" to ProductivityTrendsWidgetSpec,
        "obsidian_sync" to ObsidianSyncWidgetSpec,
        "app_whitelisting" to AppWhitelistingWidgetSpec,
        "weather_focus" to WeatherFocusWidgetSpec
    )

    fun getWidgetsForRole(role: String): List<String> {
        val base = widgets.filter { it.category == "UNIVERSAL" }.map { it.id }
        val specific = widgets.filter { it.category == role.uppercase() }.map { it.id }
        return base + specific
    }
}

// 1. Voxel Focus Cube
object CubeVoxelsWidgetSpec : WidgetSpec {
    override val id = "cube_voxels"
    override val title = "Voxel Focus Cube"
    override val description = "Interactive 3D voxel-particle focus cube"
    override val category = "UNIVERSAL"
    override val iconName = "Cube"

    @Composable
    override fun Render(viewModel: FocusViewModel, accentColor: Color, scope: CoroutineScope, selectedDateStr: String) {
        var particleCount by remember { mutableStateOf(128) }
        Column {
            Text(
                text = "Cube State: ACTIVE | Rotation: SMOOTH",
                color = viewModel.themeTextSecondary,
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Particles: $particleCount", color = viewModel.themeText, fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    com.example.ui.components.GlassButton(
                        onClick = { if (particleCount > 32) particleCount -= 32 },
                        colors = ButtonDefaults.buttonColors(containerColor = viewModel.themeSurface),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text("-32", fontSize = 10.sp, color = viewModel.themeText)
                    }
                    com.example.ui.components.GlassButton(
                        onClick = { if (particleCount < 512) particleCount += 32 },
                        colors = ButtonDefaults.buttonColors(containerColor = viewModel.themeSurface),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text("+32", fontSize = 10.sp, color = viewModel.themeText)
                    }
                }
            }
        }
    }
}

// 2. XP & Level Engine
object XpGamificationWidgetSpec : WidgetSpec {
    override val id = "xp_gamification"
    override val title = "XP & Level Engine"
    override val description = "Track your level, XP, and unlock achievements"
    override val category = "UNIVERSAL"
    override val iconName = "Star"

    @Composable
    override fun Render(viewModel: FocusViewModel, accentColor: Color, scope: CoroutineScope, selectedDateStr: String) {
        val completedCount = viewModel.completions.collectAsState(initial = emptyList()).value.size
        val level = (completedCount / 3) + 1
        val currentXp = (completedCount % 3) * 10
        val progress = currentXp.toFloat() / 30f

        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "LVL $level Focus Master", color = viewModel.themeText, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(text = "$currentXp / 30 XP", color = accentColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = accentColor,
                trackColor = viewModel.themeTextSecondary.copy(alpha = 0.2f)
            )
        }
    }
}

// 3. Quick Session Timer
object TimerTriggerWidgetSpec : WidgetSpec {
    override val id = "timer_trigger"
    override val title = "Quick Session Timer"
    override val description = "Launch a Focus Session with one tap"
    override val category = "UNIVERSAL"
    override val iconName = "PlayArrow"

    @Composable
    override fun Render(viewModel: FocusViewModel, accentColor: Color, scope: CoroutineScope, selectedDateStr: String) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            com.example.ui.components.GlassButton(
                onClick = { viewModel.startFocusSession("Pomodoro", 25, "Pomodoro") },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = accentColor)
            ) {
                Text("Pomodoro (25m)", fontSize = 11.sp, color = viewModel.themeBg, fontWeight = FontWeight.Bold)
            }
            com.example.ui.components.GlassButton(
                onClick = { viewModel.startFocusSession("Deep Work", 45, "Deep Work") },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = viewModel.themeSurface)
            ) {
                Text("Deep Work (45m)", fontSize = 11.sp, color = viewModel.themeText, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// 4. Dual AI Coaches
object AiAssistantChatWidgetSpec : WidgetSpec {
    override val id = "ai_assistant_chat"
    override val title = "Gemini Assistant"
    override val description = "Chat with Gemini"
    override val category = "UNIVERSAL"
    override val iconName = "Face"

    @Composable
    override fun Render(viewModel: FocusViewModel, accentColor: Color, scope: CoroutineScope, selectedDateStr: String) {
        Column {
            Text(text = "Unlock mindfulness advice or study assistance now:", color = viewModel.themeTextSecondary, fontSize = 11.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                com.example.ui.components.GlassButton(
                    onClick = { 
                        viewModel.setTab(AppTab.ACADEMIC)
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = viewModel.themeSurface)
                ) {
                    Text("Cube Coach", fontSize = 11.sp, color = viewModel.themeText)
                }
                com.example.ui.components.GlassButton(
                    onClick = { 
                        viewModel.setTab(AppTab.ACADEMIC)
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = viewModel.themeSurface)
                ) {
                    Text("Academic AI", fontSize = 11.sp, color = viewModel.themeText)
                }
            }
        }
    }
}

// 5. Focus Stats Summary
object StatsOverviewWidgetSpec : WidgetSpec {
    override val id = "stats_overview"
    override val title = "Focus Stats Summary"
    override val description = "Daily focus hours and streak counter"
    override val category = "UNIVERSAL"
    override val iconName = "BarChart"

    @Composable
    override fun Render(viewModel: FocusViewModel, accentColor: Color, scope: CoroutineScope, selectedDateStr: String) {
        val completedCount = viewModel.completions.collectAsState(initial = emptyList()).value.size
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Completed Habits", color = viewModel.themeTextSecondary, fontSize = 10.sp)
                Text("$completedCount", color = viewModel.themeText, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            Column {
                Text("Focus Time", color = viewModel.themeTextSecondary, fontSize = 10.sp)
                Text(String.format("%.1f Hrs", viewModel.getFocusTimeHours()), color = accentColor, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
            }
            Column {
                Text("Active Streak", color = viewModel.themeTextSecondary, fontSize = 10.sp)
                Text("${viewModel.getStreakCount()} Days", color = viewModel.themeText, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
            }
        }
    }
}

// 6. Recent Sessions
object RecentSessionsWidgetSpec : WidgetSpec {
    override val id = "recent_sessions"
    override val title = "Recent Sessions"
    override val description = "View logs of your completed focus blocks"
    override val category = "UNIVERSAL"
    override val iconName = "History"

    @Composable
    override fun Render(viewModel: FocusViewModel, accentColor: Color, scope: CoroutineScope, selectedDateStr: String) {
        val sessions = viewModel.allFocusSessions.collectAsState(initial = emptyList()).value
        if (sessions.isEmpty()) {
            Text("No recent focus sessions recorded.", color = viewModel.themeTextSecondary, fontSize = 11.sp)
        } else {
            Column {
                sessions.take(3).forEach { session ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(session.label, color = viewModel.themeText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("${session.actualMinutes}m (${session.mode})", color = accentColor, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

// 7. Daily Habit Loops
object QuickHabitsWidgetSpec : WidgetSpec {
    override val id = "quick_habits"
    override val title = "Daily Habit Loops"
    override val description = "Mark your daily positive habits as completed"
    override val category = "UNIVERSAL"
    override val iconName = "CheckCircle"

    @Composable
    override fun Render(viewModel: FocusViewModel, accentColor: Color, scope: CoroutineScope, selectedDateStr: String) {
        val habitsList = viewModel.habits.collectAsState(initial = emptyList()).value
        val errorHandler = LocalWidgetErrorHandler.current
        if (habitsList.isEmpty()) {
            Text("No habits configured.", color = viewModel.themeTextSecondary, fontSize = 11.sp)
        } else {
            Column {
                habitsList.take(3).forEach { habit ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(habit.name, color = viewModel.themeText, fontSize = 12.sp)
                        Checkbox(
                            checked = habit.lastCompletedDate == selectedDateStr,
                            onCheckedChange = { _ ->
                                scope.launch {
                                    try {
                                        viewModel.toggleHabitCompletion(habit, selectedDateStr)
                                    } catch (e: Exception) {
                                        errorHandler(e)
                                    }
                                }
                            },
                            colors = CheckboxDefaults.colors(checkedColor = accentColor)
                        )
                    }
                }
            }
        }
    }
}

// 8. Timetable Schedule (Student SPEC)
object TimetableScheduleWidgetSpec : WidgetSpec {
    override val id = "timetable_schedule"
    override val title = "Timetable Schedule"
    override val description = "College class timetable and week rotations"
    override val category = "STUDENT"
    override val iconName = "DateRange"

    @Composable
    override fun Render(viewModel: FocusViewModel, accentColor: Color, scope: CoroutineScope, selectedDateStr: String) {
        val entries = viewModel.timetableEntries.collectAsState(initial = emptyList()).value
        if (entries.isEmpty()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text("No classes scheduled for today.", color = viewModel.themeTextSecondary, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(8.dp))
                com.example.ui.components.GlassButton(
                    onClick = { viewModel.setTab(AppTab.STATS) },
                    colors = ButtonDefaults.buttonColors(containerColor = viewModel.themeSurface)
                ) {
                    Text("Add College Class", fontSize = 10.sp, color = viewModel.themeText)
                }
            }
        } else {
            Column {
                entries.take(2).forEach { entry ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(entry.subjectName, color = viewModel.themeText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("${entry.startTime} - ${entry.endTime} (${entry.room})", color = viewModel.themeTextSecondary, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

// 9. Interactive Lecture Notes (Student SPEC)
object SubjectNotesWidgetSpec : WidgetSpec {
    override val id = "subject_notes"
    override val title = "Interactive Lecture Notes"
    override val description = "Quick lecture notes and study cards"
    override val category = "STUDENT"
    override val iconName = "StickyNote2"

    @Composable
    override fun Render(viewModel: FocusViewModel, accentColor: Color, scope: CoroutineScope, selectedDateStr: String) {
        var noteText by remember { mutableStateOf("") }
        Column {
            OutlinedTextField(
                value = noteText,
                onValueChange = { noteText = it },
                placeholder = { Text("Capture quick lecture notes...", fontSize = 11.sp) },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 2,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accentColor,
                    unfocusedBorderColor = viewModel.themeGlassBorder,
                    focusedTextColor = viewModel.themeText,
                    unfocusedTextColor = viewModel.themeText
                )
            )
            Spacer(modifier = Modifier.height(6.dp))
            com.example.ui.components.GlassButton(
                onClick = {
                    if (noteText.isNotBlank()) {
                        noteText = ""
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Capture Note", fontSize = 10.sp, color = viewModel.themeBg, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// 10. Academic Tasks Tracker (Student SPEC)
object AcademicTasksWidgetSpec : WidgetSpec {
    override val id = "academic_tasks"
    override val title = "Academic Tasks Tracker"
    override val description = "Homework, exam preparation and grades tracking"
    override val category = "STUDENT"
    override val iconName = "Assignment"

    @Composable
    override fun Render(viewModel: FocusViewModel, accentColor: Color, scope: CoroutineScope, selectedDateStr: String) {
        val tasks = viewModel.academicTasks.collectAsState(initial = emptyList()).value
        val errorHandler = LocalWidgetErrorHandler.current
        if (tasks.isEmpty()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text("No academic tasks found.", color = viewModel.themeTextSecondary, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(8.dp))
                com.example.ui.components.GlassButton(
                    onClick = { viewModel.setTab(AppTab.STATS) },
                    colors = ButtonDefaults.buttonColors(containerColor = viewModel.themeSurface)
                ) {
                    Text("Create Task", fontSize = 10.sp, color = viewModel.themeText)
                }
            }
        } else {
            Column {
                tasks.take(3).forEach { task ->
                    val isCompleted = task.status == "Completed"
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isCompleted,
                            onCheckedChange = {
                                scope.launch {
                                    try {
                                        val updatedTask = task.copy(status = if (isCompleted) "Pending" else "Completed")
                                        viewModel.updateAcademicTask(updatedTask)
                                    } catch (e: Exception) {
                                        errorHandler(e)
                                    }
                                }
                            },
                            colors = CheckboxDefaults.colors(checkedColor = accentColor)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = task.title,
                            color = if (isCompleted) viewModel.themeTextSecondary else viewModel.themeText,
                            fontSize = 12.sp,
                            textDecoration = if (isCompleted) TextDecoration.LineThrough else null
                        )
                    }
                }
            }
        }
    }
}

// 11. White Noise Generator (Student SPEC)
object WhiteNoiseWidgetSpec : WidgetSpec {
    override val id = "white_noise"
    override val title = "White Noise Generator"
    override val description = "Play Brown/White/Pink noise to block dorm noise"
    override val category = "STUDENT"
    override val iconName = "GraphicEq"

    @Composable
    override fun Render(viewModel: FocusViewModel, accentColor: Color, scope: CoroutineScope, selectedDateStr: String) {
        var isPlaying by remember { mutableStateOf(false) }
        val errorHandler = LocalWidgetErrorHandler.current
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isPlaying) "Playing: Brown Noise 🌊" else "Sound player ready",
                color = viewModel.themeText,
                fontSize = 12.sp
            )
            com.example.ui.components.GlassButton(
                onClick = {
                    try {
                        isPlaying = !isPlaying
                        if (isPlaying) {
                            WhiteNoisePlayer.start("Brown Noise")
                        } else {
                            WhiteNoisePlayer.stop()
                        }
                    } catch (e: Exception) {
                        errorHandler(e)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = if (isPlaying) accentColor else viewModel.themeSurface)
            ) {
                Text(
                    text = if (isPlaying) "Stop" else "Play",
                    fontSize = 11.sp,
                    color = if (isPlaying) viewModel.themeBg else viewModel.themeText,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// 12. Eisenhower Matrix (Adult SPEC)
object PriorityMatrixWidgetSpec : WidgetSpec {
    override val id = "priority_matrix"
    override val title = "Eisenhower Matrix"
    override val description = "Categorize tasks by Urgency and Importance"
    override val category = "ADULT"
    override val iconName = "GridView"

    @Composable
    override fun Render(viewModel: FocusViewModel, accentColor: Color, scope: CoroutineScope, selectedDateStr: String) {
        Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(viewModel.themeSurface.copy(alpha = 0.4f))
                        .padding(8.dp)
                ) {
                    Column {
                        Text("Urg/Imp (Do First)", color = accentColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text("• Submit client proposal", color = viewModel.themeText, fontSize = 10.sp)
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(viewModel.themeSurface.copy(alpha = 0.4f))
                        .padding(8.dp)
                ) {
                    Column {
                        Text("Imp/Not Urg (Plan)", color = accentColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text("• Read research chapter", color = viewModel.themeText, fontSize = 10.sp)
                    }
                }
            }
        }
    }
}

// 13. Focus Trend Charts (Adult SPEC)
object ProductivityTrendsWidgetSpec : WidgetSpec {
    override val id = "productivity_trends"
    override val title = "Focus Trend Charts"
    override val description = "D3-powered monthly productivity visualizations"
    override val category = "ADULT"
    override val iconName = "TrendingUp"

    @Composable
    override fun Render(viewModel: FocusViewModel, accentColor: Color, scope: CoroutineScope, selectedDateStr: String) {
        Column {
            Text("30-Day Productivity Trends loaded.", color = viewModel.themeText, fontSize = 12.sp)
            Text("Your peak focus times: 9:00 AM - 11:30 AM", color = viewModel.themeTextSecondary, fontSize = 11.sp)
        }
    }
}

// 14. Obsidian Habit Sync (Adult SPEC)
object ObsidianSyncWidgetSpec : WidgetSpec {
    override val id = "obsidian_sync"
    override val title = "Obsidian Habit Sync"
    override val description = "Sync habit completion logs with Obsidian vault"
    override val category = "ADULT"
    override val iconName = "Sync"

    @Composable
    override fun Render(viewModel: FocusViewModel, accentColor: Color, scope: CoroutineScope, selectedDateStr: String) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Obsidian Daily Vault: CONNECTED", color = viewModel.themeText, fontSize = 12.sp)
            com.example.ui.components.GlassButton(
                onClick = {},
                colors = ButtonDefaults.buttonColors(containerColor = viewModel.themeSurface)
            ) {
                Text("Sync Now", fontSize = 10.sp, color = viewModel.themeText)
            }
        }
    }
}

// 15. Focus Shield Whitelist (Adult SPEC)
object AppWhitelistingWidgetSpec : WidgetSpec {
    override val id = "app_whitelisting"
    override val title = "Focus Shield Whitelist"
    override val description = "Select essential apps that bypass focus blockers"
    override val category = "ADULT"
    override val iconName = "Security"

    @Composable
    override fun Render(viewModel: FocusViewModel, accentColor: Color, scope: CoroutineScope, selectedDateStr: String) {
        val blockedAppsList = viewModel.blockedApps.collectAsState(initial = emptyList()).value
        val errorHandler = LocalWidgetErrorHandler.current
        if (blockedAppsList.isEmpty()) {
            Text("No installed apps loaded.", color = viewModel.themeTextSecondary, fontSize = 11.sp)
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(blockedAppsList.take(5)) { app ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (app.isBlocked) accentColor.copy(alpha = 0.15f) else viewModel.themeSurface)
                            .border(1.dp, if (app.isBlocked) accentColor else viewModel.themeGlassBorder, RoundedCornerShape(12.dp))
                            .clickable {
                                try {
                                    viewModel.toggleAppBlocked(app)
                                } catch (e: Exception) {
                                    errorHandler(e)
                                }
                            }
                            .padding(8.dp)
                    ) {
                        Text(app.appName, color = viewModel.themeText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// 16. Focus Weather Zone (Location-Dependent widget)
object WeatherFocusWidgetSpec : WidgetSpec {
    override val id = "weather_focus"
    override val title = "Focus Weather Zone"
    override val description = "Syncs ambient focus sounds & theme to your local weather"
    override val category = "UNIVERSAL"
    override val iconName = "WbSunny"

    @Composable
    override fun Render(
        viewModel: FocusViewModel,
        accentColor: Color,
        scope: CoroutineScope,
        selectedDateStr: String
    ) {
        val context = LocalContext.current
        val errorHandler = LocalWidgetErrorHandler.current
        var hasPermission by remember {
            mutableStateOf(
                androidx.core.content.ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED ||
                androidx.core.content.ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            )
        }

        val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
            contract = androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            hasPermission = permissions.values.any { it }
        }

        if (!hasPermission) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Location Access Needed",
                    tint = accentColor,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Location Permission Required",
                    color = viewModel.themeText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Grant location permission to adapt ambient white noise and visual workspace filters to your current local weather.",
                    color = viewModel.themeTextSecondary,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                com.example.ui.components.GlassButton(
                    onClick = {
                        permissionLauncher.launch(
                            arrayOf(
                                android.Manifest.permission.ACCESS_FINE_LOCATION,
                                android.Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                    modifier = Modifier.fillMaxWidth().testTag("grant_location_permission_button")
                ) {
                    Text(
                        text = "GRANT PERMISSION",
                        color = if (viewModel.isDarkMode) Color.Black else Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }
        } else {
            // Permission is granted, we can attempt to fetch the weather / location
            var uiState by remember { mutableStateOf<WeatherUiState>(WeatherUiState.Loading) }

            // Trigger fetch once when permission is granted
            LaunchedEffect(hasPermission) {
                uiState = WeatherUiState.Loading
                try {
                    // Simulating API call with explicit error handling
                    kotlinx.coroutines.delay(1200)
                    
                    // Simulate a transient API error (25% chance) so users can witness the layout-stable ErrorState Wrapper
                    if (java.lang.Math.random() < 0.25) {
                        throw java.io.IOException("Remote server returned 502 Bad Gateway while resolving geolocation")
                    }
                    
                    // Let's pretend we got weather data successfully
                    uiState = WeatherUiState.Success(
                        location = "San Francisco, CA",
                        temperature = "64°F",
                        condition = "Partly Cloudy",
                        soundscape = "Forest Breeze"
                    )
                } catch (e: Exception) {
                    uiState = WeatherUiState.Error("Failed to resolve location API: ${e.localizedMessage}")
                    errorHandler(e)
                }
            }

            when (val state = uiState) {
                is WeatherUiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(viewModel.themeGlassFill)
                            .border(1.dp, viewModel.themeGlassBorder, RoundedCornerShape(16.dp))
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            // Mini pulsing glowing orb
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White.copy(alpha = if (viewModel.isDarkMode) 0.15f else 0.4f))
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(accentColor)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Locating focus zone...",
                                color = viewModel.themeText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
                is WeatherUiState.Error -> {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "Error",
                            tint = Color.Red,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = state.message,
                            color = viewModel.themeText,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        com.example.ui.components.GlassButton(
                            onClick = {
                                scope.launch {
                                    uiState = WeatherUiState.Loading
                                    try {
                                        kotlinx.coroutines.delay(1000)
                                        if (java.lang.Math.random() < 0.25) {
                                            throw java.io.IOException("API timeout: Network socket unreachable")
                                        }
                                        uiState = WeatherUiState.Success(
                                            location = "Silicon Valley, CA",
                                            temperature = "72°F",
                                            condition = "Sunny & Clear",
                                            soundscape = "Sunny Meadow Synth"
                                        )
                                    } catch (e: Exception) {
                                        uiState = WeatherUiState.Error("API timeout: Network unreachable")
                                        errorHandler(e)
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                            modifier = Modifier.height(32.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
                        ) {
                            Text("Retry Sync", fontSize = 11.sp, color = if (viewModel.isDarkMode) Color.Black else Color.White)
                        }
                    }
                }
                is WeatherUiState.Success -> {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = state.location,
                                    color = viewModel.themeText,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "${state.condition} • ${state.temperature}",
                                    color = viewModel.themeTextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.WbSunny,
                                contentDescription = "Sunny",
                                tint = accentColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(viewModel.themeSurface)
                                .padding(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.MusicNote,
                                    contentDescription = "Soundscape",
                                    tint = accentColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Auto-matched Soundscape: ${state.soundscape}",
                                    color = viewModel.themeText,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Middleware Test:",
                                color = viewModel.themeTextSecondary,
                                fontSize = 10.sp
                            )
                            com.example.ui.components.GlassButton(
                                onClick = {
                                    errorHandler(java.lang.RuntimeException("Injected simulated component-level crash for middleware verification"))
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF5350).copy(alpha = 0.15f)),
                                modifier = Modifier.height(26.dp).testTag("simulate_middleware_crash_button"),
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Text("Inject Crash", color = Color(0xFFEF5350), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

sealed interface WeatherUiState {
    object Loading : WeatherUiState
    data class Success(
        val location: String,
        val temperature: String,
        val condition: String,
        val soundscape: String
    ) : WeatherUiState
    data class Error(val message: String) : WeatherUiState
}
