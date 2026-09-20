package com.aswinas.caitlin.planner.data.api

import android.graphics.Bitmap
import android.util.Base64
import com.aswinas.caitlin.planner.data.model.StudyRecord
import com.aswinas.caitlin.planner.data.model.TaskEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

data class TaskVerificationResult(
    val isVerified: Boolean,
    val score: Int,
    val feedback: String
)

data class StudyExtractionResult(
    val appName: String,
    val minutes: Int,
    val timeFormatted: String,
    val summary: String
)

data class InsightsResult(
    val progressSummary: String,
    val velocityStatus: String,
    val suggestions: List<String>
)

object GeminiService {
    const val MODEL_NAME = "gemini-3.8-flash"
    private const val FALLBACK_MODEL = "gemini-3.7-flash"
    private const val BASE_ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/models/"

    private val client = OkHttpClient.Builder()
        .connectTimeout(45, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(45, TimeUnit.SECONDS)
        .build()

    private fun executeGenerateContent(apiKey: String, jsonBody: JSONObject): okhttp3.Response {
        val primaryUrl = "$BASE_ENDPOINT$MODEL_NAME:generateContent?key=${apiKey.trim()}"
        val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())
        val primaryRequest = Request.Builder()
            .url(primaryUrl)
            .post(requestBody)
            .build()
        val response = client.newCall(primaryRequest).execute()
        if (response.code == 404) {
            response.close()
            val fallbackUrl = "$BASE_ENDPOINT$FALLBACK_MODEL:generateContent?key=${apiKey.trim()}"
            val fallbackRequest = Request.Builder()
                .url(fallbackUrl)
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()
            return client.newCall(fallbackRequest).execute()
        }
        return response
    }

    /**
     * Converts a Bitmap to a JPEG Base64 encoded string, scaled down to reasonable resolution.
     */
    fun bitmapToBase64(bitmap: Bitmap, maxDimension: Int = 1024): String {
        var scaled = bitmap
        val width = bitmap.width
        val height = bitmap.height
        if (width > maxDimension || height > maxDimension) {
            val ratio = width.toFloat() / height.toFloat()
            val newWidth = if (ratio > 1) maxDimension else (maxDimension * ratio).toInt()
            val newHeight = if (ratio > 1) (maxDimension / ratio).toInt() else maxDimension
            scaled = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
        }
        val outputStream = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }

