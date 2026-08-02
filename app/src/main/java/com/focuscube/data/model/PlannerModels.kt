package com.focuscube.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Entity(tableName = "assignments")
data class AssignmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val rawDueDate: String, // format: "YYYY-MM-DD"
    val estimatedDurationHours: Double,
    val gradingWeight: Double,
    val courseTitle: String
)

/**
 * Model representing a user's calendar availability allocations as a 15-minute chunked boolean mask.
 * There are 24 hours * 4 slots/hour = 96 slots per day.
 * We can represent weekly schedule using a list of 7 BooleanArrays (one for each day, size 96).
 */
data class UserSchedule(
    val weeklyAvailability: Map<Int, BooleanArray> = defaultWeeklyAvailability()
) {
    companion object {
        fun defaultWeeklyAvailability(): Map<Int, BooleanArray> {
            val map = mutableMapOf<Int, BooleanArray>()
            // Days: Calendar.SUNDAY to Calendar.SATURDAY
            for (day in Calendar.SUNDAY..Calendar.SATURDAY) {
                val dayMask = BooleanArray(96) { index ->
                    // Default availability: active between 8:00 AM (index 32) and 12:00 PM (index 48), 
                    // and 2:00 PM (index 56) to 6:00 PM (index 72).
                    (index in 32..48) || (index in 56..72)
                }
                map[day] = dayMask
            }
            return map
        }
    }

    /**
     * Calculates the total available study hours between two dates using the 15-minute availability mask.
     */
    fun calculateAvailableHours(startDate: Date, endDate: Date): Double {
        if (startDate.after(endDate)) return 0.0

        var totalHours = 0.0
        val currentCalendar = Calendar.getInstance().apply { time = startDate }
        val targetCalendar = Calendar.getInstance().apply { time = endDate }

        // Loop 15-minute chunks
        while (currentCalendar.before(targetCalendar)) {
            val dayOfWeek = currentCalendar.get(Calendar.DAY_OF_WEEK)
            val hour = currentCalendar.get(Calendar.HOUR_OF_DAY)
            val minute = currentCalendar.get(Calendar.MINUTE)
            val slotIndex = (hour * 4) + (minute / 15)

            val dayMask = weeklyAvailability[dayOfWeek]
            if (dayMask != null && slotIndex in 0..95) {
                if (dayMask[slotIndex]) {
                    totalHours += 0.25 // 15 minutes = 0.25 hours
                }
            }
            currentCalendar.add(Calendar.MINUTE, 15)
        }

        return totalHours
    }
}

object TimeCushionCalculator {
    /**
     * T_C(i) = (Total Available Study Hours until Due Date of task i) - (Sum of remaining workload hours for tasks 1 to i)
     * We assume tasks are sorted chronologically by due date.
     */
    fun calculateTimeCushions(
        assignments: List<AssignmentEntity>?,
        schedule: UserSchedule? = UserSchedule(),
        now: Date? = Date()
    ): List<Double> {
        if (assignments.isNullOrEmpty()) {
            return emptyList()
        }
        val safeSchedule = schedule ?: UserSchedule()
        val safeNow = now ?: Date()
        val cushions = mutableListOf<Double>()
        var runningSumDuration = 0.0

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        for (assignment in assignments) {
            // Null check/fallback for assignment fields to prevent potential NullPointerExceptions
            val duration = assignment.estimatedDurationHours ?: 0.0
            runningSumDuration += duration
            
            val rawDueDate = assignment.rawDueDate ?: ""
            val dueDate = try {
                if (rawDueDate.isEmpty()) {
                    Date(safeNow.time + 7L * 24 * 60 * 60 * 1000)
                } else {
                    dateFormat.parse(rawDueDate) ?: safeNow
                }
            } catch (e: Exception) {
                // If parsing fails, default to 7 days from now
                Date(safeNow.time + 7L * 24 * 60 * 60 * 1000)
            }

            // Set the due date time to end of day to give full credit
            val dueDateEndOfDay = Calendar.getInstance().apply {
                time = dueDate
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }.time

            val availableStudyHours = safeSchedule.calculateAvailableHours(safeNow, dueDateEndOfDay)
            val tC = availableStudyHours - runningSumDuration
            cushions.add(tC)
        }
        return cushions
    }
}

@Entity(tableName = "academic_tasks")
data class AcademicTask(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val rawDueDate: String, // "yyyy-MM-dd"
    val estimatedDurationHours: Double,
    val gradingWeight: Double,
    val courseTitle: String,
    val isHighPriority: Boolean = false
)

@Entity(tableName = "availability_blocks")
data class AvailabilityBlock(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dayOfWeek: Int, // Calendar.SUNDAY..Calendar.SATURDAY
    val slotIndex: Int, // 0..95 for 15-minute intervals in a day
    val isAvailable: Boolean = true
)

