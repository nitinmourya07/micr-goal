package com.focus3.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focus3.app.data.model.DailyTask
import com.focus3.app.ui.components.GlassBox
import com.focus3.app.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    history: Map<String, Int>,
    allTasks: List<DailyTask>,
    streak: Int,
    onBack: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    
    val sortedDates = remember(history) {
        history.keys.sortedDescending().take(7)
    }
    val tasksByDate = remember(allTasks) { allTasks.groupBy { it.date } }
    val hasHistory = remember(history) { history.isNotEmpty() }

    val today = remember { LocalDate.now() }
    
    // Memoized statistical calculations
    val (completionRate, perfectDays, totalMissions) = remember(history) {
        val missions = history.values.sum()
        val possible = history.size * 3
        val rate = if (possible > 0) (missions.toFloat() / possible * 100).toInt() else 0
        val perfect = history.values.count { it == 3 }
        Triple(rate, perfect, missions)
    }
    
    // ═══════════════════════════════════════════════════════════════
    // ANIMATIONS
    // ═══════════════════════════════════════════════════════════════
    val infiniteTransition = rememberInfiniteTransition(label = "analytics")
    
    // FAST floating particles
    val particleAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particles"
    )
    
    // Header icon rotation
    val iconRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "iconRotation"
    )
    
    // FAST streak fire animation
    val fireScale: Float = if (streak > 0) {
        val animatedFireScale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.15f,
            animationSpec = infiniteRepeatable(
                animation = tween(350, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "fireScale"
        )
        animatedFireScale
    } else {
        1f
    }
    
    // FAST glow animation
    val glowIntensity by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowIntensity"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Lightweight background accent — reduced particle count & radius
        Canvas(modifier = Modifier.fillMaxSize()) {
            val colors = listOf(
                PrimaryTeal.copy(alpha = 0.02f),
                NeonCyan.copy(alpha = 0.015f),
                PremiumGold.copy(alpha = 0.015f)
            )
            repeat(4) { i ->
                val angle = (particleAngle + i * 90) * (Math.PI / 180)
                val radius = 160f + (i * 40f)
                val x = (size.width / 2) + cos(angle).toFloat() * radius
                val y = (size.height / 4) + sin(angle).toFloat() * radius * 0.7f
                drawCircle(
                    color = colors[i % colors.size],
                    radius = 70f + (i * 10f),
                    center = Offset(x, y)
                )
            }
        }
        
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ═══════════════════════════════════════════════════════════════
            // PREMIUM HEADER
            // ═══════════════════════════════════════════════════════════════
            item(key = "analytics_header") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(top = 16.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Back button with circular background
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.05f))
                                .clickable { 
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onBack() 
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack, 
                                contentDescription = "Back", 
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column {
                            Text(
                                "INTELLIGENCE HUB",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 2.sp
                                ),
                                color = PrimaryTeal
                            )
                            Text(
                                "Analytics",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = (-0.5).sp
                                ),
                                color = Color.White
                            )
                        }
                    }
                    
                    // Animated Analytics Icon with glow
                    Box(contentAlignment = Alignment.Center) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .blur(12.dp)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            PrimaryTeal.copy(alpha = glowIntensity * 0.4f),
                                            Color.Transparent
                                        )
                                    ),
                                    CircleShape
                                )
                        )
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            PrimaryTeal.copy(alpha = 0.2f),
                                            PrimaryTeal.copy(alpha = 0.05f)
                                        )
                                    )
                                )
                                .border(
                                    1.dp,
                                    Brush.linearGradient(
                                        colors = listOf(
                                            PrimaryTeal.copy(alpha = 0.5f),
                                            NeonCyan.copy(alpha = 0.2f)
                                        )
                                    ),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Analytics,
                                contentDescription = "Analytics",
                                tint = PrimaryTeal,
                                modifier = Modifier
                                    .size(24.dp)
                                    .graphicsLayer { rotationZ = iconRotation * 0.1f }
                            )
                        }
                    }
                }
            }

            // ═══════════════════════════════════════════════════════════════
            // HERO STATS CARD
            // ═══════════════════════════════════════════════════════════════
            item(key = "hero_stats") {
                PremiumHeroStatsCard(
                    streak = streak,
                    completionRate = completionRate,
                    perfectDays = perfectDays,
                    totalMissions = totalMissions,
                    fireScale = fireScale,
                    glowIntensity = glowIntensity
                )
            }

            // ═══════════════════════════════════════════════════════════════
            // WEEKLY CHART SECTION
            // ═══════════════════════════════════════════════════════════════
            item(key = "weekly_performance") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("📊", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        "WEEKLY PERFORMANCE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.5.sp
                        ),
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                PremiumWeeklyChart(history)
            }

            if (!hasHistory) {
                item(key = "analytics_empty_state") {
                    GlassBox(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 20.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("📭", fontSize = 28.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "No analytics data yet",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Complete daily missions to unlock trends and activity history.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.55f)
                            )
                        }
                    }
                }
            }

            // ═══════════════════════════════════════════════════════════════
            // RECENT ACTIVITY SECTION
            // ═══════════════════════════════════════════════════════════════
            item(key = "recent_activity_header") {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("📋", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        "RECENT ACTIVITY",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.5.sp
                        ),
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
            }

            // Recent Activity List
            if (sortedDates.isNotEmpty()) {
                items(
                    items = sortedDates,
                    key = { it },
                    contentType = { "daily_progress_row" }
                ) { dateStr ->
                    val count = history[dateStr] ?: 0
                    val tasksForDate = tasksByDate[dateStr].orEmpty().take(3)
                    PremiumActivityItem(dateStr, count, tasksForDate)
                }
            }
            
            item(key = "analytics_bottom_spacer") { Spacer(modifier = Modifier.height(100.dp)) }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// PREMIUM HERO STATS CARD
