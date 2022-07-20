package com.dapperartisancompany.askaguru.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dapperartisancompany.askaguru.layout.HomeScreen
import com.dapperartisancompany.askaguru.viewmodels.MainViewModel
import com.dapperartisancompany.askaguru.viewmodels.PlayerViewModel
import com.example.askaguru.screens.ProfileScreen
import com.example.askaguru.screens.SettingsScreen

@Composable
fun BottomNavGraph(navController: NavHostController) {
    val navController = rememberNavController()
    val mainViewModel = viewModel<MainViewModel>()
    val playerViewModel = viewModel<PlayerViewModel>()

    NavHost(
        navController = navController,
        startDestination = BottomBarScreen.Home.route
    ) {
        composable(route = BottomBarScreen.Home.route) {
            HomeScreen(mainViewModel = mainViewModel, playerViewModel = playerViewModel)
        }
        composable(route = BottomBarScreen.Profile.route) {
            ProfileScreen()
        }
        composable(route = BottomBarScreen.Settings.route) {
            SettingsScreen()
        }
    }
}