    /**
     * Verifies if the provided user Gemini API key is active and functional.
     */
    suspend fun verifyApiKey(apiKey: String): Result<String> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("API Key cannot be blank"))
        }
        try {
            val jsonBody = JSONObject().apply {
                val contents = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val parts = JSONArray().apply {
                            put(JSONObject().put("text", "Respond with 'API_KEY_VALID'"))
                        }
                        put("parts", parts)
                    }
                    put(contentObj)
                }
                put("contents", contents)
            }

            val response = executeGenerateContent(apiKey, jsonBody)
            val responseBody = response.body?.string() ?: ""

            if (response.isSuccessful) {
                Result.success("API key verified successfully!")
            } else {
                val errorMsg = try {
                    val json = JSONObject(responseBody)
                    json.optJSONObject("error")?.optString("message") ?: "HTTP ${response.code}"
                } catch (e: Exception) {
                    "HTTP ${response.code}: ${response.message}"
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Verifies task completion by analyzing submitted screenshot and proof notes.
     */
    suspend fun verifyTaskCompletion(
        apiKey: String,
        taskTitle: String,
        proofNotes: String,
        imageBase64: String?
    ): Result<TaskVerificationResult> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext Result.failure(IllegalStateException("Gemini API key is required"))
        }
        try {
            val partsArray = JSONArray()
            val prompt = """
                You are an objective AI Task Verification Auditor in Caitlin Daily.
                The user is claiming completion for the task: "$taskTitle".
                User's notes/description: "$proofNotes".
                ${if (imageBase64 != null) "A screenshot evidence has been provided as proof." else "No screenshot was attached."}

                Carefully inspect the screenshot and notes to verify whether the task was genuinely accomplished.
                Return ONLY a valid JSON object matching this schema:
                {
                  "verified": true,
                  "score": 92,
                  "feedback": "Concise 1-2 sentence explanation of verification findings."
                }
            """.trimIndent()

            partsArray.put(JSONObject().put("text", prompt))

            if (imageBase64 != null) {
                val inlineData = JSONObject().apply {
                    put("mimeType", "image/jpeg")
                    put("data", imageBase64)
                }
                partsArray.put(JSONObject().put("inlineData", inlineData))
            }

            val jsonBody = JSONObject().apply {
                val contents = JSONArray().apply {
                    put(JSONObject().put("parts", partsArray))
                }
                put("contents", contents)
                put("generationConfig", JSONObject().apply {
                    put("responseMimeType", "application/json")
                })
            }

            val response = executeGenerateContent(apiKey, jsonBody)
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                val err = parseErrorMessage(responseBody, response.code)
                return@withContext Result.failure(Exception(err))
            }

            val root = JSONObject(responseBody)
            val candidates = root.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val text = firstCandidate?.optJSONObject("content")
                ?.optJSONArray("parts")?.optJSONObject(0)?.optString("text") ?: "{}"

            val parsedJson = JSONObject(cleanJson(text))
            val isVerified = parsedJson.optBoolean("verified", true)
            val score = parsedJson.optInt("score", if (isVerified) 90 else 40)
            val feedback = parsedJson.optString("feedback", "Verified based on evidence submitted.")

            Result.success(TaskVerificationResult(isVerified, score, feedback))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Extracts study time from a screenshot mentioning an app name (Beta feature).
     */
    suspend fun extractStudyTime(
        apiKey: String,
        appName: String,
        imageBase64: String
    ): Result<StudyExtractionResult> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext Result.failure(IllegalStateException("Gemini API key is required"))
        }
        try {
            val prompt = """
                Analyze this mobile device screenshot (e.g. Screen Time, Forest, Anki, Duolingo, Notion, YouTube, timer, or study app).
                Target application to inspect: "$appName".
                Determine the duration or study time recorded in the screenshot for "$appName" (or total focus time shown).
                Calculate the total time in integer minutes.

                Return ONLY a valid JSON object:
                {
                  "appName": "$appName",
                  "minutes": 45,
                  "timeFormatted": "45m",
                  "summary": "Detected 45m active session in $appName"
                }
            """.trimIndent()

            val partsArray = JSONArray().apply {
                put(JSONObject().put("text", prompt))
                put(JSONObject().put("inlineData", JSONObject().apply {
                    put("mimeType", "image/jpeg")
                    put("data", imageBase64)
                }))
            }

            val jsonBody = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().put("parts", partsArray))
                })
                put("generationConfig", JSONObject().apply {
                    put("responseMimeType", "application/json")
                })
            }

            val response = executeGenerateContent(apiKey, jsonBody)
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                val err = parseErrorMessage(responseBody, response.code)
                return@withContext Result.failure(Exception(err))
            }

            val root = JSONObject(responseBody)
            val text = root.optJSONArray("candidates")?.optJSONObject(0)
                ?.optJSONObject("content")?.optJSONArray("parts")?.optJSONObject(0)
                ?.optString("text") ?: "{}"

            val parsed = JSONObject(cleanJson(text))
            val detectedApp = parsed.optString("appName", appName)
            val minutes = parsed.optInt("minutes", 30)
            val timeFormatted = parsed.optString("timeFormatted", "${minutes}m")
            val summary = parsed.optString("summary", "Logged $timeFormatted from screenshot.")

            Result.success(StudyExtractionResult(detectedApp, minutes, timeFormatted, summary))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Gemini organizes completed tasks, pending tasks, and study time in Insights with advice.
     */
    suspend fun generateInsightsAndAdvice(
        apiKey: String,
        completedTasks: List<TaskEntity>,
        pendingTasks: List<TaskEntity>,
        studyRecords: List<StudyRecord>
    ): Result<InsightsResult> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext Result.failure(IllegalStateException("Gemini API key is required"))
        }
        try {
            val totalStudyMins = studyRecords.sumOf { it.minutes }
            val completedTitles = completedTasks.joinToString(", ") { "${it.title} [${it.category}]" }
            val pendingTitles = pendingTasks.joinToString(", ") { "${it.title} [${it.category}]" }
            val studySummary = studyRecords.joinToString(", ") { "${it.appName} (${it.timeFormatted})" }

            val prompt = """
                You are Caitlin Daily's AI Productivity Coach.
                Review the user's progress:
                - Completed Tasks (${completedTasks.size}): $completedTitles
                - Pending Tasks (${pendingTasks.size}): $pendingTitles
                - Study Time Logged (${totalStudyMins} mins total): $studySummary

                Synthesize their performance, correlate study time with task execution, and give 3 sharp, motivating, and actionable suggestions to improve their study velocity and focus momentum.

                Return ONLY a JSON object:
                {
                  "progressSummary": "2-3 sentences organizing their achievements and study-task synergy.",
                  "velocityStatus": "Optimal Momentum / High Focus / Needs Rest / Steady Flow",
                  "suggestions": [
                    "Actionable advice 1",
                    "Actionable advice 2",
                    "Actionable advice 3"
                  ]
                }
            """.trimIndent()

            val jsonBody = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().put("parts", JSONArray().apply {
                        put(JSONObject().put("text", prompt))
                    }))
                })
                put("generationConfig", JSONObject().apply {
                    put("responseMimeType", "application/json")
                })
            }

            val response = executeGenerateContent(apiKey, jsonBody)
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                val err = parseErrorMessage(responseBody, response.code)
                return@withContext Result.failure(Exception(err))
            }

            val root = JSONObject(responseBody)
            val text = root.optJSONArray("candidates")?.optJSONObject(0)
                ?.optJSONObject("content")?.optJSONArray("parts")?.optJSONObject(0)
                ?.optString("text") ?: "{}"

            val parsed = JSONObject(cleanJson(text))
            val progressSummary = parsed.optString("progressSummary", "Great execution today across all categories.")
            val velocityStatus = parsed.optString("velocityStatus", "High Momentum")
            val suggestionsArray = parsed.optJSONArray("suggestions")
            val suggestionsList = mutableListOf<String>()
            if (suggestionsArray != null) {
                for (i in 0 until suggestionsArray.length()) {
                    suggestionsList.add(suggestionsArray.getString(i))
                }
            } else {
                suggestionsList.add("Batch high priority deep-work tasks in the morning.")
                suggestionsList.add("Protect breaks between intense study sessions.")
                suggestionsList.add("Use screenshot verification for consistent accountability.")
            }

            Result.success(InsightsResult(progressSummary, velocityStatus, suggestionsList))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Gemini 3.6 Flash Daily AI Overview & Preference Insights.
     * Generates structured progress tips, velocity analysis, and chrono-energy advice.
     */
    suspend fun generateDailyAiOverview(
        apiKey: String,
        completedTasks: List<TaskEntity>,
        pendingTasks: List<TaskEntity>,
        studyRecords: List<StudyRecord>,
        preferenceFocus: String = "Balanced Productivity"
    ): Result<com.aswinas.caitlin.planner.data.model.DailyAiOverview> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext Result.failure(IllegalStateException("Gemini API key is required. Add your own key in Settings."))
        }
        try {
            val totalStudyMins = studyRecords.sumOf { it.minutes }
            val completedCount = completedTasks.size
            val pendingCount = pendingTasks.size
            val completedSummary = completedTasks.take(5).joinToString(", ") { it.title }
            val pendingSummary = pendingTasks.take(5).joinToString(", ") { "${it.title} (${it.priority})" }

            val prompt = """
                You are Caitlin Daily's AI Insights & Overview Coach powered by Gemini 3.6 Flash.
                User Preference Orientation: $preferenceFocus
                Current Daily Execution:
                - Completed Tasks ($completedCount): $completedSummary
                - Pending Tasks ($pendingCount): $pendingSummary
                - Total Study Logged: $totalStudyMins mins

                Analyze the user's progress and formulate an actionable daily overview tailored to their "$preferenceFocus" preference.
                Provide:
                1. focusScore (an integer 0-100 indicating productivity momentum)
                2. velocityGrade ("Optimal Momentum", "Accelerating", "Steady Pace", "Needs Reset")
                3. progressAnalysis (2-3 concise, motivating sentences evaluating their progress)
                4. actionableTips (an array of 3 distinct, high-impact tactical tips)
                5. energyScheduleAdvice (1-2 sentences on optimal timing for their tasks)

                Return ONLY a JSON object:
                {
                  "focusScore": 88,
                  "velocityGrade": "Optimal Momentum",
                  "progressAnalysis": "...",
                  "actionableTips": ["Tip 1", "Tip 2", "Tip 3"],
                  "energyScheduleAdvice": "..."
                }
            """.trimIndent()

            val jsonBody = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().put("parts", JSONArray().apply {
                        put(JSONObject().put("text", prompt))
                    }))
                })
                put("generationConfig", JSONObject().apply {
                    put("responseMimeType", "application/json")
                })
            }

            val response = executeGenerateContent(apiKey, jsonBody)
            val responseBody = response.body?.string() ?: ""
            if (!response.isSuccessful) {
                val err = parseErrorMessage(responseBody, response.code)
                return@withContext Result.failure(Exception(err))
            }

            val root = JSONObject(responseBody)
            val text = root.optJSONArray("candidates")?.optJSONObject(0)
                ?.optJSONObject("content")?.optJSONArray("parts")?.optJSONObject(0)
                ?.optString("text") ?: "{}"

            val parsed = JSONObject(cleanJson(text))
            val score = parsed.optInt("focusScore", 85)
            val grade = parsed.optString("velocityGrade", "Optimal Momentum")
            val analysis = parsed.optString("progressAnalysis", "Solid execution across key targets today.")
            val advice = parsed.optString("energyScheduleAdvice", "Align challenging work to your mid-morning peak window.")
            val tipsArr = parsed.optJSONArray("actionableTips")
            val tips = mutableListOf<String>()
            if (tipsArr != null) {
                for (i in 0 until tipsArr.length()) {
                    tips.add(tipsArr.getString(i))
                }
            } else {
                tips.add("Tackle high cognitive demands early in your cycle.")
                tips.add("Take deliberate 10-minute breaks between blocks.")
                tips.add("Verify evening tasks to maintain streak.")
            }

            val timeStr = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault()).format(java.util.Date())
            Result.success(
                com.aswinas.caitlin.planner.data.model.DailyAiOverview(
                    focusScore = score,
                    velocityGrade = grade,
                    progressAnalysis = analysis,
                    actionableTips = tips,
                    energyScheduleAdvice = advice,
                    lastUpdated = "Today at $timeStr",
                    preferenceFocus = preferenceFocus
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun verifyTaskCompletion(
        apiKey: String,
        task: TaskEntity,
        proofDescription: String,
        screenshot: Bitmap?
    ): Result<TaskVerificationResult> {
        val base64 = screenshot?.let { bitmapToBase64(it) }
        return verifyTaskCompletion(apiKey, task.title, proofDescription, base64)
    }

    suspend fun extractStudyTimeFromScreenshot(
        apiKey: String,
        appName: String,
        screenshot: Bitmap
    ): Result<StudyExtractionResult> {
        val base64 = bitmapToBase64(screenshot)
        return extractStudyTime(apiKey, appName, base64)
    }

    suspend fun optimizeSchedule(
        apiKey: String,
        pendingTasks: List<TaskEntity>
    ): List<com.aswinas.caitlin.planner.data.model.TimetableSlot> {
        return optimizeScheduleWithPresets(apiKey, emptyList(), pendingTasks)
    }

    suspend fun optimizeScheduleWithPresets(
        apiKey: String,
        presetSlots: List<com.aswinas.caitlin.planner.data.model.TimetableSlot>,
        todayTasks: List<TaskEntity>
    ): List<com.aswinas.caitlin.planner.data.model.TimetableSlot> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext if (presetSlots.isNotEmpty()) presetSlots else listOf(
                com.aswinas.caitlin.planner.data.model.TimetableSlot(timeLabel = "08:00 - 09:30", taskTitle = "Morning Deep Focus", category = "Work", isAiOptimized = true),
                com.aswinas.caitlin.planner.data.model.TimetableSlot(timeLabel = "10:00 - 11:30", taskTitle = "Study & Analysis", category = "Study", isAiOptimized = true),
                com.aswinas.caitlin.planner.data.model.TimetableSlot(timeLabel = "12:00 - 13:00", taskTitle = "Midday Reset & Nutrition", category = "Health", isAiOptimized = false, isPresetFixed = true),
                com.aswinas.caitlin.planner.data.model.TimetableSlot(timeLabel = "14:00 - 16:00", taskTitle = "Execution & Projects", category = "Work", isAiOptimized = true),
                com.aswinas.caitlin.planner.data.model.TimetableSlot(timeLabel = "17:00 - 18:30", taskTitle = "Evening Wellness", category = "Health", isAiOptimized = false, isPresetFixed = true),
                com.aswinas.caitlin.planner.data.model.TimetableSlot(timeLabel = "20:00 - 21:30", taskTitle = "Evening Synthesis", category = "Study", isAiOptimized = true)
            )
        }
        try {
            val presetDesc = if (presetSlots.isNotEmpty()) {
                presetSlots.joinToString("\n") { s ->
                    "- [Preset] ${s.timeLabel}: ${s.taskTitle ?: "Free"} (${s.category ?: "Focus"})${if (s.isPresetFixed) " [FIXED/PRESERVE]" else ""}"
                }
            } else {
                "Standard presets: 08:00-09:30 Morning Work, 10:00-11:30 Study, 12:00-13:00 Lunch (Fixed), 14:00-16:00 Work, 17:00-18:30 Exercise, 20:00-21:30 Review"
            }

            val tasksDesc = if (todayTasks.isNotEmpty()) {
                todayTasks.take(8).mapIndexed { idx, t ->
                    "${idx + 1}. ${t.title} [Category: ${t.category}, Priority: ${t.priority}]"
                }.joinToString("\n")
            } else {
                "General deep work and revision for today."
            }

            val prompt = """
                You are Caitlin Daily's AI Schedule Optimizer powered by Gemini 3.6 Flash.
                The user has a PRESET TIMETABLE of habits and baseline blocks:
                $presetDesc

                The user has the following TASKS LISTED FOR TODAY:
                $tasksDesc

                TASK:
                Make intelligent changes in the timetable according to the tasks listed for today, TAKING INTO ACCOUNT the preset timetable:
                1. Preserve fixed preset commitments (meals, exercise, fixed slots).
                2. Intelligently slot today's tasks into appropriate work/study focus windows based on priority and cognitive load (High priority tasks during morning/early afternoon peak focus).
                3. Return chronological timetable slots from morning to night.

                Return ONLY a JSON array with objects matching:
                [
                  {"timeLabel": "08:00 - 09:30", "taskTitle": "Task Name", "category": "Work", "isAiOptimized": true, "isPresetFixed": false, "alertEnabled": true},
                  {"timeLabel": "10:00 - 11:30", "taskTitle": "Task Name", "category": "Study", "isAiOptimized": true, "isPresetFixed": false, "alertEnabled": true},
                  {"timeLabel": "12:00 - 13:00", "taskTitle": "Midday Meal & Reset", "category": "Health", "isAiOptimized": false, "isPresetFixed": true, "alertEnabled": true}
                ]
            """.trimIndent()

            val jsonBody = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().put("parts", JSONArray().apply {
                        put(JSONObject().put("text", prompt))
                    }))
                })
                put("generationConfig", JSONObject().apply {
                    put("responseMimeType", "application/json")
                })
            }

            val response = executeGenerateContent(apiKey, jsonBody)
            val responseBody = response.body?.string() ?: ""
            if (!response.isSuccessful) return@withContext presetSlots

            val root = JSONObject(responseBody)
            val text = root.optJSONArray("candidates")?.optJSONObject(0)
                ?.optJSONObject("content")?.optJSONArray("parts")?.optJSONObject(0)
                ?.optString("text") ?: "[]"

            val array = JSONArray(cleanJson(text))
            val result = mutableListOf<com.aswinas.caitlin.planner.data.model.TimetableSlot>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                result.add(
                    com.aswinas.caitlin.planner.data.model.TimetableSlot(
                        timeLabel = obj.optString("timeLabel", "09:00 - 10:30"),
                        taskTitle = obj.optString("taskTitle"),
                        category = obj.optString("category", "Work"),
                        isAiOptimized = obj.optBoolean("isAiOptimized", true),
                        isPresetFixed = obj.optBoolean("isPresetFixed", false),
                        alertEnabled = obj.optBoolean("alertEnabled", true)
                    )
                )
            }
            if (result.isNotEmpty()) result else presetSlots
        } catch (e: Exception) {
            e.printStackTrace()
            presetSlots
        }
    }

    private fun cleanJson(raw: String): String {
        var s = raw.trim()
        if (s.startsWith("```json")) {
            s = s.removePrefix("```json")
        } else if (s.startsWith("```")) {
            s = s.removePrefix("```")
        }
        if (s.endsWith("```")) {
            s = s.removeSuffix("```")
        }
        return s.trim()
    }

    private fun parseErrorMessage(responseBody: String, code: Int): String {
        return try {
            val json = JSONObject(responseBody)
            json.optJSONObject("error")?.optString("message") ?: "HTTP $code"
        } catch (e: Exception) {
            "HTTP $code: $responseBody"
        }
    }
}
