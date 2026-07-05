package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = BrightEmerald,
    secondary = CorporateBlue,
    tertiary = ElectricGreen,
    background = DarkBg,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    onSurfaceVariant = Color(0xFFC9D1D9)
)

private val LightColorScheme = lightColorScheme(
    primary = ElectricGreen,
    secondary = CorporateBlue,
    tertiary = BrightEmerald,
    background = CleanOffWhite,
    surface = PureWhite,
    surfaceVariant = Color(0xFFF0F2F5),
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = DarkText,
    onSurface = DarkText,
    onSurfaceVariant = MutedLightGrey
)

@Composable
fun TansionGoTheme(
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

// Keep a backwards compatible alias in case template-generated classes use it.
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Ignored to preserve our premium hand-crafted brand theme colors!
    content: @Composable () -> Unit
) {
    TansionGoTheme(darkTheme = darkTheme, content = content)
}
