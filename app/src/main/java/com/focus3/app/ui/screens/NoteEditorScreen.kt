package com.focus3.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items as lazyItems
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Label
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focus3.app.data.model.Note
import com.focus3.app.ui.theme.*
import com.focus3.app.ui.components.GlassBox

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    note: Note?,
    onSave: (Note) -> Unit,
    onBack: () -> Unit
) {
    val noteKey = note?.id ?: -1
    val fallbackColor = Color(0xFF1E3A5F)
    val parsedColorOptions = remember {
        Note.colorOptions.map { colorOption ->
            val (colorHex, _) = colorOption
            colorHex to try {
                Color(android.graphics.Color.parseColor(colorHex))
            } catch (e: Exception) {
                fallbackColor
            }
        }
    }
    val colorLookup = remember(parsedColorOptions) { parsedColorOptions.toMap() }
    val verticalScrollState = rememberScrollState()

    var title by rememberSaveable(noteKey) { mutableStateOf(note?.title ?: "") }
    var content by rememberSaveable(noteKey) { mutableStateOf(note?.content ?: "") }
    var selectedEmoji by rememberSaveable(noteKey) { mutableStateOf(note?.emoji ?: Note.emojiOptions.first()) }
    var selectedColor by rememberSaveable(noteKey) { mutableStateOf(note?.color ?: Note.colorOptions[0].first) }
    var selectedCategory by rememberSaveable(noteKey) { mutableStateOf(note?.category ?: "General") }
    var isPinned by rememberSaveable(noteKey) { mutableStateOf(note?.isPinned ?: false) }
    
    // Google Notes features
    var checklistItems by remember(noteKey) { mutableStateOf(note?.getChecklistItemsList() ?: emptyList()) }
    var selectedLabels by remember(noteKey) { mutableStateOf(note?.getLabelsList()?.toSet() ?: emptySet()) }
    var showChecklist by rememberSaveable(noteKey) { mutableStateOf(note?.checklistItems?.isNotBlank() == true) }
    var newChecklistItem by rememberSaveable(noteKey) { mutableStateOf("") }
    
    var showEmojiPicker by remember { mutableStateOf(false) }
    var showColorPicker by remember { mutableStateOf(false) }
    var showCategoryPicker by remember { mutableStateOf(false) }
    var showLabelPicker by remember { mutableStateOf(false) }
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    
    val isNewNote = note == null
    val noteColor = remember(selectedColor, colorLookup) {
        colorLookup[selectedColor] ?: fallbackColor
    }
    val wordSplitRegex = remember { Regex("\\s+") }
    val updatedAtFormatter = remember { java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME }
    val sortedSelectedLabels = remember(selectedLabels) { selectedLabels.toList().sorted() }
    val completedChecklistCount = remember(checklistItems) { checklistItems.count { it.isChecked } }
    val initialChecklistItems = remember(noteKey) { note?.getChecklistItemsList() ?: emptyList() }
    val initialLabelsSorted = remember(noteKey) {
        note?.getLabelsList().orEmpty().map { it.trim() }.sorted()
    }
    
    // Calculate word count
    val wordCount = remember(content, title, checklistItems) {
        val titleWords = title.split(wordSplitRegex).count { it.isNotBlank() }
        val contentWords = content.split(wordSplitRegex).count { it.isNotBlank() }
        val checklistWords = checklistItems.sumOf { item ->
            item.text.split(wordSplitRegex).count { it.isNotBlank() }
        }
        titleWords + contentWords + checklistWords
    }
    val hasMeaningfulContent = remember(title, content, checklistItems, selectedLabels) {
        title.isNotBlank() ||
            content.isNotBlank() ||
            checklistItems.any { it.text.isNotBlank() } ||
            selectedLabels.isNotEmpty()
    }
    val hasChanges = remember(
        isNewNote,
        noteKey,
        title,
        content,
        selectedEmoji,
        selectedColor,
        selectedCategory,
        isPinned,
        checklistItems,
        sortedSelectedLabels,
        initialChecklistItems,
        initialLabelsSorted
    ) {
        if (isNewNote) {
            hasMeaningfulContent
        } else {
            val existingNote = note ?: return@remember false
            title.trim() != existingNote.title.trim() ||
                content != existingNote.content ||
                selectedEmoji != existingNote.emoji ||
                selectedColor != existingNote.color ||
                selectedCategory != existingNote.category ||
                isPinned != existingNote.isPinned ||
                checklistItems != initialChecklistItems ||
                sortedSelectedLabels != initialLabelsSorted
        }
    }
    val canSave = if (isNewNote) hasMeaningfulContent else hasChanges
    
    Scaffold(
        topBar = {
            val infiniteTransition = rememberInfiniteTransition(label = "note_header_glow")
            val headerPulse by infiniteTransition.animateFloat(
                initialValue = 0.6f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1500, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "pulse"
            )
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                noteColor.copy(alpha = 0.9f),
                                noteColor.copy(alpha = 0.7f)
                            )
                        )
                    )
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Animated note icon with glow
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            Color.White.copy(alpha = headerPulse * 0.3f),
                                            Color.Transparent
                                        )
                                    ),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = selectedEmoji, fontSize = 26.sp)
                        }
                        
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                if (isNewNote) "NEW MISSION LOG" else "EDIT MISSION LOG",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.5.sp
                                ),
                                color = Color.White.copy(alpha = headerPulse * 0.8f)
                            )
                            Text(
                                if (isNewNote) "CREATION" else "ARCHIVE SYNC",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = (-0.5).sp
                                ),
                                color = Color.White
                            )
                        }
                    }
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                isPinned = !isPinned
                            }
                        ) {
                            Icon(
                                Icons.Default.PushPin,
                                contentDescription = if (isPinned) "Unpin note" else "Pin note",
                                tint = if (isPinned) PrimaryTeal else Color.White.copy(alpha = 0.8f)
                            )
                        }
                        IconButton(
                            enabled = canSave,
                            onClick = {
                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                val checklistString = if (checklistItems.isNotEmpty()) {
                                    checklistItems.joinToString("|||") { "${it.text}:::${it.isChecked}" }
                                } else ""
                                val labelsString = sortedSelectedLabels.joinToString(",")
                                
                                val newNote = if (isNewNote) {
                                    Note(
                                        title = title.trim(),
                                        content = content,
                                        emoji = selectedEmoji,
                                        color = selectedColor,
                                        category = selectedCategory,
                                        isPinned = isPinned,
                                        checklistItems = checklistString,
                                        labels = labelsString
                                    )
                                } else {
                                    note!!.copy(
                                        title = title.trim(),
                                        content = content,
                                        emoji = selectedEmoji,
                                        color = selectedColor,
                                        category = selectedCategory,
                                        isPinned = isPinned,
                                        checklistItems = checklistString,
                                        labels = labelsString,
                                        updatedAt = java.time.LocalDateTime.now()
                                            .format(updatedAtFormatter)
                                    )
                                }
                                onSave(newNote)
                            }
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "Save",
                                tint = if (canSave) Color.White else Color.White.copy(alpha = 0.35f),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }
        },
        containerColor = DarkBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(verticalScrollState)
                .padding(bottom = 104.dp)
        ) {
            // Options Row: Emoji, Color, Category
            GlassBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                cornerRadius = 24.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Emoji Picker Button
                    OptionButton(
                        icon = selectedEmoji,
                        label = "SYMBOL",
                        onClick = { 
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                            showEmojiPicker = true 
                        }
                    )
                    
                    // Color Picker Button
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { 
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                            showColorPicker = true 
                        }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(noteColor.copy(alpha = 0.2f))
                                .border(1.dp, noteColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(noteColor)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "TINT",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                            color = Color.White.copy(alpha = 0.3f)
                        )
                    }
                    
                    // Category Picker Button
                    OptionButton(
                        icon = "📁",
                        label = selectedCategory.uppercase(),
                        onClick = { 
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                            showCategoryPicker = true 
                        }
                    )
                }
            }
            
            // Title Input
            BasicTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                textStyle = MaterialTheme.typography.headlineMedium.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-0.5).sp
                ),
                cursorBrush = SolidColor(PrimaryTeal),
                decorationBox = { innerTextField ->
                    Box {
                        if (title.isEmpty()) {
                            Text(
                                "Mission title...",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = (-0.5).sp
                                ),
                                color = Color.White.copy(alpha = 0.1f)
                            )
                        }
                        innerTextField()
                    }
                }
            )
            
            // Teal divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(1.dp)
                    .background(PrimaryTeal.copy(alpha = 0.3f))
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Content Input
            BasicTextField(
                value = content,
                onValueChange = { content = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 300.dp)
                    .padding(horizontal = 16.dp),
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = Color.White,
                    lineHeight = 28.sp
                ),
                cursorBrush = SolidColor(PrimaryTeal),
                decorationBox = { innerTextField ->
                    Box {
                        if (content.isEmpty()) {
                            Text(
                                "Begin mission log...",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White.copy(alpha = 0.3f)
                            )
                        }
                        innerTextField()
                    }
                }
            )
            
            // Checklist Section (Google Notes style)
            if (showChecklist) {
                Spacer(modifier = Modifier.height(20.dp))
                GlassBox(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    cornerRadius = 16.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "✅ CHECKLIST",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                                color = PrimaryTeal
                            )
                            Text(
                                "$completedChecklistCount/${checklistItems.size} complete",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.45f)
                            )
                            IconButton(onClick = { showChecklist = false }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "Hide", tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(16.dp))
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Existing items
                        checklistItems.forEachIndexed { index, item ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = item.isChecked,
                                    onCheckedChange = { checked ->
                                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                        checklistItems = checklistItems.toMutableList().apply {
                                            this[index] = item.copy(isChecked = checked)
                                        }
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = PrimaryTeal,
                                        uncheckedColor = Color.White.copy(alpha = 0.3f)
                                    )
                                )
                                Text(
                                    item.text,
                                    color = if (item.isChecked) Color.White.copy(alpha = 0.4f) else Color.White,
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        textDecoration = if (item.isChecked) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                                    )
                                )
                                IconButton(onClick = {
                                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                    checklistItems = checklistItems.toMutableList().apply { removeAt(index) }
                                }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.5f), modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                        
                        // Add new item
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Add checklist item",
                                tint = PrimaryTeal,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            BasicTextField(
                                value = newChecklistItem,
                                onValueChange = { newChecklistItem = it.replace("\n", "").take(120) },
                                modifier = Modifier.weight(1f),
                                textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                                cursorBrush = SolidColor(PrimaryTeal),
                                decorationBox = { inner ->
                                    if (newChecklistItem.isEmpty()) {
                                        Text("Add item...", color = Color.White.copy(alpha = 0.3f))
                                    }
                                    inner()
                                }
                            )
                            if (newChecklistItem.isNotBlank()) {
                                IconButton(onClick = {
                                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                    val newItemText = newChecklistItem.trim()
                                    if (newItemText.isNotEmpty()) {
                                        checklistItems = checklistItems + com.focus3.app.data.model.ChecklistItem(newItemText, false)
                                    }
                                    newChecklistItem = ""
                                }) {
                                    Icon(Icons.Default.Check, contentDescription = "Add", tint = PrimaryTeal)
                                }
                            }
                        }
                    }
                }
            }
            
            // Labels display
            if (sortedSelectedLabels.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    lazyItems(
                        items = sortedSelectedLabels,
                        key = { it },
                        contentType = { "selected_label_chip" }
                    ) { label ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = PrimaryTeal.copy(alpha = 0.2f)
                        ) {
                            Text(
                                "🏷️ $label",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = PrimaryTeal,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "$wordCount words",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.45f)
                )
                if (checklistItems.isNotEmpty()) {
                    Text(
                        "${checklistItems.size} checklist items",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.45f)
                    )
                }
                if (isPinned) {
                    Text(
                        "Pinned",
                        style = MaterialTheme.typography.labelSmall,
                        color = PrimaryTeal.copy(alpha = 0.8f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
    // Bottom Toolbar (Google Notes style) - Using floating Box
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(noteColor.copy(alpha = 0.95f))
                .navigationBarsPadding()
                .padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row {
                    IconButton(onClick = { 
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        showChecklist = !showChecklist 
                    }) {
                        Icon(Icons.Default.CheckBox, contentDescription = "Checklist", tint = if (showChecklist) PrimaryTeal else Color.White.copy(alpha = 0.7f))
                    }
                    IconButton(onClick = { 
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        isPinned = !isPinned 
                    }) {
                        Icon(
                            Icons.Default.PushPin,
                            contentDescription = "Pin",
                            tint = if (isPinned) PrimaryTeal else Color.White.copy(alpha = 0.7f)
                        )
                    }
                    IconButton(onClick = { 
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        showLabelPicker = true 
                    }) {
                        Icon(Icons.AutoMirrored.Filled.Label, contentDescription = "Labels", tint = if (selectedLabels.isNotEmpty()) PrimaryTeal else Color.White.copy(alpha = 0.7f))
                    }
                }
                // Word count + timestamp
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "$wordCount words",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                    if (!isNewNote && note != null) {
                        val modifiedDisplay = remember(note.updatedAt) {
                            try {
                                val dateTime = java.time.LocalDateTime.parse(note.updatedAt)
                                val now = java.time.LocalDateTime.now()
                                val hours = java.time.temporal.ChronoUnit.HOURS.between(dateTime, now)
                                when {
                                    hours < 1 -> "Modified just now"
                                    hours < 24 -> "Modified ${hours}h ago"
                                    else -> {
                                        val days = java.time.temporal.ChronoUnit.DAYS.between(dateTime.toLocalDate(), now.toLocalDate())
                                        "Modified ${days}d ago"
                                    }
                                }
                            } catch (e: Exception) { "" }
                        }
                        if (modifiedDisplay.isNotEmpty()) {
                            Text(
                                modifiedDisplay,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.4f)
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Emoji Picker Dialog
    if (showEmojiPicker) {
        AlertDialog(
            onDismissRequest = { showEmojiPicker = false },
            containerColor = Color(0xFF1A1A2E),
            title = {
                Text("Choose Icon", color = Color.White, fontWeight = FontWeight.Bold)
            },
            text = {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(6),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.heightIn(max = 260.dp)
                ) {
                    items(
                        items = Note.emojiOptions,
                        key = { it },
                        contentType = { "emoji_option" }
                    ) { emoji ->
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(
                                    if (emoji == selectedEmoji) PrimaryTeal.copy(alpha = 0.3f)
                                    else Color.White.copy(alpha = 0.1f)
                                )
                                .border(
                                    width = if (emoji == selectedEmoji) 2.dp else 0.dp,
                                    color = if (emoji == selectedEmoji) PrimaryTeal else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable {
                                    selectedEmoji = emoji
                                    showEmojiPicker = false
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(emoji, fontSize = 24.sp)
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }
    
    // Color Picker Dialog
    if (showColorPicker) {
        AlertDialog(
            onDismissRequest = { showColorPicker = false },
            containerColor = Color(0xFF1A1A2E),
            title = {
                Text("Choose Color", color = Color.White, fontWeight = FontWeight.Bold)
            },
            text = {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(5),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.heightIn(max = 220.dp)
                ) {
                    items(
                        items = parsedColorOptions,
                        key = { it.first },
                        contentType = { "color_option" }
                    ) { colorOption ->
                        val (colorHex, color) = colorOption
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (colorHex == selectedColor) 3.dp else 0.dp,
                                    color = if (colorHex == selectedColor) Color.White else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable {
                                    selectedColor = colorHex
                                    showColorPicker = false
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (colorHex == selectedColor) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = "Selected color",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }
    
    // Category Picker Dialog
    if (showCategoryPicker) {
        AlertDialog(
            onDismissRequest = { showCategoryPicker = false },
            containerColor = Color(0xFF1A1A2E),
            title = {
                Text("Choose Category", color = Color.White, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Note.categoryOptions.forEach { category ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    selectedCategory = category
                                    showCategoryPicker = false
                                },
                            shape = RoundedCornerShape(12.dp),
                            color = if (category == selectedCategory) 
                                PrimaryTeal.copy(alpha = 0.3f) 
                            else 
                                Color.White.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = category,
                                modifier = Modifier.padding(16.dp),
                                color = if (category == selectedCategory) PrimaryTeal else Color.White,
                                fontWeight = if (category == selectedCategory) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }
    
    // Label Picker Dialog (Google Notes style)
    if (showLabelPicker) {
        AlertDialog(
            onDismissRequest = { showLabelPicker = false },
            containerColor = Color(0xFF1A1A2E),
            title = {
                Text("Add Labels", color = Color.White, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Note.defaultLabels.forEach { label ->
                        val isSelected = selectedLabels.contains(label)
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    selectedLabels = if (isSelected) {
                                        selectedLabels - label
                                    } else {
                                        selectedLabels + label
                                    }
                                },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) 
                                PrimaryTeal.copy(alpha = 0.3f) 
                            else 
                                Color.White.copy(alpha = 0.1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = null,
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = PrimaryTeal,
                                        uncheckedColor = Color.White.copy(alpha = 0.3f)
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "🏷️ $label",
                                    color = if (isSelected) PrimaryTeal else Color.White,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLabelPicker = false }) {
                    Text("Done", color = PrimaryTeal, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
private fun OptionButton(
    icon: String,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Text(icon, fontSize = 24.sp)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.6f)
        )
    }
}



