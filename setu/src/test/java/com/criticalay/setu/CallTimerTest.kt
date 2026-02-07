package com.criticalay.setu

import com.criticalay.setu.util.CallTimer
import org.junit.Assert.assertEquals
import org.junit.Test

class CallTimerTest {

    @Test
    fun `formatDuration returns mm ss for durations less than one minute`() {
        assertEquals("00:00", CallTimer.formatDuration(0))
        assertEquals("00:05", CallTimer.formatDuration(5))
        assertEquals("00:59", CallTimer.formatDuration(59))
    }

    @Test
    fun `formatDuration returns mm ss for durations exactly one minute`() {
        assertEquals("01:00", CallTimer.formatDuration(60))
    }

    @Test
    fun `formatDuration returns mm ss for durations between 1 minute and 1 hour`() {
        assertEquals("05:30", CallTimer.formatDuration(330))
        assertEquals("59:59", CallTimer.formatDuration(3599))
    }

    @Test
    fun `formatDuration returns HH mm ss for durations exactly one hour`() {
        assertEquals("01:00:00", CallTimer.formatDuration(3600))
    }

    @Test
    fun `formatDuration returns HH mm ss for durations longer than one hour`() {
        assertEquals("01:01:01", CallTimer.formatDuration(3661))

        assertEquals("10:00:00", CallTimer.formatDuration(36000))
    }
}
