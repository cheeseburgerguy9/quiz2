package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AccountProfileDialog
import com.example.ui.theme.ForestBackground
import com.example.ui.theme.ForestSurface
import com.example.ui.theme.ForestSurfaceBorder
import com.example.ui.theme.ForestSurfaceCard
import com.example.ui.theme.MintPrimary
import com.example.ui.theme.MintPrimaryDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.viewmodel.MainViewModel

data class NavItem(
    val title: String,
    val icon: ImageVector,
    val requiresGemini: Boolean = false
)

@Composable
fun MainScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val currentTab by viewModel.currentTab.collectAsState()
    val isSetupWizardVisible by viewModel.isSetupWizardVisible.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val geminiApiKey by viewModel.geminiApiKey.collectAsState()
    val isGeminiAvailable by viewModel.isGeminiAvailable.collectAsState()
    val isVerifyingKey by viewModel.isVerifyingKey.collectAsState()
    val keyVerificationStatus by viewModel.keyVerificationStatus.collectAsState()

    val isStudyTimeBetaEnabled by viewModel.isStudyTimeBetaEnabled.collectAsState()
    val isCalendarSyncEnabled by viewModel.isCalendarSyncEnabled.collectAsState()
    val isAiOptimizationEnabled by viewModel.isAiOptimizationEnabled.collectAsState()
    val isNotificationsEnabled by viewModel.isNotificationsEnabled.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val isDynamicColor by viewModel.isDynamicColor.collectAsState()

    val filteredTasks by viewModel.filteredTasks.collectAsState()
    val rawTasks by viewModel.rawTasks.collectAsState()
    val totalTasksCount by viewModel.totalTasksCount.collectAsState()
    val completedTasksCount by viewModel.completedTasksCount.collectAsState()
    val aiVerifiedTasksCount by viewModel.aiVerifiedTasksCount.collectAsState()
    val selectedTaskForVerification by viewModel.selectedTaskForVerification.collectAsState()
    val isVerifyingTask by viewModel.isVerifyingTask.collectAsState()
    val isExtractingStudyTime by viewModel.isExtractingStudyTime.collectAsState()

    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()

    val timetableSlots by viewModel.timetableSlots.collectAsState()
    val studyRecords by viewModel.studyRecords.collectAsState()
    val totalStudyMinutes by viewModel.totalStudyMinutes.collectAsState()
    val aiInsights by viewModel.aiInsights.collectAsState()
    val isLoadingInsights by viewModel.isLoadingInsights.collectAsState()

    val isAccountDialogOpen by viewModel.isAccountDialogOpen.collectAsState()

    val navItems = listOf(
        NavItem("Agenda", Icons.Default.TaskAlt),
        NavItem("Flow", Icons.Default.Schedule),
        NavItem("AI Verify", Icons.Default.AutoAwesome, requiresGemini = true),
        NavItem("Insights", Icons.Default.ShowChart, requiresGemini = true),
        NavItem("Settings", Icons.Default.Settings)
    )

    if (isSetupWizardVisible) {
        SetupWizardScreen(
            onFinish = { name, age ->
                viewModel.updateUserProfile(name, age, userProfile.photoUri)
                viewModel.closeSetupWizard()
            },
            onSkip = { viewModel.closeSetupWizard() }
        )
    } else {
        Scaffold(
            bottomBar = {
                NavigationBar(
                    containerColor = ForestSurface,
                    contentColor = TextSecondary,
                    tonalElevation = 8.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .border(1.dp, ForestSurfaceBorder)
                        .testTag("main_navigation_bar")
                ) {
                    navItems.forEachIndexed { index, item ->
                        val isSelected = currentTab == index
                        val isGrayed = item.requiresGemini && !isGeminiAvailable

                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { viewModel.setCurrentTab(index) },
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.title,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = item.title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MintPrimaryDark,
                                selectedTextColor = MintPrimary,
                                indicatorColor = MintPrimary,
                                unselectedIconColor = if (isGrayed) TextTertiary.copy(alpha = 0.5f) else TextSecondary,
                                unselectedTextColor = if (isGrayed) TextTertiary.copy(alpha = 0.5f) else TextSecondary
                            ),
                            modifier = Modifier.testTag("nav_item_$index")
                        )
                    }
                }
            },
            containerColor = ForestBackground,
            modifier = modifier.fillMaxSize()
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = paddingValues.calculateBottomPadding())
            ) {
                when (currentTab) {
                    0 -> TasksScreen(
                        tasks = filteredTasks,
                        userProfile = userProfile,
                        searchQuery = searchQuery,
                        onSearchQueryChange = { viewModel.setSearchQuery(it) },
                        selectedCategory = selectedCategory,
                        onCategorySelected = { viewModel.setSelectedCategory(it) },
                        totalCount = totalTasksCount,
                        completedCount = completedTasksCount,
                        aiVerifiedCount = aiVerifiedTasksCount,
                        onToggleCompleted = { viewModel.toggleTaskCompleted(it) },
                        onDeleteTask = { viewModel.deleteTask(it) },
                        onRequireAiVerify = { task -> viewModel.selectTaskForVerification(task) },
                        onAddNewTask = { title, desc, cat, priority, time ->
                            viewModel.addTask(title, desc, cat, priority, time)
                        },
                        onOpenProfile = { viewModel.setAccountDialogOpen(true) }
                    )

                    1 -> TimetableScreen(
                        slots = timetableSlots,
                        isGeminiAvailable = isGeminiAvailable,
                        onOptimizeSchedule = { viewModel.optimizeTimetableWithAi() },
                        onGoToSettings = { viewModel.setCurrentTab(4) }
                    )

                    2 -> AiVerifyScreen(
                        tasks = rawTasks,
                        selectedTaskFromOutside = selectedTaskForVerification,
                        isGeminiAvailable = isGeminiAvailable,
                        isStudyTimeBetaEnabled = isStudyTimeBetaEnabled,
                        isVerifyingTask = isVerifyingTask,
                        isExtractingStudyTime = isExtractingStudyTime,
                        onVerifyTask = { task, proof, bmp, cb ->
                            viewModel.verifyTaskWithAi(task, proof, bmp, cb)
                        },
                        onExtractStudyTime = { appName, bmp, cb ->
                            viewModel.extractStudyTimeFromScreenshot(appName, bmp, cb)
                        },
                        onGoToSettings = { viewModel.setCurrentTab(4) }
                    )

                    3 -> InsightsScreen(
                        totalTasks = totalTasksCount,
                        completedTasks = completedTasksCount,
                        aiVerifiedTasks = aiVerifiedTasksCount,
                        studyRecords = studyRecords,
                        totalStudyMinutes = totalStudyMinutes,
                        aiInsights = aiInsights,
                        isLoadingInsights = isLoadingInsights,
                        isGeminiAvailable = isGeminiAvailable,
                        onGenerateInsights = { viewModel.generateAiInsights() },
                        onGoToSettings = { viewModel.setCurrentTab(4) }
                    )

                    4 -> SettingsScreen(
                        userProfile = userProfile,
                        onUpdateProfile = { name, age, photoUri ->
                            viewModel.updateUserProfile(name, age, photoUri)
                        },
                        geminiApiKey = geminiApiKey,
                        onGeminiApiKeyChange = { viewModel.setGeminiApiKey(it) },
                        onVerifyApiKey = { cb -> viewModel.verifyGeminiApiKey(cb) },
                        isVerifyingKey = isVerifyingKey,
                        keyVerificationStatus = keyVerificationStatus,
                        isStudyTimeBetaEnabled = isStudyTimeBetaEnabled,
                        onStudyTimeBetaChange = { viewModel.setStudyTimeBetaEnabled(it) },
                        isCalendarSync = isCalendarSyncEnabled,
                        onCalendarSyncChange = { viewModel.setCalendarSyncEnabled(it) },
                        isAiOptimization = isAiOptimizationEnabled,
                        onAiOptimizationChange = { viewModel.setAiOptimizationEnabled(it) },
                        isNotifications = isNotificationsEnabled,
                        onNotificationsChange = { viewModel.setNotificationsEnabled(it) },
                        themeMode = themeMode,
                        onThemeModeChange = { viewModel.setThemeMode(it) },
                        isDynamicColor = isDynamicColor,
                        onDynamicColorChange = { viewModel.setDynamicColorEnabled(it) },
                        onExportBackup = { viewModel.getBackupJson() },
                        onRestoreBackup = { json, cb -> viewModel.restoreBackup(json, cb) },
                        onLaunchSetupWizard = { viewModel.openSetupWizard() }
                    )
                }

                // Account Profile Dialog triggered from avatar or elsewhere
                if (isAccountDialogOpen) {
                    AccountProfileDialog(
                        currentProfile = userProfile,
                        onDismiss = { viewModel.setAccountDialogOpen(false) },
                        onSave = { name, age, photoUri ->
                            viewModel.updateUserProfile(name, age, photoUri)
                            viewModel.setAccountDialogOpen(false)
                        }
                    )
                }
            }
        }
    }
}
