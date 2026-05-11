package com.niked.fatless.widget

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.LinearProgressIndicator
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.niked.fatless.core.utils.Constants.PREFS_NAME
import com.niked.fatless.core.utils.Constants.PREF_STEP_GOAL
import com.niked.fatless.core.utils.Constants.PREF_TODAY_STEPS
import com.niked.fatless.ui.MainActivity

class StepWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // Достаем данные из твоих настроек по константам
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val stepsToday = prefs.getInt(PREF_TODAY_STEPS, 0)
        val goal = prefs.getInt(PREF_STEP_GOAL, 10000)

        provideContent {
            GlanceTheme {
                StepWidgetContent(steps = stepsToday, goal = goal)
            }
        }
    }

    @SuppressLint("RestrictedApi")
    @Composable
    private fun StepWidgetContent(steps: Int, goal: Int) {
        // Вычисляем прогресс (для бара максимум 1.0)
        val progress = (steps.toFloat() / goal).coerceIn(0f, 1f)

        // ЦВЕТОВАЯ ЛОГИКА
        val isOverstep = steps >= goal
        val activeColor = if (isOverstep) Color(0xFFBB86FC) else Color(0xFFFFA500) // Фиолетовый vs Оранжевый

        Row(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(Color(0xFF1C1C1E))
                .padding(16.dp)
                .clickable(actionStartActivity<MainActivity>()),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(
                    text = if (isOverstep) "СВЕРХШАГИ!" else "ШАГИ СЕГОДНЯ",
                    style = TextStyle(
                        color = ColorProvider(if (isOverstep) activeColor else Color.Gray),
                        fontSize = 10.sp,
                        fontWeight = if (isOverstep) FontWeight.Bold else FontWeight.Normal
                    )
                )
                Text(
                    text = steps.toString(),
                    style = TextStyle(
                        color = ColorProvider(Color.White),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = GlanceModifier.width(16.dp))

            Column(modifier = GlanceModifier.defaultWeight()) {
                LinearProgressIndicator(
                    progress = progress,
                    modifier = GlanceModifier.fillMaxWidth().height(8.dp),
                    // Красим бар в зависимости от прогресса
                    color = ColorProvider(activeColor),
                    backgroundColor = ColorProvider(Color.Gray.copy(alpha = 0.2f))
                )

                Spacer(modifier = GlanceModifier.height(4.dp))

                Text(
                    text = if (isOverstep) "ЦЕЛЬ ВЫПОЛНЕНА" else "ЦЕЛЬ: $goal",
                    style = TextStyle(
                        color = ColorProvider(if (isOverstep) activeColor else Color.Gray),
                        fontSize = 10.sp
                    )
                )
            }
        }
    }
}
