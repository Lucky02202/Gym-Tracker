package com.gymtracker.app.data.util

import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateUtils {

    /** Returns start-of-day epoch millis for "today" in the device's default time zone. */
    fun startOfToday(): Long = startOfDay(System.currentTimeMillis())

    fun startOfDay(epochMillis: Long): Long {
        val date = Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).toLocalDate()
        return date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    /** Monday = 0 .. Sunday = 6, matching WorkoutDay.dayIndex convention. */
    fun weekdayIndex(epochMillis: Long = System.currentTimeMillis()): Int {
        val date = Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).toLocalDate()
        return date.dayOfWeek.value - 1
    }

    fun formatDate(epochMillis: Long, pattern: String = "MMM d, yyyy"): String {
        val date = Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).toLocalDate()
        return date.format(DateTimeFormatter.ofPattern(pattern, Locale.getDefault()))
    }

    fun formatDuration(millis: Long): String {
        val totalMinutes = millis / 60000
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
    }

    /**
     * Computes the current streak (consecutive calendar days ending today or yesterday
     * that contain a completed workout) from a descending-sorted, deduplicated list of
     * workout dates (start-of-day epoch millis).
     */
    fun currentStreak(sortedDescendingDates: List<Long>): Int {
        if (sortedDescendingDates.isEmpty()) return 0
        val today = startOfToday()
        val oneDayMillis = 24L * 60 * 60 * 1000
        var expected = today
        // Allow the streak to still count if the most recent workout was yesterday
        // (i.e. today's workout simply hasn't happened yet).
        if (sortedDescendingDates.first() != today) {
            expected = today - oneDayMillis
            if (sortedDescendingDates.first() != expected) return 0
        }
        var streak = 0
        for (date in sortedDescendingDates) {
            if (date == expected) {
                streak++
                expected -= oneDayMillis
            } else if (date < expected) {
                break
            }
        }
        return streak
    }

    fun longestStreak(sortedDescendingDates: List<Long>): Int {
        if (sortedDescendingDates.isEmpty()) return 0
        val oneDayMillis = 24L * 60 * 60 * 1000
        val ascending = sortedDescendingDates.sorted()
        var longest = 1
        var current = 1
        for (i in 1 until ascending.size) {
            longest = maxOf(longest, current)
            current = if (ascending[i] - ascending[i - 1] == oneDayMillis) current + 1 else 1
        }
        return maxOf(longest, current)
    }
}
