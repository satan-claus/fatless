package com.niked.fatless.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.niked.fatless.ui.MainActivity
import com.niked.fatless.ui.screen.DashboardScreen
import com.niked.fatless.ui.screen.FoodFormScreen
import com.niked.fatless.ui.screen.HistoryScreen
import com.niked.fatless.ui.screen.NutritionScreen
import com.niked.fatless.ui.screen.SettingsScreen
import com.niked.fatless.ui.screen.WorkoutScreen
import com.niked.fatless.ui.screen.WorkoutCreateScreen

@Composable
fun FatLessNavGraph() {
    val navController = rememberNavController()
    val context = LocalContext.current

    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route
    ) {
        // Dashboard - главный экран
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onAddWorkoutClick = {
                    navController.navigate(Screen.WorkoutCreate.route)
                },
                onEditWorkoutClick = { id ->
                    navController.navigate(Screen.WorkoutCreate.editRoute(id))
                },
                onExitClick = {
                    (context as? MainActivity)?.minimizeApp()
                },
                onHistoryClick = {
                    navController.navigate(Screen.History.route)
                },
                onNutritionClick = {
                    navController.navigate(Screen.Nutrition.route)
                },
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                },
                onWorkoutClick = { id ->
                    navController.navigate(Screen.WorkoutTimer.createRoute(id))
                }
            )
        }
        // Экран добавления/удаления/редактирования справочника продуктов
        composable(
            route = Screen.FoodForm.route,
            arguments = listOf(
                navArgument("initName") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("foodId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) {
            FoodFormScreen(onBackClick = { navController.popBackStack() })
        }
        composable(Screen.History.route) {
            HistoryScreen(onBackClick = { navController.popBackStack() })
        }
        // Экран "Дневник питания"
        composable(Screen.Nutrition.route) {
            NutritionScreen(
                onBackClick = { navController.popBackStack() },
                // Переход на создание (передаем имя)
                onFoodCreateClick = { name ->
                    navController.navigate(Screen.FoodForm.createForNew(name))
                },
                // Переход на редактирование (передаем ID)
                onFoodEditClick = { id ->
                    navController.navigate(Screen.FoodForm.createForEdit(id))
                }
            )
        }
        // Экран настроек
        composable(Screen.Settings.route) {
            SettingsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
        // Конструктор (Создание)
        composable(
            // Маршрут: "workout_create?workoutId={workoutId}"
            route = Screen.WorkoutCreate.route,
            arguments = listOf(
                navArgument("workoutId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) {
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
