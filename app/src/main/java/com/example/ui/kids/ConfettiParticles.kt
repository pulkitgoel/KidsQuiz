package com.example.ui.kids

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var rotation: Float,
    var rotationSpeed: Float,
    val width: Float,
    val height: Float,
    val color: Color,
    var wobblePhase: Float,
)

/**
 * Canvas confetti rain. Particle physics run in a single withFrameNanos loop;
 * particles live in a plain ArrayList and one frame-tick state invalidates the
 * draw — no per-particle Compose state.
 */
@Composable
fun ConfettiOverlay(
    active: Boolean,
    modifier: Modifier = Modifier,
    particleCount: Int = 110,
) {
    if (!active) return

    val particles = remember { ArrayList<Particle>(particleCount) }
    var frameTick by remember { mutableLongStateOf(0L) }

    LaunchedEffect(Unit) {
        val rng = Random(0)
        var lastNanos = 0L
        while (true) {
            withFrameNanos { nanos ->
                val dt = if (lastNanos == 0L) 0.016f else ((nanos - lastNanos) / 1_000_000_000f).coerceAtMost(0.05f)
                lastNanos = nanos

                // Seed gradually so it looks like a continuous rain
                if (particles.size < particleCount) {
                    repeat(3) {
                        particles.add(
                            Particle(
                                x = rng.nextFloat(),
                                y = -0.05f - rng.nextFloat() * 0.1f,
                                vx = (rng.nextFloat() - 0.5f) * 0.10f,
                                vy = 0.12f + rng.nextFloat() * 0.18f,
                                rotation = rng.nextFloat() * 360f,
                                rotationSpeed = (rng.nextFloat() - 0.5f) * 480f,
                                width = 0.010f + rng.nextFloat() * 0.012f,
                                height = 0.016f + rng.nextFloat() * 0.014f,
                                color = KidsTheme.ConfettiColors[rng.nextInt(KidsTheme.ConfettiColors.size)],
                                wobblePhase = rng.nextFloat() * 6.28f,
                            )
                        )
                    }
                }

                for (p in particles) {
                    p.wobblePhase += dt * 3f
                    p.x += (p.vx + sin(p.wobblePhase) * 0.04f) * dt
                    p.y += p.vy * dt
                    p.rotation += p.rotationSpeed * dt
                    if (p.y > 1.1f) {
                        // recycle to the top
                        p.y = -0.05f
                        p.x = rng.nextFloat()
                    }
                }
                frameTick = nanos
            }
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        @Suppress("UNUSED_EXPRESSION")
        frameTick // read to subscribe this draw to the physics loop
        for (p in particles) {
            rotate(degrees = p.rotation, pivot = Offset(p.x * size.width, p.y * size.height)) {
                drawRect(
                    color = p.color,
                    topLeft = Offset(
                        p.x * size.width - p.width * size.width / 2f,
                        p.y * size.height - p.height * size.width / 2f
                    ),
                    size = Size(p.width * size.width, p.height * size.width)
                )
            }
        }
    }
}

/**
 * One-shot radial confetti burst from the center — used by celebrations
 * (milestone overlay). Runs its physics once and fades out.
 */
@Composable
fun ConfettiBurst(
    trigger: Any?,
    modifier: Modifier = Modifier,
    particleCount: Int = 60,
) {
    val particles = remember { ArrayList<Particle>(particleCount) }
    var frameTick by remember { mutableLongStateOf(0L) }
    var alive by remember(trigger) { androidx.compose.runtime.mutableStateOf(true) }

    LaunchedEffect(trigger) {
        val rng = Random(1)
        particles.clear()
        repeat(particleCount) {
            val angle = rng.nextFloat() * 6.283f
            val speed = 0.35f + rng.nextFloat() * 0.55f
            particles.add(
                Particle(
                    x = 0.5f, y = 0.45f,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed - 0.25f,
                    rotation = rng.nextFloat() * 360f,
                    rotationSpeed = (rng.nextFloat() - 0.5f) * 720f,
                    width = 0.012f + rng.nextFloat() * 0.012f,
                    height = 0.018f + rng.nextFloat() * 0.014f,
                    color = KidsTheme.ConfettiColors[rng.nextInt(KidsTheme.ConfettiColors.size)],
                    wobblePhase = 0f,
                )
            )
        }
        alive = true
        var lastNanos = 0L
        var elapsed = 0f
        while (elapsed < 2.2f) {
            withFrameNanos { nanos ->
                val dt = if (lastNanos == 0L) 0.016f else ((nanos - lastNanos) / 1_000_000_000f).coerceAtMost(0.05f)
                lastNanos = nanos
                elapsed += dt
                for (p in particles) {
                    p.vy += 0.9f * dt // gravity
                    p.x += p.vx * dt
                    p.y += p.vy * dt
                    p.rotation += p.rotationSpeed * dt
                }
                frameTick = nanos
            }
        }
        alive = false
    }

    if (alive) {
        Canvas(modifier = modifier.fillMaxSize()) {
            @Suppress("UNUSED_EXPRESSION")
            frameTick
            for (p in particles) {
                if (p.y > 1.2f) continue
                rotate(degrees = p.rotation, pivot = Offset(p.x * size.width, p.y * size.height)) {
                    drawRect(
                        color = p.color,
                        topLeft = Offset(
                            p.x * size.width - p.width * size.width / 2f,
                            p.y * size.height - p.height * size.width / 2f
                        ),
                        size = Size(p.width * size.width, p.height * size.width)
                    )
                }
            }
        }
    }
}
