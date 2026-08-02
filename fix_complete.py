import re

with open("app/src/main/java/com/example/ui/screens/FocusCompleteScreen.kt", "r") as f:
    content = f.read()

content = content.replace("                    isDarkMode = viewModel.isDarkMode\n", "")
content = content.replace("                    isDarkMode = viewModel.isDarkMode", "")

with open("app/src/main/java/com/example/ui/screens/FocusCompleteScreen.kt", "w") as f:
    f.write(content)
