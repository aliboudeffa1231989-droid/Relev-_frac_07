package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val IndustrialLightColorScheme = lightColorScheme(
    primary = Sky600,
    onPrimary = Color.White,
    primaryContainer = Sky100,
    onPrimaryContainer = Sky900,
    secondary = Amber600,
    onSecondary = Color.White,
    secondaryContainer = Amber100,
    onSecondaryContainer = Amber900,
    tertiary = Emerald600,
    onTertiary = Color.White,
    tertiaryContainer = Emerald100,
    onTertiaryContainer = Emerald900,
    background = SoftAlabaster,
    onBackground = Slate900,
    surface = PureWhite,
    onSurface = Slate900,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = Slate600,
    surfaceContainerLowest = PureWhite,
    surfaceContainerLow = SoftAlabaster,
    surfaceContainer = PureWhite,
    surfaceContainerHigh = LightSurfaceVariant,
    surfaceContainerHighest = Slate200,
    outline = LightBorderSlate,
    outlineVariant = Slate200,
    error = Rose600,
    onError = Color.White,
    errorContainer = Rose100,
    onErrorContainer = Rose900
)

private val IndustrialDarkColorScheme = darkColorScheme(
    primary = Amber400,
    onPrimary = Slate950,
    primaryContainer = Amber900,
    onPrimaryContainer = Amber100,
    secondary = Sky400,
    onSecondary = Slate950,
    secondaryContainer = Sky900,
    onSecondaryContainer = Sky100,
    tertiary = Emerald400,
    onTertiary = Slate950,
    tertiaryContainer = Emerald900,
    onTertiaryContainer = Emerald100,
    background = Slate950,
    onBackground = Slate50,
    surface = Slate900,
    onSurface = Slate50,
    surfaceVariant = Slate800,
    onSurfaceVariant = Slate400,
    outline = Slate600,
    error = Rose400,
    onError = Slate950,
    errorContainer = Rose900,
    onErrorContainer = Rose100
)

@Composable
fun PetroLogTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) IndustrialDarkColorScheme else IndustrialLightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.surface.toArgb()
                window.navigationBarColor = colorScheme.surface.toArgb()
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
