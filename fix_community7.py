with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    text = f.read()

text = text.replace("import androidx.compose.material.icons.filled.Groups", "import androidx.compose.material.icons.filled.Person")
with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(text)

with open("app/src/main/java/com/example/ui/screens/CommunityScreen.kt", "r") as f:
    text = f.read()

text = text.replace("progressHours = selfHours", "progressHours = selfHours.toFloat()")
with open("app/src/main/java/com/example/ui/screens/CommunityScreen.kt", "w") as f:
    f.write(text)
