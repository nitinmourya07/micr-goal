package com.focus3.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.focus3.app.ui.components.GlassBox
import com.focus3.app.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

// Available avatars - Premium collection
val avatarOptions = listOf(
    "😎", "🦁", "🐯", "🦊", "🐼", "🐨", 
    "🦄", "🐉", "🔥", "⭐", "💎", "🚀",
    "🎯", "💪", "🏆", "👑", "🌟", "✨"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    currentAvatar: String,
    userName: String,
    streak: Int,
    totalGoalsCompleted: Int,
    googlePhotoUrl: String?,
    googleDisplayName: String?,
    googleEmail: String?,
    onAvatarChange: (String) -> Unit,
    onNameChange: (String) -> Unit,
    onBack: () -> Unit
) {
    var selectedAvatar by rememberSaveable(currentAvatar) { mutableStateOf(currentAvatar) }
    // Prefer Google display name over local userName
    val effectiveUserName = remember(googleDisplayName, userName) {
        if (!googleDisplayName.isNullOrBlank()) googleDisplayName else userName
    }
    var editedName by rememberSaveable(effectiveUserName) { mutableStateOf(effectiveUserName) }
    val hasGooglePhoto = !googlePhotoUrl.isNullOrBlank()
    val haptic = LocalHapticFeedback.current
    val particleColors = remember {
        listOf(
            PrimaryTeal.copy(alpha = 0.03f),
            NeonCyan.copy(alpha = 0.02f),
            PremiumGold.copy(alpha = 0.02f)
        )
    }
    
    // Calculate level based on goals completed
    val level = remember(totalGoalsCompleted) { (totalGoalsCompleted / 10) + 1 }
    val levelProgress = remember(totalGoalsCompleted) { (totalGoalsCompleted % 10) / 10f }
    val goalsToNextLevel = remember(totalGoalsCompleted) { 10 - (totalGoalsCompleted % 10) }
    
    // Rank title based on level
    val rankTitle = remember(level) {
        when {
            level >= 50 -> "🌟 Legend"
            level >= 30 -> "👑 Master"
            level >= 20 -> "🏆 Champion"
            level >= 10 -> "💪 Warrior"
            level >= 5 -> "🔥 Achiever"
            else -> "🌱 Beginner"
        }
    }
    val hasProfileChanges = remember(selectedAvatar, editedName, currentAvatar, userName) {
        selectedAvatar != currentAvatar || editedName.trim() != userName.trim()
    }
    val normalizedEditedName = remember(editedName) { editedName.trim() }
    val canSaveProfile = remember(hasProfileChanges, normalizedEditedName) {
        hasProfileChanges && normalizedEditedName.isNotBlank()
    }
    
    // ═══════════════════════════════════════════════════════════════
    // ANIMATIONS
    // ═══════════════════════════════════════════════════════════════
    val infiniteTransition = rememberInfiniteTransition(label = "profile")
    
    // FAST avatar ring rotation
    val ringRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ringRotation"
    )
    
    // FAST pulse effect for avatar
    val avatarPulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "avatarPulse"
    )
    
    // FAST glow intensity
    val glowIntensity by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowIntensity"
    )
    
    // FAST background particles
    val particleAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particles"
    )
    
    // Streak fire animation
    val fireScale: Float = if (streak > 0) {
        val animatedFireScale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.2f,
            animationSpec = infiniteRepeatable(
                animation = tween(500, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "fireScale"
        )
        animatedFireScale
    } else {
        1f
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Animated background particles
        Canvas(modifier = Modifier.fillMaxSize()) {
            repeat(6) { i ->
                val angle = (particleAngle + i * 60) * (Math.PI / 180)
                val radius = 200f + (i * 40f)
                val x = (size.width / 2) + cos(angle).toFloat() * radius
                val y = (size.height / 4) + sin(angle).toFloat() * radius * 0.6f
                drawCircle(
                    color = particleColors[i % particleColors.size],
                    radius = 100f + (i * 20f),
                    center = Offset(x, y)
                )
            }
        }
        
        // ═══════════════════════════════════════════════════════════════
        // TOP BAR
        // ═══════════════════════════════════════════════════════════════
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Premium Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                        "OPERATIVE IDENTITY",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        ),
                        color = PrimaryTeal
                    )
                    Text(
                        "Profile",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-0.5).sp
                        ),
                        color = Color.White
                    )
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Save button with gradient
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    if (canSaveProfile) PrimaryTeal else Color.White.copy(alpha = 0.2f),
                                    if (canSaveProfile) NeonCyan else Color.White.copy(alpha = 0.1f)
                                )
                            )
                        )
                        .clickable(
                            enabled = canSaveProfile,
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            if (selectedAvatar != currentAvatar) {
                                onAvatarChange(selectedAvatar)
                            }
                            if (normalizedEditedName != userName.trim()) {
                                onNameChange(normalizedEditedName)
                            }
                            onBack()
                        }
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text(
                        "SAVE",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Black
                        ),
                        color = if (canSaveProfile) Color.Black else Color.White.copy(alpha = 0.4f)
                    )
                }
            }
            
            // ═══════════════════════════════════════════════════════════════
            // MAIN CONTENT
            // ═══════════════════════════════════════════════════════════════
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                
                // ═══════════════════════════════════════════════════════════════
                // PREMIUM AVATAR DISPLAY
                // ═══════════════════════════════════════════════════════════════
                Box(
                    modifier = Modifier.size(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Outer rotating ring
                    Canvas(
                        modifier = Modifier
                            .size(180.dp)
                            .graphicsLayer { rotationZ = ringRotation }
                    ) {
                        val ringColors = listOf(PrimaryTeal, NeonCyan, PrimaryTeal)
                        val sweepAngle = 90f
                        repeat(4) { i ->
                            drawArc(
                                brush = Brush.sweepGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        ringColors[i % ringColors.size].copy(alpha = 0.6f)
                                    )
                                ),
                                startAngle = i * 90f,
                                sweepAngle = sweepAngle,
                                useCenter = false,
                                style = Stroke(width = 3.dp.toPx())
                            )
                        }
                    }
                    
                    // Glow behind avatar
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .blur(25.dp)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        PrimaryTeal.copy(alpha = glowIntensity * 0.5f),
                                        Color.Transparent
                                    )
                                ),
                                CircleShape
                            )
                    )
                    
                    // Avatar container
                    Box(
                        modifier = Modifier
                            .size(130.dp)
                            .scale(avatarPulse)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.08f),
                                        Color.White.copy(alpha = 0.03f)
                                    )
                                )
                            )
                            .border(
                                2.5.dp,
                                Brush.linearGradient(
                                    colors = listOf(
                                        PrimaryTeal,
                                        NeonCyan.copy(alpha = 0.5f)
                                    )
                                ),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (hasGooglePhoto) {
                            // Google profile photo loaded via Coil
                            AsyncImage(
                                model = googlePhotoUrl,
                                contentDescription = "Profile Photo",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            // Fallback: emoji avatar or default icon
                            if (selectedAvatar.isNotBlank()) {
                                Text(
                                    text = selectedAvatar,
                                    fontSize = 70.sp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Default Avatar",
                                    modifier = Modifier.size(70.dp),
                                    tint = PrimaryTeal
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Rank Badge - Premium Style
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
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
                                    PrimaryTeal.copy(alpha = 0.4f),
                                    NeonCyan.copy(alpha = 0.2f)
                                )
                            ),
                            RoundedCornerShape(50.dp)
                        )
                        .padding(horizontal = 24.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = rankTitle.uppercase(),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.5.sp
                        ),
                        color = PrimaryTeal
                    )
                }
                
                Spacer(modifier = Modifier.height(28.dp))
                
                // ═══════════════════════════════════════════════════════════════
                // NAME INPUT - Premium Style
                // ═══════════════════════════════════════════════════════════════
                Text(
                    "OPERATIVE DESIGNATION",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Black, 
                        letterSpacing = 1.5.sp
                    ),
                    color = Color.White.copy(alpha = 0.4f)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.06f),
                                    Color.White.copy(alpha = 0.03f)
                                )
                            )
                        )
                        .border(
                            1.dp,
                            Color.White.copy(alpha = 0.1f),
                            RoundedCornerShape(20.dp)
                        )
                ) {
                    BasicTextField(
                        value = editedName,
                        onValueChange = { input ->
                            editedName = input
                                .replace("\n", "")
                                .take(28)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(22.dp),
                        textStyle = MaterialTheme.typography.headlineSmall.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center
                        ),
                        cursorBrush = SolidColor(PrimaryTeal),
                        singleLine = true,
                        decorationBox = { innerTextField ->
                            Box(contentAlignment = Alignment.Center) {
                                if (editedName.isEmpty()) {
                                    Text(
                                        "ENTER YOUR NAME",
                                        style = MaterialTheme.typography.headlineSmall.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = Color.White.copy(alpha = 0.15f)
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Google email display
                if (!googleEmail.isNullOrBlank()) {
                    Text(
                        text = googleEmail,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        textAlign = TextAlign.Center
                    )
                }
                
                Spacer(modifier = Modifier.height(36.dp))
                
                // ═══════════════════════════════════════════════════════════════
                // STATS SECTION - Premium Cards
                // ═══════════════════════════════════════════════════════════════
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "PERFORMANCE METRICS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Black, 
                            letterSpacing = 1.5.sp
                        ),
                        color = Color.White.copy(alpha = 0.4f)
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(PrimaryTeal.copy(alpha = 0.15f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            "TIER $level",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black
                            ),
                            color = PrimaryTeal
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Stats Row - 3 premium cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Streak Card - Animated
                    PremiumStatCard(
                        modifier = Modifier.weight(1f),
                        emoji = "🔥",
                        value = streak.toString(),
                        label = "STREAK",
                        color = StreakOrange,
                        isHighlighted = streak > 0,
                        animatedScale = if (streak > 0) fireScale else 1f
                    )
                    
                    // Missions Card
                    PremiumStatCard(
                        modifier = Modifier.weight(1f),
                        emoji = "✅",
                        value = totalGoalsCompleted.toString(),
                        label = "MISSIONS",
                        color = SuccessColor,
                        isHighlighted = false,
                        animatedScale = 1f
                    )
                    
                    // Tier Card
                    PremiumStatCard(
                        modifier = Modifier.weight(1f),
                        emoji = "⭐",
                        value = level.toString(),
                        label = "TIER",
                        color = PremiumGold,
                        isHighlighted = false,
                        animatedScale = 1f
                    )
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // ═══════════════════════════════════════════════════════════════
                // LEVEL PROGRESS - Premium XP Bar
                // ═══════════════════════════════════════════════════════════════
                GlassBox(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 24.dp
                ) {
                    Column(modifier = Modifier.padding(22.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("📈", fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    "PROGRESSION TO TIER ${level + 1}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = Color.White.copy(alpha = 0.5f)
                                )
                            }
                            Text(
                                "$goalsToNextLevel LEFT",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Black
                                ),
                                color = PrimaryTeal
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Progress bar with glow
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.05f))
                        ) {
                            // Glow effect
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(levelProgress)
                                    .fillMaxHeight()
                                    .blur(8.dp)
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(
                                                PrimaryTeal.copy(alpha = 0.5f),
                                                NeonCyan.copy(alpha = 0.5f)
                                            )
                                        ),
                                        CircleShape
                                    )
                            )
                            // Actual progress
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(levelProgress)
                                    .fillMaxHeight()
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(PrimaryTeal, NeonCyan)
                                        ),
                                        CircleShape
                                    )
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // XP indicator
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                "${(levelProgress * 100).toInt()}%",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Black
                                ),
                                color = PrimaryTeal
                            )
                            Text(
                                " complete",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White.copy(alpha = 0.4f)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}


// ═══════════════════════════════════════════════════════════════════════════════
// PREMIUM COMPONENTS
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun PremiumStatCard(
    modifier: Modifier = Modifier,
    emoji: String,
    value: String,
    label: String,
    color: Color,
    isHighlighted: Boolean,
    animatedScale: Float
) {
    GlassBox(
        modifier = modifier,
        cornerRadius = 18.dp,
        showGlow = isHighlighted
    ) {
        Column(
            modifier = Modifier.padding(vertical = 18.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.scale(animatedScale),
                contentAlignment = Alignment.Center
            ) {
                if (isHighlighted) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .blur(10.dp)
                            .background(color.copy(alpha = 0.4f), CircleShape)
                    )
                }
                Text(emoji, fontSize = 26.sp)
            }
            
            Spacer(modifier = Modifier.height(10.dp))
            
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Black
                ),
                color = color
            )
            
            Spacer(modifier = Modifier.height(2.dp))
            
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                ),
                color = Color.White.copy(alpha = 0.4f)
            )
        }
    }
}
