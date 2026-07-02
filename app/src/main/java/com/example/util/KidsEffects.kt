package com.example.util

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * CompositionLocals for the game-feedback managers. Provided by MainActivity;
 * the error defaults make missing wiring loud in debug rather than silently
 * mute. Tests/previews can provide their own no-op fakes.
 */
val LocalSoundManager = staticCompositionLocalOf<SoundManager> {
    error("SoundManager not provided")
}

val LocalHapticsManager = staticCompositionLocalOf<HapticsManager> {
    error("HapticsManager not provided")
}
