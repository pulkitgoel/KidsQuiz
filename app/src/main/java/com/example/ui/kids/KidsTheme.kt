package com.example.ui.kids

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Design tokens for the kids-facing screens. Vibrant, game-like palette
 * (Duolingo-inspired) — all new kid UI should reference these instead of
 * inline hex values.
 */
object KidsTheme {
    // Core feedback colors
    val SuccessGreen = Color(0xFF58CC02)
    val SuccessGreenDark = Color(0xFF46A302)
    val SuccessGreenLight = Color(0xFFD7FFB8)
    val ErrorRed = Color(0xFFFF4B4B)
    val ErrorRedDark = Color(0xFFD33131)
    val ErrorRedLight = Color(0xFFFFDFE0)

    // Brand / accents
    val SunnyYellow = Color(0xFFFFC800)
    val SkyBlue = Color(0xFF1CB0F6)
    val SkyBlueDark = Color(0xFF1899D6)
    val FunPurple = Color(0xFFCE82FF)
    val FunPurpleDark = Color(0xFFA560E8)
    val HotPink = Color(0xFFFF86D0)

    // Flame / streak
    val FlameOrange = Color(0xFFFF9600)
    val FlameOrangeDeep = Color(0xFFFF6B00)
    val FlameYellow = Color(0xFFFFD900)
    val FlameGrey = Color(0xFFAFAFAF) // unlit flame

    // Hearts
    val HeartRed = Color(0xFFFF4B4B)
    val HeartEmpty = Color(0xFFE5E5E5)

    // Combo tiers: index by min(comboTier, size-1)
    val ComboColors = listOf(
        Color(0xFF1CB0F6), // x2 blue
        Color(0xFFCE82FF), // x3 purple
        Color(0xFFFF9600), // x5+ fire orange
    )

    // Text
    val TextDark = Color(0xFF3C3C3C)
    val TextMuted = Color(0xFF777777)
    val TextOnColor = Color.White

    // Surfaces
    val SurfaceBright = Color(0xFFFFFFFF)
    val SurfaceCream = Color(0xFFFFF9EB)
    val SurfaceSky = Color(0xFFEAF7FF)

    // Gradients
    val CelebrationGradient = Brush.verticalGradient(listOf(Color(0xFFFFE29A), Color(0xFFFFB7B2)))
    val FlameGradient = Brush.verticalGradient(listOf(FlameYellow, FlameOrange, FlameOrangeDeep))
    val ProgressGradient = Brush.horizontalGradient(listOf(SuccessGreen, Color(0xFF8EE000)))

    // Confetti particle palette
    val ConfettiColors = listOf(
        Color(0xFFFF4B4B), Color(0xFFFFC800), Color(0xFF58CC02),
        Color(0xFF1CB0F6), Color(0xFFCE82FF), Color(0xFFFF86D0),
    )

    // Timing
    const val FEEDBACK_DURATION_MS = 800L
}

/** Springy spec for playful pop-in effects. */
fun <T> kidsBouncySpring() = spring<T>(
    dampingRatio = Spring.DampingRatioMediumBouncy,
    stiffness = Spring.StiffnessMediumLow,
)

/** Snappier spec for progress/feedback movement. */
fun <T> kidsSnappySpring() = spring<T>(
    dampingRatio = Spring.DampingRatioLowBouncy,
    stiffness = Spring.StiffnessMedium,
)
