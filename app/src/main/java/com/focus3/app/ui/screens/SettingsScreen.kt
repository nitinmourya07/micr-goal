package com.focus3.app.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focus3.app.ui.components.GlassBox
import com.focus3.app.ui.theme.*
import android.content.Intent
import kotlinx.coroutines.delay

/**
 * Settings Screen - Ultra Premium Edition
 * Streamlined with powerful Backup/Import functionality
 * Premium animations, glassmorphism, and micro-interactions
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentStreak: Int,
    longestStreak: Int,
    totalCompletedDays: Int,
    graceDaysUsed: Int,
    onExportData: () -> Unit,
    onImportData: (Uri) -> Unit = {},
    onClearData: () -> Unit,
    onBack: () -> Unit
) {
    var showClearDataDialog by rememberSaveable { mutableStateOf(false) }
    var showImportDialog by rememberSaveable { mutableStateOf(false) }
    var exportAnimating by rememberSaveable { mutableStateOf(false) }
    var importAnimating by rememberSaveable { mutableStateOf(false) }
    
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val particleColors = remember {
        listOf(
            PrimaryTeal.copy(alpha = 0.03f),
            NeonCyan.copy(alpha = 0.02f),
            PremiumGold.copy(alpha = 0.02f)
        )
    }
    
    // File picker launcher for import
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            runCatching { onImportData(it) }
                .onSuccess {
                    Toast.makeText(context, "Data imported successfully.", Toast.LENGTH_SHORT).show()
                }
                .onFailure {
                    Toast.makeText(context, "Failed to import data.", Toast.LENGTH_SHORT).show()
                }
        }
    }

    LaunchedEffect(exportAnimating) {
        if (exportAnimating) {
            delay(1000)
            exportAnimating = false
        }
    }

    LaunchedEffect(importAnimating) {
        if (importAnimating) {
            delay(1000)
            importAnimating = false
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // ANIMATIONS
    // ═══════════════════════════════════════════════════════════════
    val infiniteTransition = rememberInfiniteTransition(label = "settings")
    
    // FAST header glow animation
    val headerGlow by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "headerGlow"
    )
    
    // Floating particles animation
    val particleOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particles"
    )
    
    // FAST Streak fire animation
    val fireScale: Float = if (currentStreak > 0) {
        val animatedFireScale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.15f,
            animationSpec = infiniteRepeatable(
                animation = tween(400, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "fireScale"
        )
        animatedFireScale
    } else {
        1f
    }
    
    // Gradient shift for background
    val gradientShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradient"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Subtle background accents — lighter for settings
        Canvas(modifier = Modifier.fillMaxSize()) {
            repeat(5) { i ->
                val angle = (particleOffset + i * 72) * (Math.PI / 180)
                val radius = 140f + (i * 40f)
                val x = (size.width / 2) + kotlin.math.cos(angle).toFloat() * radius
                val y = (size.height / 3) + kotlin.math.sin(angle).toFloat() * radius * 0.5f
                drawCircle(
                    color = particleColors[i % particleColors.size],
                    radius = 60f + (i * 10f),
                    center = Offset(x, y)
                )
            }
        }
        
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // ═══════════════════════════════════════════════════════════════
            // PREMIUM HEADER
            // ═══════════════════════════════════════════════════════════════
            item(key = "settings_header") {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Back button with ripple
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
                    
                    // Animated gear icon with glow
                    Box(contentAlignment = Alignment.Center) {
                        // Glow layer
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .blur(12.dp)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            PrimaryTeal.copy(alpha = headerGlow * 0.6f),
                                            Color.Transparent
                                        )
                                    ),
                                    CircleShape
                                )
                        )
                        // Icon
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            PrimaryTeal.copy(alpha = 0.2f),
                                            NeonCyan.copy(alpha = 0.1f)
                                        )
                                    )
                                )
                                .border(
                                    1.5.dp,
                                    Brush.linearGradient(
                                        colors = listOf(
                                            PrimaryTeal.copy(alpha = 0.5f),
                                            NeonCyan.copy(alpha = 0.3f)
                                        )
                                    ),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "⚙️", 
                                fontSize = 26.sp,
                                modifier = Modifier.graphicsLayer {
                                    rotationZ = particleOffset * 0.1f
                                }
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(14.dp))
                    
                    Column {
                        Text(
                            "CONTROL CENTER",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp
                            ),
                            color = PrimaryTeal
                        )
                        Text(
                            "Settings",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = (-0.5).sp
                            ),
                            color = Color.White
                        )
                    }
                }
            }
            
            // ═══════════════════════════════════════════════════════════════
            // SECTION 1: YOUR JOURNEY - Premium Stats
            // ═══════════════════════════════════════════════════════════════
            item(key = "settings_journey") {
                AnimatedSectionHeader(
                    emoji = "🏆",
                    title = "YOUR JOURNEY",
                    subtitle = "LIFETIME ACHIEVEMENTS"
                )
                
                Spacer(modifier = Modifier.height(14.dp))
                
                // Premium Stats Card
                Box {
                    // Background glow for active streak
                    if (currentStreak > 0) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .blur(30.dp)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            StreakOrange.copy(alpha = 0.15f),
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
                        showGlow = currentStreak > 0
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            // Stats Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                // Current Streak - Highlighted
                                AnimatedStatCard(
                                    value = currentStreak.toString(),
                                    label = "CURRENT",
                                    emoji = "🔥",
                                    color = StreakOrange,
                                    isHighlighted = currentStreak > 0,
                                    animatedScale = if (currentStreak > 0) fireScale else 1f
                                )
                                
                                // Vertical Divider
                                Box(
                                    modifier = Modifier
                                        .width(1.dp)
                                        .height(75.dp)
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
                                
                                // Best Streak
                                AnimatedStatCard(
                                    value = longestStreak.toString(),
                                    label = "BEST",
                                    emoji = "🏆",
                                    color = PremiumGold,
                                    isHighlighted = false,
                                    animatedScale = 1f
                                )
                                
                                // Vertical Divider
                                Box(
                                    modifier = Modifier
                                        .width(1.dp)
                                        .height(75.dp)
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
                                
                                // Total Days
                                AnimatedStatCard(
                                    value = totalCompletedDays.toString(),
                                    label = "TOTAL",
                                    emoji = "⭐",
                                    color = NeonCyan,
                                    isHighlighted = false,
                                    animatedScale = 1f
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(18.dp))
                            
                            // Grace Days Indicator - Premium Style
                            val graceDaysRemaining = 3 - graceDaysUsed
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(
                                                PrimaryTeal.copy(alpha = 0.15f),
                                                NeonCyan.copy(alpha = 0.08f)
                                            )
                                        )
                                    )
                                    .border(
                                        1.dp,
                                        PrimaryTeal.copy(alpha = 0.2f),
                                        RoundedCornerShape(14.dp)
                                    )
                                    .padding(horizontal = 16.dp, vertical = 14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("🛡️", fontSize = 18.sp)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                "GRACE DAYS",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    letterSpacing = 1.sp
                                                ),
                                                color = Color.White.copy(alpha = 0.5f)
                                            )
                                            Text(
                                                "Streak protection",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.White.copy(alpha = 0.3f)
                                            )
                                        }
                                    }
                                    
                                    // Progress dots
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        repeat(3) { index ->
                                            val isUsed = index >= graceDaysRemaining
                                            Box(
                                                modifier = Modifier
                                                    .size(if (isUsed) 10.dp else 12.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        if (isUsed) 
                                                            Color.White.copy(alpha = 0.15f)
                                                        else 
                                                            PrimaryTeal
                                                    )
                                                    .then(
                                                        if (!isUsed) Modifier.border(
                                                            1.dp,
                                                            PrimaryTeal.copy(alpha = 0.5f),
                                                            CircleShape
                                                        ) else Modifier
                                                    )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            // ═══════════════════════════════════════════════════════════════
            // SECTION 2: DATA VAULT - Backup, Import, Clear
            // ═══════════════════════════════════════════════════════════════
            item(key = "settings_data_vault") {
                AnimatedSectionHeader(
                    emoji = "💾",
                    title = "DATA VAULT",
                    subtitle = "BACKUP & RESTORE"
                )
                
                Spacer(modifier = Modifier.height(14.dp))
                
                GlassBox(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 24.dp
                ) {
                    Column {
                        // EXPORT BACKUP - Animated
                        PremiumActionRow(
                            emoji = "📤",
                            title = "EXPORT BACKUP",
                            subtitle = "Save all data as JSON file",
                            accentColor = PrimaryTeal,
                            enabled = !exportAnimating,
                            isAnimating = exportAnimating,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                if (exportAnimating) return@PremiumActionRow
                                exportAnimating = true
                                onExportData()
                                Toast.makeText(context, "Backup created.", Toast.LENGTH_SHORT).show()
                            }
                        )
                        
                        // Gradient Divider
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp)
                                .height(1.dp)
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color.White.copy(alpha = 0.08f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )
                        
                        // IMPORT BACKUP
                        PremiumActionRow(
                            emoji = "📥",
                            title = "IMPORT BACKUP",
                            subtitle = "Restore from JSON file",
                            accentColor = NeonCyan,
                            enabled = !importAnimating,
                            isAnimating = importAnimating,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                if (importAnimating) return@PremiumActionRow
                                importAnimating = true
                                showImportDialog = true
                            }
                        )
                        
                        // Gradient Divider
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp)
                                .height(1.dp)
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color.White.copy(alpha = 0.08f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )
                        
                        // CLEAR ALL DATA - Danger
                        PremiumActionRow(
                            emoji = "🗑️",
                            title = "WIPE ALL DATA",
                            subtitle = "Permanently delete everything",
                            accentColor = ErrorColor,
                            isDanger = true,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                showClearDataDialog = true
                            }
                        )
                    }
                }
            }
            
            // ═══════════════════════════════════════════════════════════════
            // SECTION 3: APP INFO - Elegant Footer
            // ═══════════════════════════════════════════════════════════════
            item(key = "settings_about") {
                Spacer(modifier = Modifier.height(16.dp))
                
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // App Logo with glow
                    Box(contentAlignment = Alignment.Center) {
                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .blur(20.dp)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            PrimaryTeal.copy(alpha = gradientShift * 0.3f),
                                            Color.Transparent
                                        )
                                    ),
                                    CircleShape
                                )
                        )
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            Color.White.copy(alpha = 0.08f),
                                            Color.White.copy(alpha = 0.03f)
                                        )
                                    )
                                )
                                .border(
                                    1.dp,
                                    Color.White.copy(alpha = 0.1f),
                                    RoundedCornerShape(16.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🎯", fontSize = 28.sp)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        "FOCUS3",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 3.sp
                        ),
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    
                    Text(
                        "Version 1.0.0",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.3f)
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Share Button - Premium
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        PrimaryTeal.copy(alpha = 0.15f),
                                        NeonCyan.copy(alpha = 0.1f)
                                    )
                                )
                            )
                            .border(
                                1.dp,
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        PrimaryTeal.copy(alpha = 0.3f),
                                        NeonCyan.copy(alpha = 0.2f)
                                    )
                                ),
                                RoundedCornerShape(16.dp)
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                runCatching {
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(
                                            Intent.EXTRA_TEXT,
                                            "Check out Focus3. Track daily goals and build habits."
                                        )
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Share via"))
                                }.onFailure {
                                    Toast.makeText(context, "Unable to open share sheet.", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .padding(horizontal = 24.dp, vertical = 14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("📤", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                "SHARE WITH FRIENDS",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                ),
                                color = PrimaryTeal
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(28.dp))
                    
                    Text(
                        "Made with ❤️ for achievers",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.35f)
                    )
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Text(
                        "💡 Set reminders for each mission individually",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.2f),
                        textAlign = TextAlign.Center
                    )
                }
                
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
        
        // ═══════════════════════════════════════════════════════════════
        // DIALOGS - Premium Styled
        // ═══════════════════════════════════════════════════════════════
        
        // Import Confirmation Dialog
        if (showImportDialog) {
            PremiumDialog(
                onDismiss = { showImportDialog = false },
                title = "IMPORT DATA",
                subtitle = "Restore Backup",
                accentColor = NeonCyan,
                content = {
                    Text(
                        "This will merge imported data with your current data. Your existing progress will be preserved.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.6f),
                        lineHeight = 22.sp
                    )
                },
                confirmText = "SELECT FILE",
                onConfirm = {
                    showImportDialog = false
                    importLauncher.launch("application/json")
                }
            )
        }
        
        // Clear Data Confirmation Dialog
        if (showClearDataDialog) {
            PremiumDialog(
                onDismiss = { showClearDataDialog = false },
                title = "⚠️ DANGER ZONE",
                subtitle = "Delete All Data?",
                accentColor = ErrorColor,
                content = {
                    Column {
                        Text(
                            "This will permanently delete:",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        listOf(
                            "📋 All daily tasks",
                            "🎯 All missions & progress",
                            "📝 All notes",
                            "🔥 Streak history"
                        ).forEach { item ->
                            Row(
                                modifier = Modifier.padding(vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    item,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.5f)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(14.dp))
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(ErrorColor.copy(alpha = 0.15f))
                                .border(1.dp, ErrorColor.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                "⚠️ This action cannot be undone!",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = ErrorColor
                            )
                        }
                    }
                },
                confirmText = "DELETE ALL",
                isDanger = true,
                onConfirm = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClearData()
                    showClearDataDialog = false
                    Toast.makeText(context, "All data deleted.", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// PREMIUM COMPONENTS
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun AnimatedSectionHeader(
    emoji: String,
    title: String,
    subtitle: String
) {
    val infiniteTransition = rememberInfiniteTransition(label = "header")
    val emojiScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "emoji"
    )
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            emoji, 
            fontSize = 22.sp,
            modifier = Modifier.scale(emojiScale)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.5.sp
                ),
                color = Color.White
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 0.5.sp
                ),
                color = Color.White.copy(alpha = 0.4f)
            )
        }
    }
}

@Composable
private fun AnimatedStatCard(
    value: String,
    label: String,
    emoji: String,
    color: Color,
    isHighlighted: Boolean,
    animatedScale: Float
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(80.dp)
    ) {
        // Emoji with optional animation
        Box(
            modifier = Modifier
                .size(36.dp)
                .scale(animatedScale),
            contentAlignment = Alignment.Center
        ) {
            if (isHighlighted) {
                // Glow behind emoji
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .blur(8.dp)
                        .background(color.copy(alpha = 0.5f), CircleShape)
                )
            }
            Text(emoji, fontSize = 24.sp)
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            value,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Black
            ),
            color = color
        )
        
        Spacer(modifier = Modifier.height(2.dp))
        
        Text(
            label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.5.sp
            ),
            color = Color.White.copy(alpha = 0.5f)
        )
    }
}

@Composable
private fun PremiumActionRow(
    emoji: String,
    title: String,
    subtitle: String,
    accentColor: Color,
    enabled: Boolean = true,
    isDanger: Boolean = false,
    isAnimating: Boolean = false,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isAnimating) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .alpha(if (enabled) 1f else 0.55f)
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
            .padding(20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            // Icon Box with gradient
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                accentColor.copy(alpha = 0.2f),
                                accentColor.copy(alpha = 0.08f)
                            )
                        )
                    )
                    .border(
                        1.dp,
                        accentColor.copy(alpha = 0.25f),
                        RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(emoji, fontSize = 22.sp)
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = if (isDanger) accentColor else Color.White
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.4f)
                )
            }
        }
        
        // Arrow indicator
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(accentColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "→",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = accentColor
            )
        }
    }
}

@Composable
private fun PremiumDialog(
    onDismiss: () -> Unit,
    title: String,
    subtitle: String,
    accentColor: Color,
    content: @Composable () -> Unit,
    confirmText: String,
    isDanger: Boolean = false,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF12121A),
        shape = RoundedCornerShape(28.dp),
        title = {
            Column {
                Text(
                    title,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp
                    ),
                    color = accentColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    subtitle,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black
                    ),
                    color = Color.White
                )
            }
        },
        text = { content() },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentColor,
                    contentColor = if (isDanger) Color.White else Color.Black
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.height(48.dp)
            ) {
                Text(
                    confirmText,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Black
                    )
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.height(48.dp)
            ) {
                Text(
                    "CANCEL",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White.copy(alpha = 0.4f)
                )
            }
        }
    )
}
