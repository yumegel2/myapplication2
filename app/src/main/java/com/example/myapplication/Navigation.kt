package com.example.myapplication

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController


sealed class Screen(val route: String) {
    object Home : Screen("home")
    object LiveCoaching : Screen("live_coaching")
}

@Composable
fun EchoPalNavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) {
            HomeScreen(
                onStartCoaching = { navController.navigate(Screen.LiveCoaching.route) },
                onViewResults = { /* You can add a results screen here later */ }
            )
        }
        composable(Screen.LiveCoaching.route) {
            LiveCoachingScreen()
        }
    }
}
