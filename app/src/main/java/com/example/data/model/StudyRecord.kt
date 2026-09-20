package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "study_records")
data class StudyRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val appName: String,
    val minutes: Int,
    val timeFormatted: String,
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
