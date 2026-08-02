with open('app/src/main/java/com/example/ui/components/RainbowButton.kt', 'r') as f:
    content = f.read()

content = content.replace("""    Box(
        modifier = modifier.wrapContentSize(),
        contentAlignment = Alignment.Center
    ) {
        // Glow effect behind the button (simulates CSS ::before)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = 6.dp)
                .width(120.dp)
                .height(8.dp)
                .graphicsLayer { alpha = 0.5f }
                .background(
                    brush = animatedBrush,
                    shape = RoundedCornerShape(percent = 50)
                )
        )

        // Main button: gradient border + solid inner background
        Box(
            modifier = Modifier
                .height(44.dp)
                .wrapContentWidth()
                .clip(RoundedCornerShape(cornerRadius))
                .background(animatedBrush)
                .padding(borderWidth)
                .clip(RoundedCornerShape(cornerRadius - borderWidth))
                .background(if (isDark) Color.White else Color(0xFF121213))
                .clickable(onClick = onClick)
                .padding(horizontal = 32.dp),
            contentAlignment = Alignment.Center
        )""", """    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Glow effect behind the button (simulates CSS ::before)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = 6.dp)
                .width(120.dp)
                .height(8.dp)
                .graphicsLayer { alpha = 0.5f }
                .background(
                    brush = animatedBrush,
                    shape = RoundedCornerShape(percent = 50)
                )
        )

        // Main button: gradient border + solid inner background
        Box(
            modifier = Modifier
                .height(44.dp)
                .androidx.compose.foundation.layout.fillMaxWidth()
                .clip(RoundedCornerShape(cornerRadius))
                .background(animatedBrush)
                .padding(borderWidth)
                .clip(RoundedCornerShape(cornerRadius - borderWidth))
                .background(if (isDark) Color.White else Color(0xFF121213))
                .clickable(onClick = onClick)
                .padding(horizontal = 32.dp),
            contentAlignment = Alignment.Center
        )""")

with open('app/src/main/java/com/example/ui/components/RainbowButton.kt', 'w') as f:
    f.write(content)