// ═══════════════════════════════════════════════════════════════════════════════
@Composable
private fun PremiumHeroStatsCard(
    streak: Int, 
    completionRate: Int, 
    perfectDays: Int, 
    totalMissions: Int,
    fireScale: Float,
    glowIntensity: Float
) {
    Box {
        // Glow behind card for active streak
        if (streak > 0) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .blur(30.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                StreakOrange.copy(alpha = glowIntensity * 0.3f),
                                Color.Transparent
                            )
                        ),
                        RoundedCornerShape(28.dp)
                    )
            )
        }
        
        GlassBox(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 28.dp,
            showGlow = streak > 0
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Streak Display - Hero
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Animated Fire
                    Box(
                        modifier = Modifier
                            .scale(fireScale)
                            .padding(end = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (streak > 0) {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .blur(15.dp)
                                    .background(StreakOrange.copy(alpha = 0.4f), CircleShape)
                            )
                        }
                        Text("🔥", fontSize = 52.sp)
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                "$streak",
                                style = MaterialTheme.typography.displayLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 60.sp
                                ),
                                color = if (streak > 0) StreakOrange else Color.White
                            )
                            Text(
                                " DAYS",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Black
                                ),
                                color = PrimaryTeal.copy(alpha = 0.7f),
                                modifier = Modifier.padding(bottom = 14.dp)
                            )
                        }
                        Text(
                            "ACTIVE STREAK",
                            style = MaterialTheme.typography.labelMedium.copy(
                                letterSpacing = 2.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(28.dp))
                
                // Gradient Divider
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.White.copy(alpha = 0.15f),
                                    Color.Transparent
                                )
                            )
                        )
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Stats Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    PremiumStatItem(
                        value = "$completionRate%", 
                        label = "EFFICIENCY",
                        color = NeonCyan
                    )
                    
                    // Vertical divider
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(50.dp)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.White.copy(alpha = 0.1f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                    
                    PremiumStatItem(
                        value = "$perfectDays", 
                        label = "PERFECT",
                        color = PremiumGold
                    )
                    
                    // Vertical divider
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(50.dp)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.White.copy(alpha = 0.1f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                    
                    PremiumStatItem(
                        value = "$totalMissions", 
                        label = "MISSIONS",
                        color = SuccessColor
                    )
                }
            }
        }
    }
}

