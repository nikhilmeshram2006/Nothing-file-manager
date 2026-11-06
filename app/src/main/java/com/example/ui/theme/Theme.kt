package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val NothingDarkColorScheme = darkColorScheme(
    primary = NothingWhite,
    onPrimary = NothingBlack,
    primaryContainer = NothingSurfaceElevated,
    onPrimaryContainer = NothingWhite,
    secondary = NothingRed,
    onSecondary = NothingWhite,
    secondaryContainer = NothingRedDim,
    onSecondaryContainer = NothingWhite,
    tertiary = NothingGray400,
    onTertiary = NothingBlack,
    background = NothingBlack,
    onBackground = NothingWhite,
    surface = NothingSurfaceDark,
    onSurface = NothingWhite,
    surfaceVariant = NothingSurfaceElevated,
    onSurfaceVariant = NothingSubtextDark,
    outline = NothingBorderDark,
    outlineVariant = NothingGray700,
    error = NothingRedBright,
    onError = NothingWhite
)

private val NothingLightColorScheme = lightColorScheme(
    primary = NothingBlack,
    onPrimary = NothingWhite,
    primaryContainer = NothingSurfaceLightElevated,
    onPrimaryContainer = NothingBlack,
    secondary = NothingRed,
    onSecondary = NothingWhite,
    secondaryContainer = NothingRedDim,
    onSecondaryContainer = NothingWhite,
    tertiary = NothingGray600,
    onTertiary = NothingWhite,
    background = NothingWhite,
    onBackground = NothingBlack,
    surface = NothingSurfaceLight,
    onSurface = NothingBlack,
    surfaceVariant = NothingSurfaceLightElevated,
    onSurfaceVariant = NothingSubtextLight,
    outline = NothingBorderLight,
    outlineVariant = NothingGray200,
    error = NothingRed,
    onError = NothingWhite
)

@Composable
fun NothingFilesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) NothingDarkColorScheme else NothingLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = NothingTypography,
        content = content
    )
}
