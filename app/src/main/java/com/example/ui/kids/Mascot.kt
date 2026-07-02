package com.example.ui.kids

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** Emotional states for the mascot. Matches the states used across the kid screens. */
enum class MascotState {
    IDLE,       // gentle bob, blinking, soft smile
    CHEER,      // correct answer: bounce, big smile, arms up
    ENCOURAGE,  // wrong answer / rescue: concerned brows, gentle frown
    THINKING,   // waiting: eyes up, thought dots
    CELEBRATE,  // results / combos: fast bounce, star eyes, sparkles
}

/**
 * "Quizzy" — the code-built mascot: a round, cheerful blob with big eyes,
 * stubby arms and lots of squash-and-stretch. All Canvas, no assets.
 */
@Composable
fun Mascot(state: MascotState, modifier: Modifier = Modifier) {
    // --- Continuous drivers ---
    val transition = rememberInfiniteTransition(label = "mascot")
    val bobPhase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (state) {
                    MascotState.CELEBRATE -> 500
                    MascotState.CHEER -> 700
                    else -> 2200
                },
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "bob"
    )
    val sparklePhase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sparkle"
    )

    // --- Blink loop (skipped for star-eyed celebrate) ---
    val eyelid = remember { Animatable(0f) } // 0 open, 1 closed
    LaunchedEffect(state) {
        if (state != MascotState.CELEBRATE) {
            while (true) {
                kotlinx.coroutines.delay(if (state == MascotState.IDLE) 2600L else 3400L)
                eyelid.animateTo(1f, tween(70))
                eyelid.animateTo(0f, tween(90))
                // occasional double blink for personality
                if (state == MascotState.IDLE) {
                    kotlinx.coroutines.delay(180L)
                    eyelid.animateTo(1f, tween(60))
                    eyelid.animateTo(0f, tween(80))
                }
            }
        } else {
            eyelid.snapTo(0f)
        }
    }

    // --- State-driven feature targets ---
    val mouthCurve by animateFloatAsState(
        targetValue = when (state) {
            MascotState.CHEER, MascotState.CELEBRATE -> 1f
            MascotState.ENCOURAGE -> -0.6f
            MascotState.THINKING -> 0.1f
            else -> 0.6f
        },
        animationSpec = kidsBouncySpring(), label = "mouth"
    )
    val mouthOpen by animateFloatAsState(
        targetValue = when (state) {
            MascotState.CHEER -> 0.7f
            MascotState.CELEBRATE -> 1f
            else -> 0f
        },
        animationSpec = kidsBouncySpring(), label = "mouthOpen"
    )
    val armRaise by animateFloatAsState(
        targetValue = when (state) {
            MascotState.CHEER, MascotState.CELEBRATE -> 1f
            MascotState.THINKING -> 0.5f
            else -> 0f
        },
        animationSpec = kidsBouncySpring(), label = "arms"
    )
    val browWorry by animateFloatAsState(
        targetValue = if (state == MascotState.ENCOURAGE) 1f else 0f,
        animationSpec = tween(300), label = "brows"
    )
    val pupilUp by animateFloatAsState(
        targetValue = if (state == MascotState.THINKING) 1f else 0f,
        animationSpec = tween(300), label = "pupils"
    )

    val bounceHeight = when (state) {
        MascotState.CELEBRATE -> 0.10f
        MascotState.CHEER -> 0.07f
        MascotState.ENCOURAGE -> 0.015f
        else -> 0.03f
    }
    val bob = sin(bobPhase) * bounceHeight
    val squash = 1f - bob * 0.6f // squashes at the bottom of a bounce

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationY = -bob * size.height
                    scaleX = 2f - squash
                    scaleY = squash
                    transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 1f)
                }
        ) {
            drawQuizzy(
                state = state,
                eyelid = eyelid.value,
                mouthCurve = mouthCurve,
                mouthOpen = mouthOpen,
                armRaise = armRaise,
                browWorry = browWorry,
                pupilUp = pupilUp,
                sparklePhase = sparklePhase,
            )
        }
    }
}

