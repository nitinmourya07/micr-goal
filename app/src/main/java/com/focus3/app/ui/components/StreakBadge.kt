package com.focus3.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focus3.app.ui.theme.StreakOrange
import com.focus3.app.ui.theme.StreakYellow
import com.focus3.app.ui.theme.TextPrimary
import com.focus3.app.ui.theme.PrimaryTeal
import com.focus3.app.ui.theme.NeonCyan
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.delay

@Composable
fun StreakBadge(
    streak: Int,
    modifier: Modifier = Modifier
) {
    var animatedStreak by remember { mutableStateOf(0) }
    var showBounce by remember { mutableStateOf(false) }
    
    LaunchedEffect(streak) {
        if (streak > animatedStreak) {
            showBounce = true
            delay(300)
            showBounce = false
        }
        animatedStreak = streak
    }
    
    // FAST scale animation with high stiffness
    val scale by animateFloatAsState(
        targetValue = if (showBounce) 1.2f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessHigh // Fast snapping
        ),
        label = "streakScale"
    )
    
    // Pulsing glow — only for streak milestones (7+) to save GPU
    val glowAlpha = if (streak >= 7) {
        val infiniteTransition = rememberInfiniteTransition(label = "streakPulse")
        val animated by infiniteTransition.animateFloat(
            initialValue = 0.3f,
            targetValue = 0.6f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "glowAlpha"
        )
        animated
    } else {
        0.35f // Static subtle glow for lower streaks
    }
    
    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        StreakOrange.copy(alpha = 0.2f),
                        StreakYellow.copy(alpha = 0.15f)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        StreakOrange.copy(alpha = glowAlpha),
                        StreakYellow.copy(alpha = glowAlpha * 0.8f)
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Fire emoji with glow effect
            Text(
                text = "🔥",
                fontSize = 28.sp
            )
            
            Spacer(modifier = Modifier.width(10.dp))
            
            // Streak count with gradient text effect (simulated)
            Text(
                text = streak.toString(),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Black,
                    color = StreakYellow
                )
            )
            
            Spacer(modifier = Modifier.width(6.dp))
            
            Text(
                text = if (streak == 1) "DAY" else "DAYS",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )
            )
        }
    }
}

@Composable
fun StreakEmptyState(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        PrimaryTeal.copy(alpha = 0.1f),
                        NeonCyan.copy(alpha = 0.05f)
                    )
                )
            )
            .border(
                width = 1.dp,
                color = PrimaryTeal.copy(alpha = 0.2f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🎯",
                fontSize = 24.sp
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Text(
                text = "INITIATE STREAK",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = PrimaryTeal
                )
            )
        }
    }
}
