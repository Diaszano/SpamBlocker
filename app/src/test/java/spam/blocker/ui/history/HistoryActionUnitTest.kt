package spam.blocker.ui.history

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import spam.blocker.R
import spam.blocker.def.Def

class HistoryActionUnitTest {

    @Test
    fun testHistoryOpenActionRes() {
        assertEquals(R.string.call, historyOpenActionRes(Def.ForNumber))
        assertEquals(R.string.open_sms, historyOpenActionRes(Def.ForSms))
    }

    @Test
    fun testHistoryOpenActionIcon() {
        assertEquals(R.drawable.ic_call, historyOpenActionIcon(Def.ForNumber))
        assertEquals(R.drawable.ic_open_msg, historyOpenActionIcon(Def.ForSms))
    }
}
