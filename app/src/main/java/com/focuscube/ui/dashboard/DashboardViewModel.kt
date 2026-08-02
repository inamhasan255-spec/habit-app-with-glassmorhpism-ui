package com.focuscube.ui.dashboard
import kotlinx.coroutines.flow.MutableStateFlow

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.focuscube.core.datastore.BlockPreferences
import com.focuscube.data.local.FocusCubeDatabase
import com.focuscube.data.model.AssignmentEntity
import com.focuscube.data.model.AcademicTask
import com.focuscube.data.model.AvailabilityBlock
import com.focuscube.data.model.BlockState
import com.focuscube.data.model.TimeCushionCalculator
import com.focuscube.data.model.TimeCushionEngine
import com.focuscube.data.model.UserSchedule
import com.focuscube.ui.widget.FocusCubeGlanceWidget
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.Date

import com.focuscube.data.model.TodoTask

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val db = FocusCubeDatabase.getDatabase(application)
    private val dao = db.assignmentDao()
    private val todoDao = db.todoDao()
    private val prefs = BlockPreferences(application)

    val todos: StateFlow<List<TodoTask>> = todoDao.getAllTodosFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _calendarEvents = MutableStateFlow<List<com.focuscube.data.repository.CalendarEvent>>(emptyList())
    val calendarEvents: StateFlow<List<com.focuscube.data.repository.CalendarEvent>> = _calendarEvents

    fun loadCalendarEvents(context: android.content.Context) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_CALENDAR) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                val syncManager = com.focuscube.data.repository.CalendarSyncManager(context)
                _calendarEvents.value = syncManager.getUpcomingEvents(3)
            }
        }
    }

    val isSessionLocked: StateFlow<Boolean> = prefs.isSessionLocked
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val restrictedPackages: StateFlow<Set<String>> = prefs.restrictedPackages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val assignmentsWithCushions: StateFlow<List<Pair<AssignmentEntity, Double>>> = dao.getAllAssignmentsFlow()
        .combine(isSessionLocked) { list, _ ->
            val cushions = TimeCushionCalculator.calculateTimeCushions(
                assignments = list,
                schedule = UserSchedule(),
                now = Date()
            )
            list.mapIndexed { index, entity ->
                Pair(entity, cushions.getOrElse(index) { 0.0 })
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Modern AcademicTask & AvailabilityBlock State flows ---
    val academicTasks: StateFlow<List<AcademicTask>> = dao.getAllAcademicTasksFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val availabilityBlocks: StateFlow<List<AvailabilityBlock>> = dao.getAllAvailabilityBlocksFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val blockState: StateFlow<BlockState> = TimeCushionEngine.blockStateFlow

    init {
        // Initial recalculation when ViewModel starts
        viewModelScope.launch {
            TimeCushionEngine.calculateTimeCushionAndEmitState(application)
        }
    }

    fun toggleSessionLock() {
        viewModelScope.launch {
            val current = isSessionLocked.value
            prefs.setSessionLock(!current)
            try {
                FocusCubeGlanceWidget().updateAll(getApplication())
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    fun addAssignment(title: String, rawDueDate: String, estimatedHours: Double, weight: Double, course: String) {
        viewModelScope.launch {
            dao.insertAssignment(
                AssignmentEntity(
                    title = title,
                    rawDueDate = rawDueDate,
                    estimatedDurationHours = estimatedHours,
                    gradingWeight = weight,
                    courseTitle = course
                )
            )
            try {
                FocusCubeGlanceWidget().updateAll(getApplication())
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    fun deleteAssignment(id: Long) {
        viewModelScope.launch {
            // Find title first to delete corresponding academic task
            try {
                val dbInstance = FocusCubeDatabase.getDatabase(getApplication())
                val assignmentDao = dbInstance.assignmentDao()
                // Room suspend calls or flow first checks
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    val list = assignmentDao.getAllAssignmentsFlow().firstOrNull()
                    val targetTask = list?.find { it.id == id }
                    if (targetTask != null) {
                        val academicTasks = assignmentDao.getAllAcademicTasksFlow().firstOrNull()
                        val targetAcademic = academicTasks?.find { it.title == targetTask.title }
                        if (targetAcademic != null) {
                            assignmentDao.deleteAcademicTaskById(targetAcademic.id)
                        }
                    }
                    assignmentDao.deleteAssignmentById(id)
                }
            } catch (e: Exception) {
                // Fail-safe direct delete
                dao.deleteAssignmentById(id)
            }
            TimeCushionEngine.calculateTimeCushionAndEmitState(getApplication())
            try {
                FocusCubeGlanceWidget().updateAll(getApplication())
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    fun clearAllAssignments() {
        viewModelScope.launch {
            dao.clearAll()
            dao.clearAllAcademicTasks()
            TimeCushionEngine.calculateTimeCushionAndEmitState(getApplication())
            try {
                FocusCubeGlanceWidget().updateAll(getApplication())
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    fun updateRestrictedPackages(packages: Set<String>) {
        viewModelScope.launch {
            prefs.setRestrictedPackages(packages)
        }
    }

    // --- AcademicTask Core Operations ---
    fun addAcademicTask(title: String, rawDueDate: String, estimatedHours: Double, weight: Double, course: String, isHighPriority: Boolean) {
        viewModelScope.launch {
            dao.insertAcademicTask(
                AcademicTask(
                    title = title,
                    rawDueDate = rawDueDate,
                    estimatedDurationHours = estimatedHours,
                    gradingWeight = weight,
                    courseTitle = course,
                    isHighPriority = isHighPriority
                )
            )
            TimeCushionEngine.calculateTimeCushionAndEmitState(getApplication())
            try {
                FocusCubeGlanceWidget().updateAll(getApplication())
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    fun deleteAcademicTask(id: Long) {
        viewModelScope.launch {
            dao.deleteAcademicTaskById(id)
            TimeCushionEngine.calculateTimeCushionAndEmitState(getApplication())
            try {
                FocusCubeGlanceWidget().updateAll(getApplication())
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    fun clearAllAcademicTasks() {
        viewModelScope.launch {
            dao.clearAllAcademicTasks()
            TimeCushionEngine.calculateTimeCushionAndEmitState(getApplication())
            try {
                FocusCubeGlanceWidget().updateAll(getApplication())
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    fun toggleAvailabilityBlock(dayOfWeek: Int, slotIndex: Int) {
        viewModelScope.launch {
            val existing = dao.getAvailabilityBlock(dayOfWeek, slotIndex)
            val updated = if (existing != null) {
                existing.copy(isAvailable = !existing.isAvailable)
            } else {
                val wasDefaultAvailable = (slotIndex in 32..48) || (slotIndex in 56..72)
                AvailabilityBlock(dayOfWeek = dayOfWeek, slotIndex = slotIndex, isAvailable = !wasDefaultAvailable)
            }
            dao.insertAvailabilityBlock(updated)
            TimeCushionEngine.calculateTimeCushionAndEmitState(getApplication())
        }
    }

    // --- Todo Task Operations ---

    fun addTodo(title: String) {
        viewModelScope.launch {
            val currentList = todos.value
            val maxOrder = currentList.maxOfOrNull { it.orderIndex } ?: 0
            todoDao.insertTodo(TodoTask(title = title, orderIndex = maxOrder + 1))
        }
    }

    fun toggleTodoCompletion(todo: TodoTask) {
        viewModelScope.launch {
            todoDao.updateTodo(todo.copy(isCompleted = !todo.isCompleted))
        }
    }

    fun deleteTodo(id: Int) {
        viewModelScope.launch {
            todoDao.deleteTodoById(id)
        }
    }

    fun reorderTodos(fromIndex: Int, toIndex: Int) {
        val currentList = todos.value.toMutableList()
        if (fromIndex in currentList.indices && toIndex in currentList.indices) {
            val item = currentList.removeAt(fromIndex)
            currentList.add(toIndex, item)
            
            // Re-assign orderIndex
            val updatedList = currentList.mapIndexed { index, task -> 
                task.copy(orderIndex = index)
            }
            
            viewModelScope.launch {
                todoDao.updateTodos(updatedList)
            }
        }
    }
}

