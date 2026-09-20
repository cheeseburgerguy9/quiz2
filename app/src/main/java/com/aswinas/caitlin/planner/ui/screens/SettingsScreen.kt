package com.aswinas.caitlin.planner.ui.screens

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
import androidx.compose.material.icons.filled.ScreenSearchDesktop
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material.icons.filled.Tour
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
import com.aswinas.caitlin.planner.data.model.UserProfile
import com.aswinas.caitlin.planner.ui.components.AccountProfileDialog
import com.aswinas.caitlin.planner.ui.components.GeminiApiKeyGuideDialog
import com.aswinas.caitlin.planner.ui.components.SettingsDivider
import com.aswinas.caitlin.planner.ui.components.SettingsGroupCard
import com.aswinas.caitlin.planner.ui.components.SettingsItemRow
import com.aswinas.caitlin.planner.ui.components.SettingsSwitchRow
import com.aswinas.caitlin.planner.ui.theme.AmberGold
import com.aswinas.caitlin.planner.ui.theme.AmberGoldContainer
import com.aswinas.caitlin.planner.ui.theme.CoralRed
import com.aswinas.caitlin.planner.ui.theme.OceanBlue
import com.aswinas.caitlin.planner.util.BackupManager
import com.aswinas.caitlin.planner.util.SoundEffectHelper
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
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            // Android Settings Big Display Title
            Column(modifier = Modifier.padding(bottom = 20.dp, top = 6.dp)) {
                Text(
                    text = "Settings",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Preferences, custom AI key & data control",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
            }

            // 1. Android Settings Top Hero Card: Local Account Profile Style
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f))
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                        RoundedCornerShape(28.dp)
                    )
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
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .border(2.5.dp, MaterialTheme.colorScheme.primary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!userProfile.photoUri.isNullOrBlank()) {
                            Image(
                                painter = rememberAsyncImagePainter(model = userProfile.photoUri),
                                contentDescription = "Avatar",
                                modifier = Modifier
                                    .size(68.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(
                                text = userProfile.name.firstOrNull()?.uppercase() ?: "U",
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (userProfile.name.isNotBlank()) userProfile.name else "My Profile",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (userProfile.age > 0) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "(${userProfile.age} yrs)",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 13.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Offline Account • Private on device",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Edit Profile & Photo",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Profile",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 2. Appearance & Material You Group Card
            SettingsGroupCard(title = "Appearance & Display") {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 10.dp, bottom = 12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "Color & Theme",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Dynamic Material You palette & contrast",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                }

                Text(
                    text = "THEME MODE",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                )

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
                                .clip(RoundedCornerShape(18.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                    RoundedCornerShape(18.dp)
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
                                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = modeTitle,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                    else MaterialTheme.colorScheme.onSurface,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                SettingsDivider()

                // Material You Dynamic Colors Row
                SettingsSwitchRow(
                    title = "Material You Dynamic Colors",
                    subtitle = "Device wallpaper & system accents (Android 12+)",
                    icon = Icons.Default.AutoAwesome,
                    iconTint = OceanBlue,
                    iconContainerColor = OceanBlue.copy(alpha = 0.15f),
                    checked = isDynamicColor,
                    onCheckedChange = onDynamicColorChange,
                    testTag = "switch_dynamic_color"
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 3. Gemini API Key Configuration Group
            SettingsGroupCard(title = "Gemini AI Engine") {
                Column(modifier = Modifier.padding(vertical = 6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (geminiApiKey.isNotBlank()) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                                        else AmberGold.copy(alpha = 0.15f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Key,
                                    contentDescription = null,
                                    tint = if (geminiApiKey.isNotBlank()) MaterialTheme.colorScheme.primary else AmberGold,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Your Gemini API Key",
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Stored privately on device",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        // How-to guide chip
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), RoundedCornerShape(50))
                                .clickable { showApiKeyGuideDialog = true }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.HelpOutline,
                                contentDescription = "Guide",
                                tint = OceanBlue,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Guide",
                                color = OceanBlue,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = geminiApiKey,
                        onValueChange = onGeminiApiKeyChange,
                        placeholder = {
                            Text(
                                "Paste AI Studio Gemini Key...",
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                fontSize = 13.sp
                            )
                        },
                        singleLine = true,
                        visualTransformation = if (isApiKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            IconButton(onClick = { isApiKeyVisible = !isApiKeyVisible }) {
                                Icon(
                                    imageVector = if (isApiKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle Visibility",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                            focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                        ),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("gemini_api_key_input")
                    )

                    if (keyVerificationStatus != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = keyVerificationStatus,
                            color = if (keyVerificationStatus.startsWith("Valid")) MaterialTheme.colorScheme.primary else CoralRed,
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
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("verify_api_key_btn")
                        ) {
                            if (isVerifyingKey) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.onPrimary,
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
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                            ),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
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

            Spacer(modifier = Modifier.height(20.dp))

            // 4. Intelligence & Smart Automation Group
            SettingsGroupCard(title = "Intelligence & Sync") {
                SettingsSwitchRow(
                    title = "Google Calendar Sync",
                    subtitle = "Auto-sync daily tasks to calendar",
                    icon = Icons.Default.CalendarMonth,
                    iconTint = OceanBlue,
                    iconContainerColor = OceanBlue.copy(alpha = 0.15f),
                    checked = isCalendarSync,
                    onCheckedChange = onCalendarSyncChange
                )
                SettingsDivider()
                SettingsSwitchRow(
                    title = "Gemini AI Optimization",
                    subtitle = "Dynamic chrono-balancing for peak focus",
                    icon = Icons.Default.SettingsSuggest,
                    iconTint = MaterialTheme.colorScheme.primary,
                    iconContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                    checked = isAiOptimization,
                    onCheckedChange = onAiOptimizationChange
                )
                SettingsDivider()
                SettingsSwitchRow(
                    title = "Daily Smart Reminders",
                    subtitle = "Notifications for scheduled task flow",
                    icon = Icons.Default.NotificationsActive,
                    iconTint = AmberGold,
                    iconContainerColor = AmberGold.copy(alpha = 0.15f),
                    checked = isNotifications,
                    onCheckedChange = onNotificationsChange
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 5. Experimental & Beta Features Group
            SettingsGroupCard(title = "Experimental & Beta") {
                SettingsSwitchRow(
                    title = "Study Time from Screenshots",
                    subtitle = "Gemini Vision extracts focus minutes from app screenshots",
                    icon = Icons.Default.ScreenSearchDesktop,
                    iconTint = AmberGold,
                    iconContainerColor = AmberGold.copy(alpha = 0.2f),
                    checked = isStudyTimeBetaEnabled,
                    onCheckedChange = onStudyTimeBetaChange
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 6. Data Management & Tools Group
            SettingsGroupCard(title = "Data & Onboarding") {
                SettingsItemRow(
                    title = "Launch Setup Wizard",
                    subtitle = "Revisit onboarding guide and feature tours",
                    icon = Icons.Default.Tour,
                    iconTint = MaterialTheme.colorScheme.primary,
                    iconContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                    onClick = onLaunchSetupWizard,
                    testTag = "settings_relaunch_wizard_btn"
                )
                SettingsDivider()

                Column(modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp)) {
                    Text(
                        text = "Data Portability (JSON)",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Export or restore all tasks, schedule, and study logs directly on device",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    pendingExportJson = onExportBackup()
                                    createDocumentLauncher.launch("caitlin_daily_backup.json")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                                    RoundedCornerShape(50)
                                )
                                .testTag("export_backup_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudUpload,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Export",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = {
                                openDocumentLauncher.launch(arrayOf("application/json", "text/*"))
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                                    RoundedCornerShape(50)
                                )
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

            // =========================================================================
            // CREDITS SECTION (App Info & Credit: Made by Aswin A S)
            // Hidden Easter Egg:
            // Tapping "Made by Aswin A S" plays whiskey bottle pop and pouring sound effect,
            // subtle animation (tilt & ripple), and shows a Toast with how many days till
            // the next Friday (or "congrats today's the day , enjoy ." on Friday).
            // NOT mentioned anywhere in the app!
            // =========================================================================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 40.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                        RoundedCornerShape(28.dp)
                    )
                    .padding(22.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Android,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Caitlin Daily",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "Version 1.2.0 • Android 17 Ready",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    // Made by Aswin A S clickable credit banner with easter egg
                    Box(
                        modifier = Modifier
                            .scale(animScale.value)
                            .rotate(animRotation.value)
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (isPouringAnimationActive) AmberGoldContainer.copy(alpha = 0.35f)
                                else MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                            )
                            .border(
                                1.dp,
                                if (isPouringAnimationActive) AmberGold else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                                RoundedCornerShape(20.dp)
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
                                tint = if (isPouringAnimationActive) AmberGold else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Made by aswin a s",
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
