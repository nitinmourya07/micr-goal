package com.focus3.app.ui.screens

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.delay
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.focus3.app.data.model.DailyTask
import com.focus3.app.ui.components.CelebrationOverlay
import com.focus3.app.ui.components.CelebrationType
import com.focus3.app.ui.components.ConfettiOverlay
import com.focus3.app.ui.components.GlassBox
import com.focus3.app.ui.components.GoalCard
import com.focus3.app.ui.components.ProgressRing
import com.focus3.app.ui.theme.*
import com.focus3.app.ui.viewmodel.AppView
import com.focus3.app.ui.viewmodel.MainUiState
import com.focus3.app.ui.viewmodel.MainViewModel
import com.focus3.app.ui.viewmodel.NotesViewModel
import com.focus3.app.util.ShareUtils
import java.time.LocalTime
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.interaction.MutableInteractionSource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel(),
    initialNavigateTo: String? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    
    var showChallengeAddDialog by remember { mutableStateOf(false) }
    
    // Handle initial navigation from notification
    LaunchedEffect(initialNavigateTo) {
        if (initialNavigateTo == "CHALLENGES") {
            viewModel.navigateTo(AppView.CHALLENGES)
        }
    }
    
    // Handle back press
    val activity = context as? Activity
    BackHandler(enabled = uiState.currentView != AppView.HOME) {
        if (!viewModel.onBackPressed()) {
            activity?.finish()
        }
    }
    
    val showBottomBar = uiState.currentView in listOf(
        AppView.HOME, AppView.CHALLENGES, AppView.ANALYTICS, AppView.NOTES, AppView.CALENDAR
    )
    
    Scaffold(
        topBar = { },
        bottomBar = {
            if (showBottomBar) {
                PremiumBottomNavigation(
                    currentView = uiState.currentView,
                    onNavigate = { view ->
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.navigateTo(view)
                    }
                )
            }
        },
        floatingActionButton = {
            if (uiState.currentView == AppView.HOME && uiState.allCompleted) {
                FloatingActionButton(
                    onClick = {
                        ShareUtils.shareCompletedTasks(
                            context = context,
                            tasks = uiState.tasks,
                            date = uiState.formattedDate,
                            streak = uiState.streak
                        )
                    },
                    containerColor = PrimaryTeal,
                    contentColor = Color.Black
                ) {
                    Icon(Icons.Default.Share, contentDescription = "Share")
                }
            }
        },
        containerColor = DarkBackground
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            
            // ULTRA FAST screen transitions - 150ms for snappy feel
            AnimatedContent(
                targetState = uiState.currentView,
                label = "nav_transition",
                transitionSpec = {
                    val navOrder = listOf(AppView.HOME, AppView.CHALLENGES, AppView.ANALYTICS, AppView.NOTES, AppView.CALENDAR)
                    val fromIdx = navOrder.indexOf(initialState)
                    val toIdx = navOrder.indexOf(targetState)
                    
                    // Fast, snappy transitions - 150ms with FastOutSlowIn for smoothness
                    val enterDuration = 150
                    val exitDuration = 100
                    val slideOffset = { width: Int -> width / 4 } // Reduced slide distance for speed
                    
                    if (fromIdx != -1 && toIdx != -1) {
                        if (toIdx > fromIdx) {
                            slideInHorizontally(tween(enterDuration, easing = FastOutSlowInEasing)) { slideOffset(it) } + 
                            fadeIn(tween(enterDuration)) togetherWith 
                            slideOutHorizontally(tween(exitDuration)) { -slideOffset(it) } + fadeOut(tween(exitDuration))
                        } else {
                            slideInHorizontally(tween(enterDuration, easing = FastOutSlowInEasing)) { -slideOffset(it) } + 
                            fadeIn(tween(enterDuration)) togetherWith 
                            slideOutHorizontally(tween(exitDuration)) { slideOffset(it) } + fadeOut(tween(exitDuration))
                        }
                    } else if (toIdx == -1 && fromIdx != -1) {
                        slideInHorizontally(tween(enterDuration)) { slideOffset(it) } + fadeIn(tween(enterDuration)) togetherWith 
                        slideOutHorizontally(tween(exitDuration)) { -slideOffset(it) / 2 } + fadeOut(tween(exitDuration))
                    } else if (toIdx != -1 && fromIdx == -1) {
                        slideInHorizontally(tween(enterDuration)) { -slideOffset(it) / 2 } + fadeIn(tween(enterDuration)) togetherWith 
                        slideOutHorizontally(tween(exitDuration)) { slideOffset(it) } + fadeOut(tween(exitDuration))
                    } else {
                        fadeIn(tween(150)) togetherWith fadeOut(tween(100))
                    }
                }
            ) { view ->
                when (view) {
                    AppView.HOME -> {
                        HomeScreen(
                            uiState = uiState,
                            onTaskContentChange = viewModel::updateTaskContent,
                            onTaskToggle = { task ->
                                viewModel.toggleTaskCompletion(task)
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            },
                            onNavigateToAnalytics = { viewModel.navigateTo(AppView.ANALYTICS) },
                            onNavigateToStreakJourney = {
                                viewModel.selectChallenge(null)
                                viewModel.navigateTo(AppView.STREAK_JOURNEY)
                            },
                            onNavigateToProfile = { viewModel.navigateTo(AppView.PROFILE) },
                            onNavigateToChallenges = { viewModel.navigateTo(AppView.CHALLENGES) },
                            onNavigateToSettings = { viewModel.navigateTo(AppView.SETTINGS) },
                            onShare = {
                                ShareUtils.shareCompletedTasks(
                                    context = context,
                                    tasks = uiState.tasks,
                                    date = uiState.formattedDate,
                                    streak = uiState.streak
                                )
                            }
                        )
                    }
                    AppView.ANALYTICS -> {
                        AnalyticsScreen(
                            history = uiState.completionHistory,
                            allTasks = uiState.allTasksHistory,
                            streak = uiState.streak,
                            onBack = { viewModel.navigateTo(AppView.HOME) }
                        )
                    }
                    AppView.ONBOARDING -> {
                        OnboardingScreen(
                            onComplete = { viewModel.navigateTo(AppView.HOME) }
                        )
                    }
                    AppView.STREAK_JOURNEY -> {
                        StreakJourneyScreen(
                            streak = uiState.streak,
                            customMilestones = uiState.customMilestones,
                            challenge = uiState.selectedChallenge,
                            onAddCustomClick = { viewModel.navigateTo(AppView.CUSTOM_MILESTONES) },
                            onDeleteChallenge = { id -> 
                                viewModel.deleteChallenge(id)
                                viewModel.selectChallenge(null)
                                viewModel.navigateTo(AppView.STREAK_JOURNEY)
                            },
                            onCompleteChallenge = { viewModel.completeSelectedChallenge() },
                            onBack = { 
                                if (uiState.selectedChallenge != null) {
                                    viewModel.selectChallenge(null)
                                    viewModel.navigateTo(AppView.CHALLENGES)
                                } else {
                                    viewModel.navigateTo(AppView.HOME)
                                }
                            }
                        )
                    }
                    AppView.PROFILE -> {
                        ProfileScreen(
                            currentAvatar = uiState.userAvatar,
                            userName = uiState.userName,
                            streak = uiState.streak,
                            totalGoalsCompleted = uiState.completionHistory.values.sum(),
                            onAvatarChange = { viewModel.saveUserAvatar(it) },
                            onNameChange = { viewModel.saveUserName(it) },
                            onBack = { viewModel.navigateTo(AppView.HOME) }
                        )
                    }
                    AppView.CUSTOM_MILESTONES -> {
                        CustomMilestoneScreen(
                            existingMilestones = uiState.customMilestones,
                            onSave = { viewModel.updateCustomMilestones(it) },
                            onBack = { viewModel.navigateTo(AppView.STREAK_JOURNEY) }
                        )
                    }
                    AppView.TUTORIAL -> {
                        GuidedTutorial(
                            onComplete = { viewModel.navigateTo(AppView.HOME) }
                        )
                    }
                    AppView.CHALLENGES -> {
                        ChallengeScreen(
                            challenges = uiState.challenges,
                            onAddChallenge = viewModel::addChallenge,
                            onUpdateProgress = viewModel::updateChallengeProgress,
                            onDeleteChallenge = viewModel::deleteChallenge,
                            onViewJourney = { id ->
                                viewModel.selectChallenge(id)
                                viewModel.navigateTo(AppView.STREAK_JOURNEY)
                            },
                            onEditChallenge = { id ->
                                viewModel.selectChallenge(id)
                                viewModel.navigateTo(AppView.EDIT_CHALLENGE)
                            },
                            showAddDialogExternal = showChallengeAddDialog,
                            onDismissAddDialog = { showChallengeAddDialog = false }
                        )
                    }
                    AppView.NOTES -> {
                        val notesViewModel: NotesViewModel = hiltViewModel()
                        val notes by notesViewModel.notes.collectAsState()
                        val searchQuery by notesViewModel.searchQuery.collectAsState()
                        val selectedCategory by notesViewModel.selectedCategory.collectAsState()
                        
                        NotesScreen(
                            notes = notes,
                            searchQuery = searchQuery,
                            selectedCategory = selectedCategory,
                            onSearchChange = notesViewModel::updateSearchQuery,
                            onCategorySelect = notesViewModel::selectCategory,
                            onNoteClick = { note ->
                                notesViewModel.selectNote(note)
                                viewModel.navigateTo(AppView.NOTE_EDITOR)
                            },
                            onAddNote = {
                                notesViewModel.selectNote(null)
                                viewModel.navigateTo(AppView.NOTE_EDITOR)
                            },
                            onTogglePin = notesViewModel::togglePin,
                            onDeleteNote = notesViewModel::deleteNote,
                            onBack = { viewModel.navigateTo(AppView.HOME) }
                        )
                    }
                    AppView.NOTE_EDITOR -> {
                        val notesViewModel: NotesViewModel = hiltViewModel()
                        val selectedNote by notesViewModel.selectedNote.collectAsState()
                        
                        NoteEditorScreen(
                            note = selectedNote,
                            onSave = { note ->
                                notesViewModel.saveNote(note)
                                viewModel.navigateTo(AppView.NOTES)
                            },
                            onBack = { viewModel.navigateTo(AppView.NOTES) }
                        )
                    }
                    AppView.CALENDAR -> {
                        CalendarScreen(
                            completionHistory = uiState.completionHistory,
                            calendarNotes = uiState.calendarNotes,
                            currentStreak = uiState.streak,
                            longestStreak = uiState.longestStreak,
                            onBack = { viewModel.navigateTo(AppView.HOME) },
                            onSaveNote = viewModel::saveCalendarNote,
                            onDeleteNote = viewModel::deleteCalendarNote,
                            onDateClick = { }
                        )
                    }
                    AppView.EDIT_CHALLENGE -> {
                        val selectedChallenge = uiState.selectedChallenge
                        if (selectedChallenge != null) {
                            EditChallengeScreen(
                                challenge = selectedChallenge,
                                onSave = { updatedChallenge ->
                                    viewModel.updateChallenge(updatedChallenge)
                                },
                                onDelete = {
                                    viewModel.deleteChallenge(selectedChallenge.id)
                                },
                                onBack = { viewModel.navigateTo(AppView.CHALLENGES) }
                            )
                        } else {
                            viewModel.navigateTo(AppView.CHALLENGES)
                        }
                    }
                    AppView.SETTINGS -> {
                        SettingsScreen(
                            currentStreak = uiState.streak,
                            longestStreak = uiState.longestStreak,
                            totalCompletedDays = uiState.totalCompletedDays,
                            graceDaysUsed = uiState.graceDaysUsed,
                            onExportData = { 
                                val uri = com.focus3.app.util.DataExportHelper.exportToJson(
                                    context = context,
                                    tasks = uiState.tasks,
                                    challenges = uiState.challenges,
                                    notes = emptyList(),
                                    streak = uiState.streak
                                )
                                uri?.let {
                                    com.focus3.app.util.DataExportHelper.shareFile(
                                        context = context,
                                        uri = it,
                                        mimeType = "application/json"
                                    )
                                }
                            },
                            onImportData = { uri ->
                                viewModel.importDataFromUri(context, uri)
                            },
                            onClearData = { viewModel.clearAllData() },
                            onBack = { viewModel.navigateTo(AppView.HOME) }
                        )
                    }


                }
            }
        }

        if (uiState.showConfetti) {
            ConfettiOverlay(
                isVisible = true,
                message = uiState.confettiMessage,
                onDismiss = { viewModel.dismissConfetti() }
            )
        }

        // Celebration System
        if (uiState.showCelebration) {
            val celebrationType = when (uiState.celebrationType) {
                "DAILY_COMPLETE" -> CelebrationType.DAILY_COMPLETE
                "CHALLENGE_PROGRESS" -> CelebrationType.CHALLENGE_PROGRESS
                "CHALLENGE_COMPLETE" -> CelebrationType.CHALLENGE_COMPLETE
                "STREAK_MILESTONE" -> CelebrationType.STREAK_MILESTONE
                else -> CelebrationType.DAILY_COMPLETE
            }
            CelebrationOverlay(
                isVisible = true,
                type = celebrationType,
                streakCount = if (uiState.celebrationType == "STREAK_MILESTONE") 
                    uiState.celebrationChallengeName.toIntOrNull() ?: uiState.streak 
                else uiState.streak,
                challengeName = uiState.celebrationChallengeName,
                onDismiss = { viewModel.dismissCelebration() }
            )
        }
    }
}

