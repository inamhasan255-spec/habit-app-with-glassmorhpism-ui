import os
import re

files_to_check = [
    "app/src/main/java/com/focuscube/ui/dashboard/DashboardScreen.kt",
    "app/src/main/java/com/example/ui/screens/SettingsScreen.kt",
    "app/src/main/java/com/example/ui/screens/FocusShieldScreen.kt",
    "app/src/main/java/com/example/ui/screens/FocusActiveScreen.kt",
    "app/src/main/java/com/example/ui/screens/AuthScreen.kt",
    "app/src/main/java/com/example/ui/screens/MarkdownNotesFeature.kt"
]

for file_path in files_to_check:
    if not os.path.exists(file_path):
        continue
        
    with open(file_path, 'r') as f:
        content = f.read()
        
    # Replace Switch( with GlassToggle(
    content = content.replace("Switch(", "GlassToggle(")
    
    # We might need to remove colors = SwitchDefaults.colors(...)
    # Let's do this carefully with regex
    # Match colors = SwitchDefaults.colors(...)
    # It might span multiple lines.
    pattern = r"colors\s*=\s*SwitchDefaults\.colors\(([^)]*)\)"
    content = re.sub(pattern, r"/* colors omitted for GlassToggle */", content)
    
    with open(file_path, 'w') as f:
        f.write(content)
