package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.AppDatabase
import com.example.data.model.TaskEntity
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context verifies Caitlin Daily app name`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Caitlin Daily", appName)
    }

    @Test
    fun `test database task insertion and retrieval`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = AppDatabase.getDatabase(context)
        val taskDao = db.taskDao()

        val taskId = taskDao.insertTask(
            TaskEntity(
                title = "Test Robolectric Task",
                description = "Testing DB integration",
                category = "Work",
                priority = "HIGH",
                estimatedMinutes = 45,
                scheduledTime = "10:00 AM",
                createdDate = "2026-09-20"
            )
        )
        assertTrue(taskId > 0)
    }
}
