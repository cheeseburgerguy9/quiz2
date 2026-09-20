package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import coil.compose.AsyncImage
import com.example.data.model.UserProfile
import com.example.ui.theme.ExpressiveCardShape
import com.example.ui.theme.GeminiPurple
import com.example.ui.theme.PillShape
import com.example.ui.theme.PriorityHighColor
import com.example.ui.theme.PriorityLowColor
import com.example.ui.theme.PriorityMediumColor
import com.example.ui.viewmodel.MainViewModel

data class WizardDraftTask(
    val title: String,
    val category: String = "Work",
    val priority: String = "HIGH",
    val minutes: Int = 45
)

@Composable
fun SetupWizardScreen(viewModel: MainViewModel, modifier: Modifier = Modifier) {
    val profile by viewModel.userProfile.collectAsState()
    val sleep by viewModel.sleepScheduleInput.collectAsState()
    val meals by viewModel.mealScheduleInput.collectAsState()
    val extra by viewModel.extraConstraintsInput.collectAsState()

    SetupWizardScreen(
        profile = profile,
        initialSleep = sleep,
        initialMeals = meals,
        initialExtra = extra,
        onComplete = { name, age, photo, sleepValue, mealsValue, extraValue, tasks, generate ->
            viewModel.completeSetupWizard(
                name, age, photo, sleepValue, mealsValue, extraValue, tasks, generate
            )
        },
        modifier = modifier
    )
}

