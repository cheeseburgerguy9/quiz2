package com.example.ui.screens

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.Image
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TaskEntity
import com.example.ui.theme.AmberGold
import com.example.ui.theme.ForestBackground
import com.example.ui.theme.ForestSurfaceBorder
import com.example.ui.theme.ForestSurfaceCard
import com.example.ui.theme.MintPrimary
import com.example.ui.theme.MintPrimaryContainer
import com.example.ui.theme.MintPrimaryDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@Composable
fun AiVerifyScreen(
    tasks: List<TaskEntity>,
    aiAvailable: Boolean,
    aiBusy: Boolean,
    aiMessage: String,
    onVerifyTask: (TaskEntity, String, ByteArray?) -> Unit,
    studyTimeBetaEnabled: Boolean,
    studyAppName: String,
    onVerifyStudyScreenshot: (ByteArray?, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTask by remember(tasks) { mutableStateOf(tasks.firstOrNull()) }
    var proofText by remember { mutableStateOf("") }
    var studyNote by remember { mutableStateOf("") }
    var proofBytes by remember { mutableStateOf<ByteArray?>(null) }
    var studyBytes by remember { mutableStateOf<ByteArray?>(null) }
    val context = LocalContext.current
    val proofPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> proofBytes = uri?.let { context.contentResolver.openInputStream(it)?.use { input -> input.readBytes() } } }
    val studyPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> studyBytes = uri?.let { context.contentResolver.openInputStream(it)?.use { input -> input.readBytes() } } }

    Surface(modifier.fillMaxSize(), color = ForestBackground) {
        if (!aiAvailable) {
            Box(Modifier.fillMaxSize().padding(24.dp), Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.AutoAwesome, null, tint = TextTertiary, modifier = Modifier.size(44.dp))
                    Spacer(Modifier.height(12.dp)); Text("Gemini is not configured", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("Add and verify your own Gemini API key in Settings to use AI verification.", color = TextSecondary, fontSize = 14.sp)
                }
            }
        } else LazyColumn(Modifier.fillMaxSize().statusBarsPadding(), contentPadding = PaddingValues(20.dp, 16.dp, 20.dp, 96.dp)) {
            item {
                Text("AI Task Verification", color = TextPrimary, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp)); Text("Authenticate completions with Gemini AI verification", color = TextSecondary, fontSize = 14.sp)
                Spacer(Modifier.height(20.dp))
                Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(ForestSurfaceCard).border(1.dp, ForestSurfaceBorder, RoundedCornerShape(24.dp)).padding(20.dp)) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(36.dp).clip(CircleShape).background(AmberGold.copy(alpha=.2f)), Alignment.Center) { Icon(Icons.Default.AutoAwesome, null, tint = AmberGold, modifier = Modifier.size(20.dp)) }; Spacer(Modifier.width(12.dp)); Text("Submit Evidence & Notes", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold) }
                        Spacer(Modifier.height(14.dp)); Text(selectedTask?.let { "Selected Task: ${it.title}" } ?: "Select an objective below to verify", color = MintPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(value = proofText, onValueChange = { proofText = it }, placeholder = { Text("Describe what you completed and what the screenshot proves...", color = TextTertiary, fontSize = 13.sp) }, maxLines = 4, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary, focusedBorderColor = MintPrimary, unfocusedBorderColor = ForestSurfaceBorder, focusedContainerColor = ForestBackground, unfocusedContainerColor = ForestBackground), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth().testTag("verify_proof_input"))
                        Spacer(Modifier.height(10.dp))
                        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(ForestBackground).border(1.dp, ForestSurfaceBorder, RoundedCornerShape(16.dp)).clickable { proofPicker.launch("image/*") }.padding(13.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.AddPhotoAlternate, null, tint = MintPrimary); Spacer(Modifier.width(8.dp)); Text(if (proofBytes == null) "Add completion screenshot" else "Completion screenshot attached", color = TextSecondary, fontSize = 13.sp) }
                        Spacer(Modifier.height(14.dp))
                        Button(onClick = { selectedTask?.let { onVerifyTask(it, proofText, proofBytes); proofText = ""; proofBytes = null } }, enabled = selectedTask != null && proofBytes != null && proofText.isNotBlank() && !aiBusy, colors = ButtonDefaults.buttonColors(containerColor = MintPrimary, contentColor = MintPrimaryDark, disabledContainerColor = ForestSurfaceBorder, disabledContentColor = TextTertiary), shape = RoundedCornerShape(50), modifier = Modifier.fillMaxWidth().height(48.dp).testTag("verify_submit_btn")) { Icon(Icons.Default.Verified, null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text(if (aiBusy) "Checking…" else "Verify with Gemini AI", fontSize = 14.sp, fontWeight = FontWeight.Bold) }
                        if (aiMessage.isNotBlank()) { Spacer(Modifier.height(10.dp)); Text(aiMessage, color = if (aiMessage.contains("verified", true) || aiMessage.contains("Added", true)) MintPrimary else AmberGold, fontSize = 12.sp) }
                    }
                }
                Spacer(Modifier.height(24.dp))
                Text("Select Task to Verify", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold); Spacer(Modifier.height(12.dp))
            }
            if (tasks.isEmpty()) item { Text("No tasks available yet. Add tasks from the Home Agenda to start verifying.", color = TextSecondary, fontSize = 14.sp) }
            else items(tasks) { task ->
                val selected = selectedTask?.id == task.id
                Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(if (selected) MintPrimaryContainer else ForestSurfaceCard).border(1.dp, if (selected) MintPrimary else ForestSurfaceBorder, RoundedCornerShape(18.dp)).clickable { selectedTask = task }.padding(14.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Icon(if (task.isAiVerified) Icons.Default.CheckCircle else Icons.Default.AutoAwesome, null, tint = if (task.isAiVerified) AmberGold else if (selected) MintPrimary else TextTertiary, modifier = Modifier.size(22.dp)); Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) { Text(task.title, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold); Text(if (task.isAiVerified) task.aiFeedback.ifBlank { "AI verified" } else "${task.category} • ${task.priority} Priority", color = if (task.isAiVerified) AmberGold else TextSecondary, fontSize = 12.sp) } }
                }
                Spacer(Modifier.height(8.dp))
            }
            if (studyTimeBetaEnabled) item {
                Spacer(Modifier.height(22.dp))
                Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(ForestSurfaceCard).border(1.dp, ForestSurfaceBorder, RoundedCornerShape(24.dp)).padding(20.dp)) {
                    Column {
                        Text("Study Time • Beta", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp)); Text("Check a screenshot for the app name and visible duration.", color = TextSecondary, fontSize = 12.sp)
                        Spacer(Modifier.height(10.dp)); Text("App to look for: $studyAppName", color = MintPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(10.dp)); OutlinedTextField(value = studyNote, onValueChange = { studyNote = it }, placeholder = { Text("Optional note", color = TextTertiary) }, singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary, focusedBorderColor = MintPrimary, unfocusedBorderColor = ForestSurfaceBorder), modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(10.dp)); Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(ForestBackground).border(1.dp, ForestSurfaceBorder, RoundedCornerShape(16.dp)).clickable { studyPicker.launch("image/*") }.padding(13.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.AddPhotoAlternate, null, tint = MintPrimary); Spacer(Modifier.width(8.dp)); Text(if (studyBytes == null) "Add screenshot" else "Study screenshot attached", color = TextSecondary, fontSize = 13.sp) }
                        Spacer(Modifier.height(10.dp)); Button(onClick = { onVerifyStudyScreenshot(studyBytes, studyNote); studyBytes = null }, enabled = studyBytes != null && !aiBusy && studyAppName.isNotBlank(), colors = ButtonDefaults.buttonColors(containerColor = MintPrimary, contentColor = MintPrimaryDark), shape = RoundedCornerShape(50), modifier = Modifier.fillMaxWidth().height(46.dp)) { Text("Check Study Time", fontWeight = FontWeight.Bold) }
                    }
                }
            }
        }
    }
}
