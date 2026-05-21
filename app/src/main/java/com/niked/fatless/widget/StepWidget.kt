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
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
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

        provideContent {
            GlanceTheme {
                StepWidgetContent(stepsToday, goal, isOverstep, context)
            }
        }
    }

    @SuppressLint("RestrictedApi")
    @Composable
    private fun StepWidgetContent(steps: Int, goal: Int, isOverstep: Boolean, context: Context) {
        val colorToday = Color(0xFFFF9800)      // Оранжевый
        val colorOverstep = Color(0xFF673AB7)   // Фиолетовый
        val widgetBg = Color(0xFFF5F5F7)        // AppBackground
        val activeColor = if (isOverstep) colorOverstep else colorToday
        val progress = (steps.toFloat() / goal).coerceIn(0f, 1f)

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .appWidgetBackground()
                .background(widgetBg)
                .cornerRadius(16.dp)
                .clickable(actionStartActivity<MainActivity>())
        ) {
            Row(
                modifier = GlanceModifier.fillMaxSize().padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = GlanceModifier.defaultWeight()) {
                    Text(
                        text = if (isOverstep) context.getString(R.string.widget_oversteps) else context.getString(R.string.widget_steps_today),
                        style = TextStyle(color = ColorProvider(if (isOverstep) activeColor else Color(0xFF939BAA)), fontSize = 10.sp)
                    )
                    Text(
                        text = steps.toString(),
                        style = TextStyle(color = ColorProvider(Color(0xFF1A1D24)), fontSize = 26.sp, fontWeight = FontWeight.Bold)
                    )
                }
                Spacer(modifier = GlanceModifier.width(8.dp))
                Column(modifier = GlanceModifier.defaultWeight()) {
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = GlanceModifier.fillMaxWidth().height(8.dp),
                        color = ColorProvider(activeColor),
                        backgroundColor = ColorProvider(Color(0xFFEDEDEF))
                    )
                    Spacer(modifier = GlanceModifier.height(4.dp))
                    Text(
                        text = if (isOverstep) context.getString(R.string.widget_goal_reached) else context.getString(R.string.widget_goal_prefix, goal),
                        style = TextStyle(color = ColorProvider(Color(0xFF939BAA)), fontSize = 10.sp)
                    )
                }
            }
        }
    }
}
