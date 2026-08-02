with open("app/src/main/java/com/example/data/FocusDatabase.kt", "r") as f:
    content = f.read()

content = content.replace(
    "FocusSession::class",
    "FocusSession::class,\n        UserStats::class,\n        Badge::class"
).replace(
    "version = 8,",
    "version = 9,"
).replace(
    "abstract fun focusSessionDao(): FocusSessionDao",
    "abstract fun focusSessionDao(): FocusSessionDao\n    abstract fun userStatsDao(): UserStatsDao\n    abstract fun badgeDao(): BadgeDao"
)

with open("app/src/main/java/com/example/data/FocusDatabase.kt", "w") as f:
    f.write(content)
