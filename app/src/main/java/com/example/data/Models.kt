package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: String, // Health, Learn, Work, Life, etc.
    val colorHex: String,
    val isObsidianLinked: Boolean = false,
    val obsidianUri: String = "",
    val streakCount: Int = 0,
    val lastCompletedDate: String = "", // "yyyy-MM-dd"
    val targetValue: Int = 1,
    val unit: String = "times"
)

@Entity(tableName = "habit_completions")
data class HabitCompletion(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val habitId: Int,
    val date: String, // "yyyy-MM-dd"
    val value: Int = 1
)

@Entity(tableName = "work_sessions")
data class WorkSession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val label: String,
    val durationMinutes: Int,
    val focusTimeMinutes: Int,
    val timestamp: Long, // Epoch ms
    val category: String
)

@Entity(tableName = "blocked_apps")
data class BlockedApp(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val packageName: String,
    val appName: String,
    val isBlocked: Boolean,
    val category: String, // Social Media, Entertainment, Communication, etc.
    val blockMode: String // "Session only", "Scheduled", "Reminder always"
)

data class ScheduledWork(
    val id: String,
    val title: String,
    val date: String, // "yyyy-MM-dd"
    val durationMinutes: Int,
    val category: String,
    val isGoogleSynced: Boolean = false
)

@Entity(tableName = "essential_items")
data class EssentialItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val type: String, // "App" or "Website"
    val identifier: String // package name or domain name
)

@Entity(tableName = "journal_entries")
data class JournalEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val mood: Int, // 1 to 5
    val gratitudePrompt: String,
    val textEntry: String,
    val timestamp: Long,
    val isSessionReflection: Boolean = false,
    val focusMinutes: Int = 0,
    val focusCategory: String = ""
)

@Entity(tableName = "reading_books")
data class ReadingBook(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val author: String,
    val totalPages: Int,
    val currentPage: Int,
    val isFinished: Boolean = false,
    val totalTimeSpentSeconds: Long = 0,
    val lastReadTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val role: String, // "user" or "model"
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "academic_subjects")
data class AcademicSubject(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val instructor: String = "",
    val room: String = "",
    val colorHex: String = "#3B82F6"
)

@Entity(tableName = "timetable_entries")
data class TimetableEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val subjectName: String,
    val dayOfWeek: Int, // 1 = Monday, ..., 7 = Sunday
    val weekRotation: String = "All", // "All", "A", "B", "Week 1", "Week 2"
    val startTime: String, // "HH:mm"
    val endTime: String, // "HH:mm"
    val instructor: String = "",
    val room: String = "",
    val colorHex: String = "#3B82F6"
)

@Entity(tableName = "academic_tasks")
data class AcademicTask(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val subjectName: String,
    val dueDate: Long, // epoch millis
    val priority: String = "Medium", // "Low", "Medium", "High"
    val status: String = "Pending", // "Pending", "In Progress", "Completed"
    val notes: String = "",
    val subtasksJson: String = "[]", // JSON string of subtasks: [{"title": "Step 1", "completed": false}]
    val sortOrder: Int = 0
)

@Entity(tableName = "academic_profiles")
data class AcademicProfile(
    @PrimaryKey val id: Int = 1, // Single-row configuration
    val role: String = "Student", // "Student", "Teacher", "Tutor", "Parent"
    val customBgType: String = "Color", // "Color", "Image", "Animation"
    val customBgValue: String = "#0B0F19", // Hex code, or image uri, or animation type "Cosmic" / "Synth"
    val opacity: Float = 1.0f
)

@Entity(tableName = "voice_notes")
data class VoiceNote(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val filePath: String,
    val durationSec: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "academic_chat_threads")
data class AcademicChatThread(
    @PrimaryKey val id: String,
    val title: String,
    val lastActiveTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "academic_chat_messages")
data class AcademicChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val threadId: String,
    val role: String, // "user" or "model"
    val message: String,
    val imageUri: String? = null, // Store image uri (e.g. captured photo) if any
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "focus_sessions")
data class FocusSession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val label: String,
    val startTime: Long,
    val endTime: Long,
    val mode: String, // "Deep Work", "Pomodoro", "Custom"
    val plannedMinutes: Int,
    val actualMinutes: Int,
    val interruptionCount: Int,
    val completed: Boolean
)

enum class FocusSessionState {
    IDLE, RUNNING, PAUSED, COMPLETED, INTERRUPTED
}





@Entity(tableName = "community_groups")
data class CommunityGroup(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val sharedGoalHours: Int,
    val joinCode: String
)

@Entity(tableName = "community_members")
data class CommunityMember(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val groupId: Int,
    val name: String,
    val isMe: Boolean = false,
    val progressHours: Float = 0f,
    val lastUpdated: Long = System.currentTimeMillis()
)
