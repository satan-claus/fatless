package com.niked.fatless.core.sensor

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.BitmapFactory
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import androidx.core.graphics.toColorInt
import com.niked.fatless.R
import com.niked.fatless.core.data.AppSettings
import com.niked.fatless.core.utils.Constants
import com.niked.fatless.domain.repository.IActivityRepository
import com.niked.fatless.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class StepService : Service(), SensorEventListener {

    @Inject
    lateinit var settings: AppSettings

    @Inject
    lateinit var activityRepository: IActivityRepository

    private lateinit var sensorManager: SensorManager
    private var wakeLock: PowerManager.WakeLock? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        createNotificationChannel()

        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "FatLess:StepWakeLock")
        wakeLock?.acquire()

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        val notification = createNotification(settings.todaySteps, settings.currentManualSteps)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                Constants.STEP_NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH
            )
        } else {
            startForeground(Constants.STEP_NOTIFICATION_ID, notification)
        }

        reRegisterSensor()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            Constants.ACTION_START_MANUAL -> {
                settings.isManualTracking = true
                settings.manualBaseSteps = -1
                settings.currentManualSteps = 0
            }
            Constants.ACTION_STOP_MANUAL -> {
                settings.isManualTracking = false
            }
            Constants.ACTION_CLEAR_MANUAL -> {
                settings.currentManualSteps = 0
                settings.manualBaseSteps = -1
            }
        }
        reRegisterSensor()
        updateNotification(settings.todaySteps, settings.currentManualSteps)
        return START_STICKY
    }

    private fun reRegisterSensor() {
        val stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        sensorManager.unregisterListener(this)
        sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER && event.values.isNotEmpty()) {
            val totalStepsSinceBoot = event.values[0].toInt()
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            if (settings.lastStepResetDate != today) {
                val yesterdayDate = settings.lastStepResetDate
                val yesterdaySteps = settings.todaySteps

                // СОХРАНЯЕМ ВЧЕРАШНИЙ ДЕНЬ В БАЗУ
                if (yesterdayDate.isNotEmpty() && yesterdaySteps > 0) {
                    serviceScope.launch {
                        activityRepository.saveSteps(yesterdayDate, yesterdaySteps)
                    }
                }

                settings.stepBaseCount = totalStepsSinceBoot
                settings.lastStepResetDate = today
                settings.todaySteps = 0
                if (!settings.isManualTracking) settings.manualBaseSteps = -1
            }

            if (settings.stepBaseCount <= 0) {
                settings.stepBaseCount = totalStepsSinceBoot
            }

            if (totalStepsSinceBoot < settings.stepBaseCount) {
                settings.stepBaseCount = totalStepsSinceBoot - settings.todaySteps
            }

            val dailySteps = totalStepsSinceBoot - settings.stepBaseCount
            if (dailySteps >= 0 && dailySteps != settings.todaySteps) {
                settings.todaySteps = dailySteps
            }

            var currentManual = settings.currentManualSteps
            if (settings.isManualTracking) {
                if (settings.manualBaseSteps == -1 || totalStepsSinceBoot < settings.manualBaseSteps) {
                    settings.manualBaseSteps = totalStepsSinceBoot - settings.currentManualSteps
                }
                currentManual = totalStepsSinceBoot - settings.manualBaseSteps
                if (currentManual >= 0) settings.currentManualSteps = currentManual
            }

            updateNotification(settings.todaySteps, currentManual)
        }
    }

    private fun createNotification(daily: Int, manual: Int): Notification {
        val goal = settings.stepGoal
        val progress = if (goal > 0) (daily * 100 / goal).coerceAtMost(100) else 0

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val contentText = if (settings.isManualTracking) {
            "День: $daily | ЗАМЕР: $manual"
        } else {
            "Прогресс дня: $progress%"
        }

        val largeIcon = BitmapFactory.decodeResource(resources, R.drawable.ic_logo_notification)

        return NotificationCompat.Builder(this, Constants.STEP_CHANNEL_ID)
            .setContentTitle("FatLess: $daily / $goal")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_directions_walk_24)
            .setLargeIcon(largeIcon)
            .setColor("#4CAF50".toColorInt())
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .setProgress(100, progress, false)
            .setOnlyAlertOnce(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                Constants.STEP_CHANNEL_ID,
                Constants.STEP_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                setShowBadge(false)
                setSound(null, null)
                enableVibration(false)
            }
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun updateNotification(daily: Int, manual: Int) {
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(Constants.STEP_NOTIFICATION_ID, createNotification(daily, manual))
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        wakeLock?.release()
        sensorManager.unregisterListener(this)
    }
}
