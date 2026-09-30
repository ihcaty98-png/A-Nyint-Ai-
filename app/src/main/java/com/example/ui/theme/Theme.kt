package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = NyintCyan,
    onPrimary = NyintBgDark,
    primaryContainer = NyintCyanContainer,
    onPrimaryContainer = NyintCyan,
    secondary = NyintViolet,
    onSecondary = NyintBgDark,
    secondaryContainer = NyintVioletContainer,
    onSecondaryContainer = NyintViolet,
    tertiary = NyintAmber,
    onTertiary = NyintBgDark,
    background = NyintBgDark,
    onBackground = NyintTextPrimaryDark,
    surface = NyintSurfaceDark,
    onSurface = NyintTextPrimaryDark,
    surfaceVariant = NyintSurfaceElevatedDark,
    onSurfaceVariant = NyintTextSecondaryDark,
    outline = NyintCardBorderDark,
    error = NyintRed,
    onError = NyintBgDark
)

private val LightColorScheme = lightColorScheme(
    primary = NyintCyanDark,
    onPrimary = NyintSurfaceLight,
    primaryContainer = Color(0xFFE0F7FA),
    onPrimaryContainer = Color(0xFF006064),
    secondary = NyintVioletDark,
    onSecondary = NyintSurfaceLight,
    secondaryContainer = Color(0xFFF3E5F5),
    onSecondaryContainer = Color(0xFF4A148C),
    tertiary = NyintAmber,
    onTertiary = NyintSurfaceLight,
    background = NyintBgLight,
    onBackground = NyintTextPrimaryLight,
    surface = NyintSurfaceLight,
    onSurface = NyintTextPrimaryLight,
    surfaceVariant = NyintSurfaceElevatedLight,
    onSurfaceVariant = NyintTextSecondaryLight,
    outline = NyintCardBorderLight,
    error = NyintRed,
    onError = NyintSurfaceLight
)

@Composable
fun ANyintAITheme(
    darkTheme: Boolean = true, // Default to premium dark
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.background.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
