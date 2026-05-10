package com.niked.fatless.core.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatDuration(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    // %d — минуты, %02d — секунды с ведущим нулем (01, 02...)
    return "$mins:${secs.toString().padStart(2, '0')}"
}

/**
 * Форматирование времени для записи в БД логов: "10.05.26 07:45:12.123"
 */
fun formatLogTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd.MM.yy HH:mm:ss.SSS", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

/**
 * Форматирование времени для имени файла логов: "20260510_073412"
 */
fun formatFileNameTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
    return sdf.format(Date(timestamp))
}