package com.example.ui.kids

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Full-screen streak milestone celebration ("7 DAY STREAK!"). Rendered above
 * everything at the app root; blocks input behind it until dismissed.
 */
@Composable
fun MilestoneCelebrationOverlay(
    milestone: Int,
    onDismiss: () -> Unit,
) {
    var entered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { entered = true }
    val scale by animateFloatAsState(
        targetValue = if (entered) 1f else 0.4f,
        animationSpec = kidsBouncySpring(),
        label = "milestone_scale"
    )

    val subtitle = when (milestone) {
        3 -> "Three days in a row — you're building a habit!"
        7 -> "A whole week of learning! Incredible!"
        14 -> "Two weeks strong — you're unstoppable!"
        30 -> "A WHOLE MONTH! You're a learning legend!"
        100 -> "100 DAYS!! The hall of fame awaits! 👑"
        else -> "What an amazing streak!"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.65f))
            // swallow clicks so nothing behind reacts
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {},
        contentAlignment = Alignment.Center
    ) {
        ConfettiBurst(trigger = milestone)

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp),
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .scale(scale)
        ) {
            AnimatedFlame(size = 150.dp, intensity = 1f)
            Text(
                text = "🔥 $milestone DAY STREAK! 🔥",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = KidsTheme.FlameYellow,
                textAlign = TextAlign.Center,
                lineHeight = 40.sp
            )
            Text(
                text = subtitle,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = KidsTheme.SuccessGreen),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = "KEEP IT UP! 💪",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }
        }
    }
}
