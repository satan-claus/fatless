package com.niked.fatless.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.niked.fatless.domain.model.Interval
import com.niked.fatless.ui.theme.AppTypography
import com.niked.fatless.ui.theme.AppBackground
import com.niked.fatless.ui.theme.AppOrange
import com.niked.fatless.ui.theme.AppOrangeLight
import com.niked.fatless.ui.theme.AppPrimary
import com.niked.fatless.ui.theme.AppPrimaryLight
import com.niked.fatless.ui.theme.AppSecondary
import com.niked.fatless.ui.theme.AppSurface
import com.niked.fatless.ui.theme.AppTextPrimary
import com.niked.fatless.ui.theme.AppTextSecondary
import com.niked.fatless.ui.theme.AppTextTertiary
import com.niked.fatless.utils.formatDuration

@Composable
fun IntervalCard(
    interval: Interval,
    index: Int,
    isActive: Boolean = false,
    isPaused: Boolean = false,
    isCompleted: Boolean = false,
    progress: Float = 0f,
    isFinalDone: Boolean = false
) {
    val activeColor = if (isPaused) AppOrange else AppPrimary
    val activeLightColor = if (isPaused) AppOrangeLight else AppPrimaryLight
    val containerAlpha = if (isFinalDone) 0.55f else if (isCompleted) 0.45f else 1f

    val badgeBgColor = if (isActive) activeColor else AppBackground
    val badgeTextColor = if (isActive) Color.White else AppTextSecondary

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(containerAlpha),
        shape = RoundedCornerShape(12.dp),
        color = AppSurface,
        border = BorderStroke(1.5.dp, if (isActive) activeColor else Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    if (isActive && progress > 0f) {
                        drawRect(
                            color = activeLightColor,
                            size = size.copy(width = size.width * progress)
                        )
                    }
                }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        modifier = Modifier.size(28.dp),
                        shape = CircleShape,
                        color = badgeBgColor
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (isCompleted || isFinalDone) {
                                val checkColor = if (isFinalDone) AppSecondary else AppTextTertiary
                                Text(
                                    text = "✓",
                                    color = if (isActive) Color.White else checkColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            } else {
                                Text(
                                    text = index.toString(),
                                    style = AppTypography.bodySmall.copy(
                                        fontSize = 11.sp,
                                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = badgeTextColor
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = interval.name,
                        // Roboto 14 Medium
                        style = AppTypography.labelLarge,
                        color = AppTextPrimary,
                        textDecoration = if (isCompleted || isFinalDone) TextDecoration.LineThrough else null,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = formatDuration(interval.seconds),
                    style = AppTypography.bodyLarge.copy(
                        fontFamily = AppTypography.displayLarge.fontFamily,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = if (isFinalDone) AppSecondary else if (isActive) activeColor else AppTextPrimary
                )
            }
        }
    }
}
