package com.example.ui.screens

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
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TaskEntity
import com.example.ui.components.HeaderTimeArtifact
import com.example.ui.components.TaskItemCard
import com.example.ui.theme.AmberGold
import com.example.ui.theme.AmberGoldBorder
import com.example.ui.theme.AmberGoldContainer
import com.example.ui.theme.CoralRed
import com.example.ui.theme.ForestBackground
import com.example.ui.theme.ForestSurfaceBorder
import com.example.ui.theme.ForestSurfaceCard
import com.example.ui.theme.MintPrimary
import com.example.ui.theme.MintPrimaryContainer
import com.example.ui.theme.MintPrimaryDark
import com.example.ui.theme.OceanBlue
import com.example.ui.theme.OceanBlueBorder
import com.example.ui.theme.OceanBlueContainer
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@Composable
fun TasksScreen(
    tasks: List<TaskEntity>,
    totalCount: Int,
    completedCount: Int,
    aiVerifiedCount: Int,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedCategory: String,
    onSelectCategory: (String) -> Unit,
    selectedPriority: String,
    onSelectPriority: (String) -> Unit,
    onToggleCompleted: (TaskEntity) -> Unit,
    onDeleteTask: (TaskEntity) -> Unit,
    onAddNewTaskClick: () -> Unit,
    onOpenSetupWizard: () -> Unit,
    onProfileClick: () -> Unit,
    userName: String = "Aswin",
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = ForestBackground
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 96.dp)
            ) {
                // 1. Top Header with Time of the Day Background Artifact
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                    ) {
                        // Background Artifact (tinted with Material You colors based on time of day)
                        HeaderTimeArtifact(modifier = Modifier.fillMaxSize())

                        // Top Header Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .statusBarsPadding()
                                .padding(horizontal = 20.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Caitlin Daily",
                                    color = TextPrimary,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = (-0.5).sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Your AI Daily Tracker",
                                    color = TextSecondary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Normal
                                )
                            }

                            // Profile Pill
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(ForestSurfaceCard)
                                    .border(1.dp, ForestSurfaceBorder, RoundedCornerShape(50))
                                    .clickable { onProfileClick() }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                    .testTag("header_profile_pill"),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(MintPrimary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = userName.firstOrNull()?.uppercase() ?: "A",
                                        color = MintPrimaryDark,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = userName,
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Menu",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                // 2. Metric Summary Cards (Tasks, Completed, AI Verified)
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Card 1: Tasks
                        MetricCard(
                            count = totalCount,
                            label = "Tasks",
                            icon = Icons.Default.Check,
                            iconColor = MintPrimary,
                            containerColor = MintPrimaryContainer,
                            borderColor = MintPrimary.copy(alpha = 0.35f),
                            modifier = Modifier.weight(1f)
                        )

                        // Card 2: Completed
                        MetricCard(
                            count = completedCount,
                            label = "Completed",
                            icon = Icons.Default.CalendarToday,
                            iconColor = OceanBlue,
                            containerColor = OceanBlueContainer,
                            borderColor = OceanBlueBorder,
                            modifier = Modifier.weight(1f)
                        )

                        // Card 3: AI Verified
                        MetricCard(
                            count = aiVerifiedCount,
                            label = "AI Verified",
                            icon = Icons.Default.AutoAwesome,
                            iconColor = AmberGold,
                            containerColor = AmberGoldContainer,
                            borderColor = AmberGoldBorder,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // 3. Search Bar
                item {
                    Spacer(modifier = Modifier.height(18.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = onSearchQueryChange,
                            placeholder = { Text("Search tasks...", color = TextTertiary) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = TextSecondary
                                )
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedBorderColor = MintPrimary,
                                unfocusedBorderColor = ForestSurfaceBorder,
                                focusedContainerColor = ForestSurfaceCard,
                                unfocusedContainerColor = ForestSurfaceCard
                            ),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("search_tasks_input")
                        )
                    }
                }

                // 4. Category Filter Chips
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    val categories = listOf(
                        Triple("All", Icons.Default.GridView, "All"),
                        Triple("Work", Icons.Default.Work, "Work"),
                        Triple("Study", Icons.Default.School, "Study"),
                        Triple("Health", Icons.Default.Favorite, "Health"),
                        Triple("Personal", Icons.Default.Person, "Personal")
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categories.forEach { (catName, icon, key) ->
                            val isSelected = selectedCategory == key
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(if (isSelected) MintPrimary else ForestSurfaceCard)
                                    .border(
                                        1.dp,
                                        if (isSelected) MintPrimary else ForestSurfaceBorder,
                                        RoundedCornerShape(50)
                                    )
                                    .clickable { onSelectCategory(key) }
                                    .padding(horizontal = 14.dp, vertical = 9.dp)
                                    .testTag("category_chip_$key"),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = if (isSelected) MintPrimaryDark else TextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = catName,
                                        color = if (isSelected) MintPrimaryDark else TextSecondary,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }

                // 5. Priority Filter Row
                item {
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Priority:",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )

                        // All Priority
                        PriorityChip(
                            label = "All",
                            icon = Icons.AutoMirrored.Filled.TrendingUp,
                            iconColor = if (selectedPriority == "All") MintPrimaryDark else MintPrimary,
                            isSelected = selectedPriority == "All",
                            onClick = { onSelectPriority("All") }
                        )

                        // High Priority
                        PriorityChip(
                            label = "High",
                            icon = Icons.Default.ArrowUpward,
                            iconColor = CoralRed,
                            isSelected = selectedPriority == "High",
                            onClick = { onSelectPriority("High") }
                        )

                        // Medium Priority
                        PriorityChip(
                            label = "Medium",
                            icon = Icons.Default.Remove,
                            iconColor = AmberGold,
                            isSelected = selectedPriority == "Medium",
                            onClick = { onSelectPriority("Medium") }
                        )

                        // Low Priority
                        PriorityChip(
                            label = "Low",
                            icon = Icons.Default.Circle,
                            iconColor = OceanBlue,
                            isSelected = selectedPriority == "Low",
                            onClick = { onSelectPriority("Low") }
                        )
                    }
                }

                // 6. Today's Agenda Section Header
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Today's Agenda (${tasks.size})",
                            color = TextPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )

                        // Today Dropdown
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(ForestSurfaceCard)
                                .border(1.dp, ForestSurfaceBorder, RoundedCornerShape(50))
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Today",
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // 7. Tasks List or Empty State Card (Exact Match to Screenshot!)
                if (tasks.isEmpty()) {
                    item {
                        EmptyAgendaCard(
                            onAddTask = onAddNewTaskClick,
                            onOpenSetupWizard = onOpenSetupWizard
                        )
                    }
                } else {
                    items(tasks, key = { it.id }) { task ->
                        Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
                            TaskItemCard(
                                task = task,
                                onToggleCompleted = { onToggleCompleted(task) },
                                onDelete = { onDeleteTask(task) }
                            )
                        }
                    }
                }
            }

            // Extended Floating Action Button at Bottom Right ("+ New Task")
            FloatingActionButton(
                onClick = onAddNewTaskClick,
                containerColor = MintPrimary,
                contentColor = MintPrimaryDark,
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 20.dp)
                    .testTag("fab_new_task")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New Task",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "New Task",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricCard(
    count: Int,
    label: String,
    icon: ImageVector,
    iconColor: Color,
    containerColor: Color,
    borderColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .background(containerColor)
            .border(1.dp, borderColor, RoundedCornerShape(22.dp))
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
                        .background(iconColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = iconColor.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = count.toString(),
                color = TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun PriorityChip(
    label: String,
    icon: ImageVector,
    iconColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (isSelected) MintPrimary else ForestSurfaceCard)
            .border(
                1.dp,
                if (isSelected) MintPrimary else ForestSurfaceBorder,
                RoundedCornerShape(50)
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 7.dp)
            .testTag("priority_chip_$label"),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) MintPrimaryDark else iconColor,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                color = if (isSelected) MintPrimaryDark else TextSecondary,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

