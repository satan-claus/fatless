package com.niked.fatless.ui

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
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
import com.niked.fatless.core.utils.AppLogger
import com.niked.fatless.core.utils.Constants.LogLevel
import com.niked.fatless.domain.repository.ISettingsRepository
import com.niked.fatless.ui.navigation.FatLessNavGraph
import com.niked.fatless.ui.theme.FatLessTheme
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import android.provider.Settings

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settingsRepository: ISettingsRepository

    @Inject
    lateinit var audioPlayer: AndroidAudioPlayer

    @Inject
    lateinit var logger: AppLogger

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
            15, TimeUnit.MINUTES
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "StepServiceRestart",
            ExistingPeriodicWorkPolicy.KEEP,
            restartWorkRequest
        )

        // Запрос системного разрешения на игнорирование оптимизации батареи
        // Это спасет воркер и сервис от принудительной блокировки китайским "крестиком"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
                try {
                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                        data = android.net.Uri.parse("package:$packageName")
                    }
                    startActivity(intent)
                    logger.log(LogLevel.SYSTEM, "SERVICE", "Запрошено разрешение на игнорирование оптимизации батареи")
                } catch (e: Exception) {
                    logger.log(LogLevel.ERROR, "SERVICE", "Не удалось открыть системное окно оптимизации: ${e.message}")
                }
            }
        }
    }

    private val enableBtLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            logger.log(LogLevel.INFO, "BLE", "Bluetooth включен юзером")
        }
    }

    fun askToEnableBluetooth() {
        val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        enableBtLauncher.launch(intent)
    }
}
