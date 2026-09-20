package com.example

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.CalendarContract
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TaskEntity
import com.example.ui.components.AddTaskDialog
import com.example.ui.screens.AiVerifyScreen
import com.example.ui.screens.InsightsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SetupWizardScreen
import com.example.ui.screens.TasksScreen
import com.example.ui.screens.TimetableScreen
import com.example.ui.theme.ForestBackground
import com.example.ui.theme.ForestSurface
import com.example.ui.theme.ForestSurfaceBorder
import com.example.ui.theme.MintPrimary
import com.example.ui.theme.MintPrimaryContainer
import com.example.ui.theme.MintPrimaryDark
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.viewmodel.MainViewModel
import java.util.Calendar
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()
    private var pendingCalendarEvent: CalendarEvent? = null
    private val calendarPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        val granted = result[Manifest.permission.WRITE_CALENDAR] == true
        if (granted) pendingCalendarEvent?.let { insertCalendarEvent(it) }
        else Toast.makeText(this, "Calendar permission is required to add this task.", Toast.LENGTH_SHORT).show()
        pendingCalendarEvent = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { MyApplicationTheme { MainAppContent(viewModel, ::addTaskToCalendar) } }
    }

    private fun addTaskToCalendar(title: String, description: String, startMillis: Long) {
        if (startMillis <= 0L) return
        val event = CalendarEvent(title, description, startMillis, startMillis + 60 * 60 * 1000)
        if (checkSelfPermission(Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            pendingCalendarEvent = event
            calendarPermissionLauncher.launch(arrayOf(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR))
        } else insertCalendarEvent(event)
    }

    private fun insertCalendarEvent(event: CalendarEvent) {
        try {
            var calendarId = -1L
            contentResolver.query(
                CalendarContract.Calendars.CONTENT_URI,
                arrayOf(CalendarContract.Calendars._ID),
                "${CalendarContract.Calendars.VISIBLE} = 1 AND ${CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL} >= ?",
                arrayOf(CalendarContract.Calendars.CAL_ACCESS_CONTRIBUTOR.toString()),
                "${CalendarContract.Calendars._ID} ASC"
            )?.use { cursor -> if (cursor.moveToFirst()) calendarId = cursor.getLong(0) }
            if (calendarId < 0) throw IllegalStateException("No writable calendar was found")
            val values = ContentValues().apply {
                put(CalendarContract.Events.DTSTART, event.start)
                put(CalendarContract.Events.DTEND, event.end)
                put(CalendarContract.Events.TITLE, event.title)
                put(CalendarContract.Events.DESCRIPTION, event.description)
                put(CalendarContract.Events.CALENDAR_ID, calendarId)
                put(CalendarContract.Events.EVENT_TIMEZONE, java.util.TimeZone.getDefault().id)
            }
            contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
            Toast.makeText(this, "Added to calendar.", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) { Toast.makeText(this, "Could not add to calendar: ${e.message}", Toast.LENGTH_LONG).show() }
    }

    private data class CalendarEvent(val title: String, val description: String, val start: Long, val end: Long)
}

@Composable
fun MainAppContent(viewModel: MainViewModel, onAddToCalendar: (String, String, Long) -> Unit) {
    val tasks by viewModel.filteredTasks.collectAsState()
    val totalCount by viewModel.totalTasksCount.collectAsState()
    val completedCount by viewModel.completedTasksCount.collectAsState()
    val aiVerifiedCount by viewModel.aiVerifiedTasksCount.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedPriority by viewModel.selectedPriority.collectAsState()
    val currentTab by viewModel.currentTab.collectAsState()
    val isSetupWizardVisible by viewModel.isSetupWizardVisible.collectAsState()
    val isAddTaskDialogOpen by viewModel.isAddTaskDialogOpen.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val timetableSlots by viewModel.timetableSlots.collectAsState()
    val hasGeminiKey by viewModel.hasGeminiKey.collectAsState()
    val studySessions by viewModel.studySessions.collectAsState()
    val aiInsights by viewModel.aiInsights.collectAsState()
    val aiBusy by viewModel.aiBusy.collectAsState()
    val aiMessage by viewModel.aiMessage.collectAsState()
    val userAge by viewModel.userAge.collectAsState()
    val profilePhoto by viewModel.profilePhotoBase64.collectAsState()

    Box(Modifier.fillMaxSize().background(ForestBackground)) {
        Scaffold(
            modifier = Modifier.fillMaxSize(), containerColor = ForestBackground,
            bottomBar = { BottomNavBar(currentTab, hasGeminiKey, viewModel::setCurrentTab) }
        ) { innerPadding ->
            Box(Modifier.fillMaxSize().padding(innerPadding)) {
                when (currentTab) {
                    0 -> TasksScreen(tasks, totalCount, completedCount, aiVerifiedCount, searchQuery, viewModel::setSearchQuery, selectedCategory, viewModel::setSelectedCategory, selectedPriority, viewModel::setSelectedPriority, viewModel::toggleTaskCompleted, viewModel::deleteTask, { viewModel.setAddTaskDialogOpen(true) }, viewModel::openSetupWizard, { viewModel.setCurrentTab(4) }, userName)
                    1 -> TimetableScreen(timetableSlots, { if (hasGeminiKey) viewModel.optimizeTimetableWithAi() })
                    2 -> AiVerifyScreen(tasks, hasGeminiKey, aiBusy, aiMessage, viewModel::verifyTaskWithAi, viewModel.studyTimeBetaEnabled.collectAsState().value, viewModel.studyAppName.collectAsState().value, viewModel::verifyStudyScreenshot)
                    3 -> InsightsScreen(totalCount, completedCount, aiVerifiedCount, studySessions, hasGeminiKey, aiInsights, aiBusy, viewModel::generateInsights)
                    4 -> SettingsScreen(userName, userAge, profilePhoto, hasGeminiKey, viewModel::setGeminiKey, viewModel::clearGeminiKey, viewModel.studyTimeBetaEnabled.collectAsState().value, viewModel.studyAppName.collectAsState().value, viewModel::setStudyTimeBetaEnabled, viewModel::setStudyAppName, viewModel::setCalendarSyncEnabled, viewModel::setAiSuggestionsEnabled, viewModel::setNotificationsEnabled, viewModel::openSetupWizard, { uri, callback -> lifecycleScope.launch { viewModel.exportBackup(uri).fold({ callback(true, "Backup exported") }, { callback(false, it.message ?: "Backup failed") }) } }, { uri, callback -> lifecycleScope.launch { viewModel.importBackup(uri).fold({ callback(true, "Backup restored") }, { callback(false, it.message ?: "Restore failed") }) } })
                }
            }
        }
        if (isAddTaskDialogOpen) {
            AddTaskDialog(
                onDismiss = { viewModel.setAddTaskDialogOpen(false) },
                onConfirm = { title, desc, cat, priority, scheduledAt, addToCalendar ->
                    viewModel.addTask(title, desc, cat, priority, scheduledAt)
                    if (addToCalendar) onAddToCalendar(title, desc, scheduledAt)
                    viewModel.setAddTaskDialogOpen(false)
                }
            )
        }
        AnimatedVisibility(isSetupWizardVisible, enter = fadeIn() + slideInVertically { it }, exit = fadeOut() + slideOutVertically { it }) {
            SetupWizardScreen(
                onFinish = { name, age, photoUri ->
                    if (photoUri != null) {
                        viewModel.saveProfilePhoto(photoUri) { encoded ->
                            viewModel.setAccount(name, age, encoded)
                            viewModel.closeSetupWizard()
                        }
                    } else {
                        viewModel.setAccount(name, age, profilePhoto)
                        viewModel.closeSetupWizard()
                    }
                },
                onSkip = viewModel::closeSetupWizard
            )
        }
    }
}

@Composable
private fun BottomNavBar(selectedTab: Int, aiAvailable: Boolean, onSelectTab: (Int) -> Unit) {
    val navItems = listOf(Triple(0, Icons.Default.Checklist, "Tasks"), Triple(1, Icons.Default.AccessTime, "Schedule"), Triple(2, Icons.Default.AutoAwesome, "AI Verify"), Triple(3, Icons.Default.ShowChart, "Insights"), Triple(4, Icons.Default.Person, "Profile"))
    Box(Modifier.fillMaxWidth().navigationBarsPadding().clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)).background(ForestSurface).border(1.dp, ForestSurfaceBorder, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)).padding(horizontal = 12.dp, vertical = 8.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround, verticalAlignment = Alignment.CenterVertically) {
            navItems.forEach { (index, icon, label) ->
                val enabled = aiAvailable || index !in listOf(1, 2, 3)
                NavItem(icon, label, selectedTab == index, enabled) { onSelectTab(index) }
            }
        }
    }
}

@Composable
private fun NavItem(icon: ImageVector, label: String, isSelected: Boolean, enabled: Boolean, onClick: () -> Unit) {
    Column(Modifier.clip(RoundedCornerShape(16.dp)).clickable(enabled = enabled) { onClick() }.padding(horizontal = 12.dp, vertical = 6.dp).testTag("nav_tab_$label"), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(48.dp, 32.dp).clip(RoundedCornerShape(50)).background(if (isSelected && enabled) MintPrimaryContainer else androidx.compose.ui.graphics.Color.Transparent), Alignment.Center) {
            Icon(icon, label, tint = if (!enabled) TextTertiary.copy(alpha = .35f) else if (isSelected) MintPrimary else TextSecondary)
        }
        Text(label, color = if (!enabled) TextTertiary.copy(alpha = .35f) else if (isSelected) MintPrimary else TextSecondary, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
    }
}
