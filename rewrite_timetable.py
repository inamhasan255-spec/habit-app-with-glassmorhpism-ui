import re

with open('app/src/main/java/com/example/ui/screens/TimetableScreen.kt', 'r') as f:
    content = f.read()

# 1. Make cardBgColor and borderCol semi-transparent
content = re.sub(
    r'val cardBgColor = if \(isDark\) Color\(0xFF161E2E\) else Color\(0xFFFFFFFF\)',
    r'val cardBgColor = if (isDark) Color(0xFF161E2E).copy(alpha = 0.4f) else Color(0xFFFFFFFF).copy(alpha = 0.5f)',
    content
)

content = re.sub(
    r'val borderCol = if \(isDark\) Color\(0xFF1F2937\) else Color\(0xFFE5E7EB\)',
    r'val borderCol = if (isDark) Color(0xFF1F2937).copy(alpha = 0.3f) else Color(0xFFE5E7EB).copy(alpha = 0.5f)',
    content
)

# 2. Add dark theme toggle in the header
dark_mode_btn = """
                    IconButton(
                        onClick = { viewModel.toggleDarkMode() },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (isDark) Color(0xFFF59E0B).copy(alpha = 0.15f) else Color(0xFF6366F1).copy(alpha = 0.15f))
                            .border(1.dp, if (isDark) Color(0xFFF59E0B) else Color(0xFF6366F1), CircleShape)
                            .testTag("dark_mode_trigger_btn")
                    ) {
                        Icon(imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode, contentDescription = "Toggle Theme", tint = if (isDark) Color(0xFFF59E0B) else Color(0xFF6366F1))
                    }"""

header_ai_btn_pattern = r'(\s*IconButton\(\s*onClick = \{ showChatDialog = true \})'
content = re.sub(header_ai_btn_pattern, dark_mode_btn + r'\1', content, count=1)

# 3. Extract drawBehind into a separate Canvas with blur
draw_behind_pattern = r'(\.drawBehind\s*\{.*?\n\s*\})\s*\n\s*\)\s*\{'
# We need a regex that matches the entire .drawBehind block.
# Since it's multiline and has nested braces, regex is tricky. Let's do it manually with string replacement.

