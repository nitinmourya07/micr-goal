package com.focus3.app.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focus3.app.data.model.Note
import com.focus3.app.ui.theme.*
import com.focus3.app.ui.components.GlassBox

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    notes: List<Note>,
    searchQuery: String,
    selectedCategory: String?,
    onSearchChange: (String) -> Unit,
    onCategorySelect: (String?) -> Unit,
    onNoteClick: (Note) -> Unit,
    onAddNote: () -> Unit,
    onTogglePin: (Note) -> Unit,
    onDeleteNote: (Note) -> Unit,
    onArchiveNote: (Note) -> Unit = {},
    onBack: () -> Unit
) {
    var showArchived by remember { mutableStateOf(false) }
    var selectedLabel by remember { mutableStateOf<String?>(null) }
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    
    // Use derivedStateOf to prevent recompositions when computed values haven't changed
    val filteredNotes by remember(notes, showArchived, selectedLabel) {
        derivedStateOf {
            notes.filter { note ->
                val matchesArchive = if (showArchived) note.isArchived else !note.isArchived
                val matchesLabel = selectedLabel == null || note.hasLabel(selectedLabel!!)
                matchesArchive && matchesLabel
            }
        }
    }
    
    val pinnedNotes by remember(filteredNotes) {
        derivedStateOf { filteredNotes.filter { it.isPinned } }
    }
    
    val unpinnedNotes by remember(filteredNotes) {
        derivedStateOf { filteredNotes.filter { !it.isPinned } }
    }
    
    // Memoize labels collection
    val allLabels = remember(notes) { notes.flatMap { it.getLabelsList() }.distinct().sorted() }
    val categoryCount = remember(notes) { notes.map { it.category }.distinct().size }
    val hasAnyFilters = remember(showArchived, selectedLabel, searchQuery, selectedCategory) {
        showArchived || selectedLabel != null || searchQuery.isNotBlank() || selectedCategory != null
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
        ) {
            // FAST Header animation
            val infiniteTransition = rememberInfiniteTransition(label = "notesHeader")
            val headerGlow by infiniteTransition.animateFloat(
                initialValue = 0.3f,
                targetValue = 0.6f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1800, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "headerGlow"
            )
            
            Column(
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
                    
                    // Animated Icon with glow
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        PrimaryTeal.copy(alpha = headerGlow),
                                        Color.Transparent
                                    )
                                ),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("📝", fontSize = 24.sp)
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column {
                        Text(
                            "THOUGHT VAULT",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp
                            ),
                            color = PrimaryTeal
                        )
                        Text(
                            "MEMORY BANK",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = (-0.5).sp
                            ),
                            color = Color.White
                        )
                    }
                    
                    Spacer(modifier = Modifier.weight(1f))
                }
                
                // Stats Row
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Total notes
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(PrimaryTeal.copy(alpha = 0.08f))
                            .border(1.dp, PrimaryTeal.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("📋", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "${notes.size}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                color = PrimaryTeal
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "NOTES",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.5f)
                            )
                        }
                    }
                    
                    // Pinned notes
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFFB74D).copy(alpha = 0.08f))
                            .border(1.dp, Color(0xFFFFB74D).copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("📌", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "${pinnedNotes.size}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                color = Color(0xFFFFB74D)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "PINNED",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
                    
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp)) {
                    // Search Bar (Premium Glassmorphic)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = "Search",
                                tint = Color.White.copy(alpha = 0.3f),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = onSearchChange,
                                modifier = Modifier.weight(1f),
                                textStyle = MaterialTheme.typography.bodyLarge.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                ),
                                cursorBrush = SolidColor(PrimaryTeal),
                                decorationBox = { innerTextField ->
                                    if (searchQuery.isEmpty()) {
                                        Text(
                                            "ACCESS NEURAL ARCHIVE...",
                                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Black),
                                            color = Color.White.copy(alpha = 0.1f)
                                        )
                                    }
                                    innerTextField()
                                }
                            )
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchChange("") }, modifier = Modifier.size(24.dp)) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Clear",
                                        tint = Color.White.copy(alpha = 0.3f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Category Filter Chips
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = selectedCategory == null,
                                onClick = { onCategorySelect(null) },
                                label = { Text("All") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PrimaryTeal,
                                    selectedLabelColor = Color.Black,
                                    containerColor = Color.White.copy(alpha = 0.1f),
                                    labelColor = Color.White
                                )
                            )
                        }
                        items(
                            items = Note.categoryOptions,
                            key = { it },
                            contentType = { "category_chip" }
                        ) { category ->
                            FilterChip(
                                selected = selectedCategory == category,
                                onClick = { onCategorySelect(category) },
                                label = { Text(category) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PrimaryTeal,
                                    selectedLabelColor = Color.Black,
                                    containerColor = Color.White.copy(alpha = 0.1f),
                                    labelColor = Color.White
                                )
                            )
                        }
                    }
                    
                    // Archive Toggle
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                if (showArchived) "📦 Archived" else "📝 Active",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        }
                        Switch(
                            checked = showArchived,
                            onCheckedChange = { showArchived = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = PrimaryTeal,
                                checkedTrackColor = PrimaryTeal.copy(alpha = 0.3f)
                            )
                        )
                    }
                    
                    // Label Filter (if labels exist)
                    if (allLabels.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            item {
                                FilterChip(
                                    selected = selectedLabel == null,
                                    onClick = { selectedLabel = null },
                                    label = { Text("🏷️ All Labels") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFFBD00FF),
                                        selectedLabelColor = Color.White,
                                        containerColor = Color.White.copy(alpha = 0.05f),
                                        labelColor = Color.White.copy(alpha = 0.7f)
                                    )
                                )
                            }
                            items(
                                items = allLabels,
                                key = { it },
                                contentType = { "label_chip" }
                            ) { label ->
                                FilterChip(
                                    selected = selectedLabel == label,
                                    onClick = { selectedLabel = if (selectedLabel == label) null else label },
                                    label = { Text(label) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFFBD00FF),
                                        selectedLabelColor = Color.White,
                                        containerColor = Color.White.copy(alpha = 0.05f),
                                        labelColor = Color.White.copy(alpha = 0.7f)
                                    )
                                )
                            }
                        }
                    }
                    
                    // Quick Stats Row
                    if (notes.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            QuickNoteStat(
                                emoji = "📋",
                                label = "Total",
                                value = notes.size.toString(),
                                modifier = Modifier.weight(1f)
                            )
                            QuickNoteStat(
                                emoji = "📌",
                                label = "Pinned",
                                value = pinnedNotes.size.toString(),
                                modifier = Modifier.weight(1f)
                            )
                            QuickNoteStat(
                                emoji = "📂",
                                label = "Categories",
                                value = categoryCount.toString(),
                                modifier = Modifier.weight(1f)
                            )
                    }
                }
            }
            
            // Notes Grid
            if (notes.isEmpty()) {
                // Premium Inspiring Empty State with animations
                val infiniteTransition = rememberInfiniteTransition(label = "empty")
                val pulseAlpha by infiniteTransition.animateFloat(
                    initialValue = 0.3f,
                    targetValue = 0.7f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2000, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "pulse"
                )
                val iconScale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1500, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "scale"
                )
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(40.dp))
                    
                    // Animated icon with glow
                    Box(
                        modifier = Modifier
                            .size(130.dp)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        PrimaryTeal.copy(alpha = pulseAlpha),
                                        NeonCyan.copy(alpha = pulseAlpha * 0.5f),
                                        Color.Transparent
                                    )
                                ),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "📝",
                            fontSize = 64.sp,
                            modifier = Modifier.graphicsLayer {
                                scaleX = iconScale
                                scaleY = iconScale
                            }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(28.dp))
                    
                    Text(
                        "MIND PALACE",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 3.sp
                        ),
                        color = Color.White
                    )
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Text(
                        "Capture your elite ideas and structure\nyour thoughts. Your digital second brain.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.5f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        lineHeight = 24.sp
                    )
                    
                    Spacer(modifier = Modifier.height(36.dp))
                    
                    // Feature Cards Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Quick Notes
                        GlassBox(modifier = Modifier.weight(1f), cornerRadius = 16.dp) {
                            Column(
                                Modifier.padding(vertical = 20.dp, horizontal = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(PrimaryTeal.copy(alpha = 0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("⚡", fontSize = 24.sp)
                                }
                                Spacer(Modifier.height(10.dp))
                                Text("QUICK", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black), color = Color.White)
                                Text("Notes", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.5f))
                            }
                        }
                        
                        // Organize
                        GlassBox(modifier = Modifier.weight(1f), cornerRadius = 16.dp) {
                            Column(
                                Modifier.padding(vertical = 20.dp, horizontal = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(Color(0xFFBD00FF).copy(alpha = 0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("🏷️", fontSize = 24.sp)
                                }
                                Spacer(Modifier.height(10.dp))
                                Text("LABELS", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black), color = Color.White)
                                Text("Organize", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.5f))
                            }
                        }
                        
                        // Checklists
                        GlassBox(modifier = Modifier.weight(1f), cornerRadius = 16.dp) {
                            Column(
                                Modifier.padding(vertical = 20.dp, horizontal = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(Color(0xFF00E676).copy(alpha = 0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("☑️", fontSize = 24.sp)
                                }
                                Spacer(Modifier.height(10.dp))
                                Text("LISTS", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black), color = Color.White)
                                Text("Tasks", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.5f))
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(36.dp))
                    
                    // Enhanced CTA Button
                    Button(
                        onClick = onAddNote,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(listOf(PrimaryTeal, NeonCyan)),
                                    RoundedCornerShape(16.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("✍️", fontSize = 20.sp)
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    "CREATE FIRST NOTE",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black),
                                    color = Color.Black
                                )
                            }
                        }
                    }
                }
            } else if (filteredNotes.isEmpty()) {
                val emptyFilterMessage = when {
                    showArchived -> "No archived notes found for current filters."
                    selectedLabel != null -> "No notes found with label \"$selectedLabel\"."
                    selectedCategory != null -> "No notes found in \"$selectedCategory\"."
                    searchQuery.isNotBlank() -> "No notes match \"$searchQuery\"."
                    else -> "No notes match the selected filters."
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 28.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(40.dp))
                    Text("🔎", fontSize = 58.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "NO MATCHES",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        emptyFilterMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.6f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    if (hasAnyFilters) {
                        Spacer(modifier = Modifier.height(20.dp))
                        OutlinedButton(
                            onClick = {
                                showArchived = false
                                selectedLabel = null
                                onCategorySelect(null)
                                onSearchChange("")
                            },
                            border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryTeal.copy(alpha = 0.5f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryTeal)
                        ) {
                            Text("CLEAR FILTERS", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(2),
                    contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 120.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalItemSpacing = 12.dp,
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Pinned Notes Section
                    if (pinnedNotes.isNotEmpty()) {
                        item(span = StaggeredGridItemSpan.FullLine) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("📌", fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Pinned",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = Color.White.copy(alpha = 0.7f),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = PrimaryTeal.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        "${pinnedNotes.size}",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = PrimaryTeal,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        items(
                            items = pinnedNotes,
                            key = { it.id },
                            contentType = { "pinned_note" }
                        ) { note ->
                            NoteCard(
                                note = note,
                                onClick = { onNoteClick(note) },
                                onTogglePin = { onTogglePin(note) },
                                onDelete = { onDeleteNote(note) },
                                onArchive = { onArchiveNote(note) },
                                onCopy = {
                                    clipboardManager.setText(AnnotatedString(buildNoteCopyText(note)))
                                    Toast.makeText(context, "Note copied", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                    
                    // Other Notes Section
                    if (unpinnedNotes.isNotEmpty() && pinnedNotes.isNotEmpty()) {
                        item(span = StaggeredGridItemSpan.FullLine) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp, bottom = 8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("📋", fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Notes",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = Color.White.copy(alpha = 0.7f),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color.White.copy(alpha = 0.1f)
                                ) {
                                    Text(
                                        "${unpinnedNotes.size}",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White.copy(alpha = 0.7f),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                    items(
                        items = unpinnedNotes,
                        key = { it.id },
                        contentType = { "note" }
                    ) { note ->
                        NoteCard(
                            note = note,
                            onClick = { onNoteClick(note) },
                            onTogglePin = { onTogglePin(note) },
                            onDelete = { onDeleteNote(note) },
                            onArchive = { onArchiveNote(note) },
                            onCopy = {
                                clipboardManager.setText(AnnotatedString(buildNoteCopyText(note)))
                                Toast.makeText(context, "Note copied", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }
        
        // Enhanced Floating Add Button with pulsing glow
        val fabTransition = rememberInfiniteTransition(label = "fab")
        val fabGlow by fabTransition.animateFloat(
            initialValue = 0.3f,
            targetValue = 0.7f,
            animationSpec = infiniteRepeatable(
                animation = tween(1500, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "fabGlow"
        )
        
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {
            // Glow layer
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .align(Alignment.Center)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                PrimaryTeal.copy(alpha = fabGlow),
                                Color.Transparent
                            )
                        ),
                        CircleShape
                    )
            )
            FloatingActionButton(
                onClick = onAddNote,
                modifier = Modifier.size(60.dp),
                containerColor = PrimaryTeal,
                contentColor = Color.Black,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 8.dp,
                    pressedElevation = 12.dp
                )
            ) {
                Text("✍️", fontSize = 24.sp)
            }
        }
    }
}

// Quick Note Stat for header
@Composable
fun QuickNoteStat(
    emoji: String,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.08f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(emoji, fontSize = 16.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    value,
                    style = MaterialTheme.typography.titleMedium,
                    color = PrimaryTeal,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// Feature chip for empty state
@Composable
fun NoteFeatureChip(text: String) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White.copy(alpha = 0.08f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            style = MaterialTheme.typography.labelMedium,
            color = Color.White.copy(alpha = 0.8f)
        )
    }
}

private fun buildNoteCopyText(note: Note): String {
    val checklist = note.getChecklistItemsList()
    return buildString {
        if (note.title.isNotBlank()) {
            appendLine(note.title)
            appendLine()
        }
        if (note.content.isNotBlank()) {
            appendLine(note.content)
            appendLine()
        }
        if (checklist.isNotEmpty()) {
            appendLine("Checklist:")
            checklist.forEach { item ->
                appendLine("${if (item.isChecked) "[x]" else "[ ]"} ${item.text}")
            }
            appendLine()
        }
        append("Category: ${note.category}")
    }.trim()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteCard(
    note: Note,
    onClick: () -> Unit,
    onTogglePin: () -> Unit,
    onDelete: () -> Unit,
    onArchive: () -> Unit = {},
    onCopy: () -> Unit = {}
) {
    var showMenu by rememberSaveable(note.id) { mutableStateOf(false) }
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    val noteColor = remember(note.color) {
        try {
            Color(android.graphics.Color.parseColor(note.color))
        } catch (e: Exception) {
            PrimaryTeal
        }
    }
    
    // Parse timestamp for display
    val timeDisplay = remember(note.updatedAt) {
        try {
            val dateTime = java.time.LocalDateTime.parse(note.updatedAt)
            val now = java.time.LocalDateTime.now()
            val days = java.time.temporal.ChronoUnit.DAYS.between(dateTime.toLocalDate(), now.toLocalDate())
            when {
                days == 0L -> "Today"
                days == 1L -> "Yesterday"
                days < 7 -> "${days}d ago"
                else -> dateTime.format(java.time.format.DateTimeFormatter.ofPattern("MMM d"))
            }
        } catch (e: Exception) { "" }
    }
    
    // Get checklist items for preview
    val checklistItems = remember(note.checklistItems) { note.getChecklistItemsList() }
    val hasChecklist = checklistItems.isNotEmpty()
    val labels = remember(note.labels) { note.getLabelsList() }
    
    GlassBox(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        cornerRadius = 24.dp,
        showGlow = note.isPinned
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header: Emoji + Pin
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(noteColor.copy(alpha = 0.1f), CircleShape)
                        .border(1.dp, noteColor.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(note.emoji, fontSize = 22.sp)
                }
                
                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "Menu",
                            tint = Color.White.copy(alpha = 0.3f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(DarkSurfaceVariant)
                    ) {
                        DropdownMenuItem(
                            text = { Text(if (note.isPinned) "Unpin" else "Pin to top") },
                            leadingIcon = { 
                                Text(if (note.isPinned) "📌" else "📍", fontSize = 16.sp)
                            },
                            onClick = {
                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                onTogglePin()
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Copy") },
                            leadingIcon = { Text("📋", fontSize = 16.sp) },
                            onClick = {
                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                onCopy()
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(if (note.isArchived) "Unarchive" else "Archive") },
                            leadingIcon = { Text("📦", fontSize = 16.sp) },
                            onClick = {
                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                onArchive()
                                showMenu = false
                            }
                        )
                        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                        DropdownMenuItem(
                            text = { Text("Delete", color = Color.Red) },
                            leadingIcon = { Text("🗑️", fontSize = 16.sp) },
                            onClick = {
                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                onDelete()
                                showMenu = false
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Title
            if (note.title.isNotBlank()) {
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // Content Preview or Checklist Preview
            if (hasChecklist) {
                // Show checklist preview (first 3 items)
                checklistItems.take(3).forEach { item ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Text(
                            if (item.isChecked) "☑️" else "☐",
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            item.text,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (item.isChecked) Color.White.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.6f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textDecoration = if (item.isChecked) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                        )
                    }
                }
                if (checklistItems.size > 3) {
                    Text(
                        "+${checklistItems.size - 3} more",
                        style = MaterialTheme.typography.labelSmall,
                        color = PrimaryTeal.copy(alpha = 0.7f)
                    )
                }
            } else if (note.content.isNotBlank()) {
                Text(
                    text = note.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.5f),
                    maxLines = 6,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 22.sp
                )
            }
            
            // Labels preview
            if (labels.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    labels.take(2).forEach { label ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(PrimaryTeal.copy(alpha = 0.1f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                label,
                                style = MaterialTheme.typography.labelSmall,
                                color = PrimaryTeal,
                                fontSize = 10.sp
                            )
                        }
                    }
                    if (labels.size > 2) {
                        Text("+${labels.size - 2}", fontSize = 10.sp, color = Color.White.copy(alpha = 0.3f))
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Footer: Category + Timestamp + Pin indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(noteColor.copy(alpha = 0.1f))
                            .border(1.dp, noteColor.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = note.category.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                            color = noteColor,
                            fontSize = 10.sp
                        )
                    }
                    if (timeDisplay.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            timeDisplay,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.3f),
                            fontSize = 10.sp
                        )
                    }
                }
                if (note.isPinned) {
                    Text("📌", fontSize = 14.sp)
                }
            }
        }
    }
}
