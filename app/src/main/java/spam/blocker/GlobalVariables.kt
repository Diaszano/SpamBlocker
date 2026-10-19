package spam.blocker

import android.content.Context
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import spam.blocker.db.Notification.Channel
import spam.blocker.db.Notification.ChannelTable
import spam.blocker.ui.history.CallViewModel
import spam.blocker.ui.history.HistoryOptions.showHistoryBlocked
import spam.blocker.ui.history.HistoryOptions.showHistoryPassed
import spam.blocker.ui.history.SmsViewModel
import spam.blocker.ui.setting.TestingViewModel
import spam.blocker.ui.setting.api.ApiQueryViewModel
import spam.blocker.ui.setting.api.ApiReportViewModel
import spam.blocker.ui.setting.api.ApiViewModel
import spam.blocker.ui.setting.bot.BotViewModel
import spam.blocker.ui.setting.regex.ContentRegexViewModel
import spam.blocker.ui.setting.regex.NumberRegexViewModel
import spam.blocker.ui.setting.regex.QuickCopyRegexViewModel
import spam.blocker.ui.isLight
import spam.blocker.ui.theme.ThemeAppearance
import spam.blocker.ui.theme.ThemeChoice
import spam.blocker.ui.theme.ThemeFamily
import spam.blocker.ui.theme.isDark
import spam.blocker.ui.theme.presetColors
import spam.blocker.ui.theme.resolveThemeChoice
import spam.blocker.ui.widgets.BottomBarViewModel
import spam.blocker.util.Notification.syncSystemChannels
import spam.blocker.util.Permission
import spam.blocker.util.PermissionChain
import spam.blocker.util.spf
import spam.blocker.util.spf.Palette

@Immutable
object G {
    val globallyEnabled : MutableState<Boolean> = mutableStateOf(false)
    val pausedUntil : MutableState<Long> = mutableStateOf(0L)
    val callEnabled : MutableState<Boolean> = mutableStateOf(false)
    val smsEnabled : MutableState<Boolean> = mutableStateOf(false)
    val dynamicTile0Enabled : MutableState<Boolean> = mutableStateOf(false)

    val notificationChannels : SnapshotStateList<Channel> = mutableStateListOf()
    lateinit var palette : Palette
    val currentThemeChoice : MutableState<ThemeChoice> = mutableStateOf(
        ThemeChoice(ThemeFamily.TRUECALLER, ThemeAppearance.SYSTEM)
    )

    val callVM : CallViewModel = CallViewModel()
    val smsVM : SmsViewModel = SmsViewModel()

    val NumberRuleVM : NumberRegexViewModel = NumberRegexViewModel()
    val ContentRuleVM : ContentRegexViewModel = ContentRegexViewModel()
    val QuickCopyRuleVM : QuickCopyRegexViewModel = QuickCopyRegexViewModel()
    val botVM : BotViewModel = BotViewModel()
    val apiQueryVM : ApiViewModel = ApiQueryViewModel()
    val apiReportVM : ApiViewModel = ApiReportViewModel()

    lateinit var bottomBarVM : BottomBarViewModel

    val testingVM : TestingViewModel = TestingViewModel()

    val permissionChain = PermissionChain()

    fun initialize(ctx: Context) {
        // Global switches
        run {
            val globalSpf = spf.Global(ctx)
            globallyEnabled.value = globalSpf.isGloballyEnabled
            pausedUntil.value = globalSpf.pausedUntil
            callEnabled.value = globalSpf.isCallEnabled && Permission.callScreening.isGranted
            smsEnabled.value = globalSpf.isSmsEnabled && Permission.receiveSMS.isGranted
            palette = Palette(ctx)

            val themeSpf = spf.Theme(ctx)
            val choice = resolveThemeChoice(
                themeSpf.themeFamily,
                themeSpf.themeAppearance,
                palette.hasSavedCustomColors()
            )
            currentThemeChoice.value = choice

            val systemIsDark = runCatching {
                (ctx.resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES
            }.getOrDefault(false)

            val isDark = choice.isDark(
                systemIsDark = systemIsDark,
                customBackgroundIsDark = !palette.background.isLight()
            )
            if (choice.family != ThemeFamily.CUSTOM) {
                palette.applyPreset(presetColors(choice.family, isDark))
            } else {
                palette.restoreCustom()
            }
        }

        // Workflow switches
        run {
            val spf = spf.BotOptions(ctx)
            dynamicTile0Enabled.value = spf.isDynamicTileEnabled(0)
        }

        // History options, these two needs to be loaded here because they are used to
        //  calculate the unread count on the bottom bar.
        run {
            val spf = spf.HistoryOptions(ctx)
            showHistoryPassed.value = spf.showPassed
            showHistoryBlocked.value = spf.showBlocked
        }

        // Notifications
        run {
            syncSystemChannels(ctx)
            notificationChannels.clear()
            notificationChannels.addAll(ChannelTable.listAll(ctx))
        }
    }
}

