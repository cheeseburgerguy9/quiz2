package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.TimetableSlot
import kotlinx.coroutines.flow.Flow

@Dao
interface TimetableDao {
    @Query("SELECT * FROM timetable_slots WHERE date = :date ORDER BY id ASC")
    fun getTimetableForDate(date: String): Flow<List<TimetableSlot>>

    @Query("SELECT * FROM timetable_slots ORDER BY id ASC")
    suspend fun getAllSlotsSnapshot(): List<TimetableSlot>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSlots(slots: List<TimetableSlot>)

    @Query("DELETE FROM timetable_slots WHERE date = :date")
    suspend fun clearSlotsForDate(date: String)

    @Query("DELETE FROM timetable_slots")
    suspend fun clearAll()

    @Update
    suspend fun updateSlot(slot: TimetableSlot)

    @Query("UPDATE timetable_slots SET calendarSynced = :synced WHERE id = :id")
    suspend fun updateCalendarSync(id: Long, synced: Boolean)
}
