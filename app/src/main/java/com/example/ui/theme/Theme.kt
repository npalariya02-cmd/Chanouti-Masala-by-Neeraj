package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = TurmericPrimary,
    onPrimary = Color.White,
    primaryContainer = TurmericLightContainer,
    onPrimaryContainer = OnTurmericContainer,
    secondary = ChiliRed,
    onSecondary = Color.White,
    secondaryContainer = ChiliRedLightContainer,
    onSecondaryContainer = OnChiliContainer,
    tertiary = CardamomGreen,
    onTertiary = Color.White,
    tertiaryContainer = CardamomLightContainer,
    onTertiaryContainer = OnCardamomContainer,
    background = WarmBackground,
    onBackground = SlateInk,
    surface = PureWhiteSurface,
    onSurface = SlateInk,
    surfaceVariant = WarmSurfaceContainer,
    onSurfaceVariant = MutedSlate,
    surfaceContainerLowest = PureWhiteSurface,
    surfaceContainerLow = WarmSurfaceContainerLow,
    surfaceContainer = WarmSurfaceContainer,
    surfaceContainerHigh = WarmSurfaceContainerHigh,
    outline = WarmOutline,
    outlineVariant = WarmOutlineVariant,
    error = ChiliRed,
    onError = Color.White,
    errorContainer = ChiliRedLightContainer,
    onErrorContainer = OnChiliContainer
)

private val DarkColorScheme = darkColorScheme(
    primary = TurmericDarkPrimary,
    onPrimary = Color(0xFF2B1300),
    primaryContainer = TurmericDarkContainer,
    onPrimaryContainer = TurmericLightContainer,
    secondary = ChiliDarkSecondary,
    onSecondary = Color(0xFF450A0A),
    secondaryContainer = ChiliDarkContainer,
    onSecondaryContainer = ChiliRedLightContainer,
    tertiary = CardamomDarkGreen,
    onTertiary = Color(0xFF052E16),
    tertiaryContainer = Color(0xFF14532D),
    onTertiaryContainer = CardamomLightContainer,
    background = DarkEspressoBackground,
    onBackground = WarmCreamText,
    surface = DarkEspressoSurface,
    onSurface = WarmCreamText,
    surfaceVariant = DarkEspressoSurfaceHigh,
    onSurfaceVariant = WarmMutedText,
    surfaceContainerLowest = Color(0xFF120D0A),
    surfaceContainerLow = DarkEspressoSurface,
    surfaceContainer = DarkEspressoSurfaceHigh,
    surfaceContainerHigh = Color(0xFF3B2E23),
    outline = Color(0xFF57473B),
    outlineVariant = Color(0xFF3E3229),
    error = ChiliDarkSecondary,
    onError = Color(0xFF450A0A),
    errorContainer = ChiliDarkContainer,
    onErrorContainer = ChiliRedLightContainer
)

val ChanoutiShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun ChanoutiMasalaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = ChanoutiTypography,
        shapes = ChanoutiShapes,
        content = content
    )
}
