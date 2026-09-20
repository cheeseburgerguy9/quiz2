package com.example.ai

import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiService {
    private val client = OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).readTimeout(60, TimeUnit.SECONDS).build()

    suspend fun verifyTask(apiKey: String, title: String, description: String, proofText: String, imageBytes: ByteArray): VerificationResult = withContext(Dispatchers.IO) {
        val prompt = """
            Verify a task from the supplied screenshot and the user's explanation. Do not assume completion without visual evidence.
            Task: $title
            Description: $description
            User explanation: $proofText
            Reply exactly: VERIFIED|score(0-100)|reason OR NOT_VERIFIED|score(0-100)|reason
        """.trimIndent()
        val text = generate(apiKey, prompt, imageBytes)
        parseVerification(text)
    }

    suspend fun extractStudyMinutes(apiKey: String, appName: String, imageBytes: ByteArray, note: String): StudyResult = withContext(Dispatchers.IO) {
        val prompt = """
            This is a beta study-time screenshot. Target app name: $appName. User note: $note
            Check that the target app name is visibly present and read a visible duration/screen-time value if present.
            Never invent a duration. Reply exactly: FOUND|minutes|reason OR NOT_FOUND|0|reason.
        """.trimIndent()
        val text = generate(apiKey, prompt, imageBytes)
        val line = text.lineSequence().firstOrNull { it.contains("FOUND") || it.contains("NOT_FOUND") } ?: text
        val parts = line.split("|", limit = 3)
        StudyResult(parts.firstOrNull()?.trim()?.uppercase() == "FOUND", parts.getOrNull(1)?.trim()?.toIntOrNull()?.coerceAtLeast(0) ?: 0, parts.getOrNull(2)?.trim().orEmpty().ifBlank { text.trim() })
    }

    suspend fun createInsights(apiKey: String, tasksJson: String, studyJson: String): String = withContext(Dispatchers.IO) {
        generate(apiKey, """
            Organize this productivity data into a concise practical report.
            TASK DATA: $tasksJson
            STUDY TIME DATA: $studyJson
            Include progress, task completion patterns, study-time summary, 3 concrete improvement suggestions, and one next step. Do not fabricate data.
        """.trimIndent(), null)
    }

    suspend fun testKey(apiKey: String): Result<Unit> = withContext(Dispatchers.IO) {
        try { generate(apiKey, "Reply exactly OK.", null); Result.success(Unit) } catch (e: Exception) { Result.failure(e) }
    }

    private fun parseVerification(text: String): VerificationResult {
        val line = text.lineSequence().firstOrNull { it.contains("VERIFIED") || it.contains("NOT_VERIFIED") } ?: text
        val parts = line.split("|", limit = 3)
        val status = parts.firstOrNull()?.trim()?.uppercase().orEmpty()
        return VerificationResult(status == "VERIFIED", parts.getOrNull(1)?.trim()?.toIntOrNull()?.coerceIn(0, 100) ?: 0, parts.getOrNull(2)?.trim().orEmpty().ifBlank { text.trim() })
    }

    private fun generate(apiKey: String, prompt: String, imageBytes: ByteArray?): String {
        require(apiKey.isNotBlank()) { "Gemini API key is not configured." }
        val parts = JSONArray().put(JSONObject().put("text", prompt))
        imageBytes?.let { parts.put(JSONObject().put("inline_data", JSONObject().put("mime_type", "image/png").put("data", Base64.encodeToString(it, Base64.NO_WRAP)))) }
        val body = JSONObject().put("contents", JSONArray().put(JSONObject().put("parts", parts))).toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder().url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.6-flash:generateContent?key=$apiKey").post(body).build()
        var resultText = ""
        client.newCall(request).execute().use { response ->
            val raw = response.body?.string().orEmpty()
            if (!response.isSuccessful) throw IllegalStateException("Gemini request failed (${response.code}).")
            resultText = JSONObject(raw).optJSONArray("candidates")?.optJSONObject(0)?.optJSONObject("content")?.optJSONArray("parts")?.optJSONObject(0)?.optString("text").orEmpty()
        }
        if (resultText.isBlank()) throw IllegalStateException("Gemini returned no text.")
        return resultText
    }
    data class VerificationResult(val verified: Boolean, val score: Int, val reason: String)
    data class StudyResult(val found: Boolean, val minutes: Int, val reason: String)
}
