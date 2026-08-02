import re

with open("app/src/main/java/com/example/ui/screens/HomeDashboardScreen.kt", "r") as f:
    content = f.read()

pattern_to_remove = r"""                var showSoundControl by remember \{ mutableStateOf\(false\) \}.*?AnimatedVisibility\(\s*visible = showSoundControl,.*?\) \{\s*LuminaFloatingSoundControl\(viewModel\)\s*\}"""

content = re.sub(pattern_to_remove, "", content, flags=re.DOTALL)

pattern_btn_remove = r"""                        FloatingActionButton\(\s*onClick = \{ showSoundControl = !showSoundControl \},.*?\)\s*\{\s*Icon\(if\(showSoundControl\) Icons\.Default\.Close else Icons\.Default\.GraphicEq, contentDescription = "Ambient Sounds"\)\s*\}"""

content = re.sub(pattern_btn_remove, "", content, flags=re.DOTALL)

with open("app/src/main/java/com/example/ui/screens/HomeDashboardScreen.kt", "w") as f:
    f.write(content)
