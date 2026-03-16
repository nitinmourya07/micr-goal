package com.focus3.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
 * Premium Non-blocking Confetti Toast
 * Glass-morphic toast with glow effects + confetti
 * BUG FIXES: emoji corruption, broken layout, popup positioning
 */
@Composable
fun ConfettiOverlay(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    message: String = "\uD83C\uDF89 Goal Complete!",
    isCelebrationShowing: Boolean = false // Suppress if full celebration is active
) {
    // Don't show small popup if the full celebration overlay is active
    if (isCelebrationShowing) return

    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.confetti)
    )

    val progress by animateLottieCompositionAsState(
        composition = composition,
        isPlaying = isVisible,
        restartOnPlay = true
    )

    // Toast slide-in animation
    val toastOffset = remember { Animatable(100f) }
    val toastAlpha = remember { Animatable(0f) }

    // Pulsing glow effect
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.03f,
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
            // Entrance animation
            launch { toastAlpha.animateTo(1f, tween(300)) }
            toastOffset.animateTo(
                0f,
                spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        }
    }

    // Auto-dismiss after 2.5 seconds
    LaunchedEffect(isVisible) {
        if (isVisible) {
            delay(2500)
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(300)
        ) + fadeIn(tween(300)),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(250)
        ) + fadeOut(tween(250))
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
                    .fillMaxHeight(0.5f)
                    .align(Alignment.TopCenter)
            )

            // FIXED POPUP — positioned ABOVE bottom nav with proper padding
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 90.dp) // Clear the bottom navigation bar
                    .offset(y = toastOffset.value.dp)
                    .graphicsLayer {
                        alpha = toastAlpha.value
                        scaleX = pulseScale
                        scaleY = pulseScale
                    }
            ) {
                // Outer glow effect
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .blur(15.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    PrimaryTeal.copy(alpha = glowPulse * 0.3f),
                                    Color.Transparent
                                )
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )
                )

                // FIXED LAYOUT: Proper Card with weights/spacing
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1A2A2A)
                    ),
                    border = BorderStroke(
                        1.dp,
                        Brush.horizontalGradient(
                            colors = listOf(
                                PrimaryTeal.copy(alpha = 0.6f),
                                NeonCyan.copy(alpha = 0.6f),
                                PrimaryTeal.copy(alpha = 0.6f)
                            )
                        )
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // FIXED: Unicode emoji instead of corrupted string
                        Text(
                            text = "\u2705", // ✅
                            fontSize = 20.sp
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Task Done!",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Keep going! \uD83D\uDD25", // 🔥
                                color = PrimaryTeal,
                                fontSize = 12.sp
                            )
                        }
                        // FIXED: Unicode emoji
                        Text(
                            text = "\uD83C\uDFAF", // 🎯
                            fontSize = 20.sp
                        )
                    }
                }
            }
        }
    }
}
