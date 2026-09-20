package com.example.ui.components

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.example.ui.theme.AmberGold
import com.example.ui.theme.ForestBackground
import com.example.ui.theme.ForestSurface
import com.example.ui.theme.ForestSurfaceBorder
import com.example.ui.theme.ForestSurfaceCard
import com.example.ui.theme.MintPrimary
import com.example.ui.theme.MintPrimaryDark
import com.example.ui.theme.OceanBlue
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.util.CalendarHelper
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, description: String, category: String, priority: String, timeString: String) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Work") }
    var selectedPriority by remember { mutableStateOf("Medium") }
    var addToCalendar by remember { mutableStateOf(false) }

    // Calendar & Clock state
    val calendar = remember { Calendar.getInstance() }
    var selectedDateCalendar by remember { mutableStateOf(Calendar.getInstance()) }
    val dateFormat = remember { SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault()) }
    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }

    var durationMinutes by remember { mutableStateOf(60) }
    var selectedEndTimeCalendar by remember {
        mutableStateOf(
            (Calendar.getInstance().clone() as Calendar).apply { add(Calendar.MINUTE, 60) }
        )
    }

    var selectedDateText by remember { mutableStateOf(dateFormat.format(selectedDateCalendar.time)) }
    var selectedTimeText by remember { mutableStateOf(timeFormat.format(selectedDateCalendar.time)) }
    var selectedEndTimeText by remember { mutableStateOf(timeFormat.format(selectedEndTimeCalendar.time)) }

    // Calendar Permission launcher
    val calendarPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            addToCalendar = true
            Toast.makeText(context, "Calendar permission granted. Task will sync to device calendar.", Toast.LENGTH_SHORT).show()
        } else {
            addToCalendar = false
            Toast.makeText(context, "Calendar permission denied. Task will be saved locally.", Toast.LENGTH_SHORT).show()
        }
    }

    val categories = listOf("Work", "Study", "Health", "Personal")
    val priorities = listOf("High", "Medium", "Low")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .border(1.dp, ForestSurfaceBorder, RoundedCornerShape(28.dp)),
            color = ForestSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Create New Task",
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title", color = TextSecondary) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = MintPrimary,
                        unfocusedBorderColor = ForestSurfaceBorder,
                        focusedContainerColor = ForestSurfaceCard,
                        unfocusedContainerColor = ForestSurfaceCard
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("task_title_input"),
                    shape = RoundedCornerShape(16.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Description Input
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)", color = TextSecondary) },
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = MintPrimary,
                        unfocusedBorderColor = ForestSurfaceBorder,
                        focusedContainerColor = ForestSurfaceCard,
                        unfocusedContainerColor = ForestSurfaceCard
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("task_desc_input"),
                    shape = RoundedCornerShape(16.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Date & Time Picker Section (Calendar & Clock pop-ups)
                Text(
                    text = "Date & Time Schedule",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Date picker card
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(ForestSurfaceCard)
                            .border(1.dp, ForestSurfaceBorder, RoundedCornerShape(14.dp))
                            .clickable {
                                DatePickerDialog(
                                    context,
                                    { _, year, month, dayOfMonth ->
                                        selectedDateCalendar.set(Calendar.YEAR, year)
                                        selectedDateCalendar.set(Calendar.MONTH, month)
                                        selectedDateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                        selectedDateText = dateFormat.format(selectedDateCalendar.time)
                                    },
                                    selectedDateCalendar.get(Calendar.YEAR),
                                    selectedDateCalendar.get(Calendar.MONTH),
                                    selectedDateCalendar.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            }
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                            .testTag("date_picker_btn")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = "Calendar",
                                tint = OceanBlue,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Date",
                                    color = TextTertiary,
                                    fontSize = 10.sp
                                )
                                Text(
                                    text = selectedDateText,
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    // Time clock picker card (Start Time)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(ForestSurfaceCard)
                            .border(1.dp, ForestSurfaceBorder, RoundedCornerShape(14.dp))
                            .clickable {
                                TimePickerDialog(
                                    context,
                                    { _, hourOfDay, minute ->
                                        selectedDateCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                                        selectedDateCalendar.set(Calendar.MINUTE, minute)
                                        selectedTimeText = timeFormat.format(selectedDateCalendar.time)

                                        // Update end time based on selected duration
                                        selectedEndTimeCalendar = (selectedDateCalendar.clone() as Calendar).apply {
                                            add(Calendar.MINUTE, durationMinutes)
                                        }
                                        selectedEndTimeText = timeFormat.format(selectedEndTimeCalendar.time)
                                    },
                                    selectedDateCalendar.get(Calendar.HOUR_OF_DAY),
                                    selectedDateCalendar.get(Calendar.MINUTE),
                                    false
                                ).show()
                            }
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                            .testTag("time_picker_btn")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = "Start Time",
                                tint = OceanBlue,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Start Time",
                                    color = TextTertiary,
                                    fontSize = 10.sp
                                )
                                Text(
                                    text = selectedTimeText,
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Row: End Time Picker & Duration Display
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // End Time picker card
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(ForestSurfaceCard)
                            .border(1.dp, ForestSurfaceBorder, RoundedCornerShape(14.dp))
                            .clickable {
                                TimePickerDialog(
                                    context,
                                    { _, hourOfDay, minute ->
                                        val newEnd = (selectedDateCalendar.clone() as Calendar).apply {
                                            set(Calendar.HOUR_OF_DAY, hourOfDay)
                                            set(Calendar.MINUTE, minute)
                                        }
                                        // If user picks end time earlier than start time, treat as next day
                                        if (newEnd.before(selectedDateCalendar)) {
                                            newEnd.add(Calendar.DAY_OF_YEAR, 1)
                                        }
                                        selectedEndTimeCalendar = newEnd
                                        selectedEndTimeText = timeFormat.format(newEnd.time)
                                        val diff = ((newEnd.timeInMillis - selectedDateCalendar.timeInMillis) / (60 * 1000)).toInt()
                                        durationMinutes = maxOf(15, diff)
                                    },
                                    selectedEndTimeCalendar.get(Calendar.HOUR_OF_DAY),
                                    selectedEndTimeCalendar.get(Calendar.MINUTE),
                                    false
                                ).show()
                            }
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                            .testTag("end_time_picker_btn")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = "End Time",
                                tint = AmberGold,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "End Time",
                                    color = TextTertiary,
                                    fontSize = 10.sp
                                )
                                Text(
                                    text = selectedEndTimeText,
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    // Total Duration Display Card
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(ForestSurfaceCard)
                            .border(1.dp, ForestSurfaceBorder, RoundedCornerShape(14.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MintPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Duration",
                                    color = TextTertiary,
                                    fontSize = 10.sp
                                )
                                Text(
                                    text = "$durationMinutes min",
                                    color = MintPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Quick Durations: 30min, 60min, 90min
                Text(
                    text = "Quick Duration",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(30, 60, 90).forEach { mins ->
                        val isSelected = durationMinutes == mins
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) MintPrimary else ForestSurfaceCard)
                                .border(
                                    1.dp,
                                    if (isSelected) MintPrimary else ForestSurfaceBorder,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    durationMinutes = mins
                                    selectedEndTimeCalendar = (selectedDateCalendar.clone() as Calendar).apply {
                                        add(Calendar.MINUTE, mins)
                                    }
                                    selectedEndTimeText = timeFormat.format(selectedEndTimeCalendar.time)
                                }
                                .padding(vertical = 8.dp)
                                .testTag("duration_${mins}m_btn"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${mins}m",
                                color = if (isSelected) MintPrimaryDark else TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Add to device calendar option with permission request
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(ForestBackground)
                        .clickable {
                            val nextState = !addToCalendar
                            if (nextState) {
                                val hasPermission = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.WRITE_CALENDAR
                                ) == PackageManager.PERMISSION_GRANTED
                                if (!hasPermission) {
                                    calendarPermissionLauncher.launch(Manifest.permission.WRITE_CALENDAR)
                                } else {
                                    addToCalendar = true
                                }
                            } else {
                                addToCalendar = false
                            }
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = addToCalendar,
                        onCheckedChange = { isChecked ->
                            if (isChecked) {
                                val hasPermission = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.WRITE_CALENDAR
                                ) == PackageManager.PERMISSION_GRANTED
                                if (!hasPermission) {
                                    calendarPermissionLauncher.launch(Manifest.permission.WRITE_CALENDAR)
                                } else {
                                    addToCalendar = true
                                }
                            } else {
                                addToCalendar = false
                            }
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = MintPrimary,
                            checkmarkColor = MintPrimaryDark,
                            uncheckedColor = TextTertiary
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Add directly to device Calendar",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Syncs with Google Calendar / system provider",
                            color = TextTertiary,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Category selector
                Text(
                    text = "Category",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { category ->
                        val isSelected = selectedCategory == category
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) MintPrimary else ForestSurfaceCard)
                                .border(
                                    1.dp,
                                    if (isSelected) MintPrimary else ForestSurfaceBorder,
                                    RoundedCornerShape(14.dp)
                                )
                                .clickable { selectedCategory = category }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
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

                Spacer(modifier = Modifier.height(16.dp))

                // Priority level selector
                Text(
                    text = "Priority Level",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    priorities.forEach { priority ->
                        val isSelected = selectedPriority == priority
                        val isHigh = priority == "High"
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) MintPrimary else ForestSurfaceCard)
                                .border(
                                    1.dp,
                                    if (isSelected) MintPrimary else ForestSurfaceBorder,
                                    RoundedCornerShape(14.dp)
                                )
                                .clickable { selectedPriority = priority }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isHigh) "High (AI)" else priority,
                                color = if (isSelected) MintPrimaryDark else TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            val scheduledTimeCombined = "$selectedDateText, $selectedTimeText - $selectedEndTimeText (${durationMinutes}m)"
                            if (addToCalendar) {
                                CalendarHelper.addEventToCalendar(
                                    context = context,
                                    title = title.trim(),
                                    description = description.trim(),
                                    startMillis = selectedDateCalendar.timeInMillis,
                                    durationMinutes = durationMinutes,
                                    endMillisOverride = selectedEndTimeCalendar.timeInMillis
                                )
                            }
                            onConfirm(
                                title,
                                description,
                                selectedCategory,
                                selectedPriority,
                                scheduledTimeCombined
                            )
                        }
                    },
                    enabled = title.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MintPrimary,
                        contentColor = MintPrimaryDark,
                        disabledContainerColor = ForestSurfaceBorder,
                        disabledContentColor = TextTertiary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("confirm_add_task_btn"),
                    shape = RoundedCornerShape(50)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Add to Agenda",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
