package com.focuscube.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.focuscube.data.entity.TimetableHabit
import com.focuscube.data.local.FocusCubeDatabase
import com.focuscube.data.repository.TimetableRepository
import com.focuscube.domain.usecase.SeedTimetableUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TimetableViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = FocusCubeDatabase.getDatabase(application).timetableDao()
    private val repository = TimetableRepository(dao)
    private val seedUseCase = SeedTimetableUseCase(application, repository)

    val weeks: StateFlow<List<TimetableHabit>> = repository.getAllWeeks()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val totalCompletedMinutes: StateFlow<Int> = repository.getTotalStudyMinutes()
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    val completedWeeksCount: StateFlow<Int> = repository.getCompletedCount()
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    init {
        viewModelScope.launch {
            seedUseCase()
        }
    }

    fun logStudySession(minutes: Int, weekNumber: Int) {
        viewModelScope.launch {
            repository.addMinutes(weekNumber, minutes)
        }
    }

    fun toggleWeekDone(weekNumber: Int, isDone: Boolean) {
        viewModelScope.launch {
            repository.setWeekDone(weekNumber, isDone)
        }
    }

    fun updateNotes(weekNumber: Int, notes: String) {
        viewModelScope.launch {
            repository.updateNotes(weekNumber, notes)
        }
    }
}
