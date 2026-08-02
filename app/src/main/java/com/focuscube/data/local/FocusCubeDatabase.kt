package com.focuscube.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.focuscube.data.model.AssignmentEntity
import com.focuscube.data.model.AcademicTask
import com.focuscube.data.model.AvailabilityBlock
import com.focuscube.data.model.Flashcard
import com.focuscube.data.model.TodoTask
import com.focuscube.data.entity.TimetableHabit
import com.focuscube.data.dao.TimetableDao

@Database(
    entities = [AssignmentEntity::class, AcademicTask::class, AvailabilityBlock::class, Flashcard::class, TodoTask::class, TimetableHabit::class],
    version = 5,
    exportSchema = false
)
abstract class FocusCubeDatabase : RoomDatabase() {
    abstract fun assignmentDao(): AssignmentDao
    abstract fun flashcardDao(): FlashcardDao
    abstract fun todoDao(): TodoDao
    abstract fun timetableDao(): TimetableDao

    companion object {
        @Volatile
        private var INSTANCE: FocusCubeDatabase? = null

        fun getDatabase(context: Context): FocusCubeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = try {
                    Room.databaseBuilder(
                        context.applicationContext,
                        FocusCubeDatabase::class.java,
                        "focus_cube_planner_database"
                    )
                    .fallbackToDestructiveMigration()
                    .build()
                } catch (e: Exception) {
                    // Fallback to an in-memory database to prevent any startup or cold-start widget crashes
                    Room.inMemoryDatabaseBuilder(
                        context.applicationContext,
                        FocusCubeDatabase::class.java
                    )
                    .fallbackToDestructiveMigration()
                    .build()
                }
                INSTANCE = instance
                instance
            }
        }
    }
}