@Composable
fun QuoteOverlay(
    quote: String, 
    color: Color,
    durationMs: Int = 5000,
    onDismiss: () -> Unit = {}
) {
    var isHolding by remember { mutableStateOf(false) }
    var timeRemaining by remember { mutableLongStateOf(durationMs.toLong()) }
    
    // Timer Logic
    LaunchedEffect(isHolding) {
        if (!isHolding) {
            val startTime = System.currentTimeMillis()
            val initialRemaining = timeRemaining
            while (timeRemaining > 0) {
                delay(50) // ~20fps — only draws thin progress bar
                val elapsed = System.currentTimeMillis() - startTime
                timeRemaining = (initialRemaining - elapsed).coerceAtLeast(0)
            }
            onDismiss()
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.9f))
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isHolding = true
                        try {
                            awaitRelease()
                        } finally {
                            isHolding = false
                        }
                    },
                    onTap = { onDismiss() }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // Progress Bar at top
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 40.dp)
                .fillMaxWidth(0.8f)
                .height(4.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.1f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(timeRemaining.toFloat() / durationMs.toFloat())
                    .background(color)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "\u2728",
                fontSize = 56.sp,
                modifier = Modifier.scale(if (isHolding) 1.2f else 1f)
            )
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = quote,
                style = MaterialTheme.typography.headlineSmall,
                color = color,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium,
                lineHeight = 32.sp
            )
            Spacer(modifier = Modifier.height(48.dp))
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (isHolding) "\u23F8\uFE0F PAUSED" else "Hold to read longer...",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isHolding) color else Color.White.copy(alpha = 0.4f),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Tap to skip",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.2f)
                )
            }
        }
    }
}

