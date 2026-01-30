package com.bodyland.muscunombre.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Couleurs personnalisées pour Gym Rat
private val primaryLight = Color(0xFF2E7D32) // Vert sportif
private val onPrimaryLight = Color(0xFFFFFFFF)
private val primaryContainerLight = Color(0xFFA5D6A7)
private val onPrimaryContainerLight = Color(0xFF1B5E20)

private val secondaryLight = Color(0xFF1976D2) // Bleu dynamique
private val onSecondaryLight = Color(0xFFFFFFFF)
private val secondaryContainerLight = Color(0xFFBBDEFB)
private val onSecondaryContainerLight = Color(0xFF0D47A1)

private val tertiaryLight = Color(0xFFFF6F00) // Orange énergique
private val onTertiaryLight = Color(0xFFFFFFFF)
private val tertiaryContainerLight = Color(0xFFFFE0B2)
private val onTertiaryContainerLight = Color(0xFFE65100)

private val backgroundLight = Color(0xFFFAFAFA)
private val onBackgroundLight = Color(0xFF1C1B1F)
private val surfaceLight = Color(0xFFFFFFFF)
private val onSurfaceLight = Color(0xFF1C1B1F)

// Dark theme colors
private val primaryDark = Color(0xFF81C784)
private val onPrimaryDark = Color(0xFF1B5E20)
private val primaryContainerDark = Color(0xFF2E7D32)
private val onPrimaryContainerDark = Color(0xFFC8E6C9)

private val secondaryDark = Color(0xFF64B5F6)
private val onSecondaryDark = Color(0xFF0D47A1)
private val secondaryContainerDark = Color(0xFF1565C0)
private val onSecondaryContainerDark = Color(0xFFBBDEFB)

private val tertiaryDark = Color(0xFFFFB74D)
private val onTertiaryDark = Color(0xFFE65100)
private val tertiaryContainerDark = Color(0xFFF57C00)
private val onTertiaryContainerDark = Color(0xFFFFE0B2)

private val backgroundDark = Color(0xFF121212)
private val onBackgroundDark = Color(0xFFE6E1E5)
private val surfaceDark = Color(0xFF1E1E1E)
private val onSurfaceDark = Color(0xFFE6E1E5)

private val LightColorScheme = lightColorScheme(
    primary = primaryLight,
    onPrimary = onPrimaryLight,
    primaryContainer = primaryContainerLight,
    onPrimaryContainer = onPrimaryContainerLight,
    secondary = secondaryLight,
    onSecondary = onSecondaryLight,
    secondaryContainer = secondaryContainerLight,
    onSecondaryContainer = onSecondaryContainerLight,
    tertiary = tertiaryLight,
    onTertiary = onTertiaryLight,
    tertiaryContainer = tertiaryContainerLight,
    onTertiaryContainer = onTertiaryContainerLight,
    background = backgroundLight,
    onBackground = onBackgroundLight,
    surface = surfaceLight,
    onSurface = onSurfaceLight,
)

private val DarkColorScheme = darkColorScheme(
    primary = primaryDark,
    onPrimary = onPrimaryDark,
    primaryContainer = primaryContainerDark,
    onPrimaryContainer = onPrimaryContainerDark,
    secondary = secondaryDark,
    onSecondary = onSecondaryDark,
    secondaryContainer = secondaryContainerDark,
    onSecondaryContainer = onSecondaryContainerDark,
    tertiary = tertiaryDark,
    onTertiary = onTertiaryDark,
    tertiaryContainer = tertiaryContainerDark,
    onTertiaryContainer = onTertiaryContainerDark,
    background = backgroundDark,
    onBackground = onBackgroundDark,
    surface = surfaceDark,
    onSurface = onSurfaceDark,
)

@Composable
fun MuscuNombreTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
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
