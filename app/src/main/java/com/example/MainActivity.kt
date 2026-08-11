package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.domain.model.AppThemeMode
import com.example.ui.components.TimeChimeBottomNavigationBar
import com.example.ui.navigation.ScreenRoute
import com.example.ui.navigation.TimeChimeNavGraph
import com.example.ui.screen.AddEditAnnouncementDialog
import com.example.ui.theme.TimeChimeTheme
import com.example.ui.viewmodel.ScheduleViewModel
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.saveable.rememberSaveable
import com.example.ui.screen.WelcomeSplashScreen
import com.example.ui.viewmodel.SettingsViewModel
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TimeChimeApp()
        }
    }
}

@Composable
fun TimeChimeApp() {
    val context = LocalContext.current
    val app = context.applicationContext as TimeChimeApplication
    val container = app.container

    var showSplash by rememberSaveable { mutableStateOf(true) }

    val settingsViewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModel.factory(container)
    )
    val scheduleViewModel: ScheduleViewModel = viewModel(
        factory = ScheduleViewModel.factory(container)
    )

    val prefs by settingsViewModel.userPreferences.collectAsStateWithLifecycle()
    val isDark = when (prefs.themeMode) {
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
        AppThemeMode.LIGHT, AppThemeMode.AURORA_BLOOM, AppThemeMode.ROSE_MIST -> false
        AppThemeMode.DARK, AppThemeMode.AURORA_DARK, AppThemeMode.MIDNIGHT_INDIGO -> true
    }


    val notifLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { }

    androidx.compose.runtime.LaunchedEffect(showSplash) {
        if (!showSplash) {
            container.alarmScheduler.scheduleHourlyChime()
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                if (androidx.core.content.ContextCompat.checkSelfPermission(
                        context,
                        android.Manifest.permission.POST_NOTIFICATIONS
                    ) != android.content.pm.PackageManager.PERMISSION_GRANTED
                ) {
                    notifLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }

    TimeChimeTheme(
        themeMode = prefs.themeMode,
        darkTheme = isDark,
        dynamicColor = prefs.dynamicColors
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            val navController = rememberNavController()
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            var showAddDialog by remember { mutableStateOf(false) }

            val mainRoutes = listOf(
                ScreenRoute.Home.route,
                ScreenRoute.Schedule.route,
                ScreenRoute.Settings.route
            )
            val showBottomNav = currentRoute in mainRoutes

            var backPressedTime by remember { androidx.compose.runtime.mutableLongStateOf(0L) }
            val activity = (context as? ComponentActivity)

            androidx.activity.compose.BackHandler(enabled = currentRoute == ScreenRoute.Home.route) {
                val currentTime = System.currentTimeMillis()
                if (currentTime - backPressedTime < 2000) {
                    activity?.finish()
                } else {
                    backPressedTime = currentTime
                    android.widget.Toast.makeText(context, "Press back again to exit", android.widget.Toast.LENGTH_SHORT).show()
                }
            }

            Scaffold(
                modifier = Modifier.fillMaxSize(),
                bottomBar = {
                    if (showBottomNav) {
                        TimeChimeBottomNavigationBar(
                            currentRoute = currentRoute,
                            onNavigate = { route ->
                                if (currentRoute != route) {
                                    navController.navigate(route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }
                }
            ) { innerPadding ->
                TimeChimeNavGraph(
                    navController = navController,
                    container = container,
                    onAddClick = { showAddDialog = true },
                    modifier = Modifier.padding(innerPadding)
                )

                if (showAddDialog) {
                    val scheduleUiState by scheduleViewModel.uiState.collectAsStateWithLifecycle()
                    AddEditAnnouncementDialog(
                        initialAnnouncement = null,
                        userPreferences = prefs,
                        existingAnnouncements = scheduleUiState.announcements,
                        is24Hour = prefs.is24HourFormat,
                        audioBellPlayer = container.audioBellPlayer,
                        timeSpeechManager = container.timeSpeechManager,
                        onDismiss = { showAddDialog = false },
                        onSave = { newAnnouncement ->
                            scheduleViewModel.saveAnnouncement(newAnnouncement)
                        }
                    )
                }
            }

            // Seamless Crossfade Splash Overlay
            AnimatedVisibility(
                visible = showSplash,
                enter = fadeIn(),
                exit = fadeOut(animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing))
            ) {
                WelcomeSplashScreen(
                    onSplashFinished = { showSplash = false }
                )
            }
        }
    }
}
