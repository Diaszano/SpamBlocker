package spam.blocker.ui.setting

import android.content.Context
import spam.blocker.G
import spam.blocker.util.spf

fun setGlobalEnabled(ctx: Context, enabled: Boolean) {
    val spfGlobal = spf.Global(ctx)
    spfGlobal.isGloballyEnabled = enabled
    G.globallyEnabled.value = enabled
    if (enabled) {
        if (!G.callEnabled.value && !G.smsEnabled.value) {
            spfGlobal.isCollapsed = false
        }
    } else {
        spfGlobal.isCollapsed = true
    }
}
