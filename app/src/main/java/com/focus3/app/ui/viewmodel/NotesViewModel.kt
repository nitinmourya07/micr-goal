package com.focus3.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focus3.app.data.dao.NoteDao
import com.focus3.app.data.model.Note
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class NotesViewModel @Inject constructor(
    private val noteDao: NoteDao
) : ViewModel() {
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()
    
    private val _selectedNote = MutableStateFlow<Note?>(null)
    val selectedNote: StateFlow<Note?> = _selectedNote.asStateFlow()
    
    // Debounced search for faster UI - only triggers after 300ms of no typing
    private val debouncedSearchQuery = _searchQuery
        .debounce(300)
        .distinctUntilChanged()

    private val allNotes: StateFlow<List<Note>> = noteDao.getAllNotes().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Smart stats derived from shared source data to avoid duplicate DB observers
    val noteStats: StateFlow<NoteStats> = allNotes
        .map(::buildStats)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = NoteStats()
        )
    
    val notes: StateFlow<List<Note>> = combine(
        debouncedSearchQuery,
        _selectedCategory,
        allNotes
    ) { query, category, allNotes ->
        var result = allNotes
        
        // Filter by category
        if (category != null) {
            result = result.filter { it.category == category }
        }
        
        // Filter by search query (fast in-memory search)
        if (query.isNotBlank()) {
            val lowerQuery = query.lowercase()
            result = result.filter { note ->
                note.title.lowercase().contains(lowerQuery) ||
                note.content.lowercase().contains(lowerQuery) ||
                note.category.lowercase().contains(lowerQuery)
            }
        }
        
        result
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    private fun buildStats(notes: List<Note>): NoteStats {
        val totalWords = notes.sumOf { note ->
            val trimmed = note.content.trim()
            if (trimmed.isEmpty()) 0 else trimmed.split("\\s+".toRegex()).size
        }
        return NoteStats(
            totalNotes = notes.size,
            totalPinned = notes.count { it.isPinned },
            totalWords = totalWords,
            mostUsedCategory = notes.groupingBy { it.category }
                .eachCount()
                .maxByOrNull { it.value }?.key ?: "General"
        )
    }
    
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    fun selectCategory(category: String?) {
        _selectedCategory.value = category
    }
    
    fun selectNote(note: Note?) {
        _selectedNote.value = note
    }
    
    // Fast save with IO dispatcher + error handling
    fun saveNote(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (note.id == 0) {
                    noteDao.insertNote(note)
                } else {
                    noteDao.updateNote(note)
                }
            } catch (e: Exception) {
                android.util.Log.e("NotesViewModel", "Error saving note: ${e.message}")
            }
        }
    }
    
    // Quick duplicate note with error handling
    fun duplicateNote(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val duplicated = note.copy(
                    id = 0,
                    title = "${note.title} (Copy)",
                    createdAt = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                    updatedAt = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                    isPinned = false
                )
                noteDao.insertNote(duplicated)
            } catch (e: Exception) {
                android.util.Log.e("NotesViewModel", "Error duplicating note: ${e.message}")
            }
        }
    }
    
    fun deleteNote(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                noteDao.deleteNote(note)
            } catch (e: Exception) {
                android.util.Log.e("NotesViewModel", "Error deleting note: ${e.message}")
            }
        }
    }
    
    fun togglePin(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                noteDao.togglePin(
                    id = note.id,
                    updatedAt = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                )
            } catch (e: Exception) {
                android.util.Log.e("NotesViewModel", "Error toggling pin: ${e.message}")
            }
        }
    }
    
    // Quick unpin all notes with error handling
    fun unpinAllNotes() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                notes.value.filter { it.isPinned }.forEach { note ->
                    noteDao.togglePin(
                        id = note.id,
                        updatedAt = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("NotesViewModel", "Error unpinning notes: ${e.message}")
            }
        }
    }
}

// Smart stats data class
data class NoteStats(
    val totalNotes: Int = 0,
    val totalPinned: Int = 0,
    val totalWords: Int = 0,
    val mostUsedCategory: String = "General"
)
