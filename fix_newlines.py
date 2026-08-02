def fix_file(path):
    with open(path, 'r') as f:
        content = f.read()
    
    # Replace the mangled top
    if 'GlassTogglepackage' in content:
        content = content.replace('GlassTogglepackage', 'GlassToggle\npackage')
    
    # Also add newlines before all imports that are jammed together
    import re
    content = re.sub(r'(import [a-zA-Z0-9_.*]+)(import )', r'\1\n\2', content)
    content = re.sub(r'(import [a-zA-Z0-9_.*]+)(import )', r'\1\n\2', content)
    content = re.sub(r'(import [a-zA-Z0-9_.*]+)(import )', r'\1\n\2', content)
    content = re.sub(r'(import [a-zA-Z0-9_.*]+)(import )', r'\1\n\2', content)
    content = re.sub(r'(import [a-zA-Z0-9_.*]+)(import )', r'\1\n\2', content)
    content = re.sub(r'(import [a-zA-Z0-9_.*]+)(import )', r'\1\n\2', content)
    content = re.sub(r'(import [a-zA-Z0-9_.*]+)(import )', r'\1\n\2', content)
    content = re.sub(r'(import [a-zA-Z0-9_.*]+)(import )', r'\1\n\2', content)
    content = re.sub(r'(import [a-zA-Z0-9_.*]+)(import )', r'\1\n\2', content)
    content = re.sub(r'(import [a-zA-Z0-9_.*]+)(import )', r'\1\n\2', content)
    content = re.sub(r'(import [a-zA-Z0-9_.*]+)(import )', r'\1\n\2', content)
    content = re.sub(r'(import [a-zA-Z0-9_.*]+)(import )', r'\1\n\2', content)
    content = re.sub(r'(import [a-zA-Z0-9_.*]+)(import )', r'\1\n\2', content)
    content = re.sub(r'(import [a-zA-Z0-9_.*]+)(import )', r'\1\n\2', content)
    content = re.sub(r'(import [a-zA-Z0-9_.*]+)(import )', r'\1\n\2', content)
    content = re.sub(r'(import [a-zA-Z0-9_.*]+)(import )', r'\1\n\2', content)
    content = re.sub(r'(import [a-zA-Z0-9_.*]+)(import )', r'\1\n\2', content)

    # Any remaining ones
    while 'import' in content.replace('import ', 'I_M_P_O_R_T '):
        # We can just do a general regex for jammed imports
        new_content = re.sub(r'([a-zA-Z0-9_*])(import )', r'\1\n\2', content)
        if new_content == content:
            break
        content = new_content
        
    with open(path, 'w') as f:
        f.write(content)

fix_file("app/src/main/java/com/focuscube/ui/dashboard/DashboardScreen.kt")
fix_file("app/src/main/java/com/example/ui/screens/MarkdownNotesFeature.kt")
