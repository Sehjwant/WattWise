package com.fit5046.wattwise.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// WattWise brand colour scheme — used as the Material3 fallback
// when dynamic colour is disabled (required so the green theme shows in screenshots)
private val WattWiseLightColorScheme = lightColorScheme(
    primary          = WattWiseGreen,
    onPrimary        = Color.White,
    primaryContainer = WattWiseGreenLight,
    onPrimaryContainer = WattWiseGreenDark,
    secondary        = WattWiseGreenMid,
    onSecondary      = Color.White,
    background       = Color(0xFFF9FBF9),
    surface          = Color.White,
    onBackground     = Color(0xFF1C1C1C),
    onSurface        = Color(0xFF1C1C1C),
    error            = Color(0xFFB00020),
    onError          = Color.White
)

@Composable
fun WattWiseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // ── CRITICAL FIX ─────────────────────────────────────────────────────────
    // dynamicColor MUST be false so Android 12+ does not override the green
    // WattWise brand colours with the system wallpaper palette.
    // Leaving this true would replace every Color(0xFF1B5E20) with the device
    // theme colour and make all screenshots look wrong for the report.
    // ─────────────────────────────────────────────────────────────────────────
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // Always use WattWise brand scheme — dark mode uses the same scheme for
    // the skeleton prototype (can be expanded in A4 if needed)
    val colorScheme = WattWiseLightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = WattWiseGreenDark.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
