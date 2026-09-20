package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.TaskEntity
import com.example.ui.theme.ExpressiveCardShape
import com.example.ui.theme.PillShape
import com.example.ui.theme.VerifiedGold
import com.example.ui.theme.VerifiedGreen

@Composable
fun ProgressGraph(
    tasks: List<TaskEntity>,
    modifier: Modifier = Modifier
) {
    val totalTasks = tasks.size
    val completedTasks = tasks.count { it.isCompleted }
    val verifiedTasks = tasks.count { it.isVerified }
    val completionRatio = if (totalTasks > 0) completedTasks.toFloat() / totalTasks else 0f
    val verifiedRatio = if (totalTasks > 0) verifiedTasks.toFloat() / totalTasks else 0f

    val animatedCompletion by animateFloatAsState(
        targetValue = completionRatio,
        animationSpec = tween(1000),
        label = "completion_anim"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = ExpressiveCardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header: Title + Streak badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.TrendingUp,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.size(10.dp))
                    Text(
                        text = "Productivity Analytics",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Surface(
                    shape = PillShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.LocalFireDepartment,
                            contentDescription = "Streak",
                            tint = Color(0xFFFF6D00),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.size(4.dp))
                        Text(
                            text = "Daily Streak",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // Central Metric: Circular Progress Gauge + Stats Summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Circular Gauge
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(110.dp)
                ) {
                    val primaryColor = MaterialTheme.colorScheme.primary
                    val trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                    val verifiedColor = VerifiedGold

                    Canvas(modifier = Modifier.size(100.dp)) {
                        val strokeWidth = 10.dp.toPx()
                        // Track
                        drawCircle(
                            color = trackColor,
                            radius = size.minDimension / 2 - strokeWidth / 2,
                            style = Stroke(width = strokeWidth)
                        )
                        // Completed Arc
                        drawArc(
                            color = primaryColor,
                            startAngle = -90f,
                            sweepAngle = animatedCompletion * 360f,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                        // Verified inner subtle arc
                        if (verifiedRatio > 0f) {
                            drawArc(
                                color = verifiedColor,
                                startAngle = -90f,
                                sweepAngle = (verifiedRatio * 360f).coerceAtMost(animatedCompletion * 360f),
                                useCenter = false,
                                style = Stroke(width = strokeWidth * 0.45f, cap = StrokeCap.Round)
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${(animatedCompletion * 100).toInt()}%",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Done",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Metric Counters
                Column(
                    modifier = Modifier.padding(start = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricCounterRow(
                        label = "Total Tasks",
                        value = "$totalTasks",
                        color = MaterialTheme.colorScheme.primary
                    )
                    MetricCounterRow(
                        label = "Completed",
                        value = "$completedTasks",
                        color = VerifiedGreen
                    )
                    MetricCounterRow(
                        label = "AI Verified",
                        value = "$verifiedTasks",
                        color = VerifiedGold,
                        icon = {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = VerifiedGold,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    )
                }
            }

            // 7-Day Completion Bar Chart (Mon - Sun)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Weekly Activity Distribution",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                WeeklyBarChart(
                    completedToday = completedTasks,
                    totalToday = totalTasks
                )
            }
        }
    }
}

@Composable
private fun MetricCounterRow(
    label: String,
    value: String,
    color: Color,
    icon: (@Composable () -> Unit)? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape)
        )
        if (icon != null) {
            icon()
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.weight(1f, fill = false))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun WeeklyBarChart(
    completedToday: Int,
    totalToday: Int,
    modifier: Modifier = Modifier
) {
    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val plannedValues = listOf(5, 6, 4, 7, 6, 3, totalToday.coerceAtLeast(4))
    val completedValues = listOf(4, 5, 4, 6, 5, 2, completedToday)

    var selectedDayIndex by remember { mutableIntStateOf(6) }

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val outlineVariant = MaterialTheme.colorScheme.outlineVariant

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val barWidth = 24.dp.toPx()
                val spacing = (size.width - (barWidth * days.size)) / (days.size + 1)
                val maxPlanned = 8f

                // Draw background grid lines
                for (i in 1..3) {
                    val y = size.height * (i / 4f)
                    drawLine(
                        color = outlineVariant.copy(alpha = 0.25f),
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // Draw bars
                days.indices.forEach { index ->
                    val x = spacing + index * (barWidth + spacing)
                    val planned = plannedValues[index]
                    val completed = completedValues[index]

                    val plannedHeight = (planned / maxPlanned) * (size.height - 20.dp.toPx())
                    val completedHeight = (completed / maxPlanned) * (size.height - 20.dp.toPx())

                    val barBottom = size.height - 10.dp.toPx()

                    // Planned bar (background ghost)
                    drawRoundRect(
                        color = surfaceVariant.copy(alpha = 0.8f),
                        topLeft = Offset(x, barBottom - plannedHeight),
                        size = Size(barWidth, plannedHeight),
                        cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                    )

                    // Completed bar (gradient fill)
                    val isToday = index == 6
                    val barBrush = Brush.verticalGradient(
                        colors = if (isToday) {
                            listOf(primaryColor, primaryColor.copy(alpha = 0.6f))
                        } else {
                            listOf(secondaryColor, secondaryColor.copy(alpha = 0.6f))
                        }
                    )

                    drawRoundRect(
                        brush = barBrush,
                        topLeft = Offset(x, barBottom - completedHeight),
                        size = Size(barWidth, completedHeight.coerceAtLeast(4.dp.toPx())),
                        cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                    )
                }
            }
        }

        // Day Labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            days.forEachIndexed { index, day ->
                val isSelected = index == selectedDayIndex
                Text(
                    text = day,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.clickable { selectedDayIndex = index }
                )
            }
        }

        // Selected Day Details Banner (Pill shape)
        Surface(
            shape = PillShape,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${days[selectedDayIndex]}: ${completedValues[selectedDayIndex]} of ${plannedValues[selectedDayIndex]} tasks done",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${((completedValues[selectedDayIndex].toFloat() / plannedValues[selectedDayIndex]) * 100).toInt()}%",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
