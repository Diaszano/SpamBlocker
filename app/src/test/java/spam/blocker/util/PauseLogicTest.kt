package spam.blocker.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import spam.blocker.ui.setting.PauseHelper

class PauseLogicTest {

    @Test
    fun testIsPausedReturnsTrueWhenTimeIsInFuture() {
        val now = 1000000L
        val pausedUntil = now + 15 * 60 * 1000L // 15 min in future
        assertTrue(PauseHelper.isPaused(pausedUntil, now))
    }

    @Test
    fun testIsPausedReturnsFalseWhenTimeHasElapsed() {
        val now = 1000000L
        val pausedUntil = now - 1L // 1 ms in past
        assertFalse(PauseHelper.isPaused(pausedUntil, now))
        assertFalse(PauseHelper.isPaused(0L, now))
        assertFalse(PauseHelper.isPaused(now, now))
    }

    @Test
    fun testCalculatePausedUntil() {
        val now = 1700000000000L
        val fifteenMinutes = 15 * 60 * 1000L
        val calculated = PauseHelper.calculatePausedUntil(fifteenMinutes, now)
        assertEquals(now + fifteenMinutes, calculated)
    }

    @Test
    fun testFormatRemainingTime() {
        val now = 1000000L
        val duration = 14 * 60 * 1000L + 30 * 1000L // 14m 30s
        val remainingMinutes = PauseHelper.remainingMinutes(now + duration, now)
        assertEquals(15, remainingMinutes) // rounded up
    }

    @Test
    fun testRemainingMinutesRoundsUpAndExpiresAtBoundary() {
        val now = 1000000L

        assertEquals(0, PauseHelper.remainingMinutes(now, now))
        assertEquals(1, PauseHelper.remainingMinutes(now + 1L, now))
        assertEquals(1, PauseHelper.remainingMinutes(now + 60000L, now))
        assertEquals(2, PauseHelper.remainingMinutes(now + 60001L, now))
    }
}
