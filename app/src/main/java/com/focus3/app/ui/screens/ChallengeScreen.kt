package com.focus3.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focus3.app.data.model.Challenge
import com.focus3.app.ui.components.GlassBox
import com.focus3.app.ui.components.ProgressRing
import com.focus3.app.ui.theme.DarkBackground
import com.focus3.app.ui.theme.NeonCyan
import com.focus3.app.ui.theme.PrimaryTeal
import androidx.compose.foundation.Canvas
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import java.time.LocalDate
import java.time.temporal.ChronoUnit

val challengeIcons = listOf(
    "📚", "📖", "✏️", "🎯", "💪", "🏆",
    "🔥", "⭐", "🚀", "💡", "🧠", "📝"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChallengeScreen(
    challenges: List<Challenge>,
    onAddChallenge: (String, Int, String, String?) -> Unit,
    onUpdateProgress: (Int) -> Unit,
    onDeleteChallenge: (Int) -> Unit,
    onViewJourney: (Int) -> Unit,
    onEditChallenge: (Int) -> Unit = {},
    showAddDialogExternal: Boolean = false,
    onDismissAddDialog: () -> Unit = {}
) {
    var showAddDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(showAddDialogExternal) {
        if (showAddDialogExternal) {
            showAddDialog = true
            onDismissAddDialog()
        }
    }
    
    // Memoized stats calculation
    val totalChallenges = remember(challenges) { challenges.size }
    val activeChallenges = remember(challenges) { challenges.count { it.completedDays < it.targetDays } }
    val completedChallenges = remember(challenges) { challenges.count { it.completedDays >= it.targetDays } }
    val totalProgress = remember(challenges) {
        if (challenges.isNotEmpty()) {
            challenges.sumOf { it.completedDays } * 100 / challenges.sumOf { it.targetDays }.coerceAtLeast(1)
        } else 0
    }
    val sortedChallenges = remember(challenges) {
        challenges.sortedWith(
            compareBy<Challenge> { it.completedDays >= it.targetDays }
                .thenBy { (it.targetDays - it.completedDays).coerceAtLeast(0) }
                .thenBy { it.id }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (challenges.isEmpty()) {
            // Premium Inspiring Empty state with FAST animations
            val infiniteTransition = rememberInfiniteTransition(label = "empty_state")
            val pulseAlpha by infiniteTransition.animateFloat(
                initialValue = 0.3f,
                targetValue = 0.7f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1800, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "pulseAlpha"
            )
            val iconScale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.08f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1500, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "iconScale"
            )
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Animated large icon with pulsing glow
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        PrimaryTeal.copy(alpha = pulseAlpha),
                                        NeonCyan.copy(alpha = pulseAlpha * 0.5f),
                                        Color.Transparent
                                    )
                                ),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "🎯",
                            fontSize = 90.sp,
                            modifier = Modifier.graphicsLayer {
                                scaleX = iconScale
                                scaleY = iconScale
                            }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(36.dp))
                    
                    Text(
                        "FORGE YOUR LEGEND",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 3.sp
                        ),
                        color = Color.White
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        "Transform your goals into achievements.\nOne day at a time, one mission at a time.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center,
                        lineHeight = 24.sp
                    )
                    
                    Spacer(modifier = Modifier.height(36.dp))
                    
                    // Feature Cards Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Track Progress Card
                        GlassBox(
                            modifier = Modifier.weight(1f),
                            cornerRadius = 16.dp
                        ) {
                            Column(
                                Modifier.padding(vertical = 20.dp, horizontal = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(
                                            PrimaryTeal.copy(alpha = 0.15f),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("📊", fontSize = 24.sp)
                                }
                                Spacer(Modifier.height(10.dp))
                                Text(
                                    "TRACK",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                                    color = Color.White
                                )
                                Text(
                                    "Progress",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.5f)
                                )
                            }
                        }
                        
                        // Build Streaks Card
                        GlassBox(
                            modifier = Modifier.weight(1f),
                            cornerRadius = 16.dp
                        ) {
                            Column(
                                Modifier.padding(vertical = 20.dp, horizontal = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(
                                            Color(0xFFFF9800).copy(alpha = 0.15f),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("🔥", fontSize = 24.sp)
                                }
                                Spacer(Modifier.height(10.dp))
                                Text(
                                    "BUILD",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                                    color = Color.White
                                )
                                Text(
                                    "Streaks",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.5f)
                                )
                            }
                        }
                        
                        // Achieve Goals Card
                        GlassBox(
                            modifier = Modifier.weight(1f),
                            cornerRadius = 16.dp
                        ) {
                            Column(
                                Modifier.padding(vertical = 20.dp, horizontal = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(
                                            Color(0xFF00E676).copy(alpha = 0.15f),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("🏆", fontSize = 24.sp)
                                }
                                Spacer(Modifier.height(10.dp))
                                Text(
                                    "WIN",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                                    color = Color.White
                                )
                                Text(
                                    "Goals",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(36.dp))
                    
                    // Enhanced CTA Button
                    // Hyper-Premium CTA
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .shadow(12.dp, RoundedCornerShape(20.dp), spotColor = PrimaryTeal)
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(PrimaryTeal, NeonCyan, PrimaryTeal)
                                )
                            )
                            .clickable { showAddDialog = true }
                            .padding(2.dp) // Border Width
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(18.dp))
                                .background(DarkBackground),
                            contentAlignment = Alignment.Center
                        ) {
                             // Background glow mesh
                             Box(modifier = Modifier.fillMaxSize().alpha(0.1f).background(Brush.radialGradient(listOf(PrimaryTeal, Color.Transparent))))
                             
                             Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🚀", fontSize = 22.sp)
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    "INITIATE MISSION",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 96.dp)
            ) {
                item(key = "challenge_header") { Spacer(modifier = Modifier.height(8.dp)) }
                
                item(key = "operative_command") {
                    // FAST Header animation
                    val infiniteTransition = rememberInfiniteTransition(label = "header")
                    val headerGlow by infiniteTransition.animateFloat(
                        initialValue = 0.3f,
                        targetValue = 0.6f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1800, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "headerGlow"
                    )
                    
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(top = 16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // FAST Animated Radar Icon
                            val infiniteTransition = rememberInfiniteTransition(label = "radar")
                            val radarRotate by infiniteTransition.animateFloat(
                                initialValue = 0f,
                                targetValue = 360f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(2500, easing = LinearEasing),
                                    repeatMode = RepeatMode.Restart
                                ),
                                label = "radarRotate"
                            )
                            
                            Box(contentAlignment = Alignment.Center) {
                                Canvas(modifier = Modifier.size(50.dp)) {
                                    drawCircle(
                                        color = PrimaryTeal.copy(alpha = 0.1f),
                                        radius = size.minDimension / 2
                                    )
                                    drawCircle(
                                        color = PrimaryTeal.copy(alpha = 0.3f),
                                        radius = size.minDimension / 2,
                                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
                                    )
                                    rotate(radarRotate) {
                                        drawArc(
                                            brush = Brush.sweepGradient(
                                                colors = listOf(Color.Transparent, PrimaryTeal.copy(alpha = 0.6f))
                                            ),
                                            startAngle = 0f,
                                            sweepAngle = 90f,
                                            useCenter = true,
                                            size = androidx.compose.ui.geometry.Size(size.width, size.height),
                                            topLeft = androidx.compose.ui.geometry.Offset.Zero
                                        )
                                    }
                                }
                                Text("🎯", fontSize = 24.sp)
                            }
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        "MISSION CONTROL",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = 2.sp
                                        ),
                                        color = PrimaryTeal
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    // Live Indicator
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF00E676))
                                    )
                                }
                                
                                Text(
                                    "ACTIVE OPERATIONS",
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = (-0.5).sp
                                    ),
                                    color = Color.White
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Cyberpunk Divider
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.width(20.dp).height(2.dp).background(PrimaryTeal))
                            Box(modifier = Modifier.width(4.dp).height(4.dp).background(PrimaryTeal))
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(1.dp)
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(PrimaryTeal.copy(alpha = 0.5f), Color.Transparent)
                                        )
                                    )
                            )
                        }
                    }
                }
                
                // Challenges Stats Header
                item(key = "stats_header") {
                    ChallengesStatsHeader(
                        total = totalChallenges,
                        active = activeChallenges,
                        completed = completedChallenges,
                        overallProgress = totalProgress
                    )
                }
                
                // Section title
                item(key = "active_missions_header") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "ACTIVE MISSIONS",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 1.sp),
                            color = Color.White.copy(alpha = 0.3f)
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(PrimaryTeal.copy(alpha = 0.1f))
                                .border(1.dp, PrimaryTeal.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                "$activeChallenges ACTIVE",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                                color = PrimaryTeal
                            )
                        }
                    }
                }
                
                items(
                    items = sortedChallenges,
                    key = { it.id },
                    contentType = { "challenge_card" }
                ) { challenge ->
                    ChallengeCard(
                        challenge = challenge,
                        onMarkToday = { onUpdateProgress(challenge.id) },
                        onDelete = { onDeleteChallenge(challenge.id) },
                        onViewJourney = { onViewJourney(challenge.id) },
                        onEdit = { onEditChallenge(challenge.id) }
                    )
                }
                
                item(key = "challenge_list_bottom_spacer") { Spacer(modifier = Modifier.height(28.dp)) }
            }
        }
        
        if (showAddDialog) {
            AddChallengeDialog(
                onAdd = { name, days, icon, reminder ->
                    onAddChallenge(name, days, icon, reminder)
                    showAddDialog = false
                },
                onDismiss = { showAddDialog = false }
            )
        }
        
        // Enhanced FAB with pulsing animation
        if (challenges.isNotEmpty()) {
            val fabTransition = rememberInfiniteTransition(label = "fab")
            val fabGlow by fabTransition.animateFloat(
                initialValue = 0.3f,
                targetValue = 0.7f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1500, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "fabGlow"
            )
            
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp)
            ) {
                // Glow layer
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .align(Alignment.Center)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    PrimaryTeal.copy(alpha = fabGlow),
                                    Color.Transparent
                                )
                            ),
                            CircleShape
                        )
                )
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    modifier = Modifier.size(60.dp),
                    containerColor = PrimaryTeal,
                    contentColor = Color.Black,
                    shape = CircleShape,
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 8.dp,
                        pressedElevation = 12.dp
                    )
                ) {
                    Text("➕", fontSize = 24.sp)
                }
            }
        }
    }
}

