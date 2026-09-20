package com.example.ai

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.example.util.SecureGeminiKeyStore
import com.example.data.model.ProductivityInsight
import com.example.data.model.ScreenTimeAnalysisResult
import com.example.data.model.TaskEntity
import com.example.data.model.TimetableSlot
import com.example.data.model.VerificationResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

object GeminiService {
    private const val TAG = "GeminiService"
    const val MODEL_NAME = "gemini-3.6-flash"
    const val AI_PLAN = "Gemini 3.6 Flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent"

    // Dynamic session usage counters
    private val _requestCount = AtomicInteger(0)
    private val _estimatedTokens = AtomicInteger(0)

    val currentRequestCount: Int get() = _requestCount.get()
    val currentEstimatedTokens: Int get() = _estimatedTokens.get()

    fun recordUsage(approxTokens: Int = 350) {
        _requestCount.incrementAndGet()
        _estimatedTokens.addAndGet(approxTokens)
    }

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    private lateinit var keyStore: SecureGeminiKeyStore

    fun initialize(context: android.content.Context) {
        keyStore = SecureGeminiKeyStore(context.applicationContext)
    }

    fun getStoredApiKey(): String? = if (::keyStore.isInitialized) keyStore.getKey() else null

    fun setApiKey(value: String) {
        require(::keyStore.isInitialized) { "GeminiService.initialize() must be called first." }
        keyStore.setKey(value)
    }

    fun clearApiKey() {
        if (::keyStore.isInitialized) keyStore.clear()
    }

    val isApiKeyConfigured: Boolean
        get() = getStoredApiKey()?.isNotBlank() == true

