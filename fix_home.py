import re

with open("app/src/main/java/com/example/ui/screens/HomeDashboardScreen.kt", "r") as f:
    content = f.read()

# Remove the Floating Action Buttons Group (Bottom Start) which contains the Notes button
pattern_to_remove = r"""                // Floating Action Buttons Group \(Bottom Start\)\s*Column\(\s*modifier = Modifier\s*\.align\(Alignment\.BottomStart\)\s*\.padding\(bottom = 24\.dp, start = 24\.dp\),\s*verticalArrangement = Arrangement\.spacedBy\(16\.dp\)\s*\)\s*\{\s*Row\(horizontalArrangement = Arrangement\.spacedBy\(16\.dp\)\)\s*\{\s*FloatingActionButton\(\s*onClick = \{ showNotesSheet = true \},\s*containerColor = viewModel\.themeGlassFill,\s*contentColor = viewModel\.themeText,\s*elevation = FloatingActionButtonDefaults\.elevation\(0\.dp\)\s*\)\s*\{\s*Icon\(Icons\.Default\.Description, contentDescription = "Markdown Notes"\)\s*\}\s*\}\s*\}"""

content = re.sub(pattern_to_remove, "", content)

# Now, put the Document button in a Column with the AI button at BottomEnd
pattern_ai = r"""                // Floating AI Assistant Button\s*FloatingActionButton\(\s*onClick = \{ showChatSheet = true \},\s*containerColor = MaterialTheme\.colorScheme\.primary,\s*contentColor = MaterialTheme\.colorScheme\.onPrimary,\s*modifier = Modifier\s*\.align\(Alignment\.BottomEnd\)\s*\.padding\(bottom = 24\.dp, end = 24\.dp\)\s*\.testTag\("floating_cube_coach_button"\)\s*\)\s*\{\s*Icon\(com\.example\.ui\.components\.CubeIcon, contentDescription = "Gemini", tint = MaterialTheme\.colorScheme\.onPrimary\)\s*\}"""

replacement_ai = """                // Floating Action Buttons Group (Bottom End)
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 24.dp, end = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    FloatingActionButton(
                        onClick = { showNotesSheet = true },
                        containerColor = viewModel.themeGlassFill,
                        contentColor = viewModel.themeText,
                        elevation = FloatingActionButtonDefaults.elevation(0.dp)
                    ) {
                        Icon(Icons.Default.Description, contentDescription = "Markdown Notes")
                    }
                    
                    FloatingActionButton(
                        onClick = { showChatSheet = true },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.testTag("floating_cube_coach_button")
                    ) {
                        Icon(com.example.ui.components.CubeIcon, contentDescription = "Gemini", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                }"""

content = re.sub(pattern_ai, replacement_ai, content)

with open("app/src/main/java/com/example/ui/screens/HomeDashboardScreen.kt", "w") as f:
    f.write(content)
