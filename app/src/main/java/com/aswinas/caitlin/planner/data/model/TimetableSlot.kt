package com.aswinas.caitlin.planner.data.model

data class TimetableSlot(
    val id: String = java.util.UUID.randomUUID().toString(),
    val timeLabel: String,
    val taskTitle: String? = null,
    val category: String? = null,
    val priority: String? = null,
    val isAiOptimized: Boolean = false,
    val alertEnabled: Boolean = false,
    val isPresetFixed: Boolean = false
)
