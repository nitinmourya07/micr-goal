package com.focus3.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Premium Shimmer Loading Effect
 * Used for skeleton loading states
 */
@Composable
fun ShimmerEffect(
    modifier: Modifier = Modifier,
    widthFraction: Float = 1f,
    height: Dp = 60.dp,
    cornerRadius: Dp = 16.dp
) {
    val shimmerColors = listOf(
        Color.White.copy(alpha = 0.05f),
        Color.White.copy(alpha = 0.15f),
        Color.White.copy(alpha = 0.05f)
    )

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim - 200f, translateAnim - 200f),
        end = Offset(translateAnim, translateAnim)
    )

    Box(
        modifier = modifier
            .fillMaxWidth(widthFraction)
            .height(height)
            .clip(RoundedCornerShape(cornerRadius))
            .background(brush)
    )
}

/**
 * Shimmer Goal Card - Loading state for goal cards
 */
@Composable
fun ShimmerGoalCard(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.03f))
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Circle placeholder
            ShimmerEffect(
                modifier = Modifier.size(32.dp),
                widthFraction = 1f,
                height = 32.dp,
                cornerRadius = 16.dp
            )
            
            // Text placeholder
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ShimmerEffect(
                    widthFraction = 0.7f,
                    height = 16.dp,
                    cornerRadius = 8.dp
                )
                ShimmerEffect(
                    widthFraction = 0.4f,
                    height = 12.dp,
                    cornerRadius = 6.dp
                )
            }
            
            // Checkbox placeholder
            ShimmerEffect(
                modifier = Modifier.size(28.dp),
                widthFraction = 1f,
                height = 28.dp,
                cornerRadius = 14.dp
            )
        }
    }
}

/**
 * Loading state with 3 shimmer goal cards
 */
@Composable
fun ShimmerGoalsList() {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        repeat(3) {
            ShimmerGoalCard()
        }
    }
}
