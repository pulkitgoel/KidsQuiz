package com.example.ui.kids

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.sin

/**
 * Canvas flame that flickers and sways. [intensity] 0..1 scales the inner
 * glow and flicker energy with streak length.
 */
@Composable
fun AnimatedFlame(size: Dp, intensity: Float = 0.5f, lit: Boolean = true, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "flame")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(if (lit) (900 - (intensity * 350)).toInt() else 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "flame_phase"
    )

    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val flicker = if (lit) 1f + sin(phase) * (0.05f + 0.06f * intensity) else 1f
        val sway = if (lit) sin(phase * 0.7f) * w * 0.03f else 0f

        val outer = if (lit) KidsTheme.FlameOrange else KidsTheme.FlameGrey
        val mid = if (lit) KidsTheme.FlameYellow else Color(0xFFCFCFCF)
        val core = if (lit) Color.White else Color(0xFFE8E8E8)

        fun flamePath(scale: Float, xShift: Float): Path = Path().apply {
            val fw = w * 0.72f * scale * flicker
            val fh = h * 0.9f * scale * flicker
            val cx = w / 2f + xShift
            val base = h * 0.95f
            moveTo(cx, base - fh)                       // tip
            cubicTo(
                cx + fw * 0.55f, base - fh * 0.55f,
                cx + fw * 0.50f, base - fh * 0.18f,
                cx, base
            )
            cubicTo(
                cx - fw * 0.50f, base - fh * 0.18f,
                cx - fw * 0.55f, base - fh * 0.55f,
                cx, base - fh
            )
            close()
        }

        drawPath(flamePath(1f, sway), color = outer)
        drawPath(flamePath(0.62f, sway * 1.4f), color = mid)
        drawPath(flamePath(0.32f, sway * 1.8f), color = core.copy(alpha = 0.9f))
    }
}

/**
 * Streak pill for the home top bar: animated flame + day count. Greyed-out
 * flame when the streak is 0.
 */
@Composable
fun StreakCapsule(streakCount: Int, todayDone: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val lit = streakCount > 0
    // Gentle pulse when today's quiz is still pending (streak at risk)
    val pulse = rememberInfiniteTransition(label = "streak_pulse")
    val pulseScale by pulse.animateFloat(
        initialValue = 1f,
        targetValue = if (lit && !todayDone) 1.12f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(650),
            repeatMode = RepeatMode.Reverse
        ),
        label = "streak_pulse_scale"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
            .background(
                color = if (lit) KidsTheme.FlameOrange.copy(alpha = 0.14f) else Color(0xFFF3EDF7),
                shape = RoundedCornerShape(100.dp)
            )
            .border(
                width = 1.5.dp,
                color = if (lit) KidsTheme.FlameOrange.copy(alpha = 0.6f) else Color(0xFFCAC4D0),
                shape = RoundedCornerShape(100.dp)
            )
            .bouncyClickKids(onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Box(modifier = Modifier.scale(pulseScale)) {
            AnimatedFlame(size = 20.dp, intensity = (streakCount / 10f).coerceIn(0.2f, 1f), lit = lit)
        }
        Text(
            text = "$streakCount",
            fontWeight = FontWeight.Black,
            fontSize = 15.sp,
            color = if (lit) KidsTheme.FlameOrangeDeep else Color(0xFF49454F)
        )
    }
}

/** Amber banner shown when the streak survives from yesterday but today's quiz isn't done. */
@Composable
fun StreakAtRiskBanner(streakCount: Int, urgent: Boolean, onStartQuiz: () -> Unit, modifier: Modifier = Modifier) {
    val pulse = rememberInfiniteTransition(label = "risk_pulse")
    val glow by pulse.animateFloat(
        initialValue = if (urgent) 0.75f else 1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(700), repeatMode = RepeatMode.Reverse),
        label = "risk_glow"
    )

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3D6).copy(alpha = glow)),
        border = androidx.compose.foundation.BorderStroke(2.dp, KidsTheme.FlameOrange.copy(alpha = 0.6f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AnimatedFlame(size = 34.dp, intensity = 0.8f)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Don't lose your $streakCount-day streak!",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = KidsTheme.FlameOrangeDeep
                )
                Text(
                    text = "Play one quiz today to keep the flame alive!",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = KidsTheme.TextMuted
                )
            }
            Button(
                onClick = onStartQuiz,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = KidsTheme.FlameOrange),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text("GO!", fontWeight = FontWeight.Black, fontSize = 14.sp, color = Color.White)
            }
        }
    }
}

/** Local bouncy-click (mirror of the one in QuizAppUI to avoid a cross-file dependency). */
private fun Modifier.bouncyClickKids(onClick: () -> Unit): Modifier =
    composed {
        val interactionSource = androidx.compose.runtime.remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()
        val scale by androidx.compose.animation.core.animateFloatAsState(
            targetValue = if (isPressed) 0.9f else 1f,
            animationSpec = kidsBouncySpring(),
            label = "bouncy_kids"
        )
        this
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    }
