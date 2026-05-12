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
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.niked.fatless.R
import com.niked.fatless.core.utils.Constants.PREFS_NAME
import com.niked.fatless.core.utils.Constants.PREF_STEP_GOAL
import com.niked.fatless.core.utils.Constants.PREF_TODAY_STEPS
import com.niked.fatless.ui.MainActivity

class StepWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val stepsToday = prefs.getInt(PREF_TODAY_STEPS, 0)
        val goal = prefs.getInt(PREF_STEP_GOAL, 10000)

        val isOverstep = stepsToday >= goal

        val labelText = if (isOverstep) context.getString(R.string.widget_oversteps)
        else context.getString(R.string.widget_steps_today)

        val statusText = if (isOverstep) context.getString(R.string.widget_goal_reached)
        else context.getString(R.string.widget_goal_prefix, goal)

        provideContent {
            GlanceTheme {
                StepWidgetContent(
                    steps = stepsToday,
                    goal = goal,
                    label = labelText,
                    status = statusText,
                    isOverstep = isOverstep
                )
            }
        }
    }

    @SuppressLint("RestrictedApi")
    @Composable
    private fun StepWidgetContent(
        steps: Int,
        goal: Int,
        label: String,
        status: String,
        isOverstep: Boolean
    ) {
        val progress = (steps.toFloat() / goal).coerceIn(0f, 1f)

        // ТВОЯ ПАЛИТРА
        val colorToday = Color(0xFFFF9800)      // ColorStepsToday (Оранжевый)
        val colorOverstep = Color(0xFF673AB7)   // ColorOverSteps (Фиолетовый)
        val widgetBg = Color(0xFFF5F5F7)        // AppBackground
        val textPrimary = Color(0xFF1A1D24)     // AppTextPrimary
        val textTertiary = Color(0xFF939BAA)    // AppTextTertiary

        val activeColor = if (isOverstep) colorOverstep else colorToday

        Row(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(widgetBg)
                .padding(16.dp)
                .clickable(actionStartActivity<MainActivity>()),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Инфо о шагах
            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(
                    text = label,
                    style = TextStyle(
                        color = ColorProvider(if (isOverstep) activeColor else textTertiary),
                        fontSize = 10.sp,
                        fontWeight = if (isOverstep) FontWeight.Bold else FontWeight.Normal
                    )
                )
                Text(
                    text = steps.toString(),
                    style = TextStyle(
                        color = ColorProvider(textPrimary),
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = GlanceModifier.width(16.dp))

            // Прогресс
            Column(modifier = GlanceModifier.defaultWeight()) {
                LinearProgressIndicator(
                    progress = progress,
                    modifier = GlanceModifier.fillMaxWidth().height(8.dp),
                    color = ColorProvider(activeColor),
                    backgroundColor = ColorProvider(Color(0xFFEDEDEF)) // AppDisabledBg
                )

                Spacer(modifier = GlanceModifier.height(4.dp))

                Text(
                    text = status,
                    style = TextStyle(
                        color = ColorProvider(if (isOverstep) activeColor else textTertiary),
                        fontSize = 10.sp
                    )
                )
            }
        }
    }
}
