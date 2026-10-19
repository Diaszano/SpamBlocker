package spam.blocker.ui.setting

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ProtectionSummaryTest {

    @Test
    fun testOffWhenGlobalDisabledRegardlessOfOtherFlags() {
        assertEquals(
            ProtectionSummary.OFF,
            protectionSummary(
                globalEnabled = false,
                paused = false,
                permissionRequired = false,
                anyChannelEnabled = false
            )
        )
        assertEquals(
            ProtectionSummary.OFF,
            protectionSummary(
                globalEnabled = false,
                paused = true,
                permissionRequired = true,
                anyChannelEnabled = true
            )
        )
    }

    @Test
    fun testPausedWhenPausedRegardlessOfPermissionOrChannels() {
        assertEquals(
            ProtectionSummary.PAUSED,
            protectionSummary(
                globalEnabled = true,
                paused = true,
                permissionRequired = false,
                anyChannelEnabled = true
            )
        )
        assertEquals(
            ProtectionSummary.PAUSED,
            protectionSummary(
                globalEnabled = true,
                paused = true,
                permissionRequired = true,
                anyChannelEnabled = false
            )
        )
    }

    @Test
    fun testPermissionRequiredWhenPermissionNeeded() {
        assertEquals(
            ProtectionSummary.PERMISSION_REQUIRED,
            protectionSummary(
                globalEnabled = true,
                paused = false,
                permissionRequired = true,
                anyChannelEnabled = true
            )
        )
        assertEquals(
            ProtectionSummary.PERMISSION_REQUIRED,
            protectionSummary(
                globalEnabled = true,
                paused = false,
                permissionRequired = true,
                anyChannelEnabled = false
            )
        )
    }

    @Test
    fun testChannelsOffWhenNoChannelEnabled() {
        assertEquals(
            ProtectionSummary.CHANNELS_OFF,
            protectionSummary(
                globalEnabled = true,
                paused = false,
                permissionRequired = false,
                anyChannelEnabled = false
            )
        )
    }

    @Test
    fun testActiveWhenAllConditionsMet() {
        assertEquals(
            ProtectionSummary.ACTIVE,
            protectionSummary(
                globalEnabled = true,
                paused = false,
                permissionRequired = false,
                anyChannelEnabled = true
            )
        )
    }
}
