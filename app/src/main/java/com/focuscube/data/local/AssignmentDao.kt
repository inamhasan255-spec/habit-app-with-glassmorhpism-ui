package com.focuscube.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.focuscube.data.model.AssignmentEntity
import com.focuscube.data.model.AcademicTask
import com.focuscube.data.model.AvailabilityBlock
import kotlinx.coroutines.flow.Flow

@Dao
interface AssignmentDao {
    @Query("SELECT * FROM assignments ORDER BY rawDueDate ASC")
    fun getAllAssignmentsFlow(): Flow<List<AssignmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssignment(assignment: AssignmentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssignments(assignments: List<AssignmentEntity>)

    @Query("DELETE FROM assignments WHERE id = :id")
    suspend fun deleteAssignmentById(id: Long)

    @Query("DELETE FROM assignments")
    suspend fun clearAll()

    // --- AcademicTask methods ---
    @Query("SELECT * FROM academic_tasks ORDER BY rawDueDate ASC")
    fun getAllAcademicTasksFlow(): Flow<List<AcademicTask>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAcademicTask(task: AcademicTask)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAcademicTasks(tasks: List<AcademicTask>)

    @Query("DELETE FROM academic_tasks WHERE id = :id")
    suspend fun deleteAcademicTaskById(id: Long)

    @Query("DELETE FROM academic_tasks")
    suspend fun clearAllAcademicTasks()

    // --- AvailabilityBlock methods ---
    @Query("SELECT * FROM availability_blocks")
    fun getAllAvailabilityBlocksFlow(): Flow<List<AvailabilityBlock>>

    @Query("SELECT * FROM availability_blocks WHERE dayOfWeek = :dayOfWeek AND slotIndex = :slotIndex LIMIT 1")
    suspend fun getAvailabilityBlock(dayOfWeek: Int, slotIndex: Int): AvailabilityBlock?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAvailabilityBlock(block: AvailabilityBlock)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAvailabilityBlocks(blocks: List<AvailabilityBlock>)
}

