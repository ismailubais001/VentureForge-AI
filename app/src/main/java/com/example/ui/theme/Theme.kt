package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = ForgeGold,
    onPrimary = ObsidianNavyDark,
    primaryContainer = ForgeGoldDark,
    onPrimaryContainer = ForgeGoldLight,
    secondary = TechTeal,
    onSecondary = ObsidianNavyDark,
    secondaryContainer = TechTealDark,
    onSecondaryContainer = Color.White,
    tertiary = ProfitGreen,
    background = SurfaceDark,
    onBackground = TextPrimaryDark,
    surface = CardDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SlateNavyCardDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = SlateNavyBorderDark
)

private val LightColorScheme = lightColorScheme(
    primary = ForgeGoldPrimaryLight,
    onPrimary = Color.White,
    primaryContainer = ForgeGoldLight,
    onPrimaryContainer = ForgeGoldDark,
    secondary = TechTealDark,
    onSecondary = Color.White,
    secondaryContainer = TechTeal,
    onSecondaryContainer = ObsidianNavyDark,
    tertiary = ProfitGreen,
    background = SurfaceLight,
    onBackground = TextPrimaryLight,
    surface = CardLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = TextSecondaryLight,
    outline = BorderLight
)

@Composable
fun VentureForgeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep branded Forge look by default
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
