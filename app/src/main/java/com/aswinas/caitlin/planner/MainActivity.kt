package com.aswinas.caitlin.planner

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import com.aswinas.caitlin.planner.ui.screens.MainScreen
import com.aswinas.caitlin.planner.ui.theme.CaitlinDailyTheme
import com.aswinas.caitlin.planner.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        requestExactAlarmAccessIfNeeded()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeMode by viewModel.themeMode.collectAsState()
            val isDynamicColor by viewModel.isDynamicColor.collectAsState()
            val isDarkTheme = when (themeMode.lowercase()) {
                "light" -> false
                "dark" -> true
                else -> isSystemInDarkTheme()
            }

            CaitlinDailyTheme(
                darkTheme = isDarkTheme,
                dynamicColor = isDynamicColor
            ) {
                MainScreen(viewModel = viewModel)
            }

            LaunchedEffect(Unit) {
                requestAppPermissions()
            }
        }
    }

    private fun requestAppPermissions() {
        val permissions = buildList {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(
                    this@MainActivity,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) add(Manifest.permission.POST_NOTIFICATIONS)

            if (ContextCompat.checkSelfPermission(
                    this@MainActivity,
                    Manifest.permission.READ_CALENDAR
                ) != PackageManager.PERMISSION_GRANTED
            ) add(Manifest.permission.READ_CALENDAR)

            if (ContextCompat.checkSelfPermission(
                    this@MainActivity,
                    Manifest.permission.WRITE_CALENDAR
                ) != PackageManager.PERMISSION_GRANTED
            ) add(Manifest.permission.WRITE_CALENDAR)
        }

        if (permissions.isEmpty()) {
            requestExactAlarmAccessIfNeeded()
        } else {
            permissionLauncher.launch(permissions.toTypedArray())
        }
    }

    private fun requestExactAlarmAccessIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(ALARM_SERVICE) as android.app.AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                startActivity(
                    Intent(
                        Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                        Uri.parse("package:$packageName")
                    )
                )
            }
        }
    }
}
