package spam.blocker.qa

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class QaPackageIsolationTest {

    @Test
    fun instrumentationTargetsOnlyTheQaApplication() {
        val context = QaTestEnvironment.context()
        assertTrue(context.packageName in setOf("spam.blocker.qa", "spam.blocker.googleplay.qa"))
    }
}