private fun DrawScope.drawQuizzy(
    state: MascotState,
    eyelid: Float,
    mouthCurve: Float,
    mouthOpen: Float,
    armRaise: Float,
    browWorry: Float,
    pupilUp: Float,
    sparklePhase: Float,
) {
    val w = size.width
    val h = size.height
    val cx = w / 2f
    val bodyR = w * 0.36f
    val bodyCy = h * 0.58f

    val bodyBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFF4FC3F7), Color(0xFF1E88E5)),
        startY = bodyCy - bodyR,
        endY = bodyCy + bodyR,
    )

    // Shadow
    drawOval(
        color = Color.Black.copy(alpha = 0.10f),
        topLeft = Offset(cx - bodyR * 0.8f, h * 0.90f),
        size = Size(bodyR * 1.6f, bodyR * 0.28f)
    )

    // Arms (behind body) — stubby rounded lines that swing up when raised
    val armColor = Color(0xFF1E88E5)
    val armY = bodyCy + bodyR * 0.05f
    val armLen = bodyR * 0.75f
    for (side in listOf(-1f, 1f)) {
        val baseX = cx + side * bodyR * 0.85f
        // interpolate angle: down (-40°) → up (+55°)
        val angleDeg = -40f + 95f * armRaise
        val angleRad = Math.toRadians(angleDeg.toDouble()).toFloat()
        val endX = baseX + side * cos(angleRad) * armLen
        val endY = armY - sin(angleRad) * armLen
        drawLine(
            color = armColor,
            start = Offset(baseX, armY),
            end = Offset(endX, endY),
            strokeWidth = bodyR * 0.28f,
            cap = StrokeCap.Round
        )
    }

    // Body
    drawCircle(brush = bodyBrush, radius = bodyR, center = Offset(cx, bodyCy))
    // Belly highlight
    drawCircle(
        color = Color.White.copy(alpha = 0.35f),
        radius = bodyR * 0.62f,
        center = Offset(cx, bodyCy + bodyR * 0.28f)
    )

    // Antenna with bobble
    drawLine(
        color = Color(0xFF1565C0),
        start = Offset(cx, bodyCy - bodyR * 0.95f),
        end = Offset(cx + bodyR * 0.12f, bodyCy - bodyR * 1.28f),
        strokeWidth = bodyR * 0.08f,
        cap = StrokeCap.Round
    )
    drawCircle(
        color = KidsTheme.SunnyYellow,
        radius = bodyR * 0.13f,
        center = Offset(cx + bodyR * 0.14f, bodyCy - bodyR * 1.33f)
    )

    // Eyes
    val eyeY = bodyCy - bodyR * 0.22f
    val eyeDx = bodyR * 0.38f
    val eyeR = bodyR * 0.26f
    for (side in listOf(-1f, 1f)) {
        val ex = cx + side * eyeDx
        drawCircle(color = Color.White, radius = eyeR, center = Offset(ex, eyeY))

        if (state == MascotState.CELEBRATE) {
            // Star eyes
            drawStar(
                center = Offset(ex, eyeY),
                radius = eyeR * 0.72f,
                color = KidsTheme.SunnyYellow,
                rotationDeg = sparklePhase * 360f * 0.25f
            )
        } else {
            val pupilYOffset = -pupilUp * eyeR * 0.45f
            drawCircle(
                color = Color(0xFF263238),
                radius = eyeR * 0.45f,
                center = Offset(ex + eyeR * 0.08f, eyeY + eyeR * 0.10f + pupilYOffset)
            )
            drawCircle(
                color = Color.White,
                radius = eyeR * 0.14f,
                center = Offset(ex + eyeR * 0.20f, eyeY - eyeR * 0.08f + pupilYOffset)
            )
            // Eyelid: body-colored circle sliding down
            if (eyelid > 0.02f) {
                drawCircle(
                    color = Color(0xFF29A0E0),
                    radius = eyeR * 1.02f,
                    center = Offset(ex, eyeY - eyeR * 2f + eyelid * eyeR * 2f)
                )
            }
        }

        // Worried brows
        if (browWorry > 0.05f) {
            val browTilt = side * browWorry * 0.35f
            rotate(degrees = browTilt * 57f, pivot = Offset(ex, eyeY - eyeR * 1.25f)) {
                drawLine(
                    color = Color(0xFF1565C0),
                    start = Offset(ex - eyeR * 0.6f, eyeY - eyeR * 1.25f),
                    end = Offset(ex + eyeR * 0.6f, eyeY - eyeR * 1.25f),
                    strokeWidth = bodyR * 0.09f,
                    cap = StrokeCap.Round
                )
            }
        }
    }

    // Blush
    val blushAlpha = if (mouthCurve > 0.3f) 0.5f else 0.25f
    for (side in listOf(-1f, 1f)) {
        drawCircle(
            color = Color(0xFFFF8A80).copy(alpha = blushAlpha),
            radius = bodyR * 0.14f,
            center = Offset(cx + side * bodyR * 0.62f, bodyCy + bodyR * 0.10f)
        )
    }

    // Mouth
    val mouthY = bodyCy + bodyR * 0.32f
    val mouthW = bodyR * 0.55f
    if (mouthOpen > 0.15f) {
        // Open happy mouth
        val openH = bodyR * 0.35f * mouthOpen
        val mouthPath = Path().apply {
            moveTo(cx - mouthW / 2, mouthY - openH * 0.2f)
            quadraticBezierTo(cx, mouthY + openH, cx + mouthW / 2, mouthY - openH * 0.2f)
            quadraticBezierTo(cx, mouthY + openH * 0.35f, cx - mouthW / 2, mouthY - openH * 0.2f)
        }
        drawPath(mouthPath, color = Color(0xFF37474F))
        // Tongue
        drawArc(
            color = Color(0xFFFF7043),
            startAngle = 0f,
            sweepAngle = 180f,
            useCenter = true,
            topLeft = Offset(cx - mouthW * 0.22f, mouthY + openH * 0.18f),
            size = Size(mouthW * 0.44f, openH * 0.5f)
        )
    } else {
        // Curved line mouth: positive = smile, negative = frown
        val curve = mouthCurve * bodyR * 0.28f
        val mouthPath = Path().apply {
            moveTo(cx - mouthW / 2, mouthY)
            quadraticBezierTo(cx, mouthY + curve, cx + mouthW / 2, mouthY)
        }
        drawPath(
            mouthPath,
            color = Color(0xFF37474F),
            style = Stroke(width = bodyR * 0.09f, cap = StrokeCap.Round)
        )
    }

    // Thought dots (THINKING)
    if (state == MascotState.THINKING) {
        for (i in 0 until 3) {
            val phase = ((sparklePhase + i * 0.33f) % 1f)
            val alpha = (sin(phase * PI).toFloat()).coerceIn(0.15f, 1f)
            drawCircle(
                color = Color.White.copy(alpha = alpha),
                radius = bodyR * (0.06f + 0.03f * i),
                center = Offset(
                    cx + bodyR * (0.75f + 0.22f * i),
                    bodyCy - bodyR * (1.05f + 0.3f * i)
                )
            )
        }
    }

    // Orbiting sparkles (CELEBRATE)
    if (state == MascotState.CELEBRATE) {
        for (i in 0 until 5) {
            val angle = sparklePhase * 2f * PI.toFloat() + i * (2f * PI.toFloat() / 5f)
            val orbitR = bodyR * 1.45f
            drawStar(
                center = Offset(cx + cos(angle) * orbitR, bodyCy + sin(angle) * orbitR * 0.7f),
                radius = bodyR * 0.11f,
                color = KidsTheme.ConfettiColors[i % KidsTheme.ConfettiColors.size],
                rotationDeg = sparklePhase * 360f
            )
        }
    }
}

private fun DrawScope.drawStar(center: Offset, radius: Float, color: Color, rotationDeg: Float = 0f) {
    val path = Path()
    val points = 5
    for (i in 0 until points * 2) {
        val r = if (i % 2 == 0) radius else radius * 0.45f
        val angle = Math.toRadians((i * 180.0 / points) - 90.0 + rotationDeg).toFloat()
        val x = center.x + cos(angle) * r
        val y = center.y + sin(angle) * r
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    drawPath(path, color = color)
}
