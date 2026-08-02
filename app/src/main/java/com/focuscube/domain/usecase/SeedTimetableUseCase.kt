package com.focuscube.domain.usecase

import android.content.Context
import com.focuscube.data.entity.TimetableHabit
import com.focuscube.data.repository.TimetableRepository
import org.json.JSONArray
import java.io.InputStreamReader

class SeedTimetableUseCase(
    private val context: Context,
    private val repository: TimetableRepository
) {
    suspend operator fun invoke() {
        if (repository.getCount() == 0) {
            val resourceId = context.resources.getIdentifier("timetable", "raw", context.packageName)
            if (resourceId != 0) {
                val inputStream = context.resources.openRawResource(resourceId)
                val jsonString = InputStreamReader(inputStream).readText()
                val jsonArray = JSONArray(jsonString)
                val habits = mutableListOf<TimetableHabit>()

                for (i in 0 until jsonArray.length()) {
                    val jsonObj = jsonArray.getJSONObject(i)
                    habits.add(
                        TimetableHabit(
                            weekNumber = jsonObj.getInt("week"),
                            phase = jsonObj.getString("phase"),
                            chapter = jsonObj.getString("chapter"),
                            dates = jsonObj.getString("dates"),
                            focus = jsonObj.getString("focus"),
                            tag = jsonObj.getString("tag"),
                            targetMinutes = jsonObj.getInt("targetMinutes")
                        )
                    )
                }

                repository.insertAll(habits)
            }
        }
    }
}
