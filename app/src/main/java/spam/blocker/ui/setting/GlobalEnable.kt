package spam.blocker.ui.setting

import android.content.Context
import spam.blocker.G
import spam.blocker.util.spf
import kotlin.math.ceil

object PauseHelper {
    fun isPaused(pausedUntil: Long, now: Long = System.currentTimeMillis()): Boolean {
        return pausedUntil > now
    }

    fun calculatePausedUntil(durationMillis: Long, now: Long = System.currentTimeMillis()): Long {
        return now + durationMillis
    }

    fun remainingMinutes(pausedUntil: Long, now: Long = System.currentTimeMillis()): Int {
        val diff = pausedUntil - now
        if (diff <= 0) return 0
        return ceil(diff / 60000.0).toInt()
    }
}

fun isPaused(ctx: Context, now: Long = System.currentTimeMillis()): Boolean {
    val pausedUntil = spf.Global(ctx).pausedUntil
    return PauseHelper.isPaused(pausedUntil, now)
}

fun pauseBlocking(ctx: Context, durationMillis: Long, now: Long = System.currentTimeMillis()) {
    val until = PauseHelper.calculatePausedUntil(durationMillis, now)
    spf.Global(ctx).pausedUntil = until
    G.pausedUntil.value = until
}

fun resumeBlocking(ctx: Context) {
    spf.Global(ctx).pausedUntil = 0L
    G.pausedUntil.value = 0L
}

fun setGlobalEnabled(ctx: Context, enabled: Boolean) {
    val spfGlobal = spf.Global(ctx)
    spfGlobal.isGloballyEnabled = enabled
    G.globallyEnabled.value = enabled
    resumeBlocking(ctx)
    if (enabled) {
        if (!G.callEnabled.value && !G.smsEnabled.value) {
            spfGlobal.isCollapsed = false
        }
    } else {
        spfGlobal.isCollapsed = true
    }
}
