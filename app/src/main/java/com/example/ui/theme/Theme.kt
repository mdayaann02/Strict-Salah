package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

enum class AppThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
    AMOLED
}

enum class AppColorPalette {
    EMERALD,
    GOLD,
    INDIGO,
    CRIMSON,
    DYNAMIC
}

fun getCustomColorScheme(
    isDark: Boolean,
    isAmoled: Boolean,
    palette: AppColorPalette
): ColorScheme {
    val (pLight, onPLight, pContLight, onPContLight) = when (palette) {
        AppColorPalette.EMERALD, AppColorPalette.DYNAMIC -> Quad(EmeraldPrimaryLight, EmeraldOnPrimaryLight, EmeraldPrimaryContainerLight, EmeraldOnPrimaryContainerLight)
        AppColorPalette.GOLD -> Quad(GoldPrimaryLight, GoldOnPrimaryLight, GoldPrimaryContainerLight, GoldOnPrimaryContainerLight)
        AppColorPalette.INDIGO -> Quad(IndigoPrimaryLight, IndigoOnPrimaryLight, IndigoPrimaryContainerLight, IndigoOnPrimaryContainerLight)
        AppColorPalette.CRIMSON -> Quad(CrimsonPrimaryLight, CrimsonOnPrimaryLight, CrimsonPrimaryContainerLight, CrimsonOnPrimaryContainerLight)
    }

    val (pDark, onPDark, pContDark, onPContDark) = when (palette) {
        AppColorPalette.EMERALD, AppColorPalette.DYNAMIC -> Quad(EmeraldPrimaryDark, EmeraldOnPrimaryDark, EmeraldPrimaryContainerDark, EmeraldOnPrimaryContainerDark)
        AppColorPalette.GOLD -> Quad(GoldPrimaryDark, GoldOnPrimaryDark, GoldPrimaryContainerDark, GoldOnPrimaryContainerDark)
        AppColorPalette.INDIGO -> Quad(IndigoPrimaryDark, IndigoOnPrimaryDark, IndigoPrimaryContainerDark, IndigoOnPrimaryContainerDark)
        AppColorPalette.CRIMSON -> Quad(CrimsonPrimaryDark, CrimsonOnPrimaryDark, CrimsonPrimaryContainerDark, CrimsonOnPrimaryContainerDark)
    }

    return when {
        isAmoled -> darkColorScheme(
            primary = pDark,
            onPrimary = onPDark,
            primaryContainer = pContDark,
            onPrimaryContainer = onPContDark,
            secondary = GoldSecondaryDark,
            onSecondary = Color(0xFF422D00),
            secondaryContainer = Color(0xFF332300),
            onSecondaryContainer = Color(0xFFFFDF9E),
            tertiary = Color(0xFF8CD4C7),
            background = AmoledBg,
            onBackground = Color(0xFFE8EAE6),
            surface = AmoledSurface,
            onSurface = Color(0xFFE8EAE6),
            surfaceVariant = AmoledSurfaceVariant,
            onSurfaceVariant = Color(0xFFCAD0CA),
            outline = AmoledBorder,
            outlineVariant = Color(0xFF1E2F26),
            error = AlertLockRedDark
        )
        isDark -> darkColorScheme(
            primary = pDark,
            onPrimary = onPDark,
            primaryContainer = pContDark,
            onPrimaryContainer = onPContDark,
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
        else -> lightColorScheme(
            primary = pLight,
            onPrimary = onPLight,
            primaryContainer = pContLight,
            onPrimaryContainer = onPContLight,
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
    }
}

private data class Quad(val a: Color, val b: Color, val c: Color, val d: Color)

@Composable
fun MyApplicationTheme(
    themeMode: String = "SYSTEM",
    colorPalette: String = "EMERALD",
    content: @Composable () -> Unit
) {
    val systemInDark = isSystemInDarkTheme()
    val isDark = when (themeMode.uppercase()) {
        "LIGHT" -> false
        "DARK", "AMOLED" -> true
        else -> systemInDark
    }
    val isAmoled = themeMode.equals("AMOLED", ignoreCase = true)
    val palette = try {
        AppColorPalette.valueOf(colorPalette.uppercase())
    } catch (e: Exception) {
        AppColorPalette.EMERALD
    }

    val context = LocalContext.current
    val colorScheme = when {
        palette == AppColorPalette.DYNAMIC && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val base = if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            if (isAmoled) {
                base.copy(
                    background = AmoledBg,
                    surface = AmoledSurface,
                    surfaceVariant = AmoledSurfaceVariant
                )
            } else {
                base
            }
        }
        else -> getCustomColorScheme(isDark = isDark, isAmoled = isAmoled, palette = palette)
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
