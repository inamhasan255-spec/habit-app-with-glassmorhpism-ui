package com.focuscube.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters

@Entity(tableName = "flashcards")
@TypeConverters(StringListConverter::class)
data class Flashcard(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val question: String,
    val correctAnswer: String,
    val distractors: List<String>,
    val associatedCourseId: String
)

class StringListConverter {
    @TypeConverter
    fun fromString(value: String): List<String> {
        if (value.isEmpty()) return emptyList()
        return value.split("|||")
    }

    @TypeConverter
    fun fromList(list: List<String>): String {
        return list.joinToString("|||")
    }
}
