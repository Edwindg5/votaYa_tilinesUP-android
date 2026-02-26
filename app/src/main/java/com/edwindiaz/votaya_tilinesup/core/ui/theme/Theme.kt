package com.edwindiaz.votaya_tilinesup.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = VotaPurple,
    onPrimary = OnSurfaceDark,
    primaryContainer = VotaPurpleDark,
    onPrimaryContainer = VotaPurpleLight,
    secondary = VotaAccent,
    onSecondary = BackgroundDark,
    secondaryContainer = VotaAccentLight,
    background = BackgroundDark,
    surface = SurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onBackground = OnSurfaceDark,
    onSurface = OnSurfaceDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    error = VotaError,
)

@Composable
fun VotaYaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}