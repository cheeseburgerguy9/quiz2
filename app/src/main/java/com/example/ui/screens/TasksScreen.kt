package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import coil.compose.AsyncImage
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.ui.components.AddTaskDialog
import com.example.ui.components.TaskItemCard
import com.example.ui.theme.ExpressiveCardShape
import com.example.ui.theme.ExpressiveButtonShape
import com.example.ui.theme.PillShape
import com.example.ui.theme.PriorityHighColor
import com.example.ui.theme.PriorityLowColor
import com.example.ui.theme.PriorityMediumColor
import com.example.ui.viewmodel.MainViewModel
import java.io.File
import java.util.Calendar

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
    val isAiEnabled by viewModel.isAiEnabled.collectAsState()
    val profile by viewModel.userProfile.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }

    val totalCount = rawTasks.size
    val completedCount = rawTasks.count { it.isCompleted }
    val verifiedCount = rawTasks.count { it.isVerified }
    val uncompletedCount = rawTasks.count { !it.isCompleted }

    val categories = listOf(
        "ALL" to Icons.Default.CheckCircle,
        "Work" to Icons.Default.Work,
        "Study" to Icons.Default.School,
        "Health" to Icons.Default.HealthAndSafety,
        "Personal" to Icons.Default.Person
    )
    val priorities = listOf(
        "ALL" to MaterialTheme.colorScheme.primary,
        "HIGH" to PriorityHighColor,
        "MEDIUM" to PriorityMediumColor,
        "LOW" to PriorityLowColor
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
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
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                HeaderHero(
                    name = profile.name,
                    photoPath = profile.photoPath,
                    isAiEnabled = isAiEnabled
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricMiniCard(
                        title = "Tasks",
                        value = "$totalCount",
                        icon = Icons.Default.CheckCircle,
                        accent = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    MetricMiniCard(
                        title = "Completed",
                        value = "$completedCount",
                        icon = Icons.Default.CalendarMonth,
                        accent = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.weight(1f)
                    )
                    MetricMiniCard(
                        title = "AI Verified",
                        value = "$verifiedCount",
                        icon = Icons.Default.AutoAwesome,
                        accent = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = viewModel::onSearchQueryChange,
                    placeholder = { Text("Search tasks...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear search")
                            }
                        }
                    },
                    shape = PillShape,
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.58f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.58f),
                        focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.55f),
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("task_search_field")
                )
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { (cat, icon) ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { viewModel.onCategoryFilterChange(cat) },
                            label = { Text(if (cat == "ALL") "All" else cat) },
                            leadingIcon = { Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp)) },
                            shape = PillShape
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Priority:",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    priorities.forEach { (prio, accent) ->
                        FilterChip(
                            selected = selectedPriority == prio,
                            onClick = { viewModel.onPriorityFilterChange(prio) },
                            label = { Text(if (prio == "ALL") "All" else prio.lowercase().replaceFirstChar { it.uppercase() }) },
                            leadingIcon = {
                                Box(
                                    Modifier
                                        .size(9.dp)
                                        .background(accent, CircleShape)
                                )
                            },
                            shape = PillShape
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            "Today's Agenda (${tasks.size})",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        if (uncompletedCount > 0) {
                            Text(
                                "$uncompletedCount task${if (uncompletedCount == 1) "" else "s"} still open",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Surface(
                        shape = PillShape,
                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.16f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CalendarMonth, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(7.dp))
                            Text("Today", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }

            if (tasks.isEmpty()) {
                item {
                    EmptyTasksView(
                        onAddTask = { showAddDialog = true },
                        onRunSetup = viewModel::restartSetupWizard
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

            item { Spacer(Modifier.height(92.dp)) }
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

@Composable
private fun HeaderHero(
    name: String,
    photoPath: String?,
    isAiEnabled: Boolean
) {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val (greeting, icon) = when (hour) {
        in 5..11 -> "Good morning" to Icons.Default.WbSunny
        in 12..16 -> "Good afternoon" to Icons.Default.Cloud
        in 17..20 -> "Good evening" to Icons.Default.WbSunny
        else -> "Good night" to Icons.Default.NightsStay
    }
    val initial = name.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "C"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
    ) {
        // Small time-of-day artifact: deliberately subtle and tinted by Material You colors.
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(155.dp)
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.22f), CircleShape)
                .alpha(0.8f)
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 16.dp, end = 28.dp)
                .size(74.dp)
                .background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.30f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = greeting,
                tint = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.75f),
                modifier = Modifier.size(40.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomStart)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Caitlin Daily",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        if (isAiEnabled) "$greeting, ${name.ifBlank { "there" }} • AI Daily Tracker" else "$greeting, ${name.ifBlank { "there" }}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = PillShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.88f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.14f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(38.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            if (!photoPath.isNullOrBlank()) {
                                AsyncImage(
                                    model = File(photoPath),
                                    contentDescription = "Profile photo",
                                    modifier = Modifier.size(38.dp)
                                )
                            } else {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        initial,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(
                            name.ifBlank { "User" },
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricMiniCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(126.dp),
        shape = ExpressiveCardShape,
        colors = CardDefaults.cardColors(
            containerColor = accent.copy(alpha = 0.14f)
        ),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.18f))
    ) {
        Column(
            modifier = Modifier.padding(15.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                shape = PillShape,
                color = accent.copy(alpha = 0.18f)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.padding(7.dp).size(20.dp)
                )
            }
            Spacer(Modifier.height(5.dp))
            Text(
                value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EmptyTasksView(
    onAddTask: () -> Unit,
    onRunSetup: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(92.dp),
                shape = RoundedCornerShape(32.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(52.dp)
                    )
                }
            }
            Spacer(Modifier.height(18.dp))
            Text(
                "Your day is clear!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Add tasks to organize your schedule, sync them with Calendar, and let Gemini optimize your timetable.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                androidx.compose.material3.Button(
                    onClick = onAddTask,
                    shape = ExpressiveButtonShape,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Add, null, Modifier.size(20.dp))
                    Spacer(Modifier.width(7.dp))
                    Text("Add Task")
                }
                androidx.compose.material3.OutlinedButton(
                    onClick = onRunSetup,
                    shape = ExpressiveButtonShape,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.AutoAwesome, null, Modifier.size(19.dp))
                    Spacer(Modifier.width(7.dp))
                    Text("Setup Wizard")
                }
            }
        }
    }
}
