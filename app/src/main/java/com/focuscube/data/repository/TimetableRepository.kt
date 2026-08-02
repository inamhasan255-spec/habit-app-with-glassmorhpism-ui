package com.focuscube.data.repository

import com.focuscube.data.dao.TimetableDao
import com.focuscube.data.entity.TimetableHabit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class TimetableRepository(private val dao: TimetableDao) {
    fun getAllWeeks(): Flow<List<TimetableHabit>> = dao.getAllWeeks().flowOn(Dispatchers.IO)

    suspend fun getWeekByNumber(weekNumber: Int): TimetableHabit? = withContext(Dispatchers.IO) {
        dao.getWeekByNumber(weekNumber)
    }

    fun getWeeksByPhase(phase: String): Flow<List<TimetableHabit>> = dao.getWeeksByPhase(phase).flowOn(Dispatchers.IO)

    fun getCompletedCount(): Flow<Int> = dao.getCompletedCount().flowOn(Dispatchers.IO)

    fun getTotalStudyMinutes(): Flow<Int> = dao.getTotalStudyMinutes().flowOn(Dispatchers.IO)

    suspend fun getMostRecentWeek(): TimetableHabit? = withContext(Dispatchers.IO) {
        dao.getMostRecentWeek()
    }

    suspend fun insertAll(habits: List<TimetableHabit>) = withContext(Dispatchers.IO) {
        dao.insertAll(habits)
    }

    suspend fun update(habit: TimetableHabit) = withContext(Dispatchers.IO) {
        dao.update(habit)
    }

    suspend fun setWeekDone(weekNumber: Int, isDone: Boolean) = withContext(Dispatchers.IO) {
        dao.setWeekDone(weekNumber, isDone)
    }

    suspend fun addMinutes(weekNumber: Int, minutes: Int, timestamp: Long = System.currentTimeMillis()) = withContext(Dispatchers.IO) {
        dao.addMinutes(weekNumber, minutes, timestamp)
    }

    suspend fun updateNotes(weekNumber: Int, notes: String) = withContext(Dispatchers.IO) {
        dao.updateNotes(weekNumber, notes)
    }

    suspend fun getCount(): Int = withContext(Dispatchers.IO) {
        dao.getCount()
    }
}
