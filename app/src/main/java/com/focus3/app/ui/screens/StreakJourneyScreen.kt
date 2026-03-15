package com.focus3.app.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import java.time.LocalDate
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focus3.app.data.model.Challenge
import com.focus3.app.ui.components.GlassBox
import com.focus3.app.ui.theme.DarkBackground
import com.focus3.app.ui.theme.NeonCyan
import com.focus3.app.ui.theme.PrimaryTeal

data class StreakMilestone(
    val days: Int,
    val title: String,
    val icon: String,
    val description: String,
    val isUnlocked: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StreakJourneyScreen(
    streak: Int,
    customMilestones: List<CustomMilestone> = emptyList(),
    challenge: Challenge? = null,
    onAddCustomClick: () -> Unit,
    onDeleteChallenge: (Int) -> Unit,
    onCompleteChallenge: () -> Unit,
    onBack: () -> Unit
) {
    val effectiveStreak = remember(streak, challenge) {
        val base = (challenge?.currentStreak ?: streak).coerceAtLeast(0)
        if (challenge != null) base.coerceAtMost(challenge.targetDays.coerceAtLeast(1)) else base
    }
    val todayString = remember { LocalDate.now().toString() }
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    
    // Generate the full journey path
    val allMilestones = remember(effectiveStreak, customMilestones, challenge) {
        if (challenge != null) {
            // SHOW FULL JOURNEY FROM 1 TO TARGET DAYS
            (1..challenge.targetDays).map { day ->
                StreakMilestone(
                    days = day,
                    title = "Day $day",
                    icon = if (effectiveStreak >= day) "🔥" else "○",
                    description = "Day $day of ${challenge.targetDays}",
                    isUnlocked = effectiveStreak >= day
                )
            }
        } else {
            // For regular streak - show first 7 days as individual fires + custom milestones
            val displayMilestones = (1..7).map { day ->
                StreakMilestone(
                    days = day,
                    title = "Day $day",
                    icon = if (streak >= day) "🔥" else "○",
                    description = "Day $day",
                    isUnlocked = streak >= day
                )
            }
            val merged = (displayMilestones + customMilestones.map { 
                StreakMilestone(it.days, it.title, it.icon, "${it.days} days custom!", effectiveStreak >= it.days)
            }).sortedBy { it.days }
            merged.distinctBy { it.days }
        }
    }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Column(modifier = Modifier.padding(start = 8.dp)) {
                        Text(
                            if (challenge != null) "ELITE MISSION" else "JOURNEY ARCHIVE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.5.sp
                            ),
                            color = PrimaryTeal
                        )
                        Text(
                            if (challenge != null) challenge.name.uppercase() else "STREAK PROGRESSION",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = (-0.5).sp
                            ),
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    if (challenge != null) {
                        IconButton(onClick = { 
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                            onDeleteChallenge(challenge.id) 
                        }) {
                            Icon(
                                Icons.Default.Delete, 
                                contentDescription = "Delete",
                                tint = Color.White.copy(alpha = 0.3f)
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            if (challenge != null) {
                val isCompletedToday = challenge.lastCompletedDate == todayString
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(24.dp)
                ) {
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                            onCompleteChallenge()
                        },
                        enabled = !isCompletedToday,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isCompletedToday) Color.White.copy(alpha = 0.05f) else PrimaryTeal,
                            contentColor = if (isCompletedToday) Color.White.copy(alpha = 0.3f) else Color.Black,
                            disabledContainerColor = Color.White.copy(alpha = 0.05f),
                            disabledContentColor = Color.White.copy(alpha = 0.2f)
                        ),
                        shape = RoundedCornerShape(20.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isCompletedToday) {
                                Text("✓ MISSION SECURED FOR TODAY", fontWeight = FontWeight.Black)
                            } else {
                                Text("AUTHORIZE MISSION COMPLETION", fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }
        },
        containerColor = DarkBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                item(key = "journey_header") {
                    if (challenge != null) {
                        ChallengeJourneyHeader(challenge, effectiveStreak)
                    } else {
                        CurrentStreakCard(effectiveStreak)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                item(key = "journey_daily_fires") {
                    // Daily fire streak display
                    DailyStreakFires(
                        totalDays = challenge?.targetDays ?: 30,
                        completedDays = effectiveStreak
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
                
                itemsIndexed(
                    items = allMilestones,
                    key = { _, milestone -> milestone.days },
                    contentType = { _, milestone ->
                        if (milestone.isUnlocked) "milestone_unlocked" else "milestone_locked"
                    }
                ) { index, milestone ->
                    val isLeft = index % 2 != 0
                    val isToday = milestone.days == (effectiveStreak + 1)
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        contentAlignment = if (isLeft) Alignment.CenterStart else Alignment.CenterEnd
                    ) {
                        MilestoneNode(
                            milestone = milestone,
                            isFirst = index == 0,
                            isLast = index == allMilestones.lastIndex,
                            isToday = isToday
                        )
                    }
                    
                    if (index < allMilestones.lastIndex) {
                        JourneyConnectorCurve(
                            isLeftToRight = isLeft,
                            isCompleted = milestone.isUnlocked && allMilestones[index + 1].isUnlocked
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChallengeJourneyHeader(challenge: Challenge, streak: Int) {
    val progress = ((streak.toFloat() / challenge.targetDays.coerceAtLeast(1)) * 100)
        .toInt()
        .coerceIn(0, 100)
    
    GlassBox(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        cornerRadius = 24.dp,
        showGlow = streak > 0
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "TARGET STATUS",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 1.sp),
                    color = PrimaryTeal.copy(alpha = 0.6f)
                )
                Text(
                    text = "DAY $streak OF ${challenge.targetDays}",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                    color = Color.White
                )
            }
            
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(PrimaryTeal.copy(alpha = 0.1f))
                    .border(2.dp, PrimaryTeal.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$progress%",
                    color = PrimaryTeal,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black)
                )
            }
        }
    }
}

@Composable
fun JourneyConnectorCurve(isLeftToRight: Boolean, isCompleted: Boolean) {
    val color = if (isCompleted) PrimaryTeal else Color.White.copy(alpha = 0.05f)
    val pulseAlpha: Float = if (isCompleted) {
        val infiniteTransition = rememberInfiniteTransition(label = "pathGlow")
        val animatedPulseAlpha by infiniteTransition.animateFloat(
            initialValue = 0.1f,
            targetValue = 0.5f,
            animationSpec = infiniteRepeatable(
                animation = tween(1500, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulseAlpha"
        )
        animatedPulseAlpha
    } else {
        0.1f
    }

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .padding(horizontal = 60.dp)
    ) {
        val path = Path().apply {
            if (isLeftToRight) {
                moveTo(0f, 0f)
                cubicTo(
                    x1 = 0f, y1 = size.height * 0.5f,
                    x2 = size.width, y2 = size.height * 0.5f,
                    x3 = size.width, y3 = size.height
                )
            } else {
                moveTo(size.width, 0f)
                cubicTo(
                    x1 = size.width, y1 = size.height * 0.5f,
                    x2 = 0f, y2 = size.height * 0.5f,
                    x3 = 0f, y3 = size.height
                )
            }
        }
        
        // Background Path
        drawPath(
            path = path,
            color = Color.White.copy(alpha = 0.05f),
            style = Stroke(width = 4f, cap = StrokeCap.Round)
        )
        
        if (isCompleted) {
            // Glow
            drawPath(
                path = path,
                color = PrimaryTeal.copy(alpha = pulseAlpha),
                style = Stroke(width = 16f + (pulseAlpha * 20f), cap = StrokeCap.Round)
            )
            // Stroke
            drawPath(
                path = path,
                color = color,
                style = Stroke(width = 6f, cap = StrokeCap.Round)
            )
        }
    }
}

@Composable
fun CurrentStreakCard(streak: Int) {
    GlassBox(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        cornerRadius = 32.dp,
        showGlow = streak > 0
    ) {
        Column(
            modifier = Modifier.padding(32.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(if (streak > 0) PrimaryTeal.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.05f))
                    .border(2.dp, if (streak > 0) PrimaryTeal.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (streak > 0) "🔥" else "🧊",
                    fontSize = 40.sp
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "$streak",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-2).sp
                ),
                color = if (streak > 0) PrimaryTeal else Color.White
            )
            Text(
                text = "CONSECUTIVE DAYS",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black, letterSpacing = 2.sp),
                color = Color.White.copy(alpha = 0.4f)
            )
        }
    }
}

@Composable
fun MilestoneNode(
    milestone: StreakMilestone,
    isFirst: Boolean,
    isLast: Boolean,
    isToday: Boolean = false
) {
    val shouldAnimate = milestone.isUnlocked || isToday
    val glowScale: Float
    val glowAlpha: Float
    if (shouldAnimate) {
        val infiniteTransition = rememberInfiniteTransition(label = "nodeGlow")
        val animatedGlowScale by infiniteTransition.animateFloat(
            initialValue = 1.0f,
            targetValue = 1.15f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "glowScale"
        )
        val animatedGlowAlpha by infiniteTransition.animateFloat(
            initialValue = 0.2f,
            targetValue = 0.5f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "glowAlpha"
        )
        glowScale = animatedGlowScale
        glowAlpha = animatedGlowAlpha
    } else {
        glowScale = 1f
        glowAlpha = 0.2f
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if (isFirst) {
            Text(
                "ORIGIN",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 1.sp),
                color = NeonCyan,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }
        
        Box(
            modifier = Modifier.size(90.dp),
            contentAlignment = Alignment.Center
        ) {
            if (milestone.isUnlocked || isToday) {
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .scale(glowScale)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    if (isToday) NeonCyan.copy(alpha = glowAlpha) else PrimaryTeal.copy(alpha = glowAlpha),
                                    Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        )
                )
            }
            
            Canvas(modifier = Modifier.fillMaxSize()) {
                val path = createHexagonPath(size.width, size.height)
                
                // Base
                drawPath(
                    path = path,
                    color = when {
                        isToday -> NeonCyan.copy(alpha = 0.2f)
                        milestone.isUnlocked -> PrimaryTeal.copy(alpha = 0.15f)
                        else -> Color.White.copy(alpha = 0.03f)
                    }
                )
                
                // Border with shadow/glow
                drawPath(
                    path = path,
                    color = when {
                        isToday -> NeonCyan
                        milestone.isUnlocked -> PrimaryTeal
                        else -> Color.White.copy(alpha = 0.1f)
                    },
                    style = Stroke(width = if (milestone.isUnlocked || isToday) 6f else 2f)
                )
                
                if (milestone.isUnlocked || isToday) {
                    drawPath(
                        path = path,
                        color = if (isToday) NeonCyan.copy(alpha = 0.3f) else PrimaryTeal.copy(alpha = 0.3f),
                        style = Stroke(width = 16f)
                    )
                }
            }
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (isToday) {
                    Text("📍", fontSize = 24.sp)
                } else {
                    Text(
                        text = if (milestone.isUnlocked) "🔥" else "${milestone.days}",
                        style = if (milestone.isUnlocked) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                        color = if (milestone.isUnlocked) Color.White else Color.White.copy(alpha = 0.2f)
                    )
                }
            }
        }
        
        if (isLast && !isFirst) {
            Text(
                "DESTINATION",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 1.sp),
                color = PrimaryTeal,
                modifier = Modifier.padding(top = 12.dp)
            )
        }
    }
}

