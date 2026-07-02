@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.launch
import android.media.AudioManager
import android.media.ToneGenerator
import android.net.Uri
import coil.compose.AsyncImage
import com.example.viewmodel.QuizViewModel
import com.example.viewmodel.Screen
import com.example.data.Question
import com.example.data.QuizAttempt
import com.example.ui.kids.KidsTheme
import com.example.ui.kids.Mascot
import com.example.ui.kids.MascotState
import com.example.ui.kids.shake
import com.example.util.Sfx
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.withSaveLayer
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size

// Helper for bouncy clicks
fun Modifier.bouncyClick(onClick: () -> Unit): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "bouncy"
    )
    this
        .scale(scale)
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )
}

// Sleek Interface Theme Colors
val SleekPurple = Color(0xFF6750A4)
val SleekTextDark = Color(0xFF1D1B20)
val SleekBgLight = Color(0xFFFEF7FF)
val SleekSurfaceVariant = Color(0xFFF3EDF7)
val SleekBorderLight = Color(0xFFE7E0EC)
val SleekBorderDark = Color(0xFFCAC4D0)

val KidSkyBlue = Color(0xFFE0F4FF)
val KidSunnyYellow = Color(0xFFFFF6C3)
val KidTeal = Color(0xFFC1F4C5)
val KidPurple = Color(0xFFE2D4F0)
val KidOrange = Color(0xFFFFD4B2)
val KidCoral = Color(0xFFFFB7B2)

val GradientMath = Brush.linearGradient(listOf(Color(0xFFD0E4FF), Color(0xFFA1C2FF)))
val GradientEnglish = Brush.linearGradient(listOf(Color(0xFFFFDBCB), Color(0xFFFFB494)))
val GradientGK = Brush.linearGradient(listOf(Color(0xFFF2D8FF), Color(0xFFD8A1FF)))
val GradientScience = Brush.linearGradient(listOf(Color(0xFFC2F0C2), Color(0xFF94D194)))
val GradientFlame = Brush.linearGradient(listOf(Color(0xFF6750A4), Color(0xFF9F8CD4)))
val GradientStars = Brush.linearGradient(listOf(Color(0xFFFFDF00), Color(0xFFFFA500)))

@Composable
fun QuizAppUI(viewModel: QuizViewModel) {
    val rootContext = androidx.compose.ui.platform.LocalContext.current
    val soundManager = remember {
        com.example.util.SoundManager.get(rootContext).apply {
            muted = viewModel.preferences.soundMuted
        }
    }
    val hapticsManager = remember { com.example.util.HapticsManager.get(rootContext) }
    androidx.compose.runtime.CompositionLocalProvider(
        com.example.util.LocalSoundManager provides soundManager,
        com.example.util.LocalHapticsManager provides hapticsManager,
    ) {
        QuizAppContent(viewModel)
    }
}

@Composable
private fun QuizAppContent(viewModel: QuizViewModel) {
    val currentScreen = viewModel.currentScreen
    val context = androidx.compose.ui.platform.LocalContext.current
    var showAppExitDialog by remember { mutableStateOf(false) }

    // Back press: on Home → ask exit confirmation; elsewhere → navigate home
    androidx.activity.compose.BackHandler {
        when (currentScreen) {
            is Screen.Home -> showAppExitDialog = true
            is Screen.ParentDashboard, is Screen.ParentEnterPin, is Screen.QuizResult,
            is Screen.QuizSession, is Screen.NoQuestions -> viewModel.navigateTo(Screen.Home)
            else -> {}
        }
    }

    // Exit App Confirmation Dialog
    if (showAppExitDialog) {
        Dialog(onDismissRequest = { showAppExitDialog = false }) {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("👋", fontSize = 52.sp)
                    Text(
                        text = "Leaving so soon?",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = SleekPurple,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Your stars and progress are saved! Come back soon for more fun learning! 🌟",
                        fontSize = 13.sp,
                        color = Color(0xFF6C757D),
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showAppExitDialog = false },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(2.dp, SleekPurple)
                        ) {
                            Text("Keep Playing! 🎮", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SleekPurple)
                        }
                        Button(
                            onClick = {
                                showAppExitDialog = false
                                (context as? androidx.activity.ComponentActivity)?.finish()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SleekPurple)
                        ) {
                            Text("Bye Bye! 👋", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }


    KidsAnimatedBackground()

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {

        // Screen selection with slide/fade animation for a bubbly, responsive UI
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = {
                slideInHorizontally(initialOffsetX = { x -> if (targetState is Screen.Home) -x else x }) + fadeIn() togetherWith
                slideOutHorizontally(targetOffsetX = { x -> if (targetState is Screen.Home) x else -x }) + fadeOut()
            },
            label = "screen_navigation"
        ) { screen ->
            when (screen) {
                is Screen.Home -> {
                    HomeScreen(viewModel = viewModel)
                }
                is Screen.QuizSession -> {
                    QuizSessionScreen(viewModel = viewModel, subject = screen.subject)
                }
                is Screen.QuizResult -> {
                    QuizResultScreen(
                        viewModel = viewModel,
                        subject = screen.subject,
                        score = screen.score,
                        total = screen.total,
                        starsEarned = screen.starsEarned
                    )
                }
                is Screen.ParentEnterPin -> {
                    ParentPinScreen(viewModel = viewModel)
                }
                is Screen.ParentDashboard -> {
                    ParentDashboardScreen(viewModel = viewModel)
                }
                is Screen.NoQuestions -> {
                    NoQuestionsScreen(viewModel = viewModel, subject = screen.subject)
                }
            }
        }

        // Streak milestone celebration — renders above every screen
        viewModel.pendingMilestone?.let { milestone ->
            val sound = com.example.util.LocalSoundManager.current
            val haptics = com.example.util.LocalHapticsManager.current
            LaunchedEffect(milestone) {
                sound.play(Sfx.MILESTONE)
                haptics.celebrate()
            }
            com.example.ui.kids.MilestoneCelebrationOverlay(
                milestone = milestone,
                onDismiss = { viewModel.pendingMilestone = null }
            )
        }
    }
}

@Composable
fun NoQuestionsScreen(viewModel: QuizViewModel, subject: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = SleekSurfaceVariant),
            border = BorderStroke(1.dp, SleekBorderLight),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(32.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Mascot(state = MascotState.THINKING, modifier = Modifier.size(110.dp))

                Text(
                    text = "No questions available for $subject!",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = SleekPurple,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Please ask your parents to set up new questions or reactivate archived ones in the Parent Dashboard.",
                    fontSize = 15.sp,
                    color = Color(0xFF49454F),
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )

                Button(
                    onClick = { viewModel.navigateTo(Screen.Home) },
                    colors = ButtonDefaults.buttonColors(containerColor = SleekPurple),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text("Back to Home 🏠", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun ScratchCard(
    onScratchComplete: () -> Unit,
    rewardText: String,
    modifier: Modifier = Modifier
) {
    var scratchedPercentage by remember { mutableStateOf(0f) }
    
    Box(
        modifier = modifier
            .size(width = 280.dp, height = 160.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFFFFF0C2), Color(0xFFFFD194))
                )
            )
            .border(4.dp, Color(0xFFFFD700), RoundedCornerShape(24.dp)),
        contentAlignment = Alignment.Center
    ) {
        // Underneath Content (The Reward)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "🎁", fontSize = 48.sp)
            Text(
                text = rewardText,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF8B6508)
            )
            Text(
                text = "BONUS STAR REWARD!",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFC79100),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        
        // Scratchable Overlay
        val drawPoints = remember { mutableStateListOf<Offset>() }
        
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            event.changes.forEach { change ->
                                if (change.pressed) {
                                    change.consume()
                                    drawPoints.add(change.position)
                                    if (drawPoints.size > 25 && scratchedPercentage < 0.8f) {
                                        scratchedPercentage = 0.8f
                                        onScratchComplete()
                                    }
                                }
                            }
                        }
                    }
                }
        ) {
            if (scratchedPercentage < 0.8f) {
                // We want to clear the drawn points!
                val paint = Paint().apply {
                    color = Color.Transparent
                    blendMode = BlendMode.Clear
                    style = PaintingStyle.Stroke
                    strokeWidth = 35.dp.toPx()
                    strokeCap = StrokeCap.Round
                    strokeJoin = StrokeJoin.Round
                }
                
                drawIntoCanvas { canvas ->
                    canvas.saveLayer(Rect(0f, 0f, size.width, size.height), Paint())
                    // Draw grey overlay inside save layer
                    canvas.drawRoundRect(
                        0f, 0f, size.width, size.height,
                        24.dp.toPx(), 24.dp.toPx(),
                        Paint().apply { color = Color(0xFFB0BEC5) }
                    )
                    
                    // Draw "Scratch Here" text inside save layer
                    val textPaint = android.graphics.Paint().apply {
                        color = android.graphics.Color.WHITE
                        textSize = 16.sp.toPx()
                        isFakeBoldText = true
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                    canvas.nativeCanvas.drawText(
                        "Scratch Here to Reveal! ✨",
                        size.width / 2,
                        size.height / 2 + 6.dp.toPx(),
                        textPaint
                    )
                    
                    // Clear the path from the save layer
                    if (drawPoints.isNotEmpty()) {
                        val p = Path()
                        p.moveTo(drawPoints.first().x, drawPoints.first().y)
                        for (i in 1 until drawPoints.size) {
                            p.lineTo(drawPoints[i].x, drawPoints[i].y)
                        }
                        canvas.drawPath(p, paint)
                    }
                    canvas.restore()
                }
            }
        }
    }
}

