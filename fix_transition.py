import re

with open("app/src/main/java/com/example/ui/screens/HomeDashboardScreen.kt", "r") as f:
    content = f.read()

pattern = r"""\s*fadeIn\(animationSpec = tween\(300\)\) togetherWith fadeOut\(animationSpec = tween\(300\)\)"""

replacement = """
                        val springSpec = spring<Float>(
                            dampingRatio = 0.85f,
                            stiffness = 300f
                        )
                        (fadeIn(animationSpec = tween(250)) +
                        scaleIn(initialScale = 0.92f, animationSpec = springSpec)) togetherWith
                        (fadeOut(animationSpec = tween(200)) +
                        scaleOut(targetScale = 0.96f, animationSpec = tween(200)))"""

content = re.sub(pattern, replacement, content)

with open("app/src/main/java/com/example/ui/screens/HomeDashboardScreen.kt", "w") as f:
    f.write(content)
