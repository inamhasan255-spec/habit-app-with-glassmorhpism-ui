package com.example.data.db

import kotlinx.coroutines.flow.Flow

class AppRepository(
    private val habitDao: HabitDao,
    private val focusSessionDao: FocusSessionDao,
    private val noteDao: NoteDao
) {
    val allHabits: Flow<List<Habit>> = habitDao.getAllHabits()
    val allSessions: Flow<List<FocusSession>> = focusSessionDao.getAllSessions()
    val allNotes: Flow<List<Note>> = noteDao.getAllNotes()

    suspend fun insertHabit(habit: Habit) = habitDao.insertHabit(habit)
    suspend fun insertSession(session: FocusSession) = focusSessionDao.insertSession(session)
    suspend fun insertNote(note: Note) = noteDao.insertNote(note)
}