@Composable
fun HomeScreen(viewModel: QuizViewModel) {
    val totalStars = viewModel.totalStars
    val streakCount = viewModel.streakCount
    val todayCompleted = viewModel.todayQuizzesCompleted
    val dailyGoal = viewModel.dailyQuizGoal
    var showTrophyDialog by remember { mutableStateOf(false) }
    var showStreakDialog by remember { mutableStateOf(false) }

    // Streak status for today (recomputed when returning to Home)
    val todayStr = remember(todayCompleted) {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }
    val todayDone = viewModel.preferences.lastQuizDate == todayStr
    val yesterdayStr = remember(todayCompleted) {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000))
    }
    val streakAtRisk = streakCount > 0 && !todayDone && viewModel.preferences.lastQuizDate == yesterdayStr
    val quizDates = remember(todayCompleted) { viewModel.preferences.getQuizDates() }

    // Streak detail dialog: big flame + weekly calendar + next milestone
    if (showStreakDialog) {
        Dialog(onDismissRequest = { showStreakDialog = false }) {
            Card(
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    com.example.ui.kids.AnimatedFlame(
                        size = 90.dp,
                        intensity = (streakCount / 10f).coerceIn(0.3f, 1f),
                        lit = streakCount > 0
                    )
                    Text(
                        text = if (streakCount > 0) "$streakCount Day Streak!" else "Start your streak!",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = com.example.ui.kids.KidsTheme.FlameOrangeDeep
                    )
                    Text(
                        text = when {
                            streakCount == 0 -> "Play one quiz today to light your flame! 🔥"
                            todayDone -> "You're safe for today — see you tomorrow!"
                            else -> "Play a quiz today to keep it going!"
                        },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF6C757D),
                        textAlign = TextAlign.Center
                    )
                    com.example.ui.kids.WeeklyCalendarStrip(days = com.example.ui.kids.last7Days(quizDates))
                    val nextMilestone = QuizViewModel.STREAK_MILESTONES.firstOrNull { it > streakCount }
                    if (nextMilestone != null) {
                        Text(
                            text = "${nextMilestone - streakCount} more days to the $nextMilestone-day badge! 🏅",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = SleekPurple,
                            textAlign = TextAlign.Center
                        )
                    }
                    Button(
                        onClick = { showStreakDialog = false },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = com.example.ui.kids.KidsTheme.FlameOrange),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Got it! 🔥", fontWeight = FontWeight.Black, color = Color.White)
                    }
                }
            }
        }
    }

    val levelInfo = when {
        totalStars <= 5 -> Pair("Star Cadet 🚀", "Catching sweet learning stars!")
        totalStars <= 15 -> Pair("Math & Science Cadet 🔬", "Exploring cool subject fields!")
        totalStars <= 30 -> Pair("Brilliant Scholar 🧠", "Super quiz-solving powers activated!")
        else -> Pair("KidQuiz Champion 🏆", "The absolute master of learning!")
    }

    if (showTrophyDialog) {
        AlertDialog(
            onDismissRequest = { showTrophyDialog = false },
            title = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "🏆 Trophy Room 🏆", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = SleekPurple)
                }
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(text = "🏅 Level Ranking:", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Text(
                        text = levelInfo.first,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = SleekPurple,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = levelInfo.second,
                        fontSize = 13.sp,
                        color = Color.DarkGray,
                        textAlign = TextAlign.Center
                    )
                    HorizontalDivider(color = SleekBorderDark, thickness = 1.dp)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Total Stars Collected:", fontSize = 14.sp)
                        Text(text = "⭐ $totalStars", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFFE28F00))
                    }
                }
            },
            confirmButton = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = { showTrophyDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = SleekPurple),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Awesome! ✨", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            },
            shape = RoundedCornerShape(28.dp),
            containerColor = SleekBgLight
        )
    }

    var showLevelUpDialog by remember { mutableStateOf(false) }
    var isChestOpened by remember { mutableStateOf(false) }
    // Capture level number at dialog-open time so text doesn't change mid-dialog
    var dialogTargetLevel by remember { mutableStateOf(1) }

    if (showLevelUpDialog) {
        AlertDialog(
            onDismissRequest = {
                // Always allow dismissal
                showLevelUpDialog = false
                isChestOpened = false
            },
            title = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isChestOpened) "🎉 REWARD CLAIMED! 🎉" else "🎉 LEVEL UP! 🎉",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFE28F00),
                        textAlign = TextAlign.Center
                    )
                }
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = if (isChestOpened)
                            "You are now Level $dialogTargetLevel! 🚀"
                        else
                            "You reached Level $dialogTargetLevel! Open your reward chest!",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = SleekPurple,
                        textAlign = TextAlign.Center
                    )

                    val rotateTransition = rememberInfiniteTransition(label = "rotate_gem")
                    val gemRotation by rotateTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 360f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(4000, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        ),
                        label = "rotate"
                    )

                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .background(Color(0xFFFFF9C4), CircleShape)
                            .border(3.dp, Color(0xFFFFD54F), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isChestOpened) "👑" else "🎁",
                            fontSize = 64.sp,
                            modifier = Modifier.rotate(if (isChestOpened) gemRotation else 0f)
                        )
                    }

                    Text(
                        text = if (isChestOpened) {
                            "You claimed 5 BONUS STARS! ⭐ Keep going to reach Level ${dialogTargetLevel + 1}!"
                        } else {
                            "Tap the button below to reveal your bonus star reward!"
                        },
                        fontSize = 14.sp,
                        color = Color(0xFF49454F),
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                }
            },
            confirmButton = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    if (!isChestOpened) {
                        Button(
                            onClick = {
                                viewModel.claimLevelUpReward(5)
                                isChestOpened = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Text("Open Chest! 🎁", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    } else {
                        Button(
                            onClick = {
                                showLevelUpDialog = false
                                isChestOpened = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SleekPurple),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Text("Awesome! Keep Learning! 🌟", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            },
            shape = RoundedCornerShape(32.dp),
            containerColor = Color(0xFFFFFDE7)
        )
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left Capsule (Stars)
                Box(
                    modifier = Modifier
                        .background(Color(0xFFEADDFF), shape = RoundedCornerShape(100.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(text = "⭐", fontSize = 16.sp)
                        Text(
                            text = "$totalStars",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = Color(0xFF21005D)
                        )
                    }
                }

                // Center (Logo)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "KidQuiz",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = SleekPurple,
                        letterSpacing = (-0.5).sp
                    )
                    Box(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .width(32.dp)
                            .height(4.dp)
                            .background(SleekPurple.copy(alpha = 0.3f), RoundedCornerShape(100.dp))
                    )
                }

                // Right: mute toggle + streak capsule
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val sound = com.example.util.LocalSoundManager.current
                    var muted by remember { mutableStateOf(sound.muted) }
                    Text(
                        text = if (muted) "🔇" else "🔊",
                        fontSize = 18.sp,
                        modifier = Modifier.bouncyClick {
                            muted = !muted
                            sound.muted = muted
                            viewModel.preferences.soundMuted = muted
                            if (!muted) sound.play(Sfx.TAP)
                        }
                    )
                    com.example.ui.kids.StreakCapsule(
                        streakCount = streakCount,
                        todayDone = todayDone,
                        onClick = { showStreakDialog = true }
                    )
                }
            }
        },
        bottomBar = {
            Column {
                HorizontalDivider(color = SleekBorderDark, thickness = 1.dp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SleekSurfaceVariant)
                        .navigationBarsPadding()
                        .height(80.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    // Home option
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {}
                            )
                    ) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFEADDFF), shape = RoundedCornerShape(100.dp))
                                .padding(horizontal = 20.dp, vertical = 6.dp)
                        ) {
                            Text(text = "🏠", fontSize = 20.sp)
                        }
                        Text(text = "HOME", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF21005D))
                    }

                    // Trophy option
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { showTrophyDialog = true }
                            )
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                        ) {
                            Text(text = "🏆", fontSize = 20.sp)
                        }
                        Text(text = "TROPHY", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF49454F))
                    }

                    // Parents option (Replacing parentZone button)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .testTag("parent_gate_button")
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { viewModel.navigateTo(Screen.ParentEnterPin) }
                            )
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(text = "⚙️", fontSize = 20.sp)
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFFBA1A1A), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(text = "PIN", fontSize = 8.sp, fontWeight = FontWeight.Black, color = Color.White)
                                }
                            }
                        }
                        Text(text = "PARENTS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF49454F))
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Mascot Greeting Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Mascot(
                    state = MascotState.IDLE,
                    modifier = Modifier.size(90.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                // Not remembered: must reflect the current device time/timezone
                // every time Home is shown, not just whenever the app first launched.
                val greeting = com.example.ui.kids.MascotMessages.greetingForTimeOfDay(viewModel.childName)
                com.example.ui.kids.MascotSpeechBubble(
                    text = greeting,
                    visible = true
                )
            }

            // Streak at risk? Nudge to play today (more urgent in the evening)
            if (streakAtRisk) {
                val isEvening = remember { java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY) >= 17 }
                com.example.ui.kids.StreakAtRiskBanner(
                    streakCount = streakCount,
                    urgent = isEvening,
                    onStartQuiz = {
                        val allSubjects = listOf("Math", "English", "Science", "General Knowledge") + viewModel.customSubjects
                        viewModel.startQuiz(allSubjects.random())
                    }
                )
            }

            // Level Chest & Progress Bar
            val currentLevel = (totalStars / 10) + 1
            val nextLevelStars = currentLevel * 10
            val prevLevelStars = (currentLevel - 1) * 10
            val levelProgress = (totalStars - prevLevelStars) / 10f
            
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDE7)),
                shape = RoundedCornerShape(32.dp),
                border = BorderStroke(2.dp, Color(0xFFFFF59D)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Cute level circle
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(Color(0xFFFFD54F), CircleShape)
                            .border(3.dp, Color(0xFFF57F17), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "Lv $currentLevel", fontWeight = FontWeight.Black, fontSize = 14.sp, color = Color(0xFF5D4037))
                    }
                    
                    // Level progress bar
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Level Progress",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFF5D4037)
                            )
                            Text(
                                text = "$totalStars / $nextLevelStars ⭐",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.sp,
                                color = Color(0xFFF57F17)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { levelProgress.coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(16.dp)
                                .clip(RoundedCornerShape(100.dp)),
                            color = Color(0xFFFFB300),
                            trackColor = Color(0xFFFFF9C4)
                        )
                    }
                    
                    // pendingLevelUp: true only if earned stars (not bonus) justify a new level that hasn't been claimed
                    val pendingLevelUp = currentLevel > viewModel.lastClaimedLevel
                    val chestTransition = rememberInfiniteTransition(label = "chest_throb")
                    val chestScale by chestTransition.animateFloat(
                        initialValue = 1f,
                        targetValue = if (pendingLevelUp) 1.3f else 1.1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(if (pendingLevelUp) 350 else 600, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "scale"
                    )
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .bouncyClick {
                                if (pendingLevelUp) {
                                    dialogTargetLevel = currentLevel // lock current level at open time
                                    showLevelUpDialog = true
                                } else {
                                    showTrophyDialog = true
                                }
                            }
                            .padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .scale(chestScale),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = if (pendingLevelUp) "🎁" else "📦", fontSize = 32.sp)
                        }
                        if (pendingLevelUp) {
                            val pulseAlpha by chestTransition.animateFloat(
                                initialValue = 0.5f,
                                targetValue = 1f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(350, easing = LinearEasing),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "pulse"
                            )
                            Text(
                                text = "TAP ME! ✨",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFE28F00),
                                modifier = Modifier.graphicsLayer { alpha = pulseAlpha }
                            )
                        } else {
                            Text(
                                text = "LOCKED",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }

            // Achievements Badges section
            // Subject badges require real accuracy, not just showing up: at least 2
            // completed attempts AND a 60%+ average score in that subject.
            val attempts by viewModel.allAttempts.collectAsState(initial = emptyList())
            fun subjectMastered(subjectNames: Set<String>): Boolean {
                val subjectAttempts = attempts.filter { it.subject in subjectNames && it.totalQuestions > 0 }
                if (subjectAttempts.size < 2) return false
                val totalCorrect = subjectAttempts.sumOf { it.score }
                val totalQs = subjectAttempts.sumOf { it.totalQuestions }
                return totalQs > 0 && totalCorrect.toFloat() / totalQs >= 0.6f
            }
            val mathMastered = subjectMastered(setOf("Math"))
            val englishMastered = subjectMastered(setOf("English"))
            val scienceMastered = subjectMastered(setOf("Science"))

            val kidBadges = listOf(
                Triple("Math Wiz 🔢", "Score 60%+ over 2+ Math quizzes", mathMastered),
                Triple("Word Star 📖", "Score 60%+ over 2+ English quizzes", englishMastered),
                Triple("Explorer 🧪", "Score 60%+ over 2+ Science quizzes", scienceMastered),
                Triple("Genius 🧠", "Collect 20+ Stars", totalStars >= 20),
                Triple("Streak Master 🔥", "Daily streak of 3+", streakCount >= 3)
            )
            
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "My Learning Badges 🏅",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = SleekPurple
                )
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    items(kidBadges) { badge ->
                        val unlocked = badge.third
                        val cardBg = if (unlocked) Color(0xFFE8F5E9) else Color(0xFFECEFF1)
                        val borderCol = if (unlocked) Color(0xFF81C784) else Color(0xFFCFD8DC)
                        val badgeScale = if (unlocked) 1f else 0.9f
                        
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = cardBg),
                            border = BorderStroke(2.dp, borderCol),
                            modifier = Modifier
                                .size(width = 120.dp, height = 110.dp)
                                .scale(badgeScale)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = if (unlocked) "⭐" else "🔒",
                                    fontSize = 16.sp,
                                    modifier = Modifier.padding(bottom = 2.dp)
                                )
                                Text(
                                    text = badge.first,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (unlocked) Color(0xFF2E7D32) else Color(0xFF546E7A),
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = badge.second,
                                    fontSize = 8.sp,
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 10.sp,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Welcome Greeting with customizable Kid Name + Circular Beautiful Photo Avatar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Pick a Subject! 🎯",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        color = SleekTextDark,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "What are we learning today?",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFF49454F)
                    )
                }

                // Child profile image thumbnail or default avatar
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(SleekSurfaceVariant, CircleShape)
                        .border(3.dp, SleekPurple, CircleShape)
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (viewModel.childPhotoUri.isNotEmpty()) {
                        AsyncImage(
                            model = viewModel.childPhotoUri,
                            contentDescription = "Child Photo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = "👦",
                            fontSize = 32.sp
                        )
                    }
                }
            }

            // Subject Selections in a beautiful tactile Grid overlay
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val baseSubjects = listOf("Math", "English", "Science", "General Knowledge")
                val displaySubjects = (baseSubjects + viewModel.customSubjects).distinct()

                val colorsList = listOf(
                    Triple(Color(0xFFD0E4FF), Color(0xFFA1C2FF), Color(0xFF003366)),
                    Triple(Color(0xFFFFDBCB), Color(0xFFFFB494), Color(0xFF3E2D00)),
                    Triple(Color(0xFFC2F0C2), Color(0xFF94D194), Color(0xFF00390A)),
                    Triple(Color(0xFFF2D8FF), Color(0xFFD8A1FF), Color(0xFF31111D)),
                    Triple(Color(0xFFFFF0C2), Color(0xFFFFD194), Color(0xFF392500)),
                    Triple(Color(0xFFC2F0E9), Color(0xFF94D1C3), Color(0xFF003930))
                )
                val emojiList = listOf("➕", "📖", "🧪", "🌍", "💡", "🎯", "🌟", "📚")

                val chunked = displaySubjects.chunked(2)
                for (chunk in chunked) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        for (it in chunk) {
                            val idx = displaySubjects.indexOf(it)
                            val colorScheme = colorsList[idx % colorsList.size]
                            val emoji = emojiList[idx % emojiList.size]
                            
                            Box(modifier = Modifier.weight(1f)) {
                                SleekSubjectCard(
                                    emoji = emoji,
                                    title = if (it == "General Knowledge") "General" else it,
                                    containerColor = colorScheme.first,
                                    borderColor = colorScheme.second,
                                    textColor = colorScheme.third,
                                    testTag = "subject_${it.lowercase()}",
                                    onClick = { viewModel.startQuiz(it) }
                                )
                            }
                        }
                        if (chunk.size == 1) {
                             Box(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            // Primary Action Button matches Sleek Interface bottom button precisely
            Button(
                onClick = {
                    val baseSubjects = listOf("Math", "English", "Science", "General Knowledge")
                    val displaySubjects = (baseSubjects + viewModel.customSubjects).distinct()
                    val randomSubject = displaySubjects.random()
                    viewModel.startQuiz(randomSubject)
                },
                colors = ButtonDefaults.buttonColors(containerColor = SleekPurple),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 24.dp)
                    .height(58.dp)
                    .testTag("start_daily_quiz_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "🚀 Start Daily Quiz",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun SleekSubjectCard(
    emoji: String,
    title: String,
    containerColor: Color,
    borderColor: Color,
    textColor: Color,
    testTag: String,
    onClick: () -> Unit
) {
    val sound = com.example.util.LocalSoundManager.current
    val haptics = com.example.util.LocalHapticsManager.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .background(borderColor, RoundedCornerShape(28.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(bottom = 5.dp) // Creates the precise tactile border-b-4 3D button effect from CSS!
                .background(containerColor, RoundedCornerShape(28.dp))
                .clip(RoundedCornerShape(28.dp))
                .bouncyClick {
                    sound.play(Sfx.TAP)
                    haptics.tick()
                    onClick()
                }
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = emoji, fontSize = 36.sp)
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = textColor,
                    modifier = Modifier.testTag(testTag)
                )
            }
        }
    }
}

@Composable
fun QuizSessionScreen(viewModel: QuizViewModel, subject: String) {
    val activeQuestions = viewModel.activeQuestions
    val currentQuestionIdx = viewModel.currentQuestionIdx
    val currentQuestion = activeQuestions.getOrNull(currentQuestionIdx)
    val selectedOptionIdx = viewModel.selectedOptionIdx
    val isAnswerChecked = viewModel.isAnswerChecked
    val activeScore = viewModel.activeScore
    
    val sound = com.example.util.LocalSoundManager.current
    val haptics = com.example.util.LocalHapticsManager.current

    // Inline feedback driver: evaluateAnswer() flips isAnswerChecked; we play
    // sound + haptics, hold for the feedback beat, then advance.
    LaunchedEffect(isAnswerChecked) {
        if (isAnswerChecked) {
            val combo = viewModel.comboCount
            if (viewModel.lastAnswerCorrect == true) {
                when {
                    combo >= 5 -> sound.play(com.example.util.Sfx.ON_FIRE)
                    combo == 3 || combo == 4 -> sound.play(com.example.util.Sfx.COMBO)
                    else -> sound.play(com.example.util.Sfx.CORRECT, rate = sound.correctPitchFor(combo))
                }
                if (combo >= 2) haptics.combo(combo) else haptics.success()
            } else {
                sound.play(com.example.util.Sfx.WRONG)
                haptics.error()
                kotlinx.coroutines.delay(150L)
                sound.play(com.example.util.Sfx.HEART_BREAK)
            }
            kotlinx.coroutines.delay(com.example.ui.kids.KidsTheme.FEEDBACK_DURATION_MS)
            viewModel.advanceAfterFeedback(subject)
        }
    }

    // Mascot rescue when hearts run out
    if (viewModel.showRescueOffer) {
        LaunchedEffect(Unit) { sound.play(com.example.util.Sfx.RESCUE) }
        com.example.ui.kids.MascotRescueDialog(
            onAccept = {
                sound.play(com.example.util.Sfx.TAP)
                viewModel.acceptRescue(subject)
            },
            onDecline = { viewModel.declineRescue(subject) },
            mascot = {
                Mascot(
                    state = MascotState.ENCOURAGE,
                    modifier = Modifier.size(110.dp)
                )
            }
        )
    }

    val initialTimeMinutes = viewModel.subjectTimers[subject] ?: 0
    var timeLeftSeconds by remember(subject) { mutableStateOf(initialTimeMinutes * 60) }
    val toneGenerator = remember { ToneGenerator(AudioManager.STREAM_ALARM, 100) }
    
    DisposableEffect(Unit) {
        onDispose {
            toneGenerator.release()
        }
    }
    
    LaunchedEffect(timeLeftSeconds, initialTimeMinutes, currentQuestionIdx) {
        if (initialTimeMinutes > 0 && currentQuestion != null) {
            if (timeLeftSeconds > 0) {
                if (timeLeftSeconds <= 10) {
                    toneGenerator.startTone(ToneGenerator.TONE_CDMA_PIP, 150)
                }
                kotlinx.coroutines.delay(1000L)
                timeLeftSeconds--
            } else if (timeLeftSeconds == 0) {
                viewModel.forceEndQuiz(subject)
            }
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Exit button
                IconButton(
                    onClick = { viewModel.navigateTo(Screen.Home) },
                    modifier = Modifier
                        .background(SleekSurfaceVariant, CircleShape)
                        .size(44.dp)
                ) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = SleekPurple)
                }

                // Progress Indicator text
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$subject Quiz!",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = SleekPurple
                    )
                    if (initialTimeMinutes > 0) {
                        val mins = timeLeftSeconds / 60
                        val secs = timeLeftSeconds % 60
                        
                        val infiniteTransition = rememberInfiniteTransition(label="timer_throb")
                        val scale by infiniteTransition.animateFloat(
                            initialValue = 1f,
                            targetValue = if (timeLeftSeconds <= 10 && timeLeftSeconds > 0) 1.2f else 1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(500),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "timer_throb_scale"
                        )

                        Text(
                            text = "⏱️ ${String.format("%02d:%02d", mins, secs)}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (timeLeftSeconds <= 10) Color(0xFFBA1A1A) else SleekPurple,
                            modifier = Modifier.scale(scale)
                        )
                    }
                }

                // Hearts + current stars
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    com.example.ui.kids.HeartsRow(heartsRemaining = viewModel.heartsRemaining)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(Color(0xFFEADDFF), RoundedCornerShape(100.dp))
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(text = "⭐ $activeScore", fontWeight = FontWeight.Bold, color = Color(0xFF21005D))
                    }
                }
            }
        }
    ) { paddingValues ->
        if (currentQuestion == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues), contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    CircularProgressIndicator(color = SleekPurple)
                    Text(text = "Setting up quiz questions...", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = SleekTextDark)
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Interactive Mascot
                val mascotState = if (isAnswerChecked) {
                    when {
                        viewModel.lastAnswerCorrect != true -> MascotState.ENCOURAGE
                        viewModel.comboCount >= 3 -> MascotState.CELEBRATE
                        else -> MascotState.CHEER
                    }
                } else {
                    MascotState.IDLE
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box {
                        Mascot(
                            state = mascotState,
                            modifier = Modifier.size(100.dp)
                        )
                        com.example.ui.kids.FloatingStarBurst(
                            trigger = currentQuestionIdx to isAnswerChecked,
                            visible = isAnswerChecked && viewModel.lastAnswerCorrect == true,
                            modifier = Modifier.align(Alignment.CenterEnd)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        com.example.ui.kids.ComboBadge(comboCount = viewModel.comboCount)
                        // Reaction line, re-rolled per answer; greeting on question 1
                        val bubbleText = remember(currentQuestionIdx, isAnswerChecked) {
                            when {
                                isAnswerChecked && viewModel.lastAnswerCorrect == true ->
                                    com.example.ui.kids.MascotMessages.forCorrect(viewModel.comboCount)
                                isAnswerChecked ->
                                    com.example.ui.kids.MascotMessages.forWrong()
                                currentQuestionIdx == 0 ->
                                    com.example.ui.kids.MascotMessages.forQuizStart()
                                else -> ""
                            }
                        }
                        com.example.ui.kids.MascotSpeechBubble(
                            text = bubbleText,
                            visible = bubbleText.isNotBlank()
                        )
                    }
                }

                // Linear Progress Segment
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Question ${currentQuestionIdx + 1} of ${activeQuestions.size}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = SleekPurple
                        )
                        Text(
                            text = "${((currentQuestionIdx + 1) * 100) / activeQuestions.size}% Done",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = SleekPurple.copy(alpha = 0.7f)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    com.example.ui.kids.JuicyProgressBar(
                        progress = (currentQuestionIdx + 1).toFloat() / activeQuestions.size
                    )
                }

                // Question Box
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = SleekSurfaceVariant),
                    border = BorderStroke(1.dp, SleekBorderLight)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = currentQuestion.questionText,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            lineHeight = 32.sp,
                            textAlign = TextAlign.Center,
                            color = SleekPurple
                        )
                    }
                }

                // Playful guide helper
                Text(
                    text = "Pick the best answer below! 👇",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF49454F),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                // Option Choice Buttons
                val options = listOf(
                    currentQuestion.optionA,
                    currentQuestion.optionB,
                    currentQuestion.optionC,
                    currentQuestion.optionD
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    options.forEachIndexed { idx, optionText ->
                        if (optionText.isNotBlank()) {
                            val isCorrectOption = idx == currentQuestion.correctOptionIndex
                            val isWrongPick = isAnswerChecked && selectedOptionIdx == idx && !isCorrectOption

                            // Style determination based on whether answer is verified or not
                            val buttonColor = when {
                                isAnswerChecked -> {
                                    when {
                                        isCorrectOption -> com.example.ui.kids.KidsTheme.SuccessGreenLight
                                        selectedOptionIdx == idx -> com.example.ui.kids.KidsTheme.ErrorRedLight
                                        else -> SleekSurfaceVariant
                                    }
                                }
                                selectedOptionIdx == idx -> Color(0xFFEADDFF) // selected purple tint
                                else -> SleekSurfaceVariant
                            }

                            val borderColor = when {
                                isAnswerChecked && isCorrectOption -> com.example.ui.kids.KidsTheme.SuccessGreen
                                isWrongPick -> com.example.ui.kids.KidsTheme.ErrorRed
                                selectedOptionIdx == idx -> SleekPurple
                                else -> SleekBorderLight
                            }
                            val strokeWidth = if (selectedOptionIdx == idx || (isAnswerChecked && isCorrectOption)) 2.dp else 1.dp
                            val textStyleColor = SleekTextDark

                            // Correct answer pops; wrong pick shakes
                            val revealScale by animateFloatAsState(
                                targetValue = if (isAnswerChecked && isCorrectOption) 1.04f else 1f,
                                animationSpec = com.example.ui.kids.kidsBouncySpring(),
                                label = "option_reveal_$idx"
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 60.dp)
                                    .scale(revealScale)
                                    .shake(trigger = isWrongPick)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(buttonColor)
                                    .border(strokeWidth, borderColor, RoundedCornerShape(20.dp))
                                    .testTag("quiz_option_$idx")
                                    .clickable(enabled = !isAnswerChecked) {
                                        sound.play(com.example.util.Sfx.TAP)
                                        haptics.tick()
                                        viewModel.selectOption(idx)
                                    }
                                    .padding(horizontal = 20.dp, vertical = 14.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    // Option Prefix Icon indicator
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .background(
                                                color = if (selectedOptionIdx == idx) SleekPurple else SleekBorderDark,
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = ('A'.code + idx).toChar().toString(),
                                            color = if (selectedOptionIdx == idx) Color.White else SleekTextDark,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 14.sp
                                        )
                                    }

                                    Text(
                                        text = optionText,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = textStyleColor,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bottom Action buttons (Check Answer / Next question)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp)
                ) {
                    val isLast = currentQuestionIdx == activeQuestions.size - 1
                    Button(
                        onClick = {
                            viewModel.evaluateAnswer()
                        },
                        enabled = selectedOptionIdx != -1 && !isAnswerChecked,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = KidsTheme.SuccessGreen,
                            disabledContainerColor = Color(0xFFE7E0EC)
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("submit_answer_button")
                    ) {
                        val buttonText = when {
                            isAnswerChecked -> "..."
                            isLast -> "See Results! 🏆"
                            else -> "Check! ✅"
                        }
                        Text(text = buttonText, fontSize = 18.sp, fontWeight = FontWeight.Black, color = if (selectedOptionIdx != -1) Color.White else Color(0xFFCAC4D0))
                    }
                }
            }
        }
    }
}

