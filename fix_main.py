with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

replacement = """
        if (viewModel.isBackgroundEnabled) {
            if (viewModel.backgroundImageUrl.isNotEmpty()) {
                coil.compose.AsyncImage(
                    model = viewModel.backgroundImageUrl,
                    contentDescription = "Background",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            if (viewModel.isDarkMode) androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.5f)
                            else androidx.compose.ui.graphics.Color.White.copy(alpha = 0.2f)
                        )
                )
            } else {
                // Immersive premium glassmorphism background stage with animated floating color blobs and tinting layer
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .glassmorphismBackgroundEffect(isDarkMode = viewModel.isDarkMode)
                ) {
                    LuminaStageBackground(isDarkMode = viewModel.isDarkMode)
                }
            }
        }
"""

content = content.replace("""
        if (viewModel.isBackgroundEnabled) {
            // Immersive premium glassmorphism background stage with animated floating color blobs and tinting layer
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .glassmorphismBackgroundEffect(isDarkMode = viewModel.isDarkMode)
            ) {
                LuminaStageBackground(isDarkMode = viewModel.isDarkMode)
            }
        }""", replacement.strip('\n'))

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
