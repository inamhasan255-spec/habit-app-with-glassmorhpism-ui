import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

fun Modifier.glassmorphismBackground(
    fillColor: Color = Color(0xFF141419).copy(alpha = 0.9f),
    borderColor: Color = Color.White.copy(alpha = 0.08f),
    blurRadius: androidx.compose.ui.unit.Dp = 16.dp,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(24.dp)
): Modifier {
    return this
        .blur(blurRadius)
        .clip(shape)
        .background(fillColor)
        .border(1.dp, borderColor, shape)
}
