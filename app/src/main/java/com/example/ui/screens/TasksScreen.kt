package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.data.model.TaskEntity
import com.example.data.model.UserProfile
import com.example.ui.components.AddTaskDialog
import com.example.ui.components.HighPriorityAiVerifyRequiredDialog
import com.example.ui.components.TaskItemCard
import com.example.ui.components.TimeOfDayArtwork
import com.example.ui.theme.AmberGold
import com.example.ui.theme.ForestBackground
import com.example.ui.theme.ForestSurfaceBorder
import com.example.ui.theme.ForestSurfaceCard
import com.example.ui.theme.MintPrimary
import com.example.ui.theme.MintPrimaryContainer
import com.example.ui.theme.MintPrimaryDark
import com.example.ui.theme.OceanBlue
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.TimeOfDayHelper
import com.example.util.TimeOfDayPeriod
import com.example.ui.theme.TextTertiary

@Composable
fun TasksScreen(
    tasks: List<TaskEntity>,
    userProfile: UserProfile,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    totalCount: Int,
    completedCount: Int,
    aiVerifiedCount: Int,
    onToggleCompleted: (TaskEntity) -> Unit,
    onDeleteTask: (TaskEntity) -> Unit,
    onRequireAiVerify: (TaskEntity) -> Unit,
    onAddNewTask: (title: String, description: String, category: String, priority: String, time: String) -> Unit,
    onOpenProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isAddTaskDialogOpen by remember { mutableStateOf(false) }
    var highPriorityPromptTask by remember { mutableStateOf<TaskEntity?>(null) }

    val categories = listOf("All", "Work", "Study", "Health", "Personal")

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        val currentPeriod = remember { TimeOfDayHelper.getCurrentPeriod() }
        val greetingSubtext = remember(currentPeriod) { TimeOfDayHelper.getGreetingSubtext(currentPeriod) }

        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 100.dp)
            ) {
                // Greeting & Avatar Header with Faded Artwork
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(22.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                RoundedCornerShape(22.dp)
                            )
                            .padding(horizontal = 18.dp, vertical = 16.dp)
                    ) {
                        // Minor faded artwork behind the greeting
                        TimeOfDayArtwork(
                            period = currentPeriod,
                            modifier = Modifier.matchParentSize()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Hello, ${userProfile.name} 👋",
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when (currentPeriod) {
                                                    TimeOfDayPeriod.MORNING -> AmberGold
                                                    TimeOfDayPeriod.AFTERNOON -> OceanBlue
                                                    TimeOfDayPeriod.EVENING -> Color(0xFFFF8A65)
                                                    TimeOfDayPeriod.NIGHT -> MintPrimary
                                                }
                                            )
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = greetingSubtext,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Avatar button
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(MintPrimaryContainer)
                                    .border(2.dp, MintPrimary, CircleShape)
                                    .clickable { onOpenProfile() }
                                    .testTag("home_avatar_btn"),
                                contentAlignment = Alignment.Center
                            ) {
                                if (!userProfile.photoUri.isNullOrBlank()) {
                                    Image(
                                        painter = rememberAsyncImagePainter(model = userProfile.photoUri),
                                        contentDescription = "Avatar",
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Text(
                                        text = userProfile.name.firstOrNull()?.uppercase() ?: "A",
                                        color = MintPrimary,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Quick Progress Bar / Metric Pills
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Total
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(ForestSurfaceCard)
                                .border(1.dp, ForestSurfaceBorder, RoundedCornerShape(16.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text("Total", color = TextSecondary, fontSize = 11.sp)
                                Text("$totalCount", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        // Completed
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(ForestSurfaceCard)
                                .border(1.dp, ForestSurfaceBorder, RoundedCornerShape(16.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text("Done", color = MintPrimary, fontSize = 11.sp)
                                Text("$completedCount", color = MintPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        // AI Verified
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(ForestSurfaceCard)
                                .border(1.dp, ForestSurfaceBorder, RoundedCornerShape(16.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text("AI Verified", color = AmberGold, fontSize = 11.sp)
                                Text("$aiVerifiedCount", color = AmberGold, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Search input
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = { Text("Search your tasks & milestones...", color = TextTertiary, fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = TextSecondary,
                                modifier = Modifier.size(20.dp)
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
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("task_search_bar")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Category Filter Pills
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categories.forEach { category ->
                            val isSelected = selectedCategory.equals(category, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(if (isSelected) MintPrimary else ForestSurfaceCard)
                                    .border(
                                        1.dp,
                                        if (isSelected) MintPrimary else ForestSurfaceBorder,
                                        RoundedCornerShape(50)
                                    )
                                    .clickable { onCategorySelected(category) }
                                    .padding(horizontal = 14.dp, vertical = 7.dp)
                            ) {
                                Text(
                                    text = category,
                                    color = if (isSelected) MintPrimaryDark else TextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }

                // Tasks List
                if (tasks.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MintPrimary.copy(alpha = 0.5f),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "All clear! No tasks found.",
                                    color = TextSecondary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Tap the + button below to create one.",
                                    color = TextTertiary,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                } else {
                    items(tasks, key = { it.id }) { task ->
                        TaskItemCard(
                            task = task,
                            onToggleCompleted = { onToggleCompleted(task) },
                            onDelete = { onDeleteTask(task) },
                            onRequireAiVerify = {
                                // For high-priority tasks: ai verify is the only way to mark completed
                                highPriorityPromptTask = task
                            }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }

            // Floating Action Button
            FloatingActionButton(
                onClick = { isAddTaskDialogOpen = true },
                containerColor = MintPrimary,
                contentColor = MintPrimaryDark,
                shape = CircleShape,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 24.dp, bottom = 100.dp)
                    .size(60.dp)
                    .testTag("fab_add_task")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Task",
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        // Add Task Dialog (with Calendar & Clock popups)
        if (isAddTaskDialogOpen) {
            AddTaskDialog(
                onDismiss = { isAddTaskDialogOpen = false },
                onConfirm = { title, description, category, priority, time ->
                    onAddNewTask(title, description, category, priority, time)
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
