import re

with open("app/src/main/java/com/example/data/FocusDatabase.kt", "r") as f:
    text = f.read()

print("communityGroupDao" in text)
print("communityMemberDao" in text)
