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
import com.niked.fatless.domain.repository.ISettingsRepository
import com.niked.fatless.ui.navigation.FatLessNavGraph
import com.niked.fatless.ui.theme.FatLessTheme
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settingsRepository: ISettingsRepository

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

        // Инициализация OSMDroid
        org.osmdroid.config.Configuration.getInstance().load(
            applicationContext,
            android.preference.PreferenceManager.getDefaultSharedPreferences(applicationContext)
        )

        setContent {
            FatLessTheme {
                FatLessNavGraph(settingsRepository = settingsRepository)
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

        // 1. Шаги (ACTIVITY_RECOGNITION) - Нужно для всех (minSdk 29+)
        permissionsToRequest.add(android.Manifest.permission.ACTIVITY_RECOGNITION)

        // 2. Уведомления (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(android.Manifest.permission.POST_NOTIFICATIONS)
        }

        // 3. GPS (Нужен для Карт и для Bluetooth на Android 10-11)
        permissionsToRequest.add(android.Manifest.permission.ACCESS_FINE_LOCATION)
        permissionsToRequest.add(android.Manifest.permission.ACCESS_COARSE_LOCATION)

        // 4. Bluetooth (Android 12+)
        // На API < 31 эти разрешения вызовут крэш, если их добавить в список
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissionsToRequest.add(android.Manifest.permission.BLUETOOTH_SCAN)
            permissionsToRequest.add(android.Manifest.permission.BLUETOOTH_CONNECT)
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
