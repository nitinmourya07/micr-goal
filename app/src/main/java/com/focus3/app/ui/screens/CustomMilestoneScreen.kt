package com.focus3.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focus3.app.ui.theme.DarkBackground
import com.focus3.app.ui.theme.NeonCyan
import com.focus3.app.ui.theme.PrimaryTeal

data class CustomMilestone(
    val id: Int = 0,
    val days: Int,
    val title: String,
    val icon: String
)

val milestoneIcons = listOf(
    "🎯", "🏆", "👑", "🔥", "⭐", "💎",
    "🚀", "💪", "🌟", "✨", "🎉", "🏅"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomMilestoneScreen(
    existingMilestones: List<CustomMilestone>,
    onSave: (List<CustomMilestone>) -> Unit,
    onBack: () -> Unit
) {
    var milestones by remember { mutableStateOf(existingMilestones) }
    var showAddDialog by remember { mutableStateOf(false) }
    val sortedMilestones = remember(milestones) { milestones.sortedBy { it.days } }
    val hasChanges = remember(milestones, existingMilestones) { milestones != existingMilestones }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Column(modifier = Modifier.padding(start = 8.dp)) {
                        Text(
                            "JOURNEY PROTOCOL",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.5.sp
                            ),
                            color = PrimaryTeal
                        )
                        Text(
                            "CUSTOM MILESTONES",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = (-0.5).sp
                            ),
                            color = Color.White
                        )
                    }
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    TextButton(
                        enabled = hasChanges,
                        onClick = {
                            onSave(milestones)
                            onBack()
                        }
                    ) {
                        Text(
                            "COMMIT",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black),
                            color = if (hasChanges) PrimaryTeal else Color.White.copy(alpha = 0.3f)
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = PrimaryTeal,
                contentColor = Color.Black
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Milestone")
            }
        },
        containerColor = DarkBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Create your own milestones based on your goals. They'll appear in your Streak Journey!",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.6f)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            if (milestones.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(PrimaryTeal.copy(alpha = 0.05f))
                                .border(1.dp, PrimaryTeal.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🎯", fontSize = 48.sp)
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            "NO CUSTOM MILESTONES",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                            color = Color.White
                        )
                        Text(
                            "Tap the action button to establish your targets",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.3f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    items(
                        items = sortedMilestones,
                        key = { it.id },
                        contentType = { "milestone_item" }
                    ) { milestone ->
                        MilestoneItem(
                            milestone = milestone,
                            onDelete = {
                                milestones = milestones.filter { it.id != milestone.id }
                            }
                        )
                    }
                }
            }
        }
        
        if (showAddDialog) {
            AddMilestoneDialog(
                onAdd = { days, title, icon ->
                    val newId = (milestones.maxOfOrNull { it.id } ?: 0) + 1
                    milestones = milestones + CustomMilestone(newId, days, title, icon)
                    showAddDialog = false
                },
                onDismiss = { showAddDialog = false }
            )
        }
    }
}

@Composable
fun MilestoneItem(
    milestone: CustomMilestone,
    onDelete: () -> Unit
) {
    com.focus3.app.ui.components.GlassBox(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 20.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = milestone.icon, fontSize = 28.sp)
            }
            
            Spacer(modifier = Modifier.width(20.dp))
            
            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = milestone.title.uppercase(),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-0.5).sp
                    ),
                    color = Color.White
                )
                Text(
                    text = "REACHED AT ${milestone.days} DAYS",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                    color = PrimaryTeal
                )
            }
            
            // Delete button
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color.White.copy(alpha = 0.2f)
                )
            }
        }
    }
}

@Composable
fun AddMilestoneDialog(
    onAdd: (Int, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var days by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    val iconRows = remember { milestoneIcons.chunked(6) }
    var selectedIcon by remember { mutableStateOf("🎯") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF151525),
        title = {
            Column {
                Text(
                    "INITIALIZE TARGET",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 1.sp),
                    color = PrimaryTeal
                )
                Text(
                    "CREATE MILESTONE",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                    color = Color.White
                )
            }
        },
        text = {
            Column {
                Spacer(modifier = Modifier.height(16.dp))
                // Days input
                Text(
                    "REQUIRED DURATION (DAYS)",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                    color = Color.White.copy(alpha = 0.3f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                BasicTextField(
                    value = days,
                    onValueChange = { days = it.filter { c -> c.isDigit() } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.White, fontWeight = FontWeight.Bold),
                    cursorBrush = SolidColor(PrimaryTeal),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    decorationBox = { innerTextField ->
                        if (days.isEmpty()) {
                            Text("e.g. 45", color = Color.White.copy(alpha = 0.1f))
                        }
                        innerTextField()
                    }
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Title input
                Text(
                    "IDENTIFIER",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                    color = Color.White.copy(alpha = 0.3f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                BasicTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.White, fontWeight = FontWeight.Bold),
                    cursorBrush = SolidColor(PrimaryTeal),
                    decorationBox = { innerTextField ->
                        if (title.isEmpty()) {
                            Text("e.g. HALF CENTURY", color = Color.White.copy(alpha = 0.1f))
                        }
                        innerTextField()
                    }
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Icon selector
                Text(
                    "ASSIGN SYMBOL",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                    color = Color.White.copy(alpha = 0.3f)
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    iconRows.forEach { row ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            row.forEach { icon ->
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            if (icon == selectedIcon) PrimaryTeal.copy(alpha = 0.1f)
                                            else Color.White.copy(alpha = 0.05f)
                                        )
                                        .border(
                                            width = if (icon == selectedIcon) 2.dp else 0.dp,
                                            color = if (icon == selectedIcon) PrimaryTeal else Color.Transparent,
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .clickable { selectedIcon = icon },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = icon, fontSize = 22.sp)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val daysInt = days.toIntOrNull() ?: 0
                    if (daysInt > 0 && title.isNotBlank()) {
                        onAdd(daysInt, title, selectedIcon)
                    }
                },
                enabled = days.isNotBlank() && title.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryTeal,
                    contentColor = Color.Black,
                    disabledContainerColor = Color.White.copy(alpha = 0.05f),
                    disabledContentColor = Color.White.copy(alpha = 0.2f)
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Text("AUTHORIZE CREATION", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("CANCEL", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black), color = Color.White.copy(alpha = 0.3f))
            }
        }
    )
}
