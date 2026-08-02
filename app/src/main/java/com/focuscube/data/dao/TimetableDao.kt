package com.focuscube.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.focuscube.data.entity.TimetableHabit

@Dao
interface TimetableDao {

    @Query("SELECT * FROM timetable_habits ORDER BY weekNumber ASC")
    fun getAllWeeks(): Flow<List<TimetableHabit>>

    @Query("SELECT * FROM timetable_habits WHERE weekNumber = :weekNumber")
    suspend fun getWeekByNumber(weekNumber: Int): TimetableHabit?

    @Query("SELECT * FROM timetable_habits WHERE phase = :phase ORDER BY weekNumber ASC")
    fun getWeeksByPhase(phase: String): Flow<List<TimetableHabit>>

    @Query("SELECT COUNT(*) FROM timetable_habits WHERE isDone = 1")
    fun getCompletedCount(): Flow<Int>

    @Query("SELECT SUM(completedMinutes) FROM timetable_habits")
    fun getTotalStudyMinutes(): Flow<Int>

    @Query("SELECT * FROM timetable_habits WHERE lastStudied IS NOT NULL ORDER BY lastStudied DESC LIMIT 1")
    suspend fun getMostRecentWeek(): TimetableHabit?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(habits: List<TimetableHabit>)

    @Update
    suspend fun update(habit: TimetableHabit)

    @Query("UPDATE timetable_habits SET isDone = :isDone WHERE weekNumber = :weekNumber")
    suspend fun setWeekDone(weekNumber: Int, isDone: Boolean)

    @Query("UPDATE timetable_habits SET completedMinutes = completedMinutes + :minutes, lastStudied = :timestamp WHERE weekNumber = :weekNumber")
    suspend fun addMinutes(weekNumber: Int, minutes: Int, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE timetable_habits SET notes = :notes WHERE weekNumber = :weekNumber")
    suspend fun updateNotes(weekNumber: Int, notes: String)

    @Query("SELECT COUNT(*) FROM timetable_habits")
    suspend fun getCount(): Int
}
