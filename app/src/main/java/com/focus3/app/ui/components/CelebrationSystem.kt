package com.focus3.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focus3.app.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * Ultimate Dopamine Celebration System 🎉
 * Creates epic celebration effects that hit the dopamine receptors!
 */

// ==================== CONFETTI PARTICLES ====================

data class ConfettiParticle(
    val x: Float,
    val y: Float,
    val color: Color,
    val size: Float,
    val velocityX: Float,
    val velocityY: Float,
    val rotation: Float,
    val rotationSpeed: Float
)

@Composable
fun EpicConfettiExplosion(
    isVisible: Boolean,
    onComplete: () -> Unit
) {
    val confettiColors = listOf(
        Color(0xFFFFD700), // Gold
        Color(0xFFFF6B6B), // Red
        Color(0xFF4ECDC4), // Teal
        Color(0xFFFF69B4), // Pink
        Color(0xFF00D4FF), // Cyan
        Color(0xFFFFFF00), // Yellow
        Color(0xFF9B59B6), // Purple
        Color(0xFF2ECC71), // Green
    )
    
    var particles by remember { mutableStateOf(listOf<ConfettiParticle>()) }
    val animationProgress = remember { Animatable(0f) }
    
    LaunchedEffect(isVisible) {
        if (isVisible) {
            // Generate particles (optimized for performance)
            particles = List(50) {
                ConfettiParticle(
                    x = 0.5f + Random.nextFloat() * 0.1f - 0.05f,
                    y = 0.5f,
                    color = confettiColors.random(),
                    size = Random.nextFloat() * 15f + 5f,
                    velocityX = Random.nextFloat() * 2f - 1f,
                    velocityY = Random.nextFloat() * -2f - 0.5f,
                    rotation = Random.nextFloat() * 360f,
                    rotationSpeed = Random.nextFloat() * 20f - 10f
                )
            }
            
            animationProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(3000, easing = LinearEasing)
            )
            onComplete()
        } else {
            particles = emptyList()
            animationProgress.snapTo(0f)
        }
    }
    
    if (isVisible && particles.isNotEmpty()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val progress = animationProgress.value
            val gravity = 0.02f
            
            particles.forEach { particle ->
                val time = progress * 3f
                val x = particle.x + particle.velocityX * time * 0.3f
                val y = particle.y + particle.velocityY * time + gravity * time * time
                val rotation = particle.rotation + particle.rotationSpeed * time * 10f
                val alpha = (1f - progress).coerceIn(0f, 1f)
                
                if (y < 1.2f && y > -0.2f) {
                    rotate(rotation, pivot = Offset(x * size.width, y * size.height)) {
                        drawRect(
                            color = particle.color.copy(alpha = alpha),
                            topLeft = Offset(
                                x * size.width - particle.size / 2,
                                y * size.height - particle.size / 2
                            ),
                            size = androidx.compose.ui.geometry.Size(particle.size, particle.size)
                        )
                    }
                }
            }
        }
    }
}

// ==================== FIREWORKS EFFECT ====================

data class Firework(
    val x: Float,
    val y: Float,
    val color: Color,
    val particles: List<FireworkParticle>
)

data class FireworkParticle(
    val angle: Float,
    val speed: Float,
    val size: Float
)