@Composable
fun QuizResultScreen(
    viewModel: QuizViewModel,
    subject: String,
    score: Int,
    total: Int,
    starsEarned: Int
) {
    var scratchClaimed by remember { mutableStateOf(false) }
    var scratchRevealed by remember { mutableStateOf(false) }

    val sound = com.example.util.LocalSoundManager.current
    val haptics = com.example.util.LocalHapticsManager.current
    val endedEarly = viewModel.endedEarly
    // A quiz only "went well" if it wasn't cut short AND the score actually
    // clears a real bar — an early end or a low score (e.g. 2/5) shouldn't
    // trigger the same celebration as a strong finish.
    val quizWentWell = !endedEarly && total > 0 && score.toFloat() / total >= 0.6f

    // Entry fanfare (gentler when the result isn't something to celebrate)
    LaunchedEffect(Unit) {
        if (quizWentWell) {
            sound.play(Sfx.FANFARE)
            haptics.celebrate()
        } else {
            sound.play(Sfx.RESCUE)
        }
    }

    Scaffold(containerColor = Color.Transparent) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            com.example.ui.kids.ConfettiOverlay(active = quizWentWell)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Spacer(modifier = Modifier.height(48.dp))

                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .background(
                            brush = Brush.radialGradient(listOf(Color(0xFFFEF7FF), Color(0xFFEADDFF))),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Mascot(
                        state = if (quizWentWell) MascotState.CELEBRATE else MascotState.ENCOURAGE,
                        modifier = Modifier.size(140.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = if (quizWentWell) "YAY! YOU DID IT! 🎉" else "GOOD TRY! 💪",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Black,
                    color = SleekPurple,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = when {
                        quizWentWell -> "You completed the $subject quiz!"
                        endedEarly -> "Let's practice and try $subject again — you're learning!"
                        else -> "So close! A little more practice with $subject and you'll nail it!"
                    },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF49454F),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Bonus reward only for a quiz that actually went well — matches
                // the same bar used for the celebration above.
                if (!scratchClaimed && quizWentWell) {
                    // Show Scratch Card Game
                    Card(
                        shape = RoundedCornerShape(32.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF7FF)),
                        border = BorderStroke(3.dp, Color(0xFFFFD54F)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "🎁 BONUS REWARD! 🎁",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                color = SleekPurple
                            )
                            
                            Text(
                                text = "Scratch the card below to reveal your bonus learning reward!",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF49454F),
                                textAlign = TextAlign.Center
                            )
                            
                            ScratchCard(
                                onScratchComplete = {
                                    scratchRevealed = true
                                },
                                rewardText = "+5 BONUS STARS! ⭐",
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                            
                            if (scratchRevealed) {
                                val claimScale by animateFloatAsState(
                                    targetValue = 1f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    ),
                                    label = "claim_scale"
                                )
                                Button(
                                    onClick = {
                                        viewModel.claimScratchStars(5)
                                        scratchClaimed = true
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                                    shape = RoundedCornerShape(20.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp)
                                        .scale(claimScale)
                                ) {
                                    Text("Claim 5 Bonus Stars! 👑", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                } else {
                    // Show standard report statistics when claimed
                    Card(
                         shape = RoundedCornerShape(32.dp),
                         colors = CardDefaults.cardColors(containerColor = SleekSurfaceVariant),
                         border = BorderStroke(1.dp, SleekBorderLight),
                         modifier = Modifier
                             .fillMaxWidth()
                             .padding(horizontal = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "QUIZ REPORT",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = SleekPurple,
                                letterSpacing = 1.sp
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .background(Color(0xFFEADDFF), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        com.example.ui.kids.ScoreRollUp(
                                            score = score,
                                            total = total,
                                            onTick = {
                                                sound.play(Sfx.TICK)
                                                haptics.tick()
                                            }
                                        )
                                        Text(text = "Correct", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF49454F))
                                    }
                                }

                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = "Quiz Rating",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SleekPurple
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        val rating = if (total > 0) (score.toFloat() / total * 5).toInt() else 0
                                        for (i in 1..5) {
                                            Text(
                                                text = if (i <= rating) "⭐" else "☆",
                                                fontSize = 24.sp,
                                                color = if (i <= rating) Color(0xFFFFD700) else Color.LightGray
                                            )
                                        }
                                    }
                                    Text(
                                        text = "Earned $starsEarned New Stars!",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF49454F)
                                    )
                                }
                            }

                            val percentage = if (total > 0) (score.toFloat() / total * 100) else 0f
                            val motivationMessage = when {
                                percentage >= 100f -> "🌟 UNBELIEVABLE! A PERFECT SCORE! You are a genius scientist! 🚀"
                                percentage >= 80f -> "🌸 EXCELLENT WORK! You know almost everything! High five! 🙌"
                                percentage >= 60f -> "👍 GOOD JOB! You're doing great, keep it up! ✨"
                                percentage >= 40f -> "💪 NICE TRY! You're learning fast! Let's practice more! 📚"
                                else -> "🏃 DON'T GIVE UP! Every mistake is a step towards learning! Try again! ❤️"
                            }

                            Text(
                                text = motivationMessage,
                                textAlign = TextAlign.Center,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = SleekTextDark,
                                lineHeight = 20.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFEADDFF).copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                                    .border(1.dp, SleekBorderLight, RoundedCornerShape(16.dp))
                                    .padding(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Streak day celebration with weekly calendar
                    val quizDates = remember { viewModel.preferences.getQuizDates() }
                    com.example.ui.kids.StreakDayCelebrationCard(
                        streakCount = viewModel.streakCount,
                        days = com.example.ui.kids.last7Days(quizDates),
                        isNewStreakDay = viewModel.isNewStreakDay
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    if (viewModel.activeQuestions.isNotEmpty() && viewModel.userAnswers.isNotEmpty()) {
                        Text(
                            text = "Detailed Answers Report 📝",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = SleekPurple,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Start
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        viewModel.activeQuestions.forEachIndexed { idx, q ->
                            val uAns = viewModel.userAnswers.getOrNull(idx) ?: -1
                            val isCorrect = uAns == q.correctOptionIndex
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = if (isCorrect) Color(0xFFF1F8F1) else Color(0xFFFCF2F2)),
                                border = BorderStroke(1.dp, if (isCorrect) Color(0xFFD4EDDA) else Color(0xFFF8D7DA))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Q${idx + 1}: ${q.questionText}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SleekTextDark)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    val ops = listOf(q.optionA, q.optionB, q.optionC, q.optionD)
                                    val uStr = if (uAns in ops.indices) ops[uAns] else "Out of Time / Skipped"
                                    Text(
                                        "Your Answer: $uStr", 
                                        color = if (isCorrect) Color(0xFF28A745) else Color(0xFFDC3545), 
                                        fontSize = 13.sp, 
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (!isCorrect) {
                                        val cStr = ops.getOrNull(q.correctOptionIndex) ?: ""
                                        Text(
                                            "Correct Answer: $cStr", 
                                            color = Color(0xFF28A745), 
                                            fontSize = 13.sp, 
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { viewModel.navigateTo(Screen.Home) },
                        colors = ButtonDefaults.buttonColors(containerColor = SleekPurple),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("result_done_button")
                    ) {
                        Text(text = "Awesome! Back to Home 🏠", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}

@Composable
fun ParentPinScreen(viewModel: QuizViewModel) {
    val inputPin = viewModel.parentPinInput
    val isIncorrect = viewModel.isPinIncorrect
    val setupNeeded = viewModel.pinSetupNeeded

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        viewModel.parentPinInput = ""
                        viewModel.isPinIncorrect = false
                        viewModel.navigateTo(Screen.Home)
                    },
                    modifier = Modifier
                        .background(SleekSurfaceVariant, CircleShape)
                        .size(44.dp)
                ) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = SleekPurple)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 20.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .background(Color(0xFFEADDFF), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🔒", fontSize = 34.sp)
                }

                Text(
                    text = if (setupNeeded) "Set Parent Pin 🛡️" else "Parents Area Keypad",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = SleekPurple
                )

                Text(
                    text = if (setupNeeded) "Enter a 4-digit PIN code to secure parent controls. Default passcode is 1234" else "Please enter your 4-digit secret PIN code.",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF49454F),
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // Asterisks PIN dots preview visual
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 0 until 4) {
                        val active = i < inputPin.length
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(
                                    color = if (active) SleekPurple else Color(0xFFEADDFF).copy(alpha = 0.5f),
                                    shape = CircleShape
                                )
                                .border(
                                    width = 2.dp,
                                    color = if (active) SleekPurple else SleekBorderDark,
                                    shape = CircleShape
                                )
                        )
                    }
                }

                // Error message
                if (isIncorrect) {
                    Text(
                        text = "❌ Incorrect passcode PIN! Please try again.",
                        color = Color(0xFFBA1A1A),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                } else if (setupNeeded) {
                    Text(
                        text = "Default PIN is 1234. You can change this below.",
                        color = Color(0xFFE28F00),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                } else {
                    Spacer(modifier = Modifier.height(18.dp))
                }
            }

            // Custom Physical Button Number Pad UI
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(bottom = 40.dp)
            ) {
                val row1 = listOf("1", "2", "3")
                val row2 = listOf("4", "5", "6")
                val row3 = listOf("7", "8", "9")

                PinKeyboardRow(numbers = row1, onKey = { k -> onPinKeyPress(k, viewModel) })
                PinKeyboardRow(numbers = row2, onKey = { k -> onPinKeyPress(k, viewModel) })
                PinKeyboardRow(numbers = row3, onKey = { k -> onPinKeyPress(k, viewModel) })

                // Clears, Zero
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.wrapContentSize()
                ) {
                    Button(
                        onClick = { viewModel.parentPinInput = "" },
                        colors = ButtonDefaults.buttonColors(containerColor = SleekSurfaceVariant),
                        shape = CircleShape,
                        modifier = Modifier
                            .size(72.dp)
                            .testTag("pin_key_clear")
                    ) {
                        Text(text = "CLR", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SleekPurple)
                    }

                    Button(
                        onClick = { onPinKeyPress("0", viewModel) },
                        colors = ButtonDefaults.buttonColors(containerColor = SleekSurfaceVariant),
                        shape = CircleShape,
                        border = BorderStroke(1.dp, SleekBorderLight),
                        modifier = Modifier
                            .size(72.dp)
                            .testTag("pin_key_0")
                    ) {
                        Text(text = "0", fontSize = 22.sp, fontWeight = FontWeight.Black, color = SleekPurple)
                    }

                    // Done/Unlock
                    Button(
                        onClick = {
                            if (setupNeeded) {
                                // Save input Pin
                                if (inputPin.length == 4) {
                                    viewModel.changeParentPin(inputPin)
                                    viewModel.parentPinInput = ""
                                    viewModel.navigateTo(Screen.ParentDashboard)
                                }
                            } else {
                                viewModel.verifyParentPin()
                             }
                        },
                        enabled = inputPin.length == 4,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SleekPurple,
                            disabledContainerColor = Color(0xFFE7E0EC)
                        ),
                        shape = CircleShape,
                        modifier = Modifier
                            .size(72.dp)
                            .testTag("pin_key_enter")
                    ) {
                        Text(text = "GO", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (inputPin.length == 4) Color.White else Color(0xFFCAC4D0))
                    }
                }
            }
        }
    }
}

fun onPinKeyPress(key: String, viewModel: QuizViewModel) {
    if (viewModel.parentPinInput.length < 4) {
        viewModel.parentPinInput = viewModel.parentPinInput + key
        viewModel.isPinIncorrect = false
    }
}

@Composable
fun PinKeyboardRow(numbers: List<String>, onKey: (String) -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.wrapContentSize()
    ) {
        numbers.forEach { num ->
            Button(
                onClick = { onKey(num) },
                colors = ButtonDefaults.buttonColors(containerColor = SleekSurfaceVariant),
                shape = CircleShape,
                border = BorderStroke(1.dp, SleekBorderLight),
                modifier = Modifier
                    .size(72.dp)
                    .testTag("pin_key_$num")
            ) {
                Text(text = num, fontSize = 22.sp, fontWeight = FontWeight.Black, color = SleekPurple)
            }
        }
    }
}