// Connector logic moved to JourneyConnectorCurve

// End of file

fun createHexagonPath(width: Float, height: Float): Path {
    val path = Path()
    val centerX = width / 2
    val centerY = height / 2
    val radius = minOf(width, height) / 2 * 0.9f
    
    for (i in 0..5) {
        val angle = Math.PI / 3 * i - Math.PI / 2
        val x = centerX + radius * kotlin.math.cos(angle).toFloat()
        val y = centerY + radius * kotlin.math.sin(angle).toFloat()
        
        if (i == 0) {
            path.moveTo(x, y)
        } else {
            path.lineTo(x, y)
        }
    }
    path.close()
    return path
}

fun getMilestoneAfter(days: Int, allMilestones: List<StreakMilestone>): Int {
    val allDays = allMilestones.map { it.days }
    val index = allDays.indexOf(days)
    return if (index >= 0 && index < allDays.lastIndex) allDays[index + 1] else allDays.last() + 1
}

@Composable
fun DailyStreakFires(
    totalDays: Int,
    completedDays: Int
) {
    // Show 7 days centered around current progress
    val displayDays = minOf(totalDays, 7)
    val startDay = maxOf(0, completedDays - 3).coerceAtMost(maxOf(0, totalDays - displayDays))
    
    // Run glow animation only when there is active progress
    val glowAlpha: Float = if (completedDays > 0) {
        val infiniteTransition = rememberInfiniteTransition(label = "fireGlow")
        val animatedGlowAlpha by infiniteTransition.animateFloat(
            initialValue = 0.3f,
            targetValue = 0.7f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "glowAlpha"
        )
        animatedGlowAlpha
    } else {
        0.3f
    }

    GlassBox(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        cornerRadius = 24.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "DAILY PROGRESSION",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 1.5.sp),
                color = Color.White.copy(alpha = 0.3f)
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(displayDays) { index ->
                    val dayNumber = startDay + index + 1
                    val isCompleted = dayNumber <= completedDays
                    val isNext = dayNumber == completedDays + 1
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    when {
                                        isCompleted -> PrimaryTeal.copy(alpha = 0.1f + glowAlpha * 0.1f)
                                        isNext -> NeonCyan.copy(alpha = 0.05f)
                                        else -> Color.White.copy(alpha = 0.02f)
                                    }
                                )
                                .border(
                                    if (isCompleted) 2.dp else 1.dp,
                                    when {
                                        isCompleted -> PrimaryTeal.copy(alpha = glowAlpha)
                                        isNext -> NeonCyan.copy(alpha = 0.3f)
                                        else -> Color.White.copy(alpha = 0.05f)
                                    },
                                    RoundedCornerShape(14.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isCompleted) {
                                Text("🔥", fontSize = 22.sp)
                            } else {
                                Text(
                                    "$dayNumber",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                                    color = if (isNext) NeonCyan else Color.White.copy(alpha = 0.1f)
                                )
                            }
                        }
                    }
                }
            }
            
            if (completedDays > 0) {
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(PrimaryTeal.copy(alpha = 0.1f))
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🔥", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "$completedDays DAY STREAK ACTIVE",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                        color = PrimaryTeal
                    )
                }
            }
        }
    }
}
