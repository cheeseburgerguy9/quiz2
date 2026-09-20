package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.ai.GeminiService
import com.example.ui.theme.ExpressiveCardShape
import com.example.ui.theme.GeminiPurple
import com.example.ui.theme.PillShape
import com.example.ui.viewmodel.MainViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val profile by viewModel.userProfile.collectAsState()
    val isAiEnabled by viewModel.isAiEnabled.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val studyBeta by viewModel.studyTimeBetaEnabled.collectAsState()
    val geminiStatus by viewModel.geminiStatus.collectAsState()

    var showProfileEditor by remember { mutableStateOf(false) }
    var apiKey by remember { mutableStateOf(GeminiService.getStoredApiKey().orEmpty()) }

    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            viewModel.updateProfile(profile.name, profile.age, uri)
        }
    }

    val backupCreator = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) viewModel.exportBackup(uri)
    }

    val backupImporter = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) viewModel.importBackup(uri)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Settings", fontWeight = FontWeight.ExtraBold)
                        Text(
                            "Offline account • Gemini • backup • appearance",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Spacer(Modifier.height(4.dp)) }

            item { SectionTitle("Account & Profile") }
            item {
                Card(
                    shape = ExpressiveCardShape,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ProfileAvatar(profile.photoPath, profile.name)
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(profile.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(
                                profile.age?.let { "$it years old" } ?: "Age not set",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "Offline account • no Google sign-in",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        TextButton(onClick = { showProfileEditor = true }) {
                            Text("Edit")
                        }
                    }
                    Row(
                        Modifier.fillMaxWidth().padding(start = 18.dp, end = 18.dp, bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showProfileEditor = true },
                            modifier = Modifier.weight(1f),
                            shape = PillShape
                        ) {
                            Icon(Icons.Default.Person, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Edit profile")
                        }
                        OutlinedButton(
                            onClick = { photoPicker.launch("image/*") },
                            modifier = Modifier.weight(1f),
                            shape = PillShape
                        ) {
                            Text("Change photo")
                        }
                    }
                }
            }

            item { SectionTitle("Gemini AI") }
            item {
                Card(
                    shape = ExpressiveCardShape,
                    colors = CardDefaults.cardColors(
                        containerColor = if (geminiStatus.isConfigured)
                            GeminiPurple.copy(alpha = 0.09f)
                        else MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                ) {
                    Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, null, tint = GeminiPurple)
                            Spacer(Modifier.width(8.dp))
                            Column(Modifier.weight(1f)) {
                                Text("Bring your own Gemini API key", fontWeight = FontWeight.Bold)
                                Text(
                                    geminiStatus.statusMessage,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(checked = isAiEnabled, onCheckedChange = viewModel::setAiEnabled)
                        }

                        OutlinedTextField(
                            value = apiKey,
                            onValueChange = { apiKey = it },
                            label = { Text("Gemini API key") },
                            placeholder = { Text("Paste your key here") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp)
                        )

                        Text(
                            "The key is encrypted with Android Keystore and is never included in the source code, APK, or GitHub repository.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { viewModel.setGeminiApiKey(apiKey.trim()) },
                                enabled = apiKey.isNotBlank(),
                                shape = PillShape,
                                modifier = Modifier.weight(1f)
                            ) { Text("Save key") }

                            OutlinedButton(
                                onClick = {
                                    apiKey = ""
                                    viewModel.clearGeminiApiKey()
                                },
                                shape = PillShape,
                                modifier = Modifier.weight(1f)
                            ) { Text("Remove") }
                        }

                        OutlinedButton(
                            onClick = {
                                context.startActivity(
                                    Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("https://aistudio.google.com/app/apikey")
                                    )
                                )
                            },
                            shape = PillShape,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.OpenInBrowser, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Get a Gemini API key")
                        }

                        Text(
                            "How: open Google AI Studio → sign in → Create API key → choose/create a Google Cloud project → copy the key → paste it above. API usage is billed/limited by your Google AI Studio or Google Cloud account, not by Caitlin Daily.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        OutlinedButton(
                            onClick = { viewModel.pingGemini() },
                            enabled = geminiStatus.isConfigured && !geminiStatus.isPinging,
                            shape = PillShape,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (geminiStatus.isPinging) "Testing…" else "Test Gemini connection")
                        }

                        Text(
                            "Model: ${GeminiService.MODEL_NAME}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            item { SectionTitle("Offline backup & restore") }
            item {
                Card(
                    shape = ExpressiveCardShape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                ) {
                    Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Your backup stays under your control", fontWeight = FontWeight.Bold)
                        Text(
                            "The JSON backup includes your name, age, profile photo, app date, settings, tasks, verification metadata and timetable. It does not include your Gemini API key.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { backupCreator.launch("caitlin-daily-backup.json") },
                                shape = PillShape,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Backup, null, Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Export")
                            }
                            OutlinedButton(
                                onClick = { backupImporter.launch(arrayOf("application/json", "text/plain")) },
                                shape = PillShape,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Restore, null, Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Import")
                            }
                        }
                        Text(
                            "Import replaces the current local tasks/timetable/profile with the backup contents.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            item { SectionTitle("Appearance") }
            item {
                Card(
                    shape = ExpressiveCardShape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                ) {
                    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Material You / dynamic color", fontWeight = FontWeight.Bold)
                        Text(
                            "On Android 12+ the app follows the device's dynamic Material palette. Android 16/17 devices can therefore supply their own system-derived colors.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(
                                Triple("SYSTEM", "System", Icons.Default.PhoneAndroid),
                                Triple("LIGHT", "Light", Icons.Default.LightMode),
                                Triple("DARK", "Dark", Icons.Default.DarkMode)
                            ).forEach { (mode, label, icon) ->
                                FilterChip(
                                    selected = themeMode == mode,
                                    onClick = { viewModel.setThemeMode(mode) },
                                    label = { Text(label) },
                                    leadingIcon = { Icon(icon, null, Modifier.size(16.dp)) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            item { SectionTitle("Experimental") }
            item {
                Card(
                    shape = ExpressiveCardShape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Science, null, tint = MaterialTheme.colorScheme.tertiary)
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Study time from screenshots", fontWeight = FontWeight.Bold)
                            Text(
                                "Uses Gemini vision to read Digital Wellbeing or battery screenshots.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(checked = studyBeta, onCheckedChange = viewModel::setStudyTimeBetaEnabled)
                    }
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }

    if (showProfileEditor) {
        ProfileEditorDialog(
            initialName = profile.name,
            initialAge = profile.age,
            onDismiss = { showProfileEditor = false },
            onSave = { name, age ->
                viewModel.updateProfile(name, age, null)
                showProfileEditor = false
            },
            onPickPhoto = { photoPicker.launch("image/*") }
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun ProfileAvatar(path: String?, name: String) {
    if (path != null) {
        AsyncImage(
            model = File(path),
            contentDescription = "Profile photo",
            modifier = Modifier.size(56.dp).clip(CircleShape)
        )
    } else {
        Box(
            Modifier.size(56.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                name.take(1).uppercase(),
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun ProfileEditorDialog(
    initialName: String,
    initialAge: Int?,
    onDismiss: () -> Unit,
    onSave: (String, Int?) -> Unit,
    onPickPhoto: () -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var age by remember { mutableStateOf(initialAge?.toString().orEmpty()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit offline profile") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = age,
                    onValueChange = { value -> if (value.all(Char::isDigit) && value.length <= 3) age = value },
                    label = { Text("Age (optional)") },
                    singleLine = true
                )
                OutlinedButton(onClick = onPickPhoto, shape = PillShape, modifier = Modifier.fillMaxWidth()) {
                    Text("Choose profile photo")
                }
            }
        },
        confirmButton = {
            Button(onClick = { onSave(name, age.toIntOrNull()) }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
