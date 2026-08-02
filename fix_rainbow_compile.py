with open('app/src/main/java/com/example/ui/components/RainbowButton.kt', 'r') as f:
    content = f.read()

content = content.replace('.androidx.compose.foundation.layout.fillMaxWidth()', '.fillMaxWidth()')

with open('app/src/main/java/com/example/ui/components/RainbowButton.kt', 'w') as f:
    f.write(content)
