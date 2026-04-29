package com.niked.fatless.ui

import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.niked.fatless.core.audio.AndroidAudioPlayer
import com.niked.fatless.core.sensor.StepService
import com.niked.fatless.core.sensor.StepRestartWorker
import com.niked.fatless.ui.navigation.FatLessNavGraph
import com.niked.fatless.ui.theme.FatLessTheme
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var audioPlayer: AndroidAudioPlayer

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
        installSplashScreen()

        super.onCreate(savedInstanceState)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        checkAndRequestPermissions()

        setContent {
            FatLessTheme {
                FatLessNavGraph()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        audioPlayer.checkAndInit()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing) {
            audioPlayer.release()
        }
    }

    fun minimizeApp() {
        moveTaskToBack(true)
    }

    private fun checkAndRequestPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        // 1. Опасное разрешение (нужно подтверждение юзера)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissionsToRequest.add(android.Manifest.permission.ACTIVITY_RECOGNITION)
        }

        // 2. Уведомления (нужно подтверждение для Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(android.Manifest.permission.POST_NOTIFICATIONS)
        }

        // Фильтруем те, что уже даны
        val notGranted = permissionsToRequest.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (notGranted.isEmpty()) {
            startStepService()
        } else {
            requestPermissionLauncher.launch(notGranted.toTypedArray())
        }
    }

    private fun startStepService() {
        // 1. Запускаем сам сервис
        val stepIntent = Intent(this, StepService::class.java)
        ContextCompat.startForegroundService(this, stepIntent)

        // 2. ЗАПУСКАЕМ ПИНАТЕЛЬ (Worker)
        val restartWorkRequest = PeriodicWorkRequestBuilder<StepRestartWorker>(
            15, TimeUnit.MINUTES // Интервал 15 минут (минимум для Android)
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "StepServiceRestart",
            ExistingPeriodicWorkPolicy.KEEP, // Если уже работает — не перезапускать
            restartWorkRequest
        )
    }
}
