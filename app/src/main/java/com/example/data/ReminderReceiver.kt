package com.example.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * BroadcastReceiver to receive scheduled habit reminder alarms
 * and trigger notification delivery.
 */
class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val habitId = intent.getIntExtra("habit_id", 0)
        val habitName = intent.getStringExtra("habit_name") ?: "Your Habit"
        
        FocusNotificationManager.showHabitReminderNotification(
            context = context,
            habitId = habitId,
            habitName = habitName
        )
    }
}
