with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

content = content.replace(
    ".align(Alignment.BottomEnd)\n                .navigationBarsPadding()\n                .padding(bottom = 82.dp, end = 12.dp)",
    ".align(Alignment.BottomStart)\n                .navigationBarsPadding()\n                .padding(bottom = 82.dp, start = 12.dp)"
)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
