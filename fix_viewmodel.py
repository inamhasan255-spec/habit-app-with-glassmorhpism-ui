with open("app/src/main/java/com/example/data/FocusViewModel.kt", "r") as f:
    text = f.read()

text = text.replace("private val repository: FocusRepository", "val repository: FocusRepository")

with open("app/src/main/java/com/example/data/FocusViewModel.kt", "w") as f:
    f.write(text)
