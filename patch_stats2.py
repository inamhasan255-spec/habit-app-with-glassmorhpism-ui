with open("app/src/main/java/com/example/ui/screens/StatsScreen.kt", "r") as f:
    content = f.read()

content = content.replace('listOf("Habits", "Focus", "Badges", "Rank")', 'listOf("Habits", "Focus", "Shield", "Awards", "Rank")')

content = content.replace("""                1 -> FocusStatsTab(sessionsList, accentColor, viewModel)
                2 -> AchievementsStatsTab(viewModel)
                3 -> LeaderboardStatsTab(viewModel)""",
"""                1 -> FocusStatsTab(sessionsList, accentColor, viewModel)
                2 -> ShieldStatsTab(blockedAppsList, accentColor, viewModel)
                3 -> AchievementsStatsTab(viewModel)
                4 -> LeaderboardStatsTab(viewModel)""")

with open("app/src/main/java/com/example/ui/screens/StatsScreen.kt", "w") as f:
    f.write(content)
