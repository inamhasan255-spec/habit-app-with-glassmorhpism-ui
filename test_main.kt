import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.unit.dp

fun Modifier.glassmorphismBlur(): Modifier {
    return this.blur(16.dp) // Example
}