@Composable
fun ParentDashboardScreen(viewModel: QuizViewModel) {
    val allQuestions by viewModel.allQuestions.collectAsState()
    val allAttempts by viewModel.allAttempts.collectAsState()

    var activeTab by remember { mutableStateOf(0) } // 0: Scores, 1: Questions, 2: Subjects, 3: Archive, 4: Settings
    var showParentExitDialog by remember { mutableStateOf(false) }

    // Back on tab 0 → show exit confirmation; other tabs → go back to tab 0
    androidx.activity.compose.BackHandler {
        if (activeTab != 0) {
            activeTab = 0
        } else {
            showParentExitDialog = true
        }
    }

    // Parent Exit Confirmation Dialog
    if (showParentExitDialog) {
        Dialog(onDismissRequest = { showParentExitDialog = false }) {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("🔒", fontSize = 52.sp)
                    Text(
                        text = "Exit Parent Section?",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = SleekPurple,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "You'll be taken back to the Kids section. All your changes have been saved! ✅",
                        fontSize = 13.sp,
                        color = Color(0xFF6C757D),
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showParentExitDialog = false },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(2.dp, SleekPurple)
                        ) {
                            Text("Stay Here", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SleekPurple)
                        }
                        Button(
                            onClick = {
                                showParentExitDialog = false
                                viewModel.navigateTo(Screen.Home)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SleekPurple)
                        ) {
                            Text("Exit ✅", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = when (activeTab) {
                        0 -> "Score History 📈"
                        1 -> "Questions 📝"
                        2 -> "Manage Subjects 📚"
                        3 -> "Archived Bank 🗄️"
                        4 -> "Settings ⚙️"
                        else -> "Admin Panel 🛠️"
                    },
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = SleekPurple
                )
                IconButton(
                    onClick = { showParentExitDialog = true },
                    modifier = Modifier
                        .background(SleekSurfaceVariant, CircleShape)
                        .size(36.dp)
                ) {
                    Icon(imageVector = Icons.Default.ExitToApp, contentDescription = "Exit", tint = SleekPurple, modifier = Modifier.size(18.dp))
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = SleekBgLight,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    icon = { Text("📈", fontSize = 20.sp) },
                    label = { Text("Scores", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = SleekPurple.copy(alpha = 0.15f)
                    )
                )
                NavigationBarItem(
                    icon = { Text("📝", fontSize = 20.sp) },
                    label = { Text("Questions", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = SleekPurple.copy(alpha = 0.15f)
                    )
                )
                NavigationBarItem(
                    icon = { Text("📚", fontSize = 20.sp) },
                    label = { Text("Subjects", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = SleekPurple.copy(alpha = 0.15f)
                    )
                )
                NavigationBarItem(
                    icon = { Text("🗄️", fontSize = 20.sp) },
                    label = { Text("Archive", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    selected = activeTab == 3,
                    onClick = { activeTab = 3 },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = SleekPurple.copy(alpha = 0.15f)
                    )
                )
                NavigationBarItem(
                    icon = { Text("⚙️", fontSize = 20.sp) },
                    label = { Text("Settings", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    selected = activeTab == 4,
                    onClick = { activeTab = 4 },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = SleekPurple.copy(alpha = 0.15f)
                    )
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (activeTab) {
                0 -> ParentScoresHistoryTab(allAttempts = allAttempts)
                1 -> ParentQuestionsTab(viewModel = viewModel, allQuestions = allQuestions)
                2 -> ParentSubjectsTab(viewModel = viewModel)
                3 -> ParentArchiveTab(viewModel = viewModel, allQuestions = allQuestions)
                4 -> ParentSettingsTab(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun ParentScoresHistoryTab(allAttempts: List<QuizAttempt>) {
    // -1: Landing Hub, 0: Full History, 1: Subject Detail (filtered by subject)
    var activeMode by remember { mutableStateOf(-1) }
    var selectedSubject by remember { mutableStateOf("") }

    androidx.activity.compose.BackHandler(enabled = activeMode != -1) {
        activeMode = -1
        selectedSubject = ""
    }

    // Computed stats
    val totalSessions = allAttempts.size
    val totalStars = allAttempts.sumOf { it.starsEarned }
    val perfectSessions = allAttempts.count { it.score == it.totalQuestions }
    val avgAccuracy = if (totalSessions > 0)
        (allAttempts.sumOf { it.score }.toFloat() / allAttempts.sumOf { it.totalQuestions }.toFloat() * 100).toInt()
    else 0
    val subjectGroups = allAttempts.groupBy { it.subject }

    AnimatedContent(
        targetState = activeMode,
        transitionSpec = { fadeIn(animationSpec = tween(200)) togetherWith fadeOut(animationSpec = tween(200)) },
        label = "scores_mode"
    ) { mode ->
        when (mode) {
            -1 -> {
                // LANDING HUB
                if (allAttempts.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("📭", fontSize = 56.sp)
                            Text(
                                text = "No scores recorded yet!",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = SleekPurple
                            )
                            Text(
                                text = "When the kid plays a quiz, their results, stars, and completion history will appear here.",
                                fontSize = 13.sp,
                                color = Color(0xFF6C757D),
                                lineHeight = 20.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Progress Overview 📈",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = SleekPurple,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        // Summary stats row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Sessions card
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                                border = BorderStroke(1.dp, Color(0xFFA5D6A7))
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text("🎮", fontSize = 24.sp)
                                    Text("$totalSessions", fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color(0xFF2E7D32))
                                    Text("Sessions", fontSize = 11.sp, color = Color(0xFF388E3C), fontWeight = FontWeight.Bold)
                                }
                            }
                            // Stars card
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9C4)),
                                border = BorderStroke(1.dp, Color(0xFFFFF176))
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text("⭐", fontSize = 24.sp)
                                    Text("$totalStars", fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color(0xFFF9A825))
                                    Text("Stars Earned", fontSize = 11.sp, color = Color(0xFFF57F17), fontWeight = FontWeight.Bold)
                                }
                            }
                            // Accuracy card
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                                border = BorderStroke(1.dp, Color(0xFF90CAF9))
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text("🎯", fontSize = 24.sp)
                                    Text("$avgAccuracy%", fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color(0xFF1565C0))
                                    Text("Accuracy", fontSize = 11.sp, color = Color(0xFF1976D2), fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Perfect sessions badge
                        if (perfectSessions > 0) {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5)),
                                border = BorderStroke(1.dp, Color(0xFFCE93D8)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("🏆", fontSize = 28.sp)
                                    Column {
                                        Text("$perfectSessions Perfect Score${if (perfectSessions > 1) "s" else ""}!", fontWeight = FontWeight.Black, fontSize = 14.sp, color = Color(0xFF6A1B9A))
                                        Text("Amazing! Got every question right.", fontSize = 12.sp, color = Color(0xFF8E24AA))
                                    }
                                }
                            }
                        }

                        // Full history card
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                            border = BorderStroke(1.dp, Color(0xFFBBDEFB)),
                            modifier = Modifier.fillMaxWidth().clickable { activeMode = 0 }
                        ) {
                            Row(
                                modifier = Modifier.padding(20.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier.size(56.dp).background(Color(0xFFBBDEFB), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) { Text("📋", fontSize = 28.sp) }
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
                                    Text("Full History", fontWeight = FontWeight.Black, fontSize = 15.sp, color = SleekTextDark)
                                    Text("Browse all $totalSessions quiz sessions in one place", fontSize = 12.sp, color = Color(0xFF6C757D), lineHeight = 18.sp)
                                }
                                Text("›", fontSize = 24.sp, color = SleekPurple, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Subject breakdown section
                        Text(
                            text = "By Subject",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = SleekPurple,
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        val subjectColors = mapOf(
                            "Math" to Pair(Color(0xFFE8F5E9), Color(0xFFA5D6A7)),
                            "English" to Pair(Color(0xFFFFF9C4), Color(0xFFFFF176)),
                            "Science" to Pair(Color(0xFFFCE4EC), Color(0xFFF48FB1)),
                            "General Knowledge" to Pair(Color(0xFFF3E5F5), Color(0xFFCE93D8))
                        )
                        val subjectEmojis = mapOf(
                            "Math" to "🧮", "English" to "📝", "Science" to "🧪", "General Knowledge" to "🌍"
                        )

                        subjectGroups.entries.forEachIndexed { idx, (subject, attempts) ->
                            val colors = subjectColors[subject] ?: Pair(Color(0xFFF5F5F5), Color(0xFFBDBDBD))
                            val emoji = subjectEmojis[subject] ?: "🌟"
                            val subAvg = if (attempts.isNotEmpty())
                                (attempts.sumOf { it.score }.toFloat() / attempts.sumOf { it.totalQuestions }.toFloat() * 100).toInt()
                            else 0
                            Card(
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = colors.first),
                                border = BorderStroke(1.dp, colors.second),
                                modifier = Modifier.fillMaxWidth().clickable {
                                    selectedSubject = subject
                                    activeMode = 1
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(20.dp),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier.size(56.dp).background(colors.second, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) { Text(emoji, fontSize = 28.sp) }
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
                                        Text(subject, fontWeight = FontWeight.Black, fontSize = 15.sp, color = SleekTextDark)
                                        Text("${attempts.size} session${if (attempts.size != 1) "s" else ""} · Avg accuracy: $subAvg%", fontSize = 12.sp, color = Color(0xFF6C757D), lineHeight = 18.sp)
                                    }
                                    Text("›", fontSize = 24.sp, color = SleekPurple, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
            0 -> {
                // FULL HISTORY LIST
                val sortedAttempts = allAttempts.sortedByDescending { it.timestamp }
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = { activeMode = -1 },
                            modifier = Modifier.size(36.dp).background(SleekSurfaceVariant, CircleShape)
                        ) {
                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = SleekPurple, modifier = Modifier.size(18.dp))
                        }
                        Text("All Sessions (${sortedAttempts.size})", fontWeight = FontWeight.Black, fontSize = 16.sp, color = SleekPurple)
                    }
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(sortedAttempts) { attempt ->
                            ScoreAttemptCard(attempt = attempt)
                        }
                    }
                }
            }
            1 -> {
                // SUBJECT-FILTERED LIST
                val filtered = allAttempts.filter { it.subject == selectedSubject }.sortedByDescending { it.timestamp }
                val subEmojis = mapOf("Math" to "🧮", "English" to "📝", "Science" to "🧪", "General Knowledge" to "🌍")
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = { activeMode = -1; selectedSubject = "" },
                            modifier = Modifier.size(36.dp).background(SleekSurfaceVariant, CircleShape)
                        ) {
                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = SleekPurple, modifier = Modifier.size(18.dp))
                        }
                        Text("${subEmojis[selectedSubject] ?: "🌟"} $selectedSubject (${filtered.size})", fontWeight = FontWeight.Black, fontSize = 16.sp, color = SleekPurple)
                    }
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filtered) { attempt ->
                            ScoreAttemptCard(attempt = attempt)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScoreAttemptCard(attempt: QuizAttempt) {
    val dateFmt = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault())
    val formattedDate = dateFmt.format(Date(attempt.timestamp))
    var expanded by remember { mutableStateOf(false) }
    val pct = if (attempt.totalQuestions > 0) (attempt.score.toFloat() / attempt.totalQuestions * 100).toInt() else 0
    val scoreColor = when {
        pct == 100 -> Color(0xFF2E7D32)
        pct >= 70 -> Color(0xFF1565C0)
        else -> Color(0xFFC62828)
    }
    val subjectEmoji = when (attempt.subject) {
        "Math" -> "🧮"; "English" -> "📝"; "General Knowledge" -> "🌍"; "Science" -> "🧪"; else -> "🌟"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { if (attempt.answersJson.isNotBlank()) expanded = !expanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SleekSurfaceVariant),
        border = BorderStroke(1.dp, SleekBorderLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(scoreColor.copy(alpha = 0.1f), CircleShape)
                            .border(1.5.dp, scoreColor.copy(alpha = 0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) { Text(subjectEmoji, fontSize = 22.sp) }
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(attempt.subject, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SleekTextDark)
                        Text(formattedDate, fontSize = 11.sp, color = Color(0xFF6C757D))
                        if (attempt.answersJson.isNotBlank()) {
                            Text(
                                text = if (expanded) "▲ Hide Details" else "▼ View Details",
                                fontSize = 10.sp,
                                color = SleekPurple,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "${attempt.score}/${attempt.totalQuestions}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = scoreColor
                    )
                    Text("$pct%", fontSize = 11.sp, color = scoreColor, fontWeight = FontWeight.Bold)
                    Row { repeat(attempt.starsEarned) { Text("⭐", fontSize = 12.sp) } }
                }
            }

            if (expanded && attempt.answersJson.isNotBlank()) {
                HorizontalDivider(color = SleekBorderLight, thickness = 1.dp)
                val parsedDetails = remember(attempt.answersJson) {
                    val list = mutableListOf<Triple<String, Triple<String, String, Boolean>, String>>()
                    try {
                        val jArray = org.json.JSONArray(attempt.answersJson)
                        for (i in 0 until jArray.length()) {
                            val obj = jArray.getJSONObject(i)
                            val qText = obj.getString("q")
                            val optsArray = obj.getJSONArray("opts")
                            val cIdx = obj.getInt("c")
                            val sIdx = obj.getInt("s")
                            val isCorrect = cIdx == sIdx
                            val uStr = if (sIdx in 0 until optsArray.length()) optsArray.getString(sIdx) else "Out of Time / Skipped"
                            val cStr = if (cIdx in 0 until optsArray.length()) optsArray.getString(cIdx) else ""
                            list.add(Triple(qText, Triple(uStr, cStr, isCorrect), ""))
                        }
                    } catch (e: Exception) {
                        list.add(Triple("", Triple("", "", false), "Error parsing details"))
                    }
                    list
                }
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    parsedDetails.forEachIndexed { i, detail ->
                        if (detail.third.isNotEmpty()) {
                            Text("Failed to parse test details.", color = Color.Red, fontSize = 12.sp)
                        } else {
                            val isCorrect = detail.second.third
                            val uStr = detail.second.first
                            val cStr = detail.second.second
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(if (isCorrect) Color(0xFFF1F8F1) else Color(0xFFFCF2F2), RoundedCornerShape(10.dp))
                                    .border(1.dp, if (isCorrect) Color(0xFFD4EDDA) else Color(0xFFF8D7DA), RoundedCornerShape(10.dp))
                                    .padding(12.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(if (isCorrect) "✅" else "❌", fontSize = 14.sp)
                                        Text("Q${i + 1}: ${detail.first}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SleekTextDark, lineHeight = 16.sp)
                                    }
                                    Text("Answer: $uStr", color = if (isCorrect) Color(0xFF2E7D32) else Color(0xFFC62828), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    if (!isCorrect) {
                                        Text("Correct: $cStr", color = Color(0xFF2E7D32), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ParentQuestionsTab(viewModel: QuizViewModel, allQuestions: List<Question>) {
    var activeMode by remember { mutableStateOf(-1) }
    val context = LocalContext.current
    var showDeleteAllDialog by remember { mutableStateOf(false) }

    if (showDeleteAllDialog) {
        Dialog(onDismissRequest = { showDeleteAllDialog = false }) {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F2FA)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(text = "🗑️", fontSize = 48.sp, textAlign = TextAlign.Center)
                    Text(
                        text = "Delete All Custom Questions?",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFD32F2F),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "This will permanently remove all your custom questions. Default questions are not affected.",
                        fontSize = 13.sp,
                        color = Color(0xFF49454F),
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                    HorizontalDivider(color = Color(0xFFE5E0E9))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showDeleteAllDialog = false },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.5.dp, Color(0xFF6750A4))
                        ) {
                            Text("Cancel", color = Color(0xFF6750A4), fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { showDeleteAllDialog = false; viewModel.clearAllCustomQuestions(allQuestions) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                        ) {
                            Text("Delete All", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    val fileImportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val fileContent = inputStream.bufferedReader().use { it.readText() }
                    viewModel.importQuestionsFromFile(fileContent)
                }
            } catch (e: Exception) {
                viewModel.fileImportStatus = "Failed to load file: ${e.localizedMessage}"
            }
        }
    }

    val sampleFileExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    val sampleData = """[
  {
    "questionText": "What is the capital of France?",
    "options": ["Paris", "London", "Berlin", "Madrid"],
    "correctAnswerIndex": 0,
    "subject": "Geography",
    "difficulty": "Easy"
  }
]"""
                    outputStream.write(sampleData.toByteArray())
                    viewModel.fileImportStatus = "Sample file downloaded successfully!"
                }
            } catch (e: Exception) {
                viewModel.fileImportStatus = "Failed to download sample: ${e.localizedMessage}"
            }
        }
    }

    val totalQs = allQuestions.filter { !it.isArchived }.size
    val customQsList = allQuestions.filter { !it.isArchived && it.isCustom }
    val customQs = customQsList.size
    val defaultQs = totalQs - customQs
    
    androidx.activity.compose.BackHandler(enabled = activeMode != -1) {
        activeMode = -1
    }

    Column(modifier = Modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = activeMode,
            transitionSpec = { fadeIn(animationSpec = tween(200)) togetherWith fadeOut(animationSpec = tween(200)) },
            label = "questions_mode"
        ) { mode ->
            when (mode) {
                -1 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF6750A4)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "📚 Question Bank",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Card(
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.2f))
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(text = "$totalQs", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color.White)
                                            Text(text = "Total", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                                        }
                                    }
                                    Card(
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.2f))
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(text = "$defaultQs", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color.White)
                                            Text(text = "Default", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                                        }
                                    }
                                    Card(
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.2f))
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(text = "$customQs", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color.White)
                                            Text(text = "Custom", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                                        }
                                    }
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Button(
                                        onClick = { activeMode = 0 },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                                    ) {
                                        Text("View Bank 👁️", color = Color(0xFF6750A4), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                    Button(
                                        onClick = { activeMode = 2 },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD600))
                                    ) {
                                        Text("AI Generate ✨", color = Color(0xFF1A1A1A), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                        Text(
                            text = "How would you like to add questions?",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF1D1B20)
                        )
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF3EDF7)),
                            border = BorderStroke(1.dp, Color(0xFFE5E0E9)),
                            modifier = Modifier.fillMaxWidth().clickable { activeMode = 1 }
                        ) {
                            Row(
                                modifier = Modifier.padding(20.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier.size(56.dp).background(Color(0xFFE8F5E9), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) { Text("✍️", fontSize = 28.sp) }
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
                                    Text("Write Your Own Question", fontWeight = FontWeight.Black, fontSize = 15.sp, color = Color(0xFF1D1B20))
                                    Text("Type in a custom question with 4 answer choices step by step.", fontSize = 12.sp, color = Color(0xFF6C757D), lineHeight = 18.sp)
                                }
                                Text("›", fontSize = 24.sp, color = Color(0xFF6750A4), fontWeight = FontWeight.Bold)
                            }
                        }
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDE7)),
                            border = BorderStroke(1.dp, Color(0xFFFFEB3B)),
                            modifier = Modifier.fillMaxWidth().clickable { activeMode = 2 }
                        ) {
                            Row(
                                modifier = Modifier.padding(20.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier.size(56.dp).background(Color(0xFFFFF9C4), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) { Text("🤖", fontSize = 28.sp) }
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text("Generate with AI", fontWeight = FontWeight.Black, fontSize = 15.sp, color = Color(0xFF1D1B20))
                                        Box(modifier = Modifier.background(Color(0xFFFFD600), RoundedCornerShape(6.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) { Text("FAST", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color.Black) }
                                    }
                                    Text("Automatically create 3–15 fresh, child-safe quiz questions in seconds using AI.", fontSize = 12.sp, color = Color(0xFF6C757D), lineHeight = 18.sp)
                                }
                                Text("›", fontSize = 24.sp, color = Color(0xFF6750A4), fontWeight = FontWeight.Bold)
                            }
                        }
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                            border = BorderStroke(1.dp, Color(0xFFBBDEFB)),
                            modifier = Modifier.fillMaxWidth().clickable { activeMode = 3 }
                        ) {
                            Row(
                                modifier = Modifier.padding(20.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier.size(56.dp).background(Color(0xFFBBDEFB), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) { Text("📂", fontSize = 28.sp) }
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
                                    Text("Import from File", fontWeight = FontWeight.Black, fontSize = 15.sp, color = Color(0xFF1D1B20))
                                    Text("Upload a JSON or CSV file containing a list of questions. Supports bulk importing.", fontSize = 12.sp, color = Color(0xFF6C757D), lineHeight = 18.sp)
                                }
                                Text("›", fontSize = 24.sp, color = Color(0xFF6750A4), fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
                0 -> {
                    var bankFilterSubject by remember { mutableStateOf("All") }
                    val availableSubjects = remember(allQuestions) { listOf("All") + allQuestions.map { it.subject }.distinct().sorted() }
                    
                    val allActiveQs = allQuestions.filter { !it.isArchived }
                    val filteredQs = if (bankFilterSubject == "All") allActiveQs else allActiveQs.filter { it.subject == bankFilterSubject }
                    val customQsList2 = filteredQs.filter { it.isCustom }
                    val systemQsList = filteredQs.filter { !it.isCustom }
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(onClick = { activeMode = -1 }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color(0xFF6750A4))
                            }
                            Text("All Questions", fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color(0xFF1D1B20))
                        }
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(availableSubjects) { sub ->
                                val isSel = bankFilterSubject == sub
                                Box(
                                    modifier = Modifier
                                        .background(color = if (isSel) Color(0xFF6750A4) else Color(0xFFF3EDF7), shape = RoundedCornerShape(16.dp))
                                        .clickable { bankFilterSubject = sub }
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Text(text = sub, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (isSel) Color.White else Color(0xFF1D1B20))
                                }
                            }
                        }
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (customQsList2.isNotEmpty()) {
                                item {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Custom Questions (${customQsList2.size})", fontSize = 15.sp, fontWeight = FontWeight.Black, color = Color(0xFF6750A4))
                                        IconButton(onClick = { showDeleteAllDialog = true }) {
                                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Clear All Custom", tint = Color.Red)
                                        }
                                    }
                                }
                                items(customQsList2) { q ->
                                    QuestionCard(question = q, isCustom = true, onDelete = { viewModel.deleteQuestion(q) })
                                }
                            } else {
                                item {
                                    Card(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3EDF7)),
                                        border = BorderStroke(1.dp, Color(0xFFE5E0E9))
                                    ) {
                                        Column(
                                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Text(text = "✍️", fontSize = 36.sp)
                                            Text("No custom questions yet!", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1D1B20))
                                            Text("Add your first question using one of the options below:", fontSize = 12.sp, color = Color(0xFF6C757D), textAlign = TextAlign.Center)
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                OutlinedButton(
                                                    onClick = { activeMode = 1 },
                                                    shape = RoundedCornerShape(12.dp),
                                                    border = BorderStroke(1.dp, Color(0xFF6750A4))
                                                ) { Text("✍️ Write", fontSize = 12.sp, color = Color(0xFF6750A4)) }
                                                OutlinedButton(
                                                    onClick = { activeMode = 2 },
                                                    shape = RoundedCornerShape(12.dp),
                                                    border = BorderStroke(1.dp, Color(0xFFFFAB00))
                                                ) { Text("🤖 AI", fontSize = 12.sp, color = Color(0xFFFFAB00)) }
                                                OutlinedButton(
                                                    onClick = { activeMode = 3 },
                                                    shape = RoundedCornerShape(12.dp),
                                                    border = BorderStroke(1.dp, Color(0xFF1976D2))
                                                ) { Text("📂 Import", fontSize = 12.sp, color = Color(0xFF1976D2)) }
                                            }
                                        }
                                    }
                                }
                            }
                            item {
                                Text("Prepared Default Questions (${systemQsList.size})", fontSize = 15.sp, fontWeight = FontWeight.Black, color = Color(0xFF6750A4), modifier = Modifier.padding(top = 12.dp, bottom = 4.dp))
                            }
                            items(systemQsList) { q ->
                                QuestionCard(question = q, isCustom = false, onDelete = {})
                            }
                        }
                    }
                }
