package com.example.ui.screens

import com.example.ai.GeminiService

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProductivityInsight
import com.example.ui.components.ProgressGraph
import com.example.ui.components.SettingsDialog
import com.example.ui.theme.ExpressiveCardShape
import com.example.ui.theme.GeminiPurple
import com.example.ui.theme.PillShape
import com.example.ui.theme.PriorityHighColor
import com.example.ui.theme.PriorityLowColor
import com.example.ui.theme.PriorityMediumColor
import com.example.ui.theme.VerifiedGold
import com.example.ui.theme.VerifiedGreen
import com.example.ui.viewmodel.AppNavTab
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val tasks by viewModel.rawTasks.collectAsState()
    val insights by viewModel.productivityInsights.collectAsState()
    val isLoadingInsights by viewModel.isLoadingInsights.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val totalStudyMinutes by viewModel.totalStudyMinutes.collectAsState()
    val studyTimeBetaEnabled by viewModel.studyTimeBetaEnabled.collectAsState()
    val isAiEnabled by viewModel.isAiEnabled.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()

    var showSettingsDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Productivity Insights",
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
                                        text = "AI Coaching",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = GeminiPurple,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                        Text(
                            text = "Prioritization and analytics for ${userProfile.name}",
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
            // Local profile & AI status card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = ExpressiveCardShape,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = userProfile.name.take(1).uppercase(),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(userProfile.name, fontWeight = FontWeight.Bold)
                                Text(
                                    if (userProfile.age != null) "${userProfile.age} years old • Offline account"
                                    else "Offline account",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Surface(
                            shape = PillShape,
                            color = if (GeminiService.isApiKeyConfigured)
                                GeminiPurple.copy(alpha = 0.12f)
                            else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                if (GeminiService.isApiKeyConfigured) "Gemini ready" else "Offline",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Progress Graph Component (Weekly Canvas Bar Chart + Completion Gauge)
            item {
                ProgressGraph(tasks = tasks)
            }

            // Beta Feature: Study Time Display Card
            if (studyTimeBetaEnabled || totalStudyMinutes > 0) {
                item {
                    val hours = totalStudyMinutes / 60
                    val minutes = totalStudyMinutes % 60
                    val formattedDuration = if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("study_time_insights_card"),
                        shape = ExpressiveCardShape,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.28f)
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.HourglassBottom,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(Modifier.width(14.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Logged Study Time",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Surface(
                                            shape = PillShape,
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = "BETA",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = if (totalStudyMinutes > 0)
                                            "Verified from Screen Time / Digital Wellbeing"
                                        else
                                            "No screen time uploaded yet. Go to AI Verify tab.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Surface(
                                shape = PillShape,
                                color = MaterialTheme.colorScheme.primary
                            ) {
                                Text(
                                    text = formattedDuration,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }

            // AI Prioritization & Insights Header + Refresh Action
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "AI Task Prioritization",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Button(
                        onClick = { viewModel.refreshProductivityInsights() },
                        enabled = !isLoadingInsights,
                        colors = ButtonDefaults.buttonColors(containerColor = GeminiPurple),
                        shape = PillShape,
                        modifier = Modifier.testTag("refresh_insights_button")
                    ) {
                        if (isLoadingInsights) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                        }
                        Spacer(Modifier.width(6.dp))
                        Text("Analyze AI", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Eisenhower Matrix Prioritization Cards
            item {
                PrioritizationCards(
                    insights = insights,
                    pendingTasks = tasks.filter { !it.isCompleted }
                )
            }

            // Personalized Productivity Coaching Card
            item {
                ProductivityCoachingCard(insights = insights)
            }

            item {
                Spacer(Modifier.height(88.dp))
            }
        }
    }

    if (showSettingsDialog) {
        SettingsDialog(
            isAiEnabled = isAiEnabled,
            onToggleAi = { viewModel.setAiEnabled(it) },
            themeMode = themeMode,
            onSelectTheme = { viewModel.setThemeMode(it) },
            studyTimeBetaEnabled = studyTimeBetaEnabled,
            onToggleStudyTimeBeta = { viewModel.setStudyTimeBetaEnabled(it) },
            onDismiss = { showSettingsDialog = false }
        )
    }
}

@Composable
private fun PrioritizationCards(
    insights: ProductivityInsight?,
    pendingTasks: List<com.example.data.model.TaskEntity>
) {
    val doFirst = insights?.doFirstTasks?.ifEmpty { null }
        ?: pendingTasks.filter { it.priority == "HIGH" }.map { it.title }
    val schedule = insights?.scheduleTasks?.ifEmpty { null }
        ?: pendingTasks.filter { it.priority == "MEDIUM" }.map { it.title }
    val quickWins = insights?.quickWins?.ifEmpty { null }
        ?: pendingTasks.filter { it.estimatedMinutes <= 25 }.map { it.title }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Do First (High Urgency & Impact)
        PriorityBucketCard(
            title = "Do First (Prime Focus)",
            subtitle = "Highest cognitive impact & priority for today",
            tasks = doFirst,
            badgeColor = PriorityHighColor,
            icon = Icons.Default.Bolt
        )

        // Schedule (Deep Work)
        PriorityBucketCard(
            title = "Schedule (Protected Blocks)",
            subtitle = "High value tasks scheduled in focus window",
            tasks = schedule,
            badgeColor = PriorityMediumColor,
            icon = Icons.Default.Schedule
        )

        // Quick Wins
        PriorityBucketCard(
            title = "Quick Wins (< 25 mins)",
            subtitle = "Fast momentum builders between heavy blocks",
            tasks = quickWins,
            badgeColor = PriorityLowColor,
            icon = Icons.Default.Star
        )
    }
}

@Composable
private fun PriorityBucketCard(
    title: String,
    subtitle: String,
    tasks: List<String>,
    badgeColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = ExpressiveCardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(badgeColor.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = badgeColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (tasks.isEmpty()) {
                Text(
                    text = "None pending in this bucket",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(start = 42.dp)
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    tasks.take(3).forEach { taskTitle ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(start = 42.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(badgeColor, CircleShape)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = taskTitle,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductivityCoachingCard(insights: ProductivityInsight?) {
    val peakWindow = insights?.peakFocusWindow ?: "09:00 AM - 11:30 AM"
    val prediction = insights?.completionPrediction ?: 85
    val tips = insights?.actionableTips ?: listOf(
        "Dedicate your morning peak window (9:00 AM) entirely to your top high-priority task.",
        "Take a 10-minute walk outside after lunch to reset cognitive dopamine.",
        "Upload screenshot proofs after each milestone to let Gemini verify completion."
    )
    val encouragement = insights?.encouragement ?: "Consistent deliberate execution today creates massive freedom tomorrow."

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("productivity_coaching_card"),
        shape = ExpressiveCardShape,
        colors = CardDefaults.cardColors(containerColor = GeminiPurple.copy(alpha = 0.08f)),
        border = BorderStroke(1.dp, GeminiPurple.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = GeminiPurple,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "Personalized AI Coaching",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    shape = PillShape,
                    color = VerifiedGreen.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "$prediction% Day Prediction",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = VerifiedGreen,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            // Peak Focus Window Banner
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Recommended Peak Focus Window",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = peakWindow,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Actionable Tips
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Actionable AI Productivity Tips:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                tips.forEach { tip ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = GeminiPurple,
                            modifier = Modifier
                                .size(16.dp)
                                .padding(top = 2.dp)
                        )
                        Text(
                            text = tip,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Encouragement quote
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "\"$encouragement\"",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(14.dp)
                )
            }
        }
    }
}
