package com.niked.fatless.ui.screen

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State

@Composable
fun animateNumberAsState(targetValue: Int): State<Int> {
    return animateIntAsState(
        targetValue = targetValue,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "number_anim"
    )
}

@Composable
fun animateFloatNumberAsState(targetValue: Float): State<Float> {
    return animateFloatAsState(
        targetValue = targetValue,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "float_anim"
    )
}
