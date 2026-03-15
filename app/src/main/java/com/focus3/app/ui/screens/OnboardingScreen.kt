package com.focus3.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import com.focus3.app.ui.theme.DarkBackground
import com.focus3.app.ui.theme.NeonCyan
import com.focus3.app.ui.theme.PrimaryTeal
import kotlinx.coroutines.launch

data class OnboardingPage(
    val emoji: String,
    val title: String,
    val description: String
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit
) {
    val pages = remember { listOf(
        OnboardingPage(
            emoji = "🎯",
            title = "Focus on 3 Goals",
            description = "Research shows focusing on just 3 goals per day leads to better results than trying to do everything."
        ),
        OnboardingPage(
            emoji = "🔥",
            title = "Build Streaks",
            description = "Complete all 3 goals daily to build your streak. Watch your consistency grow over time!"
        ),
        OnboardingPage(
            emoji = "📊",
            title = "Track Progress",
            description = "View your history, celebrate wins, and stay motivated with beautiful analytics."
        ),
        OnboardingPage(
            emoji = "⏱️",
            title = "Focus Mode",
            description = "Use the built-in Pomodoro timer to deep work on your goals without distractions."
        )
    ) }
    
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0A0F1A),
                        DarkBackground,
                        Color(0xFF0A0A10)
                    )
                )
            )
    ) {
        // ✨ Atmospheric Particles
        val infiniteTransition = rememberInfiniteTransition(label = "particles")
        val animateOffset by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(10000, easing = LinearEasing))
        )
        
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            
            // Draw floating particles
            listOf(
                Offset(0.2f, 0.3f) to 2.dp,
                Offset(0.8f, 0.15f) to 3.dp,
                Offset(0.5f, 0.6f) to 1.5.dp,
                Offset(0.9f, 0.8f) to 2.5.dp,
                Offset(0.1f, 0.9f) to 4.dp
            ).forEachIndexed { i, (relPos, radius) ->
                val yOffset = (animateOffset * h * 0.2f * (if(i%2==0) 1 else -1))
                val center = Offset(
                    x = w * relPos.x,
                    y = (h * relPos.y + yOffset) % h
                )
                
                drawCircle(
                    color = if(i % 2 == 0) PrimaryTeal.copy(alpha=0.1f) else Color.White.copy(alpha=0.05f),
                    radius = radius.toPx(),
                    center = center
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            
            // App Branding Header
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "FOCUS3",
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 4.sp
                    ),
                    color = Color.White
                )
                Text(
                    "DAILY GOAL COMPANION",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    ),
                    color = PrimaryTeal
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Pager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                OnboardingPageContent(pages[page])
            }
            
            // Enhanced page indicators with progress
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    repeat(pages.size) { index ->
                        val isActive = pagerState.currentPage == index
                        Box(
                            modifier = Modifier
                                .size(if (isActive) 32.dp else 10.dp, 6.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isActive) PrimaryTeal 
                                    else Color.White.copy(alpha = 0.12f)
                                )
                        )
                    }
                }
                Text(
                    "STEP ${pagerState.currentPage + 1} OF ${pages.size}",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                    color = Color.White.copy(alpha = 0.2f)
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Buttons
            if (pagerState.currentPage == pages.lastIndex) {
                Button(
                    onClick = onComplete,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryTeal,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(20.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Text(
                        "INITIALIZE CORE SYSTEMS",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black)
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onComplete) {
                        Text(
                            "SKIP",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                            color = Color.White.copy(alpha = 0.2f)
                        )
                    }
                    
                    Button(
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        },
                        modifier = Modifier.height(56.dp).width(120.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryTeal,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                    ) {
                        Text(
                            "NEXT",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun OnboardingPageContent(page: OnboardingPage) {
    // Pulse animation for emoji
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    // Feature badges based on page
    val featureBadges = mapOf(
        "🎯" to listOf("Simple", "Effective", "Research-Based"),
        "🔥" to listOf("Motivating", "Visual", "Rewarding"),
        "📊" to listOf("30-Day Heatmap", "Charts", "History"),
        "⏱️" to listOf("25 min Focus", "5 min Break", "Auto-Timer")
    )
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Emoji with animated glow effect
        Box(
            modifier = Modifier
                .size(160.dp)
                .graphicsLayer { scaleX = scale; scaleY = scale },
            contentAlignment = Alignment.Center
        ) {
            // Animated Glow
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(PrimaryTeal.copy(alpha = 0.2f), Color.Transparent)
                    ),
                    radius = this.size.width / 2
                )
            }
            
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.03f))
                    .border(1.dp, Color.White.copy(alpha = 0.05f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = page.emoji,
                    fontSize = 56.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Text(
            text = page.title.uppercase(),
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = (-0.5).sp
            ),
            color = Color.White,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Feature badges
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            featureBadges[page.emoji]?.forEach { badge ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(32.dp))
                        .background(PrimaryTeal.copy(alpha = 0.08f))
                        .border(1.dp, PrimaryTeal.copy(alpha = 0.15f), RoundedCornerShape(32.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        badge.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                        color = PrimaryTeal
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.4f),
            textAlign = TextAlign.Center,
            lineHeight = 24.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

