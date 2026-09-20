package com.example.data.model

data class ScreenTimeAnalysisResult(
    val appName: String,
    val studyMinutes: Int,
    val detectedTimeFormatted: String, // e.g. "2h 45m"
    val confidence: Int,
    val explanation: String
)