// Feature Chip for empty state
@Composable
fun FeatureChip(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = Color.White.copy(alpha = 0.8f)
        )
    }
}

// Stats Header for Challenges - Enhanced
@Composable
fun ChallengesStatsHeader(
    total: Int,
    active: Int,
    completed: Int,
    overallProgress: Int
) {
    val animatedProgress by animateFloatAsState(
        targetValue = overallProgress / 100f,
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "progress"
    )
    
    GlassBox(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 24.dp,
        showGlow = true
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header Row with Progress Ring
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "⚡ MISSION HUB",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.5.sp
                        ),
                        color = PrimaryTeal
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Track your journey",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
                
                // Progress Ring
                Box(
                    modifier = Modifier.size(70.dp),
                    contentAlignment = Alignment.Center
                ) {
                    ProgressRing(
                        progress = animatedProgress,
                        size = 68.dp,
                        strokeWidth = 6.dp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = 0.1f),
                                Color.Transparent
                            )
                        )
                    )
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                EnhancedStatItem(
                    emoji = "📋",
                    value = total.toString(),
                    label = "Total",
                    accentColor = Color.White
                )
                EnhancedStatItem(
                    emoji = "🔥",
                    value = active.toString(),
                    label = "Active",
                    accentColor = Color(0xFFFF9800)
                )
                EnhancedStatItem(
                    emoji = "🏆",
                    value = completed.toString(),
                    label = "Complete",
                    accentColor = Color(0xFF00E676)
                )
            }
        }
    }
}

