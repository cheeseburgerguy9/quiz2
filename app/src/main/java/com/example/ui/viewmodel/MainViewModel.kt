package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.GeminiService
import com.example.data.model.ProductivityInsight
import com.example.data.model.ScreenTimeAnalysisResult
import com.example.data.model.TaskEntity
import com.example.data.model.TimetableSlot
import com.example.data.model.UserProfile
import com.example.data.model.VerificationResult
import com.example.data.repository.TaskRepository
import com.example.ui.screens.WizardDraftTask
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppNavTab(val title: String) {
    TASKS("Tasks"),
    TIMETABLE("AI Timetable"),
    VERIFY("AI Verify"),
    INSIGHTS("Insights & Stats"),
    SETTINGS("Settings")
}

data class VerificationUiState(
    val selectedTask: TaskEntity? = null,
    val selectedBitmap: Bitmap? = null,
    val selectedUriString: String? = null,
    val isVerifying: Boolean = false,
    val lastResult: VerificationResult? = null,
    val errorMessage: String? = null
)

data class ScreenTimeUiState(
    val selectedBitmap: Bitmap? = null,
    val selectedUriString: String? = null,
    val targetAppName: String = "Duolingo",
    val isAnalyzing: Boolean = false,
    val lastResult: ScreenTimeAnalysisResult? = null,
    val errorMessage: String? = null
)

