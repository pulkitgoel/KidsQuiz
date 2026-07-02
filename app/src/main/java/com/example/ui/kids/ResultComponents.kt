package com.example.ui.kids

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.distinctUntilChanged
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Animated score counter that fires [onTick] on every increment so the caller
 * can play a tick sound / haptic per step.
 */
@Composable
fun ScoreRollUp(score: Int, total: Int, onTick: () -> Unit, modifier: Modifier = Modifier) {
    val animatedScore by animateIntAsState(
        targetValue = score,
        animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
        label = "score_roll"
    )
    LaunchedEffect(Unit) {
        snapshotFlow { animatedScore }
            .distinctUntilChanged()
            .collect { if (it > 0) onTick() }
    }
    Text(
        text = "$animatedScore / $total",
        fontSize = 24.sp,
        fontWeight = FontWeight.Black,
        color = Color(0xFF6750A4),
        modifier = modifier
    )
}

/** Status of one day in the weekly strip. */
data class DayStatus(val dayLetter: String, val done: Boolean, val isToday: Boolean)

/** Derives the last-7-days statuses (oldest first) from the set of quiz dates. */
fun last7Days(quizDates: Set<String>): List<DayStatus> {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val dayLetterFmt = SimpleDateFormat("E", Locale.getDefault())
    val cal = Calendar.getInstance()
    cal.add(Calendar.DAY_OF_YEAR, -6)
    return (0 until 7).map {
        val date = cal.time
        val key = sdf.format(date)
        val status = DayStatus(
            dayLetter = dayLetterFmt.format(date).take(1),
            done = key in quizDates,
            isToday = it == 6
        )
        cal.add(Calendar.DAY_OF_YEAR, 1)
        status
    }
}

/** Seven circles, one per day; flame-filled when a quiz was done that day. */
@Composable
fun WeeklyCalendarStrip(days: List<DayStatus>, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        days.forEach { day ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = day.dayLetter,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (day.isToday) KidsTheme.FlameOrange else KidsTheme.TextMuted
                )
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(
                            color = if (day.done) KidsTheme.FlameOrange.copy(alpha = 0.15f) else Color(0xFFF0F0F0),
                            shape = CircleShape
                        )
                        .border(
                            width = 2.dp,
                            color = when {
                                day.done -> KidsTheme.FlameOrange
                                day.isToday -> KidsTheme.FlameOrange.copy(alpha = 0.45f)
                                else -> Color(0xFFDDDDDD)
                            },
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (day.done) "🔥" else "",
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

/**
 * Result-screen card celebrating today's streak day, with the weekly strip.
 */
@Composable
fun StreakDayCelebrationCard(
    streakCount: Int,
    days: List<DayStatus>,
    isNewStreakDay: Boolean,
    modifier: Modifier = Modifier,
) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = KidsTheme.SurfaceCream),
        border = androidx.compose.foundation.BorderStroke(2.dp, KidsTheme.FlameOrange.copy(alpha = 0.5f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AnimatedFlame(size = 42.dp, intensity = (streakCount / 10f).coerceIn(0.3f, 1f))
                Column {
                    Text(
                        text = if (isNewStreakDay) "Day $streakCount of your streak!" else "Streak: $streakCount days strong!",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = KidsTheme.FlameOrangeDeep
                    )
                    Text(
                        text = if (isNewStreakDay) "You kept the flame alive! 🎉" else "Already done today — amazing!",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = KidsTheme.TextMuted
                    )
                }
            }
            WeeklyCalendarStrip(days = days)
        }
    }
}
