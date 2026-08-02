import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

# I want to replace the two Boxes used for the background with a single Box utilizing glassmorphismBackgroundEffect
# Specifically:
#         if (viewModel.isBackgroundEnabled) {
#             // Immersive premium glassmorphism background stage with animated floating color blobs
#             LuminaStageBackground(isDarkMode = viewModel.isDarkMode)
# 
#             // Glassmorphism translucent tinting layer
#             Box(
#                 modifier = Modifier
#                     .fillMaxSize()
#                     .background(Color(0xFF141419).copy(alpha = 0.9f))
#             )
#         }

pattern = r'if\s*\(viewModel\.isBackgroundEnabled\)\s*\{\s*// Immersive[\s\S]*?LuminaStageBackground\(isDarkMode\s*=\s*viewModel\.isDarkMode\)[\s\S]*?// Glassmorphism translucent tinting layer\s*Box\(\s*modifier\s*=\s*Modifier[\s\S]*?\.fillMaxSize\(\)[\s\S]*?\.background\([\s\S]*?\)\s*\)\s*\}'

replacement = """if (viewModel.isBackgroundEnabled) {
            // Immersive premium glassmorphism background stage with animated floating color blobs and tinting layer
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .com.example.ui.components.glassmorphismBackgroundEffect(isDarkMode = viewModel.isDarkMode)
            ) {
                LuminaStageBackground(isDarkMode = viewModel.isDarkMode)
            }
        }"""

new_content = re.sub(pattern, replacement, content)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(new_content)
