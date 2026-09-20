package com.aswinas.caitlin.planner.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aswinas.caitlin.planner.data.model.TaskEntity
import com.aswinas.caitlin.planner.ui.components.AccountProfileDialog
import com.aswinas.caitlin.planner.ui.viewmodel.MainViewModel

enum class NavigationTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    TASKS("Tasks", Icons.Filled.Checklist, Icons.Outlined.Checklist, "nav_tasks"),
    TIMETABLE("Timetable", Icons.Filled.Schedule, Icons.Outlined.Schedule, "nav_timetable"),
    AI_VERIFY("AI Verify", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome, "nav_ai_verify"),
    INSIGHTS("Insights", Icons.Filled.ShowChart, Icons.Outlined.ShowChart, "nav_insights"),
    SETTINGS("Settings", Icons.Filled.Settings, Icons.Outlined.Settings, "nav_settings")
}

@Composable
fun MainScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val isWizardCompleted by viewModel.isWizardCompleted.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val tasks by viewModel.filteredTasks.collectAsState()
    val allTasks by viewModel.allTasks.collectAsState()
    val studyRecords by viewModel.studyRecords.collectAsState()
    val timetableSlots by viewModel.timetableSlots.collectAsState()
    val geminiApiKey by viewModel.geminiApiKey.collectAsState()
    val isVerifyingKey by viewModel.isVerifyingKey.collectAsState()
    val keyVerificationStatus by viewModel.keyVerificationStatus.collectAsState()
    val isStudyTimeBetaEnabled by viewModel.isStudyTimeBetaEnabled.collectAsState()
    val isCalendarSync by viewModel.isCalendarSync.collectAsState()
    val isAiOptimization by viewModel.isAiOptimization.collectAsState()
    val isNotifications by viewModel.isNotifications.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val isDynamicColor by viewModel.isDynamicColor.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedPriority by viewModel.selectedPriority.collectAsState()
    val isVerifyingTask by viewModel.isVerifyingTask.collectAsState()
    val isExtractingStudyTime by viewModel.isExtractingStudyTime.collectAsState()
    val aiInsightSummary by viewModel.aiInsightSummary.collectAsState()
    val isLoadingAiInsights by viewModel.isLoadingAiInsights.collectAsState()
    val totalFocusMinutes by viewModel.totalFocusMinutes.collectAsState()
    val currentStreakDays by viewModel.currentStreakDays.collectAsState()
    val yesterdayIncompleteTasks by viewModel.yesterdayIncompleteTasks.collectAsState()
    val dailyAiOverview by viewModel.dailyAiOverview.collectAsState()
    val isSyncingDailyOverview by viewModel.isSyncingDailyOverview.collectAsState()
    val overviewPreferenceFocus by viewModel.overviewPreferenceFocus.collectAsState()
    val presetSlots by viewModel.presetSlots.collectAsState()

    var currentTab by remember { mutableStateOf(NavigationTab.TASKS) }
    var taskPassedToAiVerify by remember { mutableStateOf<TaskEntity?>(null) }
    var showAccountDialogFromHome by remember { mutableStateOf(false) }

    // If Setup Wizard is active
    if (!isWizardCompleted) {
        SetupWizardScreen(
            onFinish = { name, age ->
                viewModel.completeWizard(name, age)
            },
            onSkip = {
                viewModel.completeWizard(userProfile.name, userProfile.age)
            }
        )
    } else {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            bottomBar = {
                // Material You Floating Pill Navigation Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        tonalElevation = 6.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(68.dp)
                            .clip(RoundedCornerShape(34.dp))
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                RoundedCornerShape(34.dp)
                            )
                    ) {
                        NavigationTab.entries.forEach { tab ->
                            val isSelected = currentTab == tab
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = { currentTab = tab },
                                icon = {
                                    Icon(
                                        imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.title,
                                        modifier = Modifier.size(22.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = tab.title,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.testTag(tab.testTag)
                            )
                        }
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = paddingValues.calculateBottomPadding())
            ) {
                Crossfade(
                    targetState = currentTab,
                    label = "tab_crossfade"
                ) { targetTab ->
                    when (targetTab) {
                        NavigationTab.TASKS -> {
                            TasksScreen(
                                tasks = tasks,
                                userProfile = userProfile,
                                searchQuery = searchQuery,
                                onSearchQueryChange = { viewModel.setSearchQuery(it) },
                                selectedCategory = selectedCategory,
                                onCategorySelected = { viewModel.setSelectedCategory(it) },
                                selectedPriority = selectedPriority,
                                onPrioritySelected = { viewModel.setSelectedPriority(it) },
                                totalCount = allTasks.size,
                                completedCount = allTasks.count { it.isCompleted },
                                aiVerifiedCount = allTasks.count { it.isAiVerified },
                                yesterdayIncompleteTasks = yesterdayIncompleteTasks,
                                onMoveYesterdayTaskToToday = { viewModel.moveYesterdayTaskToToday(it) },
                                onCompleteYesterdayTask = { viewModel.completeYesterdayTask(it) },
                                onDismissYesterdayReminder = { viewModel.dismissYesterdayReminder(it) },
                                onToggleCompleted = { viewModel.toggleTaskCompleted(it) },
                                onDeleteTask = { viewModel.deleteTask(it) },
                                onRequireAiVerify = { task ->
                                    taskPassedToAiVerify = task
                                    currentTab = NavigationTab.AI_VERIFY
                                },
                                onAddNewTask = { title, desc, cat, prio, time, recurrence, dates ->
                                    viewModel.addNewTask(title, desc, cat, prio, time, recurrence, dates)
                                },
                                onOpenProfile = { showAccountDialogFromHome = true },
                                onLaunchSetupWizard = { viewModel.relaunchWizard() }
                            )
                        }

                        NavigationTab.TIMETABLE -> {
                            TimetableScreen(
                                slots = timetableSlots,
                                presetSlots = presetSlots,
                                isGeminiAvailable = geminiApiKey.isNotBlank(),
                                onOptimizeSchedule = { viewModel.optimizeTimetableWithTodayTasks() },
                                onToggleAlert = { viewModel.toggleTimetableAlert(it) },
                                onUpdateSlot = { viewModel.updateTimetableSlot(it) },
                                onAddSlot = { viewModel.addTimetableSlot(it) },
                                onDeleteSlot = { viewModel.deleteTimetableSlot(it) },
                                onSavePreset = { viewModel.saveCurrentTimetableAsPreset() },
                                onResetPreset = { viewModel.resetToPresetTimetable() },
                                onGoToSettings = { currentTab = NavigationTab.SETTINGS }
                            )
                        }

                        NavigationTab.AI_VERIFY -> {
                            AiVerifyScreen(
                                tasks = allTasks,
                                selectedTaskFromOutside = taskPassedToAiVerify,
                                isGeminiAvailable = geminiApiKey.isNotBlank(),
                                isStudyTimeBetaEnabled = isStudyTimeBetaEnabled,
                                isVerifyingTask = isVerifyingTask,
                                isExtractingStudyTime = isExtractingStudyTime,
                                onVerifyTask = { task, proof, bmp, onComplete ->
                                    viewModel.verifyTaskWithAi(task, proof, bmp, onComplete)
                                },
                                onExtractStudyTime = { appName, bmp, onComplete ->
                                    viewModel.extractStudyTimeFromScreenshot(appName, bmp, onComplete)
                                },
                                onGoToSettings = { currentTab = NavigationTab.SETTINGS }
                            )
                        }

                        NavigationTab.INSIGHTS -> {
                            InsightsScreen(
                                studyRecords = studyRecords,
                                totalFocusMinutes = totalFocusMinutes,
                                currentStreakDays = currentStreakDays,
                                dailyOverview = dailyAiOverview,
                                isSyncingDailyOverview = isSyncingDailyOverview,
                                overviewPreferenceFocus = overviewPreferenceFocus,
                                onSyncDailyOverview = { viewModel.syncDailyOverview(force = true) },
                                onUpdatePreferenceFocus = { viewModel.updateOverviewPreference(it) },
                                isGeminiAvailable = geminiApiKey.isNotBlank(),
                                onGoToSettings = { currentTab = NavigationTab.SETTINGS }
                            )
                        }

                        NavigationTab.SETTINGS -> {
                            SettingsScreen(
                                userProfile = userProfile,
                                onUpdateProfile = { name, age, photoUri ->
                                    viewModel.updateProfile(name, age, photoUri)
                                },
                                geminiApiKey = geminiApiKey,
                                onGeminiApiKeyChange = { viewModel.updateGeminiApiKey(it) },
                                onVerifyApiKey = { onResult ->
                                    viewModel.verifyGeminiApiKey(onResult)
                                },
                                isVerifyingKey = isVerifyingKey,
                                keyVerificationStatus = keyVerificationStatus,
                                isStudyTimeBetaEnabled = isStudyTimeBetaEnabled,
                                onStudyTimeBetaChange = { viewModel.setStudyTimeBetaEnabled(it) },
                                isCalendarSync = isCalendarSync,
                                onCalendarSyncChange = { viewModel.setCalendarSync(it) },
                                isAiOptimization = isAiOptimization,
                                onAiOptimizationChange = { viewModel.setAiOptimization(it) },
                                isNotifications = isNotifications,
                                onNotificationsChange = { viewModel.setNotifications(it) },
                                themeMode = themeMode,
                                onThemeModeChange = { viewModel.setThemeMode(it) },
                                isDynamicColor = isDynamicColor,
                                onDynamicColorChange = { viewModel.setDynamicColor(it) },
                                onExportBackup = { viewModel.exportBackupJson() },
                                onRestoreBackup = { json, onResult ->
                                    viewModel.restoreBackupJson(json, onResult)
                                },
                                onLaunchSetupWizard = { viewModel.relaunchWizard() }
                            )
                        }
                    }
                }

                // Profile Dialog opened from top right of Home
                if (showAccountDialogFromHome) {
                    AccountProfileDialog(
                        currentProfile = userProfile,
                        onDismiss = { showAccountDialogFromHome = false },
                        onSave = { name, age, photoUri ->
                            viewModel.updateProfile(name, age, photoUri)
                            showAccountDialogFromHome = false
                        }
                    )
                }
            }
        }
    }
}
