package spam.blocker.qa

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import spam.blocker.db.CallTable
import spam.blocker.db.HistoryRecord
import spam.blocker.db.SmsTable
import spam.blocker.util.spf

object QaTestEnvironment {

    fun context(): Context = InstrumentationRegistry.getInstrumentation().targetContext.also {
        check(it.packageName == "spam.blocker.qa" || it.packageName == "spam.blocker.googleplay.qa")
    }

    fun resetPreferences() {
        spf.SharedPref(context()).clear()
    }

    fun clearHistory() {
        context().also {
            CallTable().clearAll(it)
            SmsTable().clearAll(it)
        }
    }

    fun addCall(record: HistoryRecord) = CallTable().addNew(context(), record)

    fun addSms(record: HistoryRecord) = SmsTable().addNew(context(), record)
}
