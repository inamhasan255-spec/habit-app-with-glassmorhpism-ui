import re
import glob

# The pattern we generated:
# com.example.ui.components.GlassContainer(
#         modifier = Modifier
#             {pre_modifiers}.padding({padding}),
#         backgroundColor = viewModel.themeGlassFill,
#         borderColor = viewModel.themeGlassBorder,
#         shape = RoundedCornerShape({radius}.dp)
#     ) {

pattern = re.compile(
    r'com\.example\.ui\.components\.GlassContainer\(\s*modifier\s*=\s*Modifier\s*(.*?)\.padding\((.*?)\),\s*backgroundColor\s*=\s*viewModel\.themeGlassFill,\s*borderColor\s*=\s*viewModel\.themeGlassBorder,\s*shape\s*=\s*RoundedCornerShape\(([\d]+)\.dp\)\s*\)\s*\{',
    re.DOTALL
)

def replacer(match):
    pre_modifiers = match.group(1)
    padding = match.group(2)
    radius = match.group(3)
    
    # We strip trailing whitespace from pre_modifiers if needed
    if pre_modifiers.endswith('\n            '):
        pre_modifiers = pre_modifiers[:-13]
        
    # Reconstruct the original Box
    # Note: The original order might have been clip -> background -> border or clip -> border -> background.
    # It doesn't matter too much as long as clip is before background.
    # In Compose, clip then background then border is standard for cards.
    return f"""Box(
        modifier = Modifier
            {pre_modifiers}
            .clip(RoundedCornerShape({radius}.dp))
            .background(viewModel.themeGlassFill)
            .border(1.dp, viewModel.themeGlassBorder, RoundedCornerShape({radius}.dp))
            .padding({padding})
    ) {{"""

for file in glob.glob('app/src/main/java/com/example/ui/screens/*.kt'):
    with open(file, 'r') as f:
        content = f.read()
    
    new_content = pattern.sub(replacer, content)
    
    if content != new_content:
        with open(file, 'w') as f:
            f.write(new_content)
        print(f"Reverted {file}")

