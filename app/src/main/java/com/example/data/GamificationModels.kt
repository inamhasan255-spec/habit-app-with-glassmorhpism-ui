package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_stats")
data class UserStats(
    @PrimaryKey val id: Int = 1,
    val points: Int = 0,
    val level: Int = 1,
    val currentStreak: Int = 0,
    val highestStreak: Int = 0
)

@Entity(tableName = "badges")
data class Badge(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val iconResName: String,
    val unlocked: Boolean = false,
    val unlockedTimestamp: Long? = null
)

// For Leaderboard (Mocked data, but could be connected to backend)
data class LeaderboardEntry(
    val rank: Int,
    val userName: String,
    val points: Int,
    val isCurrentUser: Boolean = false,
    val avatarUrl: String? = null
)