@Composable
fun EnhancedStatItem(
    emoji: String,
    value: String,
    label: String,
    accentColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .background(
                    accentColor.copy(alpha = 0.1f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(emoji, fontSize = 24.sp)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            value,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
            color = accentColor
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.5f)
        )
    }
}

@Composable
fun ChallengeStatItem(
    emoji: String,
    value: String,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(emoji, fontSize = 24.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            value,
            style = MaterialTheme.typography.headlineSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.6f)
        )
    }
}


@Composable
fun ChallengeCard(
    challenge: Challenge,
    onMarkToday: () -> Unit,
    onDelete: () -> Unit,
    onViewJourney: () -> Unit,
    onEdit: () -> Unit = {}
) {
    val safeTargetDays = challenge.targetDays.coerceAtLeast(1)
    val progress = remember(challenge.completedDays, safeTargetDays) {
        (challenge.completedDays.toFloat() / safeTargetDays).coerceIn(0f, 1f)
    }
    val daysUntilEnd = remember(challenge.startDate, challenge.targetDays) {
        runCatching {
            val startDate = LocalDate.parse(challenge.startDate)
            val endDate = startDate.plusDays(challenge.targetDays.toLong())
            ChronoUnit.DAYS.between(LocalDate.now(), endDate).toInt().coerceAtLeast(0)
        }.getOrDefault(0)
    }
    
    val isCompleted = challenge.completedDays >= challenge.targetDays
    val isOnStreak = challenge.currentStreak > 0
    
    // FAST Card animation with high stiffness
    val cardScale by animateFloatAsState(
        targetValue = if (isCompleted) 0.98f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "cardScale"
    )
    
    GlassBox(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { 
                scaleX = cardScale
                scaleY = cardScale
            },
        showGlow = !isCompleted && challenge.completedDays > 0
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // ✨ Lightweight streak glow — replaces heavy emoji particles
            if (isOnStreak && !isCompleted) {
                val infiniteTransition = rememberInfiniteTransition(label = "particles")
                val glowAlpha by infiniteTransition.animateFloat(
                    initialValue = 0.05f,
                    targetValue = 0.15f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2000, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "streakGlow"
                )
                
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    PrimaryTeal.copy(alpha = glowAlpha),
                                    Color.Transparent
                                )
                            )
                        )
                )
            }


            Column(modifier = Modifier.padding(20.dp)) {
            // ELITE Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Animated Icon with glow
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(
                                Brush.radialGradient(
                                    colors = if (isCompleted) 
                                        listOf(Color(0xFF00E676).copy(alpha = 0.3f), Color.Transparent)
                                    else 
                                        listOf(PrimaryTeal.copy(alpha = 0.2f), Color.Transparent)
                                ),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = challenge.icon, fontSize = 36.sp)
                    }
                    
                    Spacer(modifier = Modifier.width(14.dp))
                    
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = challenge.name,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black
                                ),
                                color = Color.White
                            )
                            
                            // Streak Badge
                            if (isOnStreak && !isCompleted) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .background(
                                            Color(0xFFFF9100).copy(alpha = 0.2f),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        "🔥${challenge.currentStreak}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFF9100)
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        // Status Text with icon
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isCompleted) {
                                Text(
                                    "🏆 LEGENDARY COMPLETE!",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 0.5.sp
                                    ),
                                    color = Color(0xFF00E676)
                                )
                            } else {
                                Text(
                                    "⏱️",
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "$daysUntilEnd days to glory",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }
                
                // Action buttons - more compact
                Row {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.05f))
                            .clickable { onEdit() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "Edit",
                            tint = PrimaryTeal.copy(alpha = 0.8f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.05f))
                            .clickable { onDelete() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color.White.copy(alpha = 0.4f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Visual Journey Progress (dots)
            JourneyDots(
                totalDays = challenge.targetDays,
                completedDays = challenge.completedDays
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Progress stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    value = "${challenge.completedDays}",
                    label = "Done",
                    color = PrimaryTeal
                )
                StatItem(
                    value = "${challenge.targetDays}",
                    label = "Target",
                    color = Color.White
                )
                StatItem(
                    value = "${(progress * 100).toInt()}%",
                    label = "Progress",
                    color = NeonCyan
                )
            }
            
            // Animated Progress bar
            val animatedProgress by animateFloatAsState(
                targetValue = progress,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "progress"
            )
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(Color.White.copy(alpha = 0.1f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(7.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = if (isCompleted) 
                                    listOf(Color(0xFF00E676), Color(0xFF00C853))
                                else 
                                    listOf(PrimaryTeal, NeonCyan)
                            )
                        )
                ) {
                    // Shimmer effect for active challenges
                    if (!isCompleted && challenge.completedDays > 0) {
                        val shimmerAnim = rememberInfiniteTransition(label = "shimmer")
                        val shimmerOffset by shimmerAnim.animateFloat(
                            initialValue = -0.5f,
                            targetValue = 1.5f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1500, easing = LinearEasing),
                                repeatMode = RepeatMode.Restart
                            ),
                            label = "shimmerOffset"
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color.White.copy(alpha = 0.3f),
                                            Color.Transparent
                                        ),
                                        startX = shimmerOffset * 500f,
                                        endX = (shimmerOffset + 0.3f) * 500f
                                    )
                                )
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Mark today button with improved design
            if (!isCompleted) {
                Button(
                    onClick = onMarkToday,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryTeal,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("✓", fontSize = 18.sp, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("MARK TODAY COMPLETE", fontWeight = FontWeight.Black, fontSize = 14.sp)
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedButton(
                    onClick = onViewJourney,
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, PrimaryTeal),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryTeal)
                ) {
                    Text("🚀 View My Journey", fontWeight = FontWeight.Bold)
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(PrimaryTeal.copy(alpha = 0.2f))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "🏆 You completed this challenge!",
                        color = PrimaryTeal,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        }
    }
}