@Composable
fun HomeScreen(
    uiState: MainUiState,
    onTaskContentChange: (Int, String) -> Unit,
    onTaskToggle: (DailyTask) -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToStreakJourney: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToChallenges: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onShare: () -> Unit
) {
    val hour = remember { LocalTime.now().hour }
    
    // Dynamic greeting with personality
    val greeting = remember {
        when {
            hour < 5 -> "NIGHT OWL MODE \uD83E\uDD89"
            hour < 12 -> "RISE & DOMINATE \u2600\uFE0F"
            hour < 17 -> "KEEP CRUSHING IT \uD83D\uDD25"
            hour < 21 -> "EVENING WARRIOR \uD83C\uDF06"
            else -> "MIDNIGHT LEGEND \uD83C\uDF19"
        }
    }
    
    val topTasks = remember(uiState.tasks) { uiState.tasks.take(3) }
    val totalGoals = remember(topTasks) { topTasks.size.coerceAtLeast(1) }

    // Use derivedStateOf to prevent recompositions when completion count hasn't changed
    val completedCount by remember(topTasks) {
        derivedStateOf { topTasks.count { it.isCompleted } }
    }
    
    // Only run pulse animation when all completed - prevents continuous animation
    val shouldPulse = topTasks.isNotEmpty() && completedCount == totalGoals
    
    // Animated background pulse runs only in completion state to reduce idle GPU work
    val bgPulse = if (shouldPulse) {
        val transition = rememberInfiniteTransition(label = "bg")
        val pulse by transition.animateFloat(
            initialValue = 0f,
            targetValue = 0.1f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "bgPulse"
        )
        pulse
    } else {
        0f
    }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0A0D10), // Ultra deep
                        Color(0xFF0F1318), // Dark charcoal
                        Color(0xFF151A1F), // Gunmetal
                        if (shouldPulse) Color(0xFF00C853).copy(alpha = bgPulse) else Color(0xFF1A2025)
                    ),
                    startY = 0f,
                    endY = Float.POSITIVE_INFINITY
                )
            )
            .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 120.dp) // Space for bottom bar
    ) {
        item(key = "home_spacer") { Spacer(modifier = Modifier.height(12.dp)) }
        
        // Luxury Header
        item(key = "home_header") {
            EnhancedHeaderSection(
                greeting = greeting,
                date = uiState.formattedDate,
                progress = uiState.completionProgress,
                streak = uiState.streak,
                avatar = uiState.userAvatar,
                onAnalyticsClick = onNavigateToAnalytics,
                onStreakClick = onNavigateToStreakJourney,
                onProfileClick = onNavigateToProfile,
                onChallengesClick = onNavigateToChallenges,
                onSettingsClick = onNavigateToSettings
            )
        }
        
        // Goals Section Label - ENHANCED
        item(key = "goals_label") {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "\u26A1",
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "TODAY'S MISSION",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 1.5.sp),
                            color = PrimaryTeal
                        )
                    }
                    Text(
                        "$totalGoals GOALS TO VICTORY",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-0.5).sp
                        ),
                        color = Color.White
                    )
                }
                
                // Animated Progress Pill with visual feedback
                val pillScale by animateFloatAsState(
                    targetValue = if (shouldPulse) 1.1f else 1f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                    label = "pillScale"
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Visual checkmarks
                    repeat(totalGoals) { i ->
                        Box(
                            modifier = Modifier
                                .size(if (i < completedCount) 12.dp else 10.dp)
                                .background(
                                    if (i < completedCount) Color(0xFF00C853) else Color.White.copy(alpha = 0.2f),
                                    CircleShape
                                )
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Surface(
                        modifier = Modifier.graphicsLayer { scaleX = pillScale; scaleY = pillScale },
                        shape = RoundedCornerShape(16.dp),
                        color = if (shouldPulse) 
                            Color(0xFF00C853).copy(alpha = 0.25f) 
                        else 
                            PrimaryTeal.copy(alpha = 0.15f),
                        border = BorderStroke(
                            1.dp, 
                            if (shouldPulse) Color(0xFF00C853).copy(alpha = 0.5f) else PrimaryTeal.copy(alpha = 0.3f)
                        )
                    ) {
                        Text(
                            if (shouldPulse) "\u2705 DONE!" else "$completedCount/$totalGoals",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            color = if (shouldPulse) Color(0xFF00C853) else PrimaryTeal,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
        
        // Premium Goal Cards - Only show first 3 tasks with stable keys
        itemsIndexed(
            items = topTasks,
            key = { _, task -> "task_${task.id}" },
            contentType = { _, _ -> "goal_card" }
        ) { index, task ->
            GoalCard(
                task = task,
                goalNumber = index + 1,
                onContentChange = { content -> onTaskContentChange(task.id, content) },
                onToggleComplete = { onTaskToggle(task) }
            )
        }
        
        // Final "All Done" Celebration Card - ULTRA PREMIUM
        if (uiState.allCompleted) {
            item {
                val celebrationTransition = rememberInfiniteTransition(label = "celebration")
                val starRotation by celebrationTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 360f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(8000, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "starRotation"
                )
                val glowPulse by celebrationTransition.animateFloat(
                    initialValue = 0.3f,
                    targetValue = 0.8f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2500, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "glowPulse"
                )
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(28.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF00C853).copy(alpha = 0.25f),
                                    Color(0xFF00E676).copy(alpha = 0.15f),
                                    Color(0xFF69F0AE).copy(alpha = 0.1f)
                                )
                            )
                        )
                        .border(
                            2.dp, 
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF00C853).copy(alpha = glowPulse),
                                    Color(0xFF69F0AE).copy(alpha = 0.3f)
                                )
                            ), 
                            RoundedCornerShape(28.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Animated rotating star with glow
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            Color(0xFFFFD700).copy(alpha = glowPulse),
                                            Color.Transparent
                                        )
                                    ),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "\uD83C\uDFC6",
                                fontSize = 52.sp,
                                modifier = Modifier.rotate(starRotation / 20) // Subtle rotation
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            "ABSOLUTE LEGEND!",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color(0xFF00E676),
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            "You've conquered all $totalGoals goals today",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        // Stats row
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF00C853).copy(alpha = 0.2f)
                            ) {
                                Text(
                                    "\uD83D\uDD25 ${uiState.streak} Day Streak",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color(0xFFFFB74D),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF00C853).copy(alpha = 0.2f)
                            ) {
                                Text(
                                    "\u2705 $completedCount/$totalGoals Complete",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color(0xFF00E676),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        // Share Button - Premium
                        Button(
                            onClick = onShare,
                            modifier = Modifier.fillMaxWidth(0.8f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF00C853),
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(Icons.Default.Share, "Share", modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "SHARE YOUR VICTORY",
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }
        }
        
        // Enhanced Motivational Quote Card
        item(key = "quote_card") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                PrimaryTeal.copy(alpha = 0.08f),
                                Color.White.copy(alpha = 0.03f)
                            )
                        )
                    )
                    .border(
                        1.dp,
                        Brush.verticalGradient(
                            colors = listOf(
                                PrimaryTeal.copy(alpha = 0.3f),
                                Color.Transparent
                            )
                        ),
                        RoundedCornerShape(24.dp)
                    )
                    .padding(24.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header with gradient accent
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .width(30.dp)
                                .height(2.dp)
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(Color.Transparent, PrimaryTeal)
                                    )
                                )
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("\u2728", fontSize = 28.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Box(
                            modifier = Modifier
                                .width(30.dp)
                                .height(2.dp)
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(PrimaryTeal, Color.Transparent)
                                    )
                                )
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        "\"${uiState.permanentQuote}\"",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        ),
                        color = Color.White.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center,
                        lineHeight = 26.sp,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Attribution with gradient background
                    Box(
                        modifier = Modifier
                            .background(
                                PrimaryTeal.copy(alpha = 0.15f),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text(
                            "\uD83D\uDCAB DAILY WISDOM",
                            style = MaterialTheme.typography.labelSmall,
                            color = PrimaryTeal,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.2.sp
                        )
                    }
                }
            }
        }
        
        item { Spacer(modifier = Modifier.height(40.dp)) }
    }
}

