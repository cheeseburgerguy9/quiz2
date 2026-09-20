package com.aistudio.caitlindaily.util

import android.content.Context
import android.net.Uri
import com.aistudio.caitlindaily.data.model.StudyRecord
import com.aistudio.caitlindaily.data.model.TaskEntity
import com.aistudio.caitlindaily.data.model.TimetableSlot
import com.aistudio.caitlindaily.data.model.UserProfile
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter

/** Portable, user-triggered backup. Gemini credentials are intentionally never serialized. */
data class ParsedBackup(
    val userProfile: UserProfile,
    val photoBase64: String?,
    val tasks: List<TaskEntity>,
    val studyRecords: List<StudyRecord>,
    val timetableSlots: List<TimetableSlot>,
    val isCalendarSync: Boolean,
    val isAiOptimization: Boolean,
    val isNotifications: Boolean,
    val isStudyTimeBeta: Boolean,
    val appCreatedAt: Long,
    val themeMode: String,
    val isDynamicColor: Boolean
)

object BackupManager {
    private const val CURRENT_VERSION = 2

    fun createBackupJson(
        userProfile: UserProfile,
        tasks: List<TaskEntity>,
        studyRecords: List<StudyRecord>,
        timetableSlots: List<TimetableSlot>,
        isCalendarSync: Boolean,
        isAiOptimization: Boolean,
        isNotifications: Boolean,
        isStudyTimeBeta: Boolean,
        appCreatedAt: Long,
        themeMode: String,
        isDynamicColor: Boolean,
        photoBase64: String? = null
    ): String {
        val root = JSONObject().apply {
            put("format", "caitlin_daily_backup")
            put("version", CURRENT_VERSION)
            put("exportedAt", System.currentTimeMillis())
            put("appCreatedAt", appCreatedAt)
            put("profile", JSONObject().apply {
                put("name", userProfile.name)
                put("age", userProfile.age)
                put("photoUri", userProfile.photoUri ?: "")
                if (!photoBase64.isNullOrBlank()) put("photoBase64", photoBase64)
                put("isCreated", userProfile.isCreated)
            })
            put("settings", JSONObject().apply {
                put("isCalendarSync", isCalendarSync)
                put("isAiOptimization", isAiOptimization)
                put("isNotifications", isNotifications)
                put("isStudyTimeBeta", isStudyTimeBeta)
                put("themeMode", themeMode)
                put("isDynamicColor", isDynamicColor)
            })
            // API keys are deliberately absent.
        }

        root.put("tasks", JSONArray().apply {
            tasks.forEach { task -> put(JSONObject().apply {
                put("id", task.id)
                put("title", task.title)
                put("description", task.description)
                put("category", task.category)
                put("priority", task.priority)
                put("time", task.time)
                put("isCompleted", task.isCompleted)
                put("isAiVerified", task.isAiVerified)
                put("aiScore", task.aiScore)
                put("aiFeedback", task.aiFeedback)
                put("createdAt", task.createdAt)
            }) }
        })
        root.put("studyRecords", JSONArray().apply {
            studyRecords.forEach { record -> put(JSONObject().apply {
                put("id", record.id)
                put("appName", record.appName)
                put("minutes", record.minutes)
                put("timeFormatted", record.timeFormatted)
                put("notes", record.notes)
                put("timestamp", record.timestamp)
            }) }
        })
        root.put("timetableSlots", JSONArray().apply {
            timetableSlots.forEach { slot -> put(JSONObject().apply {
                put("timeLabel", slot.timeLabel)
                put("taskTitle", slot.taskTitle ?: "")
                put("category", slot.category ?: "")
                put("priority", slot.priority ?: "")
                put("isAiOptimized", slot.isAiOptimized)
            }) }
        })
        return root.toString(2)
    }