@Composable
private fun SetupWizardScreen(
    profile: UserProfile,
    initialSleep: String,
    initialMeals: String,
    initialExtra: String,
    onComplete: (
        String, Int?, Uri?, String, String, String, List<WizardDraftTask>, Boolean
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    var step by remember { mutableIntStateOf(1) }
    var name by remember { mutableStateOf(profile.name) }
    var age by remember { mutableStateOf(profile.age?.toString().orEmpty()) }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var sleep by remember { mutableStateOf(initialSleep) }
    var meals by remember { mutableStateOf(initialMeals) }
    var extra by remember { mutableStateOf(initialExtra) }

    val tasks = remember { mutableStateListOf<WizardDraftTask>() }
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Work") }
    var priority by remember { mutableStateOf("HIGH") }
    var minutes by remember { mutableIntStateOf(45) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        photoUri = it
    }

    val categories = listOf("Work", "Study", "Health", "Personal", "Routine")
    val priorities = listOf("HIGH", "MEDIUM", "LOW")

    Surface(modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            Modifier.fillMaxSize().padding(horizontal = 22.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (step > 1) {
                        IconButton(onClick = { step-- }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                        }
                    } else Spacer(Modifier.size(48.dp))

                    Surface(shape = PillShape, color = MaterialTheme.colorScheme.primaryContainer) {
                        Text(
                            "Step $step of 3",
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(Modifier.size(48.dp))
                }

                Row(
                    Modifier.fillMaxWidth().padding(top = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    (1..3).forEach { i ->
                        Box(
                            Modifier.weight(1f).height(4.dp).clip(PillShape).background(
                                if (i <= step) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }
                }
            }

            AnimatedContent(
                targetState = step,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                modifier = Modifier.weight(1f).fillMaxWidth(),
                label = "setup_steps"
            ) { current ->
                Column(
                    Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(vertical = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    when (current) {
                        1 -> {
                            Text("Welcome to Caitlin Daily", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
                            Text(
                                "Everything is local by default. No Google account or cloud account is required.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Card(
                                shape = ExpressiveCardShape,
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                            ) {
                                Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (photoUri != null) {
                                            AsyncImage(
                                                model = photoUri,
                                                contentDescription = "Selected profile photo",
                                                modifier = Modifier.size(64.dp).clip(CircleShape)
                                            )
                                        } else {
                                            Box(
                                                Modifier.size(64.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                                            }
                                        }
                                        Spacer(Modifier.width(12.dp))
                                        Column {
                                            Text("Offline profile", fontWeight = FontWeight.Bold)
                                            Text(
                                                "Stored only on this device",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    OutlinedTextField(
                                        value = name,
                                        onValueChange = { name = it },
                                        label = { Text("Name") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(18.dp)
                                    )

                                    OutlinedTextField(
                                        value = age,
                                        onValueChange = { value ->
                                            if (value.all(Char::isDigit) && value.length <= 3) age = value
                                        },
                                        label = { Text("Age (optional)") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(18.dp)
                                    )

                                    OutlinedButton(
                                        onClick = { picker.launch("image/*") },
                                        shape = PillShape,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(if (photoUri == null) "Choose profile photo" else "Change profile photo")
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = GeminiPurple.copy(alpha = 0.10f)
                                    ) {
                                        Row(Modifier.padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.AutoAwesome, null, tint = GeminiPurple)
                                            Spacer(Modifier.width(10.dp))
                                            Text(
                                                "Gemini is optional. You will add your own API key later in Settings; this app ships with no API key.",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        2 -> {
                            Text("Your daily rhythm", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
                            Text(
                                "Give Gemini enough context to build realistic schedules. These values remain local.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            RoutineField(Icons.Default.Bedtime, "Sleep", sleep) { sleep = it }
                            RoutineField(Icons.Default.Restaurant, "Meals", meals) { meals = it }
                            RoutineField(Icons.Default.FitnessCenter, "Other constraints", extra) { extra = it }
                        }

                        3 -> {
                            Text("Start with a few tasks", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
                            Text(
                                "You can always add, edit or delete tasks later.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Card(
                                shape = ExpressiveCardShape,
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                            ) {
                                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OutlinedTextField(
                                        value = title,
                                        onValueChange = { title = it },
                                        label = { Text("Task title") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Text("Category", style = MaterialTheme.typography.labelMedium)
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        categories.forEach { value ->
                                            FilterChip(
                                                selected = category == value,
                                                onClick = { category = value },
                                                label = { Text(value) }
                                            )
                                        }
                                    }

                                    Text("Priority", style = MaterialTheme.typography.labelMedium)
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        priorities.forEach { value ->
                                            FilterChip(
                                                selected = priority == value,
                                                onClick = { priority = value },
                                                label = { Text(value) }
                                            )
                                        }
                                    }

                                    Text("Estimated time: $minutes min", style = MaterialTheme.typography.labelMedium)
                                    Slider(
                                        value = minutes.toFloat(),
                                        onValueChange = { minutes = (it / 5).toInt() * 5 },
                                        valueRange = 5f..180f,
                                        steps = 34
                                    )

                                    Button(
                                        onClick = {
                                            if (title.isNotBlank()) {
                                                tasks += WizardDraftTask(title.trim(), category, priority, minutes)
                                                title = ""
                                            }
                                        },
                                        shape = PillShape,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Default.Add, null)
                                        Spacer(Modifier.width(6.dp))
                                        Text("Add task")
                                    }
                                }
                            }

                            tasks.forEachIndexed { index, task ->
                                Card(
                                    shape = RoundedCornerShape(18.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                                ) {
                                    Row(
                                        Modifier.fillMaxWidth().padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val priorityColor = when (task.priority) {
                                            "HIGH" -> PriorityHighColor
                                            "MEDIUM" -> PriorityMediumColor
                                            else -> PriorityLowColor
                                        }
                                        Box(
                                            Modifier.size(10.dp).background(priorityColor, CircleShape)
                                        )
                                        Spacer(Modifier.width(10.dp))
                                        Column(Modifier.weight(1f)) {
                                            Text(task.title, fontWeight = FontWeight.SemiBold)
                                            Text("${task.category} • ${task.priority} • ${task.minutes} min", style = MaterialTheme.typography.bodySmall)
                                        }
                                        IconButton(onClick = { tasks.removeAt(index) }) {
                                            Icon(Icons.Default.Delete, "Remove task")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (step < 3) {
                    Button(
                        onClick = { step++ },
                        enabled = if (step == 1) name.isNotBlank() else true,
                        shape = PillShape,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Continue")
                        Spacer(Modifier.width(6.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, null)
                    }
                } else {
                    Button(
                        onClick = {
                            onComplete(
                                name.trim(),
                                age.toIntOrNull(),
                                photoUri,
                                sleep,
                                meals,
                                extra,
                                tasks.toList(),
                                true
                            )
                        },
                        enabled = name.isNotBlank(),
                        shape = PillShape,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Check, null)
                        Spacer(Modifier.width(6.dp))
                        Text("Finish & build my day")
                    }
                }
            }
        }
    }
}

@Composable
private fun RoutineField(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    Card(
        shape = ExpressiveCardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(10.dp))
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                label = { Text(title) },
                modifier = Modifier.weight(1f),
                minLines = 2,
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}
