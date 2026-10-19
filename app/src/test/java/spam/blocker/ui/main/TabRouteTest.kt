package spam.blocker.ui.main

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import spam.blocker.def.Def

class TabRouteTest {

    @Test
    fun testProtectionTabRouteConstant() {
        assertEquals("protection", Def.PROTECTION_TAB_ROUTE)
    }

    @Test
    fun testResolveStartTabPreservesValidRoutes() {
        assertEquals(Def.CALL_TAB_ROUTE, Def.resolveStartTab("call"))
        assertEquals(Def.SMS_TAB_ROUTE, Def.resolveStartTab("sms"))
        assertEquals(Def.SETTING_TAB_ROUTE, Def.resolveStartTab("setting"))
        assertEquals(Def.PROTECTION_TAB_ROUTE, Def.resolveStartTab("protection"))
    }

    @Test
    fun testResolveStartTabFallsBackToCallForEmptyOrUnknown() {
        assertEquals(Def.CALL_TAB_ROUTE, Def.resolveStartTab(""))
        assertEquals(Def.CALL_TAB_ROUTE, Def.resolveStartTab("unknown"))
        assertEquals(Def.CALL_TAB_ROUTE, Def.resolveStartTab("history"))
        assertEquals(Def.CALL_TAB_ROUTE, Def.resolveStartTab("null"))
    }
}
