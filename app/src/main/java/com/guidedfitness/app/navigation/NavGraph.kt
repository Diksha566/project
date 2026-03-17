package com.guidedfitness.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.navArgument
import com.guidedfitness.app.data.model.WorkoutDay
import com.guidedfitness.app.ui.screens.LoginScreen
import com.guidedfitness.app.ui.screens.ProgressScreen
import com.guidedfitness.app.ui.screens.WeeklyPlanScreen
import com.guidedfitness.app.ui.screens.WorkoutDetailScreen

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object WeeklyPlan : Screen("weekly_plan")
    data object WorkoutDetail : Screen("workout_detail/{day}") {
        fun createRoute(day: WorkoutDay) = "workout_detail/${day.name}"
    }
    data object Progress : Screen("progress")
}

@Composable
fun GuidedFitnessNavGraph(
    navController: NavHostController = rememberNavController(),
    viewModelProvider: () -> com.guidedfitness.app.ui.viewmodel.AppViewModel
) {
    val viewModel = viewModelProvider()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState(initial = false)

    androidx.compose.runtime.LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            navController.navigate(Screen.WeeklyPlan.route) {
                popUpTo(Screen.Login.route) { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLogin = { name, phone -> viewModel.login(name, phone) },
                onLoginSuccess = { navController.navigate(Screen.WeeklyPlan.route) { popUpTo(Screen.Login.route) { inclusive = true } } }
            )
        }
        composable(Screen.WeeklyPlan.route) {
            WeeklyPlanScreen(
                weeklyPlan = viewModel.weeklyPlan.collectAsState(initial = emptyList()).value,
                onNavigateToProgress = { navController.navigate(Screen.Progress.route) },
                onDayClick = { day -> navController.navigate(Screen.WorkoutDetail.createRoute(day)) }
            )
        }
        composable(
            route = "workout_detail/{day}",
            arguments = listOf(navArgument("day") { type = NavType.StringType })
        ) { backStackEntry ->
            val dayName = backStackEntry.arguments?.getString("day") ?: "MONDAY"
            val day = try { WorkoutDay.valueOf(dayName) } catch (e: Exception) { WorkoutDay.MONDAY }
            val workout = viewModel.getDayWorkout(day).collectAsState(initial = null).value

            WorkoutDetailScreen(
                day = day,
                workout = workout,
                onNavigateBack = { navController.popBackStack() },
                onMarkComplete = { minutes ->
                    viewModel.recordWorkoutCompletion(minutes)
                    navController.popBackStack()
                },
                onSetYoutubeVideoId = { videoId -> viewModel.setYoutubeVideoId(day, videoId) }
            )
        }
        composable(Screen.Progress.route) {
            ProgressScreen(
                totalSessions = viewModel.totalSessions.collectAsState(initial = 0).value,
                totalMinutes = viewModel.totalMinutes.collectAsState(initial = 0).value,
                streak = viewModel.streak.collectAsState(initial = 0).value,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
