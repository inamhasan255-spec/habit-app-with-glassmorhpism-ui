package com.focuscube.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.focuscube.data.model.TodoTask
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoDao {
    @Query("SELECT * FROM todo_tasks ORDER BY orderIndex ASC")
    fun getAllTodosFlow(): Flow<List<TodoTask>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTodo(todo: TodoTask): Long

    @Update
    suspend fun updateTodo(todo: TodoTask)

    @Update
    suspend fun updateTodos(todos: List<TodoTask>)

    @Query("DELETE FROM todo_tasks WHERE id = :id")
    suspend fun deleteTodoById(id: Int)
}
