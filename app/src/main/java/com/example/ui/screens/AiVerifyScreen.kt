package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.data.model.TaskEntity
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CoralRed
import com.example.ui.theme.ForestBackground
import com.example.ui.theme.ForestSurfaceBorder
import com.example.ui.theme.ForestSurfaceCard
import com.example.ui.theme.MintPrimary
import com.example.ui.theme.MintPrimaryContainer
import com.example.ui.theme.MintPrimaryDark
import com.example.ui.theme.OceanBlue
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

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

    // Study Time Photo Picker
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
        color = ForestBackground
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 96.dp)
        ) {
            // Header
            item {
                Column {
                    Text(
                        text = "AI Verification & Vision",
                        color = TextPrimary,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Authenticate task outcomes with screenshot evidence",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Gemini API Key Warning Banner if missing
                if (!isGeminiAvailable) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(CoralRed.copy(alpha = 0.12f))
                            .border(1.dp, CoralRed.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                            .padding(16.dp)
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
                                color = TextPrimary,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = onGoToSettings,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CoralRed,
                                    contentColor = ForestBackground
                                ),
                                shape = RoundedCornerShape(50),
                                modifier = Modifier.height(38.dp)
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

                // Mode Selector (Task Verification vs Study Time Beta)
                if (isStudyTimeBetaEnabled) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(50))
                            .background(ForestSurfaceCard)
                            .border(1.dp, ForestSurfaceBorder, RoundedCornerShape(50))
                            .padding(4.dp)
                    ) {
                        // Tab 1: Task Verification
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(50))
                                .background(if (activeMode == 0) MintPrimary else androidx.compose.ui.graphics.Color.Transparent)
                                .clickable { activeMode = 0 }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Verify Tasks",
                                color = if (activeMode == 0) MintPrimaryDark else TextSecondary,
                                fontSize = 13.sp,
                                fontWeight = if (activeMode == 0) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                        // Tab 2: Study Time Beta
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(50))
                                .background(if (activeMode == 1) AmberGold else androidx.compose.ui.graphics.Color.Transparent)
                                .clickable { activeMode = 1 }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Study Time",
                                    color = if (activeMode == 1) ForestBackground else TextSecondary,
                                    fontSize = 13.sp,
                                    fontWeight = if (activeMode == 1) FontWeight.Bold else FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "BETA",
                                    color = if (activeMode == 1) ForestBackground else AmberGold,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
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
                            .clip(RoundedCornerShape(24.dp))
                            .background(ForestSurfaceCard)
                            .border(1.dp, ForestSurfaceBorder, RoundedCornerShape(24.dp))
                            .padding(20.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
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
                                        color = TextPrimary,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "High priority tasks strictly require AI verification",
                                        color = TextTertiary,
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
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isHighPriority) CoralRed.copy(alpha = 0.12f) else MintPrimaryContainer.copy(alpha = 0.4f))
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
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
                                        color = if (isHighPriority) CoralRed else MintPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            } else {
                                Text(
                                    text = "Select a task from below to verify",
                                    color = TextSecondary,
                                    fontSize = 13.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Screenshot Upload / Preview Area
                            Text(
                                text = "Screenshot Proof (Mandatory for High Priority)",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            if (taskScreenshotUri != null) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(140.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .border(1.dp, ForestSurfaceBorder, RoundedCornerShape(16.dp))
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
                                            .background(ForestBackground.copy(alpha = 0.8f))
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Remove",
                                            tint = TextPrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(ForestBackground)
                                        .border(1.dp, ForestSurfaceBorder, RoundedCornerShape(16.dp))
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
                                            tint = if (isGeminiAvailable) MintPrimary else TextTertiary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Attach Screenshot Evidence",
                                            color = if (isGeminiAvailable) TextPrimary else TextTertiary,
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
                                        color = TextTertiary,
                                        fontSize = 13.sp
                                    )
                                },
                                enabled = isGeminiAvailable,
                                maxLines = 3,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedBorderColor = MintPrimary,
                                    unfocusedBorderColor = ForestSurfaceBorder,
                                    focusedContainerColor = ForestBackground,
                                    unfocusedContainerColor = ForestBackground,
                                    disabledContainerColor = ForestBackground.copy(alpha = 0.5f),
                                    disabledTextColor = TextTertiary
                                ),
                                shape = RoundedCornerShape(16.dp),
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
                                    containerColor = MintPrimary,
                                    contentColor = MintPrimaryDark,
                                    disabledContainerColor = ForestSurfaceBorder,
                                    disabledContentColor = TextTertiary
                                ),
                                shape = RoundedCornerShape(50),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("verify_submit_btn")
                            ) {
                                if (isVerifyingTask) {
                                    CircularProgressIndicator(
                                        color = MintPrimaryDark,
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
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(AmberGold.copy(alpha = 0.12f))
                                        .padding(12.dp)
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
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (tasks.isEmpty()) {
                    item {
                        Text(
                            text = "No tasks available yet. Add tasks from the Home Agenda to start verifying.",
                            color = TextSecondary,
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
                                .clip(RoundedCornerShape(18.dp))
                                .background(if (isSelected) MintPrimaryContainer else ForestSurfaceCard)
                                .border(
                                    1.dp,
                                    if (isSelected) MintPrimary else ForestSurfaceBorder,
                                    RoundedCornerShape(18.dp)
                                )
                                .clickable { selectedTask = task }
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (task.isAiVerified) Icons.Default.CheckCircle else if (isHigh) Icons.Default.Lock else Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = if (task.isAiVerified) AmberGold else if (isHigh) CoralRed else if (isSelected) MintPrimary else TextTertiary,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = task.title,
                                            color = TextPrimary,
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
                                            color = TextSecondary,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
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
                            .clip(RoundedCornerShape(24.dp))
                            .background(ForestSurfaceCard)
                            .border(1.dp, AmberGold.copy(alpha = 0.35f), RoundedCornerShape(24.dp))
                            .padding(20.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
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
                                            color = TextPrimary,
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
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Target App Name input
                            Text(
                                text = "Target App Name to Inspect",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = studyAppName,
                                onValueChange = { studyAppName = it },
                                placeholder = { Text("e.g. Forest, Anki, Duolingo, Notion, YouTube...", color = TextTertiary) },
                                singleLine = true,
                                enabled = isGeminiAvailable,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedBorderColor = AmberGold,
                                    unfocusedBorderColor = ForestSurfaceBorder,
                                    focusedContainerColor = ForestBackground,
                                    unfocusedContainerColor = ForestBackground
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("study_app_name_input")
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Screenshot Picker / Preview for Study Time
                            Text(
                                text = "App Screen Time Screenshot",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            if (studyScreenshotUri != null) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(150.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .border(1.dp, ForestSurfaceBorder, RoundedCornerShape(16.dp))
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
                                            .background(ForestBackground.copy(alpha = 0.8f))
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Remove",
                                            tint = TextPrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(ForestBackground)
                                        .border(1.dp, ForestSurfaceBorder, RoundedCornerShape(16.dp))
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
                                            tint = if (isGeminiAvailable) AmberGold else TextTertiary,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = "Upload Screen Time / Study Screenshot",
                                            color = if (isGeminiAvailable) TextPrimary else TextTertiary,
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
                                    contentColor = ForestBackground,
                                    disabledContainerColor = ForestSurfaceBorder,
                                    disabledContentColor = TextTertiary
                                ),
                                shape = RoundedCornerShape(50),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("extract_study_time_btn")
                            ) {
                                if (isExtractingStudyTime) {
                                    CircularProgressIndicator(
                                        color = ForestBackground,
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
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MintPrimary.copy(alpha = 0.12f))
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = lastStudyExtractionMessage ?: "",
                                        color = MintPrimary,
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
