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

// Palette Revolut / Linear — minimaliste, propre, premium
private val Blue600      = Color(0xFF2563EB)
private val Blue50       = Color(0xFFEFF6FF)
private val Blue100      = Color(0xFFDBEAFE)
private val Slate900     = Color(0xFF0F172A)
private val Slate700     = Color(0xFF334155)
private val Slate500     = Color(0xFF64748B)
private val Slate200     = Color(0xFFE2E8F0)
private val Slate100     = Color(0xFFF1F5F9)
private val Slate50      = Color(0xFFF8FAFC)
private val Emerald600   = Color(0xFF059669)
private val Emerald50    = Color(0xFFECFDF5)
private val White        = Color(0xFFFFFFFF)

// Dark palette
private val BlueDark     = Color(0xFF60A5FA)
private val Slate800     = Color(0xFF1E293B)
private val Slate950     = Color(0xFF020617)
private val EmeraldDark  = Color(0xFF34D399)

private val LightColorScheme = lightColorScheme(
    primary                = Blue600,
    onPrimary              = White,
    primaryContainer       = Blue50,
    onPrimaryContainer     = Blue600,
    secondary              = Slate500,
    onSecondary            = White,
    secondaryContainer     = Slate100,
    onSecondaryContainer   = Slate700,
    tertiary               = Emerald600,
    onTertiary             = White,
    tertiaryContainer      = Emerald50,
    onTertiaryContainer    = Emerald600,
    background             = Slate50,
    onBackground           = Slate900,
    surface                = White,
    onSurface              = Slate900,
    surfaceVariant         = Slate100,
    onSurfaceVariant       = Slate500,
    outline                = Slate200,
    outlineVariant         = Slate100,
)

private val DarkColorScheme = darkColorScheme(
    primary                = BlueDark,
    onPrimary              = Slate950,
    primaryContainer       = Color(0xFF1D3461),
    onPrimaryContainer     = Blue100,
    secondary              = Color(0xFF94A3B8),
    onSecondary            = Slate950,
    secondaryContainer     = Slate800,
    onSecondaryContainer   = Color(0xFFCBD5E1),
    tertiary               = EmeraldDark,
    onTertiary             = Slate950,
    tertiaryContainer      = Color(0xFF064E3B),
    onTertiaryContainer    = Color(0xFFA7F3D0),
    background             = Slate950,
    onBackground           = Color(0xFFF1F5F9),
    surface                = Slate800,
    onSurface              = Color(0xFFF1F5F9),
    surfaceVariant         = Color(0xFF1E293B),
    onSurfaceVariant       = Color(0xFF94A3B8),
    outline                = Color(0xFF334155),
    outlineVariant         = Color(0xFF1E293B),
)

val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small      = RoundedCornerShape(10.dp),
    medium     = RoundedCornerShape(14.dp),
    large      = RoundedCornerShape(18.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

@Composable
fun MuscuNombreTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    tierColorOverride: Color? = null,
    content: @Composable () -> Unit
) {
    val baseScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    // Override primary avec la couleur du tier si fournie
    val colorScheme = if (tierColorOverride != null) {
        baseScheme.copy(
            primary = tierColorOverride,
            primaryContainer = tierColorOverride.copy(alpha = 0.12f),
            onPrimaryContainer = tierColorOverride
        )
    } else baseScheme
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
        typography  = Typography,
        shapes      = AppShapes,
        content     = content
    )
}
