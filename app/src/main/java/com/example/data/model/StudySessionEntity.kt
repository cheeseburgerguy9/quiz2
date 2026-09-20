package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "study_sessions")
data class StudySessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateMillis: Long = System.currentTimeMillis(),
    val appName: String,
    val minutes: Int,
    val sourceNote: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
