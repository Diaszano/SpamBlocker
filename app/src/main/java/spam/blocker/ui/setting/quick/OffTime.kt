package spam.blocker.ui.setting.quick

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import spam.blocker.G
import spam.blocker.R
import spam.blocker.ui.setting.SettingItem
import spam.blocker.ui.widgets.GreyIcon18
import spam.blocker.ui.widgets.Str
import spam.blocker.ui.widgets.StrokeButton
import spam.blocker.ui.widgets.SwitchBox
import spam.blocker.ui.widgets.TimeRangePicker
import spam.blocker.util.TimeUtils.timeRangeStr
import spam.blocker.util.spf

@Composable
fun OffTime() {
    val ctx = LocalContext.current
    val spf = spf.OffTime(ctx)

    var isEnabled by remember { mutableStateOf(spf.isEnabled) }

    val popupTrigger = rememberSaveable { mutableStateOf(false) }

    var sHour by remember { mutableIntStateOf(spf.startHour) }
    var sMin by remember { mutableIntStateOf(spf.startMin) }
    var eHour by remember { mutableIntStateOf(spf.endHour) }
    var eMin by remember { mutableIntStateOf(spf.endMin) }

    if (popupTrigger.value) {
        TimeRangePicker(
            trigger = popupTrigger,
            sHour, sMin, eHour, eMin,
        ) { sH, sM, eH, eM ->
            spf.startHour = sH
            spf.startMin = sM
            spf.endHour = eH
            spf.endMin = eM
            sHour = sH
            sMin = sM
            eHour = eH
            eMin = eM
        }
    }
    SettingItem(
        labelId = R.string.off_time,
        helpTooltip = Str(R.string.help_off_time),
        value = if (isEnabled) timeRangeStr(ctx, sHour, sMin, eHour, eMin) else null,
        onValueClick = { popupTrigger.value = true },
        checked = isEnabled,
        onCheckedChange = { isTurningOn ->
            spf.isEnabled = isTurningOn
            isEnabled = isTurningOn
        }
    )
}

@Composable
fun OffTimeSummary() {
    val ctx = LocalContext.current
    val spf = spf.OffTime(ctx)

    val isEnabled by remember { mutableStateOf(spf.isEnabled) }
    if (isEnabled) {
        val sHour by remember { mutableIntStateOf(spf.startHour) }
        val sMin by remember { mutableIntStateOf(spf.startMin) }
        val eHour by remember { mutableIntStateOf(spf.endHour) }
        val eMin by remember { mutableIntStateOf(spf.endMin) }

        StrokeButton(
            label = timeRangeStr(
                ctx, sHour, sMin, eHour, eMin
            ),
            icon = { GreyIcon18(R.drawable.ic_yes) },
            color = G.palette.textGrey,
            enabled = false,
        )
    }
}