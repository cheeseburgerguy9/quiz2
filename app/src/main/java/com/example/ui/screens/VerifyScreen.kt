package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ExpressiveCardShape
import com.example.ui.theme.GeminiPurple
import com.example.ui.theme.PillShape
import com.example.ui.theme.VerifiedGold
import com.example.ui.theme.VerifiedGreen
import com.example.ui.viewmodel.AppNavTab
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerifyScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val tasks by viewModel.rawTasks.collectAsState()
    val verifyState by viewModel.verificationState.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val isAiEnabled by viewModel.isAiEnabled.collectAsState()
    val studyTimeBetaEnabled by viewModel.studyTimeBetaEnabled.collectAsState()
    val screenTimeState by viewModel.screenTimeState.collectAsState()

    var taskDropdownExpanded by remember { mutableStateOf(false) }

    // Modern Zero-Permission Android Photo Picker for Task Proof
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                @Suppress("DEPRECATION")
                val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                viewModel.onScreenshotSelected(bitmap, uri.toString())
            } catch (e: Exception) {
                // Ignore or handle
            }
        }
    }

    // Modern Zero-Permission Android Photo Picker for Screen Time
    val screenTimePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                @Suppress("DEPRECATION")
                val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                viewModel.onScreenTimeScreenshotSelected(bitmap, uri.toString())
            } catch (e: Exception) {
                // Ignore or handle
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Task Verification",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(Modifier.width(8.dp))
                            Surface(
                                shape = PillShape,
                                color = VerifiedGold.copy(alpha = 0.16f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Verified,
                                        contentDescription = null,
                                        tint = VerifiedGold,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = "Gemini Vision",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFFC67C00),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                        Text(
                            text = "Multimodal proof verification for ${userProfile.name}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(if (isAiEnabled) 1f else 0.28f),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
            // Step 1: Select Task
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = ExpressiveCardShape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "1. Select Task to Verify",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        ExposedDropdownMenuBox(
                            expanded = taskDropdownExpanded,
                            onExpandedChange = { taskDropdownExpanded = !taskDropdownExpanded }
                        ) {
                            OutlinedTextField(
                                value = verifyState.selectedTask?.title ?: "Select a task...",
                                onValueChange = {},
                                readOnly = true,
                                shape = RoundedCornerShape(16.dp),
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = taskDropdownExpanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                                    .testTag("verify_task_dropdown")
                            )

                            ExposedDropdownMenu(
                                expanded = taskDropdownExpanded,
                                onDismissRequest = { taskDropdownExpanded = false }
                            ) {
                                tasks.forEach { task ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(task.title, modifier = Modifier.weight(1f))
                                                if (task.isVerified) {
                                                    Icon(
                                                        Icons.Default.Verified,
                                                        contentDescription = null,
                                                        tint = VerifiedGold,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        },
                                        onClick = {
                                            viewModel.prepareTaskForVerification(task)
                                            taskDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        verifyState.selectedTask?.let { task ->
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "Category: ${task.category} • Priority: ${task.priority} • Est: ${task.estimatedMinutes}m",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (task.description.isNotBlank()) {
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            text = "Criteria: ${task.description}",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Step 2: Upload Screenshot / Photo
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = ExpressiveCardShape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "2. Upload Screenshot Evidence",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        if (verifyState.selectedBitmap != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(210.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color.Black)
                            ) {
                                Image(
                                    bitmap = verifyState.selectedBitmap!!.asImageBitmap(),
                                    contentDescription = "Uploaded Screenshot",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .clickable {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.Default.AddPhotoAlternate,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(40.dp)
                                    )
                                    Text(
                                        text = "Choose screenshot from gallery",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "PNG, JPG supported (Android Photo Picker)",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Buttons row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                shape = PillShape,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("pick_screenshot_button")
                            ) {
                                Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Pick Proof", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            }

                            OutlinedButton(
                                onClick = {
                                    val sampleBitmap = createSampleProofBitmap(
                                        verifyState.selectedTask?.title ?: "Task Completion"
                                    )
                                    viewModel.onScreenshotSelected(sampleBitmap, "sample_proof")
                                },
                                shape = PillShape,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("sample_screenshot_button")
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Simulate Proof", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }

            // Step 3: Run Verification Button
            item {
                Button(
                    onClick = { viewModel.runScreenshotVerification() },
                    enabled = verifyState.selectedTask != null && verifyState.selectedBitmap != null && !verifyState.isVerifying,
                    colors = ButtonDefaults.buttonColors(containerColor = GeminiPurple),
                    shape = PillShape,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("run_verify_button")
                ) {
                    if (verifyState.isVerifying) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.5.dp
                        )
                        Spacer(Modifier.width(10.dp))
                        Text("Gemini Vision is Verifying Evidence...")
                    } else {
                        Icon(Icons.Default.Verified, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Confirm Completion with Gemini AI", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Error Message Banner
            if (verifyState.errorMessage != null) {
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = verifyState.errorMessage ?: "",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }
            }

            // Step 4: Verification Results Card
            verifyState.lastResult?.let { result ->
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("verification_result_card"),
                        shape = ExpressiveCardShape,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(
                            width = 2.dp,
                            color = if (result.verified) VerifiedGold else MaterialTheme.colorScheme.error
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        if (result.verified) Icons.Default.CheckCircle else Icons.Default.Close,
                                        contentDescription = null,
                                        tint = if (result.verified) VerifiedGreen else MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Text(
                                        text = if (result.verified) "Task Verified!" else "Verification Inconclusive",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Surface(
                                    shape = PillShape,
                                    color = VerifiedGold.copy(alpha = 0.16f)
                                ) {
                                    Text(
                                        text = result.badge,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFC67C00),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            // Confidence Meter
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "AI Confidence Score",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${result.confidence}%",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (result.confidence >= 80) VerifiedGreen else MaterialTheme.colorScheme.primary
                                    )
                                }
                                LinearProgressIndicator(
                                    progress = { result.confidence / 100f },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(10.dp)
                                        .clip(PillShape),
                                    color = if (result.confidence >= 80) VerifiedGreen else MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            }

                            // Gemini Explanation
                            Text(
                                text = "Gemini AI Analysis:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = result.explanation,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            if (result.detectedEvidence.isNotBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Visual Evidence: ${result.detectedEvidence}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Beta Feature: Calculate Study Time from Screen Time / Digital Wellbeing
            if (studyTimeBetaEnabled) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("beta_study_time_card"),
                        shape = ExpressiveCardShape,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)),
                        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.HourglassBottom,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = "Study Time Calculator",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Surface(
                                    shape = PillShape,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "BETA",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Text(
                                text = "Upload a screenshot of your Battery usage or Digital Wellbeing screen. Gemini Vision will read the usage duration for your chosen study app and log it directly into your Insights.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            // Select / Type Study App
                            OutlinedTextField(
                                value = screenTimeState.targetAppName,
                                onValueChange = { viewModel.setScreenTimeTargetApp(it) },
                                label = { Text("Select / Enter Study App Name") },
                                placeholder = { Text("e.g. Duolingo, Anki, Coursera, Quizlet") },
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("study_app_name_field")
                            )

                            // Upload Screenshot or Sample
                            if (screenTimeState.selectedBitmap != null) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(Color.Black)
                                ) {
                                    Image(
                                        bitmap = screenTimeState.selectedBitmap!!.asImageBitmap(),
                                        contentDescription = "Screen Time Screenshot",
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        screenTimePickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    shape = PillShape,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("pick_screen_time_btn")
                                ) {
                                    Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Upload Screen Time", fontSize = 12.sp)
                                }

                                OutlinedButton(
                                    onClick = {
                                        val sample = createSampleDigitalWellbeingBitmap(screenTimeState.targetAppName)
                                        viewModel.onScreenTimeScreenshotSelected(sample, "sample_screentime")
                                    },
                                    shape = PillShape,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("sample_screen_time_btn")
                                ) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Sample Screen", fontSize = 12.sp)
                                }
                            }

                            // Calculate Button
                            Button(
                                onClick = { viewModel.analyzeScreenTimeStudyMinutes() },
                                enabled = !screenTimeState.isAnalyzing && screenTimeState.selectedBitmap != null && isAiEnabled,
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                shape = PillShape,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("calculate_study_time_btn")
                            ) {
                                if (screenTimeState.isAnalyzing) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text("Calculating Study Time with Gemini...")
                                } else {
                                    Icon(Icons.Default.HourglassBottom, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Calculate & Add to Insights", fontWeight = FontWeight.Bold)
                                }
                            }

                            if (!isAiEnabled) {
                                Text(
                                    text = "AI is currently disabled in Settings. Turn on AI to use this calculation feature.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }

                            screenTimeState.errorMessage?.let { err ->
                                Text(
                                    text = err,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }

                            screenTimeState.lastResult?.let { res ->
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = VerifiedGreen.copy(alpha = 0.12f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = "Logged: ${res.detectedTimeFormatted} for ${res.appName}",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = VerifiedGreen
                                        )
                                        Spacer(Modifier.height(2.dp))
                                        Text(
                                            text = res.explanation,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(Modifier.height(88.dp))
            }
        }

        if (!isAiEnabled) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.55f))
                    .clickable {
                        viewModel.showNotification("Turn on Gemini to access AI Verify")
                    }
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = ExpressiveCardShape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    border = BorderStroke(1.5.dp, GeminiPurple.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            viewModel.showNotification("Turn on Gemini to access AI Verify")
                        }
                        .testTag("ai_verify_disabled_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = GeminiPurple.copy(alpha = 0.12f),
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = GeminiPurple,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }

                        Text(
                            text = "AI Verification is Paused",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold
                        )

                        Text(
                            text = "Turn on Gemini to access multimodal screenshot verification, proof inspection, and screen time calculation.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        Spacer(Modifier.height(4.dp))

                        Button(
                            onClick = {
                                viewModel.setAiEnabled(true)
                                viewModel.showNotification("Gemini AI enabled!")
                            },
                            shape = PillShape,
                            colors = ButtonDefaults.buttonColors(containerColor = GeminiPurple),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("turn_on_gemini_verify_btn")
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Turn On Gemini")
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.selectTab(AppNavTab.SETTINGS)
                            },
                            shape = PillShape,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Open Settings")
                        }
                    }
                }
            }
        }
    }
}
}

/**
 * Creates a high quality sample screenshot bitmap with realistic UI evidence
 * so users can test multimodal verification even in emulator without files.
 */
private fun createSampleProofBitmap(taskTitle: String): Bitmap {
    val width = 720
    val height = 480
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    // Background
    val bgPaint = Paint().apply { color = android.graphics.Color.rgb(28, 28, 34) }
    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

    // Window Header
    val headerPaint = Paint().apply { color = android.graphics.Color.rgb(42, 42, 50) }
    canvas.drawRect(0f, 0f, width.toFloat(), 60f, headerPaint)

    // Window Buttons
    val circlePaint = Paint().apply { isAntiAlias = true }
    circlePaint.color = android.graphics.Color.rgb(255, 95, 87)
    canvas.drawCircle(30f, 30f, 10f, circlePaint)
    circlePaint.color = android.graphics.Color.rgb(254, 188, 46)
    canvas.drawCircle(60f, 30f, 10f, circlePaint)
    circlePaint.color = android.graphics.Color.rgb(40, 200, 64)
    canvas.drawCircle(90f, 30f, 10f, circlePaint)

    // Text: Status header
    val textPaint = Paint().apply {
        color = android.graphics.Color.rgb(76, 175, 80)
        textSize = 32f
        isAntiAlias = true
        isFakeBoldText = true
    }
    canvas.drawText("TASK COMPLETED: SUCCESS (100%)", 40f, 130f, textPaint)

    // Text: Task title
    val titlePaint = Paint().apply {
        color = android.graphics.Color.WHITE
        textSize = 28f
        isAntiAlias = true
    }
    canvas.drawText("Verified Target: $taskTitle", 40f, 180f, titlePaint)

    // Simulated Code / Activity Output lines
    val codePaint = Paint().apply {
        color = android.graphics.Color.rgb(180, 180, 210)
        textSize = 22f
        isAntiAlias = true
    }
    canvas.drawText("> git commit -m \"feat: complete task milestone with full tests\"", 40f, 240f, codePaint)
    canvas.drawText("> [main a8f912c] All checks passed. Build verified cleanly.", 40f, 280f, codePaint)
    canvas.drawText("> Progress: 100% | Duration: 45 mins | Metrics logged", 40f, 320f, codePaint)
    canvas.drawText("> Proof hash: SHA-256 verified by Caitlin Daily Engine", 40f, 360f, codePaint)

    // Success Stamp Badge
    val stampPaint = Paint().apply {
        color = android.graphics.Color.rgb(76, 175, 80)
        style = Paint.Style.STROKE
        strokeWidth = 4f
        isAntiAlias = true
    }
    val stampRect = RectF(480f, 320f, 680f, 440f)
    canvas.drawRoundRect(stampRect, 16f, 16f, stampPaint)

    val stampTextPaint = Paint().apply {
        color = android.graphics.Color.rgb(76, 175, 80)
        textSize = 30f
        isFakeBoldText = true
        isAntiAlias = true
    }
    canvas.drawText("VERIFIED", 510f, 385f, stampTextPaint)

    return bitmap
}

/**
 * Creates a sample battery / digital wellbeing screen capture showing the study app
 * with clear screen-on usage statistics for Gemini vision testing.
 */
private fun createSampleDigitalWellbeingBitmap(appName: String): Bitmap {
    val width = 720
    val height = 640
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    // Android System Dark Background
    val bgPaint = Paint().apply { color = android.graphics.Color.rgb(18, 18, 22) }
    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

    // Status bar / App Bar
    val barPaint = Paint().apply { color = android.graphics.Color.rgb(28, 29, 36) }
    canvas.drawRect(0f, 0f, width.toFloat(), 70f, barPaint)

    val titlePaint = Paint().apply {
        color = android.graphics.Color.WHITE
        textSize = 28f
        isFakeBoldText = true
        isAntiAlias = true
    }
    canvas.drawText("Digital Wellbeing & Parental Controls", 40f, 48f, titlePaint)

    // Screen Time Header
    val headerPaint = Paint().apply {
        color = android.graphics.Color.rgb(160, 165, 185)
        textSize = 22f
        isAntiAlias = true
    }
    canvas.drawText("Today's Screen Time", 40f, 120f, headerPaint)

    val bigTimePaint = Paint().apply {
        color = android.graphics.Color.rgb(220, 225, 245)
        textSize = 46f
        isFakeBoldText = true
        isAntiAlias = true
    }
    canvas.drawText("3 hr 42 min", 40f, 175f, bigTimePaint)

    // App Usage List Card
    val cardPaint = Paint().apply { color = android.graphics.Color.rgb(32, 33, 42) }
    val cardRect = RectF(30f, 210f, (width - 30).toFloat(), 600f)
    canvas.drawRoundRect(cardRect, 24f, 24f, cardPaint)

    // Target Study App Item
    val iconPaint = Paint().apply { color = android.graphics.Color.rgb(69, 85, 255); isAntiAlias = true }
    canvas.drawCircle(80f, 280f, 30f, iconPaint)

    val appTitlePaint = Paint().apply {
        color = android.graphics.Color.WHITE
        textSize = 30f
        isFakeBoldText = true
        isAntiAlias = true
    }
    val target = if (appName.isNotBlank()) appName else "Duolingo"
    canvas.drawText(target, 130f, 275f, appTitlePaint)

    val usagePaint = Paint().apply {
        color = android.graphics.Color.rgb(140, 210, 140)
        textSize = 24f
        isAntiAlias = true
        isFakeBoldText = true
    }
    canvas.drawText("1 hr 25 min usage today", 130f, 310f, usagePaint)

    // Progress bar for the study app
    val barTrack = Paint().apply { color = android.graphics.Color.rgb(55, 58, 72) }
    canvas.drawRoundRect(RectF(130f, 325f, 650f, 337f), 6f, 6f, barTrack)
    val barProgress = Paint().apply { color = android.graphics.Color.rgb(69, 85, 255) }
    canvas.drawRoundRect(RectF(130f, 325f, 480f, 337f), 6f, 6f, barProgress)

    // Divider
    val divPaint = Paint().apply { color = android.graphics.Color.rgb(45, 47, 58) }
    canvas.drawLine(50f, 360f, (width - 50).toFloat(), 360f, divPaint)

    // Secondary App Item (e.g. YouTube / Browser)
    val icon2Paint = Paint().apply { color = android.graphics.Color.rgb(240, 70, 60); isAntiAlias = true }
    canvas.drawCircle(80f, 430f, 30f, icon2Paint)
    canvas.drawText("Chrome Browser", 130f, 425f, appTitlePaint)
    val usage2Paint = Paint().apply {
        color = android.graphics.Color.rgb(180, 185, 200)
        textSize = 22f
        isAntiAlias = true
    }
    canvas.drawText("54 min usage today", 130f, 458f, usage2Paint)
    canvas.drawRoundRect(RectF(130f, 475f, 650f, 487f), 6f, 6f, barTrack)
    val barProgress2 = Paint().apply { color = android.graphics.Color.rgb(240, 70, 60) }
    canvas.drawRoundRect(RectF(130f, 475f, 320f, 487f), 6f, 6f, barProgress2)

    return bitmap
}
