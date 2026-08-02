package com.focuscube.data.model

import android.content.Context
import com.focuscube.data.local.FocusCubeDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class BlockState {
    NORMAL,
    STRICT_LOCK
}

object TimeCushionEngine {

    private val _blockStateFlow = MutableStateFlow(BlockState.NORMAL)
    val blockStateFlow: StateFlow<BlockState> = _blockStateFlow.asStateFlow()

    /**
     * Cross-process lightweight writer for block state
     */
    fun writeBlockStateToDisk(context: Context, state: BlockState) {
        try {
            val file = File(context.filesDir, "block_state.txt")
            file.writeText(state.name)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Cross-process lightweight reader for block state
     */
    fun readBlockStateFromDisk(context: Context): BlockState {
        return try {
            val file = File(context.filesDir, "block_state.txt")
            if (file.exists()) {
                val content = file.readText().trim()
                BlockState.valueOf(content)
            } else {
                BlockState.NORMAL
            }
        } catch (e: Exception) {
            BlockState.NORMAL
        }
    }

    /**
     * Recalculates the Time Cushion utilizing discretized 15-minute time-blocks
     * and evaluates if any high-priority tasks have T_C < 0.
     * Emits the block state via StateFlow and persists to disk for Accessibility Service.
     */
    suspend fun calculateTimeCushionAndEmitState(context: Context): BlockState = withContext(Dispatchers.Default) {
        val database = FocusCubeDatabase.getDatabase(context)
        val dao = database.assignmentDao()

        // 1. Fetch academic tasks
        val tasks = withContext(Dispatchers.IO) {
            dao.getAllAcademicTasksFlow().firstOrNull() ?: emptyList()
        }

        // 2. Fetch or populate availability blocks
        var blocks = withContext(Dispatchers.IO) {
            dao.getAllAvailabilityBlocksFlow().firstOrNull() ?: emptyList()
        }

        if (blocks.isEmpty()) {
            // Seed default availability blocks
            val defaultBlocks = mutableListOf<AvailabilityBlock>()
            for (day in Calendar.SUNDAY..Calendar.SATURDAY) {
                for (slot in 0..95) {
                    val isAvailable = (slot in 32..48) || (slot in 56..72)
                    defaultBlocks.add(
                        AvailabilityBlock(
                            dayOfWeek = day,
                            slotIndex = slot,
                            isAvailable = isAvailable
                        )
                    )
                }
            }
            withContext(Dispatchers.IO) {
                dao.insertAvailabilityBlocks(defaultBlocks)
            }
            blocks = defaultBlocks
        }

        // Create a fast lookup map for availability blocks
        val availabilityMap = blocks.associate { Pair(it.dayOfWeek, it.slotIndex) to it.isAvailable }

        val safeNow = Date()
        val cushions = mutableListOf<Double>()
        var runningSumDuration = 0.0

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        var anyHighPriorityDeficit = false

        // Sort tasks chronologically by due date
        val sortedTasks = tasks.sortedBy { task ->
            try {
                if (task.rawDueDate.isEmpty()) {
                    Date(safeNow.time + 7L * 24 * 60 * 60 * 1000)
                } else {
                    dateFormat.parse(task.rawDueDate) ?: safeNow
                }
            } catch (e: Exception) {
                Date(safeNow.time + 7L * 24 * 60 * 60 * 1000)
            }
        }

        for (task in sortedTasks) {
            val duration = task.estimatedDurationHours
            runningSumDuration += duration

            val dueDate = try {
                if (task.rawDueDate.isEmpty()) {
                    Date(safeNow.time + 7L * 24 * 60 * 60 * 1000)
                } else {
                    dateFormat.parse(task.rawDueDate) ?: safeNow
                }
            } catch (e: Exception) {
                Date(safeNow.time + 7L * 24 * 60 * 60 * 1000)
            }

            // Set due date to end of day to give full availability buffer on that day
            val dueDateEndOfDay = Calendar.getInstance().apply {
                time = dueDate
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }.time

            // Calculate available hours using discretized blocks
            val availableHours = calculateAvailableHoursDiscretized(safeNow, dueDateEndOfDay, availabilityMap)
            val tC = availableHours - runningSumDuration
            cushions.add(tC)

            if (task.isHighPriority && tC < 0.0) {
                anyHighPriorityDeficit = true
            }
        }

        // Also check legacy AssignmentEntity deficits if any exist, to preserve user-entered tasks safety
        val legacyAssignments = withContext(Dispatchers.IO) {
            dao.getAllAssignmentsFlow().firstOrNull() ?: emptyList()
        }
        val legacyCushions = TimeCushionCalculator.calculateTimeCushions(legacyAssignments, UserSchedule(), safeNow)
        val legacyDeficit = legacyCushions.any { it < 0.0 }

        val finalState = if (anyHighPriorityDeficit || legacyDeficit) BlockState.STRICT_LOCK else BlockState.NORMAL
        
        // Emit State to Flow
        _blockStateFlow.value = finalState

        // Write State to Disk for Accessibility Service Process
        writeBlockStateToDisk(context, finalState)

        finalState
    }

    private fun calculateAvailableHoursDiscretized(
        startDate: Date,
        endDate: Date,
        availabilityMap: Map<Pair<Int, Int>, Boolean>
    ): Double {
        if (startDate.after(endDate)) return 0.0

        var totalHours = 0.0
        val currentCalendar = Calendar.getInstance().apply { time = startDate }
        val targetCalendar = Calendar.getInstance().apply { time = endDate }

        // Discretized 15-minute loop
        while (currentCalendar.before(targetCalendar)) {
            val dayOfWeek = currentCalendar.get(Calendar.DAY_OF_WEEK)
            val hour = currentCalendar.get(Calendar.HOUR_OF_DAY)
            val minute = currentCalendar.get(Calendar.MINUTE)
            val slotIndex = (hour * 4) + (minute / 15)

            // Lookup availability or fallback to standard hours
            val isAvailable = availabilityMap[Pair(dayOfWeek, slotIndex)] ?: (
                (slotIndex in 32..48) || (slotIndex in 56..72)
            )

            if (isAvailable) {
                totalHours += 0.25
            }
            currentCalendar.add(Calendar.MINUTE, 15)
        }

        return totalHours
    }
}
