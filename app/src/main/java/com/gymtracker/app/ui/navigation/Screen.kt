package com.gymtracker.app.ui.navigation

/** Sealed definition of every navigable destination and its route pattern. */
sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")

    object Home : Screen("home")
    object TodayWorkout : Screen("today_workout")

    object History : Screen("history")
    object WorkoutDetail : Screen("workout_detail/{workoutId}") {
        fun createRoute(workoutId: Long) = "workout_detail/$workoutId"
    }

    object Progress : Screen("progress")
    object ExerciseProgress : Screen("exercise_progress/{exerciseId}") {
        fun createRoute(exerciseId: Long) = "exercise_progress/$exerciseId"
    }
    object Statistics : Screen("statistics")

    object Calendar : Screen("calendar")
    object Search : Screen("search")
    object BodyWeight : Screen("body_weight")
    object ExerciseList : Screen("exercise_list")

    object Settings : Screen("settings")
    object SplitEditor : Screen("split_editor")

    companion object {
        /** Bottom navigation destinations, in display order, per spec. */
        val bottomNavItems = listOf(Home, TodayWorkout, History, Progress, Settings)
    }
}
