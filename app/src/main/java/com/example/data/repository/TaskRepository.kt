package com.example.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Base64
import com.example.ai.GeminiService
import com.example.data.db.AppDatabase
import com.example.data.model.ProductivityInsight
import com.example.data.model.TaskEntity
import com.example.data.model.TimetableSlot
import com.example.data.model.UserProfile
import com.example.data.model.VerificationResult
import com.example.util.CalendarHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TaskRepository(
    private val context: Context,
    private val database: AppDatabase = AppDatabase.getDatabase(context)
) {
    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences("caitlin_daily_prefs", Context.MODE_PRIVATE)
    private val taskDao = database.taskDao()
    private val timetableDao = database.timetableDao()

    val allTasks: Flow<List<TaskEntity>> = taskDao.getAllTasks()

    private val _isSetupCompleted = MutableStateFlow(prefs.getBoolean("setup_completed", false))
    val isSetupCompleted = _isSetupCompleted.asStateFlow()

    private val _userProfile = MutableStateFlow(loadProfile())
    val userProfile = _userProfile.asStateFlow()

    private val _cachedInsights = MutableStateFlow<ProductivityInsight?>(null)
    val cachedInsights = _cachedInsights.asStateFlow()

    private val _isAiEnabled = MutableStateFlow(prefs.getBoolean("ai_enabled", true))
    val isAiEnabled = _isAiEnabled.asStateFlow()

    private val _themeMode = MutableStateFlow(
        prefs.getString("theme_mode", "SYSTEM") ?: "SYSTEM"
    )
    val themeMode = _themeMode.asStateFlow()

    private val _studyTimeBetaEnabled = MutableStateFlow(
        prefs.getBoolean("study_time_beta_enabled", false)
    )
    val studyTimeBetaEnabled = _studyTimeBetaEnabled.asStateFlow()

    private val _totalStudyMinutes = MutableStateFlow(
        prefs.getInt("total_study_minutes", 0)
    )
    val totalStudyMinutes = _totalStudyMinutes.asStateFlow()

    fun setAiEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("ai_enabled", enabled).apply()
        _isAiEnabled.value = enabled
    }

    fun setThemeMode(mode: String) {
        prefs.edit().putString("theme_mode", mode).apply()
        _themeMode.value = mode
    }

    fun setStudyTimeBetaEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("study_time_beta_enabled", enabled).apply()
        _studyTimeBetaEnabled.value = enabled
    }

    fun addStudyMinutes(minutes: Int) {
        val updated = (_totalStudyMinutes.value + minutes).coerceAtLeast(0)
        prefs.edit().putInt("total_study_minutes", updated).apply()
        _totalStudyMinutes.value = updated
    }

    fun completeSetup(
        name: String,
        age: Int?,
        sleepSchedule: String,
        mealSchedule: String,
        extraConstraints: String
    ) {
        prefs.edit()
            .putBoolean("setup_completed", true)
            .putString("user_name", name.trim().ifBlank { "Caitlin" })
            .apply {
                if (age != null) putInt("user_age", age) else remove("user_age")
                putString("sleep_schedule", sleepSchedule)
                putString("meal_schedule", mealSchedule)
                putString("extra_constraints", extraConstraints)
            }
            .apply()

        _isSetupCompleted.value = true
        _userProfile.value = loadProfile()
    }

    fun updateProfile(name: String, age: Int?, photoSource: Uri? = null) {
        val cleanName = name.trim().ifBlank { "Caitlin" }
        prefs.edit()
            .putString("user_name", cleanName)
            .apply {
                if (age != null) putInt("user_age", age) else remove("user_age")
            }
            .apply()

        if (photoSource != null) {
            appContext.contentResolver.openInputStream(photoSource)?.use { input ->
                File(appContext.filesDir, PROFILE_PHOTO).outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }
        _userProfile.value = loadProfile()
    }

    fun getProfilePhotoFile(): File? {
        val file = File(appContext.filesDir, PROFILE_PHOTO)
        return file.takeIf { it.exists() }
    }

    fun getSavedRoutine(): Triple<String, String, String> {
        val sleep = prefs.getString("sleep_schedule", "11:00 PM - 07:00 AM (8h sleep)")
            ?: "11:00 PM - 07:00 AM"
        val meals = prefs.getString(
            "meal_schedule",
            "Breakfast: 8:00 AM, Lunch: 1:00 PM, Dinner: 8:00 PM"
        ) ?: "Breakfast: 8:00 AM"
        val extra = prefs.getString(
            "extra_constraints",
            "Daily workout at 5:30 PM (45 min), 10 min break every 90 min"
        ) ?: ""
        return Triple(sleep, meals, extra)
    }

    fun getTimetableForDate(date: String = getTodayDate()): Flow<List<TimetableSlot>> =
        timetableDao.getTimetableForDate(date)

    suspend fun addTask(
        title: String,
        description: String,
        category: String,
        priority: String,
        estimatedMinutes: Int,
        scheduledTime: String
    ): Long = taskDao.insertTask(
        TaskEntity(
            title = title,
            description = description,
            category = category,
            priority = priority,
            estimatedMinutes = estimatedMinutes,
            scheduledTime = scheduledTime,
            createdDate = getTodayDate()
        )
    )

    suspend fun addTasksBatch(tasks: List<TaskEntity>) = taskDao.insertTasks(tasks)

    suspend fun toggleTaskCompletion(task: TaskEntity) {
        val nextCompleted = !task.isCompleted
        taskDao.updateCompletionStatus(
            task.id,
            nextCompleted,
            if (nextCompleted) System.currentTimeMillis() else null
        )
    }

    suspend fun deleteTask(task: TaskEntity) = taskDao.deleteTask(task)
    suspend fun updateTask(task: TaskEntity) = taskDao.updateTask(task)

    fun addTaskToCalendar(task: TaskEntity): Boolean =
        CalendarHelper.addTaskToCalendar(appContext, task)

    fun addTimetableSlotToCalendar(slot: TimetableSlot): Boolean =
        CalendarHelper.addTimetableSlotToCalendar(appContext, slot)

    suspend fun markTaskCalendarSynced(taskId: Long, synced: Boolean) =
        taskDao.updateCalendarSync(taskId, synced)

    suspend fun generateTimetableWithGemini(
        tasks: List<TaskEntity>,
        sleepSchedule: String,
        mealSchedule: String,
        extraConstraints: String,
        date: String = getTodayDate()
    ): Result<List<TimetableSlot>> {
        val profile = _userProfile.value
        val result = GeminiService.generateTimetable(
            tasks, sleepSchedule, mealSchedule, extraConstraints,
            profile.name, date
        )
        if (result.isSuccess) {
            val slots = result.getOrNull().orEmpty()
            if (slots.isNotEmpty()) {
                timetableDao.clearSlotsForDate(date)
                timetableDao.insertSlots(slots)
            }
        }
        return result
    }

    suspend fun verifyTaskWithScreenshot(
        task: TaskEntity,
        bitmap: Bitmap,
        screenshotUriString: String?
    ): Result<VerificationResult> {
        val profile = _userProfile.value
        val result = GeminiService.verifyTaskCompletion(
            task.title, task.description, task.category, bitmap, profile.name
        )
        if (result.isSuccess) {
            val verification = result.getOrNull()
            if (verification != null && verification.verified) {
                taskDao.updateVerification(
                    task.id, true, verification.confidence,
                    verification.explanation, screenshotUriString
                )
                taskDao.updateCompletionStatus(
                    task.id, true, System.currentTimeMillis()
                )
            }
        }
        return result
    }

    suspend fun analyzeScreenTime(
        bitmap: Bitmap,
        targetAppName: String
    ) = GeminiService.analyzeScreenTimeForApp(bitmap, targetAppName, _userProfile.value.name)

    suspend fun fetchProductivityInsights(tasks: List<TaskEntity>): Result<ProductivityInsight> {
        val result = GeminiService.generateProductivityInsights(tasks, _userProfile.value.name)
        if (result.isSuccess) _cachedInsights.value = result.getOrNull()
        return result
    }

    suspend fun pingGemini(): Result<Pair<Long, String>> = GeminiService.pingGemini()

    fun resetSetup() {
        prefs.edit().putBoolean("setup_completed", false).apply()
        _isSetupCompleted.value = false
        _userProfile.value = loadProfile()
    }

    fun getTodayDate(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    /**
     * A portable, offline backup. It contains the profile, app date, preferences,
     * all tasks, timetable slots, and the optional profile photo.
     */
    suspend fun createBackupJson(): String {
        val root = JSONObject()
            .put("format", "caitlin_daily_backup")
            .put("version", 1)
            .put("appDate", getTodayDate())
            .put("exportedAt", System.currentTimeMillis())

        val profile = _userProfile.value
        root.put(
            "profile",
            JSONObject()
                .put("name", profile.name)
                .put("age", profile.age ?: JSONObject.NULL)
                .put("photoBase64", encodeProfilePhoto())
        )

        val settings = JSONObject()
            .put("setupCompleted", prefs.getBoolean("setup_completed", false))
            .put("aiEnabled", prefs.getBoolean("ai_enabled", true))
            .put("themeMode", prefs.getString("theme_mode", "SYSTEM"))
            .put("studyTimeBetaEnabled", prefs.getBoolean("study_time_beta_enabled", false))
            .put("totalStudyMinutes", prefs.getInt("total_study_minutes", 0))
            .put("sleepSchedule", prefs.getString("sleep_schedule", "") ?: "")
            .put("mealSchedule", prefs.getString("meal_schedule", "") ?: "")
            .put("extraConstraints", prefs.getString("extra_constraints", "") ?: "")
        root.put("settings", settings)

        root.put("tasks", JSONArray().apply {
            taskDao.getAllTasksSnapshot().forEach { put(taskToJson(it)) }
        })
        root.put("timetable", JSONArray().apply {
            timetableDao.getAllSlotsSnapshot().forEach { put(slotToJson(it)) }
        })

        return root.toString(2)
    }

    suspend fun restoreBackupJson(json: String): Result<Unit> {
        return try {
            val root = JSONObject(json)
            require(root.optString("format") == "caitlin_daily_backup") {
                "This file is not a Caitlin Daily backup."
            }

            val profile = root.optJSONObject("profile")
            val settings = root.optJSONObject("settings")

            prefs.edit()
                .putBoolean("setup_completed", settings?.optBoolean("setupCompleted", true) ?: true)
                .putString("user_name", profile?.optString("name", "Caitlin") ?: "Caitlin")
                .apply {
                    if (profile != null && !profile.isNull("age")) {
                        putInt("user_age", profile.optInt("age"))
                    } else remove("user_age")
                    putBoolean("ai_enabled", settings?.optBoolean("aiEnabled", true) ?: true)
                    putString("theme_mode", settings?.optString("themeMode", "SYSTEM") ?: "SYSTEM")
                    putBoolean(
                        "study_time_beta_enabled",
                        settings?.optBoolean("studyTimeBetaEnabled", false) ?: false
                    )
                    putInt("total_study_minutes", settings?.optInt("totalStudyMinutes", 0) ?: 0)
                    putString("sleep_schedule", settings?.optString("sleepSchedule", "") ?: "")
                    putString("meal_schedule", settings?.optString("mealSchedule", "") ?: "")
                    putString("extra_constraints", settings?.optString("extraConstraints", "") ?: "")
                }
                .apply()

            val photo = profile?.optString("photoBase64", "").orEmpty()
            val photoFile = File(appContext.filesDir, PROFILE_PHOTO)
            if (photo.isNotBlank()) {
                photoFile.writeBytes(Base64.decode(photo, Base64.DEFAULT))
            } else {
                photoFile.delete()
            }

            taskDao.clearAll()
            val tasks = mutableListOf<TaskEntity>()
            val taskArray = root.optJSONArray("tasks") ?: JSONArray()
            for (i in 0 until taskArray.length()) tasks += taskFromJson(taskArray.getJSONObject(i))
            if (tasks.isNotEmpty()) taskDao.insertTasks(tasks)

            timetableDao.clearAll()
            val slots = mutableListOf<TimetableSlot>()
            val slotArray = root.optJSONArray("timetable") ?: JSONArray()
            for (i in 0 until slotArray.length()) slots += slotFromJson(slotArray.getJSONObject(i))
            if (slots.isNotEmpty()) timetableDao.insertSlots(slots)

            _isSetupCompleted.value = prefs.getBoolean("setup_completed", true)
            _userProfile.value = loadProfile()
            _isAiEnabled.value = prefs.getBoolean("ai_enabled", true)
            _themeMode.value = prefs.getString("theme_mode", "SYSTEM") ?: "SYSTEM"
            _studyTimeBetaEnabled.value = prefs.getBoolean("study_time_beta_enabled", false)
            _totalStudyMinutes.value = prefs.getInt("total_study_minutes", 0)
            _cachedInsights.value = null
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun loadProfile(): UserProfile {
        val photo = getProfilePhotoFile()?.absolutePath
        return UserProfile(
            name = prefs.getString("user_name", "Caitlin") ?: "Caitlin",
            age = if (prefs.contains("user_age")) prefs.getInt("user_age", 0).takeIf { it > 0 } else null,
            photoPath = photo,
            setupCompleted = prefs.getBoolean("setup_completed", false)
        )
    }

    private fun encodeProfilePhoto(): String {
        val file = getProfilePhotoFile() ?: return ""
        return Base64.encodeToString(file.readBytes(), Base64.NO_WRAP)
    }

    private fun taskToJson(t: TaskEntity) = JSONObject()
        .put("id", t.id)
        .put("title", t.title)
        .put("description", t.description)
        .put("category", t.category)
        .put("priority", t.priority)
        .put("estimatedMinutes", t.estimatedMinutes)
        .put("scheduledTime", t.scheduledTime)
        .put("isCompleted", t.isCompleted)
        .put("isVerified", t.isVerified)
        .put("verificationNotes", t.verificationNotes ?: JSONObject.NULL)
        .put("verificationScore", t.verificationScore ?: JSONObject.NULL)
        .put("screenshotUri", t.screenshotUri ?: JSONObject.NULL)
        .put("calendarSynced", t.calendarSynced)
        .put("createdDate", t.createdDate)
        .put("completedAt", t.completedAt ?: JSONObject.NULL)

    private fun taskFromJson(o: JSONObject) = TaskEntity(
        id = o.optLong("id", 0),
        title = o.optString("title"),
        description = o.optString("description"),
        category = o.optString("category", "Work"),
        priority = o.optString("priority", "MEDIUM"),
        estimatedMinutes = o.optInt("estimatedMinutes", 30),
        scheduledTime = o.optString("scheduledTime"),
        isCompleted = o.optBoolean("isCompleted", false),
        isVerified = o.optBoolean("isVerified", false),
        verificationNotes = if (o.isNull("verificationNotes")) null else o.optString("verificationNotes"),
        verificationScore = if (o.isNull("verificationScore")) null else o.optInt("verificationScore"),
        screenshotUri = if (o.isNull("screenshotUri")) null else o.optString("screenshotUri"),
        calendarSynced = o.optBoolean("calendarSynced", false),
        createdDate = o.optString("createdDate", getTodayDate()),
        completedAt = if (o.isNull("completedAt")) null else o.optLong("completedAt")
    )

    private fun slotToJson(s: TimetableSlot) = JSONObject()
        .put("id", s.id)
        .put("startTime", s.startTime)
        .put("endTime", s.endTime)
        .put("title", s.title)
        .put("type", s.type)
        .put("notes", s.notes)
        .put("date", s.date)
        .put("taskId", s.taskId ?: JSONObject.NULL)
        .put("calendarSynced", s.calendarSynced)

    private fun slotFromJson(o: JSONObject) = TimetableSlot(
        id = o.optLong("id", 0),
        startTime = o.optString("startTime", "09:00 AM"),
        endTime = o.optString("endTime", "10:00 AM"),
        title = o.optString("title"),
        type = o.optString("type", "WORK"),
        notes = o.optString("notes"),
        date = o.optString("date", getTodayDate()),
        taskId = if (o.isNull("taskId")) null else o.optLong("taskId"),
        calendarSynced = o.optBoolean("calendarSynced", false)
    )

    companion object {
        private const val PROFILE_PHOTO = "profile_photo.jpg"
    }
}
