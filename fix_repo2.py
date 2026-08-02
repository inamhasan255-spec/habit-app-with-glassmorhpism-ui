with open("app/src/main/java/com/example/data/FocusRepository.kt", "r") as f:
    text = f.read()

text = text.replace("private val badgeDao = database.badgeDao()", 
"val badgeDao = database.badgeDao()")

with open("app/src/main/java/com/example/data/FocusRepository.kt", "w") as f:
    f.write(text)
