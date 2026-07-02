package com.example.ui.kids

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Duolingo-style progress bar: springy fill, green gradient, soft highlight
 * stripe on top of the fill, shimmer sweep while active.
 */
@Composable
fun JuicyProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    trackColor: Color = Color(0xFFE5E5E5),
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = kidsSnappySpring(),
        label = "juicy_progress"
    )

    val shimmer = rememberInfiniteTransition(label = "progress_shimmer")
    val shimmerX by shimmer.animateFloat(
        initialValue = -0.3f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_x"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(16.dp)
            .clip(RoundedCornerShape(100.dp))
            .background(trackColor)
    ) {
        if (animatedProgress > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(100.dp))
                    .background(KidsTheme.ProgressGradient)
            ) {
                // Glossy highlight strip
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .padding(horizontal = 8.dp)
                        .padding(top = 3.dp)
                        .clip(RoundedCornerShape(100.dp))
                        .background(Color.White.copy(alpha = 0.35f))
                )
                // Shimmer sweep
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(0.18f)
                        .background(
                            Brush.horizontalGradient(
                                0f to Color.Transparent,
                                0.5f to Color.White.copy(alpha = 0.25f),
                                1f to Color.Transparent,
                                startX = 0f,
                            )
                        )
                        .align(androidx.compose.ui.BiasAlignment(shimmerX * 2f - 1f, 0f))
                )
            }
        }
    }
}
