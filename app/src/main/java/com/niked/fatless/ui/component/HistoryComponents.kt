package com.niked.fatless.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.niked.fatless.R
import com.niked.fatless.domain.model.DailyActivity
import com.niked.fatless.ui.theme.AppRed
import com.niked.fatless.ui.theme.AppSecondary
import com.niked.fatless.ui.theme.AppTextPrimary
import com.niked.fatless.ui.theme.AppTextSecondary
import com.niked.fatless.ui.theme.AppTextTertiary
import com.niked.fatless.ui.theme.AppTypography
import com.niked.fatless.ui.theme.ColorCalories
import com.niked.fatless.ui.theme.ColorCarbohydrates
import com.niked.fatless.ui.theme.ColorFats
import com.niked.fatless.ui.theme.ColorOverSteps
import com.niked.fatless.ui.theme.ColorProteins
import com.niked.fatless.ui.theme.ColorSteps
import com.niked.fatless.ui.theme.ColorStepsToday
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun CalendarGrid(
    month: YearMonth,
    selectedDate: LocalDate,
    monthData: List<DailyActivity>,
    onDateClick: (LocalDate) -> Unit
) {
    val daysInMonth = month.lengthOfMonth()
    val firstDayOfWeek = month.atDay(1).dayOfWeek.value
    val offset = firstDayOfWeek - 1

    // Весь список ячеек (пустые + числа)
    val totalCells = offset + daysInMonth
    val rows = (totalCells + 6) / 7

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        for (row in 0 until rows) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (column in 0 until 7) {
                    val cellIndex = row * 7 + column
                    val dayNumber = cellIndex - offset + 1

                    Box(modifier = Modifier.weight(1f)) {
                        if (dayNumber in 1..daysInMonth) {
                            val date = month.atDay(dayNumber)
                            val hasData = monthData.any { it.date == date.toString() }

                            DayItem(
                                day = dayNumber,
                                isSelected = selectedDate == date,
                                hasData = hasData,
                                isToday = date == LocalDate.now(),
                                onClick = { onDateClick(date) }
                            )
                        } else {
                            Spacer(modifier = Modifier.aspectRatio(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DaysOfWeekHeader() {
    val days = listOf(
        R.string.day_mon_short to R.string.content_description_day_mon,
        R.string.day_tue_short to R.string.content_description_day_tue,
        R.string.day_wed_short to R.string.content_description_day_wed,
        R.string.day_thu_short to R.string.content_description_day_thu,
        R.string.day_fri_short to R.string.content_description_day_fri,
        R.string.day_sat_short to R.string.content_description_day_sat,
        R.string.day_sun_short to R.string.content_description_day_sun
    )

    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        days.forEach { (shortRes, fullRes) ->
            val fullDayName = stringResource(fullRes)
            Text(
                text = stringResource(shortRes),
                modifier = Modifier
                    .weight(1f)
                    .clearAndSetSemantics { contentDescription = fullDayName },
                textAlign = TextAlign.Center,
                style = AppTypography.labelMedium,
                color = if (shortRes == R.string.day_sat_short || shortRes == R.string.day_sun_short) AppRed else AppTextSecondary
            )
        }
    }
}

@Composable
fun DayItem(
    day: Int,
    isSelected: Boolean,
    hasData: Boolean,
    isToday: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) AppSecondary else Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = day.toString(),
                style = AppTypography.bodyMedium,
                color = when {
                    isSelected -> Color.White
                    isToday -> AppSecondary
                    else -> AppTextPrimary
                },
                fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal
            )
            if (hasData) {
                Box(
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) Color.White else AppSecondary)
                )
            }
        }
    }
}

