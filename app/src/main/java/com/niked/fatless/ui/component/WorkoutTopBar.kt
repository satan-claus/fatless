package com.niked.fatless.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.niked.fatless.ui.theme.AppBorder
import com.niked.fatless.ui.theme.AppSurface
import com.niked.fatless.ui.theme.AppTextPrimary
import com.niked.fatless.ui.theme.AppTextSecondary

@Composable
fun WorkoutTopBar(
    title: String,
    subTitle: String,
    subTitleColor: Color = AppTextSecondary,
    onBackClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(56.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Рисуем кнопку Back только если передан колбэк
        if (onBackClick != null) {
            Surface(
                onClick = onBackClick,
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = AppSurface,
                border = BorderStroke(1.dp, AppBorder)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "‹",
                        fontSize = 24.sp,
                        color = AppTextSecondary,
                        modifier = Modifier.offset(y = (-1).dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
        }

        // 2. Заголовок (занимает всё свободное место)
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = AppTextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(8.dp))

        // 3. Субтитры (Время или кол-во тренировок)
        Text(
            text = subTitle,
            style = MaterialTheme.typography.bodySmall,
            color = subTitleColor,
            maxLines = 1
        )

        // 4. ДОБАВЛЯЕМ: Место для кнопок справа
        Row(
            verticalAlignment = Alignment.CenterVertically,
            content = actions
        )
    }
}