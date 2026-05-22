package com.niked.fatless.core.utils

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.FileProvider
import com.niked.fatless.BuildConfig
import com.niked.fatless.R
import com.niked.fatless.core.utils.Constants.LOG_TAG
import com.niked.fatless.core.utils.Constants.LogLevel
import com.niked.fatless.data.local.dao.LogDao
import com.niked.fatless.data.local.entity.LogEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * LogLevel.SYSTEM:
 * - StepService.onCreate / onDestroy — «Жив ли сервис?».
 * - onSensorChanged (смена дня) — «Улетели ли данные?».
 * - syncCurrentDayToDatabase — «Зафиксирован ли текущий день?».
 *
 * LogLevel.ERROR:
 * - Все catch блоки.
 * - sensorManager.getDefaultSensor == null.
 *
 * LogLevel.INFO:
 * - selectDate(date) — «Куда нажал юзер?».
 * - nextMonth() / prevMonth() — «Что он там листал?».
 */
@Singleton
class AppLogger @Inject constructor(
    private val logDao: LogDao
) {

    /**
     * Основной метод записи лога
     */
    fun log(level: LogLevel, tag: String, message: String) {
        val currentTime = System.currentTimeMillis()
        val formattedTime = formatLogTime(currentTime)

        // 1. КОНСОЛЬ (Logcat): только в Debug
        if (BuildConfig.DEBUG) {
            val consoleMessage = "[$level] [$tag] $message"
            if (level == LogLevel.ERROR) Log.e(LOG_TAG, consoleMessage)
            else Log.d(LOG_TAG, consoleMessage)
        }

        // 2. БАЗА ДАННЫХ: Фильтруем, что записывать в релизе
        val shouldRecord = when (level) {
            LogLevel.ERROR, LogLevel.SYSTEM -> true // Пишем всегда
            LogLevel.INFO, LogLevel.DEBUG -> BuildConfig.DEBUG // Пишем только если Debug
        }

        // 2. Асинхронная запись в БД
        if (shouldRecord) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    logDao.insert(
                        LogEntity(
                            timestamp = formattedTime,
                            level = level.name,
                            tag = tag,
                            message = message
                        )
                    )
                } catch (e: Exception) {
                    if (BuildConfig.DEBUG) {
                        Log.e(LOG_TAG, "Database log error: ${e.message}")
                    }
                }
            }
        }
    }

    /**
     * Формирование файла, очистка БД и шаринг
     */
    fun shareLogs(context: Context, scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            try {
                val logs = logDao.getAll()
                if (logs.isEmpty()) return@launch

                // Формируем текст
                val logText = logs.joinToString("\n") { log ->
                    "${log.timestamp} | ${log.level} | ${log.tag}: ${log.message}"
                }

                // Создаем файл в кэше через Formatter
                val fileName = "fatless_log_${formatFileNameTime(System.currentTimeMillis())}.txt"
                val file = File(context.cacheDir, fileName)
                file.writeText(logText)

                // Чистим базу сразу после выгрузки
                logDao.clearAll()

                // Шаринг через FileProvider
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )

                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }

                val dialogTitle = context.getString(R.string.settings_logs_chooser_title)
                context.startActivity(Intent.createChooser(intent, dialogTitle))

            } catch (e: Exception) {
                if (BuildConfig.DEBUG) {
                    Log.e(LOG_TAG, "Sharing error: ${e.message}")
                }
            }
        }
    }
}
