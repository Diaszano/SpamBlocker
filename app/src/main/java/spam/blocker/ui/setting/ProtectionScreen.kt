package spam.blocker.ui.setting

import android.view.ViewTreeObserver
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import spam.blocker.G
import spam.blocker.R
import spam.blocker.service.checker.IChecker
import spam.blocker.ui.M
import spam.blocker.ui.setting.quick.Answered
import spam.blocker.ui.setting.quick.AnsweredSummary
import spam.blocker.ui.setting.quick.BlockType
import spam.blocker.ui.setting.quick.BlockTypeSummary
import spam.blocker.ui.setting.quick.CallerID
import spam.blocker.ui.setting.quick.CallerIDSummary
import spam.blocker.ui.setting.quick.Contacts
import spam.blocker.ui.setting.quick.ContactsSummary
import spam.blocker.ui.setting.quick.Dialed
import spam.blocker.ui.setting.quick.DialedSummary
import spam.blocker.ui.setting.quick.EmergencySituation
import spam.blocker.ui.setting.quick.EmergencySituationSummary
import spam.blocker.ui.setting.quick.LocalAI
import spam.blocker.ui.setting.quick.LocalAISummary
import spam.blocker.ui.setting.quick.MeetingMode
import spam.blocker.ui.setting.quick.MeetingModeSummary
import spam.blocker.ui.setting.quick.Notification
import spam.blocker.ui.setting.quick.NotificationSummary
import spam.blocker.ui.setting.quick.OffTime
import spam.blocker.ui.setting.quick.OffTimeSummary
import spam.blocker.ui.setting.quick.RecentApps
import spam.blocker.ui.setting.quick.RecentAppsSummary
import spam.blocker.ui.setting.quick.RepeatedCall
import spam.blocker.ui.setting.quick.RepeatedCallSummary
import spam.blocker.ui.setting.quick.SpamDB
import spam.blocker.ui.setting.quick.SpamDBSummary
import spam.blocker.ui.setting.quick.Stir
import spam.blocker.ui.setting.quick.StirSummary
import spam.blocker.ui.setting.regex.PushAlertHeader
import spam.blocker.ui.setting.regex.PushAlertList
import spam.blocker.ui.setting.regex.PushAlertSummary
import spam.blocker.ui.setting.regex.PushAlertViewModel
import spam.blocker.ui.setting.regex.RegexHeader
import spam.blocker.ui.setting.regex.RegexList
import spam.blocker.ui.setting.regex.RegexSummary
import spam.blocker.ui.setting.regex.RegexViewModel
import spam.blocker.ui.setting.regex.SmsAlert
import spam.blocker.ui.setting.regex.SmsAlertSummary
import spam.blocker.ui.setting.regex.SmsBomb
import spam.blocker.ui.setting.regex.SmsBombSummary
import spam.blocker.ui.slightDiff
import spam.blocker.ui.theme.White
import spam.blocker.ui.widgets.AnimatedVisibleV
import spam.blocker.ui.widgets.Fab
import spam.blocker.ui.widgets.FabWrapper
import spam.blocker.ui.widgets.FlowRowSpaced
import spam.blocker.ui.widgets.NormalColumnScrollbar
import spam.blocker.ui.widgets.SearchBox
import spam.blocker.ui.widgets.Section
import spam.blocker.ui.widgets.Str
import spam.blocker.util.Util.isFreshInstall
import spam.blocker.util.spf

enum class ProtectionSummary {
    OFF,
    PAUSED,
    PERMISSION_REQUIRED,
    CHANNELS_OFF,
    ACTIVE,
}

fun protectionSummary(
    globalEnabled: Boolean,
    paused: Boolean,
    permissionRequired: Boolean,
    anyChannelEnabled: Boolean,
): ProtectionSummary {
    return when {
        !globalEnabled -> ProtectionSummary.OFF
        paused -> ProtectionSummary.PAUSED
        permissionRequired -> ProtectionSummary.PERMISSION_REQUIRED
        !anyChannelEnabled -> ProtectionSummary.CHANNELS_OFF
        else -> ProtectionSummary.ACTIVE
    }
}