    fun parseBackupJson(jsonString: String): Result<ParsedBackup> = runCatching {
        val root = JSONObject(jsonString)
        require(root.optString("format") == "caitlin_daily_backup") { "Not a Caitlin Daily backup" }
        val version = root.optInt("version", 1)
        require(version in 1..CURRENT_VERSION) { "Unsupported backup version: $version" }

        val profile = root.optJSONObject("profile")
        val profileName = profile?.optString("name", "")?.trim().orEmpty()
        val profileAge = profile?.optInt("age", 0)?.coerceIn(0, 120) ?: 0
        val profileCreated = profile?.optBoolean("isCreated", false) ?: false
        if (profileCreated) {
            require(profileName.isNotBlank()) { "Backup account name is missing" }
            require(profileAge in 1..120) { "Backup account age is invalid" }
        }
        val userProfile = UserProfile(
            name = profileName, age = profileAge,
            photoUri = profile?.optString("photoUri")?.takeIf { it.isNotBlank() },
            isCreated = profileCreated
        )
        val photoBase64 = profile?.optString("photoBase64")?.takeIf { it.isNotBlank() }
        val settings = root.optJSONObject("settings")
        val tasks = mutableListOf<TaskEntity>()
        root.optJSONArray("tasks")?.let { array ->
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val title = obj.optString("title").trim()
                if (title.isBlank()) continue
                tasks += TaskEntity(
                    id = obj.optLong("id", 0), title = title,
                    description = obj.optString("description", ""),
                    category = obj.optString("category", "Personal"),
                    priority = obj.optString("priority", "Medium"),
                    time = obj.optString("time", "Today"),
                    isCompleted = obj.optBoolean("isCompleted", false),
                    isAiVerified = obj.optBoolean("isAiVerified", false),
                    aiScore = obj.optInt("aiScore", 0),
                    aiFeedback = obj.optString("aiFeedback", ""),
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                )
            }
        }
        val studyRecords = mutableListOf<StudyRecord>()
        root.optJSONArray("studyRecords")?.let { array ->
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val appName = obj.optString("appName").trim()
                val minutes = obj.optInt("minutes", 0)
                if (appName.isBlank() || minutes <= 0) continue
                studyRecords += StudyRecord(
                    id = obj.optLong("id", 0), appName = appName, minutes = minutes,
                    timeFormatted = obj.optString("timeFormatted", "${minutes}m"),
                    notes = obj.optString("notes", ""), timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                )
            }
        }
        val slots = mutableListOf<TimetableSlot>()
        root.optJSONArray("timetableSlots")?.let { array ->
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                slots += TimetableSlot(
                    timeLabel = obj.optString("timeLabel", "Scheduled"),
                    taskTitle = obj.optString("taskTitle").takeIf { it.isNotBlank() },
                    category = obj.optString("category").takeIf { it.isNotBlank() },
                    priority = obj.optString("priority").takeIf { it.isNotBlank() },
                    isAiOptimized = obj.optBoolean("isAiOptimized", false)
                )
            }
        }
        ParsedBackup(
            userProfile = userProfile, photoBase64 = photoBase64, tasks = tasks, studyRecords = studyRecords, timetableSlots = slots,
            isCalendarSync = settings?.optBoolean("isCalendarSync", true) ?: true,
            isAiOptimization = settings?.optBoolean("isAiOptimization", true) ?: true,
            isNotifications = settings?.optBoolean("isNotifications", true) ?: true,
            isStudyTimeBeta = settings?.optBoolean("isStudyTimeBeta", false) ?: false,
            appCreatedAt = root.optLong("appCreatedAt", root.optLong("timestamp", System.currentTimeMillis())),
            themeMode = settings?.optString("themeMode", "auto") ?: "auto",
            isDynamicColor = settings?.optBoolean("isDynamicColor", true) ?: true
        )
    }

    fun writeToUri(context: Context, uri: Uri, content: String): Boolean = try {
        context.contentResolver.openOutputStream(uri)?.use { os ->
            OutputStreamWriter(os).use { it.write(content) }
        } ?: return false
        true
    } catch (_: Exception) { false }

    fun readFromUri(context: Context, uri: Uri): String? = try {
        context.contentResolver.openInputStream(uri)?.use { input ->
            BufferedReader(InputStreamReader(input)).use { it.readText() }
        }
    } catch (_: Exception) { null }
}
