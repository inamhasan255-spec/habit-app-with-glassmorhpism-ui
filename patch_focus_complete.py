with open("app/src/main/java/com/example/ui/screens/FocusCompleteScreen.kt", "r") as f:
    content = f.read()

# Import LocalContext and Intent
if "import androidx.compose.ui.platform.LocalContext" not in content:
    content = content.replace("import androidx.compose.ui.Alignment", "import androidx.compose.ui.Alignment\nimport androidx.compose.ui.platform.LocalContext\nimport android.content.Intent\nimport androidx.core.content.ContextCompat.startActivity")
if "import androidx.compose.material.icons.filled.Share" not in content:
    content = content.replace("import androidx.compose.material.icons.filled.CheckCircle", "import androidx.compose.material.icons.filled.CheckCircle\nimport androidx.compose.material.icons.filled.Share")

share_button = """            val context = LocalContext.current

            // Save & Continue CTA
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                com.example.ui.components.LiquidMetalButton(
                    onClick = {
                        val shareIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "I just completed a ${viewModel.timerTotalMinutes}-minute focus session on FocusCube! Current Streak: ${viewModel.getStreakCount()}🔥 #FocusCube #Productivity")
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Session"))
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(16.dp))
                        Text(
                            text = "SHARE WORKOUT",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }

                com.example.ui.components.GlassButton(
                    onClick = { viewModel.completeAndSaveSession() },
                    modifier = Modifier.fillMaxWidth(),
                    isDarkMode = viewModel.isDarkMode
                ) {
                    Text(
                        text = "SAVE & DISMISS CUBE",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = viewModel.themeText
                    )
                }
            }"""

content = content.replace("""            // Save & Continue CTA
            com.example.ui.components.LiquidMetalButton(
                onClick = { viewModel.completeAndSaveSession() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                Text(
                    text = "SAVE & DISMISS CUBE",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }""", share_button)

with open("app/src/main/java/com/example/ui/screens/FocusCompleteScreen.kt", "w") as f:
    f.write(content)
