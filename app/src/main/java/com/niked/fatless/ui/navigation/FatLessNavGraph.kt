package com.niked.fatless.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.niked.fatless.ui.screen.WorkoutScreen
import com.niked.fatless.ui.screen.WorkoutCreateScreen
import com.niked.fatless.ui.screen.WorkoutListScreen

@Composable
fun FatLessNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.WorkoutList.route
    ) {
        // Список тренировок
        composable(Screen.WorkoutList.route) {
            WorkoutListScreen(
                onWorkoutClick = { id ->
                    navController.navigate(Screen.WorkoutTimer.createRoute(id))
                },
                onAddWorkoutClick = {
                    navController.navigate(Screen.WorkoutCreate.route)
                }
            )
        }

        // Конструктор (Создание)
        composable(Screen.WorkoutCreate.route) {
            WorkoutCreateScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        // Сам таймер (Процесс)
        composable(
            route = Screen.WorkoutTimer.route,
            arguments = listOf(
                navArgument("workoutId") { type = NavType.StringType }
            )
        ) {
            WorkoutScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
