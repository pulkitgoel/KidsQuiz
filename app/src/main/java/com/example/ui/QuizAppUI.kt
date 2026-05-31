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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    val currentScreen = viewModel.currentScreen

    androidx.activity.compose.BackHandler(enabled = currentScreen !is Screen.Home) {
        if (currentScreen is Screen.ParentDashboard || currentScreen is Screen.ParentEnterPin || currentScreen is Screen.QuizResult || currentScreen is Screen.QuizSession || currentScreen is Screen.NoQuestions) {
            viewModel.navigateTo(Screen.Home)
        }
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SleekBgLight)
    ) {
        // Playful background decoration (floating bubble shapes matching the Sleek brand)
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = SleekPurple.copy(alpha = 0.04f),
                radius = 180.dp.toPx(),
                center = Offset(size.width * 0.1f, size.height * 0.15f)
            )
            drawCircle(
                color = SleekPurple.copy(alpha = 0.05f),
                radius = 220.dp.toPx(),
                center = Offset(size.width * 0.85f, size.height * 0.75f)
            )
            drawCircle(
                color = SleekPurple.copy(alpha = 0.03f),
                radius = 120.dp.toPx(),
                center = Offset(size.width * 0.8f, size.height * 0.3f)
            )
        }

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
                Text(text = "📭", fontSize = 64.sp)
                
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
fun HomeScreen(viewModel: QuizViewModel) {
    val totalStars = viewModel.totalStars
    val streakCount = viewModel.streakCount
    val todayCompleted = viewModel.todayQuizzesCompleted
    val dailyGoal = viewModel.dailyQuizGoal
    var showTrophyDialog by remember { mutableStateOf(false) }

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

                // Right Capsule (Streak)
                Box(
                    modifier = Modifier
                        .background(SleekSurfaceVariant, shape = RoundedCornerShape(100.dp))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .border(1.dp, SleekBorderDark, shape = RoundedCornerShape(100.dp))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(text = "🔥", fontSize = 16.sp)
                        Text(
                            text = "$streakCount",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = Color(0xFF49454F)
                        )
                    }
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
            // Daily Progress goals Card styled precisely like Sleek Interface
            Card(
                colors = CardDefaults.cardColors(containerColor = SleekSurfaceVariant),
                shape = RoundedCornerShape(28.dp),
                border = BorderStroke(1.dp, SleekBorderLight),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    val progressRatio = if (dailyGoal > 0) todayCompleted.toFloat() / dailyGoal else 1f

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Daily Goal: $todayCompleted/${dailyGoal} Stars",
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            color = Color(0xFF49454F)
                        )
                        Text(
                            text = "Keep it up!",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = SleekPurple
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = { progressRatio.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(16.dp)
                            .clip(RoundedCornerShape(100.dp)),
                        color = SleekPurple,
                        trackColor = SleekBorderLight
                    )
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
                        text = "Hi, ${viewModel.childName}!",
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
                .clickable(onClick = onClick)
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
                        Text(
                            text = "⏱️ ${String.format("%02d:%02d", mins, secs)}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (timeLeftSeconds <= 10) Color(0xFFBA1A1A) else SleekPurple
                        )
                    }
                }

                // Current stars holder
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
                    LinearProgressIndicator(
                        progress = { (currentQuestionIdx + 1).toFloat() / activeQuestions.size },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(CircleShape),
                        color = SleekPurple,
                        trackColor = SleekBorderLight
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
                            // Style determination based on whether answer is verified or not
                            val buttonColor = when {
                                isAnswerChecked -> {
                                    when {
                                        idx == currentQuestion.correctOptionIndex -> Color(0xFFD4EDDA) // correct green accent
                                        selectedOptionIdx == idx -> Color(0xFFF8D7DA) // incorrect red accent
                                        else -> SleekSurfaceVariant
                                    }
                                }
                                selectedOptionIdx == idx -> Color(0xFFEADDFF) // selected purple tint
                                else -> SleekSurfaceVariant
                            }

                            val borderColor = if (selectedOptionIdx == idx) SleekPurple else SleekBorderLight
                            val strokeWidth = if (selectedOptionIdx == idx) 2.dp else 1.dp
                            val textStyleColor = SleekTextDark

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 60.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(buttonColor)
                                    .border(strokeWidth, borderColor, RoundedCornerShape(20.dp))
                                    .testTag("quiz_option_$idx")
                                    .clickable {
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
                            viewModel.submitAnswerAndNext(subject)
                        },
                        enabled = selectedOptionIdx != -1,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SleekPurple,
                            disabledContainerColor = Color(0xFFE7E0EC)
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("submit_answer_button")
                    ) {
                        val buttonText = if (isLast) "See Results! 🏆" else "Next 👉"
                        Text(text = buttonText, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = if (selectedOptionIdx != -1) Color.White else Color(0xFFCAC4D0))
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
    // Confetti canvas particles helper simulation to celebrate!
    val infiniteTransition = rememberInfiniteTransition(label = "confetti")
    val multiplier by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(animation = tween(4000, easing = LinearEasing)),
        label = "rotation"
    )

    Scaffold(containerColor = Color.Transparent) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Confetti canvas sparkles
            Canvas(modifier = Modifier.fillMaxSize()) {
                val rSeed = multiplier.toInt()
                for (i in 0..40) {
                    val angle = (i * 12 + rSeed) % 360
                    val rad = Math.toRadians(angle.toDouble())
                    val distance = (120 + (i * 7) % 300).dp.toPx()
                    val x = size.width / 2 + (distance * Math.cos(rad)).toFloat()
                    val y = size.height * 0.35f + (distance * Math.sin(rad)).toFloat()
                    val color = when (i % 5) {
                        0 -> Color(0xFFD0E4FF)
                        1 -> Color(0xFFFFDBCB)
                        2 -> Color(0xFFC2F0C2)
                        3 -> Color(0xFFF2D8FF)
                        else -> SleekPurple
                    }
                    drawCircle(color = color, radius = (4 + i % 6).dp.toPx(), center = Offset(x, y))
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.height(48.dp))

                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .background(
                            brush = Brush.radialGradient(listOf(Color(0xFFFEF7FF), Color(0xFFEADDFF))),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "👑", fontSize = 80.sp, modifier = Modifier.rotate(multiplier * 0.1f))
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "YAY! YOU DID IT! 🎉",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Black,
                    color = SleekPurple,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "You completed the $subject quiz!",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF49454F),
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Score stats circle holder
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
                            // Circular Score meter
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .background(Color(0xFFEADDFF), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(text = "$score / $total", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color(0xFF21005D))
                                    Text(text = "Correct", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF49454F))
                                }
                            }

                            // Star points added indicators
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

                        // Playful performance review messages
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

                Spacer(modifier = Modifier.height(24.dp))
                
                // Detailed Report
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

                // Done Button to bring back Home
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

    var activeTab by remember { mutableStateOf(0) } // 0: Scores, 1: Add Quiz Questions, 2: Goal & PIN Settings, 3: Archive, 4: Subjects
    val drawerState = androidx.compose.material3.rememberDrawerState(initialValue = androidx.compose.material3.DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    androidx.compose.material3.ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            androidx.compose.material3.ModalDrawerSheet(
                modifier = Modifier.width(280.dp),
                drawerContainerColor = SleekSurfaceVariant
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Parents Admin Menu",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = SleekPurple,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )
                HorizontalDivider(color = SleekBorderLight, modifier = Modifier.padding(bottom = 8.dp))
                
                androidx.compose.material3.NavigationDrawerItem(
                    icon = { Text("📈") },
                    label = { Text("Score History", fontWeight = FontWeight.Bold) },
                    selected = activeTab == 0,
                    onClick = {
                        activeTab = 0
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    colors = androidx.compose.material3.NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = SleekPurple,
                        selectedTextColor = Color.White,
                        selectedIconColor = Color.White,
                        unselectedTextColor = SleekTextDark
                    )
                )
                androidx.compose.material3.NavigationDrawerItem(
                    icon = { Text("📝") },
                    label = { Text("Questions & Bank", fontWeight = FontWeight.Bold) },
                    selected = activeTab == 1,
                    onClick = {
                        activeTab = 1
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    colors = androidx.compose.material3.NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = SleekPurple,
                        selectedTextColor = Color.White,
                        selectedIconColor = Color.White,
                        unselectedTextColor = SleekTextDark
                    )
                )
                androidx.compose.material3.NavigationDrawerItem(
                    icon = { Text("📚") },
                    label = { Text("Manage Subjects", fontWeight = FontWeight.Bold) },
                    selected = activeTab == 4,
                    onClick = {
                        activeTab = 4
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    colors = androidx.compose.material3.NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = SleekPurple,
                        selectedTextColor = Color.White,
                        selectedIconColor = Color.White,
                        unselectedTextColor = SleekTextDark
                    )
                )
                androidx.compose.material3.NavigationDrawerItem(
                    icon = { Text("🗄️") },
                    label = { Text("Archived Questions", fontWeight = FontWeight.Bold) },
                    selected = activeTab == 3,
                    onClick = {
                        activeTab = 3
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    colors = androidx.compose.material3.NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = SleekPurple,
                        selectedTextColor = Color.White,
                        selectedIconColor = Color.White,
                        unselectedTextColor = SleekTextDark
                    )
                )
                androidx.compose.material3.NavigationDrawerItem(
                    icon = { Text("⚙️") },
                    label = { Text("Settings & Access", fontWeight = FontWeight.Bold) },
                    selected = activeTab == 2,
                    onClick = {
                        activeTab = 2
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    colors = androidx.compose.material3.NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = SleekPurple,
                        selectedTextColor = Color.White,
                        selectedIconColor = Color.White,
                        unselectedTextColor = SleekTextDark
                    )
                )
                
                Spacer(modifier = Modifier.weight(1f))
                androidx.compose.material3.NavigationDrawerItem(
                    icon = { Icon(Icons.Default.ExitToApp, contentDescription = "Exit") },
                    label = { Text("Exit to App", fontWeight = FontWeight.Bold) },
                    selected = false,
                    onClick = { viewModel.navigateTo(Screen.Home) },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 24.dp),
                    colors = androidx.compose.material3.NavigationDrawerItemDefaults.colors(
                        unselectedTextColor = SleekPurple,
                        unselectedIconColor = SleekPurple
                    )
                )
            }
        }
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Transparent)
                        .padding(top = 16.dp, bottom = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(
                            onClick = { scope.launch { drawerState.open() } },
                            modifier = Modifier
                                .background(SleekSurfaceVariant, CircleShape)
                                .size(36.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Menu, contentDescription = "Menu", tint = SleekPurple)
                        }

                        Text(
                            text = when (activeTab) {
                                0 -> "Score History 📈"
                                1 -> "Questions 📝"
                                2 -> "Settings ⚙️"
                                3 -> "Archived Bank 🗄️"
                                4 -> "Manage Subjects 📚"
                                else -> "Admin Panel 🛠️"
                            },
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = SleekPurple
                        )

                        Spacer(modifier = Modifier.width(36.dp)) // horizontal alignment spacing
                    }
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
                    2 -> ParentSettingsTab(viewModel = viewModel)
                    3 -> ParentArchiveTab(viewModel = viewModel, allQuestions = allQuestions)
                    4 -> ParentSubjectsTab(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun ParentScoresHistoryTab(allAttempts: List<QuizAttempt>) {
    if (allAttempts.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "📭", fontSize = 48.sp)
                Text(
                    text = "No scores recorded yet!",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = SleekPurple
                )
                Text(
                    text = "When the kid plays a quiz, their results, stars, and completion history will populate here.",
                    fontSize = 13.sp,
                    color = Color(0xFF49454F),
                    lineHeight = 18.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Active Progress Tracker",
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    color = SleekPurple,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            items(allAttempts) { attempt ->
                val dateFmt = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault())
                val formattedDate = dateFmt.format(Date(attempt.timestamp))
                var expanded by remember { mutableStateOf(false) }

                Card(
                    modifier = Modifier.fillMaxWidth().clickable {
                        if (attempt.answersJson.isNotBlank()) expanded = !expanded
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SleekSurfaceVariant),
                    border = BorderStroke(1.dp, SleekBorderLight)
                ) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val labelEmoji = when (attempt.subject) {
                                        "Math" -> "🧮 "
                                        "English" -> "📝 "
                                        "General Knowledge" -> "🌍 "
                                        "Science" -> "🧪 "
                                        else -> "🌟 "
                                    }
                                    Text(
                                        text = "$labelEmoji${attempt.subject}",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SleekPurple
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = formattedDate,
                                    fontSize = 11.sp,
                                    color = Color(0xFF49454F)
                                )
                                if (attempt.answersJson.isNotBlank()) {
                                    Text(text = if (expanded) "Tap to hide details" else "Tap to review test details 📝", fontSize = 10.sp, color = SleekPurple, modifier = Modifier.padding(top = 4.dp))
                                }
                            }

                            // Score metrics
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${attempt.score} / ${attempt.totalQuestions} Right",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (attempt.score == attempt.totalQuestions) Color(0xFF28A745) else SleekPurple
                                )
                                Row {
                                    for (i in 1..attempt.starsEarned) {
                                        Text(text = "⭐", fontSize = 12.sp)
                                    }
                                }
                            }
                        }

                        if (expanded && attempt.answersJson.isNotBlank()) {
                            HorizontalDivider(color = SleekBorderLight, thickness = 1.dp)
                            
                            val parsedDetails = remember(attempt.answersJson) {
                                val list = mutableListOf<Triple<String, Triple<String, String, Boolean>, String>>() // Q, <UStr, CStr, IsCorrect>, Err
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

                            Column(modifier = Modifier.padding(16.dp)) {
                                parsedDetails.forEachIndexed { i, detail ->
                                    if (detail.third.isNotEmpty()) {
                                        Text("Failed to parse test details.", color = Color.Red, fontSize = 12.sp)
                                    } else {
                                        val qText = detail.first
                                        val uStr = detail.second.first
                                        val cStr = detail.second.second
                                        val isCorrect = detail.second.third
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(bottom = 8.dp)
                                                .background(if (isCorrect) Color(0xFFF1F8F1) else Color(0xFFFCF2F2), RoundedCornerShape(8.dp))
                                                .border(1.dp, if (isCorrect) Color(0xFFD4EDDA) else Color(0xFFF8D7DA), RoundedCornerShape(8.dp))
                                                .padding(12.dp)
                                        ) {
                                            Column {
                                                Text("Q${i + 1}: $qText", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SleekTextDark)
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text("Given Answer: $uStr", color = if (isCorrect) Color(0xFF28A745) else Color(0xFFDC3545), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                if (!isCorrect) {
                                                    Text("Correct Answer: $cStr", color = Color(0xFF28A745), fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
        }
    }
}

@Composable
fun ParentQuestionsTab(viewModel: QuizViewModel, allQuestions: List<Question>) {
    var activeMode by remember { mutableStateOf(0) } // 0: Bank, 1: Manual, 2: AI, 3: Import
    val context = LocalContext.current

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
                val sampleJson = "[\n" +
                        "  {\n" +
                        "    \"subject\": \"Math\",\n" +
                        "    \"questionText\": \"What is 10 + 5?\",\n" +
                        "    \"optionA\": \"10\",\n" +
                        "    \"optionB\": \"15\",\n" +
                        "    \"optionC\": \"20\",\n" +
                        "    \"optionD\": \"25\",\n" +
                        "    \"correctOptionIndex\": 1\n" +
                        "  }\n" +
                        "]"
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(sampleJson.toByteArray())
                }
            } catch (e: Exception) {
                viewModel.fileImportStatus = "Failed to export: ${e.localizedMessage}"
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Tab-like Section Modes at the very top of custom questions management area
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val modes = listOf("Manage Bank 📁", "Add Manually ➕", "AI Generator ✨", "Import File 📤")
            modes.forEachIndexed { index, title ->
                val isSel = activeMode == index
                Box(
                    modifier = Modifier
                        .background(
                            color = if (isSel) SleekPurple else SleekSurfaceVariant,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .border(1.dp, if (isSel) SleekPurple else SleekBorderLight, RoundedCornerShape(12.dp))
                        .testTag("questions_mode_$index")
                        .clickable { activeMode = index }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSel) Color.White else SleekTextDark
                    )
                }
            }
        }

        // Horizontal separator layout line
        HorizontalDivider(color = SleekBorderLight, thickness = 1.dp)

        AnimatedContent(
            targetState = activeMode,
            transitionSpec = {
                fadeIn(animationSpec = tween(150)) togetherWith fadeOut(animationSpec = tween(150))
            },
            label = "active_management_mode"
        ) { mode ->
            when (mode) {
                0 -> {
                    // QUESTION BANK VIEW LIST (Mode 0)
                    var bankFilterSubject by remember { mutableStateOf("All") }
                    val availableSubjects = remember(allQuestions) { listOf("All") + allQuestions.map { it.subject }.distinct().sorted() }
                    
                    Column(modifier = Modifier.fillMaxSize()) {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(availableSubjects) { sub ->
                                val isSel = bankFilterSubject == sub
                                Box(
                                    modifier = Modifier
                                        .background(
                                            color = if (isSel) SleekPurple else SleekSurfaceVariant,
                                            shape = RoundedCornerShape(16.dp)
                                        )
                                        .clickable { bankFilterSubject = sub }
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = sub,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSel) Color.White else SleekTextDark
                                    )
                                }
                            }
                        }

                        val allActiveQs = allQuestions.filter { !it.isArchived }
                        val filteredQs = if (bankFilterSubject == "All") allActiveQs else allActiveQs.filter { it.subject == bankFilterSubject }

                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            val customQs = filteredQs.filter { it.isCustom }
                            val systemQs = filteredQs.filter { !it.isCustom }

                            if (customQs.isNotEmpty()) {
                                item {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Custom Questions (${customQs.size})",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Black,
                                            color = SleekPurple
                                        )
                                        IconButton(onClick = { viewModel.clearAllCustomQuestions(allQuestions) }) {
                                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Clear All Custom", tint = Color.Red)
                                        }
                                    }
                                }

                                items(customQs) { q ->
                                    QuestionCard(question = q, isCustom = true, onDelete = { viewModel.deleteQuestion(q) })
                                }
                            } else {
                            item {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = SleekSurfaceVariant),
                                    border = BorderStroke(1.dp, SleekBorderLight)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(text = "✍️", fontSize = 36.sp)
                                        Text(
                                            text = "No custom questions yet!",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = SleekTextDark
                                        )
                                        Text(
                                            text = "Create your own manually, generate with AI or upload a list file (CSV/JSON) above!",
                                            fontSize = 12.sp,
                                            color = Color(0xFF6C757D),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            Text(
                                text = "Prepared Default Questions (${systemQs.size})",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                color = SleekPurple,
                                modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                            )
                        }

                        items(systemQs) { q ->
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

@Composable
fun ParentSubjectsTab(viewModel: QuizViewModel) {
    val standardTopicSuggestions = remember {
        mapOf(
            "Math" to listOf("Addition ➕", "Subtraction ➖", "Multiplication ⚔️", "Division ➗", "Fractions 🍰", "Geometry 📐", "Counting 🔢", "Decimals 🪙"),
            "English" to listOf("Nouns 🏷️", "Verbs 🏃", "Adjectives ✨", "Pronouns 👥", "Spelling 🔠", "Opposites 🔄", "Punctuation 💬", "Synonyms 👥"),
            "Science" to listOf("Plants 🌿", "Planets 🪐", "Water Cycle 💧", "Animals 🦁", "Human Body 🫁", "Gravity 🍎", "Matter States 🧪", "Electricity ⚡"),
            "General Knowledge" to listOf("Capitals 🏛️", "Oceans 🌊", "Continents 🗺️", "Landmarks 🗽", "History 📜", "Inventions 💡", "Sports ⚽", "Nature ⛰️")
        )
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        var newSubjectInput by remember { mutableStateOf("") }
        var newCategoryInput by remember { mutableStateOf("") }
        var selectedCatSubject by remember { mutableStateOf("Math") }
        
        // SECTION 1: ADD MAIN SUBJECT
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
                    text = "Add Custom Subject 📚",
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    color = SleekPurple
                )
                Text(
                    text = "Create new main subjects that you want to show in the Kids section.",
                    fontSize = 12.sp,
                    color = Color(0xFF49454F),
                    lineHeight = 18.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newSubjectInput,
                        onValueChange = { newSubjectInput = it },
                        label = { Text("Subject Name (e.g. Geography)") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black
                        )
                    )
                    
                    Button(
                        onClick = {
                            viewModel.addSubject(newSubjectInput)
                            newSubjectInput = ""
                        },
                        enabled = newSubjectInput.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = SleekPurple),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Text("Add ➕")
                    }
                }
            }
        }

        Text(
            text = "Active Subjects:",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = SleekPurple,
            modifier = Modifier.padding(top = 8.dp)
        )

        val allCurrentSubjects = (listOf("Math", "English", "General Knowledge", "Science") + viewModel.customSubjects).distinct()
        
        allCurrentSubjects.forEach { subj ->
            val isCustom = viewModel.customSubjects.contains(subj)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .border(1.dp, SleekBorderLight, RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = subj + if (!isCustom) " (System)" else "",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = SleekTextDark
                )
                if (isCustom) {
                    IconButton(
                        onClick = { viewModel.removeSubject(subj) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete subject",
                            tint = Color(0xFFBA1A1A),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // SECTION 2: ADD CATEGORY/TOPIC
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
                    text = "Add Custom Topic under Subject 🏷️",
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    color = SleekPurple
                )
                Text(
                    text = "Define specific topics within subjects to help AI strictly generate focused questions.",
                    fontSize = 12.sp,
                    color = Color(0xFF49454F),
                    lineHeight = 18.sp
                )

                Text(text = "Select Parent Subject:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SleekPurple)
                val combinedSubjects = (listOf("Math", "English", "General Knowledge", "Science") + viewModel.customSubjects).distinct()
                
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(combinedSubjects) { sub ->
                        val isSel = selectedCatSubject == sub
                        Box(
                            modifier = Modifier
                                .background(if (isSel) SleekPurple else Color.White, RoundedCornerShape(12.dp))
                                .border(1.dp, if (isSel) SleekPurple else SleekBorderLight, RoundedCornerShape(12.dp))
                                .clickable { selectedCatSubject = sub }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(text = sub, fontSize = 13.sp, color = if (isSel) Color.White else SleekTextDark)
                        }
                    }
                }

                // Suggestions & Intelligent Search Title
                val suggestionsList = standardTopicSuggestions[selectedCatSubject] ?: emptyList()
                val filteredSuggestions = if (newCategoryInput.isBlank()) {
                    suggestionsList
                } else {
                    suggestionsList.filter { it.contains(newCategoryInput, ignoreCase = true) }
                }

                if (filteredSuggestions.isNotEmpty()) {
                    Text(
                        text = "Smart Suggestions (Tap to select):",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredSuggestions) { suggestionText ->
                            val cleanText = suggestionText.substringBefore(" ").trim()
                            Box(
                                modifier = Modifier
                                    .background(SleekPurple.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                                    .border(1.dp, SleekPurple.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                    .clickable { newCategoryInput = cleanText }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(text = suggestionText, fontSize = 11.sp, color = SleekPurple, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newCategoryInput,
                        onValueChange = { newCategoryInput = it },
                        label = { Text("Topic Name (e.g. Fractions)") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black
                        )
                    )
                    
                    Button(
                        onClick = {
                            viewModel.addCategory(subject = selectedCatSubject, topic = newCategoryInput)
                            newCategoryInput = ""
                        },
                        enabled = newCategoryInput.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = SleekPurple),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Text("Add 🏷️")
                    }
                }
            }
        }

        Text(
            text = "Your Configured Topics:",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = SleekPurple,
            modifier = Modifier.padding(top = 8.dp)
        )

        if (viewModel.customCategories.isEmpty()) {
            Text(
                text = "No custom topics added yet.",
                fontSize = 13.sp,
                color = Color.Gray
            )
        } else {
            viewModel.customCategories.forEach { categoryKey ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .border(1.dp, SleekBorderLight, RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val parts = categoryKey.split(":")
                    val subLabel = parts.getOrNull(0) ?: ""
                    val tpcLabel = parts.getOrNull(1) ?: categoryKey
                    Column {
                        Text(
                            text = tpcLabel,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = SleekTextDark
                        )
                        Text(
                            text = "Subject: $subLabel",
                            fontWeight = FontWeight.Normal,
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


@Composable
fun ParentArchiveTab(viewModel: QuizViewModel, allQuestions: List<Question>) {
    val archivedQuestions = remember(allQuestions) { allQuestions.filter { it.isArchived } }
    var filterSubject by remember { mutableStateOf("All") }
    val availableSubjects = remember(archivedQuestions) { listOf("All") + archivedQuestions.map { it.subject }.distinct().sorted() }

    if (archivedQuestions.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "🗄️", fontSize = 48.sp)
                Text(
                    text = "No archived questions!",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = SleekPurple
                )
                Text(
                    text = "When your child completes a quiz, the questions will be moved here. You can reactivate them anytime.",
                    fontSize = 13.sp,
                    color = Color(0xFF49454F),
                    lineHeight = 18.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            // Subject Filter Row
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(availableSubjects) { sub ->
                    val isSel = filterSubject == sub
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (isSel) SleekPurple else SleekSurfaceVariant,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .border(1.dp, if (isSel) SleekPurple else SleekBorderLight, RoundedCornerShape(16.dp))
                            .clickable { filterSubject = sub }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = sub,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSel) Color.White else SleekTextDark
                        )
                    }
                }
            }

            val filteredQs = if (filterSubject == "All") archivedQuestions else archivedQuestions.filter { it.subject == filterSubject }

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Archived List (${filteredQs.size})",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = SleekPurple
                        )
                        Button(
                            onClick = { 
                                filteredQs.forEach { viewModel.updateQuestion(it.copy(isArchived = false)) }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SleekPurple),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text("Activate All 🔁", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                items(filteredQs) { q ->
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
                                Box(
                                    modifier = Modifier
                                        .background(SleekSurfaceVariant, RoundedCornerShape(8.dp))
                                        .border(1.dp, SleekBorderDark, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = q.subject,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SleekPurple
                                    )
                                }

                                Button(
                                    onClick = { viewModel.updateQuestion(q.copy(isArchived = false)) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF28A745)),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text("Activate ✓", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Text(
                                text = q.questionText,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = SleekTextDark
                            )
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

@Composable
fun ParentSettingsTab(viewModel: QuizViewModel) {
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

    // AI Engine settings interactive inputs
    var apiKeyInput by remember { mutableStateOf(viewModel.aiApiKey) }
    var selectedProvider by remember { mutableStateOf(viewModel.aiProvider) }
    var saveAiSuccess by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Child Profile Customization Section Card
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
                    text = "Child Profile Settings 🎒",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = SleekPurple
                )

                Text(
                    text = "Personalize the quiz with your child's name and choose a lovely photo as their main avatar icon.",
                    fontSize = 13.sp,
                    color = Color(0xFF49454F),
                    lineHeight = 18.sp
                )

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

        // AI Question Engine Credentials Card
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
                    text = "AI Question Engine Credentials 🤖",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = SleekPurple
                )

                Text(
                    text = "Configure custom AI components to automatically generate brand-new quiz questions in English, Math, Science or General Knowledge.",
                    fontSize = 13.sp,
                    color = Color(0xFF49454F),
                    lineHeight = 18.sp
                )

                // Selector for Provider Choice
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = "Choose Provider:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SleekPurple)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("openai", "deepseek").forEach { prov ->
                            val isSel = selectedProvider.lowercase() == prov
                            val provLabel = if (prov == "openai") "OpenAI (GPT-4o)" else "DeepSeek AI"
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        color = if (isSel) SleekPurple else Color.Transparent,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .border(1.dp, if (isSel) SleekPurple else SleekBorderDark, RoundedCornerShape(12.dp))
                                    .testTag("ai_provider_$prov")
                                    .clickable {
                                        selectedProvider = prov
                                        saveAiSuccess = false
                                    }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = provLabel,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) Color.White else SleekTextDark
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = apiKeyInput,
                    onValueChange = {
                        apiKeyInput = it
                        saveAiSuccess = false
                    },
                    label = { Text("API Secret Key (Bearer token)") },
                    placeholder = { Text("e.g. sk-...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ai_api_key_input"),
                    shape = RoundedCornerShape(12.dp),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true
                )

                Button(
                    onClick = {
                        viewModel.updateAiSettings(selectedProvider, apiKeyInput)
                        saveAiSuccess = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SleekPurple),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("save_ai_credentials_button")
                ) {
                    Text(text = "Save Credentials 🔑", fontWeight = FontWeight.Bold)
                }

                if (saveAiSuccess) {
                    Text(
                        text = "✓ AI engine credentials saved successfully!",
                        color = Color(0xFF28A745),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
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
        val questionsList by viewModel.allQuestions.collectAsState()
        val defaultQuestionsCount = questionsList.count { !it.isCustom }
        
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
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = "ℹ️", fontSize = 24.sp)
                Column {
                    Text(text = "KidQuiz Application - Ver 1.0", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SleekPurple)
                    Text(text = "100% Offline Local Device Persistence Database", fontSize = 11.sp, color = Color(0xFF49454F))
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
