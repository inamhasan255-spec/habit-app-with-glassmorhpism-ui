with open("app/src/main/java/com/example/ui/screens/HabitsScreen.kt", "r") as f:
    content = f.read()

share_icon = """
                                if (habit.streakCount > 0) {
                                    Icon(
                                        imageVector = androidx.compose.material.icons.Icons.Default.Share,
                                        contentDescription = "Share",
                                        tint = accentColor,
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clickable {
                                                val shareIntent = Intent().apply {
                                                    action = Intent.ACTION_SEND
                                                    putExtra(Intent.EXTRA_TEXT, "I'm on a ${habit.streakCount}-day streak for my habit '${habit.name}'! Building better habits with FocusCube! 🔥 #FocusCube #HabitTracker")
                                                    type = "text/plain"
                                                }
                                                context.startActivity(Intent.createChooser(shareIntent, "Share Habit Streak"))
                                            }
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                }
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = viewModel.themeTextSecondary.copy(0.7f),
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clickable { habitToDelete = habit }
                                )"""

content = content.replace("""                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = viewModel.themeTextSecondary.copy(0.7f),
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clickable { habitToDelete = habit }
                                )""", share_icon)

if "import androidx.compose.material.icons.filled.Share" not in content:
    content = content.replace("import androidx.compose.material.icons.filled.Link", "import androidx.compose.material.icons.filled.Link\nimport androidx.compose.material.icons.filled.Share")

with open("app/src/main/java/com/example/ui/screens/HabitsScreen.kt", "w") as f:
    f.write(content)
