with open("app/src/main/java/com/example/ui/screens/StatsScreen.kt", "r") as f:
    content = f.read()

content = content.replace('listOf("Habits", "Focus", "Shield", "Awards")', 'listOf("Habits", "Focus", "Badges", "Rank")')

content = content.replace("""                1 -> FocusStatsTab(sessionsList, accentColor, viewModel)
                2 -> ShieldStatsTab(blockedAppsList, accentColor, viewModel)
                3 -> AchievementsStatsTab(viewModel)""",
"""                1 -> FocusStatsTab(sessionsList, accentColor, viewModel)
                2 -> AchievementsStatsTab(viewModel)
                3 -> LeaderboardStatsTab(viewModel)""")

content = content.replace("import androidx.compose.ui.text.font.FontWeight", "import androidx.compose.ui.text.font.FontWeight\nimport android.content.Intent\nimport androidx.compose.ui.platform.LocalContext\nimport androidx.core.content.ContextCompat.startActivity")

with open("app/src/main/java/com/example/ui/screens/StatsScreen.kt", "w") as f:
    f.write(content)
