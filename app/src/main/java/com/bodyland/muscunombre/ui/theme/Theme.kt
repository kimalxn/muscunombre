package com.bodyland.muscunombre.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

// Couleurs Notion-like pour Gym Rat
private val primaryLight = Color(0xFF2563EB) // Bleu Notion
private val onPrimaryLight = Color(0xFFFFFFFF)
private val primaryContainerLight = Color(0xFFEFF6FF) // Bleu très clair
private val onPrimaryContainerLight = Color(0xFF1E40AF)

private val secondaryLight = Color(0xFF6B7280) // Gris Notion
private val onSecondaryLight = Color(0xFFFFFFFF)
private val secondaryContainerLight = Color(0xFFF3F4F6)
private val onSecondaryContainerLight = Color(0xFF374151)

private val tertiaryLight = Color(0xFF059669) // Vert Notion
private val onTertiaryLight = Color(0xFFFFFFFF)
private val tertiaryContainerLight = Color(0xFFD1FAE5)
private val onTertiaryContainerLight = Color(0xFF065F46)

private val backgroundLight = Color(0xFFFAFAFA) // Gris très clair Notion
private val onBackgroundLight = Color(0xFF191919) // Noir Notion
private val surfaceLight = Color(0xFFFFFFFF)
private val onSurfaceLight = Color(0xFF191919)
private val surfaceVariantLight = Color(0xFFF5F5F5)

// Dark theme colors Notion-like
private val primaryDark = Color(0xFF60A5FA)
private val onPrimaryDark = Color(0xFF1E3A5F)
private val primaryContainerDark = Color(0xFF1E3A5F)
private val onPrimaryContainerDark = Color(0xFFBFDBFE)

private val secondaryDark = Color(0xFF9CA3AF)
private val onSecondaryDark = Color(0xFF1F2937)
private val secondaryContainerDark = Color(0xFF374151)
private val onSecondaryContainerDark = Color(0xFFE5E7EB)

private val tertiaryDark = Color(0xFF34D399)
private val onTertiaryDark = Color(0xFF064E3B)
private val tertiaryContainerDark = Color(0xFF065F46)
private val onTertiaryContainerDark = Color(0xFFA7F3D0)

private val backgroundDark = Color(0xFF191919)
private val onBackgroundDark = Color(0xFFE5E5E5)
private val surfaceDark = Color(0xFF252525)
private val onSurfaceDark = Color(0xFFE5E5E5)

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
    surfaceVariant = surfaceVariantLight,
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

// Notion-like shapes with minimal rounding
val NotionShapes = Shapes(
    extraSmall = RoundedCornerShape(2.dp),
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(6.dp),
    large = RoundedCornerShape(8.dp),
    extraLarge = RoundedCornerShape(8.dp)
)

@Composable
fun MuscuNombreTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Désactivé pour garder le style Notion
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
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = NotionShapes,
        content = content
    )
}
