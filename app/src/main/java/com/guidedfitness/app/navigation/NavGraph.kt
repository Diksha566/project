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
import com.guidedfitness.app.ui.screens.ProgressScreen
import com.guidedfitness.app.ui.screens.WeeklyPlanScreen
import com.guidedfitness.app.ui.screens.WorkoutDetailScreen

sealed class Screen(val route: String) {
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

    NavHost(
        navController = navController,
        startDestination = Screen.WeeklyPlan.route
    ) {
        composable(Screen.WeeklyPlan.route) {
            val meta = viewModel.planMetadata.collectAsState(initial = null).value
            WeeklyPlanScreen(
                userName = viewModel.userName.collectAsState(initial = null).value,
                userPhone = viewModel.userPhone.collectAsState(initial = null).value,
                planTitle = meta?.title ?: "My Fitness Plan",
                planDescription = meta?.description ?: "Your weekly schedule",
                weeklyPlan = viewModel.weeklyPlan.collectAsState(initial = emptyList()).value,
                onNavigateToProgress = { navController.navigate(Screen.Progress.route) },
                onDayClick = { day -> navController.navigate(Screen.WorkoutDetail.createRoute(day)) },
                onUpdatePlanMetadata = { title, description ->
                    viewModel.updatePlanMetadata(title, description)
                },
                onUpdateDayFocus = { day, focus ->
                    viewModel.updateDayFocus(day, focus)
                },
                onUpdateDayIcon = { day, iconKey ->
                    viewModel.updateDayIcon(day, iconKey)
                },
                onUpsertProfile = { name, phone ->
                    // WeeklyPlanScreen calls this from a coroutine scope
                    viewModel.upsertProfile(name, phone)
                }
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
                    val focus = workout?.focus ?: day.focus
                    viewModel.recordWorkoutCompletion(day, focus, minutes)
                    navController.popBackStack()
                },
                onSetYoutubeVideoId = { videoId -> viewModel.setYoutubeVideoId(day, videoId) },
                onUpdateFocus = { focus -> viewModel.updateDayFocus(day, focus) },
                onUpsertExercise = { exercise -> viewModel.addExercise(day, exercise) },
                onRemoveExercise = { exerciseId -> viewModel.removeExercise(day, exerciseId) }
            )
        }
        composable(Screen.Progress.route) {
            ProgressScreen(
                totalSessions = viewModel.totalSessions.collectAsState(initial = 0).value,
                totalMinutes = viewModel.totalMinutes.collectAsState(initial = 0).value,
                streak = viewModel.streak.collectAsState(initial = 0).value,
                longestStreak = viewModel.longestStreak.collectAsState(initial = 0).value,
                weeklyCompletionPercent = viewModel.weeklyCompletionPercent.collectAsState(initial = 0).value,
                breathingSessions = viewModel.breathingSessions.collectAsState(initial = 0).value,
                workoutSessions = viewModel.workoutSessions.collectAsState(initial = 0).value,
                dailyMinutesSeries = viewModel.dailyMinutesSeries.collectAsState(initial = emptyList()).value,
                weeklyMinutesSeries = viewModel.weeklyMinutesSeries.collectAsState(initial = emptyList()).value,
                monthlyMinutesSeries = viewModel.monthlyMinutesSeries.collectAsState(initial = emptyList()).value,
                yearlyMinutesSeries = viewModel.yearlyMinutesSeries.collectAsState(initial = emptyList()).value,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
