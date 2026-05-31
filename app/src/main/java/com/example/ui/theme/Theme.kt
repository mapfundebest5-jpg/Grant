package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = FacebookBlue,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    background = Color(0xFF18191A),
    surface = Color(0xFF242526),
    onPrimary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = FacebookBlue,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    background = FacebookLightGray,
    surface = FacebookWhite,
    onPrimary = Color.White,
    onBackground = FacebookTextPrimary,
    onSurface = FacebookTextPrimary,
    outline = FacebookBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
