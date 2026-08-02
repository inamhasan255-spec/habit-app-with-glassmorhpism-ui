import re

with open("app/src/main/java/com/focuscube/ui/dashboard/DashboardScreen.kt", "r") as f:
    text = f.read()

# Replace all mangled tops
text = re.sub(r'import com.example.ui.components.GlassTogglepackage com.focuscube.ui.dashboard', 
              r'package com.focuscube.ui.dashboard\nimport com.example.ui.components.GlassToggle\n', text)
              
# The issue is all newlines got removed in DashboardScreen.kt somehow!?
