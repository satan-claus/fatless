package com.niked.fatless.ui.navigation

sealed class Screen(val route: String) {
    object WorkoutList : Screen("workout_list")
    object WorkoutCreate : Screen("workout_create")
    // Для этого экрана нам нужен аргумент в строке
    object WorkoutTimer : Screen("workout_timer/{workoutId}") {
        fun createRoute(workoutId: String) = "workout_timer/$workoutId"
    }
}