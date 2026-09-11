package org.openflux.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Rounder than Material3's defaults (4/8/12/16/28dp) - cards, dialogs, and
// text fields read as noticeably more "designed" with a bit more curve, and
// nothing here changes where anything sits, only its corners.
val OpenFluxShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
)
