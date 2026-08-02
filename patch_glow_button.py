import re

with open("app/src/main/java/com/example/ui/components/RainbowCornerGlowButton.kt", "r") as f:
    content = f.read()

# Add GlowTheme enum and getDynamicGlowColors
imports_pattern = r"import kotlin\.math\.max"
replacement_imports = """import kotlin.math.max
import java.util.Calendar"""

content = content.replace("import kotlin.math.max", replacement_imports)

theme_enum = """

enum class GlowTheme {
    Default,
    Dynamic,
    Morning,
    Afternoon,
    Evening,
    Night
}
"""
content = content.replace("@Composable", theme_enum + "\n@Composable", 1)

# Add theme parameter
param_pattern = r"isBreathing: Boolean = false\n\)"
replacement_param = """isBreathing: Boolean = false,
    theme: GlowTheme = GlowTheme.Dynamic
)"""

content = content.replace("isBreathing: Boolean = false\n)", replacement_param)

# Add time-of-day logic
logic_pattern = r"val cornerRadius = 28\.dp\n\s*// Outer box adds padding so the glow doesn't get clipped"
replacement_logic = """val cornerRadius = 28.dp

    val currentHour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    val resolvedTheme = if (theme == GlowTheme.Dynamic) {
        when (currentHour) {
            in 5..11 -> GlowTheme.Morning
            in 12..17 -> GlowTheme.Afternoon
            in 18..20 -> GlowTheme.Evening
            else -> GlowTheme.Night
        }
    } else theme

    // Outer box adds padding so the glow doesn't get clipped"""

content = content.replace("val cornerRadius = 28.dp\n\n    // Outer box adds padding so the glow doesn't get clipped", replacement_logic)

# Replace the color logic
colors_pattern = r"""val colors = if \(glowColor != null\) \{
                            listOf\(glowColor, glowColor, glowColor, glowColor\)
                        \} else \{
                            listOf\(
                                Color\(0xFFFF0040\), // Red
                                Color\(0xFFFF8000\), // Orange
                                Color\(0xFF00FF80\), // Green
                                Color\(0xFF0080FF\)  // Blue
                            \)
                        \}"""

replacement_colors = """val colors = if (glowColor != null) {
                            listOf(glowColor, glowColor, glowColor, glowColor)
                        } else {
                            when (resolvedTheme) {
                                GlowTheme.Morning -> listOf(
                                    Color(0xFFFFB74D), // Warm Orange
                                    Color(0xFFFF8A65), // Coral
                                    Color(0xFF4FC3F7), // Light Blue
                                    Color(0xFF81C784)  // Soft Green
                                )
                                GlowTheme.Afternoon -> listOf(
                                    Color(0xFFFF5252), // Red
                                    Color(0xFFFFAB40), // Orange
                                    Color(0xFF64FFDA), // Teal
                                    Color(0xFF448AFF)  // Blue
                                )
                                GlowTheme.Evening -> listOf(
                                    Color(0xFF9575CD), // Deep Purple
                                    Color(0xFFE57373), // Muted Red
                                    Color(0xFF4DB6AC), // Teal
                                    Color(0xFF7986CB)  // Indigo
                                )
                                GlowTheme.Night -> listOf(
                                    Color(0xFF5E35B1), // Deep Violet
                                    Color(0xFF3949AB), // Deep Blue
                                    Color(0xFF00ACC1), // Cyan
                                    Color(0xFF039BE5)  // Light Blue
                                )
                                else -> listOf(
                                    Color(0xFFFF0040), // Red
                                    Color(0xFFFF8000), // Orange
                                    Color(0xFF00FF80), // Green
                                    Color(0xFF0080FF)  // Blue
                                )
                            }
                        }"""

content = content.replace("""val colors = if (glowColor != null) {
                            listOf(glowColor, glowColor, glowColor, glowColor)
                        } else {
                            listOf(
                                Color(0xFFFF0040), // Red
                                Color(0xFFFF8000), // Orange
                                Color(0xFF00FF80), // Green
                                Color(0xFF0080FF)  // Blue
                            )
                        }""", replacement_colors)

with open("app/src/main/java/com/example/ui/components/RainbowCornerGlowButton.kt", "w") as f:
    f.write(content)
