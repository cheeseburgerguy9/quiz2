package com.aswinas.caitlin.planner.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aswinas.caitlin.planner.data.model.TimetableSlot
import com.aswinas.caitlin.planner.ui.components.EditTimetableSlotDialog
import com.aswinas.caitlin.planner.ui.components.HeaderTimeArtifact
import com.aswinas.caitlin.planner.ui.theme.AmberGold
import com.aswinas.caitlin.planner.ui.theme.CoralRed
import com.aswinas.caitlin.planner.ui.theme.EmeraldGreen
import com.aswinas.caitlin.planner.ui.theme.OceanBlue

@Composable
fun TimetableScreen(
    slots: List<TimetableSlot>,
    presetSlots: List<TimetableSlot> = emptyList(),
    isGeminiAvailable: Boolean,
    onOptimizeSchedule: () -> Unit,
    onToggleAlert: (TimetableSlot) -> Unit,
    onUpdateSlot: (TimetableSlot) -> Unit,
    onAddSlot: (TimetableSlot) -> Unit,
    onDeleteSlot: (String) -> Unit,
    onSavePreset: () -> Unit = {},
    onResetPreset: () -> Unit = {},
    onGoToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    var editingSlot by remember { mutableStateOf<TimetableSlot?>(null) }
    var isAddSlotDialogOpen by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 96.dp)
        ) {
            item {
                Column(modifier = Modifier.padding(top = 6.dp, bottom = 4.dp)) {
                    Text(
                        text = "Timetable & Flow",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Preset baseline alignment with AI chrono-adaptation",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Time artifact hero
                HeaderTimeArtifact(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Action Bar: AI Optimize with Today's Tasks + Manual Add Slot
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // AI Adapt Button
                    Button(
                        onClick = {
                            if (isGeminiAvailable) {
                                onOptimizeSchedule()
                            } else {
                                onGoToSettings()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isGeminiAvailable) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (isGeminiAvailable) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("optimize_timetable_btn")
                    ) {
                        Icon(
                            imageVector = if (isGeminiAvailable) Icons.Default.AutoAwesome else Icons.Default.Key,
                            contentDescription = null,
                            tint = if (isGeminiAvailable) MaterialTheme.colorScheme.onPrimaryContainer else AmberGold,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isGeminiAvailable) "AI Adapt for Today" else "Setup API Key",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Add Slot Manually Button
                    Button(
                        onClick = { isAddSlotDialogOpen = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .height(50.dp)
                            .testTag("manual_add_slot_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Slot",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Add Slot",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Preset Controls Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(18.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Bookmark,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Preset Timetable Baseline",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Fixed slots are guarded during AI adaptation",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            // Reset to Preset
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { onResetPreset() }
                                    .padding(horizontal = 8.dp, vertical = 5.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(13.dp), tint = MaterialTheme.colorScheme.onSurface)
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("Reset", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                                }
                            }

                            // Save as Preset
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                    .clickable { onSavePreset() }
                                    .padding(horizontal = 8.dp, vertical = 5.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Save Preset", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Today's Schedule (${slots.size} events)",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Tap to edit slot",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            items(slots, key = { it.id }) { slot ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f))
                        .border(
                            1.dp,
                            if (slot.alertEnabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                            else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                            RoundedCornerShape(22.dp)
                        )
                        .clickable { editingSlot = slot }
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Time Icon Box
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    if (slot.alertEnabled) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceVariant
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = if (slot.alertEnabled) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = slot.timeLabel,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (slot.isPresetFixed) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(50))
                                            .background(OceanBlue.copy(alpha = 0.15f))
                                            .padding(horizontal = 6.dp, vertical = 1.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Lock, contentDescription = null, tint = OceanBlue, modifier = Modifier.size(9.dp))
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Text("Fixed Preset", fontSize = 9.sp, color = OceanBlue, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = slot.taskTitle ?: "Free Focus Slot",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val categoryName = slot.category ?: "Focus"
                                val catColor = when (categoryName.lowercase()) {
                                    "work" -> OceanBlue
                                    "study" -> AmberGold
                                    "health" -> EmeraldGreen
                                    else -> CoralRed
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(catColor.copy(alpha = 0.15f))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = categoryName,
                                        color = catColor,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                if (slot.isAiOptimized) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(50))
                                            .background(AmberGold.copy(alpha = 0.15f))
                                            .border(1.dp, AmberGold.copy(alpha = 0.4f), RoundedCornerShape(50))
                                            .padding(horizontal = 7.dp, vertical = 2.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.AutoAwesome,
                                                contentDescription = null,
                                                tint = AmberGold,
                                                modifier = Modifier.size(10.dp)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = "AI Arranged",
                                                color = AmberGold,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Action Controls: Notification Alert Bell + Edit Button
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Notification Alert Button
                            IconButton(
                                onClick = { onToggleAlert(slot) },
                                modifier = Modifier
                                    .size(38.dp)
                                    .testTag("toggle_alert_${slot.id}")
                            ) {
                                Icon(
                                    imageVector = if (slot.alertEnabled) Icons.Default.Notifications else Icons.Default.NotificationsOff,
                                    contentDescription = if (slot.alertEnabled) "Alert enabled" else "Alert disabled",
                                    tint = if (slot.alertEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Edit Slot Button
                            IconButton(
                                onClick = { editingSlot = slot },
                                modifier = Modifier
                                    .size(38.dp)
                                    .testTag("edit_slot_${slot.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Slot",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        // Dialog for Manual Add
        if (isAddSlotDialogOpen) {
            EditTimetableSlotDialog(
                initialSlot = null,
                onDismiss = { isAddSlotDialogOpen = false },
                onSave = { newSlot ->
                    onAddSlot(newSlot)
                    isAddSlotDialogOpen = false
                }
            )
        }

        // Dialog for Edit Slot
        if (editingSlot != null) {
            EditTimetableSlotDialog(
                initialSlot = editingSlot,
                onDismiss = { editingSlot = null },
                onSave = { updatedSlot ->
                    onUpdateSlot(updatedSlot)
                    editingSlot = null
                },
                onDelete = { slotId ->
                    onDeleteSlot(slotId)
                    editingSlot = null
                }
            )
        }
    }
}
