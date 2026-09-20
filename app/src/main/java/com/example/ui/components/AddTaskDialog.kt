package com.example.ui.components

import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.ui.theme.ForestSurface
import com.example.ui.theme.ForestSurfaceBorder
import com.example.ui.theme.ForestSurfaceCard
import com.example.ui.theme.MintPrimary
import com.example.ui.theme.MintPrimaryDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, description: String, category: String, priority: String, scheduledAt: Long, addToCalendar: Boolean) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Work") }
    var selectedPriority by remember { mutableStateOf("Medium") }
    var addToCalendar by remember { mutableStateOf(false) }
    val selectedDateTime = remember { Calendar.getInstance() }
    var dateLabel by remember { mutableStateOf(SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()).format(selectedDateTime.time)) }
    var timeLabel by remember { mutableStateOf(SimpleDateFormat("hh:mm a", Locale.getDefault()).format(selectedDateTime.time)) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    val categories = listOf("Work", "Study", "Health", "Personal")
    val priorities = listOf("High", "Medium", "Low")

    LaunchedEffect(showDatePicker) {
        if (showDatePicker) {
            DatePickerDialog(context, { _, y, m, d -> selectedDateTime.set(y, m, d); dateLabel = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()).format(selectedDateTime.time); showDatePicker = false }, selectedDateTime.get(Calendar.YEAR), selectedDateTime.get(Calendar.MONTH), selectedDateTime.get(Calendar.DAY_OF_MONTH)).show()
        }
    }
    LaunchedEffect(showTimePicker) {
        if (showTimePicker) {
            TimePickerDialog(context, { _, h, m -> selectedDateTime.set(Calendar.HOUR_OF_DAY, h); selectedDateTime.set(Calendar.MINUTE, m); timeLabel = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(selectedDateTime.time); showTimePicker = false }, selectedDateTime.get(Calendar.HOUR_OF_DAY), selectedDateTime.get(Calendar.MINUTE), false).show()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp)).border(1.dp, ForestSurfaceBorder, RoundedCornerShape(28.dp)), color = ForestSurface) {
            Column(Modifier.fillMaxWidth().padding(24.dp)) {
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Text("Create New Task", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, "Close", tint = TextSecondary) }
                }
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Task Title", color = TextSecondary) }, singleLine = true, colors = fieldColors(), modifier = Modifier.fillMaxWidth().testTag("task_title_input"), shape = RoundedCornerShape(16.dp))
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description (Optional)", color = TextSecondary) }, maxLines = 3, colors = fieldColors(), modifier = Modifier.fillMaxWidth().testTag("task_desc_input"), shape = RoundedCornerShape(16.dp))
                Spacer(Modifier.height(16.dp))
                Text("Date & Time", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) {
                    DateTimeButton(Icons.Default.CalendarMonth, dateLabel, Modifier.weight(1f)) { showDatePicker = true }
                    DateTimeButton(Icons.Default.Schedule, timeLabel, Modifier.weight(1f)) { showTimePicker = true }
                }
                Spacer(Modifier.height(16.dp))
                Text("Category", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) { categories.forEach { category -> Chip(category, selectedCategory == category, Modifier.weight(1f)) { selectedCategory = category } } }
                Spacer(Modifier.height(16.dp))
                Text("Priority Level", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) { priorities.forEach { priority -> Chip(priority, selectedPriority == priority, Modifier.weight(1f)) { selectedPriority = priority } } }
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) { Text("Add directly to Google Calendar", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium); Text("Permission will be requested when you add it", color = TextSecondary, fontSize = 11.sp) }
                    Switch(checked = addToCalendar, onCheckedChange = { addToCalendar = it }, colors = SwitchDefaults.colors(checkedThumbColor = MintPrimaryDark, checkedTrackColor = MintPrimary))
                }
                Spacer(Modifier.height(18.dp))
                Button(onClick = { if (title.isNotBlank()) onConfirm(title, description, selectedCategory, selectedPriority, selectedDateTime.timeInMillis, addToCalendar) }, enabled = title.isNotBlank(), colors = ButtonDefaults.buttonColors(containerColor = MintPrimary, contentColor = MintPrimaryDark, disabledContainerColor = ForestSurfaceBorder, disabledContentColor = TextTertiary), modifier = Modifier.fillMaxWidth().height(52.dp).testTag("confirm_add_task_btn"), shape = RoundedCornerShape(50)) {
                    Icon(Icons.Default.Add, null); Spacer(Modifier.width(8.dp)); Text("Add to Agenda", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable private fun fieldColors() = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary, focusedBorderColor = MintPrimary, unfocusedBorderColor = ForestSurfaceBorder, focusedContainerColor = ForestSurfaceCard, unfocusedContainerColor = ForestSurfaceCard)
@Composable private fun DateTimeButton(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, modifier: Modifier, onClick: () -> Unit) { Box(modifier.clip(RoundedCornerShape(14.dp)).background(ForestSurfaceCard).border(1.dp, ForestSurfaceBorder, RoundedCornerShape(14.dp)).clickable { onClick() }.padding(12.dp)) { Row(verticalAlignment = Alignment.CenterVertically) { Icon(icon, null, tint = MintPrimary, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(7.dp)); Text(label, color = TextPrimary, fontSize = 12.sp) } } }
@Composable private fun Chip(text: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) { Box(modifier.clip(RoundedCornerShape(14.dp)).background(if (selected) MintPrimary else ForestSurfaceCard).border(1.dp, if (selected) MintPrimary else ForestSurfaceBorder, RoundedCornerShape(14.dp)).clickable { onClick() }.padding(vertical = 10.dp), Alignment.Center) { Text(text, color = if (selected) MintPrimaryDark else TextSecondary, fontSize = 12.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium) } }
