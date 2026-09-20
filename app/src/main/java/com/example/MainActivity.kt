package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
        setContent { CaitlinDailyApp() }
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
        notificationMessage?.let {
            snackbarHostState.showSnackbar(it)
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
        Crossfade(targetState = isSetupCompleted, label = "setup_vs_main_transition") { setupDone ->
            if (!setupDone) {
                SetupWizardScreen(viewModel = viewModel)
            } else {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    bottomBar = {
                        Surface(
                            modifier = Modifier
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                                .shadow(8.dp, RoundedCornerShape(28.dp))
                                .clip(RoundedCornerShape(28.dp))
                                .testTag("app_navigation_bar"),
                            shape = RoundedCornerShape(28.dp),
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            tonalElevation = 3.dp
                        ) {
                            NavigationBar(
                                containerColor = Color.Transparent,
                                tonalElevation = 0.dp
                            ) {
                                NavigationBarItem(
                                    selected = currentTab == AppNavTab.TASKS,
                                    onClick = { viewModel.selectTab(AppNavTab.TASKS) },
                                    icon = { Icon(Icons.Default.CheckCircle, "Tasks") },
                                    label = { NavLabel("Tasks", currentTab == AppNavTab.TASKS) },
                                    colors = navColors(),
                                    modifier = Modifier.testTag("nav_tab_tasks")
                                )
                                NavigationBarItem(
                                    selected = currentTab == AppNavTab.TIMETABLE,
                                    onClick = { viewModel.selectTab(AppNavTab.TIMETABLE) },
                                    icon = { Icon(Icons.Default.CalendarMonth, "Timetable") },
                                    label = { NavLabel("Timetable", currentTab == AppNavTab.TIMETABLE) },
                                    colors = navColors(),
                                    modifier = Modifier.testTag("nav_tab_timetable")
                                )
                                NavigationBarItem(
                                    selected = currentTab == AppNavTab.VERIFY,
                                    onClick = {
                                        viewModel.selectTab(AppNavTab.VERIFY)
                                        if (!isAiEnabled) viewModel.showNotification("Turn on Gemini to access AI Verify")
                                    },
                                    icon = { Icon(Icons.Default.Verified, "AI Verify") },
                                    label = { NavLabel("AI Verify", currentTab == AppNavTab.VERIFY) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                        unselectedIconColor = if (isAiEnabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface.copy(alpha = .35f),
                                        unselectedTextColor = if (isAiEnabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface.copy(alpha = .35f)
                                    ),
                                    modifier = Modifier
                                        .testTag("nav_tab_verify")
                                        .alpha(if (isAiEnabled) 1f else 0.5f)
                                )
                                NavigationBarItem(
                                    selected = currentTab == AppNavTab.INSIGHTS,
                                    onClick = { viewModel.selectTab(AppNavTab.INSIGHTS) },
                                    icon = { Icon(Icons.Default.TrendingUp, "Insights") },
                                    label = { NavLabel("Insights", currentTab == AppNavTab.INSIGHTS) },
                                    colors = navColors(),
                                    modifier = Modifier.testTag("nav_tab_insights")
                                )
                                NavigationBarItem(
                                    selected = currentTab == AppNavTab.SETTINGS,
                                    onClick = { viewModel.selectTab(AppNavTab.SETTINGS) },
                                    icon = { Icon(Icons.Default.Person, "Profile") },
                                    label = { NavLabel("Profile", currentTab == AppNavTab.SETTINGS) },
                                    colors = navColors(),
                                    modifier = Modifier.testTag("nav_tab_settings")
                                )
                            }
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

@Composable
private fun NavLabel(text: String, selected: Boolean) {
    Text(
        text,
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
    )
}

@Composable
private fun navColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
    selectedTextColor = MaterialTheme.colorScheme.primary,
    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
)

