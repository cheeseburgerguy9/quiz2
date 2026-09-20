package com.aistudio.caitlindaily.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

val DarkColorScheme = darkColorScheme(
    primary = MintPrimary,
    onPrimary = MintPrimaryDark,
    primaryContainer = MintPrimaryContainer,
    onPrimaryContainer = MintOnPrimaryContainer,
    secondary = OceanBlue,
    onSecondary = OceanBlueDark,
    secondaryContainer = OceanBlueContainer,
    onSecondaryContainer = TextPrimary,
    tertiary = AmberGold,
    onTertiary = AmberGoldDark,
    tertiaryContainer = AmberGoldContainer,
    onTertiaryContainer = TextPrimary,
    background = ForestBackground,
    onBackground = TextPrimary,
    surface = ForestSurface,
    onSurface = TextPrimary,
    surfaceVariant = ForestSurfaceCard,
    onSurfaceVariant = TextSecondary,
    outline = ForestSurfaceBorder,
    outlineVariant = ForestOutline,
    error = CoralRed,
    errorContainer = CoralRedContainer
)

val LightColorScheme = lightColorScheme(
    primary = Color(0xFF0C6B3E),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFB9F2D0),
    onPrimaryContainer = Color(0xFF00210E),
    secondary = Color(0xFF1E6586),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD4EEFC),
    onSecondaryContainer = Color(0xFF001F2A),
    tertiary = Color(0xFF8B5400),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFDFB0),
    onTertiaryContainer = Color(0xFF2C1600),
    background = Color(0xFFF7FAF8),
    onBackground = Color(0xFF14241B),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF14241B),
    surfaceVariant = Color(0xFFE8F1EB),
    onSurfaceVariant = Color(0xFF405247),
    outline = Color(0xFFCAD8CF),
    outlineVariant = Color(0xFFE0ECE4),
    error = Color(0xFFBA1A1A),
    errorContainer = Color(0xFFFFDAD6)
)

enum class AppThemeMode(val value: String, val displayName: String) {
    AUTO("auto", "System Auto"),
    DARK("dark", "Dark"),
    LIGHT("light", "Light");

    companion object {
        fun fromString(value: String): AppThemeMode =
            values().firstOrNull { it.value.equals(value, ignoreCase = true) } ?: AUTO
    }
}

@Composable
fun CaitlinDailyTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    CaitlinDailyMaterialTheme(darkTheme, dynamicColor, content)
}

@Composable
fun CaitlinDailyMaterialTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
