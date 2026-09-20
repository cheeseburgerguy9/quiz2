package com.example.data.api

import android.graphics.Bitmap
import android.util.Base64
import com.example.data.model.StudyRecord
import com.example.data.model.TaskEntity
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
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(45, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(45, TimeUnit.SECONDS)
        .build()

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

            val request = Request.Builder()
                .url("$BASE_URL?key=${apiKey.trim()}")
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
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

            val request = Request.Builder()
                .url("$BASE_URL?key=${apiKey.trim()}")
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
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

            val request = Request.Builder()
                .url("$BASE_URL?key=${apiKey.trim()}")
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
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

            val request = Request.Builder()
                .url("$BASE_URL?key=${apiKey.trim()}")
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
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
