with open("app/src/main/java/com/example/data/FocusRepository.kt", "r") as f:
    text = f.read()

# Remove the closing brace at the old location
text = text.replace("    suspend fun deleteFocusSession(session: FocusSession) = focusSessionDao.deleteFocusSession(session)\n}", "    suspend fun deleteFocusSession(session: FocusSession) = focusSessionDao.deleteFocusSession(session)")

# Add it at the end
if not text.rstrip().endswith("}"):
    text = text.rstrip() + "\n}\n"
    
with open("app/src/main/java/com/example/data/FocusRepository.kt", "w") as f:
    f.write(text)
