package com.niked.fatless.ui

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.niked.fatless.core.sensor.StepService
import com.niked.fatless.ui.navigation.FatLessNavGraph
import com.niked.fatless.ui.theme.FatLessTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // 1. Создаем лаунчер для запроса пачки разрешений
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Если разрешение на активность дали — запускаем сервис
        val activityGranted = permissions[android.Manifest.permission.ACTIVITY_RECOGNITION] ?: false
        if (activityGranted) {
            startStepService()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkAndRequestPermissions()

        setContent {
            FatLessTheme {
                FatLessNavGraph()
            }
        }
    }

    fun minimizeApp() {
        moveTaskToBack(true)
    }

    private fun checkAndRequestPermissions() {
        val permissions = mutableListOf(
            android.Manifest.permission.ACTIVITY_RECOGNITION,
            android.Manifest.permission.FOREGROUND_SERVICE_HEALTH
        )

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            permissions.add(android.Manifest.permission.POST_NOTIFICATIONS)
        }

        val allGranted = permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }

        if (allGranted) {
            startStepService()
        } else {
            // Запрашиваем через новый лаунчер
            requestPermissionLauncher.launch(permissions.toTypedArray())
        }
    }

    private fun startStepService() {
        val stepIntent = Intent(this, StepService::class.java)
        ContextCompat.startForegroundService(this, stepIntent)
    }
}
