import re

with open('app/src/main/java/com/example/ui/screens/HomeDashboardScreen.kt', 'r') as f:
    content = f.read()

# Replace LuminaHeroSection
old_hero = """    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .border(1.dp, viewModel.themeGlassBorder, RoundedCornerShape(32.dp))
            .background(viewModel.themeGlassFill)
            .padding(28.dp)
    ) {"""

new_hero = """    com.example.ui.components.GlassContainer(
        modifier = Modifier
            .fillMaxWidth()
            .padding(28.dp),
        backgroundColor = viewModel.themeGlassFill,
        borderColor = viewModel.themeGlassBorder,
        shape = RoundedCornerShape(32.dp)
    ) {"""
content = content.replace(old_hero, new_hero)

# Replace Quick Actions Box
old_actions = """    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .border(1.dp, viewModel.themeGlassBorder, RoundedCornerShape(24.dp))
            .background(viewModel.themeGlassFill)
            .padding(24.dp)
    ) {"""

new_actions = """    com.example.ui.components.GlassContainer(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        backgroundColor = viewModel.themeGlassFill,
        borderColor = viewModel.themeGlassBorder,
        shape = RoundedCornerShape(24.dp)
    ) {"""
content = content.replace(old_actions, new_actions)

with open('app/src/main/java/com/example/ui/screens/HomeDashboardScreen.kt', 'w') as f:
    f.write(content)

