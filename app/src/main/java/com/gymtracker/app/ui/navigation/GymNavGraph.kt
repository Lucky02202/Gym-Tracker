package com.gymtracker.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.gymtracker.app.ui.screens.bodyweight.BodyWeightScreen
import com.gymtracker.app.ui.screens.calendar.CalendarScreen
import com.gymtracker.app.ui.screens.history.HistoryScreen
import com.gymtracker.app.ui.screens.history.WorkoutDetailScreen
import com.gymtracker.app.ui.screens.home.HomeScreen
import com.gymtracker.app.ui.screens.onboarding.OnboardingScreen
import com.gymtracker.app.ui.screens.progress.ExerciseProgressScreen
import com.gymtracker.app.ui.screens.progress.ProgressScreen
import com.gymtracker.app.ui.screens.progress.StatisticsScreen
import com.gymtracker.app.ui.screens.search.SearchScreen
import com.gymtracker.app.ui.screens.settings.ExerciseListScreen
import com.gymtracker.app.ui.screens.settings.SettingsScreen
import com.gymtracker.app.ui.screens.settings.SplitEditorScreen
import com.gymtracker.app.ui.screens.workout.TodayWorkoutScreen
import com.gymtracker.app.ui.viewmodel.SettingsViewModel
import com.gymtracker.app.ui.viewmodel.ViewModelFactory
import androidx.lifecycle.viewmodel.compose.viewModel

private data class BottomNavEntry(val screen: Screen, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

private val bottomNavEntries = listOf(
    BottomNavEntry(Screen.Home, "Home", Icons.Default.Home),
    BottomNavEntry(Screen.TodayWorkout, "Today", Icons.Default.FitnessCenter),
    BottomNavEntry(Screen.History, "History", Icons.Default.History),
    BottomNavEntry(Screen.Progress, "Progress", Icons.Default.TrendingUp),
    BottomNavEntry(Screen.Settings, "Settings", Icons.Default.Settings)
)

@Composable
fun GymNavGraph(factory: ViewModelFactory, hasCompletedOnboarding: Boolean) {
    val navController = rememberNavController()
    val settingsViewModel: SettingsViewModel = viewModel(factory = factory)
    val settings by settingsViewModel.settings.collectAsState()

    val startDestination = if (hasCompletedOnboarding) Screen.Home.route else Screen.Onboarding.route

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = bottomNavEntries.any { it.screen.route == currentRoute }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavEntries.forEach { entry ->
                        NavigationBarItem(
                            selected = currentRoute == entry.screen.route,
                            onClick = {
                                navController.navigate(entry.screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(entry.icon, contentDescription = entry.label) },
                            label = { Text(entry.label) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = if (showBottomBar) androidx.compose.ui.Modifier.padding(padding) else androidx.compose.ui.Modifier
        ) {
            composable(Screen.Onboarding.route) {
                OnboardingScreen(factory = factory, onFinished = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                })
            }

            composable(Screen.Home.route) {
                HomeScreen(
                    factory = factory,
                    onNavigateCalendar = { navController.navigate(Screen.Calendar.route) },
                    onNavigateBodyWeight = { navController.navigate(Screen.BodyWeight.route) },
                    onNavigateSearch = { navController.navigate(Screen.Search.route) },
                    onNavigateExerciseList = { navController.navigate(Screen.ExerciseList.route) },
                    onNavigateStatistics = { navController.navigate(Screen.Statistics.route) }
                )
            }

            composable(Screen.TodayWorkout.route) {
                TodayWorkoutScreen(factory = factory, units = settings.units)
            }

            composable(Screen.History.route) {
                HistoryScreen(
                    factory = factory,
                    units = settings.units,
                    onOpenWorkout = { id -> navController.navigate(Screen.WorkoutDetail.createRoute(id)) }
                )
            }

            composable(
                Screen.WorkoutDetail.route,
                arguments = listOf(navArgument("workoutId") { type = androidx.navigation.NavType.LongType })
            ) { backStack ->
                val workoutId = backStack.arguments?.getLong("workoutId") ?: return@composable
                WorkoutDetailScreen(
                    factory = factory,
                    units = settings.units,
                    workoutId = workoutId,
                    onBack = { navController.popBackStack() },
                    onDuplicated = {
                        navController.navigate(Screen.TodayWorkout.route) {
                            popUpTo(Screen.Home.route)
                        }
                    }
                )
            }

            composable(Screen.Progress.route) {
                ProgressScreen(
                    factory = factory,
                    onOpenExercise = { id -> navController.navigate(Screen.ExerciseProgress.createRoute(id)) },
                    onOpenStatistics = { navController.navigate(Screen.Statistics.route) }
                )
            }

            composable(
                Screen.ExerciseProgress.route,
                arguments = listOf(navArgument("exerciseId") { type = androidx.navigation.NavType.LongType })
            ) { backStack ->
                val exerciseId = backStack.arguments?.getLong("exerciseId") ?: return@composable
                ExerciseProgressScreen(
                    factory = factory,
                    units = settings.units,
                    exerciseId = exerciseId,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Statistics.route) {
                StatisticsScreen(factory = factory, units = settings.units, onBack = { navController.popBackStack() })
            }

            composable(Screen.Calendar.route) {
                CalendarScreen(
                    factory = factory,
                    onBack = { navController.popBackStack() },
                    onOpenWorkout = { id -> navController.navigate(Screen.WorkoutDetail.createRoute(id)) }
                )
            }

            composable(Screen.Search.route) {
                SearchScreen(
                    factory = factory,
                    onBack = { navController.popBackStack() },
                    onOpenExercise = { id -> navController.navigate(Screen.ExerciseProgress.createRoute(id)) },
                    onOpenWorkout = { id -> navController.navigate(Screen.WorkoutDetail.createRoute(id)) }
                )
            }

            composable(Screen.BodyWeight.route) {
                BodyWeightScreen(factory = factory, units = settings.units, onBack = { navController.popBackStack() })
            }

            composable(Screen.ExerciseList.route) {
                ExerciseListScreen(
                    factory = factory,
                    onBack = { navController.popBackStack() },
                    onOpenExercise = { id -> navController.navigate(Screen.ExerciseProgress.createRoute(id)) }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    factory = factory,
                    onEditSplit = { navController.navigate(Screen.SplitEditor.route) },
                    onOpenExerciseList = { navController.navigate(Screen.ExerciseList.route) }
                )
            }

            composable(Screen.SplitEditor.route) {
                SplitEditorScreen(factory = factory, onBack = { navController.popBackStack() })
            }
        }
    }
}
