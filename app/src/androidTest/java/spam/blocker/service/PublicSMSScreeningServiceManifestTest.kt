package spam.blocker.service

import android.content.pm.PackageManager
import android.content.pm.PermissionInfo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PublicSMSScreeningServiceManifestTest {
    @Test
    fun screeningServiceRequiresSignaturePermission() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val service = context.packageManager.getServiceInfo(
            android.content.ComponentName(context, PublicSMSScreeningService::class.java),
            PackageManager.ComponentInfoFlags.of(0),
        )
        val permission = context.packageManager.getPermissionInfo(
            "spam.blocker.permission.SCREEN_SMS",
            0,
        )

        assertEquals("spam.blocker.permission.SCREEN_SMS", service.permission)
        assertNotNull(permission)
        assertEquals(PermissionInfo.PROTECTION_SIGNATURE, permission.protection)
    }
}
