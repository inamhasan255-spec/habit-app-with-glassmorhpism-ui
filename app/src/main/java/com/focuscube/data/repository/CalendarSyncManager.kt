package com.focuscube.data.repository

import android.content.Context
import android.database.Cursor
import android.provider.CalendarContract
import java.util.Calendar

data class CalendarEvent(
    val id: Long,
    val title: String,
    val startTime: Long,
    val endTime: Long,
    val color: Int,
    val accountName: String
)

class CalendarSyncManager(private val context: Context) {
    fun getUpcomingEvents(daysAhead: Int = 7): List<CalendarEvent> {
        val events = mutableListOf<CalendarEvent>()
        val projection = arrayOf(
            CalendarContract.Events._ID,
            CalendarContract.Events.TITLE,
            CalendarContract.Events.DTSTART,
            CalendarContract.Events.DTEND,
            CalendarContract.Events.DISPLAY_COLOR,
            CalendarContract.Events.ACCOUNT_NAME
        )
        
        val now = Calendar.getInstance().timeInMillis
        val future = now + (daysAhead * 24 * 60 * 60 * 1000L)
        
        val selection = "${CalendarContract.Events.DTSTART} >= ? AND ${CalendarContract.Events.DTSTART} <= ?"
        val selectionArgs = arrayOf(now.toString(), future.toString())
        
        try {
            val resolver = context.contentResolver
            val cursor: Cursor? = resolver.query(
                CalendarContract.Events.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                "${CalendarContract.Events.DTSTART} ASC"
            )
            
            cursor?.use {
                val idIdx = it.getColumnIndexOrThrow(CalendarContract.Events._ID)
                val titleIdx = it.getColumnIndexOrThrow(CalendarContract.Events.TITLE)
                val startIdx = it.getColumnIndexOrThrow(CalendarContract.Events.DTSTART)
                val endIdx = it.getColumnIndexOrThrow(CalendarContract.Events.DTEND)
                val colorIdx = it.getColumnIndexOrThrow(CalendarContract.Events.DISPLAY_COLOR)
                val accountIdx = it.getColumnIndexOrThrow(CalendarContract.Events.ACCOUNT_NAME)
                
                while (it.moveToNext()) {
                    events.add(
                        CalendarEvent(
                            id = it.getLong(idIdx),
                            title = it.getString(titleIdx) ?: "Untitled Event",
                            startTime = it.getLong(startIdx),
                            endTime = it.getLong(endIdx),
                            color = it.getInt(colorIdx),
                            accountName = it.getString(accountIdx) ?: ""
                        )
                    )
                }
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
        
        return events
    }
}
