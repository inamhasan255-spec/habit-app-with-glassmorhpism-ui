with open('app/src/main/java/com/example/ui/screens/OnboardingScreen.kt', 'r') as f:
    content = f.read()

content = content.replace('com.example.ui.components.LiquidMetalButton(', 'com.example.ui.components.RainbowButton(')

with open('app/src/main/java/com/example/ui/screens/OnboardingScreen.kt', 'w') as f:
    f.write(content)

with open('app/src/main/java/com/example/ui/screens/FocusCompleteScreen.kt', 'r') as f:
    content = f.read()

content = content.replace('com.example.ui.components.LiquidMetalButton(', 'com.example.ui.components.RainbowButton(')

with open('app/src/main/java/com/example/ui/screens/FocusCompleteScreen.kt', 'w') as f:
    f.write(content)
