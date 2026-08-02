with open("app/src/main/java/com/focuscube/ui/dashboard/DashboardScreen.kt", "r") as f:
    text = f.read()

text = text.replace("""                            GlassToggle(
                                checked = isLocked,
                                onCheckedChange = { viewModel.toggleSessionLock() },
                                thumbContent = {
                                    Icon(
                                        imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                        contentDescription = null,
                                        modifier = Modifier.size(SwitchDefaults.IconSize)
                                    )
                                }
                            )""", """                            GlassToggle(
                                checked = isLocked,
                                onCheckedChange = { viewModel.toggleSessionLock() }
                            )""")

text = "import com.example.ui.components.GlassToggle\n" + text

with open("app/src/main/java/com/focuscube/ui/dashboard/DashboardScreen.kt", "w") as f:
    f.write(text)
