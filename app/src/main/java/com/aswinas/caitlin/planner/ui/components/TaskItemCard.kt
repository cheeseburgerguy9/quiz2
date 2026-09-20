package com.aswinas.caitlin.planner.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aswinas.caitlin.planner.data.model.TaskEntity
import com.aswinas.caitlin.planner.ui.theme.AmberGold
import com.aswinas.caitlin.planner.ui.theme.CoralRed
import com.aswinas.caitlin.planner.ui.theme.OceanBlue

@Composable
fun TaskItemCard(
    task: TaskEntity,
    onToggleCompleted: () -> Unit,
    onDelete: () -> Unit,
    onRequireAiVerify: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val cardBorderColor by animateColorAsState(
        targetValue = if (task.isCompleted) MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                      else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
        label = "border_color"
    )

    val isHighPriority = task.priority.equals("High", ignoreCase = true)
    val requiresAiVerification = isHighPriority && !task.isCompleted

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                if (task.isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f)
            )
            .border(1.dp, cardBorderColor, RoundedCornerShape(24.dp))
            .padding(18.dp)
            .testTag("task_item_${task.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox button or Lock indicator for High Priority
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            task.isCompleted -> MaterialTheme.colorScheme.primary
                            requiresAiVerification -> CoralRed.copy(alpha = 0.15f)
                            else -> Color.Transparent
                        }
                    )
                    .border(
                        2.dp,
                        when {
                            task.isCompleted -> MaterialTheme.colorScheme.primary
                            requiresAiVerification -> CoralRed.copy(alpha = 0.7f)
                            else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                        },
                        CircleShape
                    )
                    .clickable {
                        if (requiresAiVerification) {
                            onRequireAiVerify()
                        } else {
                            onToggleCompleted()
                        }
                    }
                    .testTag("checkbox_${task.id}"),
                contentAlignment = Alignment.Center
            ) {
                if (task.isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completed",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                } else if (requiresAiVerification) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "AI Verify Required",
                        tint = CoralRed,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            else MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                )

                if (task.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = task.description,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (task.time.isNotBlank() && task.time != "Today") {
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "Scheduled: ${task.time}",
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Category pill
                    val catColor = when (task.category.lowercase()) {
                        "work" -> OceanBlue
                        "study" -> AmberGold
                        "health" -> MaterialTheme.colorScheme.primary
                        else -> CoralRed
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(catColor.copy(alpha = 0.15f))
                            .border(1.dp, catColor.copy(alpha = 0.35f), RoundedCornerShape(50))
                            .padding(horizontal = 9.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = task.category,
                            color = catColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Priority pill
                    val priorityColor = when (task.priority.lowercase()) {
                        "high" -> CoralRed
                        "medium" -> AmberGold
                        else -> OceanBlue
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(priorityColor.copy(alpha = 0.15f))
                            .border(1.dp, priorityColor.copy(alpha = 0.35f), RoundedCornerShape(50))
                            .padding(horizontal = 9.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (isHighPriority) "High (AI Verify)" else task.priority,
                            color = priorityColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // AI Verified Badge
                    if (task.isAiVerified) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(AmberGold.copy(alpha = 0.18f))
                                .border(1.dp, AmberGold.copy(alpha = 0.5f), RoundedCornerShape(50))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "AI Verified",
                                    tint = AmberGold,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = if (task.aiScore > 0) "AI Verified ${task.aiScore}%" else "AI Verified",
                                    color = AmberGold,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.testTag("delete_task_${task.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete Task",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
