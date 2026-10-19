package spam.blocker.ui.setting

import android.os.Build
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LifecycleResumeEffect
import spam.blocker.G
import spam.blocker.R
import spam.blocker.def.Def
import spam.blocker.ui.M
import spam.blocker.ui.widgets.AnimatedVisibleV
import spam.blocker.ui.widgets.Button
import spam.blocker.ui.widgets.GreenDot
import spam.blocker.ui.widgets.HtmlText
import spam.blocker.ui.widgets.LabelItem
import spam.blocker.ui.widgets.MenuButton
import spam.blocker.ui.widgets.PopupDialog
import spam.blocker.ui.widgets.ResIcon
import spam.blocker.ui.widgets.ResImage
import spam.blocker.ui.widgets.RowVCenterSpaced
import spam.blocker.ui.widgets.Section
import spam.blocker.ui.widgets.Str
import spam.blocker.ui.widgets.StrokeButton
import spam.blocker.ui.widgets.SwitchBox
import spam.blocker.util.Permission
import spam.blocker.util.PermissionWrapper
import spam.blocker.util.Util
import spam.blocker.util.Util.isDefaultSmsAppNotificationEnabled
import spam.blocker.util.spf

@Composable
fun GloballyEnabledSummaryIcons(
    mmsEnabled: Boolean
) {
    val C = G.palette
    ResImage(
        R.drawable.ic_call,
        if (G.callEnabled.value) C.teal200 else C.disabled,
        M.size(20.dp)
    )
    ResImage(
        R.drawable.ic_sms,
        if (G.smsEnabled.value) C.teal200 else C.disabled,
        M.size(20.dp)
    )
    if (G.smsEnabled.value) {
        ResImage(
            R.drawable.ic_mms,
            if (mmsEnabled) C.teal200 else C.disabled,
            M.size(20.dp)
        )
    }
}

