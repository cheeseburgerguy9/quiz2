package com.example.data.model

data class ProductivityInsight(
    val summary: String,
    val peakFocusWindow: String,
    val completionPrediction: Int,
    val doFirstTasks: List<String> = emptyList(),
    val scheduleTasks: List<String> = emptyList(),
    val quickWins: List<String> = emptyList(),
    val actionableTips: List<String> = emptyList(),
    val encouragement: String
)

data class VerificationResult(
    val verified: Boolean,
    val confidence: Int,
    val explanation: String,
    val badge: String,
    val detectedEvidence: String = ""
)

data class UserProfile(
    val name: String = "Caitlin",
    val age: Int? = null,
    val photoPath: String? = null,
    val setupCompleted: Boolean = false
)
