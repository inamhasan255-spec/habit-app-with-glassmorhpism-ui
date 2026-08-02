import re

files = [
    "app/src/main/java/com/example/ui/screens/MarkdownNotesFeature.kt",
    "app/src/main/java/com/focuscube/ui/dashboard/DashboardScreen.kt"
]

for path in files:
    with open(path, 'r') as f:
        content = f.read()
    
    content = content.replace("GlassTogglepackage", "GlassToggle\npackage ")
    content = content.replace("screensimport", "screens\nimport ")
    content = content.replace("dashboardimport", "dashboard\nimport ")
    
    # Fix remaining ones like .layout.*import
    content = re.sub(r'([a-zA-Z0-9_*>])import ', r'\1\nimport ', content)
    
    with open(path, 'w') as f:
        f.write(content)
