with open("app/src/main/java/com/example/data/FocusRepository.kt", "r") as f:
    content = f.read()

content = content.replace(
    "private val focusSessionDao = database.focusSessionDao()",
    """private val focusSessionDao = database.focusSessionDao()
    private val userStatsDao = database.userStatsDao()
    private val badgeDao = database.badgeDao()"""
)

content = content.replace(
    "val allFocusSessions: Flow<List<FocusSession>> = focusSessionDao.getAllFocusSessions()",
    """val allFocusSessions: Flow<List<FocusSession>> = focusSessionDao.getAllFocusSessions()

    val userStats: Flow<UserStats?> = userStatsDao.getUserStats()
    val allBadges: Flow<List<Badge>> = badgeDao.getAllBadges()

    suspend fun insertUserStats(stats: UserStats) {
        userStatsDao.insertUserStats(stats)
    }

    suspend fun updateUserStats(stats: UserStats) {
        userStatsDao.updateUserStats(stats)
    }

    suspend fun insertBadges(badges: List<Badge>) {
        badgeDao.insertBadges(badges)
    }

    suspend fun updateBadge(badge: Badge) {
        badgeDao.updateBadge(badge)
    }
"""
)

with open("app/src/main/java/com/example/data/FocusRepository.kt", "w") as f:
    f.write(content)
