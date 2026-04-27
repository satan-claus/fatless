package com.niked.fatless.ui.navigation

sealed class Screen(val route: String) {
    object FoodCreate : Screen("food_create/{initName}") {
        fun createRoute(initName: String): String {
            val encodedName = android.net.Uri.encode(initName)
            return "food_create/$encodedName"
        }
    }
    object Nutrition : Screen(route = "nutrition")
    object Settings : Screen(route = "settings")
    object WorkoutCreate : Screen("workout_create")
    object WorkoutList : Screen("workout_list")
    // Для этого экрана нам нужен аргумент в строке
    object WorkoutTimer : Screen("workout_timer/{workoutId}") {
        fun createRoute(workoutId: String) = "workout_timer/$workoutId"
    }
}