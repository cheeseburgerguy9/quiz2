package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TimetableSlot
import com.example.ui.theme.ExpressiveCardShape
import com.example.ui.theme.GeminiPurple
import com.example.ui.theme.PillShape
import com.example.ui.theme.VerifiedGreen
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimetableScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val timetableSlots by viewModel.timetableSlots.collectAsState()
    val isGenerating by viewModel.isGeneratingTimetable.collectAsState()
    val rawTasks by viewModel.rawTasks.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()

    val sleepInput by viewModel.sleepScheduleInput.collectAsState()
    val mealInput by viewModel.mealScheduleInput.collectAsState()
    val extraInput by viewModel.extraConstraintsInput.collectAsState()

    var showConstraintsPanel by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Daily Timetable",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(Modifier.width(8.dp))
                            Surface(
                                shape = PillShape,
                                color = GeminiPurple.copy(alpha = 0.14f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = GeminiPurple,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = "Gemini Scheduler",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = GeminiPurple,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                        Text(
                            text = "Personalized for ${userProfile.name} • Offline account",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Routine & Constraints Input Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("timetable_constraints_card"),
                    shape = ExpressiveCardShape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showConstraintsPanel = !showConstraintsPanel },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Tune,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Routine & Habits",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (showConstraintsPanel) "Tap to collapse" else "Sleep, meals & breaks used by Gemini",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            IconButton(onClick = { showConstraintsPanel = !showConstraintsPanel }) {
                                Icon(
                                    if (showConstraintsPanel) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = "Toggle constraints"
                                )
                            }
                        }

                        AnimatedVisibility(visible = showConstraintsPanel) {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                OutlinedTextField(
                                    value = sleepInput,
                                    onValueChange = { viewModel.sleepScheduleInput.value = it },
                                    label = { Text("Sleep Schedule (Fixed)") },
                                    leadingIcon = { Icon(Icons.Default.Bedtime, contentDescription = null) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("sleep_input_field")
                                )

                                OutlinedTextField(
                                    value = mealInput,
                                    onValueChange = { viewModel.mealScheduleInput.value = it },
                                    label = { Text("Food & Meals Schedule (Fixed)") },
                                    leadingIcon = { Icon(Icons.Default.Restaurant, contentDescription = null) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("food_input_field")
                                )

                                OutlinedTextField(
                                    value = extraInput,
                                    onValueChange = { viewModel.extraConstraintsInput.value = it },
                                    label = { Text("Workout & Habit Constraints") },
                                    leadingIcon = { Icon(Icons.Default.FitnessCenter, contentDescription = null) },
                                    maxLines = 2,
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("extra_constraints_field")
                                )
                            }
                        }

                        Button(
                            onClick = { viewModel.generateTimetableWithGemini() },
                            enabled = !isGenerating,
                            colors = ButtonDefaults.buttonColors(containerColor = GeminiPurple),
                            shape = PillShape,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("generate_timetable_button")
                        ) {
                            if (isGenerating) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = Color.White,
                                    strokeWidth = 2.5.dp
                                )
                                Spacer(Modifier.width(10.dp))
                                Text("Gemini is Scheduling Your Day...")
                            } else {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Generate Timetable with Gemini", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Timetable Header & Calendar Action
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Daily Timeline (${timetableSlots.size} Blocks)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    if (timetableSlots.isNotEmpty()) {
                        OutlinedButton(
                            onClick = {
                                timetableSlots.firstOrNull()?.let {
                                    viewModel.addTimetableSlotToCalendar(it)
                                }
                            },
                            shape = PillShape
                        ) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Calendar Sync", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            // Timeline Items
            if (timetableSlots.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        shape = ExpressiveCardShape,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                Icons.Default.Schedule,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = "No timetable generated yet",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Tap 'Generate Timetable with Gemini' above to create an optimized schedule tailored to your routine!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(timetableSlots, key = { it.id }) { slot ->
                    TimetableSlotCard(
                        slot = slot,
                        onAddToCalendar = { viewModel.addTimetableSlotToCalendar(slot) }
                    )
                }
            }

            item {
                Spacer(Modifier.height(88.dp))
            }
        }
    }
}

@Composable
fun TimetableSlotCard(
    slot: TimetableSlot,
    onAddToCalendar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (typeColor, typeIcon, typeLabel) = when (slot.type.uppercase()) {
        "SLEEP" -> Triple(Color(0xFF283593), Icons.Default.Bedtime, "Sleep")
        "FOOD" -> Triple(Color(0xFFEF6C00), Icons.Default.Restaurant, "Meal")
        "WORK" -> Triple(MaterialTheme.colorScheme.primary, Icons.Default.Work, "Focus Work")
        "STUDY" -> Triple(Color(0xFF00838F), Icons.Default.MenuBook, "Study")
        "EXERCISE" -> Triple(Color(0xFF2E7D32), Icons.Default.FitnessCenter, "Workout")
        "BREAK" -> Triple(Color(0xFF6A1B9A), Icons.Default.Coffee, "Break")
        else -> Triple(Color(0xFF455A64), Icons.Default.Person, "Personal")
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("timetable_slot_card_${slot.id}"),
        shape = ExpressiveCardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Type Icon Circle
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(typeColor.copy(alpha = 0.14f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    typeIcon,
                    contentDescription = typeLabel,
                    tint = typeColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Time Range Badge (Pill Shape)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = PillShape,
                        color = typeColor.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "${slot.startTime} – ${slot.endTime}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = typeColor,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    Surface(
                        shape = PillShape,
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = typeLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(Modifier.height(6.dp))

                Text(
                    text = slot.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (slot.notes.isNotBlank()) {
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = slot.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            // Calendar Add Quick Button (Pill shape)
            IconButton(
                onClick = onAddToCalendar,
                modifier = Modifier.testTag("add_slot_calendar_${slot.id}")
            ) {
                Icon(
                    Icons.Default.CalendarMonth,
                    contentDescription = "Add to Calendar",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