@Composable
fun DayHistoryDetails(activity: DailyActivity?) {
    if (activity == null) {
        Box(
            modifier = Modifier.fillMaxWidth().padding(48.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.history_no_data),
                style = AppTypography.bodyMedium,
                color = AppTextTertiary
            )
        }
        return
    }

    Column(modifier = Modifier.padding(24.dp).fillMaxWidth()) {
        Text(
            text = stringResource(R.string.history_energy_balance),
            style = AppTypography.titleSmall,
            color = AppTextPrimary
        )
        Spacer(modifier = Modifier.height(16.dp))

        EnergySaldoBar(consumed = activity.consumedCalories, burned = activity.burnedCalories)

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = stringResource(R.string.history_macronutrients),
            style = AppTypography.titleSmall,
            color = AppTextPrimary
        )
        Spacer(modifier = Modifier.height(16.dp))

        BjuRow(p = activity.proteins, f = activity.fats, c = activity.carbs)
    }
}

@Composable
fun EnergySaldoBar(consumed: Float, burned: Float) {
    val total = (consumed + burned).coerceAtLeast(1f)
    val consumedWeight = consumed / total

    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                text = stringResource(R.string.history_consumed_format, consumed.toInt()),
                style = AppTypography.bodySmall,
                color = ColorCalories // Синий
            )
            Text(
                text = stringResource(R.string.history_burned_format, burned.toInt()),
                style = AppTypography.bodySmall,
                color = ColorCarbohydrates // Красный
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth().height(10.dp).clip(CircleShape).background(ColorCarbohydrates.copy(alpha = 0.2f))) {
            // Синяя часть (Съедено)
            Box(modifier = Modifier.fillMaxHeight().weight(consumedWeight.coerceAtLeast(0.01f)).background(ColorCalories))
            // Красная часть (Сжёг)
            Box(modifier = Modifier.fillMaxHeight().weight((1f - consumedWeight).coerceAtLeast(0.01f)).background(ColorCarbohydrates))
        }
    }
}


@Composable
fun BjuRow(p: Float, f: Float, c: Float) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Белки — Зеленый
        BjuItem(stringResource(R.string.history_proteins), p, ColorProteins)

        // Жиры — Желтый
        BjuItem(stringResource(R.string.history_fats), f, ColorFats)

        // Углеводы — Красный
        BjuItem(stringResource(R.string.history_carbs), c, ColorCarbohydrates)
    }
}

@Composable
fun BjuItem(label: String, value: Float, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = stringResource(R.string.history_grams_format, value.toInt()),
            style = AppTypography.bodyLarge,
            color = color,
            fontWeight = FontWeight.Bold
        )
        Text(text = label, style = AppTypography.labelSmall, color = AppTextTertiary)
    }
}

