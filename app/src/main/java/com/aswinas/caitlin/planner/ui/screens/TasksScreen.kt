package com.aswinas.caitlin.planner.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import android.app.DatePickerDialog
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.Upcoming
import androidx.compose.material.icons.filled.WorkOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.aswinas.caitlin.planner.data.model.TaskEntity
import com.aswinas.caitlin.planner.data.model.UserProfile
import com.aswinas.caitlin.planner.util.HapticFeedbackHelper
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import com.aswinas.caitlin.planner.ui.components.AddTaskDialog
import com.aswinas.caitlin.planner.ui.components.HeaderTimeArtifact
import com.aswinas.caitlin.planner.ui.components.HighPriorityAiVerifyRequiredDialog
import com.aswinas.caitlin.planner.ui.components.TaskItemCard
import com.aswinas.caitlin.planner.ui.theme.AmberGold
import com.aswinas.caitlin.planner.ui.theme.CoralRed
import com.aswinas.caitlin.planner.ui.theme.OceanBlue
import com.aswinas.caitlin.planner.util.TimeOfDayHelper

data class CategoryItem(val name: String, val icon: ImageVector)
data class PriorityItem(val name: String, val color: Color)

enum class TaskDateFilter {
    YESTERDAY,
    TODAY,
    TOMORROW,
    CUSTOM,
    ALL
}

data class DateFilterItem(
    val type: TaskDateFilter,
    val label: String,
    val icon: ImageVector
)

private fun matchesCalendarDay(task: TaskEntity, targetCal: Calendar, relativeLabel: String?): Boolean {
    val targetYear = targetCal.get(Calendar.YEAR)
    val targetDoy = targetCal.get(Calendar.DAY_OF_YEAR)

    if (relativeLabel != null && task.time.contains(relativeLabel, ignoreCase = true)) {
        return true
    }

    if (task.dueDateMillis > 0L) {
        val taskCal = Calendar.getInstance().apply { timeInMillis = task.dueDateMillis }
        if (taskCal.get(Calendar.YEAR) == targetYear && taskCal.get(Calendar.DAY_OF_YEAR) == targetDoy) {
            return true
        }
    }

    val sdf = SimpleDateFormat("MMM d", Locale.getDefault())
    val targetFormatted = sdf.format(targetCal.time)
    if (task.time.contains(targetFormatted, ignoreCase = true)) {
        return true
    }

    if (task.dueDateMillis == 0L || task.dueDateMillis == task.createdAt) {
        val createdCal = Calendar.getInstance().apply { timeInMillis = task.createdAt }
        if (createdCal.get(Calendar.YEAR) == targetYear && createdCal.get(Calendar.DAY_OF_YEAR) == targetDoy) {
            return true
        }
    }

    return false
}

