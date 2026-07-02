package com.example.ui.kids

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Row of hearts (lives). A heart that was just lost plays a "crack" animation:
 * it pops bigger, tilts, fades to the empty state.
 */
@Composable
fun HeartsRow(heartsRemaining: Int, maxHearts: Int = 3, modifier: Modifier = Modifier) {
    var previousHearts by remember { mutableStateOf(heartsRemaining) }
    var crackingIndex by remember { mutableStateOf(-1) }

    LaunchedEffect(heartsRemaining) {
        if (heartsRemaining < previousHearts) {
            crackingIndex = heartsRemaining // the heart that was just lost
            kotlinx.coroutines.delay(700L)
            crackingIndex = -1
        }
        previousHearts = heartsRemaining
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until maxHearts) {
            val filled = i < heartsRemaining
            val cracking = i == crackingIndex

            val scale by animateFloatAsState(
                targetValue = if (cracking) 1.5f else 1f,
                animationSpec = if (cracking) kidsBouncySpring() else tween(250),
                label = "heart_scale_$i"
            )
            val rotation by animateFloatAsState(
                targetValue = if (cracking) 20f else 0f,
                animationSpec = tween(400),
                label = "heart_rot_$i"
            )
            val alpha by animateFloatAsState(
                targetValue = if (filled || cracking) 1f else 0.35f,
                animationSpec = tween(400),
                label = "heart_alpha_$i"
            )

            Text(
                text = if (filled || cracking) "❤️" else "🖤",
                fontSize = 16.sp,
                modifier = Modifier
                    .scale(scale)
                    .rotate(rotation)
                    .graphicsLayer { this.alpha = alpha }
            )
        }
    }
}

/**
 * Combo badge — appears at 2 consecutive correct answers, escalates color and
 * copy at higher tiers, adds a fire ring vibe at 5+.
 */
@Composable
fun ComboBadge(comboCount: Int, modifier: Modifier = Modifier) {
    AnimatedVisibility(
        visible = comboCount >= 2,
        enter = scaleIn(animationSpec = kidsBouncySpring()),
        exit = scaleOut() + fadeOut(),
        modifier = modifier
    ) {
        val tierColor = when {
            comboCount >= 5 -> KidsTheme.ComboColors[2]
            comboCount >= 3 -> KidsTheme.ComboColors[1]
            else -> KidsTheme.ComboColors[0]
        }
        val label = when {
            comboCount >= 5 -> "🔥 x$comboCount ON FIRE!"
            comboCount >= 3 -> "⚡ x$comboCount Amazing!"
            else -> "✨ x$comboCount Combo!"
        }

        // Re-pop on every combo increase
        var pop by remember { mutableStateOf(false) }
        LaunchedEffect(comboCount) {
            pop = true
            kotlinx.coroutines.delay(220L)
            pop = false
        }
        val popScale by animateFloatAsState(
            targetValue = if (pop) 1.25f else 1f,
            animationSpec = kidsBouncySpring(),
            label = "combo_pop"
        )

        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Black,
            color = KidsTheme.TextOnColor,
            modifier = Modifier
                .scale(popScale)
                .background(tierColor, RoundedCornerShape(100.dp))
                .padding(horizontal = 12.dp, vertical = 5.dp)
        )
    }
}
