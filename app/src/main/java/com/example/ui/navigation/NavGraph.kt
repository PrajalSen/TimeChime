package com.example.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.di.AppContainer
import com.example.ui.screen.AboutScreen
import com.example.ui.screen.AppearanceSettingsScreen
import com.example.ui.screen.BellSettingsScreen
import com.example.ui.screen.HomeScreen
import com.example.ui.screen.PermissionsSettingsScreen
import com.example.ui.screen.ScheduleScreen
import com.example.ui.screen.SettingsScreen
import com.example.ui.screen.SpeechSettingsScreen
import com.example.ui.viewmodel.HomeViewModel
import com.example.ui.viewmodel.ScheduleViewModel
import com.example.ui.viewmodel.SettingsViewModel

import com.example.ui.screen.HourlyChimeFocusSettingsScreen
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut

@Composable
fun TimeChimeNavGraph(

    navController: NavHostController,
    container: AppContainer,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val homeViewModel: HomeViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = HomeViewModel.factory(container)
    )
    val scheduleViewModel: ScheduleViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = ScheduleViewModel.factory(container)
    )
    val settingsViewModel: SettingsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = SettingsViewModel.factory(container)
    )

    NavHost(
        navController = navController,
        startDestination = ScreenRoute.Home.route,
        enterTransition = { fadeIn(animationSpec = tween(300, easing = FastOutSlowInEasing)) },
        exitTransition = { fadeOut(animationSpec = tween(300, easing = FastOutSlowInEasing)) },
        popEnterTransition = { fadeIn(animationSpec = tween(300, easing = FastOutSlowInEasing)) },
        popExitTransition = { fadeOut(animationSpec = tween(300, easing = FastOutSlowInEasing)) },
        modifier = modifier
    ) {
        composable(ScreenRoute.Home.route) {
            HomeScreen(
                viewModel = homeViewModel,
                audioBellPlayer = container.audioBellPlayer,
                timeSpeechManager = container.timeSpeechManager,
                onNavigateToSchedule = {
                    navController.navigate(ScreenRoute.Schedule.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onAddClick = onAddClick
            )
        }

        composable(ScreenRoute.Schedule.route) {
            ScheduleScreen(
                viewModel = scheduleViewModel,
                audioBellPlayer = container.audioBellPlayer,
                timeSpeechManager = container.timeSpeechManager,
                onAddClick = onAddClick
            )
        }

        composable(ScreenRoute.Settings.route) {
            SettingsScreen(
                onNavigateToSpeechSettings = {
                    navController.navigate(ScreenRoute.SpeechSettings.route)
                },
                onNavigateToBellSettings = {
                    navController.navigate(ScreenRoute.BellSettings.route)
                },
                onNavigateToAppearance = {
                    navController.navigate(ScreenRoute.AppearanceSettings.route)
                },
                onNavigateToPermissions = {
                    navController.navigate(ScreenRoute.PermissionsSettings.route)
                },
                onNavigateToHourlyChimeFocus = {
                    navController.navigate(ScreenRoute.HourlyChimeFocusSettings.route)
                },
                onNavigateToAbout = {
                    navController.navigate(ScreenRoute.About.route)
                }
            )
        }


        composable(ScreenRoute.SpeechSettings.route) {
            SpeechSettingsScreen(
                viewModel = settingsViewModel,
                timeSpeechManager = container.timeSpeechManager,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(ScreenRoute.BellSettings.route) {
            BellSettingsScreen(
                viewModel = settingsViewModel,
                audioBellPlayer = container.audioBellPlayer,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(ScreenRoute.AppearanceSettings.route) {
            AppearanceSettingsScreen(
                viewModel = settingsViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(ScreenRoute.PermissionsSettings.route) {
            PermissionsSettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(ScreenRoute.HourlyChimeFocusSettings.route) {
            HourlyChimeFocusSettingsScreen(
                viewModel = settingsViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }


        composable(ScreenRoute.About.route) {
            AboutScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
