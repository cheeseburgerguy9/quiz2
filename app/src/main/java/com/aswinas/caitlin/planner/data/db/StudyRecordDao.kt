package com.aswinas.caitlin.planner.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aswinas.caitlin.planner.data.model.StudyRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface StudyRecordDao {
    @Query("SELECT * FROM study_records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<StudyRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: StudyRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<StudyRecord>)

    @Delete
    suspend fun deleteRecord(record: StudyRecord)

    @Query("DELETE FROM study_records")
    suspend fun clearAll()
}