@Composable
fun ProtectionScreen() {
    val C = G.palette
    val ctx = LocalContext.current

    val testingTrigger = rememberSaveable { mutableStateOf(false) }
    TestDialog(testingTrigger)

    val scrollState = rememberScrollState()
    val bottomReached by remember {
        derivedStateOf {
            scrollState.maxValue > 0 && scrollState.value == scrollState.maxValue
        }
    }

    val priorityConflicts = remember { mutableStateListOf<IChecker>() }

    fun checkConflicts() {
        priorityConflicts.apply {
            clear()
            if (!spf.Global(ctx).ignorePriorityConflict) {
                addAll(detectConflictCheckers(ctx))
            }
        }
    }

    LaunchedEffect(Unit) {
        checkConflicts()
    }

    val view = LocalView.current
    DisposableEffect(view) {
        val listener = ViewTreeObserver.OnWindowFocusChangeListener { hasFocus ->
            if (hasFocus) {
                checkConflicts()
            }
        }

        val observer = view.viewTreeObserver
        observer.addOnWindowFocusChangeListener(listener)

        onDispose {
            if (observer.isAlive) {
                observer.removeOnWindowFocusChangeListener(listener)
            }
        }
    }

    val conflictTrigger = remember { mutableStateOf(false) }
    PriorityConflictDialog(trigger = conflictTrigger, conflicts = priorityConflicts)

    val spfSections = spf.SettingSections(ctx)
    val spfGlobal = spf.Global(ctx)

    var alsoShowTestButtonLabel by remember {
        mutableStateOf(
            isFreshInstall(ctx) && !spfGlobal.isTestIconClicked
        )
    }

    FabWrapper(
        fabRow = { positionModifier ->
            Fab(
                visible = !bottomReached,
                contentDescription = Str(R.string.title_rule_testing),
                text = if (alsoShowTestButtonLabel) Str(R.string.title_rule_testing) else null,
                iconId = R.drawable.ic_tube,
                iconColor = White,
                bgColor = if (priorityConflicts.isEmpty()) C.teal200 else C.warning,
                modifier = positionModifier
            ) {
                checkConflicts()
                if (priorityConflicts.isEmpty()) {
                    testingTrigger.value = true

                    spfGlobal.isTestIconClicked = true
                    alsoShowTestButtonLabel = false
                } else {
                    conflictTrigger.value = true
                }
            }
        }
    ) {
        NormalColumnScrollbar(state = scrollState) {
            Column(
                modifier = M
                    .verticalScroll(scrollState)
                    .padding(top = 8.dp)
            ) {
                // Global blocking and channels
                GloballyEnabled()

                // Quick rules
                Section(
                    title = Str(R.string.quick_settings),
                    horizontalPadding = 8,
                    isCollapsed = remember { mutableStateOf(spfSections.isQuickSettingsCollapsed) },
                    onToggle = {
                        spfSections.isQuickSettingsCollapsed = it
                    },
                    content = {
                        Column {
                            QuickSettingHeader(R.string.signals, isFirst = true)
                            Contacts()
                            Stir()
                            SpamDB()
                            LocalAI()

                            QuickSettingHeader(R.string.exceptions)
                            RepeatedCall()
                            Dialed()
                            Answered()

                            QuickSettingHeader(R.string.context_rules)
                            OffTime()
                            EmergencySituation()
                            RecentApps()
                            MeetingMode()

                            QuickSettingHeader(R.string.behavior)
                            BlockType()
                            Notification()
                            CallerID()
                        }
                    },
                    contentCollapsed = {
                        FlowRowSpaced(4) {
                            ContactsSummary()
                            StirSummary()
                            SpamDBSummary()
                            LocalAISummary()
                            RepeatedCallSummary()
                            DialedSummary()
                            AnsweredSummary()
                            OffTimeSummary()
                            EmergencySituationSummary()
                            RecentAppsSummary()
                            MeetingModeSummary()

                            HorizontalDivider(
                                modifier = M.padding(vertical = 3.dp),
                                thickness = 1.dp,
                                color = G.palette.background.slightDiff()
                            )

                            BlockTypeSummary()
                            NotificationSummary()
                            CallerIDSummary()
                        }
                    }
                )

                // Regex rules
                Section(
                    title = Str(R.string.regex_settings),
                    horizontalPadding = 8,
                    isCollapsed = remember { mutableStateOf(spfSections.isRegexSettingsCollapsed) },
                    onToggle = {
                        spfSections.isRegexSettingsCollapsed = it
                    },
                    content = {
                        Column {
                            listOf<RegexViewModel>(
                                G.NumberRuleVM,
                                G.ContentRuleVM,
                                G.QuickCopyRuleVM,
                            ).forEach { vm ->
                                vm.reloadOptions(ctx)
                                LaunchedEffect(true) { vm.reloadDbAndOptions(ctx) }

                                RegexHeader(vm)
                                AnimatedVisibleV(!vm.listCollapsed.value) {
                                    Column {
                                        SearchBox(vm.searchEnabled, vm.filter) {
                                            vm.reloadDb(ctx)
                                        }
                                        RegexList(vm)
                                    }
                                }
                            }

                            LaunchedEffect(true) { PushAlertViewModel.reloadDbAndOptions(ctx) }
                            PushAlertHeader()
                            AnimatedVisibleV(!PushAlertViewModel.listCollapsed.value) {
                                PushAlertList()
                            }

                            SmsAlert()
                            SmsBomb()
                        }
                    },
                    contentCollapsed = {
                        FlowRowSpaced(4) {
                            RegexSummary(G.NumberRuleVM)
                            RegexSummary(G.ContentRuleVM)
                            RegexSummary(G.QuickCopyRuleVM)
                            PushAlertSummary(PushAlertViewModel)
                            SmsAlertSummary()
                            SmsBombSummary()
                        }
                    }
                )
            }
        }
    }
}