@Composable
fun EnhancedHeaderSection(
    greeting: String,
    date: String,
    progress: Float,
    streak: Int,
    avatar: String,
    onAnalyticsClick: () -> Unit,
    onStreakClick: () -> Unit,
    onProfileClick: () -> Unit,
    onChallengesClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Column {
        // Luxury Top Row
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    greeting.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp
                    ),
                    color = PrimaryTeal
                )
                Text(
                    date.uppercase(),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-0.5).sp
                    ),
                    color = Color.White
                )
            }
            
            // Premium Avatar with Glass border
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.05f))
                    .border(1.2.dp, GlassBorder, CircleShape)
                    .clickable(onClick = onProfileClick),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    avatar,
                    fontSize = 28.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // High-End Stats Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Glass Progress Card (Elite Version)
            GlassBox(
                modifier = Modifier
                    .weight(1.2f)
                    .height(200.dp)
                    .clickable(onClick = onAnalyticsClick),
                cornerRadius = 32.dp,
                showGlow = progress > 0.9f
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    ProgressRing(
                        progress = progress,
                        size = 110.dp,
                        strokeWidth = 12.dp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        "MISSION PROGRESS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.2.sp
                        ),
                        color = Color.White.copy(alpha = 0.4f)
                    )
                }
            }
            
            // Neon Streak Card (Elite) with pulsing animation
            GlassBox(
                modifier = Modifier
                    .weight(0.9f)
                    .height(200.dp)
                    .clickable { onStreakClick() },
                cornerRadius = 32.dp,
                showGlow = streak > 0
            ) {
                val (fireScale, fireGlow) = if (streak > 0) {
                    val transition = rememberInfiniteTransition(label = "streak")
                    val animatedScale by transition.animateFloat(
                        initialValue = 1f,
                        targetValue = 1.12f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1500, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "fireScale"
                    )
                    val animatedGlow by transition.animateFloat(
                        initialValue = 0.3f,
                        targetValue = 0.65f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(2000, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "fireGlow"
                    )
                    animatedScale to animatedGlow
                } else {
                    1f to 0.1f
                }
                
                Column(
                    modifier = Modifier.fillMaxSize().padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Animated fire with glow
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        StreakOrange.copy(alpha = fireGlow),
                                        Color.Transparent
                                    )
                                ),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "\uD83D\uDD25",
                            fontSize = 36.sp,
                            modifier = Modifier.scale(fireScale)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        streak.toString(),
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-1).sp
                        ),
                        color = if (streak > 0) Color.White else Color.White.copy(alpha = 0.5f)
                    )
                    Text(
                        if (streak > 0) "DAY STREAK" else "START NOW",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        ),
                        color = if (streak > 0) StreakOrange else Color.White.copy(alpha = 0.5f)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Premium Quick Action Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            QuickActionButton(
                icon = Icons.Default.Flag,
                contentDescription = "Open missions",
                label = "Missions",
                color = PrimaryTeal,
                onClick = onChallengesClick
            )
            QuickActionButton(
                icon = Icons.Default.Analytics,
                contentDescription = "Open analytics",
                label = "Data",
                color = NeonCyan,
                onClick = onAnalyticsClick
            )
            QuickActionButton(
                icon = Icons.Default.Settings,
                contentDescription = "Open settings",
                label = "Config",
                color = Color.White.copy(alpha = 0.5f),
                onClick = onSettingsClick
            )
        }
    }
}