@Composable
fun FireworksShow(
    isVisible: Boolean,
    onComplete: () -> Unit
) {
    val fireworkColors = listOf(
        Color(0xFFFFD700),
        Color(0xFFFF6B6B),
        Color(0xFF00D4FF),
        Color(0xFFFF69B4),
        Color(0xFF9B59B6),
    )
    
    var fireworks by remember { mutableStateOf(listOf<Firework>()) }
    val animationProgress = remember { Animatable(0f) }
    
    LaunchedEffect(isVisible) {
        if (isVisible) {
            // Generate fewer fireworks for performance
            fireworks = List(3) { index ->
                Firework(
                    x = 0.2f + index * 0.15f,
                    y = 0.3f + Random.nextFloat() * 0.2f,
                    color = fireworkColors.random(),
                    particles = List(15) {
                        FireworkParticle(
                            angle = it * 12f,
                            speed = 0.3f + Random.nextFloat() * 0.2f,
                            size = 4f + Random.nextFloat() * 4f
                        )
                    }
                )
            }
            
            animationProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(2500, easing = FastOutSlowInEasing)
            )
            onComplete()
        } else {
            fireworks = emptyList()
            animationProgress.snapTo(0f)
        }
    }
    
    if (isVisible && fireworks.isNotEmpty()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val progress = animationProgress.value
            
            fireworks.forEach { firework ->
                val alpha = (1f - progress * 0.8f).coerceIn(0f, 1f)
                
                firework.particles.forEach { particle ->
                    val distance = particle.speed * progress
                    val x = firework.x + cos(Math.toRadians(particle.angle.toDouble())).toFloat() * distance
                    val y = firework.y + sin(Math.toRadians(particle.angle.toDouble())).toFloat() * distance
                    
                    val trailLength = 3
                    for (i in 0 until trailLength) {
                        val trailProgress = progress - i * 0.05f
                        if (trailProgress > 0) {
                            val trailDistance = particle.speed * trailProgress
                            val trailX = firework.x + cos(Math.toRadians(particle.angle.toDouble())).toFloat() * trailDistance
                            val trailY = firework.y + sin(Math.toRadians(particle.angle.toDouble())).toFloat() * trailDistance
                            val trailAlpha = alpha * (1f - i * 0.3f)
                            
                            drawCircle(
                                color = firework.color.copy(alpha = trailAlpha),
                                radius = particle.size * (1f - i * 0.2f),
                                center = Offset(trailX * size.width, trailY * size.height)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==================== PULSE GLOW EFFECT ====================

@Composable
fun PulseGlowEffect(
    isVisible: Boolean,
    color: Color = Color(0xFFFFD700),
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    
    Box(contentAlignment = Alignment.Center) {
        if (isVisible) {
            Box(
                modifier = Modifier
                    .scale(scale * 1.3f)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                color.copy(alpha = alpha),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
                    .size(100.dp)
            )
        }
        content()
    }
}

// ==================== MAIN CELEBRATION OVERLAY ====================

enum class CelebrationType {
    DAILY_COMPLETE,      // All 3 daily goals done
    CHALLENGE_PROGRESS,  // One challenge task done
    CHALLENGE_COMPLETE,  // Entire challenge finished
    STREAK_MILESTONE,    // Hit a streak milestone
    EPIC_ACHIEVEMENT     // Special achievement
}

@Composable
fun CelebrationOverlay(
    isVisible: Boolean,
    type: CelebrationType,
    streakCount: Int = 0,
    challengeName: String = "",
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    
    // Haptic feedback
    LaunchedEffect(isVisible) {
        if (isVisible) {
            triggerHapticFeedback(context, type)
        }
    }
    
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(300)) + scaleIn(tween(300)),
        exit = fadeOut(tween(300)) + scaleOut(tween(300))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f))
                .pointerInput(Unit) {
                    detectTapGestures { onDismiss() }
                },
            contentAlignment = Alignment.Center
        ) {
            when (type) {
                CelebrationType.DAILY_COMPLETE -> DailyCompleteCelebration(streakCount, onDismiss)
                CelebrationType.CHALLENGE_PROGRESS -> ChallengeProgressCelebration(onDismiss)
                CelebrationType.CHALLENGE_COMPLETE -> ChallengeCompleteCelebration(challengeName, onDismiss)
                CelebrationType.STREAK_MILESTONE -> StreakMilestoneCelebration(streakCount, onDismiss)
                CelebrationType.EPIC_ACHIEVEMENT -> EpicAchievementCelebration(onDismiss)
            }
            
            // Confetti for all celebrations
            if (type != CelebrationType.CHALLENGE_PROGRESS) {
                EpicConfettiExplosion(isVisible = true, onComplete = {})
            }
            
            // Fireworks for major celebrations
            if (type == CelebrationType.CHALLENGE_COMPLETE || type == CelebrationType.EPIC_ACHIEVEMENT) {
                FireworksShow(isVisible = true, onComplete = {})
            }
        }
    }
}

@Composable
private fun DailyCompleteCelebration(streakCount: Int, onDismiss: () -> Unit) {
    val scale = remember { Animatable(0f) }
    val rotation = remember { Animatable(0f) }
    
    LaunchedEffect(Unit) {
        scale.animateTo(1f, tween(500, easing = OvershootInterpolator().toEasing()))
        rotation.animateTo(360f, tween(1000))
        delay(3000)
        onDismiss()
    }
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .scale(scale.value)
            .padding(32.dp)
    ) {
        // Trophy with glow
        Box(contentAlignment = Alignment.Center) {
            // Glow effect
            Text(
                "🏆",
                fontSize = 120.sp,
                modifier = Modifier
                    .blur(20.dp)
                    .scale(1.2f)
            )
            Text(
                "🏆",
                fontSize = 120.sp,
                modifier = Modifier.rotate(rotation.value)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            "🔥 ALL GOALS COMPLETE! 🔥",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFFFFD700),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            "You're on FIRE! 💪",
            fontSize = 20.sp,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        
        if (streakCount > 0) {
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFFFF6B35).copy(alpha = 0.3f)
            ) {
                Text(
                    "🔥 $streakCount Day Streak! 🔥",
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF6B35)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            motivationalQuotes.random(),
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Composable
private fun ChallengeProgressCelebration(onDismiss: () -> Unit) {
    val scale = remember { Animatable(0f) }
    
    LaunchedEffect(Unit) {
        scale.animateTo(1f, tween(300, easing = OvershootInterpolator().toEasing()))
        delay(1500)
        onDismiss()
    }
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .scale(scale.value)
            .padding(32.dp)
    ) {
        Text("✅", fontSize = 80.sp)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            "Nice! Keep Going!",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryTeal
        )
        
        Text(
            "+1 Day Added 🎯",
            fontSize = 16.sp,
            color = Color.White.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun ChallengeCompleteCelebration(challengeName: String, onDismiss: () -> Unit) {
    val scale = remember { Animatable(0f) }
    val starRotation = remember { Animatable(0f) }
    val trophyScale = remember { Animatable(0f) }
    val glowAlpha = remember { Animatable(0f) }
    val badgeScale = remember { Animatable(0f) }
    val statsSlide = remember { Animatable(-300f) }
    
    // Pulsing glow animation
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val pulsingGlow by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    val goldenRain by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rain"
    )
    
    LaunchedEffect(Unit) {
        // Phase 1: Background glow
        glowAlpha.animateTo(1f, tween(500))
        
        // Phase 2: Trophy enters with bounce
        trophyScale.animateTo(1.2f, tween(400, easing = OvershootInterpolator().toEasing()))
        trophyScale.animateTo(1f, tween(200))
        
        // Phase 3: Main content scales in
        scale.animateTo(1f, tween(300, easing = OvershootInterpolator().toEasing()))
        
        // Phase 4: Badge unlocks
        delay(500)
        badgeScale.animateTo(1.3f, tween(300, easing = OvershootInterpolator().toEasing()))
        badgeScale.animateTo(1f, tween(200))
        
        // Phase 5: Stats slide in
        statsSlide.animateTo(0f, tween(500, easing = FastOutSlowInEasing))
        
        // Keep celebration visible for 8 seconds (EPIC duration!)
        delay(8000)
        onDismiss()
    }
    
    // Separate effect for star rotation (runs in parallel)
    LaunchedEffect(Unit) {
        delay(600) // Start after trophy appears
        starRotation.animateTo(720f, tween(3000))
    }
    
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Golden radial glow background
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFFD700).copy(alpha = pulsingGlow * 0.4f),
                        Color(0xFFFF8C00).copy(alpha = pulsingGlow * 0.2f),
                        Color.Transparent
                    ),
                    radius = size.minDimension * 0.8f
                ),
                radius = size.minDimension
            )
        }
        
        // Golden particles rain
        Canvas(modifier = Modifier.fillMaxSize()) {
            for (i in 0 until 50) {
                val x = (i * 37 % 100) / 100f * size.width
                val y = ((goldenRain + i * 0.1f) % 1.2f) * size.height
                val particleAlpha = (1f - (y / size.height)).coerceIn(0f, 1f)
                
                drawCircle(
                    color = Color(0xFFFFD700).copy(alpha = particleAlpha * 0.6f),
                    radius = 3f + (i % 3) * 2f,
                    center = Offset(x, y)
                )
            }
        }
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // 🌟 Spinning Stars Ring 🌟 (Reduced size for phones)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(180.dp)
            ) {
                // Outer ring of stars
                for (i in 0 until 8) {
                    val angle = i * 45f + starRotation.value
                    val radius = 70f
                    Text(
                        "⭐",
                        fontSize = 18.sp,
                        modifier = Modifier
                            .offset(
                                x = (cos(Math.toRadians(angle.toDouble())) * radius).dp,
                                y = (sin(Math.toRadians(angle.toDouble())) * radius).dp
                            )
                            .rotate(angle)
                    )
                }
                
                // Inner ring
                for (i in 0 until 4) {
                    val angle = i * 90f - starRotation.value * 0.5f
                    val radius = 60f
                    Text(
                        "✨",
                        fontSize = 20.sp,
                        modifier = Modifier
                            .offset(
                                x = (cos(Math.toRadians(angle.toDouble())) * radius).dp,
                                y = (sin(Math.toRadians(angle.toDouble())) * radius).dp
                            )
                    )
                }
                
                // 🏆 CENTER TROPHY with glow (Reduced for phones)
                Box(contentAlignment = Alignment.Center) {
                    // Glow effect
                    Text(
                        "🏆",
                        fontSize = 80.sp,
                        modifier = Modifier
                            .blur(20.dp)
                            .scale(trophyScale.value * 1.2f)
                    )
                    // Main trophy
                    Text(
                        "🏆",
                        fontSize = 80.sp,
                        modifier = Modifier.scale(trophyScale.value)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 🎊 CHALLENGE COMPLETED Banner
            Box(
                modifier = Modifier
                    .scale(scale.value)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFFFFD700),
                                Color(0xFFFFA500),
                                Color(0xFFFFD700)
                            )
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Text(
                    "🎊 CHALLENGE COMPLETED! 🎊",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Challenge Name
            Text(
                challengeName.uppercase(),
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.scale(scale.value)
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // 🏅 BADGE UNLOCK Animation
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.scale(badgeScale.value)
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFFFD700).copy(alpha = 0.2f),
                    modifier = Modifier.size(100.dp)
                ) {}
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🏅", fontSize = 50.sp)
                    Text(
                        "BADGE UNLOCKED!",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD700)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // 📊 STATS Section (slides in)
            Row(
                modifier = Modifier
                    .offset(x = statsSlide.value.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Days Completed
                StatCard(
                    emoji = "📅",
                    value = "DONE",
                    label = "All Days"
                )
                
                // Achievement
                StatCard(
                    emoji = "🔥",
                    value = "100%",
                    label = "Complete"
                )
                
                // Rank
                StatCard(
                    emoji = "👑",
                    value = "LEGEND",
                    label = "Status"
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Motivational Text
            Text(
                "\"You did what many only dream of!\"",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center,
                modifier = Modifier.scale(scale.value)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                "YOU'RE A TRUE CHAMPION! 👑",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFFFF6B35),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Share Button
            Surface(
                onClick = { /* Share functionality */ },
                shape = RoundedCornerShape(25.dp),
                color = PrimaryTeal.copy(alpha = 0.9f),
                modifier = Modifier.scale(scale.value)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("📢", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Share Achievement",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                "Tap anywhere to continue",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
private fun StatCard(emoji: String, value: String, label: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.1f)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(12.dp)
        ) {
            Text(emoji, fontSize = 24.sp)
            Text(
                value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFD700)
            )
            Text(
                label,
                fontSize = 10.sp,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun StreakMilestoneCelebration(days: Int, onDismiss: () -> Unit) {
    val scale = remember { Animatable(0f) }
    
    val (emoji, title) = when {
        days >= 365 -> "🎊" to "1 YEAR LEGEND!"
        days >= 100 -> "🏆" to "100 DAY MASTER!"
        days >= 75 -> "💎" to "75 DAY DIAMOND!"
        days >= 50 -> "🔥" to "50 DAY FIRE!"
        days >= 30 -> "👑" to "1 MONTH KING!"
        days >= 21 -> "💪" to "21 DAY WARRIOR!"
        days >= 14 -> "🌟" to "2 WEEK STAR!"
        days >= 7 -> "⭐" to "1 WEEK CHAMP!"
        else -> "🎯" to "$days DAY MILESTONE!"
    }
    
    LaunchedEffect(Unit) {
        scale.animateTo(1f, tween(500, easing = OvershootInterpolator().toEasing()))
        delay(4000)
        onDismiss()
    }
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .scale(scale.value)
            .padding(32.dp)
    ) {
        Text(emoji, fontSize = 100.sp)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            title,
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFFFFD700),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            "$days Days Strong! 💪",
            fontSize = 20.sp,
            color = Color.White,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun EpicAchievementCelebration(onDismiss: () -> Unit) {
    val scale = remember { Animatable(0f) }
    
    LaunchedEffect(Unit) {
        scale.animateTo(1f, tween(500, easing = OvershootInterpolator().toEasing()))
        delay(5000)
        onDismiss()
    }
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .scale(scale.value)
            .padding(32.dp)
    ) {
        Text("🎖️", fontSize = 100.sp)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            "EPIC ACHIEVEMENT!",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFFFFD700)
        )
        
        Text(
            "You're absolutely incredible!",
            fontSize = 18.sp,
            color = Color.White
        )
    }
}

// ==================== HELPER FUNCTIONS ====================

private fun triggerHapticFeedback(context: android.content.Context, type: CelebrationType) {
    try {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(android.content.Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as Vibrator
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val pattern = when (type) {
                CelebrationType.DAILY_COMPLETE -> longArrayOf(0, 100, 50, 100, 50, 200)
                CelebrationType.CHALLENGE_PROGRESS -> longArrayOf(0, 50, 50, 50)
                CelebrationType.CHALLENGE_COMPLETE -> longArrayOf(0, 100, 50, 100, 50, 100, 50, 300)
                CelebrationType.STREAK_MILESTONE -> longArrayOf(0, 100, 50, 100, 50, 200)
                CelebrationType.EPIC_ACHIEVEMENT -> longArrayOf(0, 100, 50, 100, 50, 100, 50, 100, 50, 400)
            }
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(200)
        }
    } catch (e: Exception) {
        // Ignore vibration errors
    }
}

private fun OvershootInterpolator(): android.view.animation.OvershootInterpolator {
    return android.view.animation.OvershootInterpolator(2f)
}

private fun android.view.animation.Interpolator.toEasing(): Easing {
    return Easing { x -> this.getInterpolation(x) }
}

private val motivationalQuotes = listOf(
    "\"The secret of getting ahead is getting started.\" - Mark Twain",
    "\"Believe you can and you're halfway there.\" - Theodore Roosevelt",
    "\"You are capable of amazing things!\"",
    "\"Success is the sum of small efforts repeated daily.\"",
    "\"The only way to do great work is to love what you do.\"",
    "\"Your potential is endless. Keep going!\"",
    "\"Champions are made when no one is watching.\"",
    "\"Every accomplishment starts with the decision to try.\""
)
