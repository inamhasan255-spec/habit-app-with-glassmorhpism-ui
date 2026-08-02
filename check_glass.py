import re
with open('app/src/main/java/com/example/ui/screens/HomeDashboardScreen.kt', 'r') as f:
    content = f.read()

for i, line in enumerate(content.split('\n')):
    if 'GlassContainer' in line:
        print(f"Line {i+1}: {line}")
