package com.focuscube.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "timetable_habits")
data class TimetableHabit(
    @PrimaryKey
    val weekNumber: Int,
    val phase: String,           // calculus | algebra | geometry | revision
    val chapter: String,
    val dates: String,
    val focus: String,
    val tag: String,             // Foundation | Core | Application | Consolidation | Test | Revision | Exam
    val targetMinutes: Int = 1260, // 3 hrs/day × 7 days
    val completedMinutes: Int = 0,
    val isDone: Boolean = false,
    val notes: String = "",
    val streakDays: Int = 0,
    val lastStudied: Long? = null
) {
    val progress: Float
        get() = (completedMinutes.toFloat() / targetMinutes).coerceIn(0f, 1f)

    val phaseColor: String
        get() = when (phase) {
            "calculus" -> "#EF4444"
            "algebra" -> "#8B5CF6"
            "geometry" -> "#06B6D4"
            "revision" -> "#10B981"
            else -> "#94A3B8"
        }
}