@Composable
fun QuickActionButton(
    icon: ImageVector,
    contentDescription: String,
    label: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var isPressed by remember { mutableStateOf(false) }
    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(120)
            isPressed = false
        }
    }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "quickActionScale"
    )
    
    Column(
        modifier = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        color.copy(alpha = 0.06f),
                        Color.Transparent
                    )
                )
            )
            .border(
                width = 0.5.dp,
                color = color.copy(alpha = 0.1f),
                shape = RoundedCornerShape(22.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { 
                isPressed = true
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick() 
            }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            color.copy(alpha = 0.18f),
                            color.copy(alpha = 0.04f),
                            Color.Transparent
                        )
                    ),
                    CircleShape
                )
                .border(
                    1.dp, 
                    Brush.linearGradient(
                        colors = listOf(
                            color.copy(alpha = 0.4f),
                            color.copy(alpha = 0.1f)
                        )
                    ), 
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = color,
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            label.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = 0.8.sp,
                fontSize = 10.sp
            ),
            color = color.copy(alpha = 0.9f)
        )
    }
}

/**
 * Premium Bottom Navigation with glassmorphic pill and neon glow
 * Features: Animated pill indicator, icon scale, active dot, smooth transitions
 */
@Composable
fun PremiumBottomNavigation(
    currentView: AppView,
    onNavigate: (AppView) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val navItems = remember {
        listOf(
            NavItem(AppView.HOME, Icons.Default.Home, "HOME", "Home"),
            NavItem(AppView.CHALLENGES, Icons.Default.Flag, "MISSIONS", "Missions"),
            NavItem(AppView.ANALYTICS, Icons.Default.Analytics, "STATS", "Analytics"),
            NavItem(AppView.NOTES, Icons.Default.Description, "NOTES", "Notes"),
            NavItem(AppView.CALENDAR, Icons.Default.DateRange, "CALENDAR", "Calendar")
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        DarkBackground.copy(alpha = 0.97f),
                        DarkBackground
                    )
                )
            )
    ) {
        // Glassmorphic bar container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.06f),
                            Color.White.copy(alpha = 0.02f)
                        )
                    )
                )
                .border(
                    width = 0.8.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.12f),
                            Color.White.copy(alpha = 0.03f)
                        )
                    ),
                    shape = RoundedCornerShape(28.dp)
                )
                .padding(horizontal = 4.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                navItems.forEachIndexed { index, item ->
                    val isSelected = currentView == item.view

                    // Snappy spring scale
                    val scale by animateFloatAsState(
                        targetValue = if (isSelected) 1.08f else 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessHigh
                        ),
                        label = "scale_$index"
                    )

                    // Icon tint animation
                    val tintColor by animateColorAsState(
                        targetValue = if (isSelected) PrimaryTeal else Color.White.copy(alpha = 0.55f),
                        animationSpec = tween(180),
                        label = "tint_$index"
                    )

                    // Active pill alpha
                    val pillAlpha by animateFloatAsState(
                        targetValue = if (isSelected) 1f else 0f,
                        animationSpec = tween(160),
                        label = "pill_$index"
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onNavigate(item.view)
                            }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .graphicsLayer {
                                    scaleX = scale
                                    scaleY = scale
                                }
                        ) {
                            // Glow behind selected icon
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(50.dp)
                                        .graphicsLayer { alpha = pillAlpha * 0.4f }
                                        .background(
                                            brush = Brush.radialGradient(
                                                colors = listOf(
                                                    PrimaryTeal.copy(alpha = 0.35f),
                                                    Color.Transparent
                                                )
                                            ),
                                            shape = CircleShape
                                        )
                                )
                            }

                            // Glass pill indicator around selected icon
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(42.dp)
                                    .then(
                                        if (isSelected) {
                                            Modifier
                                                .clip(CircleShape)
                                                .background(
                                                    Brush.radialGradient(
                                                        colors = listOf(
                                                            PrimaryTeal.copy(alpha = 0.18f),
                                                            PrimaryTeal.copy(alpha = 0.06f)
                                                        )
                                                    )
                                                )
                                                .border(
                                                    width = 1.dp,
                                                    color = PrimaryTeal.copy(alpha = 0.35f),
                                                    shape = CircleShape
                                                )
                                        } else Modifier
                                    )
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.contentDescription,
                                    tint = tintColor,
                                    modifier = Modifier.size(if (isSelected) 23.dp else 21.dp)
                                )
                            }
                        }

                        // Active label with fast animation
                        AnimatedVisibility(
                            visible = isSelected,
                            enter = fadeIn(tween(80)) + expandVertically(tween(80)),
                            exit = fadeOut(tween(40)) + shrinkVertically(tween(40))
                        ) {
                            Text(
                                text = item.label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.8.sp,
                                    fontSize = 9.sp
                                ),
                                color = PrimaryTeal,
                                modifier = Modifier.padding(top = 3.dp)
                            )
                        }

                        // Active dot indicator below label for unselected items
                        if (!isSelected) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Box(
                                modifier = Modifier
                                    .size(3.dp)
                                    .background(
                                        Color.White.copy(alpha = 0.15f),
                                        CircleShape
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class NavItem(
    val view: AppView,
    val icon: ImageVector,
    val label: String,
    val contentDescription: String
)