/**
 * Empty Agenda Card matching the screenshot perfectly:
 * - Illustration: Rounded clipboard with checkmark and sparkles
 * - Headline: "Your day is clear!"
 * - Description: "Add tasks to organize your schedule, sync them with Google Calendar, and let Gemini AI optimize your timetable."
 * - Buttons: "+ Add Task" and "✨ Setup Wizard"
 */
@Composable
private fun EmptyAgendaCard(
    onAddTask: () -> Unit,
    onOpenSetupWizard: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(ForestSurfaceCard)
            .border(1.dp, ForestSurfaceBorder, RoundedCornerShape(28.dp))
            .padding(28.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Stylized Clipboard Graphic with Checkmark & Stars matching screenshot
            Box(
                modifier = Modifier.size(120.dp),
                contentAlignment = Alignment.Center
            ) {
                // Organic background ambient glow
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(MintPrimary.copy(alpha = 0.08f))
                )
                // Outer clipboard card
                Box(
                    modifier = Modifier
                        .size(width = 68.dp, height = 86.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MintPrimaryContainer)
                        .border(2.dp, MintPrimary.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = MintPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }
                // Ambient sparkles
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = MintPrimary,
                    modifier = Modifier
                        .size(18.dp)
                        .align(Alignment.TopEnd)
                )
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = OceanBlue,
                    modifier = Modifier
                        .size(14.dp)
                        .align(Alignment.BottomStart)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Your day is clear!",
                color = TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Add tasks to organize your schedule, sync them with Google Calendar, and let Gemini AI optimize your timetable.",
                color = TextSecondary,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons: + Add Task & ✨ Setup Wizard
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // + Add Task Button
                Button(
                    onClick = onAddTask,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MintPrimary,
                        contentColor = MintPrimaryDark
                    ),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("empty_state_add_task_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Add Task",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // ✨ Setup Wizard Button
                OutlinedButton(
                    onClick = onOpenSetupWizard,
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = TextPrimary
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ForestSurfaceBorder),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("empty_state_setup_wizard_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MintPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Setup Wizard",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
