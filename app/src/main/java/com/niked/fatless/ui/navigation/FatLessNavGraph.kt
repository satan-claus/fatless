package com.niked.fatless.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.niked.fatless.ui.MainActivity
import com.niked.fatless.ui.screen.FoodCreateScreen
import com.niked.fatless.ui.screen.NutritionScreen
import com.niked.fatless.ui.screen.SettingsScreen
import com.niked.fatless.ui.screen.WorkoutScreen
import com.niked.fatless.ui.screen.WorkoutCreateScreen
import com.niked.fatless.ui.screen.WorkoutListScreen

@Composable
fun FatLessNavGraph() {
    val navController = rememberNavController()
    val context = LocalContext.current

    NavHost(
        navController = navController,
        startDestination = Screen.Nutrition.route
    ) {
        composable(
            route = Screen.FoodCreate.route,
            arguments = listOf(
                navArgument("initName") { type = NavType.StringType }
            )
        ) {
            FoodCreateScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
        composable(Screen.Nutrition.route) {
            NutritionScreen(
                onBackClick = { navController.popBackStack() },
                onFoodCreateClick = { name ->
                    navController.navigate(Screen.FoodCreate.createRoute(name))
                }
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
        // Конструктор (Создание)
        composable(Screen.WorkoutCreate.route) {
            WorkoutCreateScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
        // Список тренировок
        composable(Screen.WorkoutList.route) {
            WorkoutListScreen(
                onWorkoutClick = { id ->
                    navController.navigate(Screen.WorkoutTimer.createRoute(id))
                },
                onAddWorkoutClick = {
                    navController.navigate(Screen.WorkoutCreate.route)
                },
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                },
                onExitClick = {
                    (context as? MainActivity)?.minimizeApp()
                }
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
