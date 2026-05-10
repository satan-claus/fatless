package com.niked.fatless.util

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.FileProvider
import com.niked.fatless.BuildConfig
import com.niked.fatless.core.utils.Constants.LOG_TAG
import com.niked.fatless.core.utils.Constants.LogLevel
import com.niked.fatless.core.utils.formatFileNameTime
import com.niked.fatless.core.utils.formatLogTime
import com.niked.fatless.data.local.dao.LogDao
import com.niked.fatless.data.local.entity.LogEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

object AppLogger {

    /**
     * Основной метод записи лога в БД и консоль
     */
    fun log(dao: LogDao, level: LogLevel, tag: String, message: String) {
        val currentTime = System.currentTimeMillis()
        val formattedTime = formatLogTime(currentTime)

        // 1. Вывод в Logcat (КОТалоги) через твой LOG_TAG
        if (BuildConfig.DEBUG) {
            val consoleMessage = "[$level] [$tag] $message"
            if (level == LogLevel.ERROR) {
                Log.e(LOG_TAG, consoleMessage)
            } else {
                Log.d(LOG_TAG, consoleMessage)
            }
        }

        // 2. Асинхронная запись в базу данных Room
        CoroutineScope(Dispatchers.IO).launch {
            try {
                dao.insert(
                    LogEntity(
                        timestamp = formattedTime,
                        level = level.name,
                        tag = tag,
                        message = message
                    )
                )
            } catch (e: Exception) {
                if (BuildConfig.DEBUG) {
                    Log.e(LOG_TAG, "Failed to save log to DB: ${e.message}")
                }
            }
        }
    }

    /**
     * Выгрузка логов в файл, очистка БД и запуск Share Intent
     */
    fun shareLogs(context: Context, dao: LogDao, scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            try {
                val logs = dao.getAll()
                if (logs.isEmpty()) {
                    // Можно добавить Toast "Логи пусты", но это по желанию
                    return@launch
                }

                // 1. Формируем текст файла
                val logText = logs.joinToString("\n") { log ->
                    "${log.timestamp} | ${log.level} | ${log.tag}: ${log.message}"
                }

                // 2. Создаем временный файл в кэше с таймстемпом в имени
                val fileName = "fatless_log_${formatFileNameTime(System.currentTimeMillis())}.txt"
                val file = File(context.cacheDir, fileName)
                file.writeText(logText)

                // 3. Чистим таблицу логов в БД
                dao.clearAll()

                // 4. Запускаем шаринг через FileProvider
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

                context.startActivity(Intent.createChooser(intent, "Share FatLess Logs"))

            } catch (e: Exception) {
                if (BuildConfig.DEBUG) {
                    Log.e(LOG_TAG, "Error during log sharing: ${e.message}")
                }
            }
        }
    }
}