@Composable
fun WeightChart(data: List<DailyActivity>) {
    // Если данных нет совсем — уходим
    if (data.isEmpty()) return

    val weights = data.map { it.weight }
    val minWeight = weights.minOrNull() ?: 0f
    val maxWeight = weights.maxOrNull() ?: 0f

    // Защита от деления на 0: если вес один и тот же, берем диапазон в 2 кг
    val range = (maxWeight - minWeight).let { if (it == 0f) 2f else it }

    Column(modifier = Modifier.padding(24.dp)) {
        Text(
            text = stringResource(R.string.history_weight_chart),
            style = AppTypography.titleSmall,
            color = AppTextPrimary
        )
        Spacer(modifier = Modifier.height(16.dp))

        Canvas(modifier = Modifier.fillMaxWidth().height(150.dp)) {
            val paddingPx = 8.dp.toPx()
            val usableWidth = size.width - (paddingPx * 2)
            val height = size.height

            // 1. РИСУЕМ ФОНОВУЮ СЕТКУ (Min / Max линии)
            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            // Линия Max
            drawLine(
                color = AppTextTertiary.copy(alpha = 0.2f),
                start = Offset(0f, 0f),
                end = Offset(size.width, 0f),
                pathEffect = dashEffect
            )
            // Линия Min
            drawLine(
                color = AppTextTertiary.copy(alpha = 0.2f),
                start = Offset(0f, height),
                end = Offset(size.width, height),
                pathEffect = dashEffect
            )

            if (data.size > 1) {
                // 2. РИСУЕМ ТРЕНД (если 2+ точки)
                val spacing = usableWidth / (data.size - 1)
                val points = data.indices.map { i ->
                    val x = i * spacing + paddingPx
                    val y = height - ((data[i].weight - minWeight) / range) * height
                    Offset(x, y)
                }

                val path = Path().apply {
                    moveTo(points.first().x, points.first().y)
                    for (i in 1 until points.size) lineTo(points[i].x, points[i].y)
                }

                drawPath(
                    path = path,
                    color = AppSecondary,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )

                points.forEach { point ->
                    drawCircle(AppSecondary, 4.dp.toPx(), point)
                    drawCircle(Color.White, 2.dp.toPx(), point)
                }
            } else {
                // 3. РИСУЕМ ОДНУ ТОЧКУ (если замер только один)
                val centerPoint = Offset(size.width / 2, height / 2)
                drawCircle(AppSecondary, 6.dp.toPx(), centerPoint)
                drawCircle(Color.White, 3.dp.toPx(), centerPoint)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 4. УМНЫЕ ПОДПИСИ (Логика: Начало -> Текущий)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            if (data.size > 1) {
                // Вес самого первого дня в списке
                Text(
                    text = stringResource(R.string.history_weight_format, data.first().weight),
                    style = AppTypography.labelSmall,
                    color = AppTextTertiary
                )
                // Вес последнего дня в списке (сегодня)
                Text(
                    text = stringResource(R.string.history_weight_format, data.last().weight),
                    style = AppTypography.labelSmall,
                    color = AppSecondary,
                    fontWeight = FontWeight.Bold
                )
            } else {
                // Если точка одна
                Text(
                    text = "Старт: ${data.first().weight} кг",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    style = AppTypography.labelSmall,
                    color = AppTextTertiary
                )
            }
        }
    }
}

@Composable
fun ActivityChart(
    hourlySteps: String,
    isToday: Boolean,
    stepGoal: Int
) {
    // 1. Парсим данные
    val data = try {
        hourlySteps.split(",").map { it.toFloatOrNull() ?: 0f }
    } catch (e: Exception) {
        List(8) { 0f }
    }

    // 2. Вычисляем состояние дня
    val totalSteps = data.sum().toInt()
    val isGoalReached = totalSteps >= stepGoal

    // Находим самый высокий столбик для звезды (только если есть шаги)
    val maxStepsValue = data.maxOrNull() ?: 0f
    val championIndex = if (isGoalReached && maxStepsValue > 0f) data.indexOf(maxStepsValue) else -1

    // 3. Определяем цвета по твоему стандарту
    val barColor = when {
        isGoalReached -> ColorOverSteps // Фиолетовый
        isToday -> ColorStepsToday      // Оранжевый
        else -> ColorSteps              // Синий
    }

    val labels = listOf(
        R.string.history_interval_0, R.string.history_interval_1,
        R.string.history_interval_2, R.string.history_interval_3,
        R.string.history_interval_4, R.string.history_interval_5,
        R.string.history_interval_6, R.string.history_interval_7
    )

    Column(modifier = Modifier.padding(24.dp)) {
        Text(
            text = stringResource(R.string.history_activity_chart_title),
            style = AppTypography.titleSmall,
            color = AppTextPrimary
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            data.forEachIndexed { index, steps ->
                // Рассчитываем высоту столбика (минимум 5% для видимости)
                val barHeightFraction = if (maxStepsValue > 0) {
                    (steps / maxStepsValue).coerceIn(0.05f, 1f)
                } else 0.05f

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    // Число шагов над столбиком
                    if (steps > 0) {
                        Text(
                            text = steps.toInt().toString(),
                            style = AppTypography.labelSmall,
                            color = barColor,
                            modifier = Modifier.padding(bottom = 2.dp),
                            maxLines = 1
                        )
                    }

                    // Контейнер столбика
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight(barHeightFraction)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(if (steps > 0) barColor else AppTextTertiary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            // ЗОЛОТАЯ ЗВЕЗДА ГЕРОЯ
                            if (index == championIndex) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier
                                        .padding(top = 2.dp)
                                        .size(10.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Подпись времени
                    Text(
                        text = stringResource(labels[index]),
                        style = AppTypography.labelSmall,
                        color = AppTextTertiary
                    )
                }
            }
        }
    }
}




