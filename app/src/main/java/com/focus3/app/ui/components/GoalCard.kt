package com.focus3.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focus3.app.data.model.DailyTask
import com.focus3.app.ui.theme.CheckboxChecked
import com.focus3.app.ui.theme.PrimaryTeal
import com.focus3.app.ui.theme.NeonCyan
import com.focus3.app.ui.theme.CompletedGreen
import com.focus3.app.ui.theme.CompletedGreenLight
import androidx.compose.ui.graphics.Brush
import kotlinx.coroutines.delay

@Composable
fun GoalCard(
    task: DailyTask,
    goalNumber: Int,
    onContentChange: (String) -> Unit,
    onToggleComplete: () -> Unit,
    modifier: Modifier = Modifier,
    animationDelay: Int = 0
) {
    var isVisible by remember { mutableStateOf(false) }
    var localContent by remember(task.id) { mutableStateOf(task.content) }

    LaunchedEffect(Unit) {
        delay(animationDelay.toLong())
        isVisible = true
    }

    // Completion shimmer animation
    val shimmerOffset = if (task.isCompleted) {
        val transition = rememberInfiniteTransition(label = "shimmer_$goalNumber")
        val offset by transition.animateFloat(
            initialValue = -1f,
            targetValue = 2f,
            animationSpec = infiniteRepeatable(
                animation = tween(3000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "shimmer_offset"
        )
        offset
    } else {
        -1f
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            initialOffsetY = { h -> h / 4 },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessHigh
            )
        ) + fadeIn(tween(100))
    ) {
        GlassBox(
            modifier = modifier
                .fillMaxWidth()
                .padding(bottom = 14.dp),
            cornerRadius = 24.dp,
            showGlow = task.isCompleted && task.content.isNotBlank(),
            glowColor = CompletedGreen
        ) {
            // Subtle shimmer overlay for completed tasks
            if (task.isCompleted && task.content.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(24.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    CompletedGreen.copy(alpha = 0.06f),
                                    Color.Transparent
                                ),
                                startX = shimmerOffset * 1000f,
                                endX = (shimmerOffset + 0.5f) * 1000f
                            )
                        )
                )
            }

            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Goal Number Circle — Premium Gradient
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(
                            if (task.isCompleted)
                                Brush.linearGradient(
                                    colors = listOf(CompletedGreen, CompletedGreenLight)
                                )
                            else
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.12f),
                                        Color.White.copy(alpha = 0.06f)
                                    )
                                )
                        )
                        .border(
                            width = if (task.isCompleted) 0.dp else 1.dp,
                            color = Color.White.copy(alpha = 0.08f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (task.isCompleted) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(18.dp)
                        )
                    } else {
                        Text(
                            text = goalNumber.toString(),
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Black
                            ),
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Input Field
                BasicTextField(
                    value = localContent,
                    onValueChange = {
                        localContent = it
                        onContentChange(it)
                    },
                    modifier = Modifier.weight(1f),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = if (task.isCompleted) Color.White.copy(alpha = 0.5f) else Color.White,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                        fontWeight = if (task.content.isNotBlank()) FontWeight.Medium else FontWeight.Normal
                    ),
                    cursorBrush = SolidColor(PrimaryTeal),
                    decorationBox = { innerTextField ->
                        if (localContent.isEmpty()) {
                            Text(
                                "What's goal #$goalNumber?",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White.copy(alpha = 0.18f)
                            )
                        }
                        innerTextField()
                    }
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Checkbox
                CustomCheckbox(
                    checked = task.isCompleted,
                    onCheckedChange = { onToggleComplete() }
                )
            }
        }
    }
}

@Composable
fun CustomCheckbox(
    checked: Boolean,
    onCheckedChange: () -> Unit,
    color: Color = PrimaryTeal
) {
    val haptic = LocalHapticFeedback.current

    // Scale with fast spring
    val scale by animateFloatAsState(
        targetValue = if (checked) 1.15f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "checkbox_scale"
    )

    // Background color
    val activeColor = if (checked) CompletedGreen else color
    val backgroundColor by animateColorAsState(
        targetValue = if (checked) activeColor else Color.Transparent,
        animationSpec = tween(120),
        label = "checkbox_bg"
    )

    // Border color
    val borderColor by animateColorAsState(
        targetValue = if (checked) activeColor else Color.White.copy(alpha = 0.25f),
        animationSpec = tween(120),
        label = "checkbox_border"
    )

    // Glow effect
    val glowScale by animateFloatAsState(
        targetValue = if (checked) 1.6f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "checkbox_glow"
    )

    val glowAlpha by animateFloatAsState(
        targetValue = if (checked) 0.3f else 0f,
        animationSpec = tween(150),
        label = "checkbox_glow_alpha"
    )

    Box(
        modifier = Modifier.size(36.dp),
        contentAlignment = Alignment.Center
    ) {
        // Glow ring
        if (checked) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .graphicsLayer {
                        scaleX = glowScale
                        scaleY = glowScale
                        alpha = glowAlpha
                    }
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                activeColor.copy(alpha = 0.5f),
                                activeColor.copy(alpha = 0.15f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )
        }

        // Main checkbox
        Box(
            modifier = Modifier
                .size(28.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .clip(CircleShape)
                .background(backgroundColor)
                .border(
                    width = 2.dp,
                    brush = Brush.linearGradient(
                        colors = if (checked)
                            listOf(activeColor, CompletedGreenLight)
                        else
                            listOf(borderColor, borderColor)
                    ),
                    shape = CircleShape
                )
                .clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onCheckedChange()
                },
            contentAlignment = Alignment.Center
        ) {
            // Animated checkmark
            AnimatedVisibility(
                visible = checked,
                enter = scaleIn(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessHigh
                    )
                ) + fadeIn(tween(50)),
                exit = scaleOut(tween(50)) + fadeOut(tween(50))
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
