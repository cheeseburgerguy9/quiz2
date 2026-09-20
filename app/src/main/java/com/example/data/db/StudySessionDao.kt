package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.data.model.StudySessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StudySessionDao {
    @Query("SELECT * FROM study_sessions ORDER BY dateMillis DESC, id DESC")
    fun getAll(): Flow<List<StudySessionEntity>>

    @Insert
    suspend fun insert(session: StudySessionEntity): Long

    @Query("DELETE FROM study_sessions")
    suspend fun clearAll()
}
