import re

with open("app/src/main/java/com/example/data/FocusViewModel.kt", "r") as f:
    content = f.read()

bad_block = """    val userStats = repository.userStats.stateIn(viewModelScope, SharingStarted.Lazily, null)
    val allBadges = repository.allBadges.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        viewModelScope.launch {
            // Initialize basic stats if not present
            if (repository.userStats.first() == null) {
                repository.insertUserStats(UserStats())
            }
            // Initialize badges
            val initialBadges = listOf(
                Badge("streak_3", "3-Day Streak", "Complete a habit for 3 consecutive days.", "🔥"),
                Badge("streak_10", "10-Day Streak", "Complete a habit for 10 consecutive days.", "🌟"),
                Badge("focus_novice", "Focus Novice", "Complete 5 focus sessions.", "⏱️"),
                Badge("master_focus", "Master of Focus", "Accumulate 1000 focus points.", "👑")
            )
            repository.insertBadges(initialBadges)
        }
    }

    fun addPoints(points: Int) {
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

content = content.replace(bad_block, "")

# Now add declarations to the fields section
decl = """    // Gamification
    val userStats: StateFlow<UserStats?>
    val allBadges: StateFlow<List<Badge>>

    val journalEntries: StateFlow<List<JournalEntry>>"""

content = content.replace("    val journalEntries: StateFlow<List<JournalEntry>>", decl)

# Now add initialization in init { } after repository = FocusRepository(database)
init_logic = """        repository = FocusRepository(database)

        userStats = repository.userStats.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = null
        )
        allBadges = repository.allBadges.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = emptyList()
        )

        viewModelScope.launch {
            // Initialize basic stats if not present
            if (repository.userStats.first() == null) {
                repository.insertUserStats(UserStats())
            }
            // Initialize badges
            val initialBadges = listOf(
                Badge("streak_3", "3-Day Streak", "Complete a habit for 3 consecutive days.", "🔥"),
                Badge("streak_10", "10-Day Streak", "Complete a habit for 10 consecutive days.", "🌟"),
                Badge("focus_novice", "Focus Novice", "Complete 5 focus sessions.", "⏱️"),
                Badge("master_focus", "Master of Focus", "Accumulate 1000 focus points.", "👑")
            )
            repository.insertBadges(initialBadges)
        }"""

content = content.replace("        repository = FocusRepository(database)", init_logic)

# Now add the functions at the end of the class
funcs = """

    fun addPoints(points: Int) {
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
}"""

content = content.replace("}\n", funcs, 1) # Note, this might match the wrong closing brace. 
# Better to replace at the very end.
content = re.sub(r"\}\s*\Z", funcs, content)

with open("app/src/main/java/com/example/data/FocusViewModel.kt", "w") as f:
    f.write(content)
