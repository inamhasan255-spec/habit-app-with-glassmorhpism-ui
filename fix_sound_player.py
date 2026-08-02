with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

content = content.replace(
    ".padding(bottom = 82.dp, start = 12.dp)",
    ".padding(bottom = 24.dp, start = 24.dp)"
)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
