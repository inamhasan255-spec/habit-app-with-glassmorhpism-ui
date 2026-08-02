with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

bad_string = """
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF141419).copy(alpha = 0.9f))
                    .border(1.dp, Color.White.copy(alpha = 0.08f))"""

# Wait, the indentation might vary. Let's use regex.
import re

pattern = r'Box\(\n\s*modifier = Modifier\n\s*\.fillMaxSize\(\)\n\s*\.background\(Color\(0xFF141419\)\.copy\(alpha = 0\.9f\)\)\n\s*\.border\(1\.dp, Color\.White\.copy\(alpha = 0\.08f\)\)'

content = re.sub(pattern, 'Box(', content)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
