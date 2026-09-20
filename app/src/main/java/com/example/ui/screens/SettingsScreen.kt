package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.data.model.UserProfile
import com.example.ui.components.AccountProfileDialog
import com.example.ui.components.GeminiApiKeyGuideDialog
import com.example.ui.theme.AmberGold
import com.example.ui.theme.AmberGoldContainer
import com.example.ui.theme.CoralRed
import com.example.ui.theme.ForestBackground
import com.example.ui.theme.ForestSurface
import com.example.ui.theme.ForestSurfaceBorder
import com.example.ui.theme.ForestSurfaceCard
import com.example.ui.theme.MintPrimary
import com.example.ui.theme.MintPrimaryContainer
import com.example.ui.theme.MintPrimaryDark
import com.example.ui.theme.OceanBlue
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.util.BackupManager
import com.example.util.SoundEffectHelper
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    userProfile: UserProfile,
    onUpdateProfile: (String, Int, String?) -> Unit,
    geminiApiKey: String,
    onGeminiApiKeyChange: (String) -> Unit,
    onVerifyApiKey: ((Boolean, String) -> Unit) -> Unit,
    isVerifyingKey: Boolean,
    keyVerificationStatus: String?,
    isStudyTimeBetaEnabled: Boolean,
    onStudyTimeBetaChange: (Boolean) -> Unit,
    isCalendarSync: Boolean,
    onCalendarSyncChange: (Boolean) -> Unit,
    isAiOptimization: Boolean,
    onAiOptimizationChange: (Boolean) -> Unit,
    isNotifications: Boolean,
    onNotificationsChange: (Boolean) -> Unit,
    themeMode: String = "auto",
    onThemeModeChange: (String) -> Unit = {},
    isDynamicColor: Boolean = true,
    onDynamicColorChange: (Boolean) -> Unit = {},
    onExportBackup: suspend () -> String,
    onRestoreBackup: (String, (Boolean, String) -> Unit) -> Unit,
    onLaunchSetupWizard: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var showAccountDialog by remember { mutableStateOf(false) }
    var showApiKeyGuideDialog by remember { mutableStateOf(false) }
    var isApiKeyVisible by remember { mutableStateOf(false) }

    // Backup SAF Launchers
    var pendingExportJson by remember { mutableStateOf("") }
    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null) {
            val success = BackupManager.writeToUri(context, uri, pendingExportJson)
            if (success) {
                Toast.makeText(context, "Backup exported successfully!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Failed to write backup file.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            val json = BackupManager.readFromUri(context, uri)
            if (json != null) {
                onRestoreBackup(json) { success, msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(context, "Could not read backup file.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Easter egg subtle animation state
    val animScale = remember { Animatable(1f) }
    val animRotation = remember { Animatable(0f) }
    var isPouringAnimationActive by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = ForestBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            // Header
            Text(
                text = "Profile & Settings",
                color = TextPrimary,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Configure offline account, custom Gemini key, and backup data",
                color = TextSecondary,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 1. User Offline Account Profile Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(26.dp))
                    .background(ForestSurfaceCard)
                    .border(1.dp, ForestSurfaceBorder, RoundedCornerShape(26.dp))
                    .clickable { showAccountDialog = true }
                    .padding(20.dp)
                    .testTag("user_profile_card")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(MintPrimaryContainer)
                            .border(2.dp, MintPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!userProfile.photoUri.isNullOrBlank()) {
                            Image(
                                painter = rememberAsyncImagePainter(model = userProfile.photoUri),
                                contentDescription = "Avatar",
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(
                                text = userProfile.name.firstOrNull()?.uppercase() ?: "A",
                                color = MintPrimary,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = userProfile.name,
                                color = TextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "(${userProfile.age} yrs)",
                                color = TextSecondary,
                                fontSize = 13.sp
                            )
                        }
                        Text(
                            text = "Offline Account • Stored on device",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(MintPrimary.copy(alpha = 0.15f))
                                .padding(horizontal = 10.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "Tap to Edit Profile",
                                color = MintPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Profile",
                        tint = TextTertiary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 2. Gemini API Key Configuration Section
            Text(
                text = "GEMINI API KEY (USER-PROVIDED)",
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(ForestSurfaceCard)
                    .border(1.dp, ForestSurfaceBorder, RoundedCornerShape(22.dp))
                    .padding(18.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Key,
                                contentDescription = null,
                                tint = if (geminiApiKey.isNotBlank()) MintPrimary else AmberGold,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Your Gemini API Key",
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        // How-to guide button
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable { showApiKeyGuideDialog = true }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.HelpOutline,
                                contentDescription = "Guide",
                                tint = OceanBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "How to get key",
                                color = OceanBlue,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = geminiApiKey,
                        onValueChange = onGeminiApiKeyChange,
                        placeholder = { Text("Paste AI Studio Gemini Key...", color = TextTertiary, fontSize = 13.sp) },
                        singleLine = true,
                        visualTransformation = if (isApiKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            IconButton(onClick = { isApiKeyVisible = !isApiKeyVisible }) {
                                Icon(
                                    imageVector = if (isApiKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle Visibility",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = MintPrimary,
                            unfocusedBorderColor = ForestSurfaceBorder,
                            focusedContainerColor = ForestBackground,
                            unfocusedContainerColor = ForestBackground
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("gemini_api_key_input")
                    )

                    if (keyVerificationStatus != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = keyVerificationStatus,
                            color = if (keyVerificationStatus.startsWith("Valid")) MintPrimary else CoralRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Verify Key Button
                        Button(
                            onClick = {
                                onVerifyApiKey { success, msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                            },
                            enabled = geminiApiKey.isNotBlank() && !isVerifyingKey,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MintPrimary,
                                contentColor = MintPrimaryDark,
                                disabledContainerColor = ForestSurfaceBorder,
                                disabledContentColor = TextTertiary
                            ),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("verify_api_key_btn")
                        ) {
                            if (isVerifyingKey) {
                                CircularProgressIndicator(
                                    color = MintPrimaryDark,
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Verify Key",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Get API Key button (links to Google AI Studio)
                        OutlinedButton(
                            onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://aistudio.google.com/app/apikey"))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Cannot open browser", Toast.LENGTH_SHORT).show()
                                }
                            },
                            border = androidx.compose.foundation.BorderStroke(1.dp, ForestSurfaceBorder),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = ForestBackground,
                                contentColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("get_api_key_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                contentDescription = null,
                                tint = OceanBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Get API Key",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 3. Backup & Restore Data
            Text(
                text = "BACKUP & RESTORE",
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(ForestSurfaceCard)
                    .border(1.dp, ForestSurfaceBorder, RoundedCornerShape(22.dp))
                    .padding(18.dp)
            ) {
                Column {
                    Text(
                        text = "Data Portability & Migration",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Export or restore all tasks, timetable schedule, study times, and account profile as JSON.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Export Backup
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    pendingExportJson = onExportBackup()
                                    createDocumentLauncher.launch("caitlin_daily_backup.json")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ForestBackground,
                                contentColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .border(1.dp, ForestSurfaceBorder, RoundedCornerShape(50))
                                .testTag("export_backup_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudUpload,
                                contentDescription = null,
                                tint = MintPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Export",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Restore Backup
                        Button(
                            onClick = {
                                openDocumentLauncher.launch(arrayOf("application/json", "text/*"))
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ForestBackground,
                                contentColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .border(1.dp, ForestSurfaceBorder, RoundedCornerShape(50))
                                .testTag("restore_backup_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDownload,
                                contentDescription = null,
                                tint = OceanBlue,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Restore",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 4. Beta Features Section (Study Time from Screenshots)
            Text(
                text = "EXPERIMENTAL & BETA",
                color = AmberGold,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(ForestSurfaceCard)
                    .border(1.dp, AmberGold.copy(alpha = 0.3f), RoundedCornerShape(22.dp))
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Get Study Time from Screenshots",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(AmberGold.copy(alpha = 0.2f))
                                    .border(1.dp, AmberGold.copy(alpha = 0.5f), RoundedCornerShape(50))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "BETA",
                                    color = AmberGold,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Unlocks screenshot analysis in AI Verify to extract focus & study time for designated apps.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }

                    Switch(
                        checked = isStudyTimeBetaEnabled,
                        onCheckedChange = onStudyTimeBetaChange,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MintPrimaryDark,
                            checkedTrackColor = AmberGold,
                            uncheckedThumbColor = TextSecondary,
                            uncheckedTrackColor = ForestSurface
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 5. App Onboarding & Setup Wizard
            Text(
                text = "APP ONBOARDING",
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(ForestSurfaceCard)
                    .border(1.dp, ForestSurfaceBorder, RoundedCornerShape(20.dp))
                    .clickable { onLaunchSetupWizard() }
                    .padding(18.dp)
                    .testTag("settings_relaunch_wizard_btn")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MintPrimaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MintPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Launch Setup Wizard",
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Explore feature tours and interactive guides",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = TextTertiary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 6. Integration & System Preferences
            Text(
                text = "INTEGRATION & AI",
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(ForestSurfaceCard)
                    .border(1.dp, ForestSurfaceBorder, RoundedCornerShape(22.dp))
                    .padding(vertical = 6.dp, horizontal = 16.dp)
            ) {
                Column {
                    // Google Calendar Sync
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = OceanBlue,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Google Calendar Sync",
                                    color = TextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Auto-sync daily tasks to your calendar",
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }
                        Switch(
                            checked = isCalendarSync,
                            onCheckedChange = onCalendarSyncChange,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MintPrimaryDark,
                                checkedTrackColor = MintPrimary,
                                uncheckedThumbColor = TextSecondary,
                                uncheckedTrackColor = ForestSurface
                            )
                        )
                    }

                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(ForestSurfaceBorder)
                    )

                    // Gemini AI Optimization
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.SettingsSuggest,
                                contentDescription = null,
                                tint = MintPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Gemini AI Optimization",
                                    color = TextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Dynamic chrono-balancing for peak focus",
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }
                        Switch(
                            checked = isAiOptimization,
                            onCheckedChange = onAiOptimizationChange,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MintPrimaryDark,
                                checkedTrackColor = MintPrimary,
                                uncheckedThumbColor = TextSecondary,
                                uncheckedTrackColor = ForestSurface
                            )
                        )
                    }

                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(ForestSurfaceBorder)
                    )

                    // Daily Reminders
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = AmberGold,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Morning Focus Reminder",
                                    color = TextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Briefing at 08:00 AM daily",
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }
                        Switch(
                            checked = isNotifications,
                            onCheckedChange = onNotificationsChange,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MintPrimaryDark,
                                checkedTrackColor = MintPrimary,
                                uncheckedThumbColor = TextSecondary,
                                uncheckedTrackColor = ForestSurface
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ---------------------------------------------------------
            // APPEARANCE & MATERIAL YOU THEME CARD
            // ---------------------------------------------------------
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
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = null,
                            tint = MintPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Appearance & Theme",
                                color = TextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Light, Dark, and Material You dynamic accents",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "THEME MODE",
                        color = TextTertiary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // 3-Way Mode Selector: Auto, Dark, Light
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val modes = listOf(
                            Triple("auto", "Auto", Icons.Default.SettingsSuggest),
                            Triple("dark", "Dark", Icons.Default.DarkMode),
                            Triple("light", "Light", Icons.Default.LightMode)
                        )
                        modes.forEach { (modeKey, modeTitle, modeIcon) ->
                            val isSelected = themeMode.equals(modeKey, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (isSelected) MintPrimaryContainer else ForestSurface)
                                    .border(
                                        1.dp,
                                        if (isSelected) MintPrimary else ForestSurfaceBorder,
                                        RoundedCornerShape(14.dp)
                                    )
                                    .clickable { onThemeModeChange(modeKey) }
                                    .padding(vertical = 12.dp, horizontal = 4.dp)
                                    .testTag("theme_mode_$modeKey"),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = modeIcon,
                                        contentDescription = null,
                                        tint = if (isSelected) MintPrimaryDark else TextSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = modeTitle,
                                        color = if (isSelected) MintPrimaryDark else TextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(ForestSurfaceBorder)
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    // Material You Dynamic Colors Switch
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = OceanBlue,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Material You Dynamic Colors",
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Sample wallpaper & system accents dynamically (Android 12+)",
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Switch(
                            checked = isDynamicColor,
                            onCheckedChange = onDynamicColorChange,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MintPrimaryDark,
                                checkedTrackColor = MintPrimary,
                                uncheckedThumbColor = TextSecondary,
                                uncheckedTrackColor = ForestSurface
                            ),
                            modifier = Modifier.testTag("switch_dynamic_color")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // =========================================================================
            // ANDROID SETTINGS APP STYLE BANNER (App Version & Credit: Made by Aswin A S)
            // Includes hidden easter egg interaction
            // =========================================================================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 40.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                        RoundedCornerShape(24.dp)
                    )
                    .padding(20.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Android Settings style App Icon Badge
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(MintPrimaryContainer)
                            .border(1.5.dp, MintPrimary.copy(alpha = 0.6f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Android,
                            contentDescription = null,
                            tint = MintPrimaryDark,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Caitlin Daily",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = "Version 1.2.0 • Android 15 Ready",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Made by Aswin A S clickable credit banner with easter egg
                    Box(
                        modifier = Modifier
                            .scale(animScale.value)
                            .rotate(animRotation.value)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (isPouringAnimationActive) AmberGoldContainer.copy(alpha = 0.35f)
                                else MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                            )
                            .border(
                                1.dp,
                                if (isPouringAnimationActive) AmberGold else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                RoundedCornerShape(16.dp)
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                // 1. Play whiskey bottle pop and pouring sound effect asynchronously
                                coroutineScope.launch {
                                    SoundEffectHelper.playWhiskeyPopAndPour()
                                }
                                // 2. Play subtle tilt and glass ripple animation
                                coroutineScope.launch {
                                    isPouringAnimationActive = true
                                    animScale.animateTo(
                                        targetValue = 1.12f,
                                        animationSpec = tween(150, easing = FastOutSlowInEasing)
                                    )
                                    animRotation.animateTo(
                                        targetValue = -6f,
                                        animationSpec = tween(120)
                                    )
                                    animRotation.animateTo(
                                        targetValue = 6f,
                                        animationSpec = tween(120)
                                    )
                                    animRotation.animateTo(
                                        targetValue = 0f,
                                        animationSpec = tween(140)
                                    )
                                    animScale.animateTo(
                                        targetValue = 1f,
                                        animationSpec = spring()
                                    )
                                    isPouringAnimationActive = false
                                }
                                // 3. Display Toast with days till next Friday or Friday congratulations
                                val toastMessage = SoundEffectHelper.getFridayEasterEggMessage()
                                Toast.makeText(context, toastMessage, Toast.LENGTH_LONG).show()
                            }
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                            .testTag("credits_aswin"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (isPouringAnimationActive) AmberGold else MintPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Made by Aswin A S",
                                color = if (isPouringAnimationActive) AmberGold else MaterialTheme.colorScheme.onSurface,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (isPouringAnimationActive) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.LocalBar,
                                    contentDescription = null,
                                    tint = AmberGold,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Account Profile Edit Dialog
        if (showAccountDialog) {
            AccountProfileDialog(
                currentProfile = userProfile,
                onDismiss = { showAccountDialog = false },
                onSave = { name, age, photoUri ->
                    onUpdateProfile(name, age, photoUri)
                    showAccountDialog = false
                    Toast.makeText(context, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // How-to get API Key Guide Dialog
        if (showApiKeyGuideDialog) {
            GeminiApiKeyGuideDialog(
                onDismiss = { showApiKeyGuideDialog = false },
                onGetApiKeyClick = {
                    showApiKeyGuideDialog = false
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://aistudio.google.com/app/apikey"))
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Cannot open browser", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
    }
}
