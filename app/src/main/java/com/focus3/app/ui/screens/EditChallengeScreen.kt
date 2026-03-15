package com.focus3.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focus3.app.data.model.Challenge
import com.focus3.app.ui.components.GlassBox
import com.focus3.app.ui.theme.*
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Edit Challenge Screen - allows user to edit challenge without losing progress
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditChallengeScreen(
    challenge: Challenge,
    onSave: (Challenge) -> Unit,
    onDelete: () -> Unit,
    onBack: () -> Unit
) {
    val challengeKey = challenge.id
    var name by rememberSaveable(challengeKey) { mutableStateOf(challenge.name) }
    var targetDays by rememberSaveable(challengeKey) { mutableStateOf(challenge.targetDays.toString()) }
    var selectedIcon by rememberSaveable(challengeKey) { mutableStateOf(challenge.icon) }
    var reminderEnabled by rememberSaveable(challengeKey) { mutableStateOf(challenge.reminderTime != null) }
    var reminderHour by rememberSaveable(challengeKey) { mutableStateOf(
        challenge.reminderTime?.split(":")?.getOrNull(0)?.toIntOrNull() ?: 8
    ) }
    var reminderMinute by rememberSaveable(challengeKey) { mutableStateOf(
        challenge.reminderTime?.split(":")?.getOrNull(1)?.toIntOrNull() ?: 0
    ) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var showIconPicker by remember { mutableStateOf(false) }
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    
    val challengeIcons = remember { listOf(
        "📚", "💪", "🏃", "🧘", "📖", "💻", "🎨", "🎵", 
        "✍️", "🏋️", "🚴", "💧", "🥗", "😴", "🧠", "🎯",
        "⭐", "🔥", "💎", "🏆", "❤️", "🌟", "🚀", "⚡"
    ) }
    val iconRows = remember(challengeIcons) { challengeIcons.chunked(6) }
    val normalizedName = remember(name) { name.trim() }
    val targetNum = remember(targetDays) { targetDays.toIntOrNull() ?: 0 }
    val reminderTimeString = remember(reminderEnabled, reminderHour, reminderMinute) {
        if (reminderEnabled) String.format("%02d:%02d", reminderHour, reminderMinute) else null
    }
    val isValid = remember(normalizedName, targetNum, challenge.completedDays) {
        normalizedName.isNotBlank() && targetNum >= challenge.completedDays && targetNum > 0
    }
    val hasChanges = remember(challenge, normalizedName, targetNum, selectedIcon, reminderTimeString) {
        normalizedName != challenge.name ||
            targetNum != challenge.targetDays ||
            selectedIcon != challenge.icon ||
            reminderTimeString != challenge.reminderTime
    }
    
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
                .navigationBarsPadding()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            contentPadding = PaddingValues(bottom = 28.dp)
        ) {
            // Ultra-Premium Header with Animation
            item {
                val infiniteTransition = rememberInfiniteTransition(label = "header_glow")
                val headerGlow by infiniteTransition.animateFloat(
                    initialValue = 0.3f,
                    targetValue = 0.7f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2000, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "glow"
                )
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(top = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack, 
                                contentDescription = "Back", 
                                tint = Color.White
                            )
                        }
                        
                        // Animated icon with glow
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            PrimaryTeal.copy(alpha = headerGlow * 0.4f),
                                            Color.Transparent
                                        )
                                    ),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "⚙️", fontSize = 28.sp)
                        }
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Column {
                            Text(
                                "MISSION PARAMETERS",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.5.sp
                                ),
                                color = PrimaryTeal.copy(alpha = headerGlow)
                            )
                            Text(
                                "EDIT CHALLENGE",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = (-0.5).sp
                                ),
                                color = Color.White
                            )
                        }
                        
                        Spacer(modifier = Modifier.weight(1f))
                        
                        IconButton(onClick = { showDeleteConfirmation = true }) {
                            Icon(
                                Icons.Default.Delete, 
                                contentDescription = "Delete",
                                tint = Color(0xFFFF6B6B).copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
            
            // Progress Info Card
            item {
                GlassBox(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    cornerRadius = 24.dp,
                    showGlow = challenge.currentStreak > 0
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "CURRENT PROGRESSION",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 1.sp),
                            color = PrimaryTeal
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "${challenge.completedDays}",
                                    style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Black),
                                    color = Color.White
                                )
                                Text("COMPLETED", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black), color = Color.White.copy(alpha = 0.3f))
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "${challenge.currentStreak}",
                                    style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Black),
                                    color = PrimaryTeal
                                )
                                Text("FIRE STREAK", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black), color = Color.White.copy(alpha = 0.3f))
                            }
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        LinearProgressIndicator(
                            progress = { (challenge.completedDays.toFloat() / challenge.targetDays.coerceAtLeast(1)).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(CircleShape),
                            color = PrimaryTeal,
                            trackColor = Color.White.copy(alpha = 0.05f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
            
            // Icon Selector
            item {
                Text(
                    "MISSION SYMBOL",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 1.sp),
                    color = Color.White.copy(alpha = 0.3f),
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )
                GlassBox(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clickable { showIconPicker = true },
                    cornerRadius = 20.dp
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.05f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(selectedIcon, fontSize = 32.sp)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                "CHANGE SYMBOL",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                color = Color.White
                            )
                            Text(
                                "Establish visual identity",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.4f)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
            
            // Challenge Name
            item {
                Text(
                    "CHALLENGE IDENTIFIER",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 1.sp),
                    color = Color.White.copy(alpha = 0.3f),
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryTeal,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = PrimaryTeal
                    ),
                    shape = RoundedCornerShape(16.dp),
                    placeholder = { Text("Define your objective...", color = Color.White.copy(alpha = 0.2f)) },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Target Days (Editable!)
            item {
                Text(
                    "TARGET DURATION",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 1.sp),
                    color = Color.White.copy(alpha = 0.3f),
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )
                
                GlassBox(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    cornerRadius = 20.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            BasicTextField(
                                value = targetDays,
                                onValueChange = { 
                                    if (it.all { char -> char.isDigit() } && it.length <= 4) {
                                        targetDays = it
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                textStyle = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black, color = Color.White),
                                cursorBrush = SolidColor(PrimaryTeal),
                                decorationBox = { innerTextField ->
                                    Box {
                                        if (targetDays.isEmpty()) {
                                            Text("0", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black), color = Color.White.copy(alpha = 0.1f))
                                        }
                                        innerTextField()
                                    }
                                }
                            )
                            Text(
                                "DAYS",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                                color = PrimaryTeal
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("+7", "+15", "+30").forEach { addDays ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(PrimaryTeal.copy(alpha = 0.05f))
                                        .border(1.dp, PrimaryTeal.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                        .clickable {
                                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                            val current = targetDays.toIntOrNull() ?: 0
                                            targetDays = (current + addDays.substring(1).toInt()).toString()
                                        }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        addDays,
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                                        color = PrimaryTeal
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Validation message
                if (targetNum > 0 && targetNum < challenge.completedDays) {
                    Text(
                        "⚠️ TARGET REVISION REQUIRED: MIN ${challenge.completedDays} DAYS",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                        color = Color(0xFFFF4444),
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
            }
            
            // Reminder Settings
            item {
                GlassBox(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    cornerRadius = 20.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    "🔔 Daily Reminder",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    "Get notified to complete this challenge",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.6f)
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
                                    checkedTrackColor = PrimaryTeal.copy(alpha = 0.3f)
                                )
                            )
                        }
                        
                        AnimatedVisibility(visible = reminderEnabled) {
                            Column {
                                Spacer(modifier = Modifier.height(16.dp))
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { showTimePicker = true },
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color.White.copy(alpha = 0.1f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.Notifications,
                                            contentDescription = "Reminder",
                                            tint = PrimaryTeal
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                "Reminder Time",
                                                fontSize = 14.sp,
                                                color = Color.White.copy(alpha = 0.7f)
                                            )
                                            Text(
                                                String.format("%02d:%02d", reminderHour, reminderMinute),
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                        Spacer(modifier = Modifier.weight(1f))
                                        Text(
                                            "Tap to change",
                                            fontSize = 12.sp,
                                            color = PrimaryTeal
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
            
            // Save Button
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                            val updatedChallenge = challenge.copy(
                                name = normalizedName,
                                targetDays = targetNum,
                                icon = selectedIcon,
                                reminderTime = reminderTimeString
                            )
                            onSave(updatedChallenge)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        enabled = isValid && hasChanges,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryTeal,
                            contentColor = Color.Black,
                            disabledContainerColor = Color.White.copy(alpha = 0.05f),
                            disabledContentColor = Color.White.copy(alpha = 0.2f)
                        ),
                        shape = RoundedCornerShape(20.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                    ) {
                        Text(
                            if (hasChanges) "AUTHORIZE MISSION REVISION" else "NO CHANGES DETECTED",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
        
        // Time Picker Dialog
        if (showTimePicker) {
            val timePickerState = rememberTimePickerState(
                initialHour = reminderHour,
                initialMinute = reminderMinute
            )
            
            AlertDialog(
                onDismissRequest = { showTimePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        reminderHour = timePickerState.hour
                        reminderMinute = timePickerState.minute
                        showTimePicker = false
                    }) {
                        Text("OK", color = PrimaryTeal)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showTimePicker = false }) {
                        Text("Cancel", color = Color.White.copy(alpha = 0.7f))
                    }
                },
                title = { Text("Set Reminder Time", color = Color.White) },
                text = {
                    TimePicker(
                        state = timePickerState,
                        colors = TimePickerDefaults.colors(
                            clockDialColor = Color.White.copy(alpha = 0.1f),
                            selectorColor = PrimaryTeal,
                            containerColor = DarkSurface,
                            timeSelectorSelectedContainerColor = PrimaryTeal.copy(alpha = 0.3f),
                            timeSelectorUnselectedContainerColor = Color.White.copy(alpha = 0.1f)
                        )
                    )
                },
                containerColor = DarkSurface
            )
        }
        
        // Enhanced Icon Picker Dialog with Grid
        if (showIconPicker) {
            AlertDialog(
                onDismissRequest = { showIconPicker = false },
                confirmButton = {
                    TextButton(onClick = { showIconPicker = false }) {
                        Text("DONE", color = PrimaryTeal, fontWeight = FontWeight.Black)
                    }
                },
                title = { 
                    Column {
                        Text(
                            "CHOOSE SYMBOL", 
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Select a visual identity for your mission",
                            color = Color.White.copy(alpha = 0.5f),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                },
                text = {
                    LazyColumn(
                        modifier = Modifier.height(300.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            count = iconRows.size,
                            key = { it },
                            contentType = { "icon_row" }
                        ) { rowIndex ->
                            val row = iconRows[rowIndex]
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
                            ) {
                                row.forEach { icon ->
                                    val isSelected = selectedIcon == icon
                                    val scale by animateFloatAsState(
                                        targetValue = if (isSelected) 1.1f else 1f,
                                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                                        label = "icon_scale"
                                    )
                                    
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .graphicsLayer {
                                                scaleX = scale
                                                scaleY = scale
                                            }
                                            .clip(CircleShape)
                                            .background(
                                                if (isSelected)
                                                    Brush.radialGradient(
                                                        colors = listOf(
                                                            PrimaryTeal.copy(alpha = 0.5f),
                                                            PrimaryTeal.copy(alpha = 0.2f)
                                                        )
                                                    )
                                                else Brush.radialGradient(
                                                    colors = listOf(
                                                        Color.White.copy(alpha = 0.1f),
                                                        Color.Transparent
                                                    )
                                                )
                                            )
                                            .border(
                                                width = if (isSelected) 2.dp else 1.dp,
                                                color = if (isSelected) PrimaryTeal else Color.White.copy(alpha = 0.1f),
                                                shape = CircleShape
                                            )
                                            .clickable {
                                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                                selectedIcon = icon
                                                showIconPicker = false
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(icon, fontSize = 28.sp)
                                    }
                                }
                            }
                        }
                    }
                },
                containerColor = DarkSurface,
                shape = RoundedCornerShape(24.dp)
            )
        }
        
        // Delete Confirmation Dialog
        if (showDeleteConfirmation) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmation = false },
                confirmButton = {
                    TextButton(onClick = {
                        showDeleteConfirmation = false
                        onDelete()
                    }) {
                        Text("Delete", color = Color(0xFFFF6B6B))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirmation = false }) {
                        Text("Cancel", color = Color.White.copy(alpha = 0.7f))
                    }
                },
                title = { 
                    Text("Delete Challenge?", color = Color.White) 
                },
                text = { 
                    Text(
                        "This will permanently delete \"${challenge.name}\" and all its progress. This cannot be undone.",
                        color = Color.White.copy(alpha = 0.7f)
                    ) 
                },
                containerColor = DarkSurface
            )
        }
    }
}
