import re

with open("app/src/main/java/com/example/ui/screens/HomeDashboardScreen.kt", "r") as f:
    content = f.read()

# Wrap AnimatedContent in SharedTransitionLayout
pattern = r"""\s*AnimatedContent\(\s*targetState = currentMode,\s*transitionSpec = \{([\s\S]*?)label = "mode_transitions",\s*modifier = Modifier\.fillMaxSize\(\)\s*\) \{ mode ->"""

replacement = """
                androidx.compose.animation.SharedTransitionLayout {
                    AnimatedContent(
                        targetState = currentMode,
                        transitionSpec = {\\1label = "mode_transitions",
                        modifier = Modifier.fillMaxSize()
                    ) { mode ->"""

content = re.sub(pattern, replacement, content)

# Add closing brace for SharedTransitionLayout after AnimatedContent
# The AnimatedContent is inside a Box that ends shortly after.
# Wait, it's easier to find:
#                     }
#                 }
#                 // Floating Action Buttons Group (Bottom End)

pattern2 = r"""\s*\}\s*\}\s*// Floating Action Buttons Group \(Bottom End\)"""

replacement2 = """
                    }
                }
                }
                // Floating Action Buttons Group (Bottom End)"""

content = re.sub(pattern2, replacement2, content)

# Also add OptIn
if "import androidx.compose.animation.ExperimentalSharedTransitionApi" not in content:
    content = content.replace("import androidx.compose.animation.AnimatedContent", "import androidx.compose.animation.AnimatedContent\nimport androidx.compose.animation.ExperimentalSharedTransitionApi\nimport androidx.compose.animation.SharedTransitionLayout")

content = content.replace("fun HomeDashboardScreen(viewModel: FocusViewModel) {", "@OptIn(ExperimentalSharedTransitionApi::class)\n@Composable\nfun HomeDashboardScreen(viewModel: FocusViewModel) {")

# Clean up duplicate @Composable if any
content = content.replace("@Composable\n@OptIn", "@OptIn")
content = content.replace("@Composable\n@Composable", "@Composable")

with open("app/src/main/java/com/example/ui/screens/HomeDashboardScreen.kt", "w") as f:
    f.write(content)
