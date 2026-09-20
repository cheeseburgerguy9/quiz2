package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TaskEntity
import com.example.ui.theme.ExpressiveCardShape
import com.example.ui.theme.PillShape
import com.example.ui.theme.PriorityHighColor
import com.example.ui.theme.PriorityLowColor
import com.example.ui.theme.PriorityMediumColor
import com.example.ui.theme.VerifiedGold
import com.example.ui.theme.VerifiedGreen

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TaskItemCard(
    task: TaskEntity,
    onToggleCompleted: () -> Unit,
    onAddToCalendar: () -> Unit,
    onVerifyScreenshot: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val priorityColor = when (task.priority) {
        "HIGH" -> PriorityHighColor
        "MEDIUM" -> PriorityMediumColor
        else -> PriorityLowColor
    }

    val cardBg by animateColorAsState(
        targetValue = if (task.isCompleted) {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        label = "task_card_bg"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("task_item_card_${task.id}"),
        shape = ExpressiveCardShape,
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(
            width = if (task.isVerified) 1.5.dp else 1.dp,
            color = if (task.isVerified) VerifiedGold.copy(alpha = 0.7f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (task.isCompleted) 0.dp else 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header: Checkbox + Title + Priority Pill + Delete
            val isHighPriority = task.priority.equals("HIGH", ignoreCase = true)
            val canDirectToggle = !isHighPriority || task.isVerified

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = task.isCompleted,
                    onCheckedChange = {
                        if (canDirectToggle) {
                            onToggleCompleted()
                        } else {
                            onVerifyScreenshot()
                        }
                    },
                    colors = CheckboxDefaults.colors(
                        checkedColor = if (isHighPriority) PriorityHighColor else MaterialTheme.colorScheme.primary,
                        uncheckedColor = if (isHighPriority) PriorityHighColor.copy(alpha = 0.8f) else MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier.testTag("task_checkbox_${task.id}")
                )

                Spacer(Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                        color = if (task.isCompleted) {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (task.description.isNotBlank()) {
                        Spacer(Modifier.height(3.dp))
                        Text(
                            text = task.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(Modifier.width(8.dp))

                // Priority Indicator pill (Android 16/17 Expressive Pill)
                Surface(
                    shape = PillShape,
                    color = priorityColor.copy(alpha = 0.14f),
                    modifier = Modifier.padding(horizontal = 2.dp)
                ) {
                    Text(
                        text = task.priority,
                        color = priorityColor,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.DeleteOutline,
                        contentDescription = "Delete task",
                        tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Tags row: Category, Scheduled Time, Est Duration
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(start = 44.dp)
            ) {
                // Category Chip (Pill shape)
                Surface(
                    shape = PillShape,
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = task.category,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                if (task.scheduledTime.isNotBlank()) {
                    Surface(
                        shape = PillShape,
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                Icons.Default.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = task.scheduledTime,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }

                Surface(
                    shape = PillShape,
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "${task.estimatedMinutes}m",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            // AI Verified Banner if verified
            AnimatedVisibility(visible = task.isVerified) {
                Surface(
                    shape = PillShape,
                    color = VerifiedGold.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, VerifiedGold.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 44.dp, top = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Verified,
                            contentDescription = "AI Verified",
                            tint = VerifiedGold,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Gemini AI Verified (${task.verificationScore ?: 95}%)",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (!task.verificationNotes.isNullOrBlank()) {
                                Text(
                                    text = task.verificationNotes,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            // Action row: "Calendar" button & "Verify AI" button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 44.dp, top = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AssistChip(
                    onClick = onAddToCalendar,
                    label = { Text("Calendar", fontSize = 12.sp, fontWeight = FontWeight.Medium) },
                    shape = PillShape,
                    leadingIcon = {
                        Icon(
                            Icons.Default.CalendarMonth,
                            contentDescription = "Add to Calendar",
                            modifier = Modifier.size(16.dp),
                            tint = if (task.calendarSynced) VerifiedGreen else MaterialTheme.colorScheme.primary
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (task.calendarSynced) VerifiedGreen.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.testTag("add_calendar_btn_${task.id}")
                )

                AssistChip(
                    onClick = onVerifyScreenshot,
                    label = {
                        Text(
                            text = if (task.isVerified) "Re-verify" else "Verify AI",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    },
                    shape = PillShape,
                    leadingIcon = {
                        Icon(
                            if (task.isVerified) Icons.Default.CheckCircle else Icons.Default.PhotoCamera,
                            contentDescription = "Verify screenshot",
                            modifier = Modifier.size(16.dp),
                            tint = if (task.isVerified) VerifiedGreen else MaterialTheme.colorScheme.tertiary
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.testTag("verify_screenshot_btn_${task.id}")
                )
            }
        }
    }
}
