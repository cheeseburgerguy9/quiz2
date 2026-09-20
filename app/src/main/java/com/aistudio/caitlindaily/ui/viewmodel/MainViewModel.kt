package com.aistudio.caitlindaily.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.net.Uri
import android.util.Base64
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aistudio.caitlindaily.data.api.GeminiService
import com.aistudio.caitlindaily.data.api.InsightsResult
import com.aistudio.caitlindaily.data.db.AppDatabase
import com.aistudio.caitlindaily.data.model.StudyRecord
import com.aistudio.caitlindaily.data.model.TaskEntity
import com.aistudio.caitlindaily.data.model.TimetableSlot
import com.aistudio.caitlindaily.data.model.UserProfile
import com.aistudio.caitlindaily.data.repository.TaskRepository
import com.aistudio.caitlindaily.util.BackupManager
import com.aistudio.caitlindaily.util.SecureKeyStore
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
    private val secureKeyStore: SecureKeyStore

    init {
        val db = AppDatabase.getInstance(application)
        repository = TaskRepository(db.taskDao(), db.studyDao())
        prefs = application.getSharedPreferences("caitlin_daily_prefs", Context.MODE_PRIVATE)
        secureKeyStore = SecureKeyStore(application)
        val legacyKey = prefs.getString("gemini_api_key", "").orEmpty()
        if (secureKeyStore.get().isBlank() && legacyKey.isNotBlank()) {
            secureKeyStore.put(legacyKey)
            prefs.edit().remove("gemini_api_key").apply()
        }
        if (!prefs.contains("app_created_at")) {
            prefs.edit().putLong("app_created_at", System.currentTimeMillis()).apply()
        }
    }

    // --- Offline Account Profile ---
    private val _userProfile = MutableStateFlow(
        UserProfile(
            name = prefs.getString("profile_name", "") ?: "",
            age = prefs.getInt("profile_age", 0),
            photoUri = prefs.getString("profile_photo_uri", null),
            isCreated = prefs.getBoolean("profile_is_created", false)
        )
    )
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    // --- Gemini User API Key ---
    private val _geminiApiKey = MutableStateFlow(secureKeyStore.get())
    val geminiApiKey: StateFlow<String> = _geminiApiKey.asStateFlow()

    private val _isVerifyingKey = MutableStateFlow(false)
    val isVerifyingKey: StateFlow<Boolean> = _isVerifyingKey.asStateFlow()

    private val _keyVerificationStatus = MutableStateFlow<String?>(null)
    val keyVerificationStatus: StateFlow<String?> = _keyVerificationStatus.asStateFlow()

    private val _isGeminiKeyVerified = MutableStateFlow(
        prefs.getBoolean("gemini_key_verified", false) && _geminiApiKey.value.isNotBlank()
    )
    val isGeminiAvailable: StateFlow<Boolean> = combine(_geminiApiKey, _isGeminiKeyVerified) { key, verified ->
        key.isNotBlank() && verified
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // --- Settings Preferences ---
    private val _isStudyTimeBetaEnabled = MutableStateFlow(prefs.getBoolean("beta_study_time", false))
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

    private val _isSetupWizardVisible = MutableStateFlow(!_userProfile.value.isCreated)
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
    private val _timetableSlots = MutableStateFlow<List<TimetableSlot>>(emptyList())
    val timetableSlots: StateFlow<List<TimetableSlot>> = _timetableSlots.asStateFlow()

    // --- AI Insights State ---
    private val _aiInsights = MutableStateFlow<InsightsResult?>(null)
    val aiInsights: StateFlow<InsightsResult?> = _aiInsights.asStateFlow()

    private val _isLoadingInsights = MutableStateFlow(false)
    val isLoadingInsights: StateFlow<Boolean> = _isLoadingInsights.asStateFlow()

    private val _isOptimizingTimetable = MutableStateFlow(false)
    val isOptimizingTimetable: StateFlow<Boolean> = _isOptimizingTimetable.asStateFlow()

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
            name = name.trim(),
            age = age.coerceIn(1, 120),
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
        _isGeminiKeyVerified.value = false
        if (trimmed.isBlank() && _currentTab.value in 2..3) _currentTab.value = 0
        _keyVerificationStatus.value = if (trimmed.isBlank()) null else "Key saved. Verify it to enable Gemini."
        if (trimmed.isBlank()) secureKeyStore.clear() else secureKeyStore.put(trimmed)
        prefs.edit().putBoolean("gemini_key_verified", false).apply()
    }

    fun verifyGeminiApiKey(onResult: (Boolean, String) -> Unit) {
        val key = _geminiApiKey.value
        if (key.isBlank()) {
            _isGeminiKeyVerified.value = false
            prefs.edit().putBoolean("gemini_key_verified", false).apply()
            _keyVerificationStatus.value = "Error: Key cannot be empty"
            onResult(false, "Key cannot be empty")
            return
        }
        viewModelScope.launch {
            _isVerifyingKey.value = true
            val result = GeminiService.verifyApiKey(key)
            _isVerifyingKey.value = false
            if (result.isSuccess) {
                _isGeminiKeyVerified.value = true
                prefs.edit().putBoolean("gemini_key_verified", true).apply()
                _keyVerificationStatus.value = "Valid: Key active & verified"
                onResult(true, "Key verified successfully!")
            } else {
                val msg = result.exceptionOrNull()?.message ?: "Verification failed"
                _isGeminiKeyVerified.value = false
                prefs.edit().putBoolean("gemini_key_verified", false).apply()
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
        // High-priority completion is an AI-only state; the UI is not the only guard.
        if (task.priority.equals("High", ignoreCase = true)) return
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
        if (apiKey.isBlank() || !_isGeminiKeyVerified.value) {
            onComplete(false, "Verify your Gemini API key in Settings first.")
            return
        }
        if (proofSummary.trim().isBlank() || screenshotBitmap == null) {
            onComplete(false, "A screenshot and written evidence are required for AI verification.")
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
                onComplete(data.isVerified, data.feedback)
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
        if (apiKey.isBlank() || !_isGeminiKeyVerified.value) {
            onComplete(false, "Verify your Gemini API key in Settings first.")
            return
        }
        if (!_isStudyTimeBetaEnabled.value) {
            onComplete(false, "Study Time Beta is disabled in Settings.")
            return
        }
        if (appName.trim().isBlank()) {
            onComplete(false, "Enter the app name to look for in the screenshot.")
            return
        }

        viewModelScope.launch {
            _isExtractingStudyTime.value = true
            val base64 = GeminiService.bitmapToBase64(screenshotBitmap)
            val result = GeminiService.extractStudyTime(
                apiKey = apiKey,
                appName = appName.trim(),
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
        if (apiKey.isBlank() || !_isGeminiKeyVerified.value) return

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
        if (apiKey.isBlank() || !_isGeminiKeyVerified.value) return
        viewModelScope.launch {
            _isOptimizingTimetable.value = true
            val result = GeminiService.optimizeTimetable(apiKey, repository.getAllTasksList())
            if (result.isSuccess) {
                _timetableSlots.value = result.getOrNull()?.slots.orEmpty()
            }
            _isOptimizingTimetable.value = false
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
            isStudyTimeBeta = _isStudyTimeBetaEnabled.value,
            appCreatedAt = prefs.getLong("app_created_at", System.currentTimeMillis()),
            themeMode = _themeMode.value,
            isDynamicColor = _isDynamicColor.value,
            photoBase64 = encodeProfilePhoto(_userProfile.value.photoUri)
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
                // Restore account profile, including a portable copy of the selected photo.
                val restoredPhotoUri = data.photoBase64?.let { saveRestoredProfilePhoto(it) } ?: data.userProfile.photoUri
                updateUserProfile(data.userProfile.name, data.userProfile.age, restoredPhotoUri)

                // Restore preferences
                setCalendarSyncEnabled(data.isCalendarSync)
                setAiOptimizationEnabled(data.isAiOptimization)
                setNotificationsEnabled(data.isNotifications)
                setStudyTimeBetaEnabled(data.isStudyTimeBeta)
                prefs.edit().putLong("app_created_at", data.appCreatedAt).apply()
                setThemeMode(data.themeMode)
                setDynamicColorEnabled(data.isDynamicColor)

                // Restore tasks
                repository.clearAllTasks()
                val safeTasks = data.tasks.map { task ->
                    if (task.priority.equals("High", ignoreCase = true) && !task.isAiVerified) {
                        task.copy(isCompleted = false)
                    } else task
                }
                repository.insertAllTasks(safeTasks)

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

    private fun encodeProfilePhoto(uriString: String?): String? {
        if (uriString.isNullOrBlank()) return null
        return try {
            getApplication<Application>().contentResolver.openInputStream(Uri.parse(uriString))?.use { input ->
                Base64.encodeToString(input.readBytes(), Base64.NO_WRAP)
            }
        } catch (_: Exception) { null }
    }

    private fun saveRestoredProfilePhoto(base64: String): String? {
        return try {
            val bytes = Base64.decode(base64, Base64.DEFAULT)
            val file = java.io.File(getApplication<Application>().filesDir, "profile_photo.jpg")
            file.writeBytes(bytes)
            Uri.fromFile(file).toString()
        } catch (_: Exception) { null }
    }

}
