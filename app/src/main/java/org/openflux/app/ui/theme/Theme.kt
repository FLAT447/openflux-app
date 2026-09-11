package org.openflux.app.ui.theme

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

// Same accent family as the controlplane web admin panel (see
// server/controlplane/internal/api/web/admin.html) - one brand across the
// whole product instead of each surface inventing its own palette.
//
// Every role Material3 defines is set explicitly here (not just the handful
// a screen visibly reads) - lightColorScheme()/darkColorScheme() fall back to
// M3's stock purple baseline for anything left unset, which is how a Card or
// TextField could end up tinted purple against an otherwise all-blue app
// without either color ever being written down anywhere.
private val DarkColors = darkColorScheme(
    primary = Color(0xFF7BA2F5),
    onPrimary = Color(0xFF071224),
    primaryContainer = Color(0xFF223A5E),
    onPrimaryContainer = Color(0xFFD3E2FF),
    inversePrimary = Color(0xFF3D6FD6),
    secondary = Color(0xFF9AA8C7),
    onSecondary = Color(0xFF16202F),
    secondaryContainer = Color(0xFF2B3547),
    onSecondaryContainer = Color(0xFFD7DEEC),
    tertiary = Color(0xFFE0AC2B),
    onTertiary = Color(0xFF2A1E00),
    tertiaryContainer = Color(0xFF4A3800),
    onTertiaryContainer = Color(0xFFFFDFA0),
    error = Color(0xFFEF5A5A),
    onError = Color(0xFF2A0A0A),
    errorContainer = Color(0xFF5C1A1A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF0A0E16),
    onBackground = Color(0xFFE8ECF4),
    surface = Color(0xFF121826),
    onSurface = Color(0xFFE8ECF4),
    surfaceVariant = Color(0xFF171F30),
    onSurfaceVariant = Color(0xFF8993A8),
    surfaceTint = Color(0xFF7BA2F5),
    surfaceDim = Color(0xFF0A0E16),
    surfaceBright = Color(0xFF2A3142),
    surfaceContainerLowest = Color(0xFF060A11),
    surfaceContainerLow = Color(0xFF0F1420),
    surfaceContainer = Color(0xFF141B29),
    surfaceContainerHigh = Color(0xFF1B2333),
    surfaceContainerHighest = Color(0xFF232C3E),
    outline = Color(0xFF3A4457),
    outlineVariant = Color(0xFF272F40),
    scrim = Color(0xFF000000),
    inverseSurface = Color(0xFFE8ECF4),
    inverseOnSurface = Color(0xFF1A2233),
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF3D6FD6),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFDCE6FB),
    onPrimaryContainer = Color(0xFF14284F),
    inversePrimary = Color(0xFF7BA2F5),
    secondary = Color(0xFF5B6472),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE1E6F0),
    onSecondaryContainer = Color(0xFF1B2331),
    tertiary = Color(0xFF9C6F00),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFE0A3),
    onTertiaryContainer = Color(0xFF2E2100),
    error = Color(0xFFC4453D),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410E0B),
    background = Color(0xFFF7F9FC),
    onBackground = Color(0xFF1A2233),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1A2233),
    surfaceVariant = Color(0xFFEEF1F7),
    onSurfaceVariant = Color(0xFF5B6472),
    surfaceTint = Color(0xFF3D6FD6),
    surfaceDim = Color(0xFFDCE0E9),
    surfaceBright = Color(0xFFFFFFFF),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF3F5FA),
    surfaceContainer = Color(0xFFEDF0F6),
    surfaceContainerHigh = Color(0xFFE7EBF2),
    surfaceContainerHighest = Color(0xFFE1E6EE),
    outline = Color(0xFFD8DEE9),
    outlineVariant = Color(0xFFE7EBF2),
    scrim = Color(0xFF000000),
    inverseSurface = Color(0xFF2E3648),
    inverseOnSurface = Color(0xFFF1F4F9),
)

/**
 * Deliberately does not offer Material You dynamic color: deriving every
 * button/text/border color from the device wallpaper meant some wallpapers
 * produced washed-out, barely-visible buttons (and gave up OpenFlux's own
 * brand identity for no real benefit). This is a fixed, hand-picked palette
 * instead, tuned for contrast in both themes, paired with OpenFluxTypography
 * (Inter) and OpenFluxShapes so nothing here falls back to Material3's
 * stock look.
 */
@Composable
fun OpenFluxTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) DarkColors else LightColors

    // Recolors the system status/nav bars to match instead of leaving them
    // whatever the pre-Compose window theme set (see values*/themes.xml) -
    // otherwise a mismatched bar color is the first thing that reads as
    // "not a real app" on every single screen.
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colors.background.toArgb()
            window.navigationBarColor = colors.background.toArgb()
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colors,
        typography = OpenFluxTypography,
        shapes = OpenFluxShapes,
        content = content,
    )
}
