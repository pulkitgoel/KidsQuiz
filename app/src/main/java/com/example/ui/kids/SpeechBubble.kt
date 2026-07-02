package com.example.ui.kids

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar

/** Rounded speech bubble with a little tail, pops in next to the mascot. */
@Composable
fun MascotSpeechBubble(
    text: String,
    visible: Boolean,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = visible && text.isNotBlank(),
        enter = scaleIn(animationSpec = kidsBouncySpring()),
        exit = scaleOut() + fadeOut(),
        modifier = modifier
    ) {
        Column(horizontalAlignment = Alignment.Start) {
            Text(
                text = text,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = KidsTheme.TextDark,
                textAlign = TextAlign.Center,
                lineHeight = 19.sp,
                modifier = Modifier
                    .widthIn(max = 220.dp)
                    .background(Color.White, RoundedCornerShape(18.dp))
                    .border(2.dp, Color(0xFFE0E0E0), RoundedCornerShape(18.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            )
            // Tail pointing down-left toward the mascot
            Canvas(modifier = Modifier.size(16.dp).padding(start = 4.dp)) {
                val path = Path().apply {
                    moveTo(4f, 0f)
                    lineTo(size.width, 0f)
                    lineTo(4f, size.height * 0.9f)
                    close()
                }
                drawPath(path, color = Color.White)
            }
        }
    }
}

/** Contextual message pools for the mascot. */
object MascotMessages {
    private val correct = listOf(
        "Woohoo! You got it! 🎉",
        "That's right! You're so smart!",
        "Amazing! Keep going! ⭐",
        "Yes yes yes! 🙌",
        "You nailed it!",
    )
    private val wrong = listOf(
        "Almost! You'll get the next one!",
        "Oopsie! Let's keep trying! 💪",
        "Don't worry, mistakes help us learn!",
        "So close! I believe in you!",
    )
    private val combo = listOf(
        "You're on a roll! 🔥",
        "Unstoppable! Keep it up!",
        "WOW! What a streak!",
    )
    private val quizStart = listOf(
        "Let's do this! 💪",
        "You've got this!",
        "Ready? Let's go! 🚀",
        "Show me what you know!",
    )

    fun forCorrect(comboCount: Int): String =
        if (comboCount >= 3) combo.random() else correct.random()

    fun forWrong(): String = wrong.random()

    fun forQuizStart(): String = quizStart.random()

    fun greetingForTimeOfDay(name: String): String {
        // HOUR_OF_DAY is 0-23 (midnight = 0), so the late-night/past-midnight
        // hours must be handled explicitly — "hour < 12" alone wrongly treats
        // 12:34 AM as morning.
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..11 -> "Good morning, $name! ☀️ Ready to learn?"
            in 12..16 -> "Hi $name! Let's play and learn! 🎮"
            in 17..21 -> "Good evening, $name! One more quiz? 🌙"
            else -> "Up late, $name? 🌛 Let's do a quick quiz!" // 22:00–4:59
        }
    }
}
