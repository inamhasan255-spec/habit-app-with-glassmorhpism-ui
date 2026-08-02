with open('app/src/main/java/com/example/ui/screens/SettingsScreen.kt', 'r') as f:
    content = f.read()

replacement = """                com.example.ui.components.RainbowButton(
                    onClick = {
                        viewModel.replayOnboarding()
                    },
                    modifier = Modifier.fillMaxWidth().testTag("replay_tutorial_button")
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Replay")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "REPLAY TUTORIAL", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }"""

old = """                com.example.ui.components.LiquidMetalButton(
                    onClick = {
                        viewModel.replayOnboarding()
                    },
                    modifier = Modifier.fillMaxWidth().testTag("replay_tutorial_button")
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Replay")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "REPLAY TUTORIAL", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }"""

content = content.replace(old, replacement)

with open('app/src/main/java/com/example/ui/screens/SettingsScreen.kt', 'w') as f:
    f.write(content)
