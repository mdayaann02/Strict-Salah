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
    primary = EmeraldPrimaryDark,
    onPrimary = EmeraldOnPrimaryDark,
    primaryContainer = EmeraldPrimaryContainerDark,
    onPrimaryContainer = EmeraldOnPrimaryContainerDark,
    secondary = GoldSecondaryDark,
    onSecondary = Color(0xFF422D00),
    secondaryContainer = Color(0xFF604300),
    onSecondaryContainer = Color(0xFFFFDF9E),
    tertiary = Color(0xFF8CD4C7),
    background = NightBgDark,
    onBackground = Color(0xFFE2E4DE),
    surface = NightSurfaceDark,
    onSurface = Color(0xFFE2E4DE),
    surfaceVariant = NightSurfaceVariantDark,
    onSurfaceVariant = Color(0xFFC0C9C2),
    error = AlertLockRedDark
)

private val LightColorScheme = lightColorScheme(
    primary = EmeraldPrimaryLight,
    onPrimary = EmeraldOnPrimaryLight,
    primaryContainer = EmeraldPrimaryContainerLight,
    onPrimaryContainer = EmeraldOnPrimaryContainerLight,
    secondary = GoldSecondaryLight,
    onSecondary = Color.White,
    secondaryContainer = GoldSecondaryContainer,
    onSecondaryContainer = Color(0xFF261900),
    tertiary = Color(0xFF006A60),
    background = ParchmentBgLight,
    onBackground = Color(0xFF191C1A),
    surface = ParchmentSurfaceLight,
    onSurface = Color(0xFF191C1A),
    surfaceVariant = ParchmentSurfaceVariantLight,
    onSurfaceVariant = Color(0xFF404943),
    error = AlertLockRed
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use our handcrafted rich Islamic colors for authentic aesthetic
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
