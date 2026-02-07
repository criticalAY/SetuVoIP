package com.criticalay.setu.util

import java.util.Locale

/**
 * A utility object responsible for transforming raw time data into human-readable strings.
 *
 * This component is primarily used by the notification manager and UI layers to display
 * the elapsed time of an active VoIP call in a standardized format.
 */
object CallTimer {

    /**
     * Formats a duration in seconds into a localized time string (HH:mm:ss or mm:ss).
     *
     * The output format adapts based on the length of the duration:
     * - If the duration is 1 hour or longer: `HH:mm:ss` (e.g., "01:15:30")
     * - If the duration is less than 1 hour: `mm:ss` (e.g., "05:42")
     *
     * @param seconds The total elapsed time in seconds.
     * @return A formatted string representing the duration, localized for [Locale.US].
     */
    fun formatDuration(seconds: Long): String {
        val hrs = seconds / 3600
        val mins = (seconds % 3600) / 60
        val secs = seconds % 60

        return if (hrs > 0) {
            String.format(Locale.US, "%02d:%02d:%02d", hrs, mins, secs)
        } else {
            String.format(Locale.US, "%02d:%02d", mins, secs)
        }
    }
}