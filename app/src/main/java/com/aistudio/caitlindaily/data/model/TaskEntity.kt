package com.aistudio.caitlindaily.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val category: String = "Work", // Work, Study, Health, Personal
    val priority: String = "Medium", // High, Medium, Low
    val time: String = "Today",
    val isCompleted: Boolean = false,
    val isAiVerified: Boolean = false,
    val aiScore: Int = 0,
    val aiFeedback: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
