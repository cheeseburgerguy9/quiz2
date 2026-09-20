package com.aistudio.caitlindaily

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.aistudio.caitlindaily.data.db.AppDatabase
import com.aistudio.caitlindaily.data.model.StudyRecord
import com.aistudio.caitlindaily.data.model.TaskEntity
import com.aistudio.caitlindaily.data.model.TimetableSlot
import com.aistudio.caitlindaily.data.model.UserProfile
import com.aistudio.caitlindaily.util.BackupManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CaitlinDailyCujTest {

    private lateinit var database: AppDatabase
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testOfflineAccountAndTaskInsertion() = runBlocking {
        val taskDao = database.taskDao()
        val highPriorityTask = TaskEntity(
            title = "Finalize AI Architecture Review",
            description = "Submit code verification and diagram evidence",
            category = "Work",
            priority = "High",
            time = "Today, 10:00 AM",
            isCompleted = false,
            isAiVerified = false
        )

        taskDao.insertTask(highPriorityTask)
        val allTasks = taskDao.getAllTasksList()

        assertEquals(1, allTasks.size)
        assertEquals("High", allTasks[0].priority)
        assertFalse(allTasks[0].isCompleted)
        assertFalse(allTasks[0].isAiVerified)
    }

    @Test
    fun testStudyRecordTimeCalculation() = runBlocking {
        val studyDao = database.studyDao()
        studyDao.insertRecord(
            StudyRecord(
                appName = "Forest",
                minutes = 50,
                timeFormatted = "50m",
                notes = "Focused study sprint"
            )
        )
        studyDao.insertRecord(
            StudyRecord(
                appName = "Anki",
                minutes = 25,
                timeFormatted = "25m",
                notes = "Spaced repetition vocabulary"
            )
        )

        val records = studyDao.getAllRecords().first()
        val totalMinutes = records.sumOf { it.minutes }

        assertEquals(2, records.size)
        assertEquals(75, totalMinutes)
    }

    @Test
    fun testBackupAndRestoreIntegrity() = runBlocking {
        val profile = UserProfile(
            name = "Test User",
            age = 25,
            photoUri = null,
            isCreated = true
        )

        val tasks = listOf(
            TaskEntity(id = 1, title = "Task 1", description = "Desc 1", category = "Work", priority = "High", time = "10:00 AM"),
            TaskEntity(id = 2, title = "Task 2", description = "Desc 2", category = "Study", priority = "Medium", time = "02:00 PM")
        )

        val studyRecords = listOf(
            StudyRecord(id = 1, appName = "Duolingo", minutes = 30, timeFormatted = "30m", notes = "Spanish Lesson")
        )

        val slots = listOf(
            TimetableSlot(timeLabel = "08:00 AM", taskTitle = "Morning Sprint", category = "Work", priority = "High", isAiOptimized = true)
        )

        val json = BackupManager.createBackupJson(
            userProfile = profile,
            tasks = tasks,
            studyRecords = studyRecords,
            timetableSlots = slots,
            isCalendarSync = true,
            isAiOptimization = true,
            isNotifications = true,
            isStudyTimeBeta = true,
            appCreatedAt = 1700000000000L,
            themeMode = "auto",
            isDynamicColor = true
        )

        assertTrue(json.contains("Test User"))
        assertTrue(json.contains("Task 1"))
        assertTrue(json.contains("Duolingo"))

        val parsed = BackupManager.parseBackupJson(json)
        assertTrue(parsed.isSuccess)
        val backupData = parsed.getOrNull()!!

        assertEquals("Test User", backupData.userProfile.name)
        assertEquals(25, backupData.userProfile.age)
        assertEquals(2, backupData.tasks.size)
        assertEquals(1, backupData.studyRecords.size)
        assertEquals("Duolingo", backupData.studyRecords[0].appName)
        assertEquals(30, backupData.studyRecords[0].minutes)
        assertTrue(backupData.isStudyTimeBeta)
    }
}
