with open("app/src/main/java/com/example/ui/screens/MarkdownNotesFeature.kt", "r") as f:
    text = f.read()

text = text.replace("""                GlassToggle(
                    checked = isPlaying,
                    onCheckedChange = { viewModel.toggleWhiteNoisePlayingAnytime() },
                     accentColor = accentColor)""", """                GlassToggle(
                    checked = isPlaying,
                    onCheckedChange = { viewModel.toggleWhiteNoisePlayingAnytime() },
                    checkedTrackColor = accentColor
                )""")

text = "import com.example.ui.components.GlassToggle\n" + text

with open("app/src/main/java/com/example/ui/screens/MarkdownNotesFeature.kt", "w") as f:
    f.write(text)