@Composable
fun TasksScreen(
    tasks: List<TaskEntity>,
    userProfile: UserProfile,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    selectedPriority: String = "All",
    onPrioritySelected: (String) -> Unit = {},
    totalCount: Int,
    completedCount: Int,
    aiVerifiedCount: Int,
    yesterdayIncompleteTasks: List<TaskEntity> = emptyList(),
    onMoveYesterdayTaskToToday: (TaskEntity) -> Unit = {},
    onCompleteYesterdayTask: (TaskEntity) -> Unit = {},
    onDismissYesterdayReminder: (Long) -> Unit = {},
    onToggleCompleted: (TaskEntity) -> Unit,
    onDeleteTask: (TaskEntity) -> Unit,
    onRequireAiVerify: (TaskEntity) -> Unit,
    onAddNewTask: (String, String, String, String, String, String, List<Long>) -> Unit,
    onOpenProfile: () -> Unit,
    onLaunchSetupWizard: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isAddTaskDialogOpen by remember { mutableStateOf(false) }
    var highPriorityPromptTask by remember { mutableStateOf<TaskEntity?>(null) }
    var selectedDateFilter by remember { mutableStateOf(TaskDateFilter.TODAY) }
    var selectedCustomDateMillis by remember { mutableStateOf<Long?>(null) }

    val displayedTasks = remember(tasks, selectedDateFilter, selectedCustomDateMillis) {
        val now = Calendar.getInstance()
        when (selectedDateFilter) {
            TaskDateFilter.ALL -> tasks
            TaskDateFilter.TODAY -> {
                tasks.filter { matchesCalendarDay(it, now, "Today") }
            }
            TaskDateFilter.YESTERDAY -> {
                val yesterdayCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
                tasks.filter { matchesCalendarDay(it, yesterdayCal, "Yesterday") }
            }
            TaskDateFilter.TOMORROW -> {
                val tomorrowCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
                tasks.filter { matchesCalendarDay(it, tomorrowCal, "Tomorrow") }
            }
            TaskDateFilter.CUSTOM -> {
                if (selectedCustomDateMillis == null) {
                    tasks
                } else {
                    val targetCal = Calendar.getInstance().apply { timeInMillis = selectedCustomDateMillis!! }
                    tasks.filter { matchesCalendarDay(it, targetCal, null) }
                }
            }
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        val currentPeriod = remember { TimeOfDayHelper.getCurrentPeriod() }

        Box(modifier = Modifier.fillMaxSize()) {
            // Minor artifact in the background of top header based on time of the day
            // Tinted with Material You theme colors and seamless organic contour
            HeaderTimeArtifact(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
                    .align(Alignment.TopEnd)
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 100.dp)
            ) {
                // Top Header Row:
                // Title "Caitlin Daily", "Your AI Daily Tracker" + Profile button on right
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Caitlin Daily",
                                color = MaterialTheme.colorScheme.onBackground,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.5).sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Your AI Daily Tracker",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 14.sp
                            )
                        }

                        // Profile badge pill button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f))
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), RoundedCornerShape(50))
                                .clickable { onOpenProfile() }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                                .testTag("home_avatar_btn")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (!userProfile.photoUri.isNullOrBlank()) {
                                        Image(
                                            painter = rememberAsyncImagePainter(model = userProfile.photoUri),
                                            contentDescription = "Avatar",
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Text(
                                            text = userProfile.name.firstOrNull()?.uppercase() ?: "U",
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Text(
                                    text = if (userProfile.name.isNotBlank()) userProfile.name else "Profile",
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )

                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // 3 Metric Cards Row (Green/Mint, Blue, Amber/Gold) matching screenshot
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Card 1: Tasks (Mint)
                        MetricHeroCard(
                            count = totalCount,
                            label = "Tasks",
                            icon = Icons.Default.Check,
                            accentColor = MaterialTheme.colorScheme.primary,
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                            modifier = Modifier.weight(1f)
                        )

                        // Card 2: Completed (Blue)
                        MetricHeroCard(
                            count = completedCount,
                            label = "Completed",
                            icon = Icons.Default.CalendarMonth,
                            accentColor = OceanBlue,
                            containerColor = OceanBlue.copy(alpha = 0.18f),
                            modifier = Modifier.weight(1f)
                        )

                        // Card 3: AI Verified (Amber Gold)
                        MetricHeroCard(
                            count = aiVerifiedCount,
                            label = "AI Verified",
                            icon = Icons.Default.AutoAwesome,
                            accentColor = AmberGold,
                            containerColor = AmberGold.copy(alpha = 0.18f),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    // Search tasks... input matching screenshot
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = {
                            Text(
                                "Search tasks...",
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                fontSize = 14.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                        ),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("task_search_bar")
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Date Filter Buttons (Yesterday, Today, Tomorrow, Custom, All)
                    val customDateLabel = remember(selectedCustomDateMillis, selectedDateFilter) {
                        if (selectedDateFilter == TaskDateFilter.CUSTOM && selectedCustomDateMillis != null) {
                            SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(selectedCustomDateMillis!!))
                        } else {
                            "Custom"
                        }
                    }

                    val dateFilterItems = listOf(
                        DateFilterItem(TaskDateFilter.YESTERDAY, "Yesterday", Icons.Default.History),
                        DateFilterItem(TaskDateFilter.TODAY, "Today", Icons.Default.Today),
                        DateFilterItem(TaskDateFilter.TOMORROW, "Tomorrow", Icons.Default.Upcoming),
                        DateFilterItem(TaskDateFilter.CUSTOM, customDateLabel, Icons.Default.CalendarMonth),
                        DateFilterItem(TaskDateFilter.ALL, "All", Icons.Default.GridView)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        dateFilterItems.forEach { item ->
                            val isSelected = selectedDateFilter == item.type
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                        RoundedCornerShape(50)
                                    )
                                    .clickable {
                                        if (item.type == TaskDateFilter.CUSTOM) {
                                            HapticFeedbackHelper.vibrateClick(context)
                                            val initialCal = Calendar.getInstance()
                                            if (selectedCustomDateMillis != null) {
                                                initialCal.timeInMillis = selectedCustomDateMillis!!
                                            }
                                            DatePickerDialog(
                                                context,
                                                { _, year, month, dayOfMonth ->
                                                    val chosenCal = Calendar.getInstance().apply {
                                                        set(Calendar.YEAR, year)
                                                        set(Calendar.MONTH, month)
                                                        set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                                        set(Calendar.HOUR_OF_DAY, 0)
                                                        set(Calendar.MINUTE, 0)
                                                        set(Calendar.SECOND, 0)
                                                        set(Calendar.MILLISECOND, 0)
                                                    }
                                                    selectedCustomDateMillis = chosenCal.timeInMillis
                                                    selectedDateFilter = TaskDateFilter.CUSTOM
                                                    HapticFeedbackHelper.vibrateSelection(context)
                                                },
                                                initialCal.get(Calendar.YEAR),
                                                initialCal.get(Calendar.MONTH),
                                                initialCal.get(Calendar.DAY_OF_MONTH)
                                            ).show()
                                        } else {
                                            HapticFeedbackHelper.vibrateTick(context)
                                            selectedDateFilter = item.type
                                        }
                                    }
                                    .padding(horizontal = 16.dp, vertical = 10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                                ) {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = null,
                                        tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = item.label,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Row 2: Priority Filter Pills (Priority: All, High, Medium, Low)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Priority:",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(end = 4.dp)
                        )

                        val priorityItems = listOf(
                            PriorityItem("All", MaterialTheme.colorScheme.primary),
                            PriorityItem("High", CoralRed),
                            PriorityItem("Medium", AmberGold),
                            PriorityItem("Low", OceanBlue)
                        )

                        priorityItems.forEach { item ->
                            val isSelected = selectedPriority.equals(item.name, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(
                                        if (isSelected) item.color.copy(alpha = 0.25f)
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) item.color
                                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                        RoundedCornerShape(50)
                                    )
                                    .clickable {
                                        HapticFeedbackHelper.vibrateTick(context)
                                        onPrioritySelected(item.name)
                                    }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(item.color)
                                    )
                                    Text(
                                        text = item.name,
                                        color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    // Incomplete Tasks from Yesterday Reminder Banner
                    if (yesterdayIncompleteTasks.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(20.dp))
                        val firstYesterdayTask = yesterdayIncompleteTasks.first()
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(22.dp))
                                .background(CoralRed.copy(alpha = 0.12f))
                                .border(1.dp, CoralRed.copy(alpha = 0.4f), RoundedCornerShape(22.dp))
                                .padding(16.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(CoralRed.copy(alpha = 0.2f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.History,
                                                contentDescription = null,
                                                tint = CoralRed,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Incomplete from Yesterday (${yesterdayIncompleteTasks.size})",
                                            color = CoralRed,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            HapticFeedbackHelper.vibrateTick(context)
                                            onDismissYesterdayReminder(firstYesterdayTask.id)
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Dismiss",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "\"${firstYesterdayTask.title}\"${if (yesterdayIncompleteTasks.size > 1) " and ${yesterdayIncompleteTasks.size - 1} other(s)" else ""}",
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )

                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            HapticFeedbackHelper.vibrateClick(context)
                                            onMoveYesterdayTaskToToday(firstYesterdayTask)
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            contentColor = MaterialTheme.colorScheme.onPrimary
                                        ),
                                        shape = RoundedCornerShape(50),
                                        modifier = Modifier.weight(1f).height(38.dp)
                                    ) {
                                        Text("Move to Today", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            HapticFeedbackHelper.vibrateSuccess(context)
                                            onCompleteYesterdayTask(firstYesterdayTask)
                                        },
                                        shape = RoundedCornerShape(50),
                                        modifier = Modifier.weight(1f).height(38.dp)
                                    ) {
                                        Text("Mark Done", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    } else {
                        // Dedicated breathing room between Priority filter row and Agenda Section Header
                        Spacer(modifier = Modifier.height(28.dp))
                    }

                    // Section Title: Contextual date title with expressive count badge and clean inline "+ New Task" button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = when (selectedDateFilter) {
                                    TaskDateFilter.TODAY -> "Today"
                                    TaskDateFilter.YESTERDAY -> "Yesterday"
                                    TaskDateFilter.TOMORROW -> "Tomorrow"
                                    TaskDateFilter.CUSTOM -> {
                                        if (selectedCustomDateMillis != null) {
                                            SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(selectedCustomDateMillis!!))
                                        } else "Custom Date"
                                    }
                                    TaskDateFilter.ALL -> "All Tasks"
                                },
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = (-0.4).sp
                            )

                            // Expressive Count Pill Badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.75f))
                                    .padding(horizontal = 10.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "${displayedTasks.size}",
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Clean inline action button matching Material Expressive UI (No overlapping, perfectly placed!)
                        FilledTonalButton(
                            onClick = {
                                HapticFeedbackHelper.vibrateClick(context)
                                isAddTaskDialogOpen = true
                            },
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                            modifier = Modifier.testTag("add_task_fab")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "New Task",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                }

                // Agenda Content: Empty State Card matching screenshot OR Task List
                if (displayedTasks.isEmpty()) {
                    item {
                        // Empty State Card matching screenshot:
                        // Rounded card, clipboard with checkmark & sparkles,
                        // Contextual description, [+ Add Task] and [✨ Setup Wizard] buttons
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(28.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(28.dp))
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Spacer(modifier = Modifier.height(10.dp))

                                // Visual clipboard illustration
                                Box(
                                    modifier = Modifier
                                        .size(110.dp)
                                        .clip(RoundedCornerShape(26.dp))
                                        .background(
                                            Brush.radialGradient(
                                                colors = listOf(
                                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                                                    Color.Transparent
                                                )
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    // Soft clipboard badge
                                    Box(
                                        modifier = Modifier
                                            .size(72.dp)
                                            .clip(RoundedCornerShape(18.dp))
                                            .background(MaterialTheme.colorScheme.surface)
                                            .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(18.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(36.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                val emptyHeading = when (selectedDateFilter) {
                                    TaskDateFilter.TODAY -> "Your day is clear!"
                                    TaskDateFilter.YESTERDAY -> "No tasks from yesterday"
                                    TaskDateFilter.TOMORROW -> "No tasks scheduled for tomorrow"
                                    TaskDateFilter.CUSTOM -> {
                                        val dateStr = if (selectedCustomDateMillis != null) {
                                            SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(selectedCustomDateMillis!!))
                                        } else "selected date"
                                        "No tasks on $dateStr"
                                    }
                                    TaskDateFilter.ALL -> "No tasks found"
                                }

                                Text(
                                    text = emptyHeading,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Add tasks to organize your schedule, sync them with Google Calendar, and let Gemini AI optimize your timetable.",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 13.sp,
                                    lineHeight = 19.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                // 2 Action Buttons Row: [+ Add Task] (mint pill) and [✨ Setup Wizard] (outlined pill)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            HapticFeedbackHelper.vibrateClick(context)
                                            isAddTaskDialogOpen = true
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            contentColor = MaterialTheme.colorScheme.onPrimary
                                        ),
                                        shape = RoundedCornerShape(50),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp)
                                            .testTag("empty_add_task_btn")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Add Task",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            HapticFeedbackHelper.vibrateClick(context)
                                            onLaunchSetupWizard()
                                        },
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                                        ),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                                            contentColor = MaterialTheme.colorScheme.onSurface
                                        ),
                                        shape = RoundedCornerShape(50),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp)
                                            .testTag("empty_setup_wizard_btn")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = AmberGold,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Setup Wizard",
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                            }
                        }
                    }
                } else {
                    items(displayedTasks, key = { it.id }) { task ->
                        TaskItemCard(
                            task = task,
                            onToggleCompleted = {
                                val isHigh = task.priority.equals("High", ignoreCase = true)
                                if (isHigh && !task.isCompleted) {
                                    HapticFeedbackHelper.vibrateClick(context)
                                    highPriorityPromptTask = task
                                } else {
                                    if (!task.isCompleted) {
                                        HapticFeedbackHelper.vibrateSuccess(context)
                                    } else {
                                        HapticFeedbackHelper.vibrateTick(context)
                                    }
                                    onToggleCompleted(task)
                                }
                            },
                            onDelete = {
                                HapticFeedbackHelper.vibrateClick(context)
                                onDeleteTask(task)
                            },
                            onRequireAiVerify = {
                                HapticFeedbackHelper.vibrateClick(context)
                                highPriorityPromptTask = task
                            }
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                    }
                }
            }
        }

        // Add Task Dialog (with Calendar & Clock popups)
        if (isAddTaskDialogOpen) {
            AddTaskDialog(
                onDismiss = { isAddTaskDialogOpen = false },
                onConfirm = { title, description, category, priority, time, recurrence, selectedDates ->
                    HapticFeedbackHelper.vibrateSuccess(context)
                    onAddNewTask(title, description, category, priority, time, recurrence, selectedDates)
                    isAddTaskDialogOpen = false
                }
            )
        }

        // High Priority Blocked Dialog
        if (highPriorityPromptTask != null) {
            HighPriorityAiVerifyRequiredDialog(
                task = highPriorityPromptTask!!,
                onDismiss = { highPriorityPromptTask = null },
                onGoToAiVerify = { task ->
                    val target = task
                    highPriorityPromptTask = null
                    onRequireAiVerify(target)
                }
            )
        }
    }
}

/**
 * Metric Card (Tasks, Completed, AI Verified) with count, label, and arrow
 */
@Composable
private fun MetricHeroCard(
    count: Int,
    label: String,
    icon: ImageVector,
    accentColor: Color,
    containerColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .background(containerColor)
            .border(1.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(22.dp))
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(15.dp)
                    )
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = accentColor.copy(alpha = 0.8f),
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "$count",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = label,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
        }
    }
}
