package spam.blocker.qa

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import spam.blocker.util.spf

object QaTestEnvironment {

    fun context(): Context = InstrumentationRegistry.getInstrumentation().targetContext.also {
        check(it.packageName == "spam.blocker.qa")
    }

    fun resetPreferences() {
        spf.SharedPref(context()).clear()
    }
}
