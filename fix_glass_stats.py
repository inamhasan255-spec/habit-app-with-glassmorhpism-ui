import re

with open('app/src/main/java/com/example/ui/screens/StatsScreen.kt', 'r') as f:
    content = f.read()

pattern = re.compile(
    r'Box\(\s*modifier\s*=\s*Modifier\s*(.*?)\.clip\(RoundedCornerShape\(([\d]+)\.dp\)\)\s*(?:\.background\(viewModel\.themeGlassFill\)\s*\.border\(1\.dp,\s*viewModel\.themeGlassBorder,\s*RoundedCornerShape\(\2\.dp\)\)|\.border\(1\.dp,\s*viewModel\.themeGlassBorder,\s*RoundedCornerShape\(\2\.dp\)\)\s*\.background\(viewModel\.themeGlassFill\))\s*\.padding\((.*?)\)\s*\)\s*\{',
    re.DOTALL
)

def replacer(match):
    pre_modifiers = match.group(1).strip()
    radius = match.group(2)
    padding = match.group(3)
    
    if pre_modifiers and not pre_modifiers.endswith('\n'):
        pre_modifiers += '\n            '
        
    return f"""com.example.ui.components.GlassContainer(
        modifier = Modifier
            {pre_modifiers}.padding({padding}),
        backgroundColor = viewModel.themeGlassFill,
        borderColor = viewModel.themeGlassBorder,
        shape = RoundedCornerShape({radius}.dp)
    ) {{"""

new_content = pattern.sub(replacer, content)

with open('app/src/main/java/com/example/ui/screens/StatsScreen.kt', 'w') as f:
    f.write(new_content)

