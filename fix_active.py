with open("app/src/main/java/com/example/ui/screens/FocusActiveScreen.kt", "r") as f:
    text = f.read()

text = text.replace("""                    GlassToggle(
                        checked = viewModel.isWhiteNoiseEnabled,
                        onCheckedChange = { viewModel.toggleWhiteNoise() },
                        
                            uncheckedBorderColor = Color.Transparent
                        ),
                        modifier = Modifier.scale(0.8f)
                    )""", """                    GlassToggle(
                        checked = viewModel.isWhiteNoiseEnabled,
                        onCheckedChange = { viewModel.toggleWhiteNoise() },
                        modifier = Modifier.scale(0.8f)
                    )""")

with open("app/src/main/java/com/example/ui/screens/FocusActiveScreen.kt", "w") as f:
    f.write(text)
