package spam.blocker.ui.setting

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import spam.blocker.G
import spam.blocker.R
import spam.blocker.ui.M
import spam.blocker.ui.theme.AppTheme
import spam.blocker.ui.widgets.Button
import spam.blocker.ui.widgets.FlowRowSpaced
import spam.blocker.ui.widgets.GreyIcon
import spam.blocker.ui.widgets.Section
import spam.blocker.ui.widgets.Str
import spam.blocker.ui.widgets.SwitchBox
import spam.blocker.util.spf

@Preview(name = "Quick settings groups German compact", widthDp = 320, fontScale = 1.3f, locale = "de", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Quick settings groups Russian compact", widthDp = 320, fontScale = 1.3f, locale = "ru", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun QuickSettingsGroupsPreview() {
    val ctx = LocalContext.current
    G.palette = spf.Palette(ctx)
    val C = G.palette
    var isCollapsed by remember { mutableStateOf(false) }

    var contactsEnabled by remember { mutableStateOf(true) }
    var stirEnabled by remember { mutableStateOf(false) }
    var spamDbEnabled by remember { mutableStateOf(true) }
    var localAiEnabled by remember { mutableStateOf(true) }

    var repeatedEnabled by remember { mutableStateOf(true) }
    var dialedEnabled by remember { mutableStateOf(true) }
    var answeredEnabled by remember { mutableStateOf(true) }

    var offTimeEnabled by remember { mutableStateOf(true) }
    var emergencyEnabled by remember { mutableStateOf(false) }
    var recentAppsEnabled by remember { mutableStateOf(true) }
    var meetingEnabled by remember { mutableStateOf(false) }

    var callerIdEnabled by remember { mutableStateOf(true) }

    AppTheme {
        Column(modifier = Modifier.padding(8.dp)) {
            // RCS unavailable row sample
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

            // Quick Settings section with 4 groups
            Section(
                title = Str(R.string.quick_settings),
                horizontalPadding = 8,
                isCollapsed = remember { mutableStateOf(isCollapsed) },
                onToggle = { isCollapsed = it },
                content = {
                    Column {
                        // 1. Signals
                        QuickSettingHeader(R.string.signals, isFirst = true)
                        SettingItem(
                            labelId = R.string.contacts,
                            helpTooltip = Str(R.string.help_contacts),
                            checked = contactsEnabled,
                            onCheckedChange = { contactsEnabled = it },
                        )
                        SettingItem(
                            labelId = R.string.stir_attestation,
                            helpTooltip = Str(R.string.help_stir),
                            checked = stirEnabled,
                            onCheckedChange = { stirEnabled = it },
                        )
                        SettingItem(
                            labelId = R.string.database,
                            helpTooltip = Str(R.string.help_spam_db),
                            checked = spamDbEnabled,
                            onCheckedChange = { spamDbEnabled = it },
                        )
                        LabeledRow(
                            labelId = R.string.local_ai,
                            helpTooltip = Str(R.string.help_local_ai),
                        ) {
                            if (localAiEnabled) {
                                Button(
                                    content = {
                                        Text(
                                            text = stringResource(R.string.needs_training),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    },
                                    onClick = {},
                                )
                            }
                            SwitchBox(localAiEnabled) { localAiEnabled = it }
                        }

                        // 2. Exceptions
                        QuickSettingHeader(R.string.exceptions)
                        SettingItem(
                            labelId = R.string.repeated_call,
                            helpTooltip = Str(R.string.help_repeated_call),
                            value = "1 / 5 min",
                            checked = repeatedEnabled,
                            onCheckedChange = { repeatedEnabled = it },
                        )
                        SettingItem(
                            labelId = R.string.dialed_number,
                            helpTooltip = Str(R.string.help_dialed),
                            value = "30 d",
                            checked = dialedEnabled,
                            onCheckedChange = { dialedEnabled = it },
                        )
                        SettingItem(
                            labelId = R.string.answered_number,
                            helpTooltip = Str(R.string.help_answered),
                            value = "14 d",
                            checked = answeredEnabled,
                            onCheckedChange = { answeredEnabled = it },
                        )

                        // 3. Context
                        QuickSettingHeader(R.string.context_rules)
                        SettingItem(
                            labelId = R.string.off_time,
                            helpTooltip = Str(R.string.help_off_time),
                            value = "00:00 - 23:59",
                            checked = offTimeEnabled,
                            onCheckedChange = { offTimeEnabled = it },
                        )
                        SettingItem(
                            labelId = R.string.emergency_situation,
                            helpTooltip = Str(R.string.help_emergency_situation),
                            value = "5 min",
                            checked = emergencyEnabled,
                            onCheckedChange = { emergencyEnabled = it },
                        )
                        SettingItem(
                            labelId = R.string.recent_apps,
                            helpTooltip = Str(R.string.help_recent_apps),
                            value = "3 min",
                            checked = recentAppsEnabled,
                            onCheckedChange = { recentAppsEnabled = it },
                        )
                        SettingItem(
                            labelId = R.string.in_meeting,
                            helpTooltip = Str(R.string.help_meeting_mode),
                            value = "45 min",
                            checked = meetingEnabled,
                            onCheckedChange = { meetingEnabled = it },
                        )

                        // 4. Behavior
                        QuickSettingHeader(R.string.behavior)
                        SettingItem(
                            labelId = R.string.block_type,
                            helpTooltip = Str(R.string.help_block_type),
                            value = "Reject",
                            onValueClick = {},
                        )
                        SettingItem(
                            labelId = R.string.notification,
                            helpTooltip = Str(R.string.help_notification),
                            value = "None",
                            onValueClick = {},
                        )
                        SettingItem(
                            labelId = R.string.caller_id,
                            helpTooltip = Str(R.string.help_caller_id),
                            value = Str(R.string.preview),
                            checked = callerIdEnabled,
                            onCheckedChange = { callerIdEnabled = it },
                        )
                    }
                },
                contentCollapsed = {
                    FlowRowSpaced(4) {
                        Button(
                            enabled = false,
                            content = {
                                GreyIcon(R.drawable.ic_ai, modifier = M.size(22.dp))
                                Text(
                                    text = stringResource(R.string.needs_training),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        )
                    }
                }
            )
        }
    }
}
