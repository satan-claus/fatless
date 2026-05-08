package com.niked.fatless.data.mapper

import android.annotation.SuppressLint
import android.content.Context
import com.niked.fatless.R
import com.niked.fatless.data.local.entities.ExerciseTypeEntity
import com.niked.fatless.domain.model.ExerciseType

@SuppressLint("DiscouragedApi")
fun ExerciseTypeEntity.toDomain(context: Context): ExerciseType {
    val packageName = context.packageName
    val resources = context.resources

    // Ищем ID строки по имени ключа
    val nameResId = resources.getIdentifier(nameKey, "string", packageName)
        .let { if (it != 0) it else R.string.workout_ready_hint }

    // Ищем ID иконки по имени ключа
    val iconResId = resources.getIdentifier(iconKey, "drawable", packageName)
        .let { if (it != 0) it else R.drawable.ic_directions_walk_24_green }

    return ExerciseType(
        id = id,
        nameResId = nameResId,
        metValue = metValue,
        iconResId = iconResId
    )
}