1 -> {
                    // MANUAL CREATION FORM (Mode 1)
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(onClick = { activeMode = -1 }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = SleekPurple)
                            }
                            Text("Write Your Own Question", fontWeight = FontWeight.Black, fontSize = 16.sp, color = SleekTextDark)
                        }
                        // Dropdown mock subject choice selection
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = "Select Category / Subject:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SleekPurple)
                            val mergedSubjects = (listOf("Math", "English", "General Knowledge", "Science") + viewModel.customSubjects).distinct()
                            
                            val selectedSubjectIsAvailable = mergedSubjects.contains(viewModel.customSubjectInput)
                            if (!selectedSubjectIsAvailable && mergedSubjects.isNotEmpty()) {
                                viewModel.customSubjectInput = mergedSubjects.first()
                            }
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                mergedSubjects.forEach { s ->
                                    val isSel = viewModel.customSubjectInput == s
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                color = if (isSel) SleekPurple else SleekSurfaceVariant,
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .border(1.dp, if (isSel) SleekPurple else SleekBorderLight, RoundedCornerShape(12.dp))
                                            .testTag("subject_choice_$s")
                                            .clickable { viewModel.customSubjectInput = s }
                                            .padding(horizontal = 16.dp, vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = s,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSel) Color.White else SleekTextDark
                                        )
                                    }
                                }
                            }
                        }

                        // Question Input Text Box
                        OutlinedTextField(
                            value = viewModel.customQuestionTextInput,
                            onValueChange = { viewModel.customQuestionTextInput = it },
                            label = { Text("Enter Kid's Question here") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_custom_question"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        // 4 Choices Input fields config
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(text = "Provide 4 Multiple Choice options:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SleekPurple)

                            OutlinedTextField(
                                value = viewModel.customOptionAInput,
                                onValueChange = { viewModel.customOptionAInput = it },
                                placeholder = { Text("Option A") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_option_a"),
                                shape = RoundedCornerShape(12.dp)
                            )

                            OutlinedTextField(
                                value = viewModel.customOptionBInput,
                                onValueChange = { viewModel.customOptionBInput = it },
                                placeholder = { Text("Option B") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_option_b"),
                                shape = RoundedCornerShape(12.dp)
                            )

                            OutlinedTextField(
                                value = viewModel.customOptionCInput,
                                onValueChange = { viewModel.customOptionCInput = it },
                                placeholder = { Text("Option C") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_option_c"),
                                shape = RoundedCornerShape(12.dp)
                            )

                            OutlinedTextField(
                                value = viewModel.customOptionDInput,
                                onValueChange = { viewModel.customOptionDInput = it },
                                placeholder = { Text("Option D") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_option_d"),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        // Selector for the Correct Option (0 to 3)
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = "Choose the correct Option answer index:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SleekPurple)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("A", "B", "C", "D").forEachIndexed { index, letter ->
                                    val isSel = viewModel.customCorrectIndexInput == index
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(
                                                color = if (isSel) Color(0xFF28A745) else SleekSurfaceVariant,
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .border(1.dp, if (isSel) Color(0xFF28A745) else SleekBorderLight, RoundedCornerShape(12.dp))
                                            .testTag("correct_idx_$index")
                                            .clickable { viewModel.customCorrectIndexInput = index }
                                            .padding(vertical = 12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Option $letter",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSel) Color.White else SleekTextDark
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Button(
                            onClick = {
                                viewModel.addCustomQuestion()
                                activeMode = 0 // back to bank
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF28A745)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("save_question_button")
                        ) {
                            Text("Save Custom Question! 💾", fontWeight = FontWeight.Black, fontSize = 16.sp)
                        }

                        Spacer(modifier = Modifier.height(48.dp))
                    }
                }
                2 -> {
                    // AI GENERATION PANEL (Mode 2)
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = SleekSurfaceVariant),
                            border = BorderStroke(1.dp, SleekBorderLight)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "Dynamic AI Generation ✨",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp,
                                    color = SleekPurple
                                )
                                Text(
                                    text = "Ask KidQuiz's automated AI engine to compile fresh, smart, child-safe questions instantly. Verify that you've configured your API keys in the Settings tab.",
                                    fontSize = 12.sp,
                                    color = Color(0xFF49454F),
                                    lineHeight = 18.sp
                                )
                            }
                        }

                        var aiSubject by remember { mutableStateOf("Math") }
                        var aiTopic by remember { mutableStateOf("") }
                        var aiCount by remember { mutableStateOf(5) }
                        var aiLevel by remember { mutableStateOf("Medium") }

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = "Choose Subject to Generate:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SleekPurple)
                            val allConfiguredSubjects = (listOf("Math", "English", "General Knowledge", "Science") + viewModel.customSubjects).distinct()
                            
                            val selectedSubjectIsAvailable = allConfiguredSubjects.contains(aiSubject)
                            if (!selectedSubjectIsAvailable && allConfiguredSubjects.isNotEmpty()) {
                                aiSubject = allConfiguredSubjects.first()
                            }
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                allConfiguredSubjects.forEach { s ->
                                    val isSel = aiSubject == s
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                color = if (isSel) SleekPurple else SleekSurfaceVariant,
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .border(1.dp, if (isSel) SleekPurple else SleekBorderLight, RoundedCornerShape(12.dp))
                                            .testTag("ai_selected_subject_$s")
                                            .clickable { aiSubject = s }
                                            .padding(horizontal = 16.dp, vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = s,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSel) Color.White else SleekTextDark
                                        )
                                    }
                                }
                            }
                        }

                        // Specific Topic Input / Selection
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = "Specific Topic (Optional):", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SleekPurple)
                            
                            val availableTopicsForSubject = viewModel.customCategories
                                .filter { it.startsWith("$aiSubject:") }
                                .map { it.substringAfter(":") }
                                
                            if (availableTopicsForSubject.isNotEmpty()) {
                                LazyRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(availableTopicsForSubject) { tpc ->
                                        val isSel = aiTopic.split(", ").contains(tpc)
                                        Box(
                                            modifier = Modifier
                                                .background(if (isSel) SleekPurple else Color.White, RoundedCornerShape(12.dp))
                                                .border(1.dp, if (isSel) SleekPurple else SleekBorderLight, RoundedCornerShape(12.dp))
                                                .clickable { 
                                                    val currentTopics = aiTopic.split(", ").filter { it.isNotBlank() }.toMutableSet()
                                                    if (isSel) currentTopics.remove(tpc) else currentTopics.add(tpc)
                                                    aiTopic = currentTopics.joinToString(", ")
                                                }
                                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text(text = tpc, fontSize = 13.sp, color = if (isSel) Color.White else SleekTextDark)
                                        }
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = aiTopic,
                                onValueChange = { aiTopic = it },
                                placeholder = { Text("e.g. Fractions, Nouns (comma separated)") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("ai_topic_input"),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.Black,
                                    unfocusedTextColor = Color.Black
                                )
                            )
                        }

                        // 2. Select Question Count
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = "Number of Questions to Generate:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SleekPurple)
                            val countsList = listOf(3, 5, 10, 15)
                            var customCountText by remember { mutableStateOf(if (aiCount !in countsList) aiCount.toString() else "") }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                countsList.forEach { num ->
                                    val isSel = aiCount == num
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(
                                                color = if (isSel) SleekPurple else SleekSurfaceVariant,
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .border(1.dp, if (isSel) SleekPurple else SleekBorderLight, RoundedCornerShape(12.dp))
                                            .testTag("ai_selected_count_$num")
                                            .clickable { 
                                                aiCount = num
                                                customCountText = ""
                                            }
                                            .padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "$num",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSel) Color.White else SleekTextDark
                                        )
                                    }
                                }
                                
                                // Custom count input
                                OutlinedTextField(
                                    value = customCountText,
                                    onValueChange = { 
                                        if (it.all { char -> char.isDigit() } && it.length <= 3) {
                                            customCountText = it
                                            val parsed = it.toIntOrNull()
                                            if (parsed != null && parsed > 0) {
                                                aiCount = parsed
                                            }
                                        }
                                    },
                                    placeholder = { Text("Custom", fontSize = 12.sp) },
                                    modifier = Modifier
                                        .weight(1.2f)
                                        .height(44.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = if (aiCount !in countsList && customCountText.isNotEmpty()) SleekPurple.copy(alpha = 0.1f) else Color.Transparent,
                                        unfocusedContainerColor = if (aiCount !in countsList && customCountText.isNotEmpty()) SleekPurple.copy(alpha = 0.05f) else Color.Transparent
                                    )
                                )
                            }
                        }

                        // 3. Select Difficulty Level
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = "Choose Difficulty Level:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SleekPurple)
                            val levelsList = listOf("Easy", "Medium", "Hard")
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                levelsList.forEach { lvl ->
                                    val isSel = aiLevel == lvl
                                    val levelColor = when (lvl) {
                                        "Easy" -> Color(0xFF28A745)
                                        "Medium" -> Color(0xFFFD7E14)
                                        else -> Color(0xFFDC3545)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(
                                                color = if (isSel) levelColor else SleekSurfaceVariant,
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .border(1.dp, if (isSel) levelColor else SleekBorderLight, RoundedCornerShape(12.dp))
                                            .testTag("ai_selected_level_$lvl")
                                            .clickable { aiLevel = lvl }
                                            .padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = lvl,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSel) Color.White else SleekTextDark
                                        )
                                    }
                                }
                            }
                        }

                        // 4. Target Standard Embedded Variables Summary Row
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFE8F0FE), RoundedCornerShape(12.dp))
                                .border(1.dp, Color(0xFFD2E3FC), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Target Standard Customization:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1A73E8)
                                )
                                Text(
                                    text = "Tailoring quizzes for ${viewModel.childName} (${viewModel.childAge} years old, in ${viewModel.childClass}) at a customized ${aiLevel.uppercase()} level of difficulty.",
                                    fontSize = 11.sp,
                                    color = Color(0xFF3C4043),
                                    lineHeight = 15.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (viewModel.isGeneratingQuestions) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                CircularProgressIndicator(color = SleekPurple)
                                Text(
                                    text = viewModel.aiGenerationStatus,
                                    fontSize = 13.sp,
                                    color = SleekTextDark,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else if (viewModel.pendingAiQuestions.isNotEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFFFF9C4), RoundedCornerShape(12.dp))
                                    .border(1.dp, Color(0xFFFFEB3B), RoundedCornerShape(12.dp))
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Preview Generated Questions (${viewModel.pendingAiQuestions.size}):",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                viewModel.pendingAiQuestions.forEachIndexed { i, q ->
                                    Text(text = "${i + 1}. ${q.questionText}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { viewModel.discardPendingAiQuestions() },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC3545)),
                                        modifier = Modifier.weight(1f)
                                    ) { Text("Discard ❌") }
                                    Button(
                                        onClick = { viewModel.savePendingAiQuestions() },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF28A745)),
                                        modifier = Modifier.weight(1f)
                                    ) { Text("Save ✅") }
                                }
                            }
                        } else {
                            Button(
                                onClick = { viewModel.generateAiQuestions(subject = aiSubject, topic = aiTopic, count = aiCount, level = aiLevel) },
                                colors = ButtonDefaults.buttonColors(containerColor = SleekPurple),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("generate_ai_submit")
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = "Generate $aiCount Qs with ${viewModel.aiProvider.uppercase()} ✨", fontWeight = FontWeight.Black)
                                }
                            }

                            if (viewModel.aiGenerationStatus.isNotEmpty()) {
                                Text(
                                    text = viewModel.aiGenerationStatus,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    color = if (viewModel.aiGenerationStatus.startsWith("Error")) Color.Red else Color(0xFF28A745),
                                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
                                )
                            }
                        }
                    }
                }
                3 -> {
                    // FILE IMPORT PANEL (Mode 3)
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = SleekSurfaceVariant),
                            border = BorderStroke(1.dp, SleekBorderLight)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "Import Questions from File 📤",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp,
                                    color = SleekPurple
                                )
                                Text(
                                    text = "Select a JSON or CSV file from your device storage. The system automatically handles header line detection, quoted fields, and imports them immediately into the database.",
                                    fontSize = 12.sp,
                                    color = Color(0xFF49454F),
                                    lineHeight = 18.sp
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(text = "Example CSV Row:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = SleekPurple)
                                Text(
                                    text = "Math, What represents a dozen?, 6, 12, 10, 24, 1",
                                    fontSize = 11.sp,
                                    color = Color.DarkGray
                                )

                                Text(text = "Example JSON Item:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = SleekPurple)
                                Text(
                                    text = "[{\"subject\":\"Math\",\"questionText\":\"What is 10-4?\",\"optionA\":\"5\",\"optionB\":\"6\",\"optionC\":\"7\",\"optionD\":\"8\",\"correctOptionIndex\":1}]",
                                    fontSize = 11.sp,
                                    color = Color.DarkGray
                                )

                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { sampleFileExportLauncher.launch("sample_questions.json") },
                                    colors = ButtonDefaults.buttonColors(containerColor = SleekBorderDark),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Download Sample JSON 📥", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }

                        if (viewModel.pendingImportQuestions.isNotEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFE8F0FE), RoundedCornerShape(12.dp))
                                    .border(1.dp, Color(0xFFD2E3FC), RoundedCornerShape(12.dp))
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Preview Uploaded Questions (${viewModel.pendingImportQuestions.size}):",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                viewModel.pendingImportQuestions.take(10).forEachIndexed { i, q ->
                                    Text(text = "${i + 1}. [${q.subject}] ${q.questionText}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                                if (viewModel.pendingImportQuestions.size > 10) {
                                    Text(text = "... and ${viewModel.pendingImportQuestions.size - 10} more.", fontSize = 12.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { viewModel.discardPendingImportQuestions() },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC3545)),
                                        modifier = Modifier.weight(1f)
                                    ) { Text("Discard ❌") }
                                    Button(
                                        onClick = { viewModel.savePendingImportQuestions() },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF28A745)),
                                        modifier = Modifier.weight(1f)
                                    ) { Text("Finalize Upload ✅") }
                                }
                            }
                        } else {
                            Button(
                                onClick = { fileImportLauncher.launch("*/*") },
                                colors = ButtonDefaults.buttonColors(containerColor = SleekPurple),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                            .fillMaxWidth()
                                            .height(52.dp)
                                            .testTag("import_file_picker_btn")
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = "Select and Upload CSV or JSON 📂", fontWeight = FontWeight.Black)
                                }
                            }
                        }

                        if (viewModel.fileImportStatus.isNotEmpty()) {
                            Text(
                                text = viewModel.fileImportStatus,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                color = if (viewModel.fileImportStatus.startsWith("Import error")) Color.Red else Color(0xFF28A745),
                                modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@androidx.compose.foundation.layout.ExperimentalLayoutApi
@Composable
fun ParentSubjectsTab(viewModel: QuizViewModel) {
    // -1: Landing Hub, 0: Manage Subjects, 1: Manage Topics
    var activeMode by remember { mutableStateOf(-1) }
    
    androidx.activity.compose.BackHandler(enabled = activeMode != -1) {
        activeMode = -1
    }

    val standardTopicSuggestions = remember {
        mapOf(
            "Math" to listOf("Addition ➕", "Subtraction ➖", "Multiplication ⚔️", "Division ➗", "Fractions 🍰", "Geometry 📏", "Counting 🔢", "Decimals 🪙"),
            "English" to listOf("Nouns 🏷️", "Verbs 🏃", "Adjectives ✨", "Pronouns 👥", "Spelling 🔠", "Opposites 🔄", "Punctuation 💬", "Synonyms 👥"),
            "Science" to listOf("Plants 🌿", "Planets 🪐", "Water Cycle 💧", "Animals 🦁", "Human Body 🫁", "Gravity 🍎", "Matter States 🧪", "Electricity ⚡"),
            "General Knowledge" to listOf("Capitals 🏛️", "Oceans 🌊", "Continents 🗺️", "Landmarks 🗽", "History 📜", "Inventions 💡", "Sports ⚽", "Nature ⛰️")
        )
    }

    val systemSubjects = listOf("Math", "English", "General Knowledge", "Science")
    val totalSubjects = systemSubjects.size + viewModel.customSubjects.size
    val totalTopics = viewModel.customCategories.size

    AnimatedContent(
        targetState = activeMode,
        transitionSpec = { fadeIn(animationSpec = tween(200)) togetherWith fadeOut(animationSpec = tween(200)) },
        label = "subjects_mode"
    ) { mode ->
        when (mode) {
            -1 -> {
                // LANDING HUB
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Curriculum Management 📚",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = SleekPurple,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Card 1: Subjects
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                        border = BorderStroke(1.dp, Color(0xFFBBDEFB)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { activeMode = 0 }
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .background(Color(0xFFBBDEFB), CircleShape),
                                contentAlignment = Alignment.Center
                            ) { Text("📘", fontSize = 28.sp) }
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
                                Text("Manage Subjects", fontWeight = FontWeight.Black, fontSize = 15.sp, color = SleekTextDark)
                                Text("Add or remove custom subjects. Currently active: $totalSubjects", fontSize = 12.sp, color = Color(0xFF6C757D), lineHeight = 18.sp)
                            }
                            Text("›", fontSize = 24.sp, color = SleekPurple, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Card 2: Topics
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9C4)),
                        border = BorderStroke(1.dp, Color(0xFFFFF59D)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { activeMode = 1 }
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .background(Color(0xFFFFF59D), CircleShape),
                                contentAlignment = Alignment.Center
                            ) { Text("💡", fontSize = 28.sp) }
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
                                Text("Manage Topics", fontWeight = FontWeight.Black, fontSize = 15.sp, color = SleekTextDark)
                                Text("Add micro-topics within subjects to keep quizzes focused. Active: $totalTopics", fontSize = 12.sp, color = Color(0xFF6C757D), lineHeight = 18.sp)
                            }
                            Text("›", fontSize = 24.sp, color = SleekPurple, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            0 -> {
                // MANAGE SUBJECTS
                var newSubjectInput by remember { mutableStateOf("") }
                
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(onClick = { activeMode = -1 }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = SleekPurple)
                        }
                        Text("Manage Subjects", fontWeight = FontWeight.Black, fontSize = 18.sp, color = SleekTextDark)
                    }

                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = SleekSurfaceVariant),
                        border = BorderStroke(1.dp, SleekBorderLight)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Create Custom Subject",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = SleekPurple
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedTextField(
                                    value = newSubjectInput,
                                    onValueChange = { newSubjectInput = it },
                                    label = { Text("e.g. Spanish, Coding") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true,
                                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = SleekPurple,
                                        unfocusedBorderColor = SleekBorderLight
                                    ),
                                    trailingIcon = {
                                        if (newSubjectInput.isNotEmpty()) {
                                            IconButton(onClick = { newSubjectInput = "" }) {
                                                Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(20.dp))
                                            }
                                        }
                                    }
                                )
                                Button(
                                    onClick = {
                                        if (newSubjectInput.isNotBlank()) {
                                            viewModel.addSubject(newSubjectInput.trim())
                                            newSubjectInput = ""
                                        }
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = SleekPurple),
                                    modifier = Modifier.height(56.dp)
                                ) {
                                    Text("Add", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // System Subjects
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Default Subjects", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = SleekTextDark, modifier = Modifier.padding(start = 4.dp))
                        
                        androidx.compose.foundation.layout.FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            systemSubjects.forEach { sub ->
                                Box(
                                    modifier = Modifier
                                        .background(Color.White, RoundedCornerShape(24.dp))
                                        .border(1.dp, SleekBorderLight, RoundedCornerShape(24.dp))
                                        .padding(horizontal = 16.dp, vertical = 10.dp)
                                ) {
                                    Text(sub, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.Gray)
                                }
                            }
                        }
                    }

                    // Custom Subjects
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Custom Subjects", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = SleekTextDark, modifier = Modifier.padding(start = 4.dp))
                        
                        if (viewModel.customSubjects.isEmpty()) {
                            Text("No custom subjects added yet.", fontSize = 13.sp, color = Color.Gray, modifier = Modifier.padding(start = 4.dp))
                        } else {
                            androidx.compose.foundation.layout.FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                viewModel.customSubjects.forEach { sub ->
                                    Row(
                                        modifier = Modifier
                                            .background(SleekPurple.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
                                            .border(1.dp, SleekPurple.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                                            .padding(start = 16.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(sub, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = SleekPurple)
                                        IconButton(
                                            onClick = { viewModel.removeSubject(sub) },
                                            modifier = Modifier.size(26.dp).background(Color.White, CircleShape)
                                        ) {
                                            Icon(Icons.Default.Clear, contentDescription = "Remove", modifier = Modifier.size(16.dp), tint = Color(0xFFBA1A1A))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            1 -> {
                // MANAGE TOPICS
                var newCategoryInput by remember { mutableStateOf("") }
                var selectedCatSubject by remember { mutableStateOf("Math") }
                val allSubjectsList = systemSubjects + viewModel.customSubjects

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(onClick = { activeMode = -1 }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = SleekPurple)
                        }
                        Text("Manage Topics", fontWeight = FontWeight.Black, fontSize = 18.sp, color = SleekTextDark)
                    }

                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = SleekSurfaceVariant),
                        border = BorderStroke(1.dp, SleekBorderLight)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Create Topic Category",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = SleekPurple
                            )

                            // Select Subject Scroll Row
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("1. Select Parent Subject", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = SleekTextDark)
                                Row(
                                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    allSubjectsList.forEach { s ->
                                        val isSelected = s == selectedCatSubject
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(if (isSelected) SleekPurple else Color.White)
                                                .border(1.dp, if (isSelected) SleekPurple else SleekBorderLight, RoundedCornerShape(12.dp))
                                                .clickable { selectedCatSubject = s }
                                                .padding(horizontal = 16.dp, vertical = 10.dp)
                                        ) {
                                            Text(
                                                text = s,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) Color.White else SleekTextDark
                                            )
                                        }
                                    }
                                }
                            }

                            // Enter Topic Row
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("2. Enter Topic Name", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = SleekTextDark)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    OutlinedTextField(
                                        value = newCategoryInput,
                                        onValueChange = { newCategoryInput = it },
                                        label = { Text("e.g. Addition") },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                        singleLine = true,
                                        trailingIcon = {
                                            if (newCategoryInput.isNotEmpty()) {
                                                IconButton(onClick = { newCategoryInput = "" }) {
                                                    Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(20.dp))
                                                }
                                            }
                                        }
                                    )
                                    Button(
                                        onClick = {
                                            if (newCategoryInput.isNotBlank()) {
                                                viewModel.addCategory(selectedCatSubject, newCategoryInput.trim())
                                                newCategoryInput = ""
                                            }
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = SleekPurple),
                                        modifier = Modifier.height(56.dp)
                                    ) {
                                        Text("Add", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            
                            // Suggestions
                            standardTopicSuggestions[selectedCatSubject]?.let { suggestions ->
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("Suggestions:", fontSize = 12.sp, color = Color.Gray)
                                    androidx.compose.foundation.layout.FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        suggestions.forEach { sugg ->
                                            Box(
                                                modifier = Modifier
                                                    .background(Color.White, RoundedCornerShape(12.dp))
                                                    .border(1.dp, SleekBorderLight, RoundedCornerShape(12.dp))
                                                    .clickable { newCategoryInput = sugg }
                                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                            ) {
                                                Text(sugg, fontSize = 12.sp, color = SleekPurple, fontWeight = FontWeight.SemiBold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Existing Topics List
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Existing Custom Topics", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = SleekTextDark, modifier = Modifier.padding(start = 4.dp))
                        
                        if (viewModel.customCategories.isEmpty()) {
                            Text("No custom topics added yet.", fontSize = 13.sp, color = Color.Gray, modifier = Modifier.padding(start = 4.dp))
                        } else {
                            viewModel.customCategories.forEach { categoryKey ->
                                val parts = categoryKey.split(":")
                                val sbjLabel = parts.getOrNull(0) ?: ""
                                val tpcLabel = parts.getOrNull(1) ?: ""
                                
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.White, RoundedCornerShape(12.dp))
                                        .border(1.dp, SleekBorderLight, RoundedCornerShape(12.dp))
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = tpcLabel,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 15.sp,
                                            color = SleekTextDark
                                        )
                                        Text(
                                            text = "in $sbjLabel",
                                            fontSize = 12.sp,
                                            color = Color.Gray
                                        )
                                    }
                                    IconButton(
                                        onClick = { viewModel.removeCategory(categoryKey) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete topic",
                                            tint = Color(0xFFBA1A1A),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun ParentArchiveTab(viewModel: QuizViewModel, allQuestions: List<Question>) {
    val archivedQuestions = remember(allQuestions) { allQuestions.filter { it.isArchived } }

    // -1: Landing Hub, 0: Full Archive List, 1: Subject-filtered list
    var activeMode by remember { mutableStateOf(-1) }
    var selectedSubject by remember { mutableStateOf("") }
    var showRestoreAllDialog by remember { mutableStateOf(false) }

    androidx.activity.compose.BackHandler(enabled = activeMode != -1) {
        activeMode = -1
        selectedSubject = ""
    }

    // Restore All confirmation dialog
    if (showRestoreAllDialog) {
        Dialog(onDismissRequest = { showRestoreAllDialog = false }) {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("🔁", fontSize = 48.sp)
                    Text(
                        text = "Restore All Questions?",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = SleekPurple,
                        textAlign = TextAlign.Center
                    )
                    val restoreTarget = if (selectedSubject.isEmpty()) archivedQuestions
                    else archivedQuestions.filter { it.subject == selectedSubject }
                    Text(
                        text = "${restoreTarget.size} questions will be moved back to the active question bank.",
                        fontSize = 13.sp,
                        color = Color(0xFF6C757D),
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showRestoreAllDialog = false },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(2.dp, SleekPurple)
                        ) {
                            Text("Cancel", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SleekPurple)
                        }
                        Button(
                            onClick = {
                                restoreTarget.forEach { viewModel.updateQuestion(it.copy(isArchived = false)) }
                                showRestoreAllDialog = false
                                activeMode = -1
                                selectedSubject = ""
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                        ) {
                            Text("Restore ✓", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }

    val subjectGroups = archivedQuestions.groupBy { it.subject }
    val subjectColors = mapOf(
        "Math" to Pair(Color(0xFFE8F5E9), Color(0xFFA5D6A7)),
        "English" to Pair(Color(0xFFFFF9C4), Color(0xFFFFF176)),
        "Science" to Pair(Color(0xFFFCE4EC), Color(0xFFF48FB1)),
        "General Knowledge" to Pair(Color(0xFFF3E5F5), Color(0xFFCE93D8))
    )
    val subjectEmojis = mapOf(
        "Math" to "🧮", "English" to "📝", "Science" to "🧪", "General Knowledge" to "🌍"
    )

    AnimatedContent(
        targetState = activeMode,
        transitionSpec = { fadeIn(animationSpec = tween(200)) togetherWith fadeOut(animationSpec = tween(200)) },
        label = "archive_mode"
    ) { mode ->
        when (mode) {
            -1 -> {
                // LANDING HUB
                if (archivedQuestions.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("🗄️", fontSize = 56.sp)
                            Text(
                                text = "Archive is Empty!",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = SleekPurple
                            )
                            Text(
                                text = "When your child completes quizzes, those questions will be archived here. You can restore them anytime.",
                                fontSize = 13.sp,
                                color = Color(0xFF6C757D),
                                lineHeight = 20.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Archived Questions 🗄️",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = SleekPurple,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        // Summary stats
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5)),
                                border = BorderStroke(1.dp, Color(0xFFCE93D8))
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text("📦", fontSize = 24.sp)
                                    Text("${archivedQuestions.size}", fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color(0xFF6A1B9A))
                                    Text("Archived", fontSize = 11.sp, color = Color(0xFF8E24AA), fontWeight = FontWeight.Bold)
                                }
                            }
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                                border = BorderStroke(1.dp, Color(0xFFA5D6A7))
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text("📚", fontSize = 24.sp)
                                    Text("${subjectGroups.size}", fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color(0xFF2E7D32))
                                    Text("Subjects", fontSize = 11.sp, color = Color(0xFF388E3C), fontWeight = FontWeight.Bold)
                                }
                            }
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9C4)),
                                border = BorderStroke(1.dp, Color(0xFFFFF176))
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text("🔁", fontSize = 24.sp)
                                    Text("All", fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color(0xFFF9A825))
                                    Text("Restorable", fontSize = 11.sp, color = Color(0xFFF57F17), fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Full Archive card
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                            border = BorderStroke(1.dp, Color(0xFFBBDEFB)),
                            modifier = Modifier.fillMaxWidth().clickable { activeMode = 0 }
                        ) {
                            Row(
                                modifier = Modifier.padding(20.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier.size(56.dp).background(Color(0xFFBBDEFB), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) { Text("📋", fontSize = 28.sp) }
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
                                    Text("Browse All Archived", fontWeight = FontWeight.Black, fontSize = 15.sp, color = SleekTextDark)
                                    Text("View and restore all ${archivedQuestions.size} archived questions", fontSize = 12.sp, color = Color(0xFF6C757D), lineHeight = 18.sp)
                                }
                                Text("›", fontSize = 24.sp, color = SleekPurple, fontWeight = FontWeight.Bold)
                            }
                        }

                        // By Subject section
                        Text(
                            text = "By Subject",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = SleekPurple,
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        subjectGroups.entries.forEach { (subject, qs) ->
                            val colors = subjectColors[subject] ?: Pair(Color(0xFFF5F5F5), Color(0xFFBDBDBD))
                            val emoji = subjectEmojis[subject] ?: "🌟"
                            Card(
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = colors.first),
                                border = BorderStroke(1.dp, colors.second),
                                modifier = Modifier.fillMaxWidth().clickable {
                                    selectedSubject = subject
                                    activeMode = 1
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(20.dp),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier.size(56.dp).background(colors.second, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) { Text(emoji, fontSize = 28.sp) }
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
                                        Text(subject, fontWeight = FontWeight.Black, fontSize = 15.sp, color = SleekTextDark)
                                        Text("${qs.size} question${if (qs.size != 1) "s" else ""} archived", fontSize = 12.sp, color = Color(0xFF6C757D), lineHeight = 18.sp)
                                    }
                                    Text("›", fontSize = 24.sp, color = SleekPurple, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
            0 -> {
                // FULL ARCHIVE LIST
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconButton(
                                onClick = { activeMode = -1 },
                                modifier = Modifier.size(36.dp).background(SleekSurfaceVariant, CircleShape)
                            ) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = SleekPurple, modifier = Modifier.size(18.dp))
                            }
                            Text("All Archived (${archivedQuestions.size})", fontWeight = FontWeight.Black, fontSize = 16.sp, color = SleekPurple)
                        }
                        TextButton(onClick = { showRestoreAllDialog = true }) {
                            Text("Restore All 🔁", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                        }
                    }
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(archivedQuestions) { q ->
                            ArchiveQuestionCard(q = q, onRestore = { viewModel.updateQuestion(q.copy(isArchived = false)) })
                        }
                    }
                }
            }
            1 -> {
                // SUBJECT-FILTERED LIST
                val filtered = archivedQuestions.filter { it.subject == selectedSubject }
                val emoji = subjectEmojis[selectedSubject] ?: "🌟"
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconButton(
                                onClick = { activeMode = -1; selectedSubject = "" },
                                modifier = Modifier.size(36.dp).background(SleekSurfaceVariant, CircleShape)
                            ) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = SleekPurple, modifier = Modifier.size(18.dp))
                            }
                            Text("$emoji $selectedSubject (${filtered.size})", fontWeight = FontWeight.Black, fontSize = 16.sp, color = SleekPurple)
                        }
                        if (filtered.isNotEmpty()) {
                            TextButton(onClick = { showRestoreAllDialog = true }) {
                                Text("Restore All 🔁", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                            }
                        }
                    }
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filtered) { q ->
                            ArchiveQuestionCard(q = q, onRestore = { viewModel.updateQuestion(q.copy(isArchived = false)) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ArchiveQuestionCard(q: Question, onRestore: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val subjectEmoji = when (q.subject) {
        "Math" -> "🧮"; "English" -> "📝"; "General Knowledge" -> "🌍"; "Science" -> "🧪"; else -> "🌟"
    }
    val subjectBg = when (q.subject) {
        "Math" -> Color(0xFFE8F5E9); "English" -> Color(0xFFFFF9C4)
        "Science" -> Color(0xFFFCE4EC); "General Knowledge" -> Color(0xFFF3E5F5)
        else -> Color(0xFFF5F5F5)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SleekSurfaceVariant),
        border = BorderStroke(1.dp, SleekBorderLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(subjectBg, CircleShape),
                    contentAlignment = Alignment.Center
                ) { Text(subjectEmoji, fontSize = 20.sp) }

                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = q.subject,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SleekPurple
                    )
                    Text(
                        text = q.questionText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextDark,
                        maxLines = if (expanded) Int.MAX_VALUE else 2,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (expanded) "▲ Hide answers" else "▼ Show answers",
                        fontSize = 10.sp,
                        color = SleekPurple,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = onRestore,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Restore ✓", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            if (expanded) {
                HorizontalDivider(color = SleekBorderLight, thickness = 1.dp)
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val opts = listOf(q.optionA, q.optionB, q.optionC, q.optionD)
                    opts.forEachIndexed { i, opt ->
                        if (opt.isNotBlank()) {
                            val isCorrect = i == q.correctOptionIndex
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        if (isCorrect) Color(0xFFF1F8F1) else Color.Transparent,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .then(
                                        if (isCorrect) Modifier.border(1.dp, Color(0xFFD4EDDA), RoundedCornerShape(8.dp))
                                        else Modifier
                                    )
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isCorrect) "✅" else "${('A'.code + i).toChar()}.",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCorrect) Color(0xFF2E7D32) else Color(0xFF6C757D)
                                )
                                Text(
                                    text = opt,
                                    fontSize = 13.sp,
                                    fontWeight = if (isCorrect) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isCorrect) Color(0xFF2E7D32) else SleekTextDark
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}



@Composable
fun QuestionCard(question: Question, isCustom: Boolean, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SleekSurfaceVariant),
        border = BorderStroke(1.dp, SleekBorderLight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Tag
                Box(
                    modifier = Modifier
                        .background(SleekSurfaceVariant, RoundedCornerShape(8.dp))
                        .border(1.dp, SleekBorderDark, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = question.subject,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SleekPurple
                    )
                }

                if (isCustom) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete custom question",
                            tint = Color(0xFFBA1A1A),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Text(
                text = question.questionText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = SleekPurple
            )

            // Reveal choices summary
            Column(verticalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.padding(start = 4.dp)) {
                val opts = listOf(question.optionA, question.optionB, question.optionC, question.optionD)
                opts.forEachIndexed { i, opt ->
                    if (opt.isNotBlank()) {
                        val isCorrect = i == question.correctOptionIndex
                        Text(
                            text = "${('A'.code + i).toChar()}. $opt" + if (isCorrect) " (Correct Answer ✓)" else "",
                            fontSize = 12.sp,
                            fontWeight = if (isCorrect) FontWeight.Bold else FontWeight.Normal,
                            color = if (isCorrect) Color(0xFF28A745) else Color.DarkGray
                        )
                    }
                }
            }
        }
    }
}

@androidx.compose.foundation.layout.ExperimentalLayoutApi
@Composable
fun ParentSettingsTab(viewModel: QuizViewModel) {
    // -1: Landing Hub, 0: Child Profile, 1: Quiz Preferences, 2: Security & Data, 3: About
    var activeMode by remember { mutableStateOf(-1) }

    androidx.activity.compose.BackHandler(enabled = activeMode != -1) {
        activeMode = -1
    }

    val context = androidx.compose.ui.platform.LocalContext.current
    val dailyGoal = viewModel.dailyQuizGoal
    var editingPinInput by remember { mutableStateOf("") }
    var pinMessageSuccess by remember { mutableStateOf(false) }

    // Child Profile interactive inputs
    var childNameInput by remember { mutableStateOf(viewModel.childName) }
    var childAgeInput by remember { mutableStateOf(viewModel.childAge) }
    var childClassInput by remember { mutableStateOf(viewModel.childClass) }
    var saveProfileSuccess by remember { mutableStateOf(false) }

    // Launcher for gallery image picking and cropping
    val childPhotoLauncher = rememberLauncherForActivityResult(
        contract = com.canhub.cropper.CropImageContract()
    ) { result ->
        if (result.isSuccessful) {
            val uri = result.uriContent
            if (uri != null) {
                try {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val file = java.io.File(context.filesDir, "child_photo.jpg")
                    val outputStream = java.io.FileOutputStream(file)
                    inputStream?.copyTo(outputStream)
                    inputStream?.close()
                    outputStream.close()
                    viewModel.updateChildProfile(
                        name = childNameInput,
                        photoUri = file.absolutePath,
                        age = childAgeInput,
                        className = childClassInput
                    )
                    saveProfileSuccess = true
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    val questionsList by viewModel.allQuestions.collectAsState()
    val defaultQuestionsCount = questionsList.count { !it.isCustom }

    AnimatedContent(
        targetState = activeMode,
        transitionSpec = { fadeIn(animationSpec = tween(200)) togetherWith fadeOut(animationSpec = tween(200)) },
        label = "settings_mode"
    ) { mode ->
        when (mode) {
            -1 -> {
                // LANDING HUB
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "App Settings & Preferences ⚙️",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = SleekPurple,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    // Action card 1: Child Profile
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                        border = BorderStroke(1.dp, Color(0xFFC8E6C9)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { activeMode = 0 }
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .background(Color(0xFFC8E6C9), CircleShape),
                                contentAlignment = Alignment.Center
                            ) { Text("🎒", fontSize = 28.sp) }
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
                                Text("Child Profile", fontWeight = FontWeight.Black, fontSize = 15.sp, color = SleekTextDark)
                                Text("Update name, age, class, and set a profile photo.", fontSize = 12.sp, color = Color(0xFF6C757D), lineHeight = 18.sp)
                            }
                            Text("›", fontSize = 24.sp, color = SleekPurple, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Action card 2: Quiz Preferences
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                        border = BorderStroke(1.dp, Color(0xFFFFE0B2)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { activeMode = 1 }
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .background(Color(0xFFFFE0B2), CircleShape),
                                contentAlignment = Alignment.Center
                            ) { Text("🎯", fontSize = 28.sp) }
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
                                Text("Quiz Preferences", fontWeight = FontWeight.Black, fontSize = 15.sp, color = SleekTextDark)
                                Text("Configure daily goals, questions per quiz, and custom timers.", fontSize = 12.sp, color = Color(0xFF6C757D), lineHeight = 18.sp)
                            }
                            Text("›", fontSize = 24.sp, color = SleekPurple, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Action card 3: Security & Data
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                        border = BorderStroke(1.dp, Color(0xFFFFCDD2)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { activeMode = 2 }
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .background(Color(0xFFFFCDD2), CircleShape),
                                contentAlignment = Alignment.Center
                            ) { Text("🔒", fontSize = 28.sp) }
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
                                Text("Security & Data", fontWeight = FontWeight.Black, fontSize = 15.sp, color = SleekTextDark)
                                Text("Change the admin PIN or manage the default questions database.", fontSize = 12.sp, color = Color(0xFF6C757D), lineHeight = 18.sp)
                            }
                            Text("›", fontSize = 24.sp, color = SleekPurple, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Action card 4: About
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5)),
                        border = BorderStroke(1.dp, Color(0xFFE1BEE7)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { activeMode = 3 }
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .background(Color(0xFFE1BEE7), CircleShape),
                                contentAlignment = Alignment.Center
                            ) { Text("ℹ️", fontSize = 28.sp) }
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
                                Text("About & Credits", fontWeight = FontWeight.Black, fontSize = 15.sp, color = SleekTextDark)
                                Text("App version and creator info.", fontSize = 12.sp, color = Color(0xFF6C757D), lineHeight = 18.sp)
                            }
                            Text("›", fontSize = 24.sp, color = SleekPurple, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            0 -> {
                // CHILD PROFILE
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(onClick = { activeMode = -1 }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = SleekPurple)
                        }
                        Text("Child Profile", fontWeight = FontWeight.Black, fontSize = 18.sp, color = SleekTextDark)
                    }

                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = SleekSurfaceVariant),
                        border = BorderStroke(1.dp, SleekBorderLight)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(70.dp)
                                        .background(Color.White, CircleShape)
                                        .border(3.dp, SleekPurple, CircleShape)
                                        .clip(CircleShape)
                                        .clickable {
                                            childPhotoLauncher.launch(
                                                com.canhub.cropper.CropImageContractOptions(
                                                    uri = null,
                                                    cropImageOptions = com.canhub.cropper.CropImageOptions(
                                                        imageSourceIncludeGallery = true,
                                                        imageSourceIncludeCamera = false,
                                                        aspectRatioX = 1,
                                                        aspectRatioY = 1,
                                                        fixAspectRatio = true
                                                    )
                                                )
                                            )
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (viewModel.childPhotoUri.isNotEmpty()) {
                                        AsyncImage(
                                            model = viewModel.childPhotoUri,
                                            contentDescription = "Child Photo",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                        )
                                    } else {
                                        Text(text = "📸", fontSize = 28.sp)
                                    }
                                }

                                Button(
                                    onClick = {
                                        childPhotoLauncher.launch(
                                            com.canhub.cropper.CropImageContractOptions(
                                                uri = null,
                                                cropImageOptions = com.canhub.cropper.CropImageOptions(
                                                    imageSourceIncludeGallery = true,
                                                    imageSourceIncludeCamera = false,
                                                    aspectRatioX = 1,
                                                    aspectRatioY = 1,
                                                    fixAspectRatio = true
                                                )
                                            )
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = SleekPurple),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.testTag("upload_child_photo")
                                ) {
                                    Text(text = "Pick Photo 🖼️", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }

                            OutlinedTextField(
                                value = childNameInput,
                                onValueChange = {
                                    childNameInput = it
                                    saveProfileSuccess = false
                                },
                                label = { Text("Enter Child's Name") },
                                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.Black,
                                    unfocusedTextColor = Color.Black
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("settings_child_name"),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedTextField(
                                    value = childAgeInput,
                                    onValueChange = {
                                        childAgeInput = it
                                        saveProfileSuccess = false
                                    },
                                    label = { Text("Age (e.g. 8)") },
                                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.Black,
                                        unfocusedTextColor = Color.Black
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("settings_child_age"),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true
                                )

                                OutlinedTextField(
                                    value = childClassInput,
                                    onValueChange = {
                                        childClassInput = it
                                        saveProfileSuccess = false
                                    },
                                    label = { Text("Grade / Class") },
                                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.Black,
                                        unfocusedTextColor = Color.Black
                                    ),
                                    modifier = Modifier
                                        .weight(1.5f)
                                        .testTag("settings_child_class"),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true
                                )
                            }

                            Button(
                                onClick = {
                                    if (childNameInput.isNotBlank()) {
                                        viewModel.updateChildProfile(
                                            name = childNameInput,
                                            photoUri = viewModel.childPhotoUri,
                                            age = childAgeInput,
                                            className = childClassInput
                                        )
                                        saveProfileSuccess = true
                                    }
                                },
                                enabled = childNameInput.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SleekPurple,
                                    disabledContainerColor = Color(0xFFE7E0EC)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("save_profile_button")
                            ) {
                                Text(text = "Save Child Profile 💾", fontWeight = FontWeight.Bold, color = if (childNameInput.isNotBlank()) Color.White else Color(0xFFCAC4D0))
                            }

                            if (saveProfileSuccess) {
                                Text(
                                    text = "✓ Child settings successfully saved!",
                                    color = Color(0xFF28A745),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
            1 -> {
                // QUIZ PREFERENCES
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(onClick = { activeMode = -1 }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = SleekPurple)
                        }
                        Text("Quiz Preferences", fontWeight = FontWeight.Black, fontSize = 18.sp, color = SleekTextDark)
                    }
                    
                    // Daily Quizzes Goals Setting Section
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = SleekSurfaceVariant),
                        border = BorderStroke(1.dp, SleekBorderLight)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Daily Goals Configuration",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = SleekPurple
                            )
            
                            Text(
                                 text = "A goal sets how many quizzes your child should complete every day. The current goal is set to $dailyGoal quizzes.",
                                 fontSize = 13.sp,
                                 color = Color(0xFF49454F),
                                 lineHeight = 18.sp
                            )
            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                for (i in 1..5) {
                                    val active = dailyGoal == i
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(
                                                color = if (active) SleekPurple else SleekSurfaceVariant,
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .border(1.dp, if (active) SleekPurple else SleekBorderLight, RoundedCornerShape(12.dp))
                                            .testTag("set_goal_$i")
                                            .clickable {
                                                viewModel.updateDailyGoal(i)
                                            }
                                            .padding(vertical = 12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "$i",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (active) Color.White else SleekTextDark
                                        )
                                    }
                                }
                            }
                        }
                    }
            
                    // Questions Per Quiz Section
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = SleekSurfaceVariant),
                        border = BorderStroke(1.dp, SleekBorderLight)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Questions per Quiz Session",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = SleekPurple
                            )
                            Text(
                                text = "Set the default number of questions your child will attempt per quiz session. Currently: ${viewModel.questionsPerQuiz} questions.",
                                fontSize = 13.sp,
                                color = Color(0xFF49454F),
                                lineHeight = 18.sp
                            )
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val qOptions = listOf(3, 5, 10, 15)
                                qOptions.forEach { count ->
                                    val active = viewModel.questionsPerQuiz == count
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(
                                                color = if (active) SleekPurple else SleekSurfaceVariant,
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .border(1.dp, if (active) SleekPurple else SleekBorderLight, RoundedCornerShape(12.dp))
                                            .clickable { viewModel.updateQuestionsPerQuiz(count) }
                                            .padding(vertical = 12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "$count",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (active) Color.White else SleekTextDark
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Daily Streak Reminder Section
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = SleekSurfaceVariant),
                        border = BorderStroke(1.dp, SleekBorderLight)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            val settingsContext = LocalContext.current
                            var reminderOn by remember { mutableStateOf(viewModel.preferences.reminderEnabled) }
                            var showPermissionHint by remember { mutableStateOf(false) }

                            val notifPermissionLauncher = rememberLauncherForActivityResult(
                                ActivityResultContracts.RequestPermission()
                            ) { granted ->
                                if (granted) {
                                    reminderOn = true
                                    viewModel.preferences.reminderEnabled = true
                                    com.example.notifications.ReminderScheduler.scheduleNext(settingsContext)
                                    showPermissionHint = false
                                } else {
                                    reminderOn = false
                                    viewModel.preferences.reminderEnabled = false
                                    showPermissionHint = true
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Daily Streak Reminder 🔥",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Black,
                                        color = SleekPurple
                                    )
                                    Text(
                                        text = "A friendly reminder around 6:30 PM if the daily quiz hasn't been done yet.",
                                        fontSize = 13.sp,
                                        color = Color(0xFF49454F),
                                        lineHeight = 18.sp
                                    )
                                }
                                Switch(
                                    checked = reminderOn,
                                    onCheckedChange = { wantOn ->
                                        if (wantOn) {
                                            if (android.os.Build.VERSION.SDK_INT >= 33) {
                                                notifPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                                            } else {
                                                reminderOn = true
                                                viewModel.preferences.reminderEnabled = true
                                                com.example.notifications.ReminderScheduler.scheduleNext(settingsContext)
                                            }
                                        } else {
                                            reminderOn = false
                                            viewModel.preferences.reminderEnabled = false
                                            com.example.notifications.ReminderScheduler.cancel(settingsContext)
                                        }
                                    }
                                )
                            }
                            if (showPermissionHint) {
                                Text(
                                    text = "Notifications are blocked. Enable them for KidQuiz in your phone's Settings to use reminders.",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFFBA1A1A),
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }

                    // Subject Custom Timers Section
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = SleekSurfaceVariant),
                        border = BorderStroke(1.dp, SleekBorderLight)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Custom Timer per Subject ⏱️",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = SleekPurple
                            )
                            Text(
                                text = "Configure minutes allowed per quiz by subject. 0 means no timer.",
                                fontSize = 13.sp,
                                color = Color(0xFF49454F),
                                lineHeight = 18.sp
                            )
                            
                            val timerSubjects = (listOf("Math", "English", "General Knowledge", "Science") + viewModel.customSubjects).distinct()
                            
                            timerSubjects.forEach { s ->
                                val currentMins = viewModel.subjectTimers[s] ?: 0
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.White, RoundedCornerShape(12.dp))
                                        .border(1.dp, SleekBorderLight, RoundedCornerShape(12.dp))
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = s,
                                        fontWeight = FontWeight.Bold,
                                        color = SleekTextDark,
                                        fontSize = 14.sp
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                        IconButton(
                                            onClick = { viewModel.updateSubjectTimer(s, (currentMins - 1).coerceAtLeast(0)) },
                                            modifier = Modifier.size(28.dp).background(SleekSurfaceVariant, CircleShape)
                                        ) {
                                            Text("-", fontWeight = FontWeight.Black, color = SleekPurple)
                                        }
                                        Text(
                                            text = if (currentMins == 0) "Off" else "$currentMins min",
                                            fontWeight = FontWeight.Bold,
                                            color = SleekPurple,
                                            fontSize = 14.sp,
                                            modifier = Modifier.width(50.dp),
                                            textAlign = TextAlign.Center
                                        )
                                        IconButton(
                                            onClick = { viewModel.updateSubjectTimer(s, currentMins + 1) },
                                            modifier = Modifier.size(28.dp).background(SleekSurfaceVariant, CircleShape)
                                        ) {
                                            Text("+", fontWeight = FontWeight.Black, color = SleekPurple)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            2 -> {
                // SECURITY & DATA
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(onClick = { activeMode = -1 }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = SleekPurple)
                        }
                        Text("Security & Data", fontWeight = FontWeight.Black, fontSize = 18.sp, color = SleekTextDark)
                    }
                    
                    // Parent PIN Password Setting Section
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = SleekSurfaceVariant),
                        border = BorderStroke(1.dp, SleekBorderLight)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Update Security Pass PIN",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = SleekPurple
                            )
            
                            Text(
                                text = "Change the 4-digit PIN used to access these parents admin settings to prevent children from unlocking editing screens.",
                                fontSize = 13.sp,
                                color = Color(0xFF49454F),
                                lineHeight = 18.sp
                            )
            
                            OutlinedTextField(
                                value = editingPinInput,
                                onValueChange = { input ->
                                    if (input.length <= 4 && input.all { it.isDigit() }) {
                                        editingPinInput = input
                                        pinMessageSuccess = false
                                    }
                                },
                                label = { Text("Enter New 4-digit PIN") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("settings_new_pin"),
                                shape = RoundedCornerShape(12.dp)
                            )
            
                            Button(
                                onClick = {
                                    if (editingPinInput.length == 4) {
                                        viewModel.changeParentPin(editingPinInput)
                                        editingPinInput = ""
                                        pinMessageSuccess = true
                                    }
                                },
                                enabled = editingPinInput.length == 4,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SleekPurple,
                                    disabledContainerColor = Color(0xFFE7E0EC)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("save_pin_button")
                            ) {
                                Text(text = "Change PIN Code", fontWeight = FontWeight.Bold, color = if (editingPinInput.length == 4) Color.White else Color(0xFFCAC4D0))
                            }
            
                            if (pinMessageSuccess) {
                                Text(
                                    text = "✓ PIN passcode successfully updated!",
                                    color = Color(0xFF28A745),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
            
                    // Question Database Management Section Card
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = SleekSurfaceVariant),
                        border = BorderStroke(1.dp, SleekBorderLight)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Question Database Management 🛠️",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = SleekPurple
                            )
            
                            Text(
                                text = "Clean up the database of pre-packaged default questions to allow only your custom questions, or re-populate them anytime.",
                                fontSize = 13.sp,
                                color = Color(0xFF49454F),
                                lineHeight = 18.sp
                            )
            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.clearDefaultQuestions(questionsList) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBA1A1A)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f),
                                    enabled = defaultQuestionsCount > 0
                                ) {
                                    Text(
                                        text = "Clear Defaults (${defaultQuestionsCount}) 🗑️",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = Color.White
                                    )
                                }
                                
                                Button(
                                    onClick = { viewModel.restoreDefaultQuestions() },
                                    colors = ButtonDefaults.buttonColors(containerColor = SleekPurple),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f),
                                    enabled = viewModel.preferences.defaultQuestionsCleared
                                ) {
                                    Text(
                                        text = "Restore Defaults 🔁",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                            
                            if (viewModel.preferences.defaultQuestionsCleared) {
                                Text(
                                    text = "✓ Default questions have been cleared from the active app roster.",
                                    color = Color(0xFF6750A4),
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
            3 -> {
                // ABOUT
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(onClick = { activeMode = -1 }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = SleekPurple)
                        }
                        Text("About & Credits", fontWeight = FontWeight.Black, fontSize = 18.sp, color = SleekTextDark)
                    }

                    // About / Credits Section
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = SleekSurfaceVariant),
                        border = BorderStroke(1.dp, SleekBorderLight)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = "KidQuiz App", fontWeight = FontWeight.Black, fontSize = 18.sp, color = SleekPurple)
                                Text(text = "Version 1.0.0", fontSize = 12.sp, color = Color.Gray)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = "Created by Pulkit Goel", fontSize = 12.sp, color = SleekTextDark, fontWeight = FontWeight.SemiBold)
                            }
                            Text(text = "🎉", fontSize = 48.sp)
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun KidsAnimatedBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "background_anims")
    
    val colorShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradient_color_shift"
    )
    
    val color1 = androidx.compose.ui.graphics.lerp(Color(0xFFFFF9C4), Color(0xFFFFE0B2), colorShift)
    val color2 = androidx.compose.ui.graphics.lerp(Color(0xFFF3E5F5), Color(0xFFE1BEE7), colorShift)
    val color3 = androidx.compose.ui.graphics.lerp(Color(0xFFE0F7FA), Color(0xFFB2EBF2), colorShift)
    
    val cloudOffset by infiniteTransition.animateFloat(
        initialValue = -100f,
        targetValue = 600f,
        animationSpec = infiniteRepeatable(
            animation = tween(25000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "cloud_offset"
    )

    val starScale1 by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(animation = tween(1800, easing = LinearEasing), repeatMode = RepeatMode.Reverse),
        label = "star_scale_1"
    )
    val starScale2 by infiniteTransition.animateFloat(
        initialValue = 1.2f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(animation = tween(2200, easing = LinearEasing), repeatMode = RepeatMode.Reverse),
        label = "star_scale_2"
    )
    val starRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(animation = tween(10000, easing = LinearEasing), repeatMode = RepeatMode.Restart),
        label = "star_rotation"
    )

    val bubbleProgress1 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(animation = tween(8000, easing = LinearEasing), repeatMode = RepeatMode.Restart),
        label = "bubble_progress_1"
    )
    val bubbleProgress2 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(animation = tween(11000, easing = LinearEasing), repeatMode = RepeatMode.Restart),
        label = "bubble_progress_2"
    )
    val bubbleProgress3 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(animation = tween(6500, easing = LinearEasing), repeatMode = RepeatMode.Restart),
        label = "bubble_progress_3"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(color1, color2, color3)
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            
            drawBubble(
                centerX = width * 0.15f + (Math.sin(bubbleProgress1.toDouble() * 10.0) * 30.dp.toPx()).toFloat(),
                centerY = height * bubbleProgress1,
                radius = 16.dp.toPx(),
                color = Color(0xFF80DEEA).copy(alpha = 0.25f)
            )
            drawBubble(
                centerX = width * 0.85f + (Math.cos(bubbleProgress2.toDouble() * 8.0) * 40.dp.toPx()).toFloat(),
                centerY = height * bubbleProgress2,
                radius = 24.dp.toPx(),
                color = Color(0xFFFFB7B2).copy(alpha = 0.2f)
            )
            drawBubble(
                centerX = width * 0.5f + (Math.sin(bubbleProgress3.toDouble() * 12.0) * 20.dp.toPx()).toFloat(),
                centerY = height * bubbleProgress3,
                radius = 12.dp.toPx(),
                color = Color(0xFFC1F4C5).copy(alpha = 0.22f)
            )

            drawStarAt(
                x = width * 0.25f,
                y = height * 0.15f,
                scale = starScale1,
                rotationDegrees = starRotation,
                color = Color(0xFFFFF59D).copy(alpha = 0.6f)
            )
            drawStarAt(
                x = width * 0.78f,
                y = height * 0.35f,
                scale = starScale2,
                rotationDegrees = -starRotation,
                color = Color(0xFFFFE082).copy(alpha = 0.7f)
            )
            drawStarAt(
                x = width * 0.1f,
                y = height * 0.75f,
                scale = starScale2 * 0.9f,
                rotationDegrees = starRotation * 1.5f,
                color = Color(0xFFFFD54F).copy(alpha = 0.5f)
            )

            drawCloudAt(x = (cloudOffset.dp.toPx()) % (width + 200.dp.toPx()) - 100.dp.toPx(), y = height * 0.08f, alpha = 0.35f)
            drawCloudAt(x = ((cloudOffset * 0.7f).dp.toPx()) % (width + 300.dp.toPx()) - 150.dp.toPx() + width * 0.5f, y = height * 0.25f, alpha = 0.25f)
        }
    }
}

