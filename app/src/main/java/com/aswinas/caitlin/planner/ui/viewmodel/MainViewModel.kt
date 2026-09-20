package com.aswinas.caitlin.planner.ui.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aswinas.caitlin.planner.data.api.GeminiService
import com.aswinas.caitlin.planner.data.db.AppDatabase
import com.aswinas.caitlin.planner.data.model.DailyAiOverview
import com.aswinas.caitlin.planner.data.model.StudyRecord
import com.aswinas.caitlin.planner.data.model.TaskEntity
import com.aswinas.caitlin.planner.data.model.TimetableSlot
import com.aswinas.caitlin.planner.data.model.UserProfile
import com.aswinas.caitlin.planner.data.repository.TaskRepository
import com.aswinas.caitlin.planner.util.BackupManager
import com.aswinas.caitlin.planner.util.CalendarHelper
import com.aswinas.caitlin.planner.util.NotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("caitlin_daily_prefs", Context.MODE_PRIVATE)
    private val database = AppDatabase.getInstance(application)
    private val repository = TaskRepository(database.taskDao(), database.studyDao())

    // Profile state
    private val _userProfile = MutableStateFlow(
        UserProfile(
            name = prefs.getString("user_name", "") ?: "",
            age = prefs.getInt("user_age", 0),
            photoUri = prefs.getString("user_photo_uri", null),
            isCreated = prefs.getBoolean("user_created", false)
        )
    )
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    // Wizard completed state
    private val _isWizardCompleted = MutableStateFlow(
        prefs.getBoolean("wizard_completed", false)
    )
    val isWizardCompleted: StateFlow<Boolean> = _isWizardCompleted.asStateFlow()

    // Gemini API Key & verification status
    private val _geminiApiKey = MutableStateFlow(
        prefs.getString("gemini_api_key", "") ?: ""
    )
    val geminiApiKey: StateFlow<String> = _geminiApiKey.asStateFlow()

    private val _isVerifyingKey = MutableStateFlow(false)
    val isVerifyingKey: StateFlow<Boolean> = _isVerifyingKey.asStateFlow()

    private val _keyVerificationStatus = MutableStateFlow<String?>(null)
    val keyVerificationStatus: StateFlow<String?> = _keyVerificationStatus.asStateFlow()

    // Feature Toggles
    private val _isStudyTimeBetaEnabled = MutableStateFlow(
        prefs.getBoolean("pref_study_time_beta", true)
    )
    val isStudyTimeBetaEnabled: StateFlow<Boolean> = _isStudyTimeBetaEnabled.asStateFlow()

    private val _isCalendarSync = MutableStateFlow(
        prefs.getBoolean("pref_calendar_sync", true)
    )
    val isCalendarSync: StateFlow<Boolean> = _isCalendarSync.asStateFlow()

    private val _isAiOptimization = MutableStateFlow(
        prefs.getBoolean("pref_ai_optimization", true)
    )
    val isAiOptimization: StateFlow<Boolean> = _isAiOptimization.asStateFlow()

    private val _isNotifications = MutableStateFlow(
        prefs.getBoolean("pref_notifications", true)
    )
    val isNotifications: StateFlow<Boolean> = _isNotifications.asStateFlow()

    // Theme mode ("auto", "dark", "light") and dynamic color
    private val _themeMode = MutableStateFlow(
        prefs.getString("theme_mode", "auto") ?: "auto"
    )
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    private val _isDynamicColor = MutableStateFlow(
        prefs.getBoolean("dynamic_color", true)
    )
    val isDynamicColor: StateFlow<Boolean> = _isDynamicColor.asStateFlow()

    // Task Search and Filters
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _selectedPriority = MutableStateFlow("All")
    val selectedPriority: StateFlow<String> = _selectedPriority.asStateFlow()

    // Tasks and Study Records from Room
    val allTasks: StateFlow<List<TaskEntity>> = repository.allTasks.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val filteredTasks: StateFlow<List<TaskEntity>> = combine(
        allTasks,
        searchQuery,
        selectedCategory,
        selectedPriority
    ) { list, query, category, priority ->
        list.filter { task ->
            val matchesQuery = query.isBlank() ||
                    task.title.contains(query, ignoreCase = true) ||
                    task.description.contains(query, ignoreCase = true)
            val matchesCategory = category.equals("All", ignoreCase = true) ||
                    task.category.equals(category, ignoreCase = true)
            val matchesPriority = priority.equals("All", ignoreCase = true) ||
                    task.priority.equals(priority, ignoreCase = true)
            matchesQuery && matchesCategory && matchesPriority
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val studyRecords: StateFlow<List<StudyRecord>> = repository.allStudyRecords.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // AI Verification and AI Insights in progress
    private val _isVerifyingTask = MutableStateFlow(false)
    val isVerifyingTask: StateFlow<Boolean> = _isVerifyingTask.asStateFlow()

    private val _isExtractingStudyTime = MutableStateFlow(false)
    val isExtractingStudyTime: StateFlow<Boolean> = _isExtractingStudyTime.asStateFlow()

    private val _aiInsightSummary = MutableStateFlow<String?>(null)
    val aiInsightSummary: StateFlow<String?> = _aiInsightSummary.asStateFlow()

    private val _isLoadingAiInsights = MutableStateFlow(false)
    val isLoadingAiInsights: StateFlow<Boolean> = _isLoadingAiInsights.asStateFlow()

    // Timetable Slots (Preset baseline & Active today)
    private val defaultPresetSlots = listOf(
        TimetableSlot(id = "slot_1", timeLabel = "08:00 - 09:30", taskTitle = "Morning Deep Focus", category = "Work", isAiOptimized = false, isPresetFixed = true, alertEnabled = true),
        TimetableSlot(id = "slot_2", timeLabel = "10:00 - 11:30", taskTitle = "Study & Analysis", category = "Study", isAiOptimized = false, isPresetFixed = false, alertEnabled = true),
        TimetableSlot(id = "slot_3", timeLabel = "12:00 - 13:00", taskTitle = "Midday Reset & Nutrition", category = "Health", isAiOptimized = false, isPresetFixed = true, alertEnabled = false),
        TimetableSlot(id = "slot_4", timeLabel = "14:00 - 16:00", taskTitle = "Execution & Projects", category = "Work", isAiOptimized = false, isPresetFixed = false, alertEnabled = true),
        TimetableSlot(id = "slot_5", timeLabel = "17:00 - 18:30", taskTitle = "Active Cardio & Wellness", category = "Health", isAiOptimized = false, isPresetFixed = true, alertEnabled = false),
        TimetableSlot(id = "slot_6", timeLabel = "20:00 - 21:30", taskTitle = "Evening Synthesis", category = "Study", isAiOptimized = false, isPresetFixed = false, alertEnabled = true)
    )

    private val _presetSlots = MutableStateFlow<List<TimetableSlot>>(loadSlotsFromPrefs("preset_slots", defaultPresetSlots))
    val presetSlots: StateFlow<List<TimetableSlot>> = _presetSlots.asStateFlow()

    private val _timetableSlots = MutableStateFlow<List<TimetableSlot>>(loadSlotsFromPrefs("active_slots", defaultPresetSlots))
    val timetableSlots: StateFlow<List<TimetableSlot>> = _timetableSlots.asStateFlow()

    // Yesterday's Incomplete Tasks & Reminders
    private val _dismissedYesterdayIds = MutableStateFlow<Set<Long>>(emptySet())

    val yesterdayIncompleteTasks: StateFlow<List<TaskEntity>> = allTasks.combine(_dismissedYesterdayIds) { tasks, dismissed ->
        val startOfToday = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        tasks.filter { task ->
            !task.isCompleted &&
            !dismissed.contains(task.id) &&
            (task.dueDateMillis in 1 until startOfToday || (task.dueDateMillis == 0L && task.createdAt < startOfToday))
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Daily AI Overview & Preference State
    private val _dailyAiOverview = MutableStateFlow<DailyAiOverview>(loadDailyOverview())
    val dailyAiOverview: StateFlow<DailyAiOverview> = _dailyAiOverview.asStateFlow()

    private val _isSyncingDailyOverview = MutableStateFlow(false)
    val isSyncingDailyOverview: StateFlow<Boolean> = _isSyncingDailyOverview.asStateFlow()

    private val _overviewPreferenceFocus = MutableStateFlow(
        prefs.getString("pref_overview_focus", "Balanced Productivity") ?: "Balanced Productivity"
    )
    val overviewPreferenceFocus: StateFlow<String> = _overviewPreferenceFocus.asStateFlow()

    // Streak and Metrics
    val totalFocusMinutes: StateFlow<Int> = studyRecords.combine(allTasks) { records: List<StudyRecord>, tasks: List<TaskEntity> ->
        val recordMins = records.sumOf { it.minutes }
        val taskMins = tasks.count { it.isCompleted } * 25
        recordMins + taskMins
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    val currentStreakDays: StateFlow<Int> = studyRecords.combine(allTasks) { records, tasks ->
        val activeDays = mutableSetOf<Long>()
        records.forEach { if (it.minutes > 0) activeDays.add(it.timestamp / (1000L * 60L * 60L * 24L)) }
        tasks.filter { it.isCompleted }.forEach { activeDays.add(it.createdAt / (1000L * 60L * 60L * 24L)) }
        activeDays.size
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    init {
        NotificationHelper.createNotificationChannels(application)

        // Check for yesterday's incomplete tasks to remind user once per day
        viewModelScope.launch {
            allTasks.collect { tasks ->
                val startOfToday = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis

                val yesterdayIncomplete = tasks.filter { task ->
                    !task.isCompleted &&
                    (task.dueDateMillis in 1 until startOfToday || (task.dueDateMillis == 0L && task.createdAt < startOfToday))
                }

                val lastReminderDay = prefs.getLong("last_yesterday_reminder_day", 0L)
                val currentDayNumber = System.currentTimeMillis() / (1000L * 60L * 60L * 24L)

                if (yesterdayIncomplete.isNotEmpty() && lastReminderDay != currentDayNumber) {
                    prefs.edit().putLong("last_yesterday_reminder_day", currentDayNumber).apply()
                    NotificationHelper.sendYesterdayReminderNotification(
                        context = application,
                        count = yesterdayIncomplete.size,
                        firstTaskTitle = yesterdayIncomplete.first().title
                    )
                }
            }
        }
    }

    private fun loadSlotsFromPrefs(key: String, defaultSlots: List<TimetableSlot>): List<TimetableSlot> {
        val raw = prefs.getString(key, null) ?: return defaultSlots
        return try {
            val array = JSONArray(raw)
            val list = mutableListOf<TimetableSlot>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    TimetableSlot(
                        id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                        timeLabel = obj.optString("timeLabel", "09:00 - 10:00"),
                        taskTitle = obj.optString("taskTitle").takeIf { it.isNotBlank() },
                        category = obj.optString("category").takeIf { it.isNotBlank() },
                        priority = obj.optString("priority").takeIf { it.isNotBlank() },
                        isAiOptimized = obj.optBoolean("isAiOptimized", false),
                        alertEnabled = obj.optBoolean("alertEnabled", false),
                        isPresetFixed = obj.optBoolean("isPresetFixed", false)
                    )
                )
            }
            if (list.isNotEmpty()) list else defaultSlots
        } catch (e: Exception) {
            defaultSlots
        }
    }

    private fun saveSlotsToPrefs(key: String, slots: List<TimetableSlot>) {
        try {
            val array = JSONArray()
            slots.forEach { slot ->
                val obj = JSONObject().apply {
                    put("id", slot.id)
                    put("timeLabel", slot.timeLabel)
                    put("taskTitle", slot.taskTitle ?: "")
                    put("category", slot.category ?: "")
                    put("priority", slot.priority ?: "")
                    put("isAiOptimized", slot.isAiOptimized)
                    put("alertEnabled", slot.alertEnabled)
                    put("isPresetFixed", slot.isPresetFixed)
                }
                array.put(obj)
            }
            prefs.edit().putString(key, array.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadDailyOverview(): DailyAiOverview {
        val raw = prefs.getString("saved_daily_overview", null) ?: return DailyAiOverview()
        return try {
            val obj = JSONObject(raw)
            val tipsArr = obj.optJSONArray("keyTips") ?: JSONArray()
            val tips = mutableListOf<String>()
            for (i in 0 until tipsArr.length()) {
                tips.add(tipsArr.getString(i))
            }
            val recSlotsArr = obj.optJSONArray("recommendedSlots") ?: JSONArray()
            val slots = mutableListOf<String>()
            for (i in 0 until recSlotsArr.length()) {
                slots.add(recSlotsArr.getString(i))
            }
            DailyAiOverview(
                headline = obj.optString("headline", ""),
                focusScore = obj.optInt("focusScore", 0),
                velocityGrade = obj.optString("velocityGrade", obj.optString("velocityPace", "")),
                progressAnalysis = obj.optString("progressAnalysis", obj.optString("progressSummary", "")),
                actionableTips = tips,
                recommendedSlots = slots,
                lastUpdated = obj.optString("lastUpdated", obj.optString("lastUpdatedFormatted", "")),
                preferenceFocus = obj.optString("preferenceFocus", "")
            )
        } catch (e: Exception) {
            DailyAiOverview()
        }
    }

    private fun saveDailyOverview(overview: DailyAiOverview) {
        try {
            val obj = JSONObject().apply {
                put("headline", overview.headline)
                put("focusScore", overview.focusScore)
                put("velocityGrade", overview.velocityGrade)
                put("progressAnalysis", overview.progressAnalysis)
                put("lastUpdated", overview.lastUpdated)
                put("preferenceFocus", overview.preferenceFocus)
                val tipsArr = JSONArray()
                overview.actionableTips.forEach { tipsArr.put(it) }
                put("actionableTips", tipsArr)
                val slotsArr = JSONArray()
                overview.recommendedSlots.forEach { slotsArr.put(it) }
                put("recommendedSlots", slotsArr)
            }
            prefs.edit().putString("saved_daily_overview", obj.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // ==========================================
    // Profile & Wizard Functions
    // ==========================================
    fun updateProfile(name: String, age: Int, photoUri: String?) {
        _userProfile.value = UserProfile(name, age, photoUri, isCreated = true)
        prefs.edit()
            .putString("user_name", name)
            .putInt("user_age", age)
            .putString("user_photo_uri", photoUri)
            .putBoolean("user_created", true)
            .apply()
    }

    fun completeWizard(name: String, age: Int) {
        updateProfile(name, age, _userProfile.value.photoUri)
        _isWizardCompleted.value = true
        prefs.edit().putBoolean("wizard_completed", true).apply()
    }

    fun relaunchWizard() {
        _isWizardCompleted.value = false
    }

    // ==========================================
    // Gemini Key & Verification
    // ==========================================
    fun updateGeminiApiKey(key: String) {
        _geminiApiKey.value = key
        _keyVerificationStatus.value = null
        prefs.edit().putString("gemini_api_key", key).apply()
    }

    fun verifyGeminiApiKey(onResult: (Boolean, String) -> Unit) {
        val key = _geminiApiKey.value.trim()
        if (key.isBlank()) {
            onResult(false, "Please enter an API key first")
            return
        }

        viewModelScope.launch {
            _isVerifyingKey.value = true
            val result = GeminiService.verifyApiKey(key)
            _isVerifyingKey.value = false
            result.onSuccess { msg ->
                _keyVerificationStatus.value = "Valid key • Connected to Gemini 3.6 Flash"
                onResult(true, msg)
            }.onFailure { err ->
                val errMsg = err.message ?: "Verification failed"
                _keyVerificationStatus.value = "Error: $errMsg"
                onResult(false, errMsg)
            }
        }
    }

    // ==========================================
    // Settings Toggles
    // ==========================================
    fun setStudyTimeBetaEnabled(enabled: Boolean) {
        _isStudyTimeBetaEnabled.value = enabled
        prefs.edit().putBoolean("pref_study_time_beta", enabled).apply()
    }

    fun setCalendarSync(enabled: Boolean) {
        _isCalendarSync.value = enabled
        prefs.edit().putBoolean("pref_calendar_sync", enabled).apply()
    }

    fun setAiOptimization(enabled: Boolean) {
        _isAiOptimization.value = enabled
        prefs.edit().putBoolean("pref_ai_optimization", enabled).apply()
    }

    fun setNotifications(enabled: Boolean) {
        _isNotifications.value = enabled
        prefs.edit().putBoolean("pref_notifications", enabled).apply()
    }

    fun setThemeMode(mode: String) {
        _themeMode.value = mode
        prefs.edit().putString("theme_mode", mode).apply()
    }

    fun setDynamicColor(enabled: Boolean) {
        _isDynamicColor.value = enabled
        prefs.edit().putBoolean("dynamic_color", enabled).apply()
    }

    // ==========================================
    // Task Operations
    // ==========================================
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(cat: String) {
        _selectedCategory.value = cat
    }

    fun setSelectedPriority(priority: String) {
        _selectedPriority.value = priority
    }

    fun addNewTask(
        title: String,
        description: String,
        category: String,
        priority: String,
        time: String,
        recurrence: String = "None",
        selectedDates: List<Long> = emptyList()
    ) {
        viewModelScope.launch {
            val baseTime = System.currentTimeMillis()
            val baseTask = TaskEntity(
                title = title,
                description = description,
                category = category,
                priority = priority,
                time = time,
                recurrence = recurrence,
                dueDateMillis = baseTime
            )
            val newId = repository.insertTask(baseTask)

            // If calendar sync is enabled, sync task to device calendar
            if (_isCalendarSync.value) {
                CalendarHelper.insertTaskEvent(
                    context = getApplication(),
                    task = baseTask.copy(id = newId)
                )
            }

            // Task Repetition handling:
            if (recurrence == "Next Day") {
                val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
                val nextDayDateFormat = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
                val nextDayTime = time.replace(Regex("^[A-Za-z]{3},\\s[A-Za-z]{3}\\s\\d+"), nextDayDateFormat.format(cal.time))
                val nextDayTask = baseTask.copy(
                    id = 0,
                    time = nextDayTime,
                    dueDateMillis = cal.timeInMillis,
                    recurrence = "Next Day"
                )
                repository.insertTask(nextDayTask)
            } else if (recurrence == "Daily") {
                // Schedule next 3 days
                for (dayOffset in 1..3) {
                    val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, dayOffset) }
                    val dailyDateFormat = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
                    val dailyTime = time.replace(Regex("^[A-Za-z]{3},\\s[A-Za-z]{3}\\s\\d+"), dailyDateFormat.format(cal.time))
                    repository.insertTask(
                        baseTask.copy(
                            id = 0,
                            time = dailyTime,
                            dueDateMillis = cal.timeInMillis,
                            recurrence = "Daily"
                        )
                    )
                }
            } else if (recurrence == "Selected Dates" && selectedDates.isNotEmpty()) {
                val sdf = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
                selectedDates.forEach { dateMillis ->
                    val cal = Calendar.getInstance().apply { timeInMillis = dateMillis }
                    val repeatedTime = time.replace(Regex("^[A-Za-z]{3},\\s[A-Za-z]{3}\\s\\d+"), sdf.format(cal.time))
                    repository.insertTask(
                        baseTask.copy(
                            id = 0,
                            time = repeatedTime,
                            dueDateMillis = dateMillis,
                            recurrence = "Scheduled (${sdf.format(cal.time)})"
                        )
                    )
                }
            }
        }
    }

    // Yesterday's incomplete tasks actions
    fun moveYesterdayTaskToToday(task: TaskEntity) {
        viewModelScope.launch {
            val now = Calendar.getInstance()
            val sdf = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
            val updatedTime = task.time.replace(Regex("^[A-Za-z]{3},\\s[A-Za-z]{3}\\s\\d+"), sdf.format(now.time))
            repository.updateTask(task.copy(dueDateMillis = now.timeInMillis, time = updatedTime))
        }
    }

    fun completeYesterdayTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.updateTask(task.copy(isCompleted = true))
        }
    }

    fun dismissYesterdayReminder(taskId: Long) {
        _dismissedYesterdayIds.value = _dismissedYesterdayIds.value + taskId
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

    // ==========================================
    // AI Verification Logic
    // ==========================================
    fun verifyTaskWithAi(
        task: TaskEntity,
        proofText: String,
        screenshotBitmap: Bitmap?,
        onComplete: (Boolean, String) -> Unit
    ) {
        val apiKey = _geminiApiKey.value.trim()
        if (apiKey.isBlank()) {
            onComplete(false, "Gemini API key is required in Settings")
            return
        }

        viewModelScope.launch {
            _isVerifyingTask.value = true
            val result = GeminiService.verifyTaskCompletion(
                apiKey = apiKey,
                task = task,
                proofDescription = proofText,
                screenshot = screenshotBitmap
            )
            _isVerifyingTask.value = false

            result.onSuccess { verification ->
                if (verification.isVerified) {
                    val updatedTask = task.copy(
                        isCompleted = true,
                        isAiVerified = true,
                        aiScore = verification.score,
                        aiFeedback = verification.feedback
                    )
                    repository.updateTask(updatedTask)
                    onComplete(true, verification.feedback)
                } else {
                    onComplete(false, verification.feedback)
                }
            }.onFailure { err ->
                onComplete(false, err.message ?: "Verification failed")
            }
        }
    }

    fun extractStudyTimeFromScreenshot(
        appName: String,
        screenshotBitmap: Bitmap,
        onComplete: (Boolean, String) -> Unit
    ) {
        val apiKey = _geminiApiKey.value.trim()
        if (apiKey.isBlank()) {
            onComplete(false, "Gemini API key is required in Settings")
            return
        }

        viewModelScope.launch {
            _isExtractingStudyTime.value = true
            val result = GeminiService.extractStudyTimeFromScreenshot(
                apiKey = apiKey,
                appName = appName,
                screenshot = screenshotBitmap
            )
            _isExtractingStudyTime.value = false

            result.onSuccess { extracted ->
                if (extracted.minutes > 0) {
                    val record = StudyRecord(
                        appName = extracted.appName,
                        minutes = extracted.minutes,
                        timeFormatted = extracted.timeFormatted,
                        notes = extracted.summary
                    )
                    repository.insertStudyRecord(record)
                    onComplete(true, "Extracted ${extracted.timeFormatted} of study time for ${extracted.appName}!")
                } else {
                    onComplete(false, "Could not identify valid study minutes in screenshot.")
                }
            }.onFailure { err ->
                onComplete(false, err.message ?: "Could not analyze screenshot")
            }
        }
    }

    fun requestAiInsights() {
        val apiKey = _geminiApiKey.value.trim()
        if (apiKey.isBlank()) return

        viewModelScope.launch {
            _isLoadingAiInsights.value = true
            val completed = allTasks.value.filter { it.isCompleted }
            val pending = allTasks.value.filter { !it.isCompleted }
            val result = GeminiService.generateInsightsAndAdvice(
                apiKey = apiKey,
                completedTasks = completed,
                pendingTasks = pending,
                studyRecords = studyRecords.value
            )
            _isLoadingAiInsights.value = false
            result.onSuccess { insights ->
                val summary = "${insights.progressSummary}\n\nStatus: ${insights.velocityStatus}\n" +
                        insights.suggestions.joinToString("\n") { "• $it" }
                _aiInsightSummary.value = summary
            }.onFailure { err ->
                _aiInsightSummary.value = "Failed to generate insights: ${err.message}"
            }
        }
    }

    // ==========================================
    // Timetable & Alarm Alert Management
    // ==========================================
    fun toggleTimetableAlert(slot: TimetableSlot) {
        val updatedSlot = slot.copy(alertEnabled = !slot.alertEnabled)
        updateTimetableSlot(updatedSlot)
        if (updatedSlot.alertEnabled) {
            NotificationHelper.scheduleSlotAlarm(getApplication(), updatedSlot)
        } else {
            NotificationHelper.cancelSlotAlarm(getApplication(), slot.id)
        }
    }

    fun addTimetableSlot(slot: TimetableSlot) {
        val updatedList = _timetableSlots.value + slot
        _timetableSlots.value = updatedList
        saveSlotsToPrefs("active_slots", updatedList)
        if (slot.alertEnabled) {
            NotificationHelper.scheduleSlotAlarm(getApplication(), slot)
        }
    }

    fun updateTimetableSlot(slot: TimetableSlot) {
        val updatedList = _timetableSlots.value.map {
            if (it.id == slot.id) slot else it
        }
        _timetableSlots.value = updatedList
        saveSlotsToPrefs("active_slots", updatedList)
        if (slot.alertEnabled) {
            NotificationHelper.scheduleSlotAlarm(getApplication(), slot)
        } else {
            NotificationHelper.cancelSlotAlarm(getApplication(), slot.id)
        }
    }

    fun deleteTimetableSlot(slotId: String) {
        NotificationHelper.cancelSlotAlarm(getApplication(), slotId)
        val updatedList = _timetableSlots.value.filter { it.id != slotId }
        _timetableSlots.value = updatedList
        saveSlotsToPrefs("active_slots", updatedList)
    }

    fun saveCurrentTimetableAsPreset() {
        _presetSlots.value = _timetableSlots.value
        saveSlotsToPrefs("preset_slots", _timetableSlots.value)
    }

    fun resetToPresetTimetable() {
        _timetableSlots.value = _presetSlots.value
        saveSlotsToPrefs("active_slots", _presetSlots.value)
        _presetSlots.value.forEach { slot ->
            if (slot.alertEnabled) {
                NotificationHelper.scheduleSlotAlarm(getApplication(), slot)
            }
        }
    }

    fun optimizeTimetableWithTodayTasks() {
        val apiKey = _geminiApiKey.value.trim()
        viewModelScope.launch {
            val pendingTasks = allTasks.value.filter { !it.isCompleted }
            val optimized = GeminiService.optimizeScheduleWithPresets(
                apiKey = apiKey,
                presetSlots = _presetSlots.value,
                todayTasks = pendingTasks
            )
            if (optimized.isNotEmpty()) {
                _timetableSlots.value = optimized
                saveSlotsToPrefs("active_slots", optimized)
                optimized.forEach { slot ->
                    if (slot.alertEnabled) {
                        NotificationHelper.scheduleSlotAlarm(getApplication(), slot)
                    }
                }
            }
        }
    }

    // Keep backwards compatibility for any previous calls
    fun optimizeTimetable() {
        optimizeTimetableWithTodayTasks()
    }

    // ==========================================
    // Daily AI Overview & Insights Sync
    // ==========================================
    fun updateOverviewPreference(focus: String) {
        _overviewPreferenceFocus.value = focus
        prefs.edit().putString("pref_overview_focus", focus).apply()
        syncDailyOverview(force = true)
    }

    fun syncDailyOverview(force: Boolean = false) {
        val apiKey = _geminiApiKey.value.trim()
        viewModelScope.launch {
            _isSyncingDailyOverview.value = true
            val completed = allTasks.value.filter { it.isCompleted }
            val pending = allTasks.value.filter { !it.isCompleted }
            val result = GeminiService.generateDailyAiOverview(
                apiKey = apiKey,
                completedTasks = completed,
                pendingTasks = pending,
                studyRecords = studyRecords.value,
                preferenceFocus = _overviewPreferenceFocus.value
            )
            _isSyncingDailyOverview.value = false
            result.onSuccess { overview ->
                _dailyAiOverview.value = overview
                saveDailyOverview(overview)
            }
        }
    }

    // ==========================================
    // Backup & Restore
    // ==========================================
    fun exportBackupJson(): String {
        return BackupManager.createBackupJson(
            userProfile = userProfile.value,
            tasks = allTasks.value,
            studyRecords = studyRecords.value,
            timetableSlots = timetableSlots.value,
            isCalendarSync = isCalendarSync.value,
            isAiOptimization = isAiOptimization.value,
            isNotifications = isNotifications.value,
            isStudyTimeBeta = isStudyTimeBetaEnabled.value
        )
    }

    fun restoreBackupJson(jsonString: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val parseResult = BackupManager.parseBackupJson(jsonString)
            parseResult.onSuccess { parsed ->
                repository.replaceAllTasks(parsed.tasks)
                repository.replaceAllStudyRecords(parsed.studyRecords)
                updateProfile(parsed.userProfile.name, parsed.userProfile.age, parsed.userProfile.photoUri)
                setCalendarSync(parsed.isCalendarSync)
                setAiOptimization(parsed.isAiOptimization)
                setNotifications(parsed.isNotifications)
                setStudyTimeBetaEnabled(parsed.isStudyTimeBeta)
                if (parsed.timetableSlots.isNotEmpty()) {
                    _timetableSlots.value = parsed.timetableSlots
                }
                onResult(true, "Data successfully restored!")
            }.onFailure { err ->
                onResult(false, err.message ?: "Failed to parse backup JSON")
            }
        }
    }
}
