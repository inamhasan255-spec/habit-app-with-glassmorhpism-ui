with open("app/src/main/java/com/example/ui/screens/MarkdownNotesFeature.kt", "r") as f:
    text = f.read()

text = text.replace("""                GlassToggle(
                    checked = isPlaying,
                    onCheckedChange = { viewModel.toggleWhiteNoisePlayingAnytime() },
                    checkedTrackColor = accentColor
                )
            
            val sounds""", """                GlassToggle(
                    checked = isPlaying,
                    onCheckedChange = { viewModel.toggleWhiteNoisePlayingAnytime() },
                    checkedTrackColor = accentColor
                )
            }
            
            val sounds""")

with open("app/src/main/java/com/example/ui/screens/MarkdownNotesFeature.kt", "w") as f:
    f.write(text)