fun androidx.compose.ui.graphics.drawscope.DrawScope.drawBubble(centerX: Float, centerY: Float, radius: Float, color: Color) {
    drawCircle(
        color = color,
        radius = radius,
        center = Offset(centerX, centerY)
    )
    drawCircle(
        color = color.copy(alpha = color.alpha * 1.5f),
        radius = radius,
        center = Offset(centerX, centerY),
        style = Stroke(width = 1.5f.dp.toPx())
    )
    drawCircle(
        color = Color.White.copy(alpha = 0.4f),
        radius = radius * 0.25f,
        center = Offset(centerX - radius * 0.35f, centerY - radius * 0.35f)
    )
}

fun androidx.compose.ui.graphics.drawscope.DrawScope.drawStarAt(x: Float, y: Float, scale: Float, rotationDegrees: Float, color: Color) {
    drawIntoCanvas { canvas ->
        canvas.save()
        canvas.translate(x, y)
        canvas.scale(scale, scale)
        canvas.rotate(rotationDegrees)
        
        val size = 30.dp.toPx()
        val path = Path()
        val centerX = 0f
        val centerY = 0f
        val outerRadius = size / 2
        val innerRadius = outerRadius * 0.4f
        
        for (i in 0 until 10) {
            val angle = Math.toRadians((i * 36 - 90).toDouble())
            val r = if (i % 2 == 0) outerRadius else innerRadius
            val px = centerX + (r * Math.cos(angle)).toFloat()
            val py = centerY + (r * Math.sin(angle)).toFloat()
            if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
        }
        path.close()
        drawPath(path = path, color = color)
        
        canvas.restore()
    }
}

fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCloudAt(x: Float, y: Float, alpha: Float) {
    val cloudColor = Color.White.copy(alpha = alpha)
    val r = 20.dp.toPx()
    drawCircle(color = cloudColor, radius = r, center = Offset(x, y))
    drawCircle(color = cloudColor, radius = r * 1.4f, center = Offset(x + r * 1.1f, y - r * 0.3f))
    drawCircle(color = cloudColor, radius = r * 1.1f, center = Offset(x + r * 2.2f, y))
    drawCircle(color = cloudColor, radius = r * 0.8f, center = Offset(x - r * 0.8f, y + r * 0.2f))
    
    drawRoundRect(
        color = cloudColor,
        topLeft = Offset(x - r * 1.2f, y - r * 0.1f),
        size = Size(r * 3.8f, r * 1.2f),
        cornerRadius = CornerRadius(r, r)
    )
}



