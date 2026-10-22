package spam.blocker.db

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PushAlertRecordTest {
    @Test
    fun validationRequiresAnAppBodyAndPositiveDuration() {
        assertTrue(PushAlertRecord(pkgName = "spam.blocker.qa", body = "blocked").isValid())
        assertTrue(
            PushAlertRecord(
                enabled = false,
                pkgName = "spam.blocker.qa",
                body = "blocked",
            ).isValid()
        )
        assertFalse(PushAlertRecord(body = "blocked").isValid())
        assertFalse(PushAlertRecord(pkgName = "spam.blocker.qa").isValid())
        assertFalse(PushAlertRecord(pkgName = "spam.blocker.qa", body = "blocked", duration = 0).isValid())
        assertFalse(PushAlertRecord(pkgName = "spam.blocker.qa", body = "blocked", duration = -1).isValid())
    }
}
