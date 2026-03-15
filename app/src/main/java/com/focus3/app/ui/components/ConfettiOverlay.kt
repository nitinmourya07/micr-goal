package com.focus3.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.focus3.app.R
import com.focus3.app.ui.theme.PrimaryTeal
import com.focus3.app.ui.theme.NeonCyan
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * ðŸŽ‰ NEXT-LEVEL Non-blocking Confetti Toast
 * Premium glass-morphic toast with glow effects + confetti
 */
@Composable
fun ConfettiOverlay(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    message: String = "\uD83C\uDF89 Goal Complete!"
) {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.confetti)
    )
    
    val progress by animateLottieCompositionAsState(
        composition = composition,
        isPlaying = isVisible,
        restartOnPlay = true
    )
    
    // Toast animations
    val toastOffset = remember { Animatable(150f) }
    val toastScale = remember { Animatable(0.8f) }
    val glowAlpha = remember { Animatable(0f) }
    
    // Pulsing glow effect
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowPulse"
    )
    
    LaunchedEffect(isVisible) {
        if (isVisible) {
            // Entrance animation sequence - run in parallel
            launch {
                glowAlpha.animateTo(1f, tween(300))
            }
            launch {
                toastScale.animateTo(
                    1f,
                    spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )
            }
            toastOffset.animateTo(
                0f,
                spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
    }
    
    // Auto-dismiss after animation completes
    LaunchedEffect(progress) {
        if (progress >= 0.95f && isVisible) {
            delay(300)
            onDismiss()
        }
    }
    
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(200)),
        exit = fadeOut(tween(300))
    ) {
        Box(
            modifier = modifier.fillMaxSize()
        ) {
            // Confetti animation (top area only)
            LottieAnimation(
                composition = composition,
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.6f)
                    .align(Alignment.TopCenter)
            )
            
            // PREMIUM TOAST - Glass morphic with glow
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 120.dp)
                    .offset(y = toastOffset.value.dp)
                    .graphicsLayer {
                        scaleX = toastScale.value * pulseScale
                        scaleY = toastScale.value * pulseScale
                    }
            ) {
                // Outer glow effect
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .blur(20.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    PrimaryTeal.copy(alpha = glowPulse * glowAlpha.value),
                                    NeonCyan.copy(alpha = glowPulse * 0.5f * glowAlpha.value),
                                    Color.Transparent
                                )
                            ),
                            shape = RoundedCornerShape(30.dp)
                        )
                )
                
                // Main toast container with glass effect
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(30.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF0D1B2A).copy(alpha = 0.95f),
                                    Color(0xFF1B263B).copy(alpha = 0.95f)
                                )
                            )
                        )
                        .border(
                            width = 1.5.dp,
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    PrimaryTeal.copy(alpha = 0.6f),
                                    NeonCyan.copy(alpha = 0.6f),
                                    PrimaryTeal.copy(alpha = 0.6f)
                                )
                            ),
                            shape = RoundedCornerShape(30.dp)
                        )
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Animated check icon
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(PrimaryTeal, NeonCyan.copy(alpha = 0.7f))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "âœ“",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(14.dp))
                    
                    // Message text
                    Column {
                        Text(
                            text = message,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            ),
                            color = Color.White
                        )
                        Text(
                            text = "Keep going! ðŸ’ª",
                            style = MaterialTheme.typography.bodySmall,
                            color = PrimaryTeal.copy(alpha = 0.9f)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    // Sparkle effect
                    Text(
                        "âœ¨",
                        fontSize = 24.sp,
                        modifier = Modifier.graphicsLayer { scaleX = pulseScale; scaleY = pulseScale }
                    )
                }
            }
        }
    }
}

