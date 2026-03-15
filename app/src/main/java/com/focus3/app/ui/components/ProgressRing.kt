package com.focus3.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focus3.app.ui.theme.NeonCyan
import com.focus3.app.ui.theme.PrimaryTeal
import com.focus3.app.ui.theme.ProgressRemaining
import com.focus3.app.ui.theme.CompletedGreen

@Composable
fun ProgressRing(
    progress: Float, // 0.0 to 1.0
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    strokeWidth: Dp = 12.dp,
    showPercentage: Boolean = true
) {
    val animatedProgress = remember { Animatable(0f) }
    val isComplete = progress >= 1f

    // Smooth progress animation
    LaunchedEffect(progress) {
        animatedProgress.animateTo(
            targetValue = progress,
            animationSpec = tween(
                durationMillis = 700,
                easing = FastOutSlowInEasing
            )
        )
    }

    // Pulsing glow when fully complete
    val glowPulse = if (isComplete) {
        val infiniteTransition = rememberInfiniteTransition(label = "ring_glow")
        val pulse by infiniteTransition.animateFloat(
            initialValue = 0.3f,
            targetValue = 0.7f,
            animationSpec = infiniteRepeatable(
                animation = tween(2500, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "glow_pulse"
        )
        pulse
    } else {
        0f
    }

    // Determine ring color based on progress
    val progressColors = remember(progress) {
        when {
            progress >= 1f -> listOf(CompletedGreen, Color(0xFF69F0AE), CompletedGreen)
            progress >= 0.66f -> listOf(PrimaryTeal, NeonCyan, PrimaryTeal)
            else -> listOf(PrimaryTeal, NeonCyan, PrimaryTeal)
        }
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val ringStroke = strokeWidth.toPx()

            // Background track
            drawArc(
                color = ProgressRemaining,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = ringStroke, cap = StrokeCap.Round)
            )

            // Outer glow effect (wider, softer)
            if (animatedProgress.value > 0f) {
                val glowAlpha = if (isComplete) glowPulse else 0.2f
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = progressColors.map { it.copy(alpha = glowAlpha) }
                    ),
                    startAngle = -90f,
                    sweepAngle = animatedProgress.value * 360f,
                    useCenter = false,
                    style = Stroke(width = (strokeWidth + 10.dp).toPx(), cap = StrokeCap.Round)
                )
            }

            // Main progress arc with gradient
            if (animatedProgress.value > 0f) {
                drawArc(
                    brush = Brush.sweepGradient(colors = progressColors),
                    startAngle = -90f,
                    sweepAngle = animatedProgress.value * 360f,
                    useCenter = false,
                    style = Stroke(width = ringStroke, cap = StrokeCap.Round)
                )
            }

            // Bright tip dot at the end of progress arc
            if (animatedProgress.value > 0.02f) {
                val angle = Math.toRadians((-90.0 + animatedProgress.value * 360.0))
                val radius = (this.size.minDimension / 2f) - ringStroke / 2f
                val tipX = center.x + (radius * kotlin.math.cos(angle)).toFloat()
                val tipY = center.y + (radius * kotlin.math.sin(angle)).toFloat()

                // Glow behind tip
                drawCircle(
                    color = progressColors[1].copy(alpha = 0.4f),
                    radius = ringStroke,
                    center = Offset(tipX, tipY)
                )
                // Bright tip
                drawCircle(
                    color = Color.White,
                    radius = ringStroke * 0.35f,
                    center = Offset(tipX, tipY)
                )
            }
        }

        // Percentage text
        if (showPercentage) {
            Text(
                text = "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-1).sp
                ),
                color = if (isComplete) CompletedGreen else Color.White
            )
        }
    }
}
