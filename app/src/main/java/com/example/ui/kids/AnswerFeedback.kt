package com.example.ui.kids

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

/**
 * Horizontal "no!" shake, triggered when [trigger] flips true.
 * Used on the wrongly-selected answer button.
 */
fun Modifier.shake(trigger: Boolean): Modifier = composed {
    val offsetX = remember { Animatable(0f) }
    LaunchedEffect(trigger) {
        if (trigger) {
            offsetX.animateTo(
                targetValue = 0f,
                animationSpec = keyframes {
                    durationMillis = 450
                    0f at 0
                    -14f at 60
                    12f at 130
                    -9f at 200
                    7f at 270
                    -4f at 340
                    0f at 450
                }
            )
        }
    }
    this.offset { IntOffset(offsetX.value.roundToInt(), 0) }
}

/**
 * "+1 ⭐" that floats up from the answer area toward the score capsule and
 * fades out. Re-fires on every [trigger] pulse (use the question index +
 * correctness as the key).
 */
@Composable
fun FloatingStarBurst(trigger: Any?, visible: Boolean, modifier: Modifier = Modifier) {
    var animating by remember { mutableStateOf(false) }
    val progress = remember { Animatable(0f) }

    LaunchedEffect(trigger) {
        if (visible) {
            animating = true
            progress.snapTo(0f)
            progress.animateTo(1f, animationSpec = keyframes { durationMillis = 750 })
            animating = false
        }
    }

    if (animating) {
        val p = progress.value
        Text(
            text = "+1 ⭐",
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            color = KidsTheme.SunnyYellow,
            modifier = modifier.graphicsLayer {
                translationY = -180f * p
                alpha = 1f - (p * p) // ease-in fade
                scaleX = 1f + 0.3f * p
                scaleY = 1f + 0.3f * p
            }
        )
    }
}
