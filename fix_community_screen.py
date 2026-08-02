with open("app/src/main/java/com/example/ui/screens/CommunityScreen.kt", "r") as f:
    text = f.read()

text = text.replace("import kotlinx.coroutines.flow.Flow\npackage com.example.ui.screens", "package com.example.ui.screens\n\nimport kotlinx.coroutines.flow.Flow")
with open("app/src/main/java/com/example/ui/screens/CommunityScreen.kt", "w") as f:
    f.write(text)
