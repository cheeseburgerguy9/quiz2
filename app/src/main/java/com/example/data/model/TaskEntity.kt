package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val category: String = "Work", // Work, Study, Health, Personal, Routine
    val priority: String = "MEDIUM", // HIGH, MEDIUM, LOW
    val estimatedMinutes: Int = 30,
    val scheduledTime: String = "", // e.g. "09:00 AM"
    val isCompleted: Boolean = false,
    val isVerified: Boolean = false,
    val verificationNotes: String? = null,
    val verificationScore: Int? = null, // 0 - 100
    val screenshotUri: String? = null,
    val calendarSynced: Boolean = false,
    val createdDate: String, // YYYY-MM-DD
    val completedAt: Long? = null
)
