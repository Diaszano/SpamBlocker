package spam.blocker.ui.widgets

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PopupSizeUnitTest {

    @Test
    fun testMinWidthClamping() {
        val size = PopupSize(minWidthDp = 340, maxWidthPercentage = 0.9f)
        assertEquals(288f, size.minWidth(320f), 0.001f)
        assertEquals(340f, size.minWidth(600f), 0.001f)
    }
}
