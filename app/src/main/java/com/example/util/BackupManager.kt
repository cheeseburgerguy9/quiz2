package com.example.util

import android.content.Context
import android.net.Uri
import com.example.data.model.StudyRecord
import com.example.data.model.TaskEntity
import com.example.data.model.TimetableSlot
import com.example.data.model.UserProfile
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter

data class ParsedBackup(
    val userProfile: UserProfile,
    val tasks: List<TaskEntity>,
    val studyRecords: List<StudyRecord>,
    val timetableSlots: List<TimetableSlot>,
    val isCalendarSync: Boolean,
    val isAiOptimization: Boolean,
    val isNotifications: Boolean,
    val isStudyTimeBeta: Boolean
)

object BackupManager {

    fun createBackupJson(
        userProfile: UserProfile,
        tasks: List<TaskEntity>,
        studyRecords: List<StudyRecord>,
        timetableSlots: List<TimetableSlot>,
        isCalendarSync: Boolean,
        isAiOptimization: Boolean,
        isNotifications: Boolean,
        isStudyTimeBeta: Boolean
    ): String {
        val root = JSONObject()
        root.put("version", 1)
        root.put("timestamp", System.currentTimeMillis())

        // Account Profile
        val profileJson = JSONObject().apply {
            put("name", userProfile.name)
            put("age", userProfile.age)
            put("photoUri", userProfile.photoUri ?: "")
            put("isCreated", userProfile.isCreated)
        }
        root.put("profile", profileJson)

        // Settings
        val settingsJson = JSONObject().apply {
            put("isCalendarSync", isCalendarSync)
            put("isAiOptimization", isAiOptimization)
            put("isNotifications", isNotifications)
            put("isStudyTimeBeta", isStudyTimeBeta)
        }
        root.put("settings", settingsJson)

        // Tasks
        val tasksArray = JSONArray()
        tasks.forEach { task ->
            val obj = JSONObject().apply {
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
            }
            tasksArray.put(obj)
        }
        root.put("tasks", tasksArray)

        // Study Records
        val studyArray = JSONArray()
        studyRecords.forEach { record ->
            val obj = JSONObject().apply {
                put("id", record.id)
                put("appName", record.appName)
                put("minutes", record.minutes)
                put("timeFormatted", record.timeFormatted)
                put("notes", record.notes)
                put("timestamp", record.timestamp)
            }
            studyArray.put(obj)
        }
        root.put("studyRecords", studyArray)

        // Timetable Slots
        val slotsArray = JSONArray()
        timetableSlots.forEach { slot ->
            val obj = JSONObject().apply {
                put("timeLabel", slot.timeLabel)
                put("taskTitle", slot.taskTitle ?: "")
                put("category", slot.category ?: "")
                put("priority", slot.priority ?: "")
                put("isAiOptimized", slot.isAiOptimized)
            }
            slotsArray.put(obj)
        }
        root.put("timetableSlots", slotsArray)

        return root.toString(2)
    }

    fun parseBackupJson(jsonString: String): Result<ParsedBackup> {
        return try {
            val root = JSONObject(jsonString)

            // Profile
            val profileJson = root.optJSONObject("profile")
            val userProfile = if (profileJson != null) {
                UserProfile(
                    name = profileJson.optString("name", "Aswin"),
                    age = profileJson.optInt("age", 22),
                    photoUri = profileJson.optString("photoUri").takeIf { it.isNotBlank() },
                    isCreated = profileJson.optBoolean("isCreated", true)
                )
            } else {
                UserProfile()
            }

            // Settings
            val settingsJson = root.optJSONObject("settings")
            val isCalendarSync = settingsJson?.optBoolean("isCalendarSync", true) ?: true
            val isAiOptimization = settingsJson?.optBoolean("isAiOptimization", true) ?: true
            val isNotifications = settingsJson?.optBoolean("isNotifications", true) ?: true
            val isStudyTimeBeta = settingsJson?.optBoolean("isStudyTimeBeta", false) ?: false

            // Tasks
            val tasksArray = root.optJSONArray("tasks")
            val tasksList = mutableListOf<TaskEntity>()
            if (tasksArray != null) {
                for (i in 0 until tasksArray.length()) {
                    val obj = tasksArray.getJSONObject(i)
                    tasksList.add(
                        TaskEntity(
                            id = obj.optLong("id", 0),
                            title = obj.optString("title", ""),
                            description = obj.optString("description", ""),
                            category = obj.optString("category", "Work"),
                            priority = obj.optString("priority", "Medium"),
                            time = obj.optString("time", "Today"),
                            isCompleted = obj.optBoolean("isCompleted", false),
                            isAiVerified = obj.optBoolean("isAiVerified", false),
                            aiScore = obj.optInt("aiScore", 0),
                            aiFeedback = obj.optString("aiFeedback", ""),
                            createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }
            }

            // Study Records
            val studyArray = root.optJSONArray("studyRecords")
            val studyList = mutableListOf<StudyRecord>()
            if (studyArray != null) {
                for (i in 0 until studyArray.length()) {
                    val obj = studyArray.getJSONObject(i)
                    studyList.add(
                        StudyRecord(
                            id = obj.optLong("id", 0),
                            appName = obj.optString("appName", ""),
                            minutes = obj.optInt("minutes", 0),
                            timeFormatted = obj.optString("timeFormatted", ""),
                            notes = obj.optString("notes", ""),
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                        )
                    )
                }
            }

            // Timetable Slots
            val slotsArray = root.optJSONArray("timetableSlots")
            val slotsList = mutableListOf<TimetableSlot>()
            if (slotsArray != null) {
                for (i in 0 until slotsArray.length()) {
                    val obj = slotsArray.getJSONObject(i)
                    slotsList.add(
                        TimetableSlot(
                            timeLabel = obj.optString("timeLabel", ""),
                            taskTitle = obj.optString("taskTitle").takeIf { it.isNotBlank() },
                            category = obj.optString("category").takeIf { it.isNotBlank() },
                            priority = obj.optString("priority").takeIf { it.isNotBlank() },
                            isAiOptimized = obj.optBoolean("isAiOptimized", false)
                        )
                    )
                }
            }

            Result.success(
                ParsedBackup(
                    userProfile = userProfile,
                    tasks = tasksList,
                    studyRecords = studyList,
                    timetableSlots = slotsList,
                    isCalendarSync = isCalendarSync,
                    isAiOptimization = isAiOptimization,
                    isNotifications = isNotifications,
                    isStudyTimeBeta = isStudyTimeBeta
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun writeToUri(context: Context, uri: Uri, content: String): Boolean {
        return try {
            context.contentResolver.openOutputStream(uri)?.use { os ->
                OutputStreamWriter(os).use { writer ->
                    writer.write(content)
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun readFromUri(context: Context, uri: Uri): String? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    reader.readText()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
