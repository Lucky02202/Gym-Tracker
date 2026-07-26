package com.gymtracker.app.data.util

/**
 * Generates the recommended list of day names for a given training frequency,
 * per the app's spec. The caller decides rest-day placement across the calendar week;
 * this simply defines the rotation of training day names.
 */
object SplitGenerator {

    fun generate(daysPerWeek: Int): List<String> = when (daysPerWeek) {
        2 -> listOf("Full Body A", "Full Body B")
        3 -> listOf("Push", "Pull", "Legs")
        4 -> listOf("Upper", "Lower", "Upper", "Lower")
        5 -> listOf("Push", "Pull", "Legs", "Upper", "Arms/Core")
        6 -> listOf("Push", "Pull", "Legs", "Push", "Pull", "Legs")
        else -> listOf("Full Body A", "Full Body B")
    }

    fun splitName(daysPerWeek: Int): String = "$daysPerWeek-Day Split"

    val SUPPORTED_FREQUENCIES = listOf(2, 3, 4, 5, 6)

    /**
     * Spreads [dayNames] evenly across a 7-slot week (index 0 = Monday, 6 = Sunday),
     * filling the rest with null (rest day). This determines which real-world weekday
     * each workout day — and each rest day — falls on.
     */
    fun spreadAcrossWeek(dayNames: List<String>): List<String?> {
        val daysPerWeek = dayNames.size
        val week = arrayOfNulls<String>(7)
        for (i in 0 until daysPerWeek) {
            val pos = Math.round(i * 7.0 / daysPerWeek).toInt().coerceIn(0, 6)
            week[pos] = dayNames[i]
        }
        return week.toList()
    }
}
