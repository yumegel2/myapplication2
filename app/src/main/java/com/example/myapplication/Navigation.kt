package com.example.myapplication

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController


sealed class Screen(val route: String) {
    object Home : Screen("home")
    object LiveCoaching : Screen("live_coaching")
    object Results : Screen("results")
}

@Composable
fun EchoPalNavGraph(navController: NavHostController = rememberNavController()) {
    // Create a shared ViewModel instance at the navigation graph level
    val sharedViewModel: LiveCoachingViewModel = viewModel()
    
    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) {
            HomeScreen(
                onStartCoaching = { navController.navigate(Screen.LiveCoaching.route) },
                onViewResults = { navController.navigate(Screen.Results.route) }
            )
        }
        composable(Screen.LiveCoaching.route) {
            LiveCoachingScreen(viewModel = sharedViewModel)
        }
        composable(Screen.Results.route) {
            ResultsScreen(viewModel = sharedViewModel)
        }
    }
}
