package com.example.data.model

data class TimetableSlot(
    val timeLabel: String,
    val taskTitle: String? = null,
    val category: String? = null,
    val priority: String? = null,
    val isAiOptimized: Boolean = false
)
