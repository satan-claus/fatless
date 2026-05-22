package com.niked.fatless.domain.model

/**
 * Доменная модель записи в дневнике питания.
 * Используется в UseCase и UI, полностью независима от Room/Entities.
 */
data class DiaryEntry(
    val id: Long,               // Соответствует entryId из базы
    val foodId: String,
    val foodName: String,
    val quantity: Int,          // Количество (гр/мл/шт)
    val unit: String,
    val totalProteins: Float,   // посчитанные calcProteins
    val totalFats: Float,       // посчитанные calcFats
    val totalCarbs: Float,      // посчитанные calcCarbs
    val totalCalories: Int,     // посчитанный calcCalories
    val dateTimestamp: Long
)