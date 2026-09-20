package com.example.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
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

private val LightColorScheme = DarkColorScheme // Caitlin Daily aesthetic is rich dark emerald

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Maintain Caitlin Daily signature dark aesthetic as shown in screenshot
    dynamicColor: Boolean = false, // Keep signature Caitlin Daily emerald / forest identity
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> DarkColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

