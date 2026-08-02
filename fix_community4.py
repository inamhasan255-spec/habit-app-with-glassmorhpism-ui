with open("app/src/main/java/com/example/ui/screens/CommunityScreen.kt", "r") as f:
    text = f.read()

# Fix the import statement that I added to the top of the file incorrectly
text = text.replace("import androidx.compose.runtime.collectAsState\npackage com.example.ui.screens", "package com.example.ui.screens\n\nimport androidx.compose.runtime.collectAsState")

text = text.replace("viewModel.themeAccent", "androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(viewModel.accentColorHex))")

with open("app/src/main/java/com/example/ui/screens/CommunityScreen.kt", "w") as f:
    f.write(text)