@Composable
private fun PremiumStatItem(value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Black
            ),
            color = color
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            ),
            color = Color.White.copy(alpha = 0.4f)
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// PREMIUM WEEKLY CHART
// ═══════════════════════════════════════════════════════════════════════════════
@Composable
private fun PremiumWeeklyChart(history: Map<String, Int>) {
    val today = remember { LocalDate.now() }
    var selectedIndex by rememberSaveable { mutableStateOf(-1) }
    val haptic = LocalHapticFeedback.current
    val tooltipDateFormatter = remember { DateTimeFormatter.ofPattern("MMM d") }
    
    val weeklyData = remember(history, today) {
        (0..6).map { i ->
            val date = today.minusDays(i.toLong())
            val dateStr = date.toString()
            date to (history[dateStr] ?: 0)
        }.reversed()
    }

    GlassBox(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 24.dp
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            // Tooltip Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (selectedIndex != -1) {
                    val (date, count) = weeklyData[selectedIndex]
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(PrimaryTeal.copy(alpha = 0.15f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                date.format(tooltipDateFormatter),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Black
                                ),
                                color = PrimaryTeal
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            "$count/3 Completed",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White.copy(alpha = 0.6f)
                        )
                        if (count == 3) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("⭐", fontSize = 14.sp)
                        }
                    }
                } else {
                    Text(
                        "TAP A BAR TO VIEW DETAILS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White.copy(alpha = 0.3f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Bars
            Row(
                modifier = Modifier
                    .height(130.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                weeklyData.forEachIndexed { index, (date, count) ->
                    val isSelected = selectedIndex == index
                    val isPerfect = count == 3
                    val heightFraction = (count / 3f).coerceAtLeast(0.08f)
                    
                    val animatedHeight by animateFloatAsState(
                        targetValue = heightFraction,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "bar"
                    )
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { 
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                selectedIndex = if (isSelected) -1 else index 
                            }
                    ) {
                        // Perfect day indicator
                        if (isPerfect) {
                            Text(
                                "⭐",
                                fontSize = 12.sp,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                        
                        // The Bar with glow
                        Box(contentAlignment = Alignment.BottomCenter) {
                            // Glow for perfect days
                            if (isPerfect) {
                                Box(
                                    modifier = Modifier
                                        .width(28.dp)
                                        .fillMaxHeight(animatedHeight)
                                        .blur(12.dp)
                                        .background(
                                            PrimaryTeal.copy(alpha = 0.3f),
                                            RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp)
                                        )
                                )
                            }
                            
                            Box(
                                modifier = Modifier
                                    .width(if (isSelected) 28.dp else 20.dp)
                                    .fillMaxHeight(animatedHeight)
                                    .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                                    .background(
                                        Brush.verticalGradient(
                                            colors = when {
                                                isPerfect -> listOf(PrimaryTeal, NeonCyan)
                                                count > 0 -> listOf(
                                                    PrimaryTeal.copy(alpha = 0.7f), 
                                                    PrimaryTeal.copy(alpha = 0.3f)
                                                )
                                                else -> listOf(
                                                    Color.White.copy(alpha = 0.1f), 
                                                    Color.White.copy(alpha = 0.04f)
                                                )
                                            }
                                        )
                                    )
                                    .then(
                                        if (isSelected) Modifier.border(
                                            1.dp, 
                                            Color.White.copy(alpha = 0.3f),
                                            RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp)
                                        ) else Modifier
                                    )
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        // Label
                        Text(
                            date.dayOfWeek.name.take(1),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium
                            ),
                            color = if (isSelected) PrimaryTeal else Color.White.copy(alpha = 0.4f)
                        )
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// PREMIUM ACTIVITY ITEM
// ═══════════════════════════════════════════════════════════════════════════════
@Composable
private fun PremiumActivityItem(
    dateStr: String, 
    completedCount: Int,
    tasksForDate: List<DailyTask> = emptyList()
) {
    var isExpanded by rememberSaveable(dateStr) { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    val dateFormatter = remember { DateTimeFormatter.ISO_LOCAL_DATE }
    val displayDateFormatter = remember { DateTimeFormatter.ofPattern("EEE, MMM d") }
    val today = remember { LocalDate.now() }
    val date = remember(dateStr) { LocalDate.parse(dateStr, dateFormatter) }
    val displayDate = remember(date) { date.format(displayDateFormatter) }
    val isToday = date == today
    val isPerfect = completedCount == 3
    val expandArrowRotation by animateFloatAsState(
        targetValue = if (isExpanded && tasksForDate.isNotEmpty()) 180f else 0f,
        animationSpec = tween(180),
        label = "expandArrow"
    )
    
    GlassBox(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 18.dp,
        showGlow = isPerfect
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { 
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        isExpanded = !isExpanded 
                    }
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Day indicator
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isPerfect) 
                                    Brush.linearGradient(
                                        colors = listOf(
                                            PrimaryTeal.copy(alpha = 0.3f),
                                            NeonCyan.copy(alpha = 0.15f)
                                        )
                                    )
                                else 
                                    Brush.linearGradient(
                                        colors = listOf(
                                            Color.White.copy(alpha = 0.08f),
                                            Color.White.copy(alpha = 0.04f)
                                        )
                                    )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            if (isPerfect) "⭐" else if (isToday) "📅" else "📋",
                            fontSize = 20.sp
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(14.dp))
                    
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                if (isToday) "Today" else displayDate,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color.White
                            )
                            if (isToday) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(PrimaryTeal.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        "NOW",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Black,
                                            fontSize = 9.sp
                                        ),
                                        color = PrimaryTeal
                                    )
                                }
                            }
                        }
                        Text(
                            "$completedCount of 3 missions" + 
                                if (tasksForDate.isNotEmpty()) " • Tap to expand" else "",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isPerfect) PrimaryTeal.copy(alpha = 0.7f) 
                                   else Color.White.copy(alpha = 0.4f)
                        )
                    }
                }
                
                // Progress Dots + expand affordance
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        repeat(3) { i ->
                            Box(
                                modifier = Modifier
                                    .size(if (i < completedCount) 12.dp else 10.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (i < completedCount) 
                                            Brush.radialGradient(
                                                colors = listOf(PrimaryTeal, NeonCyan.copy(alpha = 0.7f))
                                            )
                                        else 
                                            Brush.radialGradient(
                                                colors = listOf(
                                                    Color.White.copy(alpha = 0.15f),
                                                    Color.White.copy(alpha = 0.08f)
                                                )
                                            )
                                    )
                            )
                        }
                    }
                    if (tasksForDate.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = if (isExpanded) "Collapse tasks" else "Expand tasks",
                            tint = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier
                                .size(18.dp)
                                .graphicsLayer { rotationZ = expandArrowRotation }
                        )
                    }
                }
            }
            
            // Expandable Task List
            AnimatedVisibility(
                visible = isExpanded && tasksForDate.isNotEmpty(),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp)
                        .padding(bottom = 18.dp)
                ) {
                    // Gradient Divider
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
                    
                    Spacer(modifier = Modifier.height(14.dp))
                    
                    tasksForDate.take(3).forEachIndexed { index, task ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                if (task.isCompleted) "✅" else "⭕",
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                task.content.ifBlank { "Goal ${index + 1}" },
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (task.isCompleted) FontWeight.Medium else FontWeight.Normal
                                ),
                                color = if (task.isCompleted) Color.White else Color.White.copy(alpha = 0.4f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}
