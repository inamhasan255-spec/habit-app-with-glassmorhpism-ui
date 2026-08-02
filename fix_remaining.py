with open('app/src/main/java/com/example/ui/screens/HomeDashboardScreen.kt', 'r') as f:
    content = f.read()

import re
# Find all GlassContainer that haven't been reverted
pattern = re.compile(
    r'com\.example\.ui\.components\.GlassContainer\(\s*modifier\s*=\s*Modifier\s*(.*?)backgroundColor\s*=\s*viewModel\.themeGlassFill,\s*borderColor\s*=\s*viewModel\.themeGlassBorder,\s*shape\s*=\s*RoundedCornerShape\(([\d]+)\.dp\)\s*\)\s*\{',
    re.DOTALL
)

def replacer(match):
    mods = match.group(1).strip()
    # mods ends with something like:  .padding(X.dp), 
    # we need to remove the trailing comma
    if mods.endswith(','):
        mods = mods[:-1]
        
    radius = match.group(2)
    return f"""Box(
        modifier = Modifier
            {mods}
            .clip(RoundedCornerShape({radius}.dp))
            .background(viewModel.themeGlassFill)
            .border(1.dp, viewModel.themeGlassBorder, RoundedCornerShape({radius}.dp))
    ) {{"""

new_content = pattern.sub(replacer, content)
with open('app/src/main/java/com/example/ui/screens/HomeDashboardScreen.kt', 'w') as f:
    f.write(new_content)
