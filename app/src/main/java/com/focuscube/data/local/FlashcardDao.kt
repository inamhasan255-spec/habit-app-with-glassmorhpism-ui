package com.focuscube.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.focuscube.data.model.Flashcard

@Dao
interface FlashcardDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlashcard(flashcard: Flashcard)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlashcards(flashcards: List<Flashcard>)

    @Query("SELECT * FROM flashcards WHERE associatedCourseId = :courseId ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomFlashcardForCourse(courseId: String): Flashcard?

    @Query("SELECT * FROM flashcards ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomFlashcard(): Flashcard?
    
    @Query("SELECT COUNT(*) FROM flashcards")
    suspend fun getCount(): Int
}
