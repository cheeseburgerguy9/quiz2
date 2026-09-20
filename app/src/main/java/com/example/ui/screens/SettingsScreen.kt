package com.example.ui.screens

import android.widget.Toast
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberGold
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
import com.example.util.SoundEffectHelper
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    userName: String,
    userAge: Int,
    profilePhotoBase64: String,
    hasGeminiKey: Boolean,
    onSetGeminiKey: (String, (Boolean, String) -> Unit) -> Unit,
    onClearGeminiKey: () -> Unit,
    studyBetaEnabled: Boolean,
    studyAppName: String,
    onSetStudyBeta: (Boolean) -> Unit,
    onSetStudyAppName: (String) -> Unit,
    onSetCalendarSync: (Boolean) -> Unit,
    onSetAiOptimization: (Boolean) -> Unit,
    onSetNotifications: (Boolean) -> Unit,
    onLaunchSetupWizard: () -> Unit,
    onExportBackup: (Uri, (Boolean, String) -> Unit) -> Unit,
    onImportBackup: (Uri, (Boolean, String) -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var geminiKeyInput by remember { mutableStateOf("") }
    var keyConfigured by remember { mutableStateOf(hasGeminiKey) }
    var keyStatus by remember { mutableStateOf(if (hasGeminiKey) "Verified key stored on this device" else "No key configured") }
    var studyAppInput by remember { mutableStateOf(studyAppName) }
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri -> if (uri != null) onExportBackup(uri) { ok, msg -> keyStatus = msg } }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri -> if (uri != null) onImportBackup(uri) { ok, msg -> keyStatus = msg } }

    var isCalendarSync by remember { mutableStateOf(true) }
    var isAiOptimization by remember { mutableStateOf(true) }
    var isNotifications by remember { mutableStateOf(true) }
    var isStudyBeta by remember { mutableStateOf(studyBetaEnabled) }

    // Easter egg subtle animation state
    val animScale = remember { Animatable(1f) }
    val animRotation = remember { Animatable(0f) }
    var isPouringAnimationActive by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

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
                text = "Configure your AI daily tracker and sync options",
                color = TextSecondary,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // User Profile Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(26.dp))
                    .background(ForestSurfaceCard)
                    .border(1.dp, ForestSurfaceBorder, RoundedCornerShape(26.dp))
                    .padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(MintPrimaryContainer)
                            .border(2.dp, MintPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userName.firstOrNull()?.uppercase() ?: "A",
                            color = MintPrimary,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = userName,
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (userAge > 0) "Age $userAge • Offline account" else "Offline account",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(MintPrimary.copy(alpha = 0.15f))
                                .padding(horizontal = 10.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "Pro AI Daily Tracker",
                                color = MintPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Features & Setup Wizard Section
            Text(
                text = "App Onboarding",
                color = TextSecondary,
                fontSize = 13.sp,
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

            // Synchronization & AI Preferences
            Text(
                text = "Integration & AI",
                color = TextSecondary,
                fontSize = 13.sp,
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
                            onCheckedChange = { isCalendarSync = it; onSetCalendarSync(it) },
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
                            onCheckedChange = { isAiOptimization = it; onSetAiOptimization(it) },
                            enabled = keyConfigured,
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
                            onCheckedChange = { isNotifications = it; onSetNotifications(it) },
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

            Spacer(modifier = Modifier.height(24.dp))

            Text("Gemini AI", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(10.dp))
            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(ForestSurfaceCard).border(1.dp, ForestSurfaceBorder, RoundedCornerShape(22.dp)).padding(16.dp)) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Key, null, tint = MintPrimary, modifier = Modifier.size(24.dp)); Spacer(Modifier.width(12.dp)); Column { Text("Use your own Gemini API key", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold); Text("Nothing is bundled with Caitlin Daily. The key is stored locally encrypted.", color = TextSecondary, fontSize = 12.sp) } }
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(value = geminiKeyInput, onValueChange = { geminiKeyInput = it }, label = { Text("Gemini API key") }, singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary, focusedBorderColor = MintPrimary, unfocusedBorderColor = ForestSurfaceBorder), modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { onSetGeminiKey(geminiKeyInput) { ok, msg -> keyConfigured = ok; keyStatus = msg } }, enabled = geminiKeyInput.isNotBlank(), colors = ButtonDefaults.buttonColors(containerColor = MintPrimary, contentColor = MintPrimaryDark), shape = RoundedCornerShape(50), modifier = Modifier.weight(1f)) { Text("Verify key") }
                        Button(onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://aistudio.google.com/apikey"))) }, colors = ButtonDefaults.buttonColors(containerColor = ForestSurface, contentColor = TextPrimary), shape = RoundedCornerShape(50), modifier = Modifier.weight(1f)) { Text("Get API key") }
                    }
                    Spacer(Modifier.height(7.dp)); Text("How: open Google AI Studio → create an API key → paste it here → Verify key. Gemini features stay disabled until verification succeeds.", color = TextSecondary, fontSize = 11.sp, lineHeight = 15.sp)
                    Spacer(Modifier.height(6.dp)); Text(keyStatus, color = if (keyConfigured) MintPrimary else AmberGold, fontSize = 12.sp)
                    if (keyConfigured) { Spacer(Modifier.height(5.dp)); Text("Clear stored key", color = AmberGold, fontSize = 12.sp, modifier = Modifier.clickable { onClearGeminiKey(); keyConfigured = false; geminiKeyInput = ""; keyStatus = "No key configured" }) }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Study Time", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(10.dp))
            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(ForestSurfaceCard).border(1.dp, ForestSurfaceBorder, RoundedCornerShape(22.dp)).padding(16.dp)) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Science, null, tint = OceanBlue, modifier = Modifier.size(24.dp)); Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) { Text("Study-time from screenshots", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold); Text("Beta • Gemini reads app name and visible duration.", color = TextSecondary, fontSize = 12.sp) } }
                    Spacer(Modifier.height(8.dp))
                    Switch(checked = isStudyBeta, onCheckedChange = { isStudyBeta = it; onSetStudyBeta(it) }, enabled = keyConfigured, colors = SwitchDefaults.colors(checkedThumbColor = MintPrimaryDark, checkedTrackColor = MintPrimary))
                    OutlinedTextField(value = studyAppInput, onValueChange = { studyAppInput = it; onSetStudyAppName(it) }, label = { Text("App name to look for") }, placeholder = { Text("e.g. Anki, YouTube, Coursera") }, singleLine = true, enabled = keyConfigured, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary, focusedBorderColor = MintPrimary, unfocusedBorderColor = ForestSurfaceBorder), modifier = Modifier.fillMaxWidth())
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Offline Backup", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(10.dp))
            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(ForestSurfaceCard).border(1.dp, ForestSurfaceBorder, RoundedCornerShape(22.dp)).padding(16.dp)) {
                Column {
                    Text("Export or restore app data", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    Text("Includes account info, app settings, tasks, study records and timetable. Your Gemini API key is never included.", color = TextSecondary, fontSize = 12.sp, lineHeight = 16.sp)
                    Spacer(Modifier.height(10.dp)); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { exportLauncher.launch("caitlin-daily-backup.json") }, colors = ButtonDefaults.buttonColors(containerColor = MintPrimary, contentColor = MintPrimaryDark), shape = RoundedCornerShape(50), modifier = Modifier.weight(1f)) { Icon(Icons.Default.FileDownload, null); Spacer(Modifier.width(5.dp)); Text("Export") }
                        Button(onClick = { importLauncher.launch(arrayOf("application/json", "text/plain")) }, colors = ButtonDefaults.buttonColors(containerColor = ForestSurface, contentColor = TextPrimary), shape = RoundedCornerShape(50), modifier = Modifier.weight(1f)) { Icon(Icons.Default.FileUpload, null); Spacer(Modifier.width(5.dp)); Text("Restore") }
                    }
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // =========================================================================
            // HIDDEN EASTER EGG CREDIT SECTION (At the very end of settings)
            // "made by aswin a s is written as a credit section , tapping that will play
            // a whiskey bottle opening and pouring sound with an subtle animation ,
            // a toast will be shown at the bottom with how many days till the next friday
            // and if its a friday say congrats today's the day , enjoy ."
            // (Don't mention about this anywhere in app , keep it as a hidden easter egg)
            // =========================================================================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Caitlin Daily v1.0.0",
                    color = TextTertiary,
                    fontSize = 12.sp,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // The subtle clickable credit section
                Box(
                    modifier = Modifier
                        .scale(animScale.value)
                        .rotate(animRotation.value)
                        .clip(RoundedCornerShape(16.dp))
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
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .testTag("credits_aswin"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "made by aswin a s",
                            color = if (isPouringAnimationActive) AmberGold else TextTertiary,
                            fontSize = 13.sp,
                            fontWeight = if (isPouringAnimationActive) FontWeight.Bold else FontWeight.Normal
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
}