@Composable
fun JourneyDots(
    totalDays: Int,
    completedDays: Int
) {
    val scrollState = rememberScrollState()
    val progressPercentage = remember(totalDays, completedDays) {
        if (totalDays > 0) (completedDays.toFloat() / totalDays * 100).toInt() else 0
    }
    val maxVisibleDots = 100
    val showSimplified = totalDays > maxVisibleDots
    
    // Milestone days for special highlighting
    val milestones = remember { setOf(7, 14, 21, 30, 50, 75, 100) }
    
    // Glow animation for completed nodes
    val glowAlpha: Float = if (!showSimplified && completedDays > 0) {
        val infiniteTransition = rememberInfiniteTransition(label = "glow")
        val animatedGlowAlpha by infiniteTransition.animateFloat(
            initialValue = 0.3f,
            targetValue = 0.8f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "glowAlpha"
        )
        animatedGlowAlpha
    } else {
        0.3f
    }
    
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Progress Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🗺️", fontSize = 18.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    "Journey Progress",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Progress Badge
            Box(
                modifier = Modifier
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(PrimaryTeal.copy(alpha = 0.3f), NeonCyan.copy(alpha = 0.3f))
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    "$progressPercentage%",
                    style = MaterialTheme.typography.labelSmall,
                    color = PrimaryTeal,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        // Progress Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color.White.copy(alpha = 0.1f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(completedDays.toFloat() / totalDays.coerceAtLeast(1))
                    .fillMaxHeight()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(PrimaryTeal, NeonCyan)
                        )
                    )
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Days completed text
        Text(
            text = "$completedDays of $totalDays days completed",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.6f)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (showSimplified) {
            // Simplified view for large challenges (>100 days)
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🚀", fontSize = 24.sp)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(20.dp)
                            .padding(horizontal = 16.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White.copy(alpha = 0.1f))
                    ) {
                        val safeFraction = (completedDays.toFloat() / totalDays.coerceAtLeast(1)).coerceIn(0f, 1f)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(safeFraction)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Brush.horizontalGradient(listOf(PrimaryTeal, NeonCyan)))
                        )
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "$completedDays / $totalDays",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                    }
                    Text("🏆", fontSize = 24.sp)
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    "📊 Large goal: Progress shown as bar instead of dots",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.4f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        } else {
            // Full Journey View - Scrollable (only for challenges <= 100 days)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState)
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Start marker
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🚀", fontSize = 20.sp)
                    Text("Start", style = MaterialTheme.typography.labelSmall, color = PrimaryTeal, fontSize = 9.sp)
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // All days in the journey (safe since totalDays <= 100)
                repeat(totalDays) { index ->
                    val dayNumber = index + 1
                    val isCompleted = dayNumber <= completedDays
                    val isToday = dayNumber == completedDays + 1
                    val isMilestone = dayNumber in milestones
                    val isLastDay = dayNumber == totalDays
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Day circle with glow effect for completed
                        Box(
                            modifier = Modifier
                                .size(if (isMilestone || isLastDay) 36.dp else 28.dp)
                                .then(
                                    if (isCompleted) Modifier.background(
                                        color = PrimaryTeal.copy(alpha = glowAlpha * 0.3f),
                                        shape = CircleShape
                                    ) else Modifier
                                )
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isCompleted && isLastDay -> Brush.radialGradient(
                                            colors = listOf(Color(0xFFFFD700), Color(0xFFFFA500))
                                        )
                                        isCompleted && isMilestone -> Brush.radialGradient(
                                            colors = listOf(PrimaryTeal, NeonCyan)
                                        )
                                        isCompleted -> Brush.radialGradient(
                                            colors = listOf(PrimaryTeal.copy(alpha = 0.9f), PrimaryTeal.copy(alpha = 0.5f))
                                        )
                                        isToday -> Brush.radialGradient(
                                            colors = listOf(NeonCyan.copy(alpha = 0.4f), Color.Transparent)
                                        )
                                        else -> Brush.radialGradient(
                                            colors = listOf(Color.White.copy(alpha = 0.08f), Color.Transparent)
                                        )
                                    }
                                )
                                .then(
                                    if (isToday) Modifier.border(2.dp, NeonCyan, CircleShape)
                                    else if (isLastDay && !isCompleted) Modifier.border(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f), CircleShape)
                                    else if (isCompleted && isMilestone) Modifier.border(1.dp, PrimaryTeal.copy(alpha = glowAlpha), CircleShape)
                                    else Modifier
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            when {
                                isCompleted && isLastDay -> Text("🏆", fontSize = if (isMilestone) 18.sp else 14.sp)
                                isCompleted && isMilestone -> Text("⭐", fontSize = 16.sp)
                                isCompleted -> Text("🔥", fontSize = if (isMilestone) 16.sp else 12.sp)
                                isLastDay -> Text("🏆", fontSize = 14.sp, modifier = Modifier.alpha(0.5f))
                                isToday -> Text("👆", fontSize = 12.sp)
                                else -> Text(
                                    "○",
                                    fontSize = if (isMilestone) 14.sp else 10.sp,
                                    color = Color.White.copy(alpha = 0.15f)
                                )
                            }
                        }
                        
                        // Day number (show for milestones, first, last, today, or every 5th day)
                        if (isMilestone || isLastDay || dayNumber == 1 || isToday || dayNumber % 5 == 0) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "$dayNumber",
                                style = MaterialTheme.typography.labelSmall,
                                color = when {
                                    isCompleted -> PrimaryTeal
                                    isToday -> NeonCyan
                                    isLastDay -> Color(0xFFFFD700)
                                    else -> Color.White.copy(alpha = 0.4f)
                                },
                                fontWeight = if (isMilestone || isLastDay || isToday) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 8.sp
                            )
                        }
                    }
                    
                    // Connector line with pulsing glow
                    if (index < totalDays - 1) {
                        Box(
                            modifier = Modifier
                                .width(if (isMilestone) 12.dp else 8.dp)
                                .height(2.dp)
                                .background(
                                    if (isCompleted) PrimaryTeal.copy(alpha = 0.2f + glowAlpha * 0.4f)
                                    else Color.White.copy(alpha = 0.1f)
                                )
                                .then(
                                    if (isCompleted) Modifier.background(
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(PrimaryTeal.copy(alpha = glowAlpha), NeonCyan.copy(alpha = glowAlpha))
                                        )
                                    ) else Modifier
                                )
                        )
                    }
                }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // End marker
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🎯", fontSize = 20.sp)
                Text("Goal", style = MaterialTheme.typography.labelSmall, color = Color(0xFFFFD700), fontSize = 9.sp)
            }
        }
        
        // Scroll hint if many days
        if (totalDays > 10) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    "← Scroll to see full journey →",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 10.sp
                )
            }
        }
        
        // Streak Info
        if (completedDays > 0) {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = PrimaryTeal.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🔥", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "$completedDays Day Streak!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = PrimaryTeal,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${totalDays - completedDays} days remaining",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }
                
                // Next milestone
                val nextMilestone = milestones.filter { it > completedDays }.minOrNull()
                if (nextMilestone != null && nextMilestone <= totalDays) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "Next: ⭐ Day $nextMilestone",
                            style = MaterialTheme.typography.labelSmall,
                            color = NeonCyan,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "${nextMilestone - completedDays} to go",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
        }
    }
}

