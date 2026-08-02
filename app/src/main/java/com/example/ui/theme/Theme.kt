package com.example.ui.theme

import android.os.Build
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val GlassDarkColorScheme = darkColorScheme(
    primary = LuminaPrimaryDark,
    onPrimary = Color.White,
    primaryContainer = LuminaPrimaryDark.copy(alpha = 0.25f),
    onPrimaryContainer = Color(0xFFDDD6FE),
    secondary = LuminaSecondaryDark,
    onSecondary = Color.White,
    secondaryContainer = LuminaSecondaryDark.copy(alpha = 0.25f),
    onSecondaryContainer = Color(0xFFFBCFE8),
    tertiary = LuminaTertiaryDark,
    onTertiary = Color.White,
    tertiaryContainer = LuminaTertiaryDark.copy(alpha = 0.25f),
    onTertiaryContainer = Color(0xFFA7F3D0),
    background = LuminaDarkBackground,
    onBackground = LuminaTextDarkPrimary,
    surface = GlassDarkSurface,
    onSurface = LuminaTextDarkPrimary,
    surfaceVariant = GlassDarkSurfaceVariant,
    onSurfaceVariant = LuminaTextDarkSecondary,
    surfaceContainer = GlassDarkCardFill,
    surfaceContainerHigh = Color(0x80221F42),
    surfaceContainerHighest = Color(0x992A2652),
    surfaceContainerLow = Color(0x4D14122B),
    surfaceContainerLowest = Color(0x330C0B18),
    outline = GlassDarkBorder,
    outlineVariant = GlassDarkDivider,
    error = Color(0xFFEF4444),
    onError = Color.White
)

private val GlassLightColorScheme = lightColorScheme(
    primary = LuminaPrimaryLight,
    onPrimary = Color.White,
    primaryContainer = LuminaPrimaryLight.copy(alpha = 0.15f),
    onPrimaryContainer = Color(0xFF312E81),
    secondary = LuminaSecondaryLight,
    onSecondary = Color.White,
    secondaryContainer = LuminaSecondaryLight.copy(alpha = 0.15f),
    onSecondaryContainer = Color(0xFF064E3B),
    tertiary = LuminaTertiaryLight,
    onTertiary = Color.White,
    tertiaryContainer = LuminaTertiaryLight.copy(alpha = 0.15f),
    onTertiaryContainer = Color(0xFF78350F),
    background = LuminaLightBackground,
    onBackground = LuminaTextLightPrimary,
    surface = GlassLightSurface,
    onSurface = LuminaTextLightPrimary,
    surfaceVariant = GlassLightSurfaceVariant,
    onSurfaceVariant = LuminaTextLightSecondary,
    surfaceContainer = GlassLightCardFill,
    surfaceContainerHigh = Color(0xB3FFFFFF),
    surfaceContainerHighest = Color(0xCCFFFFFF),
    surfaceContainerLow = Color(0x80F1F5F9),
    surfaceContainerLowest = Color(0x66E2E8F0),
    outline = GlassLightBorder,
    outlineVariant = GlassLightDivider,
    error = Color(0xFFEF4444),
    onError = Color.White
)

val GlassShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

// Premium glassmorphism variable set / state container (ThemeContext in Jetpack Compose)
data class LuminaThemeColors(
    val bg: Color,
    val surface: Color,
    val text: Color,
    val textSecondary: Color,
    val border: Color,
    val glassFill: Color,
    val glassBorder: Color,
    val divider: Color,
    val isDark: Boolean,
    val blurRadius: androidx.compose.ui.unit.Dp = 24.dp
)

// Global CompositionLocal representing the ThemeContext
val LocalLuminaThemeColors = androidx.compose.runtime.staticCompositionLocalOf<LuminaThemeColors> {
    // Default fallback colors (Premium Light-Based White Glass)
    LuminaThemeColors(
        bg = LuminaLightBackground,
        surface = GlassLightSurface,
        text = LuminaTextLightPrimary,
        textSecondary = LuminaTextLightSecondary,
        border = GlassLightBorder,
        glassFill = GlassLightSurfaceVariant,
        glassBorder = GlassLightBorderHighlight,
        divider = GlassLightDivider,
        isDark = false,
        blurRadius = 24.dp
    )
}

// Global accessor object
object LuminaTheme {
    val colors: LuminaThemeColors
        @Composable
        get() = LocalLuminaThemeColors.current
}

