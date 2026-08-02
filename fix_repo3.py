with open("app/src/main/java/com/example/data/FocusRepository.kt", "r") as f:
    text = f.read()

# Make sure dao properties are actually public
text = text.replace("private val communityGroupDao = database.communityGroupDao()", "val communityGroupDao = database.communityGroupDao()")
text = text.replace("private val communityMemberDao = database.communityMemberDao()", "val communityMemberDao = database.communityMemberDao()")

with open("app/src/main/java/com/example/data/FocusRepository.kt", "w") as f:
    f.write(text)
