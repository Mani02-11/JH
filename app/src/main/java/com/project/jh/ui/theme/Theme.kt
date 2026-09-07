package com.project.jh.ui.theme

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
    primary = JHPrimary,
    onPrimary = Color.White,
    primaryContainer = JHPrimaryDark,
    secondary = JHPrimary,
    onSecondary = Color.White,
    background = JHSpaceBlack,
    surface = Color.White.copy(alpha = 0.05f), // Transparent surface for glass
    onBackground = Color.White,
    onSurface = Color.White
)

@Composable
fun JHTheme(
    darkTheme: Boolean = true, // Force dark theme throughout the app
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // Force the dark color scheme to keep the "Space" aesthetic consistent
    val colorScheme = DarkColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
