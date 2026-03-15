package com.focus3.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.animation.core.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.focus3.app.ui.theme.GlassBorder
import com.focus3.app.ui.theme.GlassSurface
import com.focus3.app.ui.theme.PrimaryTeal
import com.focus3.app.ui.theme.NeonCyan

/**
 * Premium Glassmorphism 2.0 Container
 * Features: Frosted glass effect, animated glow sweep, inner edge light, depth layers
 */
@Composable
fun GlassBox(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    blurRadius: Dp = 16.dp,
    showGlow: Boolean = false,
    glowColor: Color = PrimaryTeal,
    elevated: Boolean = false,
    content: @Composable () -> Unit
) {
    // Animated sweep glow for active/highlighted cards
    val sweepAngle = if (showGlow) {
        val infiniteTransition = rememberInfiniteTransition(label = "glass_glow")
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(3000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "sweep"
        ).value
    } else {
        0f
    }

    // No remember() here — sweepAngle changes every frame so remember is wasteful
    val borderBrush = if (showGlow) {
        val angle = sweepAngle / 360f
        Brush.sweepGradient(
            0.0f to Color.Transparent,
            angle to glowColor.copy(alpha = 0.85f),
            (angle + 0.12f).coerceAtMost(1f) to glowColor.copy(alpha = 0.3f),
            (angle + 0.2f).coerceAtMost(1f) to Color.Transparent,
            1.0f to Color.Transparent
        )
    } else {
        remember {
            Brush.verticalGradient(
                colors = listOf(
                    GlassBorder.copy(alpha = 0.18f),
                    GlassBorder.copy(alpha = 0.06f)
                )
            )
        }
    }

    // Surface gradient — deeper for elevated cards
    val surfaceBrush = remember(elevated) {
        if (elevated) {
            Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.07f),
                    Color.White.copy(alpha = 0.03f),
                    Color.White.copy(alpha = 0.01f)
                )
            )
        } else {
            Brush.verticalGradient(
                colors = listOf(
                    GlassSurface,
                    GlassSurface.copy(alpha = 0.25f)
                )
            )
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(brush = surfaceBrush)
            .border(
                width = 1.dp,
                brush = borderBrush,
                shape = RoundedCornerShape(cornerRadius)
            )
    ) {
        // Top edge highlight — mimics light hitting frosted glass from above
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = if (showGlow) 0.12f else 0.06f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Inner edge light for depth perception
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(1.dp)
                .clip(RoundedCornerShape(cornerRadius))
                .border(
                    width = 0.5.dp,
                    color = Color.White.copy(alpha = 0.04f),
                    shape = RoundedCornerShape(cornerRadius)
                )
        )
        content()
    }
}
