package com.focus3.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focus3.app.data.model.CalendarNote
import com.focus3.app.ui.theme.*
import com.focus3.app.ui.components.GlassBox
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

/**
 * Calendar Screen - Shows task completion history with visual calendar and notes
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    completionHistory: Map<String, Int>,
    calendarNotes: Map<String, CalendarNote>,
    currentStreak: Int,
    longestStreak: Int,
    onBack: () -> Unit,
    onSaveNote: (CalendarNote) -> Unit,
    onDeleteNote: (String) -> Unit,
    onDateClick: (LocalDate) -> Unit = {}
) {
    val today = remember { LocalDate.now() }
    val currentMonth = remember(today) { YearMonth.from(today) }
    var selectedMonth by remember { mutableStateOf(currentMonth) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var showNoteDialog by remember { mutableStateOf(false) }
    val monthTitleFormatter = remember { DateTimeFormatter.ofPattern("MMMM yyyy") }
    
    val daysInMonth = selectedMonth.lengthOfMonth()
    val firstDayOfMonth = selectedMonth.atDay(1)
    val startDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7 // Sunday = 0
    val totalPerfectDays = remember(completionHistory) { completionHistory.values.count { it == 3 } }
    val canGoNextMonth = remember(selectedMonth, currentMonth) { selectedMonth.isBefore(currentMonth) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(DarkBackground, DarkSurface)
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(16.dp)
        ) {
            // FAST Header animation
            item(key = "calendar_header") {
                val infiniteTransition = rememberInfiniteTransition(label = "calHeader")
                val headerGlow by infiniteTransition.animateFloat(
                    initialValue = 0.3f,
                    targetValue = 0.6f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2000, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "headerGlow"
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                        
                        // Animated Icon with glow
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            PrimaryTeal.copy(alpha = headerGlow),
                                            Color.Transparent
                                        )
                                    ),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("📅", fontSize = 26.sp)
                        }
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Column {
                            Text(
                                "TIME MACHINE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 2.sp
                                ),
                                color = PrimaryTeal
                            )
                            Text(
                                "CHRONICLES",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = (-0.5).sp
                                ),
                                color = Color.White
                            )
                        }
                    }
                    
                    // Total Goals Badge (Elite) with glow
                    Box(
                        modifier = Modifier
                            .size(width = 90.dp, height = 70.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        PrimaryTeal.copy(alpha = 0.15f),
                                        PrimaryTeal.copy(alpha = 0.05f)
                                    )
                                )
                            )
                            .border(1.dp, PrimaryTeal.copy(alpha = 0.3f), RoundedCornerShape(20.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("🏆", fontSize = 18.sp)
                            Text(
                                "$totalPerfectDays",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                                color = PrimaryTeal
                            )
                            Text(
                                "PERFECT DAYS",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 8.sp
                                ),
                                color = Color.White.copy(alpha = 0.5f),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            
            // Enhanced Streak Stats
            item(key = "calendar_streak_stats") {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    EnhancedStreakCard(
                        modifier = Modifier.weight(1f),
                        title = "CURRENT",
                        value = "$currentStreak",
                        subtitle = "DAYS",
                        emoji = "🔥",
                        color = Color(0xFFFF6B35),
                        isActive = currentStreak > 0
                    )
                    EnhancedStreakCard(
                        modifier = Modifier.weight(1f),
                        title = "PEAK",
                        value = "$longestStreak",
                        subtitle = "DAYS",
                        emoji = "🏆",
                        color = Color(0xFFFFD700),
                        isActive = longestStreak > 0
                    )
                }
            }
            
            // Month Navigation in GlassBox
            item(key = "calendar_month_navigation") {
                GlassBox(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                    cornerRadius = 28.dp
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { 
                                selectedMonth = selectedMonth.minusMonths(1) 
                            }) {
                                Icon(
                                    Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                    contentDescription = "Previous Month",
                                    tint = Color.White
                                )
                            }
                            
                            Text(
                                selectedMonth.format(monthTitleFormatter).uppercase(),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                ),
                                color = Color.White
                            )
                            
                            IconButton(
                                onClick = { 
                                    if (canGoNextMonth) {
                                        selectedMonth = selectedMonth.plusMonths(1) 
                                    }
                                },
                                enabled = canGoNextMonth
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = "Next Month",
                                    tint = if (canGoNextMonth) 
                                        Color.White else Color.White.copy(alpha = 0.3f)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        // Day Headers
                        Row(modifier = Modifier.fillMaxWidth()) {
                            listOf("S", "M", "T", "W", "T", "F", "S").forEach { day ->
                                Box(
                                    modifier = Modifier.weight(1f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        day,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                                        color = Color.White.copy(alpha = 0.3f)
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Calendar Grid
                        val totalCells = startDayOfWeek + daysInMonth
                        val rowsCount = (totalCells + 6) / 7
                        
                        for (week in 0 until rowsCount) {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                for (dayOfWeek in 0..6) {
                                    val dayIndex = week * 7 + dayOfWeek - startDayOfWeek + 1
                                    val isValidDay = dayIndex in 1..daysInMonth
                                    
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .padding(4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isValidDay) {
                                            val date = selectedMonth.atDay(dayIndex)
                                            val dateString = date.toString()
                                            val completedCount = completionHistory[dateString] ?: 0
                                            val hasNote = calendarNotes.containsKey(dateString)
                                            val isToday = date == today
                                            val isFuture = date.isAfter(today)
                                            val isSelected = selectedDate == date
                                            
                                            CalendarDayWithNote(
                                                day = dayIndex,
                                                completedCount = completedCount,
                                                hasNote = hasNote,
                                                isToday = isToday,
                                                isFuture = isFuture,
                                                isSelected = isSelected,
                                                onClick = { 
                                                    selectedDate = date
                                                    showNoteDialog = true
                                                    onDateClick(date)
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            // Legend & Summary
            item(key = "calendar_legend_summary") {
                val monthDates = remember(selectedMonth, daysInMonth) {
                    (1..daysInMonth).map { selectedMonth.atDay(it) }
                }
                val pastDates = remember(monthDates, today) {
                    monthDates.filter { !it.isAfter(today) }
                }
                val completeDays = remember(pastDates, completionHistory) {
                    pastDates.count { (completionHistory[it.toString()] ?: 0) == 3 }
                }
                val partialDays = remember(pastDates, completionHistory) {
                    pastDates.count {
                        val count = completionHistory[it.toString()] ?: 0
                        count in 1..2
                    }
                }
                val notesCount = remember(monthDates, calendarNotes) {
                    monthDates.count { calendarNotes.containsKey(it.toString()) }
                }
                
                GlassBox(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                    cornerRadius = 28.dp
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        // Legend
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            LegendItem(color = Color.White.copy(alpha = 0.05f), label = "IDLE")
                            LegendItem(color = Color(0xFFFFA726).copy(alpha = 0.4f), label = "PARTIAL")
                            LegendItem(color = PrimaryTeal, label = "PERFECT")
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.1f)))
                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            "PERFORMANCE INSIGHTS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.5.sp
                            ),
                            color = PrimaryTeal.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            SummaryItem(
                                value = "$completeDays",
                                label = "PERFECT",
                                color = PrimaryTeal
                            )
                            SummaryItem(
                                value = "$partialDays",
                                label = "PARTIAL",
                                color = Color(0xFFFFA726)
                            )
                            SummaryItem(
                                value = "$notesCount",
                                label = "NOTES",
                                color = NeonCyan
                            )
                        }
                        
                        if (pastDates.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(24.dp))
                            val successRate = (completeDays.toFloat() / pastDates.size * 100).toInt()
                            
                            // Elite Progress Bar
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.05f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(successRate / 100f)
                                        .fillMaxHeight()
                                        .clip(CircleShape)
                                        .background(
                                            Brush.horizontalGradient(
                                                colors = listOf(PrimaryTeal.copy(alpha = 0.5f), PrimaryTeal)
                                            )
                                        )
                                )
                            }
                            
                            Text(
                                "$successRate% MONTHLY ARCHIVE RATE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                ),
                                color = Color.White.copy(alpha = 0.4f),
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
            }
        }
        
        // Note Dialog
        if (showNoteDialog) {
            selectedDate?.let { date ->
                val dateString = date.toString()
                val existingNote = calendarNotes[dateString]
                
                DateNoteDialog(
                    date = date,
                    existingNote = existingNote,
                    completedGoals = completionHistory[dateString] ?: 0,
                    onSave = { note ->
                        onSaveNote(note)
                        showNoteDialog = false
                        selectedDate = null
                    },
                    onDelete = {
                        onDeleteNote(dateString)
                        showNoteDialog = false
                        selectedDate = null
                    },
                    onDismiss = {
                        showNoteDialog = false
                        selectedDate = null
                    }
                )
            }
        }
    }
}

@Composable
private fun CalendarDayWithNote(
    day: Int,
    completedCount: Int,
    hasNote: Boolean,
    isToday: Boolean,
    isFuture: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isFuture -> Color.Transparent
        completedCount == 3 -> PrimaryTeal
        completedCount > 0 -> Color(0xFFFFA726).copy(alpha = 0.6f)
        else -> Color.Gray.copy(alpha = 0.2f)
    }
    
    val textColor = when {
        isFuture -> Color.White.copy(alpha = 0.3f)
        completedCount > 0 -> Color.White
        else -> Color.White.copy(alpha = 0.6f)
    }
    
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .clickable(enabled = !isFuture, onClick = onClick),
        shape = CircleShape,
        color = backgroundColor,
        border = when {
            isSelected -> ButtonDefaults.outlinedButtonBorder.copy(
                brush = Brush.linearGradient(listOf(PrimaryTeal, NeonCyan))
            )
            isToday -> ButtonDefaults.outlinedButtonBorder.copy(
                brush = Brush.linearGradient(listOf(Color.White, Color.White.copy(alpha = 0.5f)))
            )
            else -> null
        }
    ) {
        Box(contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "$day",
                    fontSize = 14.sp,
                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                    color = textColor
                )
                if (hasNote) {
                    Text("📝", fontSize = 8.sp)
                } else if (completedCount == 3 && !isFuture) {
                    Text("✓", fontSize = 8.sp, color = Color.White)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateNoteDialog(
    date: LocalDate,
    existingNote: CalendarNote?,
    completedGoals: Int,
    onSave: (CalendarNote) -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    val dateKey = date.toString()
    var noteContent by rememberSaveable(dateKey, existingNote?.id) { mutableStateOf(existingNote?.content ?: "") }
    var selectedMood by rememberSaveable(dateKey, existingNote?.id) {
        mutableStateOf(existingNote?.mood ?: "\uD83D\uDE0A")
    }
    
    val moods = remember {
        listOf("😊", "😄", "🤩", "😌", "🤔", "😔", "😤", "😴", "💪", "🔥", "⭐", "❤️")
    }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("EEEE, MMM d, yyyy") }
    val today = remember { LocalDate.now() }
    val isToday = date == today
    
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    if (noteContent.isNotBlank()) {
                        val note = CalendarNote(
                            id = existingNote?.id ?: 0,
                            date = date.toString(),
                            content = noteContent.trim(),
                            mood = selectedMood,
                            createdAt = existingNote?.createdAt ?: System.currentTimeMillis(),
                            updatedAt = System.currentTimeMillis()
                        )
                        onSave(note)
                    }
                },
                enabled = noteContent.isNotBlank()
            ) {
                Text("SAVE MISSION LOG", color = if (noteContent.isNotBlank()) PrimaryTeal else Color.Gray, fontWeight = FontWeight.Black)
            }
        },
        dismissButton = {
            Row {
                if (existingNote != null) {
                    TextButton(onClick = onDelete) {
                        Text("DELETE", color = Color(0xFFFF6B6B), fontWeight = FontWeight.Bold)
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("CLOSE", color = Color.White.copy(alpha = 0.5f), fontWeight = FontWeight.Bold)
                }
            }
        },
        title = {
            Column {
                Text(
                    if (isToday) "TODAY'S MISSION LOG" else date.format(dateFormatter).uppercase(),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    ),
                    color = Color.White
                )
                // Goals status
                val goalColor = when (completedGoals) {
                    3 -> PrimaryTeal
                    in 1..2 -> Color(0xFFFFA726)
                    else -> Color.White.copy(alpha = 0.3f)
                }
                Text(
                    when (completedGoals) {
                        3 -> "ELITE PERFORMANCE: 3/3 COMPLETED"
                        in 1..2 -> "PARTIAL SUCCESS: $completedGoals/3 COMPLETED"
                        else -> "MISSION INCOMPLETE: 0/3 COMPLETED"
                    },
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                    color = goalColor
                )
            }
        },
        text = {
            Column {
                // Mood Selector
                Text(
                    "EMOTIONAL STATE",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 1.sp),
                    color = Color.White.copy(alpha = 0.4f)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    moods.take(6).forEach { mood ->
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (selectedMood == mood) PrimaryTeal.copy(alpha = 0.2f) else Color.Transparent)
                                .border(1.dp, if (selectedMood == mood) PrimaryTeal else Color.White.copy(alpha = 0.05f), CircleShape)
                                .clickable { selectedMood = mood },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(mood, fontSize = 22.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    moods.drop(6).forEach { mood ->
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (selectedMood == mood) PrimaryTeal.copy(alpha = 0.2f) else Color.Transparent)
                                .border(1.dp, if (selectedMood == mood) PrimaryTeal else Color.White.copy(alpha = 0.05f), CircleShape)
                                .clickable { selectedMood = mood },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(mood, fontSize = 22.sp)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Note Content
                Text(
                    "MISSION REFLECTIONS",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 1.sp),
                    color = Color.White.copy(alpha = 0.4f)
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = noteContent,
                    onValueChange = { noteContent = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryTeal,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = PrimaryTeal
                    ),
                    placeholder = { 
                        Text(
                            "Syncing thoughts to archive...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.2f)
                        ) 
                    }
                )
            }
        },
        containerColor = DarkSurface
    )
}

@Composable
private fun StreakStatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    emoji: String,
    color: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(emoji, fontSize = 28.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                value,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                title,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}

// Enhanced Streak Card with glow effect
@Composable
private fun EnhancedStreakCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtitle: String,
    emoji: String,
    color: Color,
    isActive: Boolean = true
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = color.copy(alpha = if (isActive) 0.2f else 0.1f)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Emoji with background
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Text(emoji, fontSize = 24.sp)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Title
            Text(
                title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.7f)
            )
            
            // Value with subtitle
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    value,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isActive) color else Color.White.copy(alpha = 0.5f)
                )
                Text(
                    " $subtitle",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }
        }
    }
}


@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
            color = Color.White.copy(alpha = 0.3f)
        )
    }
}

@Composable
private fun SummaryItem(value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
            color = color
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = Color.White.copy(alpha = 0.4f)
        )
    }
}