@Composable
fun GloballyEnabled() {
    val ctx = LocalContext.current
    val C = G.palette
    val spfSections = spf.SettingSections(ctx)
    val spfGlobal = spf.Global(ctx)

    var collapsed by remember { mutableStateOf(spfGlobal.isCollapsed) }
    fun toggleCollapse() {
        collapsed = !collapsed
        spfGlobal.isCollapsed = collapsed
    }
    fun checkMmsState(): Boolean {
        return G.smsEnabled.value
                && spfGlobal.isMmsEnabled
                && Permission.receiveMMS.isGranted
                && Permission.readSMS.isGranted
    }

    var mmsEnabled by remember { mutableStateOf(checkMmsState()) }

    val isPaused = PauseHelper.isPaused(G.pausedUntil.value)
    val remainingMinutes = if (isPaused) PauseHelper.remainingMinutes(G.pausedUntil.value) else 0

    val isCallPermMissing = G.callEnabled.value && !Permission.callScreening.isGranted
    val isSmsPermMissing = G.smsEnabled.value && !Permission.receiveSMS.isGranted
    val permissionRequired = isCallPermMissing || isSmsPermMissing

    val doubleSmsWarningTrigger = remember { mutableStateOf(false) }

    if (doubleSmsWarningTrigger.value) {
        PopupDialog(
            trigger = doubleSmsWarningTrigger,
            icon = { ResIcon(R.drawable.ic_warning, color = Color.Unspecified) },
            buttons = {
                StrokeButton(label = Str(R.string.dismiss), color = C.warning) {
                    spfGlobal.isDoubleSMSWarningDismissed = true
                    doubleSmsWarningTrigger.value = false
                }
                Spacer(modifier = M.width(10.dp))
                StrokeButton(label = Str(R.string.open_settings), color = C.teal200) {
                    doubleSmsWarningTrigger.value = false
                    Util.openDefaultSmsAppNotificationSetting(ctx)
                }
            },
        ) {
            HtmlText(Str(R.string.warning_double_sms))
        }
    }

    // Refresh permissions on resume
    LifecycleResumeEffect(Unit) {
        Permission.init(ctx)
        mmsEnabled = checkMmsState()
        onPauseOrDispose { }
    }

    // Show warnings `onResume`
    // - double sms notification
    LifecycleResumeEffect(G.smsEnabled.value) {
        if (G.smsEnabled.value) {
            if (Build.VERSION.SDK_INT >= Def.ANDROID_13) {
                if (isDefaultSmsAppNotificationEnabled(ctx) && spfGlobal.isGloballyEnabled && spfGlobal.isSmsEnabled) {
                    if (!spfGlobal.isDoubleSMSWarningDismissed) {
                        doubleSmsWarningTrigger.value = true
                    }
                }
            }
        }
        onPauseOrDispose { }
    }

    Section(
        title = Str(R.string.screening),
        horizontalPadding = 8,
        isCollapsed = remember { mutableStateOf(spfSections.isScreeningSectionCollapsed) },
        onToggle = {
            spfSections.isScreeningSectionCollapsed = it
        },
        contentCollapsed = { GloballyEnabledSummary() }
    ) {
        Column {
            LabeledRow(
                R.string.enable,
                helpTooltip = Str(R.string.help_screening),
                isCollapsed = collapsed,
                toggleCollapse = {
                    toggleCollapse()
                },
                content = {
                    if (G.globallyEnabled.value) {
                        Button(
                            content = {
                                RowVCenterSpaced(4) {
                                    GloballyEnabledSummaryIcons(mmsEnabled)
                                }
                            }
                        ) {
                            toggleCollapse()
                        }
                    }
                    if (G.globallyEnabled.value && !isPaused) {
                        val pauseItems = remember {
                            listOf(
                                LabelItem(label = ctx.getString(R.string.pause_15m)) {
                                    pauseBlocking(ctx, 15 * 60 * 1000L)
                                    it.value = false
                                },
                                LabelItem(label = ctx.getString(R.string.pause_1h)) {
                                    pauseBlocking(ctx, 60 * 60 * 1000L)
                                    it.value = false
                                },
                                LabelItem(label = ctx.getString(R.string.pause_8h)) {
                                    pauseBlocking(ctx, 8 * 60 * 60 * 1000L)
                                    it.value = false
                                }
                            )
                        }
                        MenuButton(
                            label = Str(R.string.pause_blocking),
                            items = pauseItems,
                            color = C.textGrey,
                            modifier = M.padding(end = 4.dp)
                        )
                    }
                    SwitchBox(G.globallyEnabled.value) { enabled ->
                        setGlobalEnabled(ctx, enabled)
                        collapsed = spfGlobal.isCollapsed
                    }
                }
            )

            if (isPaused) {
                RowVCenterSpaced(8, modifier = M.padding(horizontal = 12.dp, vertical = 4.dp)) {
                    ResImage(R.drawable.ic_hourglass, C.warning, M.size(16.dp))
                    Text(
                        text = Str(R.string.paused_remaining).format(remainingMinutes),
                        color = C.warning,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    StrokeButton(
                        label = Str(R.string.resume),
                        color = C.teal200,
                    ) {
                        resumeBlocking(ctx)
                    }
                }
            }

            if (permissionRequired) {
                RowVCenterSpaced(8, modifier = M.padding(horizontal = 12.dp, vertical = 6.dp)) {
                    ResImage(R.drawable.ic_warning, C.warning, M.size(16.dp))
                    Text(
                        text = Str(R.string.permission_required_desc),
                        color = C.warning,
                        fontSize = 12.sp,
                        modifier = M.weight(1f)
                    )
                    StrokeButton(
                        label = Str(R.string.grant_permissions),
                        color = C.warning
                    ) {
                        val toAsk = mutableListOf<PermissionWrapper>()
                        if (isCallPermMissing) toAsk.add(PermissionWrapper(Permission.callScreening))
                        if (isSmsPermMissing) toAsk.add(PermissionWrapper(Permission.receiveSMS))
                        G.permissionChain.ask(ctx, toAsk) {
                            Permission.init(ctx)
                            mmsEnabled = checkMmsState()
                        }
                    }
                }
            }

            AnimatedVisibleV (!collapsed) {
                Column(modifier = M.padding(start = 16.dp)) {
                    Column {
                        LabeledRow(
                            labelId = R.string.call,
                        ) {
                            SwitchBox(checked = G.callEnabled.value, onCheckedChange = { isTurningOn ->
                                if (isTurningOn) {
                                    G.permissionChain.ask(
                                        ctx,
                                        listOf(
                                            PermissionWrapper(Permission.callScreening),
                                        )
                                    ) { granted ->
                                        if (granted) {
                                            spfGlobal.isCallEnabled = true
                                            G.callEnabled.value = true
                                        }
                                    }
                                } else {
                                    spfGlobal.isCallEnabled = false
                                    G.callEnabled.value = false
                                }
                            })
                        }
                        if (isCallPermMissing) {
                            Text(
                                text = Str(R.string.permission_call_screening_missing),
                                color = C.warning,
                                fontSize = 11.sp,
                                modifier = M.padding(start = 12.dp, bottom = 4.dp)
                            )
                        }
                    }

                    Column {
                        LabeledRow(
                            labelId = R.string.sms,
                            helpTooltip = Str(R.string.help_enable_for_sms).format(Str(R.string.warning_double_sms))
                        ) {
                            SwitchBox(checked = G.smsEnabled.value, onCheckedChange = { isTurningOn ->
                                if (isTurningOn) {
                                    G.permissionChain.ask(
                                        ctx,
                                        listOf(
                                            PermissionWrapper(Permission.receiveSMS),
                                            PermissionWrapper(Permission.batteryUnRestricted, isOptional = true),
                                        )
                                    ) { granted ->
                                        if (granted) {
                                            spfGlobal.isSmsEnabled = true
                                            G.smsEnabled.value = true
                                        }
                                    }
                                } else {
                                    spfGlobal.isSmsEnabled = false
                                    G.smsEnabled.value = false
                                }
                            })
                        }
                        if (isSmsPermMissing) {
                            Text(
                                text = Str(R.string.permission_sms_missing),
                                color = C.warning,
                                fontSize = 11.sp,
                                modifier = M.padding(start = 12.dp, bottom = 4.dp)
                            )
                        }
                    }

                    AnimatedVisibleV(G.smsEnabled.value) {
                        LabeledRow(
                            labelId = R.string.mms,
                            helpTooltip = Str(R.string.help_enable_for_mms),
                        ) {
                            SwitchBox(checked = mmsEnabled, onCheckedChange = { isTurningOn ->
                                if (isTurningOn) {
                                    G.permissionChain.ask(
                                        ctx,
                                        listOf(
                                            PermissionWrapper(Permission.receiveMMS),
                                            PermissionWrapper(Permission.readSMS),
                                            PermissionWrapper(Permission.batteryUnRestricted, isOptional = true),
                                        )
                                    ) { granted ->
                                        if (granted) {
                                            spfGlobal.isMmsEnabled = true
                                            mmsEnabled = checkMmsState()
                                        }
                                    }
                                } else {
                                    spfGlobal.isMmsEnabled = false
                                    mmsEnabled = checkMmsState()
                                }
                            })
                        }
                    }
                    LabeledRow(
                        labelId = R.string.rcs,
                        helpTooltip = Str(R.string.help_rcs_message),
                        color = C.disabled,
                    ) {
                        Text(
                            text = Str(R.string.rcs_unavailable),
                            color = C.disabled,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GloballyEnabledSummary() {
    val ctx = LocalContext.current
    val C = G.palette
    val spf = spf.Global(ctx)

    val isPaused = PauseHelper.isPaused(G.pausedUntil.value)
    val isCallPermMissing = G.callEnabled.value && !Permission.callScreening.isGranted
    val isSmsPermMissing = G.smsEnabled.value && !Permission.receiveSMS.isGranted
    val permissionRequired = isCallPermMissing || isSmsPermMissing
    val anyChannelEnabled = G.callEnabled.value || G.smsEnabled.value

    val summary = protectionSummary(
        globalEnabled = G.globallyEnabled.value,
        paused = isPaused,
        permissionRequired = permissionRequired,
        anyChannelEnabled = anyChannelEnabled,
    )

    val mmsEnabled = remember { spf.isMmsEnabled }
    when (summary) {
        ProtectionSummary.ACTIVE -> {
            RowVCenterSpaced(4) {
                GreenDot()
                GloballyEnabledSummaryIcons(mmsEnabled)
            }
        }
        ProtectionSummary.PAUSED -> {
            RowVCenterSpaced(4) {
                ResImage(R.drawable.ic_hourglass, C.warning, M.size(16.dp))
                Text(
                    text = Str(R.string.protection_status_paused),
                    color = C.warning,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        ProtectionSummary.PERMISSION_REQUIRED -> {
            RowVCenterSpaced(4) {
                ResImage(R.drawable.ic_warning, C.warning, M.size(16.dp))
                Text(
                    text = Str(R.string.protection_status_permission_required),
                    color = C.warning,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        ProtectionSummary.CHANNELS_OFF -> {
            RowVCenterSpaced(4) {
                Text(
                    text = Str(R.string.protection_status_channels_off),
                    color = C.disabled,
                    fontSize = 12.sp
                )
            }
        }
        ProtectionSummary.OFF -> {
            ResImage(
                R.drawable.ic_tile,
                C.disabled,
                M.size(20.dp)
            )
        }
    }
}