package com.niked.fatless.ui.navigation

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object FoodForm : Screen("food_form?initName={initName}&foodId={foodId}") {
        fun createForNew(name: String) = "food_form?initName=${android.net.Uri.encode(name)}"
        fun createForEdit(id: String) = "food_form?foodId=$id"
    }
    object Nutrition : Screen(route = "nutrition")
    object Settings : Screen(route = "settings")
    object WorkoutCreate : Screen("workout_create?workoutId={workoutId}") {
        // Вызов для создания новой (без ID)
        fun createRoute() = "workout_create"
        // Вызов для редактирования (с ID)
        fun editRoute(workoutId: String) = "workout_create?workoutId=$workoutId"
    }
    // Для этого экрана нам нужен аргумент в строке
    object WorkoutTimer : Screen("workout_timer/{workoutId}") {
        fun createRoute(workoutId: String) = "workout_timer/$workoutId"
    }
}