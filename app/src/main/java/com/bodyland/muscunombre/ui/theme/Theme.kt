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

// ──────────────────────────────────────────
// "Clippy-Core Ligne Claire" — palette flat
// Hergé × Microsoft Office 97 × mobile clean
// ──────────────────────────────────────────
object LC {
    val Yellow   = Color(0xFFFFC107)
    val Blue     = Color(0xFF1976D2)
    val Red      = Color(0xFFD32F2F)
    val BgBlue   = Color(0xFFBBDEFB)
    val Black    = Color(0xFF000000)
    val White    = Color(0xFFFFFFFF)
    val LightYellow = Color(0xFFFFF9C4)
    val DarkBlue = Color(0xFF0D47A1)
}

private val LightColorScheme = lightColorScheme(
    primary                = LC.Blue,
    onPrimary              = LC.White,
    primaryContainer       = LC.LightYellow,
    onPrimaryContainer     = LC.Black,
    secondary              = LC.Yellow,
    onSecondary            = LC.Black,
    secondaryContainer     = LC.LightYellow,
    onSecondaryContainer   = LC.Black,
    tertiary               = LC.Red,
    onTertiary             = LC.White,
    tertiaryContainer      = Color(0xFFFFCDD2),
    onTertiaryContainer    = LC.Black,
    background             = LC.BgBlue,
    onBackground           = LC.Black,
    surface                = LC.White,
    onSurface              = LC.Black,
    surfaceVariant         = LC.BgBlue,
    onSurfaceVariant       = Color(0xFF333333),
    outline                = LC.Black,
    outlineVariant         = Color(0xFF555555),
    error                  = LC.Red,
    onError                = LC.White,
)

// Dark = same flat palette, darker bg
private val DarkColorScheme = darkColorScheme(
    primary                = Color(0xFF64B5F6),
    onPrimary              = LC.Black,
    primaryContainer       = Color(0xFF1565C0),
    onPrimaryContainer     = LC.White,
    secondary              = LC.Yellow,
    onSecondary            = LC.Black,
    secondaryContainer     = Color(0xFFF57F17),
    onSecondaryContainer   = LC.Black,
    tertiary               = Color(0xFFEF9A9A),
    onTertiary             = LC.Black,
    tertiaryContainer      = Color(0xFFB71C1C),
    onTertiaryContainer    = LC.White,
    background             = Color(0xFF0D1B2A),
    onBackground           = LC.White,
    surface                = Color(0xFF1A2740),
    onSurface              = LC.White,
    surfaceVariant         = Color(0xFF1E2D42),
    onSurfaceVariant       = Color(0xFFCCCCCC),
    outline                = LC.White,
    outlineVariant         = Color(0xFF888888),
)

// Coins arrondis doux — style BD, pas trop anguleux ni trop circulaire
val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small      = RoundedCornerShape(8.dp),
    medium     = RoundedCornerShape(10.dp),
    large      = RoundedCornerShape(10.dp),
    extraLarge = RoundedCornerShape(12.dp)
)

@Composable
fun MuscuNombreTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
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
        typography  = Typography,
        shapes      = AppShapes,
        content     = content
    )
}