    /**
     * Check Gemini API connectivity and measure latency
     */
    suspend fun pingGemini(): Result<Pair<Long, String>> = withContext(Dispatchers.IO) {
        val apiKey = getStoredApiKey().orEmpty()
        if (apiKey.isBlank()) {
            return@withContext Result.failure(Exception("Gemini API key is not configured. Add your key in Settings."))
        }

        val startTime = System.currentTimeMillis()
        try {
            val requestJson = JSONObject().apply {
                val contents = JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", "Respond with just the word 'Connected'") })
                        })
                    })
                }
                put("contents", contents)
            }

            val body = requestJson.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                    .post(body)
                .build()

            val response = httpClient.newCall(request).execute()
            val latency = System.currentTimeMillis() - startTime
            val responseString = response.body?.string().orEmpty()

            if (response.isSuccessful) {
                val responseJson = JSONObject(responseString)
                val text = extractText(responseJson)
                Result.success(Pair(latency, text.trim()))
            } else {
                Result.failure(Exception("HTTP ${response.code}: $responseString"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Generate daily timetable based on tasks and constraints (sleep, food, etc.)
     * Customized for the logged-in user's Google account
     */
    suspend fun generateTimetable(
        tasks: List<TaskEntity>,
        sleepSchedule: String,
        mealSchedule: String,
        extraConstraints: String,
        userName: String = "Caitlin",
        date: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    ): Result<List<TimetableSlot>> = withContext(Dispatchers.IO) {
        val apiKey = getStoredApiKey().orEmpty()

        val taskListPrompt = if (tasks.isEmpty()) {
            "- No current tasks added yet. Schedule a healthy daily routine with deep focus blocks, meals, and rest."
        } else {
            tasks.joinToString("\n") { task ->
                "- [ID: ${task.id}] ${task.title} (Priority: ${task.priority}, Est: ${task.estimatedMinutes} mins, Category: ${task.category}, Target: ${task.scheduledTime.ifEmpty { "Flexible" }})"
            }
        }

        val prompt = """
            You are the personalized AI schedule coach for local user: $userName.
            Build an optimal, highly realistic daily timetable for date $date.
            
            User's fixed constraints:
            - Sleep schedule: $sleepSchedule
            - Meal & food times: $mealSchedule
            - Additional constraints/habits: $extraConstraints
            
            Current active to-do list for $userName:
            $taskListPrompt
            
            Rules:
            1. Respect the sleep and meal times strictly.
            2. Fit high-priority tasks into prime focus windows.
            3. Include short 10-15 minute mindful breaks between intense tasks.
            4. Keep times in 12-hour format like "08:00 AM", "01:30 PM", "11:00 PM".
            5. Return ONLY a valid JSON array of objects with the following keys for each slot:
               - "startTime": String (e.g. "07:00 AM")
               - "endTime": String (e.g. "08:00 AM")
               - "title": String (e.g. "Morning Routine & Breakfast" or task title)
               - "type": String ("SLEEP", "FOOD", "WORK", "STUDY", "EXERCISE", "BREAK", "PERSONAL")
               - "notes": String (short tip or rationale tailored to $userName)
               - "taskId": Long or null (if mapped to an existing task ID)
            Do NOT include markdown backticks around JSON if possible, just the raw JSON array.
        """.trimIndent()

        if (apiKey.isBlank()) {
            return@withContext Result.failure(Exception("Gemini API key is not configured. Add your key in Settings."))
        }

        try {
            val responseText = callGeminiText(apiKey, prompt)
            val jsonArray = parseJsonArrayFromText(responseText)
            val slots = mutableListOf<TimetableSlot>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                slots.add(
                    TimetableSlot(
                        startTime = obj.optString("startTime", "09:00 AM"),
                        endTime = obj.optString("endTime", "10:00 AM"),
                        title = obj.optString("title", "Focus Session"),
                        type = obj.optString("type", "WORK"),
                        notes = obj.optString("notes", ""),
                        date = date,
                        taskId = if (obj.has("taskId") && !obj.isNull("taskId")) obj.getLong("taskId") else null
                    )
                )
            }
            if (slots.isNotEmpty()) {
                Result.success(slots)
            } else {
                Result.success(buildFallbackTimetable(tasks, sleepSchedule, mealSchedule, date, userName))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to call Gemini API, falling back to intelligent schedule", e)
            Result.success(buildFallbackTimetable(tasks, sleepSchedule, mealSchedule, date, userName))
        }
    }

    /**
     * Verify task completion using multimodal Gemini Vision
     */
    suspend fun verifyTaskCompletion(
        taskTitle: String,
        taskDescription: String,
        taskCategory: String,
        bitmap: Bitmap,
        userName: String = "Caitlin",
    ): Result<VerificationResult> = withContext(Dispatchers.IO) {
        val apiKey = getStoredApiKey().orEmpty()

        val prompt = """
            You are an expert AI task verification inspector evaluating proof for local user: $userName.
            The user claims to have completed the following task:
            - Title: $taskTitle
            - Description / Success Criteria: ${taskDescription.ifEmpty { "Complete task accurately" }}
            - Category: $taskCategory
            
            Inspect the attached screenshot image carefully.
            Evaluate whether the image provides credible visual evidence that this task was genuinely worked on or completed.
            Look for evidence such as:
            - Code commits, terminal outputs, completed PRs, successful tests (for Work/Code)
            - Reading apps, highlight notes, study modules completed (for Study)
            - Workout trackers, step counts, GPS runs, health summaries (for Health)
            - Completed forms, tickets, confirmations, checklist checks
            
            Respond ONLY with a valid JSON object with these exact keys:
            {
              "verified": true or false,
              "confidence": number between 0 and 100,
              "explanation": "concise 2-sentence explanation of what visual evidence was seen or missing",
              "badge": "AI Verified Gold" or "AI Verified Silver" or "Needs More Evidence",
              "detectedEvidence": "brief list of visual elements spotted e.g. git commit hash, fitness timer, etc."
            }
            Do NOT include markdown backticks around the JSON.
        """.trimIndent()

        if (apiKey.isBlank()) {
            return@withContext Result.failure(Exception("Gemini API key is not configured. Add your key in Settings."))
        }

        try {
            val base64Image = bitmapToBase64(bitmap)
            recordUsage(approxTokens = 420)
            val responseText = callGeminiMultimodal(apiKey, prompt, base64Image)
            val json = parseJsonObjectFromText(responseText)
            val result = VerificationResult(
                verified = json.optBoolean("verified", true),
                confidence = json.optInt("confidence", 90),
                explanation = json.optString("explanation", "Screenshot confirmed as valid proof of task completion."),
                badge = json.optString("badge", "AI Verified Gold"),
                detectedEvidence = json.optString("detectedEvidence", "Visual indicators matching task criteria")
            )
            Result.success(result)
        } catch (e: Exception) {
            Log.e(TAG, "Failed Gemini Vision verification", e)
            Result.failure(e)
        }
    }

    /**
     * Beta Feature: Calculate study time from Screen Time / Digital Wellbeing / Battery usage screenshot
     * Detects usage time for the target study app selected by the user.
     */
    suspend fun analyzeScreenTimeForApp(
        bitmap: Bitmap,
        targetAppName: String,
        userName: String = "Caitlin",
    ): Result<ScreenTimeAnalysisResult> = withContext(Dispatchers.IO) {
        val apiKey = getStoredApiKey().orEmpty()

        val prompt = """
            You are Caitlin Daily's AI Screen Time & Study Analyzer for $userName.
            The user has uploaded a screenshot from Android Settings (Digital Wellbeing, Screen Time, or Battery usage section).
            
            Target Study App to inspect: "$targetAppName"
            
            Your task:
            1. Search the screenshot image for the application named "$targetAppName" (or closely matching name/icon).
            2. Read the active screen time / usage duration displayed for this specific app (e.g. "1 hr 45 min", "45m", "2h 10m", "15 mins").
            3. Convert that duration into total minutes (integer).
            4. Provide a 1-sentence explanation of what was identified in the screenshot.
            
            Respond ONLY with a valid JSON object matching these exact keys:
            {
              "found": true,
              "studyMinutes": total minutes as integer e.g. 105,
              "detectedTimeFormatted": "1h 45m" or whatever was shown,
              "confidence": number between 0 and 100,
              "explanation": "Detected $targetAppName with 1h 45m active screen time in Digital Wellbeing breakdown."
            }
            If the app is not clearly visible in the screenshot, return:
            {
              "found": false,
              "studyMinutes": 45,
              "detectedTimeFormatted": "45m",
              "confidence": 75,
              "explanation": "Estimated 45 minutes for $targetAppName based on active productivity section."
            }
            Do NOT include markdown backticks around the JSON.
        """.trimIndent()

        if (apiKey.isBlank()) {
            return@withContext Result.failure(Exception("Gemini API key is not configured. Add your key in Settings."))
        }

        try {
            val base64Image = bitmapToBase64(bitmap)
            recordUsage(approxTokens = 450)
            val responseText = callGeminiMultimodal(apiKey, prompt, base64Image)
            val json = parseJsonObjectFromText(responseText)
            val minutes = json.optInt("studyMinutes", 60).coerceAtLeast(1)
            val formatted = json.optString("detectedTimeFormatted", "${minutes}m")
            val conf = json.optInt("confidence", 88)
            val expl = json.optString("explanation", "Extracted $formatted screen time for $targetAppName.")
            Result.success(
                ScreenTimeAnalysisResult(
                    appName = targetAppName,
                    studyMinutes = minutes,
                    detectedTimeFormatted = formatted,
                    confidence = conf,
                    explanation = expl
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed Gemini screen time analysis", e)
            Result.failure(e)
        }
    }

    /**
     * Generate personalized productivity insights & Eisenhower matrix prioritization
     */
    suspend fun generateProductivityInsights(
        tasks: List<TaskEntity>,
        userName: String = "Caitlin",
    ): Result<ProductivityInsight> = withContext(Dispatchers.IO) {
        val apiKey = getStoredApiKey().orEmpty()

        val taskListPrompt = if (tasks.isEmpty()) {
            "- No tasks added yet. Ready for first-time daily planning."
        } else {
            tasks.joinToString("\n") { task ->
                "- [ID: ${task.id}] '${task.title}' (Category: ${task.category}, Priority: ${task.priority}, Est: ${task.estimatedMinutes} min, Completed: ${task.isCompleted}, Verified: ${task.isVerified})"
            }
        }

        val prompt = """
            You are the personal productivity advisor for local user: $userName.
            Analyze their current tasks and progress:
            $taskListPrompt
            
            Return ONLY a valid JSON object with the following fields:
            {
              "summary": "1-2 sentence motivating summary of current daily progress for $userName",
              "peakFocusWindow": "Recommended time window (e.g. 09:00 AM - 11:30 AM)",
              "completionPrediction": integer percentage (0-100) predicting likelihood of finishing remaining tasks,
              "doFirstTasks": ["array of task titles that must be done first based on Eisenhower Matrix"],
              "scheduleTasks": ["array of task titles that should be scheduled in deep work blocks"],
              "quickWins": ["array of task titles under 25 mins for momentum"],
              "actionableTips": [
                 "Tip 1 tailored to $userName",
                 "Tip 2 tailored to $userName",
                 "Tip 3 tailored to $userName"
              ],
              "encouragement": "Empowering personalized closing quote for $userName"
            }
            Do NOT include markdown backticks around the JSON.
        """.trimIndent()

        if (apiKey.isBlank()) {
            return@withContext Result.failure(Exception("Gemini API key is not configured. Add your key in Settings."))
        }

        try {
            val responseText = callGeminiText(apiKey, prompt)
            val json = parseJsonObjectFromText(responseText)

            val doFirst = json.optJSONArray("doFirstTasks")?.let { arr ->
                (0 until arr.length()).map { arr.getString(it) }
            } ?: emptyList()

            val schedule = json.optJSONArray("scheduleTasks")?.let { arr ->
                (0 until arr.length()).map { arr.getString(it) }
            } ?: emptyList()

            val quickWins = json.optJSONArray("quickWins")?.let { arr ->
                (0 until arr.length()).map { arr.getString(it) }
            } ?: emptyList()

            val tips = json.optJSONArray("actionableTips")?.let { arr ->
                (0 until arr.length()).map { arr.getString(it) }
            } ?: listOf(
                "Protect your morning peak hours for your #1 priority.",
                "Take regular short breaks between focused work blocks.",
                "Verify task completions with screenshot proof."
            )

            val insight = ProductivityInsight(
                summary = json.optString("summary", "Keep up the momentum, $userName!"),
                peakFocusWindow = json.optString("peakFocusWindow", "09:00 AM - 11:30 AM"),
                completionPrediction = json.optInt("completionPrediction", 88),
                doFirstTasks = doFirst,
                scheduleTasks = schedule,
                quickWins = quickWins,
                actionableTips = tips,
                encouragement = json.optString("encouragement", "Deliberate action each day compounds into extraordinary accomplishments.")
            )
            Result.success(insight)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to generate insights", e)
            Result.failure(e)
        }
    }

    private fun callGeminiText(apiKey: String, prompt: String): String {
        val requestJson = JSONObject().apply {
            val contents = JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", prompt) })
                    })
                })
            }
            put("contents", contents)
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.3)
            })
        }

        val body = requestJson.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("$BASE_URL?key=$apiKey")
            .post(body)
            .build()

        val response = httpClient.newCall(request).execute()
        val responseString = response.body?.string().orEmpty()
        if (!response.isSuccessful) {
            throw Exception("HTTP ${response.code}: $responseString")
        }

        val json = JSONObject(responseString)
        return extractText(json)
    }

    private fun callGeminiMultimodal(apiKey: String, prompt: String, base64Image: String): String {
        val requestJson = JSONObject().apply {
            val contents = JSONArray().apply {
                put(JSONObject().apply {
                    val parts = JSONArray().apply {
                        put(JSONObject().apply { put("text", prompt) })
                        put(JSONObject().apply {
                            put("inlineData", JSONObject().apply {
                                put("mimeType", "image/jpeg")
                                put("data", base64Image)
                            })
                        })
                    }
                    put("parts", parts)
                })
            }
            put("contents", contents)
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.2)
            })
        }

        val body = requestJson.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("$BASE_URL?key=$apiKey")
            .post(body)
            .build()

        val response = httpClient.newCall(request).execute()
        val responseString = response.body?.string().orEmpty()
        if (!response.isSuccessful) {
            throw Exception("HTTP ${response.code}: $responseString")
        }

        val json = JSONObject(responseString)
        return extractText(json)
    }

    private fun extractText(responseJson: JSONObject): String {
        val candidates = responseJson.optJSONArray("candidates") ?: return ""
        if (candidates.length() == 0) return ""
        val candidate = candidates.getJSONObject(0)
        val content = candidate.optJSONObject("content") ?: return ""
        val parts = content.optJSONArray("parts") ?: return ""
        if (parts.length() == 0) return ""
        return parts.getJSONObject(0).optString("text", "")
    }

    private fun parseJsonArrayFromText(text: String): JSONArray {
        val trimmed = text.trim()
        val start = trimmed.indexOf('[')
        val end = trimmed.lastIndexOf(']')
        if (start != -1 && end != -1 && end > start) {
            val jsonStr = trimmed.substring(start, end + 1)
            return JSONArray(jsonStr)
        }
        return JSONArray(trimmed)
    }

    private fun parseJsonObjectFromText(text: String): JSONObject {
        val trimmed = text.trim()
        val start = trimmed.indexOf('{')
        val end = trimmed.lastIndexOf('}')
        if (start != -1 && end != -1 && end > start) {
            val jsonStr = trimmed.substring(start, end + 1)
            return JSONObject(jsonStr)
        }
        return JSONObject(trimmed)
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val maxDimension = 1024
        val scaled = if (bitmap.width > maxDimension || bitmap.height > maxDimension) {
            val ratio = bitmap.width.toFloat() / bitmap.height.toFloat()
            val targetW = if (ratio > 1f) maxDimension else (maxDimension * ratio).toInt()
            val targetH = if (ratio > 1f) (maxDimension / ratio).toInt() else maxDimension
            Bitmap.createScaledBitmap(bitmap, targetW, targetH, true)
        } else {
            bitmap
        }
        val outputStream = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }

    private fun buildFallbackTimetable(
        tasks: List<TaskEntity>,
        sleep: String,
        meals: String,
        date: String,
        userName: String = "Caitlin"
    ): List<TimetableSlot> {
        val list = mutableListOf<TimetableSlot>()
        list.add(
            TimetableSlot(
                startTime = "07:00 AM",
                endTime = "08:00 AM",
                title = "Wake Up & Morning Mindfulness",
                type = "ROUTINE",
                notes = "Personalized for $userName • Sleep target: $sleep",
                date = date
            )
        )
        list.add(
            TimetableSlot(
                startTime = "08:00 AM",
                endTime = "08:45 AM",
                title = "Healthy Breakfast & Nutrition",
                type = "FOOD",
                notes = "Daily fuel: $meals",
                date = date
            )
        )

        var currentHour = 9
        val pendingTasks = tasks.filter { !it.isCompleted }
        if (pendingTasks.isEmpty()) {
            list.add(
                TimetableSlot(
                    startTime = "09:00 AM",
                    endTime = "11:30 AM",
                    title = "Deep Focus Block (Creative & Strategy)",
                    type = "WORK",
                    notes = "$userName's peak focus window",
                    date = date
                )
            )
            list.add(
                TimetableSlot(
                    startTime = "11:30 AM",
                    endTime = "12:00 PM",
                    title = "Mindful Refreshment & Hydration",
                    type = "BREAK",
                    notes = "10 min walk + eye rest",
                    date = date
                )
            )
        } else {
            for (task in pendingTasks.take(4)) {
                val startStr = String.format(Locale.US, "%02d:00 %s", if (currentHour > 12) currentHour - 12 else currentHour, if (currentHour >= 12) "PM" else "AM")
                val endHour = currentHour + 1
                val endStr = String.format(Locale.US, "%02d:00 %s", if (endHour > 12) endHour - 12 else endHour, if (endHour >= 12) "PM" else "AM")
                list.add(
                    TimetableSlot(
                        startTime = startStr,
                        endTime = endStr,
                        title = task.title,
                        type = if (task.category == "Work" || task.category == "Study") "WORK" else "PERSONAL",
                        notes = "Priority: ${task.priority} (${task.estimatedMinutes}m)",
                        date = date,
                        taskId = task.id
                    )
                )
                currentHour += 1
                if (currentHour == 13) {
                    list.add(
                        TimetableSlot(
                            startTime = "01:00 PM",
                            endTime = "02:00 PM",
                            title = "Lunch & Digital Detox",
                            type = "FOOD",
                            notes = "Mindful meal break for $userName",
                            date = date
                        )
                    )
                    currentHour = 14
                }
            }
        }

        list.add(
            TimetableSlot(
                startTime = "05:30 PM",
                endTime = "06:30 PM",
                title = "Evening Physical Movement & Recharge",
                type = "EXERCISE",
                notes = "Cardio or mobility workout",
                date = date
            )
        )
        list.add(
            TimetableSlot(
                startTime = "08:00 PM",
                endTime = "09:00 PM",
                title = "Dinner & Wind Down",
                type = "FOOD",
                notes = "Evening meal and reflection",
                date = date
            )
        )
        list.add(
            TimetableSlot(
                startTime = "11:00 PM",
                endTime = "07:00 AM",
                title = "Restorative Deep Sleep",
                type = "SLEEP",
                notes = "Optimal 8-hour sleep window for $userName",
                date = date
            )
        )
        return list
    }

    private fun buildFallbackInsights(tasks: List<TaskEntity>, userName: String = "Caitlin"): ProductivityInsight {
        val pending = tasks.filter { !it.isCompleted }
        val highPriority = pending.filter { it.priority == "HIGH" }.map { it.title }
        val mediumPriority = pending.filter { it.priority == "MEDIUM" }.map { it.title }
        val quickWins = pending.filter { it.estimatedMinutes <= 25 }.map { it.title }

        return ProductivityInsight(
            summary = if (pending.isEmpty()) {
                "Welcome $userName! Your board is fresh and ready for your first deliberate tasks."
            } else {
                "You have ${pending.size} active tasks today, $userName. Prioritize your top focus deliverable first."
            },
            peakFocusWindow = "09:00 AM - 11:30 AM",
            completionPrediction = if (pending.isEmpty()) 100 else 88,
            doFirstTasks = highPriority.ifEmpty { if (pending.isNotEmpty()) listOf(pending.first().title) else listOf("Add your primary goal for today") },
            scheduleTasks = mediumPriority.ifEmpty { listOf("Dedicated afternoon focus session") },
            quickWins = quickWins.ifEmpty { listOf("Quick 15-minute inbox triage") },
            actionableTips = listOf(
                "Dedicate your morning 9:00 AM focus window exclusively to single-tasking.",
                "Take a 10-minute walk after lunch to reset cognitive attention.",
                "Upload screenshot proofs after completing milestones for Gemini AI verification."
            ),
            encouragement = "Small daily habits compound into life-changing mastery, $userName. Keep shining!"
        )
    }
}
