package com.guidedfitness.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.guidedfitness.app.ui.screens.ProgressScreen
import com.guidedfitness.app.ui.screens.WeeklyPlanScreen

sealed class Screen(val route: String) {
    data object WeeklyPlan : Screen("weekly_plan")
    data object Progress : Screen("progress")
}

@Composable
fun GuidedFitnessNavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.WeeklyPlan.route
    ) {
        composable(Screen.WeeklyPlan.route) {
            WeeklyPlanScreen(
                onNavigateToProgress = { navController.navigate(Screen.Progress.route) }
            )
        }
        composable(Screen.Progress.route) {
            ProgressScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
