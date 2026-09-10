package org.openflux.app

import androidx.compose.runtime.staticCompositionLocalOf

/** Provided once at the Compose root (MainActivity) from applicationContext. */
val LocalOpenFluxApp = staticCompositionLocalOf<OpenFluxApplication> {
    error("LocalOpenFluxApp not provided")
}
