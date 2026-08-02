import re

with open('app/src/main/java/com/example/ui/screens/SettingsScreen.kt', 'r') as f:
    content = f.read()

# Replace GlassToggle calls with double trailing parens
content = re.sub(r'checkedTrackColor = accentColor\s*\n\s*\)\s*\n\s*\)', r'checkedTrackColor = accentColor\n                    )', content)

with open('app/src/main/java/com/example/ui/screens/SettingsScreen.kt', 'w') as f:
    f.write(content)
