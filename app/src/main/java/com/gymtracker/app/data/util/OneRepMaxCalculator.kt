package com.gymtracker.app.data.util

import kotlin.math.roundToInt

/** Estimates a one-rep max using the Epley formula: 1RM = weight * (1 + reps/30). */
object OneRepMaxCalculator {

    fun estimate(weight: Double, reps: Int): Double {
        if (reps <= 0) return 0.0
        if (reps == 1) return weight
        return weight * (1 + reps / 30.0)
    }

    fun estimateRounded(weight: Double, reps: Int): Double =
        (estimate(weight, reps) * 10.0).roundToInt() / 10.0
}
