package com.example.brokerapp.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

val DarkGreen = Color(0xFF00C853)
val DeepBlue = Color(0xFF1A237E)
val DarkGray = Color(0xFF121212)
val SurfaceGray = Color(0xFF1E1E1E)
val LightGray = Color(0xFFF5F5F5)
val BorderGray = Color(0xFFE0E0E0)
val TextPrimary = Color(0xFF1A1A1A)
val TextSecondary = Color(0xFF757575)

private val DarkColorScheme = darkColorScheme(
    primary = DarkGreen,
    secondary = DarkGreen.copy(alpha = 0.8f),
    tertiary = Color(0xFF4A4A4A),
    background = DarkGray,
    surface = SurfaceGray,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = DeepBlue,
    secondary = DarkGreen,
    tertiary = Color(0xFFF5F5F5),
    background = LightGray,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

@Composable
fun BrokerAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        shapes = Shapes(
            small = RoundedCornerShape(8.dp),
            medium = RoundedCornerShape(12.dp),
            large = RoundedCornerShape(16.dp)
        ),
        content = content
    )
}