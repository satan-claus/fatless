package com.niked.fatless.core.utils

fun formatDuration(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    // %d — минуты, %02d — секунды с ведущим нулем (01, 02...)
    return "$mins:${secs.toString().padStart(2, '0')}"
}