// Global ThemeProvider
@Composable
fun LuminaThemeProvider(
    isDarkMode: Boolean,
    glassOpacity: Float = 0.7f,
    blurRadius: androidx.compose.ui.unit.Dp = 24.dp,
    content: @Composable () -> Unit
) {
    val colors = if (isDarkMode) {
        LuminaThemeColors(
            bg = LuminaDarkBackground,
            surface = GlassDarkSurface,
            text = LuminaTextDarkPrimary,
            textSecondary = LuminaTextDarkSecondary,
            border = GlassDarkBorder,
            glassFill = GlassDarkCardFill.copy(alpha = glassOpacity),
            glassBorder = GlassDarkBorderHighlight.copy(alpha = (glassOpacity * 0.45f).coerceIn(0.12f, 0.65f)),
            divider = GlassDarkDivider,
            isDark = true,
            blurRadius = blurRadius
        )
    } else {
        LuminaThemeColors(
            bg = LuminaLightBackground,
            surface = GlassLightSurface,
            text = LuminaTextLightPrimary,
            textSecondary = LuminaTextLightSecondary,
            border = GlassLightBorder,
            glassFill = GlassLightCardFill.copy(alpha = glassOpacity.coerceIn(0.5f, 0.85f)),
            glassBorder = GlassLightBorderHighlight.copy(alpha = (glassOpacity * 0.35f).coerceIn(0.08f, 0.3f)),
            divider = GlassLightDivider,
            isDark = false,
            blurRadius = blurRadius
        )
    }

    val colorScheme = if (isDarkMode) {
        GlassDarkColorScheme.copy(
            surface = colors.surface,
            surfaceVariant = colors.glassFill,
            background = colors.bg
        )
    } else {
        GlassLightColorScheme.copy(
            surface = colors.surface,
            surfaceVariant = colors.glassFill,
            background = colors.bg
        )
    }

    androidx.compose.runtime.CompositionLocalProvider(
        LocalLuminaThemeColors provides colors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = GlassShapes,
            content = content
        )
    }
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val calendar = java.util.Calendar.getInstance()
    val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
    
    // Night is roughly 20:00 to 06:00
    val isNight = hour >= 20 || hour < 6
    
    val glassOpacity = if (isNight && darkTheme) 0.85f else 0.7f
    val blurRadius = if (isNight && darkTheme) 32.dp else 24.dp

    LuminaThemeProvider(
        isDarkMode = darkTheme, 
        glassOpacity = glassOpacity,
        blurRadius = blurRadius,
        content = content
    )
}

// Reusable custom modifier for premium soft glass shadows
fun Modifier.softGlassShadow(
    elevation: androidx.compose.ui.unit.Dp = 12.dp,
    shape: androidx.compose.ui.graphics.Shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
    isDark: Boolean = false
): Modifier {
    return this.shadow(
        elevation = if (isDark) 4.dp else elevation,
        shape = shape,
        clip = false,
        ambientColor = if (isDark) Color.Transparent else Color(0x11000000),
        spotColor = if (isDark) Color.Transparent else Color(0x1B000000)
    )
}

/**
 * A highly reusable, premium CSS-inspired glassmorphism modifier that unifies
 * custom semi-transparent backgrounds, custom-molded light reflecting borders,
 * backdrop-blur simulation, and custom-tuned soft shadows for dark/light mode.
 */
@Composable
fun Modifier.glassmorphic(
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(24.dp),
    borderWidth: androidx.compose.ui.unit.Dp = 1.dp,
    elevation: androidx.compose.ui.unit.Dp = 12.dp,
    blurRadius: androidx.compose.ui.unit.Dp = (-1).dp
): Modifier {
    val colors = LuminaTheme.colors
    val actualBlur = if (blurRadius == (-1).dp) colors.blurRadius else blurRadius
    return this
        .softGlassShadow(elevation = elevation, shape = shape, isDark = colors.isDark)
        .clip(shape)
        .blur(radius = actualBlur) // Simulates backdrop-blur/frosted glass filter
        .background(colors.glassFill)
        .border(
            width = borderWidth,
            brush = Brush.verticalGradient(
                colors = if (colors.isDark) {
                    listOf(
                        Color.White.copy(alpha = 0.22f),
                        Color.White.copy(alpha = 0.05f),
                        Color.Transparent
                    )
                } else {
                    listOf(
                        Color.White.copy(alpha = 0.65f),
                        Color.White.copy(alpha = 0.15f),
                        Color.Transparent
                    )
                }
            ),
            shape = shape
        )
}

