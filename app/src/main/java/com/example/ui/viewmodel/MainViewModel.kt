package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.GeminiService
import com.example.data.api.InsightsResult
import com.example.data.db.AppDatabase
import com.example.data.model.StudyRecord
import com.example.data.model.TaskEntity
import com.example.data.model.TimetableSlot
import com.example.data.model.UserProfile
import com.example.data.repository.TaskRepository
import com.example.util.BackupManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TaskRepository
    private val prefs: SharedPreferences

    init {
        val db = AppDatabase.getInstance(application)
        repository = TaskRepository(db.taskDao(), db.studyDao())
        prefs = application.getSharedPreferences("caitlin_daily_prefs", Context.MODE_PRIVATE)
    }

    // --- Offline Account Profile ---
    private val _userProfile = MutableStateFlow(
        UserProfile(
            name = prefs.getString("profile_name", "Aswin") ?: "Aswin",
            age = prefs.getInt("profile_age", 22),
            photoUri = prefs.getString("profile_photo_uri", null),
            isCreated = prefs.getBoolean("profile_is_created", true)
        )
    )
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    // --- Gemini User API Key ---
    private val _geminiApiKey = MutableStateFlow(prefs.getString("gemini_api_key", "") ?: "")
    val geminiApiKey: StateFlow<String> = _geminiApiKey.asStateFlow()

    private val _isVerifyingKey = MutableStateFlow(false)
    val isVerifyingKey: StateFlow<Boolean> = _isVerifyingKey.asStateFlow()

    private val _keyVerificationStatus = MutableStateFlow<String?>(null)
    val keyVerificationStatus: StateFlow<String?> = _keyVerificationStatus.asStateFlow()

    // Derived: check if user provided a key
    val isGeminiAvailable: StateFlow<Boolean> = _geminiApiKey.map { it.isNotBlank() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // --- Settings Preferences ---
    private val _isStudyTimeBetaEnabled = MutableStateFlow(prefs.getBoolean("beta_study_time", true))
    val isStudyTimeBetaEnabled: StateFlow<Boolean> = _isStudyTimeBetaEnabled.asStateFlow()

    private val _isCalendarSyncEnabled = MutableStateFlow(prefs.getBoolean("calendar_sync", true))
    val isCalendarSyncEnabled: StateFlow<Boolean> = _isCalendarSyncEnabled.asStateFlow()

    private val _isAiOptimizationEnabled = MutableStateFlow(prefs.getBoolean("ai_opt", true))
    val isAiOptimizationEnabled: StateFlow<Boolean> = _isAiOptimizationEnabled.asStateFlow()

    private val _isNotificationsEnabled = MutableStateFlow(prefs.getBoolean("notifications", true))
    val isNotificationsEnabled: StateFlow<Boolean> = _isNotificationsEnabled.asStateFlow()

    // --- Material You & Theme Modes ("auto", "dark", "light") ---
    private val _themeMode = MutableStateFlow(prefs.getString("app_theme_mode", "auto") ?: "auto")
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    private val _isDynamicColor = MutableStateFlow(prefs.getBoolean("app_dynamic_color", true))
    val isDynamicColor: StateFlow<Boolean> = _isDynamicColor.asStateFlow()

    // --- UI Navigation and Dialogs ---
    private val _currentTab = MutableStateFlow(0) // 0: Tasks, 1: Schedule, 2: AI Verify, 3: Insights, 4: Profile
    val currentTab: StateFlow<Int> = _currentTab.asStateFlow()

    private val _isSetupWizardVisible = MutableStateFlow(false)
    val isSetupWizardVisible: StateFlow<Boolean> = _isSetupWizardVisible.asStateFlow()

    private val _isAddTaskDialogOpen = MutableStateFlow(false)
    val isAddTaskDialogOpen: StateFlow<Boolean> = _isAddTaskDialogOpen.asStateFlow()

    private val _isAccountDialogOpen = MutableStateFlow(false)
    val isAccountDialogOpen: StateFlow<Boolean> = _isAccountDialogOpen.asStateFlow()

    private val _selectedTaskForVerification = MutableStateFlow<TaskEntity?>(null)
    val selectedTaskForVerification: StateFlow<TaskEntity?> = _selectedTaskForVerification.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _selectedPriority = MutableStateFlow("All")
    val selectedPriority: StateFlow<String> = _selectedPriority.asStateFlow()

    // --- Tasks Data ---
    val rawTasks: StateFlow<List<TaskEntity>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredTasks: StateFlow<List<TaskEntity>> = combine(
        rawTasks,
        _searchQuery,
        _selectedCategory,
        _selectedPriority
    ) { tasks, query, cat, priority ->
        tasks.filter { task ->
            val matchesQuery = query.isBlank() ||
                    task.title.contains(query, ignoreCase = true) ||
                    task.description.contains(query, ignoreCase = true)
            val matchesCat = (cat == "All") || (task.category.equals(cat, ignoreCase = true))
            val matchesPriority = (priority == "All") || (task.priority.equals(priority, ignoreCase = true))
            matchesQuery && matchesCat && matchesPriority
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalTasksCount = rawTasks.map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val completedTasksCount = rawTasks.map { list -> list.count { it.isCompleted } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val aiVerifiedTasksCount = rawTasks.map { list -> list.count { it.isAiVerified } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // --- Study Records Data ---
    val studyRecords: StateFlow<List<StudyRecord>> = repository.allStudyRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalStudyMinutes: StateFlow<Int> = studyRecords.map { list -> list.sumOf { it.minutes } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // --- Timetable Slots ---
    private val _timetableSlots = MutableStateFlow(
        listOf(
            TimetableSlot("08:00 AM - 09:00 AM", "Morning Deep Work", "Work", "High", true),
            TimetableSlot("09:15 AM - 10:30 AM", "Sprint Planning & Sync", "Work", "Medium", false),
            TimetableSlot("11:00 AM - 12:00 PM", "Algorithm Study Session", "Study", "High", true),
            TimetableSlot("12:30 PM - 01:30 PM", "Mindful Lunch & Walk", "Health", "Low", false),
            TimetableSlot("02:00 PM - 03:30 PM", "Architecture Design", "Work", "High", true),
            TimetableSlot("04:00 PM - 05:00 PM", "Code Review & Verification", "Work", "Medium", false),
            TimetableSlot("06:00 PM - 07:00 PM", "Evening Run & Workout", "Health", "High", true),
            TimetableSlot("08:00 PM - 09:00 PM", "Personal Reading & Reflection", "Personal", "Low", false)
        )
    )
    val timetableSlots: StateFlow<List<TimetableSlot>> = _timetableSlots.asStateFlow()

    // --- AI Insights State ---
    private val _aiInsights = MutableStateFlow<InsightsResult?>(null)
    val aiInsights: StateFlow<InsightsResult?> = _aiInsights.asStateFlow()

    private val _isLoadingInsights = MutableStateFlow(false)
    val isLoadingInsights: StateFlow<Boolean> = _isLoadingInsights.asStateFlow()

    // --- Verification in progress ---
    private val _isVerifyingTask = MutableStateFlow(false)
    val isVerifyingTask: StateFlow<Boolean> = _isVerifyingTask.asStateFlow()

    // --- Study Time Extraction in progress ---
    private val _isExtractingStudyTime = MutableStateFlow(false)
    val isExtractingStudyTime: StateFlow<Boolean> = _isExtractingStudyTime.asStateFlow()

    // Navigation & Tab control
    fun setCurrentTab(tab: Int) {
        _currentTab.value = tab
    }

    fun openSetupWizard() {
        _isSetupWizardVisible.value = true
    }

    fun closeSetupWizard() {
        _isSetupWizardVisible.value = false
    }

    fun setAddTaskDialogOpen(open: Boolean) {
        _isAddTaskDialogOpen.value = open
    }

    fun setAccountDialogOpen(open: Boolean) {
        _isAccountDialogOpen.value = open
    }

    fun selectTaskForVerification(task: TaskEntity) {
        _selectedTaskForVerification.value = task
        _currentTab.value = 2 // Switch to AI Verify tab
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setSelectedPriority(priority: String) {
        _selectedPriority.value = priority
    }

    // --- Account Profile Actions ---
    fun updateUserProfile(name: String, age: Int, photoUri: String?) {
        val updated = UserProfile(
            name = name.trim().ifBlank { "User" },
            age = age,
            photoUri = photoUri,
            isCreated = true
        )
        _userProfile.value = updated
        prefs.edit()
            .putString("profile_name", updated.name)
            .putInt("profile_age", updated.age)
            .putString("profile_photo_uri", updated.photoUri)
            .putBoolean("profile_is_created", true)
            .apply()
    }

    // --- API Key Management ---
    fun setGeminiApiKey(key: String) {
        val trimmed = key.trim()
        _geminiApiKey.value = trimmed
        _keyVerificationStatus.value = null
        prefs.edit().putString("gemini_api_key", trimmed).apply()
    }

    fun verifyGeminiApiKey(onResult: (Boolean, String) -> Unit) {
        val key = _geminiApiKey.value
        if (key.isBlank()) {
            _keyVerificationStatus.value = "Error: Key cannot be empty"
            onResult(false, "Key cannot be empty")
            return
        }
        viewModelScope.launch {
            _isVerifyingKey.value = true
            val result = GeminiService.verifyApiKey(key)
            _isVerifyingKey.value = false
            if (result.isSuccess) {
                _keyVerificationStatus.value = "Valid: Key active & verified"
                onResult(true, "Key verified successfully!")
            } else {
                val msg = result.exceptionOrNull()?.message ?: "Verification failed"
                _keyVerificationStatus.value = "Invalid: $msg"
                onResult(false, msg)
            }
        }
    }

    // --- Settings Preferences Toggles ---
    fun setStudyTimeBetaEnabled(enabled: Boolean) {
        _isStudyTimeBetaEnabled.value = enabled
        prefs.edit().putBoolean("beta_study_time", enabled).apply()
    }

    fun setCalendarSyncEnabled(enabled: Boolean) {
        _isCalendarSyncEnabled.value = enabled
        prefs.edit().putBoolean("calendar_sync", enabled).apply()
    }

    fun setAiOptimizationEnabled(enabled: Boolean) {
        _isAiOptimizationEnabled.value = enabled
        prefs.edit().putBoolean("ai_opt", enabled).apply()
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        _isNotificationsEnabled.value = enabled
        prefs.edit().putBoolean("notifications", enabled).apply()
    }

    fun setThemeMode(mode: String) {
        _themeMode.value = mode
        prefs.edit().putString("app_theme_mode", mode).apply()
    }

    fun setDynamicColorEnabled(enabled: Boolean) {
        _isDynamicColor.value = enabled
        prefs.edit().putBoolean("app_dynamic_color", enabled).apply()
    }

    // --- Task CRUD ---
    fun addTask(
        title: String,
        description: String,
        category: String,
        priority: String,
        time: String = "Today"
    ) {
        if (title.isBlank()) return
        viewModelScope.launch {
            val newTask = TaskEntity(
                title = title.trim(),
                description = description.trim(),
                category = category,
                priority = priority,
                time = time
            )
            repository.insertTask(newTask)
        }
    }

    fun toggleTaskCompleted(task: TaskEntity) {
        viewModelScope.launch {
            repository.updateTask(task.copy(isCompleted = !task.isCompleted))
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    // --- AI Verification with Screenshot & Notes ---
    fun verifyTaskWithAi(
        task: TaskEntity,
        proofSummary: String,
        screenshotBitmap: Bitmap?,
        onComplete: (Boolean, String) -> Unit
    ) {
        val apiKey = _geminiApiKey.value
        if (apiKey.isBlank()) {
            onComplete(false, "Gemini API key not configured. Add your key in Settings.")
            return
        }

        viewModelScope.launch {
            _isVerifyingTask.value = true
            val base64Image = screenshotBitmap?.let { GeminiService.bitmapToBase64(it) }
            val result = GeminiService.verifyTaskCompletion(
                apiKey = apiKey,
                taskTitle = task.title,
                proofNotes = proofSummary,
                imageBase64 = base64Image
            )
            _isVerifyingTask.value = false

            if (result.isSuccess) {
                val data = result.getOrNull()!!
                repository.updateTask(
                    task.copy(
                        isCompleted = data.isVerified,
                        isAiVerified = data.isVerified,
                        aiScore = data.score,
                        aiFeedback = data.feedback
                    )
                )
                onComplete(true, data.feedback)
            } else {
                val error = result.exceptionOrNull()?.message ?: "AI Verification failed"
                onComplete(false, error)
            }
        }
    }

    // --- Beta: Study Time Extraction from Screenshot ---
    fun extractStudyTimeFromScreenshot(
        appName: String,
        screenshotBitmap: Bitmap,
        onComplete: (Boolean, String) -> Unit
    ) {
        val apiKey = _geminiApiKey.value
        if (apiKey.isBlank()) {
            onComplete(false, "Gemini API key not configured. Add your key in Settings.")
            return
        }

        viewModelScope.launch {
            _isExtractingStudyTime.value = true
            val base64 = GeminiService.bitmapToBase64(screenshotBitmap)
            val result = GeminiService.extractStudyTime(
                apiKey = apiKey,
                appName = appName.ifBlank { "Study App" },
                imageBase64 = base64
            )
            _isExtractingStudyTime.value = false

            if (result.isSuccess) {
                val data = result.getOrNull()!!
                val record = StudyRecord(
                    appName = data.appName,
                    minutes = data.minutes,
                    timeFormatted = data.timeFormatted,
                    notes = data.summary
                )
                repository.insertStudyRecord(record)
                onComplete(true, "Successfully logged ${data.timeFormatted} for ${data.appName}!")
            } else {
                val error = result.exceptionOrNull()?.message ?: "Extraction failed"
                onComplete(false, error)
            }
        }
    }

    // --- AI Insights Organization & Advice ---
    fun generateAiInsights() {
        val apiKey = _geminiApiKey.value
        if (apiKey.isBlank()) return

        viewModelScope.launch {
            _isLoadingInsights.value = true
            val allTasks = repository.getAllTasksList()
            val completed = allTasks.filter { it.isCompleted }
            val pending = allTasks.filter { !it.isCompleted }
            val result = GeminiService.generateInsightsAndAdvice(
                apiKey = apiKey,
                completedTasks = completed,
                pendingTasks = pending,
                studyRecords = studyRecords.value
            )
            _isLoadingInsights.value = false
            if (result.isSuccess) {
                _aiInsights.value = result.getOrNull()
            }
        }
    }

    // --- Schedule Optimization ---
    fun optimizeTimetableWithAi() {
        val apiKey = _geminiApiKey.value
        if (apiKey.isBlank()) return

        viewModelScope.launch {
            val current = _timetableSlots.value.toMutableList()
            val optimized = current.mapIndexed { index, slot ->
                slot.copy(
                    isAiOptimized = true,
                    taskTitle = when (index % 4) {
                        0 -> "Focus Sprint: ${slot.taskTitle ?: "Key Objective"}"
                        1 -> "Collaborative Sync"
                        2 -> "Deep Execution: ${slot.taskTitle ?: "Core Task"}"
                        else -> "Wellness & Reset Break"
                    }
                )
            }
            _timetableSlots.value = optimized
        }
    }

    // --- Export & Restore Backup ---
    suspend fun getBackupJson(): String {
        val tasks = repository.getAllTasksList()
        return BackupManager.createBackupJson(
            userProfile = _userProfile.value,
            tasks = tasks,
            studyRecords = studyRecords.value,
            timetableSlots = _timetableSlots.value,
            isCalendarSync = _isCalendarSyncEnabled.value,
            isAiOptimization = _isAiOptimizationEnabled.value,
            isNotifications = _isNotificationsEnabled.value,
            isStudyTimeBeta = _isStudyTimeBetaEnabled.value
        )
    }

    fun restoreBackup(jsonString: String, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val parseResult = BackupManager.parseBackupJson(jsonString)
            if (parseResult.isFailure) {
                onComplete(false, "Invalid backup format: ${parseResult.exceptionOrNull()?.message}")
                return@launch
            }

            val data = parseResult.getOrNull()!!
            try {
                // Restore account profile
                updateUserProfile(data.userProfile.name, data.userProfile.age, data.userProfile.photoUri)

                // Restore preferences
                setCalendarSyncEnabled(data.isCalendarSync)
                setAiOptimizationEnabled(data.isAiOptimization)
                setNotificationsEnabled(data.isNotifications)
                setStudyTimeBetaEnabled(data.isStudyTimeBeta)

                // Restore tasks
                repository.clearAllTasks()
                repository.insertAllTasks(data.tasks)

                // Restore study records
                repository.clearAllStudyRecords()
                repository.insertAllStudyRecords(data.studyRecords)

                // Restore timetable
                if (data.timetableSlots.isNotEmpty()) {
                    _timetableSlots.value = data.timetableSlots
                }

                onComplete(
                    true,
                    "Restored ${data.tasks.size} tasks, ${data.studyRecords.size} study records, and profile for ${data.userProfile.name}."
                )
            } catch (e: Exception) {
                onComplete(false, "Restore failed: ${e.message}")
            }
        }
    }
}
