package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.InsightsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SetupWizardScreen
import com.example.ui.screens.TasksScreen
import com.example.ui.screens.TimetableScreen
import com.example.ui.screens.VerifyScreen
import com.example.ui.theme.CaitlinDailyTheme
import com.example.ui.viewmodel.AppNavTab
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CaitlinDailyApp()
        }
    }
}

@Composable
fun CaitlinDailyApp(viewModel: MainViewModel = viewModel()) {
    val isSetupCompleted by viewModel.isSetupCompleted.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val currentTab by viewModel.currentTab.collectAsState()
    val isAiEnabled by viewModel.isAiEnabled.collectAsState()
    val notificationMessage by viewModel.userNotification.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(notificationMessage) {
        notificationMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearNotification()
        }
    }

    CaitlinDailyTheme(
        darkTheme = when (themeMode) {
            "LIGHT" -> false
            "DARK" -> true
            else -> androidx.compose.foundation.isSystemInDarkTheme()
        },
        dynamicColor = true
    ) {
    Crossfade(
        targetState = isSetupCompleted,
        label = "setup_vs_main_transition"
    ) { setupDone ->
        if (!setupDone) {
            SetupWizardScreen(viewModel = viewModel)
        } else {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                snackbarHost = { SnackbarHost(snackbarHostState) },
                bottomBar = {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 4.dp,
                        modifier = Modifier.testTag("app_navigation_bar")
                    ) {
                        NavigationBarItem(
                            selected = currentTab == AppNavTab.TASKS,
                            onClick = { viewModel.selectTab(AppNavTab.TASKS) },
                            icon = {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Tasks")
                            },
                            label = {
                                Text(
                                    text = "Tasks",
                                    fontWeight = if (currentTab == AppNavTab.TASKS) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                selectedTextColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.testTag("nav_tab_tasks")
                        )

                        NavigationBarItem(
                            selected = currentTab == AppNavTab.TIMETABLE,
                            onClick = { viewModel.selectTab(AppNavTab.TIMETABLE) },
                            icon = {
                                Icon(Icons.Default.CalendarMonth, contentDescription = "Timetable")
                            },
                            label = {
                                Text(
                                    text = "Timetable",
                                    fontWeight = if (currentTab == AppNavTab.TIMETABLE) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                selectedTextColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.testTag("nav_tab_timetable")
                        )

                        NavigationBarItem(
                            selected = currentTab == AppNavTab.VERIFY,
                            onClick = {
                                viewModel.selectTab(AppNavTab.VERIFY)
                                if (!isAiEnabled) {
                                    viewModel.showNotification("Turn on Gemini to access AI Verify")
                                }
                            },
                            icon = {
                                Icon(
                                    Icons.Default.Verified,
                                    contentDescription = "Verify",
                                    tint = if (isAiEnabled) {
                                        if (currentTab == AppNavTab.VERIFY) MaterialTheme.colorScheme.onPrimaryContainer
                                        else MaterialTheme.colorScheme.onSurfaceVariant
                                    } else {
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                    }
                                )
                            },
                            label = {
                                Text(
                                    text = "AI Verify",
                                    fontWeight = if (currentTab == AppNavTab.VERIFY) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isAiEnabled) Color.Unspecified else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = if (isAiEnabled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                selectedIconColor = if (isAiEnabled) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                selectedTextColor = if (isAiEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier
                                .testTag("nav_tab_verify")
                                .alpha(if (isAiEnabled) 1f else 0.5f)
                        )

                        NavigationBarItem(
                            selected = currentTab == AppNavTab.INSIGHTS,
                            onClick = { viewModel.selectTab(AppNavTab.INSIGHTS) },
                            icon = {
                                Icon(Icons.Default.TrendingUp, contentDescription = "Insights")
                            },
                            label = {
                                Text(
                                    text = "Insights",
                                    fontWeight = if (currentTab == AppNavTab.INSIGHTS) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                selectedTextColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.testTag("nav_tab_insights")
                        )

                        NavigationBarItem(
                            selected = currentTab == AppNavTab.SETTINGS,
                            onClick = { viewModel.selectTab(AppNavTab.SETTINGS) },
                            icon = {
                                Icon(Icons.Default.Settings, contentDescription = "Settings")
                            },
                            label = {
                                Text(
                                    text = "Settings",
                                    fontWeight = if (currentTab == AppNavTab.SETTINGS) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                selectedTextColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.testTag("nav_tab_settings")
                        )
                    }
                }
            ) { innerPadding ->
                Crossfade(
                    targetState = currentTab,
                    label = "tab_transition",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = innerPadding.calculateBottomPadding())
                ) { tab ->
                    when (tab) {
                        AppNavTab.TASKS -> TasksScreen(viewModel = viewModel)
                        AppNavTab.TIMETABLE -> TimetableScreen(viewModel = viewModel)
                        AppNavTab.VERIFY -> VerifyScreen(viewModel = viewModel)
                        AppNavTab.INSIGHTS -> InsightsScreen(viewModel = viewModel)
                        AppNavTab.SETTINGS -> SettingsScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}
    }
