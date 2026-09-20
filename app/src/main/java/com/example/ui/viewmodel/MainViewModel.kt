package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import android.util.Base64
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.GeminiService
import com.example.data.db.AppDatabase
import com.example.data.model.StudySessionEntity
import com.example.data.model.TaskEntity
import com.example.data.model.TimetableSlot
import com.example.data.repository.TaskRepository
import com.example.data.util.AppPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = AppPreferences(application)
    private val db = AppDatabase.getInstance(application)
    private val repository = TaskRepository(db.taskDao(), db.studySessionDao())
    private val gemini = GeminiService()

    val rawTasks = repository.allTasks.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val studySessions = repository.studySessions.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()
    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory = _selectedCategory.asStateFlow()
    private val _selectedPriority = MutableStateFlow("All")
    val selectedPriority = _selectedPriority.asStateFlow()
    private val _currentTab = MutableStateFlow(0)
    val currentTab = _currentTab.asStateFlow()
    private val _isSetupWizardVisible = MutableStateFlow(prefs.userName.isBlank())
    val isSetupWizardVisible = _isSetupWizardVisible.asStateFlow()
    private val _isAddTaskDialogOpen = MutableStateFlow(false)
    val isAddTaskDialogOpen = _isAddTaskDialogOpen.asStateFlow()
    private val _userName = MutableStateFlow(prefs.userName.ifBlank { "Aswin" })
    val userName = _userName.asStateFlow()
    private val _userAge = MutableStateFlow(prefs.userAge)
    val userAge = _userAge.asStateFlow()
    private val _profilePhotoBase64 = MutableStateFlow(prefs.profilePhotoBase64)
    val profilePhotoBase64 = _profilePhotoBase64.asStateFlow()
    private val _calendarSyncEnabled = MutableStateFlow(prefs.calendarSyncEnabled)
    val isCalendarSyncEnabled = _calendarSyncEnabled.asStateFlow()
    private val _isAiSuggestionsEnabled = MutableStateFlow(prefs.aiOptimizationEnabled)
    val isAiSuggestionsEnabled = _isAiSuggestionsEnabled.asStateFlow()
    private val _isNotificationsEnabled = MutableStateFlow(prefs.notificationsEnabled)
    val isNotificationsEnabled = _isNotificationsEnabled.asStateFlow()
    private val _studyTimeBetaEnabled = MutableStateFlow(prefs.studyTimeBetaEnabled)
    val studyTimeBetaEnabled = _studyTimeBetaEnabled.asStateFlow()
    private val _studyAppName = MutableStateFlow(prefs.studyAppName)
    val studyAppName = _studyAppName.asStateFlow()
    private val _hasGeminiKey = MutableStateFlow(prefs.geminiApiKey.isNotBlank())
    val hasGeminiKey = _hasGeminiKey.asStateFlow()
    private val _aiBusy = MutableStateFlow(false)
    val aiBusy = _aiBusy.asStateFlow()
    private val _aiMessage = MutableStateFlow("")
    val aiMessage = _aiMessage.asStateFlow()
    private val _aiInsights = MutableStateFlow(prefs.lastAiInsights)
    val aiInsights = _aiInsights.asStateFlow()

    private val _timetableSlots = MutableStateFlow(defaultTimetable())
    val timetableSlots = _timetableSlots.asStateFlow()

    val filteredTasks: StateFlow<List<TaskEntity>> = combine(rawTasks, _searchQuery, _selectedCategory, _selectedPriority) { tasks, query, cat, priority ->
        tasks.filter { task ->
            (query.isBlank() || task.title.contains(query, true) || task.description.contains(query, true)) &&
                (cat == "All" || task.category.equals(cat, true)) &&
                (priority == "All" || task.priority.equals(priority, true))
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val totalTasksCount = rawTasks.map { it.size }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val completedTasksCount = rawTasks.map { it.count(TaskEntity::isCompleted) }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val aiVerifiedTasksCount = rawTasks.map { it.count(TaskEntity::isAiVerified) }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun setSearchQuery(v: String) { _searchQuery.value = v }
    fun setSelectedCategory(v: String) { _selectedCategory.value = v }
    fun setSelectedPriority(v: String) { _selectedPriority.value = v }
    fun setCurrentTab(v: Int) { if (v !in listOf(2, 3) || _hasGeminiKey.value) _currentTab.value = v }
    fun openSetupWizard() { _isSetupWizardVisible.value = true }
    fun closeSetupWizard() { _isSetupWizardVisible.value = false }
    fun setAddTaskDialogOpen(v: Boolean) { _isAddTaskDialogOpen.value = v }
    fun setCalendarSyncEnabled(v: Boolean) { prefs.calendarSyncEnabled = v; _calendarSyncEnabled.value = v }
    fun setAiSuggestionsEnabled(v: Boolean) { prefs.aiOptimizationEnabled = v; _isAiSuggestionsEnabled.value = v }
    fun setNotificationsEnabled(v: Boolean) { prefs.notificationsEnabled = v; _isNotificationsEnabled.value = v }
    fun setStudyTimeBetaEnabled(v: Boolean) { prefs.studyTimeBetaEnabled = v; _studyTimeBetaEnabled.value = v }
    fun setStudyAppName(v: String) { prefs.studyAppName = v; _studyAppName.value = v }

    fun setAccount(name: String, age: Int, photoBase64: String) {
        if (name.isNotBlank()) { prefs.userName = name.trim(); _userName.value = name.trim() }
        prefs.userAge = age.coerceAtLeast(0); _userAge.value = age.coerceAtLeast(0)
        prefs.profilePhotoBase64 = photoBase64; _profilePhotoBase64.value = photoBase64
    }

    fun setGeminiKey(key: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            _aiBusy.value = true
            val result = gemini.testKey(key.trim())
            if (result.isSuccess) {
                prefs.geminiApiKey = key.trim(); _hasGeminiKey.value = true
                onResult(true, "Gemini API key verified and stored locally.")
            } else onResult(false, result.exceptionOrNull()?.message ?: "Could not verify this key.")
            _aiBusy.value = false
        }
    }
    fun clearGeminiKey() { prefs.clearGeminiKey(); _hasGeminiKey.value = false; if (_currentTab.value in listOf(2,3)) _currentTab.value = 0 }

    fun saveProfilePhoto(uri: Uri, onDone: (String) -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            val encoded = getApplication<Application>().contentResolver.openInputStream(uri)?.use { Base64.encodeToString(it.readBytes(), Base64.NO_WRAP) }.orEmpty()
            withContext(Dispatchers.Main) { if (encoded.isNotBlank()) { prefs.profilePhotoBase64 = encoded; _profilePhotoBase64.value = encoded; onDone(encoded) } }
        }
    }

    fun addTask(title: String, description: String, category: String, priority: String, scheduledAt: Long) {
        if (title.isBlank()) return
        viewModelScope.launch { repository.insertTask(TaskEntity(title.trim(), description.trim(), category, priority, scheduledAt = scheduledAt)) }
    }

    fun toggleTaskCompleted(task: TaskEntity) {
        if (task.priority.equals("High", true)) return
        viewModelScope.launch { repository.updateTask(task.copy(isCompleted = !task.isCompleted)) }
    }

    fun verifyTaskWithAi(task: TaskEntity, proofText: String, imageBytes: ByteArray?) {
        if (!_hasGeminiKey.value || imageBytes == null || imageBytes.isEmpty()) return
        viewModelScope.launch {
            _aiBusy.value = true; _aiMessage.value = ""
            try {
                val result = gemini.verifyTask(prefs.geminiApiKey, task.title, task.description, proofText, imageBytes)
                repository.updateTask(task.copy(isCompleted = result.verified, isAiVerified = result.verified, aiScore = result.score, aiFeedback = result.reason))
                _aiMessage.value = if (result.verified) "Task verified by Gemini." else "Gemini could not verify completion."
            } catch (e: Exception) { _aiMessage.value = e.message ?: "AI verification failed." }
            _aiBusy.value = false
        }
    }

    fun verifyStudyScreenshot(imageBytes: ByteArray?, note: String) {
        if (!_hasGeminiKey.value || !_studyTimeBetaEnabled.value || imageBytes == null || _studyAppName.value.isBlank()) return
        viewModelScope.launch {
            _aiBusy.value = true; _aiMessage.value = ""
            try {
                val result = gemini.extractStudyMinutes(prefs.geminiApiKey, _studyAppName.value, imageBytes, note)
                if (result.found && result.minutes > 0) {
                    repository.addStudySession(StudySessionEntity(appName = _studyAppName.value, minutes = result.minutes, sourceNote = result.reason))
                    _aiMessage.value = "Added ${result.minutes} minutes of study time."
                } else _aiMessage.value = "No matching app and readable study duration found."
            } catch (e: Exception) { _aiMessage.value = e.message ?: "Study-time analysis failed." }
            _aiBusy.value = false
        }
    }

    fun generateInsights() {
        if (!_hasGeminiKey.value) return
        viewModelScope.launch {
            _aiBusy.value = true
            try {
                val taskJson = JSONArray(rawTasks.value.map { JSONObject().put("name", it.title).put("completed", it.isCompleted).put("priority", it.priority).put("category", it.category) }).toString()
                val studyJson = JSONArray(studySessions.value.map { JSONObject().put("app", it.appName).put("minutes", it.minutes).put("date", it.dateMillis) }).toString()
                val result = gemini.createInsights(prefs.geminiApiKey, taskJson, studyJson)
                prefs.lastAiInsights = result; _aiInsights.value = result
            } catch (e: Exception) { _aiMessage.value = e.message ?: "Could not generate insights." }
            _aiBusy.value = false
        }
    }

    fun deleteTask(task: TaskEntity) { viewModelScope.launch { repository.deleteTask(task) } }

    fun optimizeTimetableWithAi() {
        if (!_hasGeminiKey.value) return
        viewModelScope.launch { _timetableSlots.value = _timetableSlots.value.mapIndexed { i, s -> s.copy(isAiOptimized = true, taskTitle = if (i % 2 == 0) "Focus Sprint: ${s.taskTitle ?: "Key Objective"}" else s.taskTitle) } }
    }

    suspend fun exportBackup(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val root = JSONObject()
                .put("format", "caitlin-daily-backup")
                .put("version", 1)
                .put("appDate", System.currentTimeMillis())
                .put("account", JSONObject().put("name", prefs.userName).put("age", prefs.userAge).put("photoBase64", prefs.profilePhotoBase64))
                .put("settings", JSONObject().put("calendarSync", prefs.calendarSyncEnabled).put("aiOptimization", prefs.aiOptimizationEnabled).put("notifications", prefs.notificationsEnabled).put("studyTimeBeta", prefs.studyTimeBetaEnabled).put("studyAppName", prefs.studyAppName).put("lastAiInsights", prefs.lastAiInsights))
                .put("tasks", JSONArray(rawTasks.value.map { taskToJson(it) }))
                .put("studySessions", JSONArray(studySessions.value.map { JSONObject().put("id", it.id).put("dateMillis", it.dateMillis).put("appName", it.appName).put("minutes", it.minutes).put("sourceNote", it.sourceNote).put("createdAt", it.createdAt) }))
                .put("timetable", JSONArray(_timetableSlots.value.map { JSONObject().put("timeLabel", it.timeLabel).put("taskTitle", it.taskTitle).put("category", it.category).put("priority", it.priority).put("isAiOptimized", it.isAiOptimized) }))
            getApplication<Application>().contentResolver.openOutputStream(uri)?.use { it.write(root.toString(2).toByteArray()) } ?: error("Could not open backup destination")
        }
    }

    suspend fun importBackup(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val text = getApplication<Application>().contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() } ?: error("Could not read backup")
            val root = JSONObject(text)
            require(root.optString("format") == "caitlin-daily-backup") { "Not a Caitlin Daily backup." }
            val account = root.getJSONObject("account"); val settings = root.getJSONObject("settings")
            prefs.userName = account.optString("name", ""); prefs.userAge = account.optInt("age", 0); prefs.profilePhotoBase64 = account.optString("photoBase64", "")
            prefs.calendarSyncEnabled = settings.optBoolean("calendarSync", true); prefs.aiOptimizationEnabled = settings.optBoolean("aiOptimization", true); prefs.notificationsEnabled = settings.optBoolean("notifications", true); prefs.studyTimeBetaEnabled = settings.optBoolean("studyTimeBeta", false); prefs.studyAppName = settings.optString("studyAppName", ""); prefs.lastAiInsights = settings.optString("lastAiInsights", "")
            _userName.value = prefs.userName.ifBlank { "Aswin" }; _userAge.value = prefs.userAge; _profilePhotoBase64.value = prefs.profilePhotoBase64; _calendarSyncEnabled.value = prefs.calendarSyncEnabled; _isAiSuggestionsEnabled.value = prefs.aiOptimizationEnabled; _isNotificationsEnabled.value = prefs.notificationsEnabled; _studyTimeBetaEnabled.value = prefs.studyTimeBetaEnabled; _studyAppName.value = prefs.studyAppName; _aiInsights.value = prefs.lastAiInsights
            repository.clearTasks(); repository.clearStudySessions()
            val tasks = root.optJSONArray("tasks") ?: JSONArray(); for (i in 0 until tasks.length()) repository.insertTask(jsonToTask(tasks.getJSONObject(i)))
            val studies = root.optJSONArray("studySessions") ?: JSONArray(); for (i in 0 until studies.length()) { val s = studies.getJSONObject(i); repository.addStudySession(StudySessionEntity(s.optLong("id", 0), s.optLong("dateMillis"), s.optString("appName"), s.optInt("minutes"), s.optString("sourceNote"), s.optLong("createdAt"))) }
            val timetable = root.optJSONArray("timetable") ?: JSONArray(); if (timetable.length() > 0) _timetableSlots.value = List(timetable.length()) { i -> val s = timetable.getJSONObject(i); TimetableSlot(s.optString("timeLabel"), s.optString("taskTitle").ifBlank { null }, s.optString("category").ifBlank { null }, s.optString("priority").ifBlank { null }, s.optBoolean("isAiOptimized")) }
        }
    }

    private fun taskToJson(t: TaskEntity) = JSONObject().put("id", t.id).put("title", t.title).put("description", t.description).put("category", t.category).put("priority", t.priority).put("time", t.time).put("isCompleted", t.isCompleted).put("isAiVerified", t.isAiVerified).put("aiScore", t.aiScore).put("aiFeedback", t.aiFeedback).put("createdAt", t.createdAt).put("scheduledAt", t.scheduledAt)
    private fun jsonToTask(j: JSONObject) = TaskEntity(j.optLong("id", 0), j.optString("title"), j.optString("description"), j.optString("category", "Work"), j.optString("priority", "Medium"), j.optString("time", "Today"), j.optBoolean("isCompleted"), j.optBoolean("isAiVerified"), j.optInt("aiScore"), j.optString("aiFeedback"), j.optLong("createdAt", System.currentTimeMillis()), j.optLong("scheduledAt", 0))

    private fun defaultTimetable() = listOf(
        TimetableSlot("08:00 AM - 09:00 AM", "Morning Deep Work", "Work", "High", true), TimetableSlot("09:15 AM - 10:30 AM", "Sprint Planning & Sync", "Work", "Medium"), TimetableSlot("11:00 AM - 12:00 PM", "Algorithm Study Session", "Study", "High", true), TimetableSlot("12:30 PM - 01:30 PM", "Mindful Lunch & Walk", "Health", "Low"), TimetableSlot("02:00 PM - 03:30 PM", "Architecture Design", "Work", "High", true), TimetableSlot("04:00 PM - 05:00 PM", "Code Review & Verification", "Work", "Medium"), TimetableSlot("06:00 PM - 07:00 PM", "Evening Run & Workout", "Health", "High", true), TimetableSlot("08:00 PM - 09:00 PM", "Personal Reading & Reflection", "Personal", "Low")
    )
}
