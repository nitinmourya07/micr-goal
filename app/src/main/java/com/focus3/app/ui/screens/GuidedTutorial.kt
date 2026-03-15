package com.focus3.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focus3.app.ui.theme.DarkBackground
import com.focus3.app.ui.theme.NeonCyan
import com.focus3.app.ui.theme.PrimaryTeal
import kotlin.random.Random

data class TutorialStep(
    val targetArea: String,
    val title: String,
    val description: String,
    val icon: String,
    val accentColor: Color = PrimaryTeal
)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun GuidedTutorial(
    onComplete: () -> Unit
) {
    val steps = remember {
        listOf(
        TutorialStep(
            "welcome",
            "Welcome to Focus3! 🎯",
            "Your 3-goal productivity companion! Every day, set just 3 important goals and watch yourself become unstoppable. Let's learn how to master this app!",
            "🎯",
            PrimaryTeal
        ),
        TutorialStep(
            "goals",
            "Set 3 Daily Goals ✏️",
            "Tap any goal card → Type your goal → That's it! Keep goals small and achievable. Example: 'Drink 8 glasses water' instead of 'Get healthy'",
            "✏️",
            PrimaryTeal
        ),
        TutorialStep(
            "complete",
            "Check Them Off! ✅",
            "Tap the circle to mark complete! Watch the celebration animation 🎉 Your progress ring fills up as you crush your goals!",
            "✅",
            Color(0xFF4CAF50)
        ),
        TutorialStep(
            "streak",
            "Build Your Streak 🔥",
            "Complete ALL 3 goals daily = +1 Streak! Don't break the chain! Streak milestones: 7, 14, 30, 60, 100 days with rewards!",
            "🔥",
            Color(0xFFFF6D00)
        ),
        TutorialStep(
            "grace",
            "Streak Protection 🛡️",
            "Life happens! You get 3 GRACE DAYS - if you miss a day, your streak is protected. Use them wisely, they don't refill!",
            "🛡️",
            Color(0xFF2196F3)
        ),
        TutorialStep(
            "challenges",
            "Long-Term Challenges 🏆",
            "Tap MISSIONS tab → Create challenges like '30 Day Workout' or 'Read 10 Books'. Perfect for bigger goals with their own journey tracking!",
            "🏆",
            Color(0xFF9C27B0)
        ),
        TutorialStep(
            "analytics",
            "Track Your Progress 📊",
            "Tap STREAKS tab → See your completion history, weekly chart, and streak stats. Review every Sunday to stay on track!",
            "📊",
            NeonCyan
        ),
        TutorialStep(
            "notes",
            "Notes & Ideas 📝",
            "Tap ARCHIVE tab → Save your thoughts, ideas, and reflections. Pin important notes, color-code them, and never lose a brilliant idea!",
            "📝",
            Color(0xFFFF9800)
        ),
        TutorialStep(
            "calendar",
            "Calendar View 📅",
            "Tap LOGS tab → See your monthly heatmap! Green = completed days, tap any date to add notes about what you achieved!",
            "📅",
            Color(0xFFE91E63)
        ),
        TutorialStep(
            "ready",
            "You're Ready! 🚀",
            "Start with just ONE goal today. Small wins create big momentum! Focus on consistency, not perfection. Let's GO!",
            "🚀",
            Color(0xFFFFD700)
        )
        )
    }
    
    var currentStep by rememberSaveable { mutableStateOf(0) }
    val proTips = remember {
        listOf(
            "Pro tip: 3 goals max keeps focus high.",
            "Pro tip: tap card to edit, circle to complete.",
            "Pro tip: small wins build momentum.",
            "Pro tip: protect your streak daily.",
            "Pro tip: use grace days only when needed.",
            "Pro tip: challenges are for long-term growth.",
            "Pro tip: weekly review improves consistency.",
            "Pro tip: notes capture useful context.",
            "Pro tip: calendar trends reveal habits.",
            "Pro tip: consistency beats intensity."
        )
    }
    
    // Multiple animation effects
    val infiniteTransition = rememberInfiniteTransition(label = "tutorial_anim")
    
    // FAST Pulse animation for icon
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    // FAST Glow rotation
    val glowRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "glowRotation"
    )
    // FAST Floating effect
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0A0A0A),
                        Color(0xFF1A1A2E),
                        Color(0xFF0D0D0D)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Background floating particles
        FloatingParticles()
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Welcome Header (only on first step)
            if (currentStep == 0) {
                Text(
                    "🎯 Welcome to Focus3",
                    fontSize = 14.sp,
                    color = PrimaryTeal,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // Enhanced Step Progress
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "📖 Tutorial",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Medium
                )
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = steps[currentStep].accentColor.copy(alpha = 0.2f)
                ) {
                    Text(
                        "${currentStep + 1}/${steps.size}",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        color = steps[currentStep].accentColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color.White.copy(alpha = 0.1f))
            ) {
                val progress = (currentStep + 1).toFloat() / steps.size
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    steps[currentStep].accentColor,
                                    steps[currentStep].accentColor.copy(alpha = 0.7f)
                                )
                            )
                        )
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Step indicator dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                repeat(steps.size) { index ->
                    val indicatorScale by animateFloatAsState(
                        targetValue = if (index == currentStep) 1.2f else 1f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                        label = "indicator_$index"
                    )
                    
                    Box(
                        modifier = Modifier
                            .size(
                                width = if (index == currentStep) 20.dp else 8.dp, 
                                height = 8.dp
                            )
                            .scale(indicatorScale)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (index <= currentStep) 
                                    steps[currentStep].accentColor
                                else 
                                    Color.White.copy(alpha = 0.15f)
                            )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Main content with slide animation
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    // Slide from right when going forward, left when going back
                    val slideDirection = if (targetState > initialState) 1 else -1
                    
                    (slideInHorizontally(
                        initialOffsetX = { it * slideDirection },
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    ) + fadeIn(
                        animationSpec = tween(400)
                    )).togetherWith(
                        slideOutHorizontally(
                            targetOffsetX = { -it * slideDirection },
                            animationSpec = tween(300)
                        ) + fadeOut(
                            animationSpec = tween(200)
                        )
                    )
                },
                label = "stepContent"
            ) { step ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.offset(y = floatOffset.dp)
                ) {
                    // Animated icon with rotating glow
                    Box(
                        modifier = Modifier.size(140.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Outer rotating glow
                        Box(
                            modifier = Modifier
                                .size(130.dp)
                                .rotate(glowRotation)
                                .clip(CircleShape)
                                .background(
                                    Brush.sweepGradient(
                                        colors = listOf(
                                            steps[step].accentColor.copy(alpha = 0.4f),
                                            Color.Transparent,
                                            steps[step].accentColor.copy(alpha = 0.2f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )
                        
                        // Inner glow
                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .scale(pulseScale)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            steps[step].accentColor.copy(alpha = 0.3f),
                                            steps[step].accentColor.copy(alpha = 0.1f),
                                            Color.Transparent
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = steps[step].icon,
                                fontSize = 56.sp
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // Title with gradient shimmer effect
                    Text(
                        text = steps[step].title,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Black
                        ),
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Step counter
                    Text(
                        text = "Step ${step + 1} of ${steps.size}",
                        style = MaterialTheme.typography.labelMedium,
                        color = steps[step].accentColor.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Description with better styling
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                            .padding(20.dp)
                    ) {
                        Text(
                            text = steps[step].description,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White.copy(alpha = 0.85f),
                            textAlign = TextAlign.Center,
                            lineHeight = 26.sp
                        )
                    }
                    
                    // Pro Tip - updated with more tips
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = steps[step].accentColor.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = proTips.getOrElse(step) { proTips.last() },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            fontSize = 12.sp,
                            color = steps[step].accentColor,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Navigation buttons with better styling
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back/Skip button
                TextButton(
                    onClick = { 
                        if (currentStep > 0) currentStep-- 
                        else onComplete()
                    }
                ) {
                    Text(
                        text = if (currentStep > 0) "← Back" else "Skip",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 16.sp
                    )
                }
                
                // Next/Finish button with animation
                val buttonScale by animateFloatAsState(
                    targetValue = if (currentStep == steps.lastIndex) 1.05f else 1f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                    label = "buttonScale"
                )
                
                Button(
                    onClick = { 
                        if (currentStep < steps.lastIndex) currentStep++ 
                        else onComplete()
                    },
                    modifier = Modifier
                        .scale(buttonScale)
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = steps[currentStep].accentColor,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 8.dp
                    )
                ) {
                    Text(
                        text = if (currentStep < steps.lastIndex) "Next →" else "Let's Go! 🚀",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// Floating particles background effect
@Composable
fun FloatingParticles() {
    val particles = remember {
        List(15) {
            Particle(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                size = Random.nextFloat() * 6f + 2f,
                alpha = Random.nextFloat() * 0.3f + 0.1f,
                speed = Random.nextFloat() * 0.5f + 0.2f
            )
        }
    }
    
    val infiniteTransition = rememberInfiniteTransition(label = "particles")
    
    val animProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particleAnim"
    )
    
    Box(modifier = Modifier.fillMaxSize()) {
        particles.forEach { particle ->
            val yOffset = ((particle.y + animProgress * particle.speed) % 1f)
            val xWobble = kotlin.math.sin(animProgress * 6.28f * particle.speed) * 0.02f
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .offset(
                        x = ((particle.x + xWobble) * 400).dp,
                        y = (yOffset * 800).dp
                    )
            ) {
                Box(
                    modifier = Modifier
                        .size(particle.size.dp)
                        .alpha(particle.alpha)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    PrimaryTeal.copy(alpha = 0.6f),
                                    Color.Transparent
                                )
                            )
                        )
                )
            }
        }
    }
}

data class Particle(
    val x: Float,
    val y: Float,
    val size: Float,
    val alpha: Float,
    val speed: Float
)
