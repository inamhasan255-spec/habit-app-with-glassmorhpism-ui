files = [
    "app/src/main/java/com/example/ui/screens/MarkdownNotesFeature.kt",
    "app/src/main/java/com/focuscube/ui/dashboard/DashboardScreen.kt"
]

for path in files:
    with open(path, 'r') as f:
        content = f.read()
    
    # Remove the bad import
    content = content.replace('import com.example.ui.components.GlassToggle\n', '', 1)
    
    # Insert it properly after package
    import_statement = "import com.example.ui.components.GlassToggle\n"
    
    if "package " in content:
        # Find end of package statement
        idx = content.find('\n', content.find("package "))
        if idx != -1:
            content = content[:idx+1] + import_statement + content[idx+1:]
        else:
            content = import_statement + content
    else:
        content = import_statement + content
        
    with open(path, 'w') as f:
        f.write(content)
