import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

content = content.replace("import com.example.ui.theme.LuminaThemeProvider", "import com.example.ui.theme.LuminaThemeProvider\nimport com.example.ui.components.glassmorphismBackgroundEffect")
content = content.replace(".com.example.ui.components.glassmorphismBackgroundEffect", ".glassmorphismBackgroundEffect")

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