@Composable
fun StatItem(
    value: String,
    label: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Black
            ),
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.5f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddChallengeDialog(
    onAdd: (String, Int, String, String?) -> Unit,
    onDismiss: () -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    var inputMode by rememberSaveable { mutableIntStateOf(0) } // 0: Total Days, 1: Target Date
    var targetDaysInput by rememberSaveable { mutableStateOf("") }
    
    // For date mode
    var targetDateInput by rememberSaveable { mutableStateOf("") } // e.g., "2025-01-15"
    
    var selectedIcon by rememberSaveable { mutableStateOf("\uD83D\uDCDA") }
    
    // Reminder states
    var reminderEnabled by rememberSaveable { mutableStateOf(false) }
    var reminderHour by rememberSaveable { mutableIntStateOf(8) }
    var reminderMinute by rememberSaveable { mutableIntStateOf(0) }
    var showTimePicker by rememberSaveable { mutableStateOf(false) }

    val normalizedName = remember(name) { name.trim() }
    val parsedTargetDate = remember(targetDateInput) {
        runCatching { LocalDate.parse(targetDateInput) }.getOrNull()
    }
    val dateInputError = remember(inputMode, targetDateInput, parsedTargetDate) {
        if (inputMode != 1 || targetDateInput.isBlank()) {
            null
        } else {
            when {
                targetDateInput.length < 10 -> "Use full format: YYYY-MM-DD"
                parsedTargetDate == null -> "Invalid date. Example: 2026-03-15"
                !parsedTargetDate.isAfter(LocalDate.now()) -> "Target date should be in the future"
                else -> null
            }
        }
    }

    val calculatedDays = remember(inputMode, targetDaysInput, targetDateInput) {
        if (inputMode == 0) {
            (targetDaysInput.toIntOrNull() ?: 0).coerceIn(0, 3650)
        } else {
            try {
                val date = LocalDate.parse(targetDateInput)
                ChronoUnit.DAYS.between(LocalDate.now(), date).toInt().coerceAtLeast(0)
            } catch (e: Exception) {
                0
            }
        }
    }
    
    val reminderTimeString = remember(reminderEnabled, reminderHour, reminderMinute) {
        if (reminderEnabled) String.format("%02d:%02d", reminderHour, reminderMinute) else null
    }
    val canCreate = remember(normalizedName, calculatedDays, dateInputError) {
        normalizedName.isNotEmpty() && calculatedDays > 0 && dateInputError == null
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkBackground,
        title = {
            Text(
                "Create Study Challenge",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                // Challenge name
                Text(
                    "Challenge Name",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                BasicTextField(
                    value = name,
                    onValueChange = { name = it.replace("\n", "").take(48) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.1f))
                        .padding(12.dp),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.White),
                    cursorBrush = SolidColor(PrimaryTeal),
                    decorationBox = { innerTextField ->
                        if (name.isEmpty()) {
                            Text("e.g. Board Exam Prep", color = Color.White.copy(alpha = 0.3f))
                        }
                        innerTextField()
                    }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Mode Selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (inputMode == 0) PrimaryTeal else Color.Transparent)
                            .clickable { 
                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                inputMode = 0 
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Total Days", 
                            color = if (inputMode == 0) Color.Black else Color.White,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (inputMode == 1) PrimaryTeal else Color.Transparent)
                            .clickable { 
                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                inputMode = 1 
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Target Date", 
                            color = if (inputMode == 1) Color.Black else Color.White,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (inputMode == 0) {
                    // Target days
                    Text(
                        "How many days to study?",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    BasicTextField(
                        value = targetDaysInput,
                        onValueChange = { targetDaysInput = it.filter { c -> c.isDigit() }.take(4) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.1f))
                            .padding(12.dp),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.White),
                        cursorBrush = SolidColor(PrimaryTeal),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        decorationBox = { innerTextField ->
                            if (targetDaysInput.isEmpty()) {
                                Text("e.g. 46", color = Color.White.copy(alpha = 0.3f))
                            }
                            innerTextField()
                        }
                    )
                    if (targetDaysInput.isNotBlank() && calculatedDays == 0) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Enter at least 1 day",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFFF8A80)
                        )
                    }
                } else {
                    // Target date
                    Text(
                        "When is your exam? (YYYY-MM-DD)",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    BasicTextField(
                        value = targetDateInput,
                        onValueChange = { targetDateInput = it.filter { c -> c.isDigit() || c == '-' }.take(10) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.1f))
                            .padding(12.dp),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.White),
                        cursorBrush = SolidColor(PrimaryTeal),
                        decorationBox = { innerTextField ->
                            if (targetDateInput.isEmpty()) {
                                Text("e.g. 2025-01-25", color = Color.White.copy(alpha = 0.3f))
                            }
                            innerTextField()
                        }
                    )
                    if (dateInputError != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            dateInputError,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFFF8A80)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Icon selector
                Text(
                    "Choose Icon",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    challengeIcons.take(6).forEach { icon ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (icon == selectedIcon) PrimaryTeal.copy(alpha = 0.3f)
                                    else Color.White.copy(alpha = 0.1f)
                                )
                                .border(
                                    width = if (icon == selectedIcon) 2.dp else 0.dp,
                                    color = if (icon == selectedIcon) PrimaryTeal else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { 
                                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                    selectedIcon = icon 
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = icon, fontSize = 20.sp)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    challengeIcons.drop(6).forEach { icon ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (icon == selectedIcon) PrimaryTeal.copy(alpha = 0.3f)
                                    else Color.White.copy(alpha = 0.1f)
                                )
                                .border(
                                    width = if (icon == selectedIcon) 2.dp else 0.dp,
                                    color = if (icon == selectedIcon) PrimaryTeal else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { 
                                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                    selectedIcon = icon 
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = icon, fontSize = 20.sp)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // ═══════════════════════════════════════════════════════════════
                // DAILY REMINDER SECTION
                // ═══════════════════════════════════════════════════════════════
                Text(
                    "Daily Reminder",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                ) {
                    Column {
                        // Toggle row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("⏰", fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    "Enable Reminder",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White
                                )
                            }
                            Switch(
                                checked = reminderEnabled,
                                onCheckedChange = { 
                                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                    reminderEnabled = it 
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = PrimaryTeal,
                                    checkedTrackColor = PrimaryTeal.copy(alpha = 0.3f),
                                    uncheckedThumbColor = Color.White.copy(alpha = 0.5f),
                                    uncheckedTrackColor = Color.White.copy(alpha = 0.1f)
                                )
                            )
                        }
                        
                        // Time picker (visible when enabled)
                        AnimatedVisibility(
                            visible = reminderEnabled,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showTimePicker = true }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Reminder Time",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.6f)
                                )
                                
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(PrimaryTeal.copy(alpha = 0.2f))
                                        .border(1.dp, PrimaryTeal.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        String.format("%02d:%02d", reminderHour, reminderMinute),
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = PrimaryTeal
                                    )
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Preview
                if (canCreate) {

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(PrimaryTeal.copy(alpha = 0.1f))
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                text = "$selectedIcon $normalizedName - Study for $calculatedDays days!",
                                color = PrimaryTeal,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            if (reminderEnabled) {
                                Text(
                                    text = "⏰ Daily reminder at ${String.format("%02d:%02d", reminderHour, reminderMinute)}",
                                    color = PrimaryTeal.copy(alpha = 0.7f),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                    if (canCreate) {
                        onAdd(normalizedName, calculatedDays, selectedIcon, reminderTimeString)
                    }
                },
                enabled = canCreate,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryTeal,
                    contentColor = Color.Black
                )
            ) {
                Text("Start Challenge 🚀")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.White.copy(alpha = 0.5f))
            }
        }
    )
    
    // Time Picker Dialog
    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = reminderHour,
            initialMinute = reminderMinute
        )
        
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            containerColor = Color(0xFF151520),
            title = {
                Text(
                    "Set Reminder Time",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            },
            text = {
                TimePicker(
                    state = timePickerState,
                    colors = TimePickerDefaults.colors(
                        clockDialColor = Color.White.copy(alpha = 0.05f),
                        selectorColor = PrimaryTeal,
                        containerColor = Color.Transparent,
                        clockDialSelectedContentColor = Color.Black,
                        clockDialUnselectedContentColor = Color.White.copy(alpha = 0.7f),
                        periodSelectorSelectedContainerColor = PrimaryTeal,
                        periodSelectorUnselectedContainerColor = Color.White.copy(alpha = 0.1f),
                        periodSelectorSelectedContentColor = Color.Black,
                        periodSelectorUnselectedContentColor = Color.White.copy(alpha = 0.5f),
                        timeSelectorSelectedContainerColor = PrimaryTeal,
                        timeSelectorUnselectedContainerColor = Color.White.copy(alpha = 0.1f),
                        timeSelectorSelectedContentColor = Color.Black,
                        timeSelectorUnselectedContentColor = Color.White.copy(alpha = 0.7f)
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        reminderHour = timePickerState.hour
                        reminderMinute = timePickerState.minute
                        showTimePicker = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryTeal,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("SET", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black))
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("CANCEL", color = Color.White.copy(alpha = 0.5f))
                }
            }
        )
    }
}

