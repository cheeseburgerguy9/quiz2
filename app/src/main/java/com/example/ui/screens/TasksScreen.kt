package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TaskEntity
import com.example.ui.components.AddTaskDialog
import com.example.ui.components.SettingsDialog
import com.example.ui.components.TaskItemCard
import com.example.ui.theme.ExpressiveCardShape
import com.example.ui.theme.GeminiPurple
import com.example.ui.theme.PillShape
import com.example.ui.theme.PriorityHighColor
import com.example.ui.theme.VerifiedGold
import com.example.ui.theme.VerifiedGreen
import com.example.ui.viewmodel.AppNavTab
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val tasks by viewModel.filteredTasks.collectAsState()
    val rawTasks by viewModel.rawTasks.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategoryFilter.collectAsState()
    val selectedPriority by viewModel.selectedPriorityFilter.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showBottomWarningBanner by remember { mutableStateOf(true) }

    val isAiEnabled by viewModel.isAiEnabled.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val studyTimeBetaEnabled by viewModel.studyTimeBetaEnabled.collectAsState()

    val totalCount = rawTasks.size
    val completedCount = rawTasks.count { it.isCompleted }
    val verifiedCount = rawTasks.count { it.isVerified }
    val uncompletedTasks = rawTasks.filter { !it.isCompleted }
    val uncompletedCount = uncompletedTasks.size

    val categories = listOf("ALL", "Work", "Study", "Health", "Personal", "Routine")
    val priorities = listOf("ALL", "HIGH", "MEDIUM", "LOW")

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Caitlin Daily",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        Text(
                            text = if (isAiEnabled) "Your AI Daily Tracker" else "Your Daily Tracker",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            if (uncompletedCount > 0 && showBottomWarningBanner) {
                Surface(
                    shape = ExpressiveCardShape,
                    color = PriorityHighColor,
                    shadowElevation = 6.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .testTag("uncompleted_tasks_bottom_warning_banner")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Default.WarningAmber,
                                contentDescription = "Warning",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "$uncompletedCount Uncompleted Task${if (uncompletedCount > 1) "s" else ""}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                                Text(
                                    text = "High priority items need AI verification",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.9f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        IconButton(
                            onClick = { showBottomWarningBanner = false },
                            modifier = Modifier.size(32.dp).testTag("close_warning_banner_btn")
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Dismiss warning banner",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = PillShape,
                modifier = Modifier.testTag("add_task_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Task")
                    Spacer(Modifier.width(8.dp))
                    Text("New Task", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Metric Cards Row (Expressive super-ellipses)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricMiniCard(
                        title = "Tasks",
                        value = "$totalCount",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    MetricMiniCard(
                        title = "Completed",
                        value = "$completedCount",
                        color = VerifiedGreen,
                        modifier = Modifier.weight(1f)
                    )
                    MetricMiniCard(
                        title = "AI Verified",
                        value = "$verifiedCount",
                        color = VerifiedGold,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Uncompleted Tasks Red Warning Card on the main screen
            if (uncompletedCount > 0) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("uncompleted_tasks_main_warning_card"),
                        shape = ExpressiveCardShape,
                        colors = CardDefaults.cardColors(containerColor = PriorityHighColor.copy(alpha = 0.10f)),
                        border = BorderStroke(1.2.dp, PriorityHighColor.copy(alpha = 0.45f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = PillShape,
                                color = PriorityHighColor.copy(alpha = 0.20f)
                            ) {
                                Icon(
                                    Icons.Default.WarningAmber,
                                    contentDescription = "Pending Warning",
                                    tint = PriorityHighColor,
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .size(22.dp)
                                )
                            }

                            Spacer(Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "$uncompletedCount Uncompleted Task${if (uncompletedCount > 1) "s" else ""} Pending",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = PriorityHighColor
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = "High priority items require AI Screenshot verification to check off.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Search Bar (Clean and breathable)
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChange(it) },
                    placeholder = { Text("Search tasks...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear search")
                            }
                        }
                    },
                    shape = RoundedCornerShape(20.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("task_search_field")
                )
            }

            // Category Filter Chips (Pill shapes with horizontal breathing room)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { viewModel.onCategoryFilterChange(cat) },
                            label = { Text(cat) },
                            shape = PillShape
                        )
                    }
                }
            }

            // Priority Filter Chips
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Priority:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    priorities.forEach { prio ->
                        FilterChip(
                            selected = selectedPriority == prio,
                            onClick = { viewModel.onPriorityFilterChange(prio) },
                            label = { Text(prio) },
                            shape = PillShape
                        )
                    }
                }
            }

            // Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Today's Agenda (${tasks.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Task List
            if (tasks.isEmpty()) {
                item {
                    EmptyTasksView(
                        onAddTask = { showAddDialog = true },
                        onRunSetup = { viewModel.restartSetupWizard() }
                    )
                }
            } else {
                items(tasks, key = { it.id }) { task ->
                    TaskItemCard(
                        task = task,
                        onToggleCompleted = { viewModel.toggleTaskCompletion(task) },
                        onAddToCalendar = { viewModel.addTaskToCalendar(task) },
                        onVerifyScreenshot = { viewModel.prepareTaskForVerification(task) },
                        onDelete = { viewModel.deleteTask(task) }
                    )
                }
            }

            item {
                Spacer(Modifier.height(88.dp))
            }
        }
    }

    if (showAddDialog) {
        AddTaskDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { title, desc, cat, prio, est, time ->
                viewModel.addTask(title, desc, cat, prio, est, time)
                showAddDialog = false
            }
        )
    }


}
