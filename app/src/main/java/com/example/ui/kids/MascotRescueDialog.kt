package com.example.ui.kids

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * Shown when the kid runs out of hearts. The mascot offers one extra heart —
 * accept refills a single heart, decline ends the quiz gently.
 * [mascot] slot lets the caller pass the animated mascot composable.
 */
@Composable
fun MascotRescueDialog(
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    mascot: @Composable () -> Unit,
) {
    Dialog(
        onDismissRequest = { /* must choose — no outside dismiss */ },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Card(
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = KidsTheme.SurfaceCream),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                mascot()
                Text(
                    text = "Oh no, you're out of hearts!",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = KidsTheme.ErrorRed,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Want one more chance? I believe in you! 💪",
                    fontSize = 15.sp,
                    color = KidsTheme.TextDark,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
                Button(
                    onClick = onAccept,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = KidsTheme.SuccessGreen)
                ) {
                    Text(
                        text = "One More Chance! ❤️",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
                OutlinedButton(
                    onClick = onDecline,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(2.dp, KidsTheme.TextMuted)
                ) {
                    Text(
                        text = "Finish for now",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = KidsTheme.TextMuted
                    )
                }
            }
        }
    }
}
