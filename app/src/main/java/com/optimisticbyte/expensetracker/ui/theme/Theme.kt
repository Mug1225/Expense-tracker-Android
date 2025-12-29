package com.optimisticbyte.expensetracker.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

enum class AppTheme {
    System, Emerald, Ocean, Charcoal
}

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

// Emerald
private val EmeraldLight = lightColorScheme(
    primary = EmeraldPrimary,
    secondary = EmeraldSecondary,
    tertiary = EmeraldTertiary,
    background = EmeraldBackground,
    surface = EmeraldSurface
)
private val EmeraldDark = darkColorScheme(
    primary = EmeraldPrimaryDark,
    secondary = EmeraldSecondaryDark,
    tertiary = EmeraldTertiaryDark,
    background = EmeraldBackgroundDark,
    surface = EmeraldSurfaceDark
)

// Ocean
private val OceanLight = lightColorScheme(
    primary = OceanPrimary,
    secondary = OceanSecondary,
    tertiary = OceanTertiary,
    background = OceanBackground,
    surface = OceanSurface
)
private val OceanDark = darkColorScheme(
    primary = OceanPrimaryDark,
    secondary = OceanSecondaryDark,
    tertiary = OceanTertiaryDark,
    background = OceanBackgroundDark,
    surface = OceanSurfaceDark
)

// Charcoal
private val CharcoalLight = lightColorScheme(
    primary = CharcoalPrimary,
    secondary = CharcoalSecondary,
    tertiary = CharcoalTertiary,
    background = CharcoalBackground,
    surface = CharcoalSurface
)
private val CharcoalDark = darkColorScheme(
    primary = CharcoalPrimaryDark,
    secondary = CharcoalSecondaryDark,
    tertiary = CharcoalTertiaryDark,
    background = CharcoalBackgroundDark,
    surface = CharcoalSurfaceDark
)

@Composable
fun SpendWiseTheme(
    theme: AppTheme = AppTheme.System,
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true, // specific for System theme
    content: @Composable () -> Unit
) {
    val colorScheme = when (theme) {
        AppTheme.System -> {
            if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            } else {
                if (darkTheme) DarkColorScheme else LightColorScheme
            }
        }
        AppTheme.Emerald -> if (darkTheme) EmeraldDark else EmeraldLight
        AppTheme.Ocean -> if (darkTheme) OceanDark else OceanLight
        AppTheme.Charcoal -> if (darkTheme) CharcoalDark else CharcoalLight
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