data class GeminiStatusUiState(
    val isConfigured: Boolean = false,
    val isPinging: Boolean = false,
    val lastPingLatencyMs: Long? = null,
    val statusMessage: String = "Add your Gemini API key in Settings"
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = TaskRepository(application.applicationContext)

    val currentTab = MutableStateFlow(AppNavTab.TASKS)
    val isSetupCompleted: StateFlow<Boolean> = repository.isSetupCompleted
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val searchQuery = MutableStateFlow("")
    val selectedCategoryFilter = MutableStateFlow("ALL")
    val selectedPriorityFilter = MutableStateFlow("ALL")

    val rawTasks: StateFlow<List<TaskEntity>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredTasks: StateFlow<List<TaskEntity>> = combine(
        rawTasks, searchQuery, selectedCategoryFilter, selectedPriorityFilter
    ) { tasks, query, catFilter, prioFilter ->
        tasks.filter { task ->
            val matchesQuery = query.isBlank() ||
                task.title.contains(query, true) || task.description.contains(query, true)
            val matchesCat = catFilter == "ALL" || task.category.equals(catFilter, true)
            val matchesPrio = prioFilter == "ALL" || task.priority.equals(prioFilter, true)
            matchesQuery && matchesCat && matchesPrio
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val timetableSlots: StateFlow<List<TimetableSlot>> = repository.getTimetableForDate()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val savedRoutine = repository.getSavedRoutine()
    val sleepScheduleInput = MutableStateFlow(savedRoutine.first)
    val mealScheduleInput = MutableStateFlow(savedRoutine.second)
    val extraConstraintsInput = MutableStateFlow(savedRoutine.third)
    val isGeneratingTimetable = MutableStateFlow(false)

    private val _verificationState = MutableStateFlow(VerificationUiState())
    val verificationState = _verificationState.asStateFlow()

    val productivityInsights: StateFlow<ProductivityInsight?> = repository.cachedInsights
    val isLoadingInsights = MutableStateFlow(false)

    val isAiEnabled: StateFlow<Boolean> = repository.isAiEnabled
    val themeMode: StateFlow<String> = repository.themeMode
    val studyTimeBetaEnabled: StateFlow<Boolean> = repository.studyTimeBetaEnabled
    val totalStudyMinutes: StateFlow<Int> = repository.totalStudyMinutes

    private val _screenTimeState = MutableStateFlow(ScreenTimeUiState())
    val screenTimeState = _screenTimeState.asStateFlow()

    val userProfile: StateFlow<UserProfile> = repository.userProfile

    private val _geminiStatus = MutableStateFlow(GeminiStatusUiState())
    val geminiStatus = _geminiStatus.asStateFlow()

    val userNotification = MutableStateFlow<String?>(null)

    init {
        GeminiService.initialize(application.applicationContext)
        refreshGeminiStatus()
    }

    fun completeSetupWizard(
        name: String,
        age: Int?,
        photoUri: Uri?,
        sleepSchedule: String,
        mealSchedule: String,
        extraConstraints: String,
        initialTasks: List<WizardDraftTask>,
        generateTimetableNow: Boolean
    ) {
        viewModelScope.launch {
            repository.completeSetup(name, age, sleepSchedule, mealSchedule, extraConstraints)
            if (photoUri != null) repository.updateProfile(name, age, photoUri)
            sleepScheduleInput.value = sleepSchedule
            mealScheduleInput.value = mealSchedule
            extraConstraintsInput.value = extraConstraints

            val today = repository.getTodayDate()
            val entities = initialTasks.map { draft ->
                TaskEntity(
                    title = draft.title,
                    category = draft.category,
                    priority = draft.priority,
                    estimatedMinutes = draft.minutes,
                    scheduledTime = "",
                    createdDate = today
                )
            }
            if (entities.isNotEmpty()) repository.addTasksBatch(entities)
            userNotification.value = "Welcome, ${name.trim().ifBlank { "Caitlin" }}! Your offline workspace is ready."
            if (generateTimetableNow) generateTimetableWithGemini()
        }
    }

    fun updateProfile(name: String, age: Int?, photoUri: Uri?) {
        viewModelScope.launch {
            try {
                repository.updateProfile(name, age, photoUri)
                userNotification.value = "Profile updated"
            } catch (e: Exception) {
                userNotification.value = "Could not update profile: ${e.message}"
            }
        }
    }

    fun setGeminiApiKey(key: String) {
        try {
            GeminiService.setApiKey(key)
            refreshGeminiStatus()
            userNotification.value = "Gemini API key saved securely on this device"
        } catch (e: Exception) {
            userNotification.value = "Could not save Gemini API key: ${e.message}"
        }
    }

    fun clearGeminiApiKey() {
        GeminiService.clearApiKey()
        refreshGeminiStatus()
        userNotification.value = "Gemini API key removed"
    }

    fun exportBackup(uri: Uri) {
        viewModelScope.launch {
            try {
                val json = repository.createBackupJson()
                getApplication<Application>().contentResolver.openOutputStream(uri)?.use {
                    it.write(json.toByteArray(Charsets.UTF_8))
                } ?: error("Could not open backup destination")
                userNotification.value = "Backup exported successfully"
            } catch (e: Exception) {
                userNotification.value = "Backup export failed: ${e.message}"
            }
        }
    }

    fun importBackup(uri: Uri) {
        viewModelScope.launch {
            try {
                val json = getApplication<Application>().contentResolver.openInputStream(uri)
                    ?.bufferedReader()?.use { it.readText() }
                    ?: error("Could not read backup file")
                val result = repository.restoreBackupJson(json)
                if (result.isSuccess) {
                    val routine = repository.getSavedRoutine()
                    sleepScheduleInput.value = routine.first
                    mealScheduleInput.value = routine.second
                    extraConstraintsInput.value = routine.third
                    userNotification.value = "Backup imported. Your offline data was restored."
                } else {
                    userNotification.value = "Import failed: ${result.exceptionOrNull()?.message}"
                }
            } catch (e: Exception) {
                userNotification.value = "Backup import failed: ${e.message}"
            }
        }
    }

    fun restartSetupWizard() = repository.resetSetup()

    fun selectTab(tab: AppNavTab) { currentTab.value = tab }
    fun onSearchQueryChange(query: String) { searchQuery.value = query }
    fun onCategoryFilterChange(category: String) { selectedCategoryFilter.value = category }
    fun onPriorityFilterChange(priority: String) { selectedPriorityFilter.value = priority }

    fun addTask(title: String, description: String, category: String, priority: String, estimatedMinutes: Int, scheduledTime: String) {
        viewModelScope.launch {
            repository.addTask(title, description, category, priority, estimatedMinutes, scheduledTime)
            userNotification.value = "Task added: $title"
        }
    }

    fun toggleTaskCompletion(task: TaskEntity) {
        if (task.priority.equals("HIGH", true) && !task.isCompleted && !task.isVerified) {
            userNotification.value = "High priority tasks require AI Verification with screenshot proof"
            return
        }
        viewModelScope.launch { repository.toggleTaskCompletion(task) }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.deleteTask(task)
            userNotification.value = "Task removed"
        }
    }

    fun addTaskToCalendar(task: TaskEntity) {
        if (repository.addTaskToCalendar(task)) {
            viewModelScope.launch { repository.markTaskCalendarSynced(task.id, true) }
            userNotification.value = "Opening calendar for '${task.title}'"
        }
    }

    fun addTimetableSlotToCalendar(slot: TimetableSlot) {
        if (repository.addTimetableSlotToCalendar(slot)) {
            userNotification.value = "Opening calendar for '${slot.title}'"
        }
    }

    fun generateTimetableWithGemini() {
        if (!isAiEnabled.value) {
            userNotification.value = "AI features are turned off in Settings."
            return
        }
        if (!GeminiService.isApiKeyConfigured) {
            userNotification.value = "Add your Gemini API key in Settings first."
            return
        }
        viewModelScope.launch {
            isGeneratingTimetable.value = true
            val result = repository.generateTimetableWithGemini(
                rawTasks.value, sleepScheduleInput.value, mealScheduleInput.value,
                extraConstraintsInput.value
            )
            isGeneratingTimetable.value = false
            userNotification.value = if (result.isSuccess) {
                "Gemini generated today's schedule"
            } else {
                "Gemini error: ${result.exceptionOrNull()?.message ?: "Unknown error"}"
            }
        }
    }

    fun prepareTaskForVerification(task: TaskEntity) {
        _verificationState.value = VerificationUiState(selectedTask = task)
        currentTab.value = AppNavTab.VERIFY
    }

    fun onScreenshotSelected(bitmap: Bitmap, uriString: String?) {
        _verificationState.value = _verificationState.value.copy(
            selectedBitmap = bitmap, selectedUriString = uriString, errorMessage = null
        )
    }

    fun runScreenshotVerification() {
        if (!isAiEnabled.value || !GeminiService.isApiKeyConfigured) {
            val message = if (!GeminiService.isApiKeyConfigured)
                "Add your Gemini API key in Settings first." else "AI features are disabled in Settings."
            _verificationState.value = _verificationState.value.copy(errorMessage = message)
            return
        }
        val task = _verificationState.value.selectedTask
        val bitmap = _verificationState.value.selectedBitmap
        if (task == null || bitmap == null) {
            _verificationState.value = _verificationState.value.copy(
                errorMessage = if (task == null) "Please select a task to verify" else "Please upload a screenshot first"
            )
            return
        }
        viewModelScope.launch {
            _verificationState.value = _verificationState.value.copy(isVerifying = true, errorMessage = null)
            val result = repository.verifyTaskWithScreenshot(task, bitmap, _verificationState.value.selectedUriString)
            _verificationState.value = _verificationState.value.copy(
                isVerifying = false,
                lastResult = result.getOrNull(),
                errorMessage = result.exceptionOrNull()?.message
            )
            if (result.isSuccess) userNotification.value = "Gemini verification complete"
        }
    }

    fun refreshProductivityInsights() {
        if (!isAiEnabled.value || !GeminiService.isApiKeyConfigured) {
            userNotification.value = if (!GeminiService.isApiKeyConfigured)
                "Add your Gemini API key in Settings first." else "AI features are disabled in Settings."
            return
        }
        viewModelScope.launch {
            isLoadingInsights.value = true
            val result = repository.fetchProductivityInsights(rawTasks.value)
            isLoadingInsights.value = false
            userNotification.value = if (result.isSuccess)
                "Gemini productivity insights refreshed"
            else "Gemini error: ${result.exceptionOrNull()?.message ?: "Unknown error"}"
        }
    }

    fun onScreenTimeScreenshotSelected(bitmap: Bitmap, uriString: String?) {
        _screenTimeState.value = _screenTimeState.value.copy(
            selectedBitmap = bitmap, selectedUriString = uriString, errorMessage = null
        )
    }

    fun setScreenTimeTargetApp(appName: String) {
        _screenTimeState.value = _screenTimeState.value.copy(targetAppName = appName)
    }

    fun analyzeScreenTimeStudyMinutes() {
        if (!isAiEnabled.value || !GeminiService.isApiKeyConfigured) {
            _screenTimeState.value = _screenTimeState.value.copy(
                errorMessage = "Add your Gemini API key in Settings first."
            )
            return
        }
        val bitmap = _screenTimeState.value.selectedBitmap
        val appName = _screenTimeState.value.targetAppName.trim()
        if (bitmap == null || appName.isBlank()) {
            _screenTimeState.value = _screenTimeState.value.copy(
                errorMessage = if (bitmap == null) "Please upload a Digital Wellbeing screenshot" else "Please enter the study app name"
            )
            return
        }
        viewModelScope.launch {
            _screenTimeState.value = _screenTimeState.value.copy(isAnalyzing = true, errorMessage = null)
            val result = repository.analyzeScreenTime(bitmap, appName)
            _screenTimeState.value = _screenTimeState.value.copy(
                isAnalyzing = false,
                lastResult = result.getOrNull(),
                errorMessage = result.exceptionOrNull()?.message
            )
            if (result.isSuccess) {
                result.getOrNull()?.let {
                    repository.addStudyMinutes(it.studyMinutes)
                    userNotification.value = "Added ${it.detectedTimeFormatted} study time"
                }
            }
        }
    }

    fun setAiEnabled(enabled: Boolean) {
        repository.setAiEnabled(enabled)
        userNotification.value = if (enabled) "AI features enabled" else "Offline mode enabled"
    }

    fun setThemeMode(mode: String) { repository.setThemeMode(mode) }

    fun setStudyTimeBetaEnabled(enabled: Boolean) {
        repository.setStudyTimeBetaEnabled(enabled)
    }

    fun pingGemini() {
        viewModelScope.launch {
            _geminiStatus.value = _geminiStatus.value.copy(isPinging = true)
            val result = repository.pingGemini()
            _geminiStatus.value = if (result.isSuccess) {
                val latency = result.getOrNull()?.first ?: 0
                GeminiStatusUiState(true, false, latency, "Connected • ${latency} ms")
            } else {
                GeminiStatusUiState(
                    GeminiService.isApiKeyConfigured, false, null,
                    if (GeminiService.isApiKeyConfigured) "Key saved; connection failed"
                    else "No API key saved"
                )
            }
        }
    }

    fun refreshGeminiStatus() {
        _geminiStatus.value = GeminiStatusUiState(
            isConfigured = GeminiService.isApiKeyConfigured,
            statusMessage = if (GeminiService.isApiKeyConfigured) "API key saved on this device" else "No API key saved"
        )
    }

    fun showNotification(message: String) { userNotification.value = message }
    fun clearNotification() { userNotification.value = null }
}
