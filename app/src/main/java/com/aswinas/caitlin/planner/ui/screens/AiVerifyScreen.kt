package com.aswinas.caitlin.planner.ui.screens

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.aswinas.caitlin.planner.data.model.TaskEntity
import com.aswinas.caitlin.planner.ui.theme.AmberGold
import com.aswinas.caitlin.planner.ui.theme.CoralRed

@Composable
fun AiVerifyScreen(
    tasks: List<TaskEntity>,
    selectedTaskFromOutside: TaskEntity?,
    isGeminiAvailable: Boolean,
    isStudyTimeBetaEnabled: Boolean,
    isVerifyingTask: Boolean,
    isExtractingStudyTime: Boolean,
    onVerifyTask: (task: TaskEntity, proof: String, bitmap: Bitmap?, onComplete: (Boolean, String) -> Unit) -> Unit,
    onExtractStudyTime: (appName: String, bitmap: Bitmap, onComplete: (Boolean, String) -> Unit) -> Unit,
    onGoToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var activeMode by remember { mutableIntStateOf(0) } // 0: Verify Task, 1: Study Time Beta

    // Task Verification states
    var selectedTask by remember { mutableStateOf<TaskEntity?>(tasks.firstOrNull()) }
    var proofText by remember { mutableStateOf("") }
    var taskScreenshotUri by remember { mutableStateOf<Uri?>(null) }
    var taskScreenshotBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var lastVerificationFeedback by remember { mutableStateOf<String?>(null) }

    // Study Time Beta states
    var studyAppName by remember { mutableStateOf("Forest") }
    var studyScreenshotUri by remember { mutableStateOf<Uri?>(null) }
    var studyScreenshotBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var lastStudyExtractionMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(selectedTaskFromOutside) {
        if (selectedTaskFromOutside != null) {
            selectedTask = selectedTaskFromOutside
            activeMode = 0
        }
    }

    // Task Photo Picker
    val taskPhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        taskScreenshotUri = uri
        if (uri != null) {
            try {
                taskScreenshotBitmap = if (Build.VERSION.SDK_INT < 28) {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                } else {
                    val source = ImageDecoder.createSource(context.contentResolver, uri)
                    ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                        decoder.isMutableRequired = true
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            taskScreenshotBitmap = null
        }
    }

    // Study Photo Picker
    val studyPhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        studyScreenshotUri = uri
        if (uri != null) {
            try {
                studyScreenshotBitmap = if (Build.VERSION.SDK_INT < 28) {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                } else {
                    val source = ImageDecoder.createSource(context.contentResolver, uri)
                    ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                        decoder.isMutableRequired = true
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            studyScreenshotBitmap = null
        }
    }

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
            // Header
            item {
                Column(modifier = Modifier.padding(top = 6.dp, bottom = 4.dp)) {
                    Text(
                        text = "AI Verification",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Authenticate task outcomes with screenshot evidence",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Gemini API Key Warning Banner if missing
                if (!isGeminiAvailable) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(CoralRed.copy(alpha = 0.12f))
                            .border(1.dp, CoralRed.copy(alpha = 0.35f), RoundedCornerShape(24.dp))
                            .padding(18.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Key,
                                    contentDescription = null,
                                    tint = CoralRed,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Gemini API Key Required",
                                    color = CoralRed,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "All AI vision and verification features are grayed out until you provide your Gemini API key in Settings.",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = onGoToSettings,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CoralRed,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(50),
                                modifier = Modifier.height(40.dp)
                            ) {
                                Text(
                                    text = "Configure Key in Settings",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(18.dp))
                }

                // Mode Selector (Task Verification vs Study Time Beta) - Segmented Style
                if (isStudyTimeBetaEnabled) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(50))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(50))
                            .padding(4.dp)
                    ) {
                        // Tab 1: Task Verification
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(50))
                                .background(
                                    if (activeMode == 0) MaterialTheme.colorScheme.primaryContainer
                                    else Color.Transparent
                                )
                                .clickable { activeMode = 0 }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Verify Tasks",
                                color = if (activeMode == 0) MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 13.sp,
                                fontWeight = if (activeMode == 0) FontWeight.Bold else FontWeight.Medium
                            )
                        }

                        // Tab 2: Study Time Beta
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(50))
                                .background(
                                    if (activeMode == 1) AmberGold.copy(alpha = 0.25f)
                                    else Color.Transparent
                                )
                                .clickable { activeMode = 1 }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Study Time",
                                    color = if (activeMode == 1) AmberGold else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 13.sp,
                                    fontWeight = if (activeMode == 1) FontWeight.Bold else FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(AmberGold.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "BETA",
                                        color = AmberGold,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            // ==========================================
            // MODE 0: TASK VERIFICATION WITH SCREENSHOT
            // ==========================================
            if (activeMode == 0) {
                item {
                    // Verification Submission Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(26.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f))
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), RoundedCornerShape(26.dp))
                            .padding(20.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(AmberGold.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = AmberGold,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Submit Evidence & Notes",
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "High priority tasks strictly require AI verification",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            if (selectedTask != null) {
                                val isHighPriority = selectedTask?.priority.equals("High", ignoreCase = true)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(
                                            if (isHighPriority) CoralRed.copy(alpha = 0.12f)
                                             else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                        )
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (isHighPriority) {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = null,
                                            tint = CoralRed,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                    }
                                    Text(
                                        text = "Verifying: ${selectedTask?.title}",
                                        color = if (isHighPriority) CoralRed else MaterialTheme.colorScheme.onPrimaryContainer,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            } else {
                                Text(
                                    text = "Select a task from below to verify",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 13.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Screenshot Upload / Preview Area
                            Text(
                                text = "Screenshot Proof (Mandatory for High Priority)",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            if (taskScreenshotUri != null) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(140.dp)
                                        .clip(RoundedCornerShape(18.dp))
                                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), RoundedCornerShape(18.dp))
                                ) {
                                    Image(
                                        painter = rememberAsyncImagePainter(model = taskScreenshotUri),
                                        contentDescription = "Uploaded Screenshot",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                    IconButton(
                                        onClick = {
                                            taskScreenshotUri = null
                                            taskScreenshotBitmap = null
                                        },
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(6.dp)
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Remove",
                                            tint = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), RoundedCornerShape(18.dp))
                                        .clickable(enabled = isGeminiAvailable) {
                                            taskPhotoPickerLauncher.launch(
                                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                            )
                                        }
                                        .padding(vertical = 18.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.AddPhotoAlternate,
                                            contentDescription = "Attach Screenshot",
                                            tint = if (isGeminiAvailable) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Attach Screenshot Evidence",
                                            color = if (isGeminiAvailable) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Notes Input
                            OutlinedTextField(
                                value = proofText,
                                onValueChange = { proofText = it },
                                placeholder = {
                                    Text(
                                        "Describe outcome, paste link, or accomplishments...",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                        fontSize = 13.sp
                                    )
                                },
                                enabled = isGeminiAvailable,
                                maxLines = 3,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                    disabledTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                ),
                                shape = RoundedCornerShape(18.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("verify_proof_input")
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Verification Submit Button (Grayed out if no API key or no task)
                            val isHigh = selectedTask?.priority.equals("High", ignoreCase = true)
                            val canSubmit = isGeminiAvailable &&
                                    selectedTask != null &&
                                    !isVerifyingTask &&
                                    (!isHigh || taskScreenshotBitmap != null)

                            Button(
                                onClick = {
                                    selectedTask?.let { task ->
                                        onVerifyTask(task, proofText, taskScreenshotBitmap) { success, msg ->
                                            lastVerificationFeedback = msg
                                            if (success) {
                                                proofText = ""
                                                taskScreenshotUri = null
                                                taskScreenshotBitmap = null
                                                Toast.makeText(context, "Task AI Verified successfully!", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    }
                                },
                                enabled = canSubmit,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary,
                                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                shape = RoundedCornerShape(50),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("verify_submit_btn")
                            ) {
                                if (isVerifyingTask) {
                                    CircularProgressIndicator(
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Verifying with Gemini...", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (!isGeminiAvailable) "Configure API Key in Settings" else "Verify with Gemini AI",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            if (lastVerificationFeedback != null) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(AmberGold.copy(alpha = 0.15f))
                                        .padding(14.dp)
                                ) {
                                    Text(
                                        text = "AI Result: $lastVerificationFeedback",
                                        color = AmberGold,
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Select Task to Verify",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (tasks.isEmpty()) {
                    item {
                        Text(
                            text = "No tasks available yet. Add tasks from the Home Agenda to start verifying.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    items(tasks) { task ->
                        val isSelected = selectedTask?.id == task.id
                        val isHigh = task.priority.equals("High", ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                    RoundedCornerShape(20.dp)
                                )
                                .clickable { selectedTask = task }
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (task.isAiVerified) Icons.Default.CheckCircle else if (isHigh) Icons.Default.Lock else Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = if (task.isAiVerified) AmberGold else if (isHigh) CoralRed else if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = task.title,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        if (isHigh && !task.isAiVerified) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(50))
                                                    .background(CoralRed.copy(alpha = 0.2f))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "AI REQUIRED",
                                                    color = CoralRed,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                    if (task.isAiVerified && task.aiFeedback.isNotBlank()) {
                                        Text(
                                            text = task.aiFeedback,
                                            color = AmberGold,
                                            fontSize = 12.sp,
                                            lineHeight = 16.sp
                                        )
                                    } else {
                                        Text(
                                            text = "${task.category} • ${task.priority} Priority",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }

            // ====================================================
            // MODE 1: STUDY TIME EXTRACTION FROM SCREENSHOT (BETA)
            // ====================================================
            if (activeMode == 1 && isStudyTimeBetaEnabled) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(26.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f))
                            .border(1.dp, AmberGold.copy(alpha = 0.35f), RoundedCornerShape(26.dp))
                            .padding(20.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(AmberGold.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Timer,
                                        contentDescription = null,
                                        tint = AmberGold,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Extract Study Time",
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(50))
                                                .background(AmberGold.copy(alpha = 0.2f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "BETA",
                                                color = AmberGold,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    Text(
                                        text = "Upload screenshot mentioning the app name to parse hours",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Target App Name input
                            Text(
                                text = "Target App Name to Inspect",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            OutlinedTextField(
                                value = studyAppName,
                                onValueChange = { studyAppName = it },
                                placeholder = {
                                    Text(
                                        "e.g. Forest, Anki, Duolingo, Notion, YouTube...",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                },
                                singleLine = true,
                                enabled = isGeminiAvailable,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    focusedBorderColor = AmberGold,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                                    focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                                ),
                                shape = RoundedCornerShape(18.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("study_app_name_input")
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Screenshot Picker / Preview for Study Time
                            Text(
                                text = "App Screen Time Screenshot",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            if (studyScreenshotUri != null) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(150.dp)
                                        .clip(RoundedCornerShape(18.dp))
                                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), RoundedCornerShape(18.dp))
                                ) {
                                    Image(
                                        painter = rememberAsyncImagePainter(model = studyScreenshotUri),
                                        contentDescription = "Study Screenshot",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                    IconButton(
                                        onClick = {
                                            studyScreenshotUri = null
                                            studyScreenshotBitmap = null
                                        },
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(6.dp)
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Remove",
                                            tint = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), RoundedCornerShape(18.dp))
                                        .clickable(enabled = isGeminiAvailable) {
                                            studyPhotoPickerLauncher.launch(
                                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                            )
                                        }
                                        .padding(vertical = 20.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.AddPhotoAlternate,
                                            contentDescription = "Upload Screen Time",
                                            tint = if (isGeminiAvailable) AmberGold else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = "Upload Screen Time / Study Screenshot",
                                            color = if (isGeminiAvailable) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            // Extract Study Time Button
                            val canExtract = isGeminiAvailable &&
                                    studyScreenshotBitmap != null &&
                                    studyAppName.isNotBlank() &&
                                    !isExtractingStudyTime

                            Button(
                                onClick = {
                                    studyScreenshotBitmap?.let { bmp ->
                                        onExtractStudyTime(studyAppName, bmp) { success, msg ->
                                            lastStudyExtractionMessage = msg
                                            if (success) {
                                                studyScreenshotUri = null
                                                studyScreenshotBitmap = null
                                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    }
                                },
                                enabled = canExtract,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AmberGold,
                                    contentColor = Color.Black,
                                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                shape = RoundedCornerShape(50),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("extract_study_time_btn")
                            ) {
                                if (isExtractingStudyTime) {
                                    CircularProgressIndicator(
                                        color = Color.Black,
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Analyzing Screenshot...", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (!isGeminiAvailable) "Configure API Key in Settings" else "Analyze & Add to Study Time",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            if (lastStudyExtractionMessage != null) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                                        .padding(14.dp)
                                ) {
                                    Text(
                                        text = lastStudyExtractionMessage ?: "",
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
