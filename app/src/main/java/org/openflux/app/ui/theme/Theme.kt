package org.openflux.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Same accent family as the controlplane web admin panel (see
// server/controlplane/internal/api/web/admin.html) - one brand across the
// whole product instead of each surface inventing its own palette.
private val DarkColors = darkColorScheme(
    primary = Color(0xFF7BA2F5),
    onPrimary = Color(0xFF071224),
    primaryContainer = Color(0xFF223A5E),
    onPrimaryContainer = Color(0xFFD3E2FF),
    secondary = Color(0xFF9AA8C7),
    onSecondary = Color(0xFF16202F),
    tertiary = Color(0xFFE0AC2B),
    onTertiary = Color(0xFF2A1E00),
    error = Color(0xFFEF5A5A),
    onError = Color(0xFF2A0A0A),
    background = Color(0xFF0A0E16),
    onBackground = Color(0xFFE8ECF4),
    surface = Color(0xFF121826),
    onSurface = Color(0xFFE8ECF4),
    surfaceVariant = Color(0xFF171F30),
    onSurfaceVariant = Color(0xFF8993A8),
    outline = Color(0xFF3A4457),
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF3D6FD6),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFDCE6FB),
    onPrimaryContainer = Color(0xFF14284F),
    secondary = Color(0xFF5B6472),
    onSecondary = Color(0xFFFFFFFF),
    tertiary = Color(0xFF9C6F00),
    onTertiary = Color(0xFFFFFFFF),
    error = Color(0xFFC4453D),
    onError = Color(0xFFFFFFFF),
    background = Color(0xFFF7F9FC),
    onBackground = Color(0xFF1A2233),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1A2233),
    surfaceVariant = Color(0xFFEEF1F7),
    onSurfaceVariant = Color(0xFF5B6472),
    outline = Color(0xFFD8DEE9),
)

/**
 * Deliberately does not offer Material You dynamic color: deriving every
 * button/text/border color from the device wallpaper meant some wallpapers
 * produced washed-out, barely-visible buttons (and gave up OpenFlux's own
 * brand identity for no real benefit). This is a fixed, hand-picked palette
 * instead, tuned for contrast in both themes.
 */
@Composable
fun OpenFluxTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(colorScheme = if (darkTheme) DarkColors else LightColors, content = content)
}
