import re

with open("app/src/main/java/com/example/data/FocusViewModel.kt", "r") as f:
    content = f.read()

bad_funcs_in_enum = """    fun addPoints(points: Int) {
        viewModelScope.launch {
            val stats = repository.userStats.first() ?: UserStats()
            val newPoints = stats.points + points
            val newLevel = (newPoints / 100) + 1
            repository.updateUserStats(stats.copy(points = newPoints, level = newLevel))
            checkBadges(newPoints)
        }
    }

    private suspend fun checkBadges(currentPoints: Int) {
        val badges = repository.allBadges.first()
        if (currentPoints >= 1000) {
            val masterBadge = badges.find { it.id == "master_focus" }
            if (masterBadge?.unlocked == false) {
                repository.updateBadge(masterBadge.copy(unlocked = true, unlockedTimestamp = System.currentTimeMillis()))
            }
        }
    }

    fun checkStreakBadges(streak: Int) {
        viewModelScope.launch {
            val badges = repository.allBadges.first()
            if (streak >= 3) {
                val badge3 = badges.find { it.id == "streak_3" }
                if (badge3?.unlocked == false) {
                    repository.updateBadge(badge3.copy(unlocked = true, unlockedTimestamp = System.currentTimeMillis()))
                }
            }
            if (streak >= 10) {
                val badge10 = badges.find { it.id == "streak_10" }
                if (badge10?.unlocked == false) {
                    repository.updateBadge(badge10.copy(unlocked = true, unlockedTimestamp = System.currentTimeMillis()))
                }
            }
        }
    }
"""

content = content.replace(bad_funcs_in_enum, "")

with open("app/src/main/java/com/example/data/FocusViewModel.kt", "w") as f:
    f.write(content)
