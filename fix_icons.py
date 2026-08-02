with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    text = f.read()

text = text.replace("Icons.Default.AccountCircle", "Icons.Default.Person")
text = text.replace("import androidx.compose.material.icons.filled.Home", "import androidx.compose.material.icons.filled.Home\nimport androidx.compose.material.icons.filled.Person")

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(text)
