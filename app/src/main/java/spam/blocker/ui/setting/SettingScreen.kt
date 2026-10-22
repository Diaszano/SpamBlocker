package spam.blocker.ui.setting

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import spam.blocker.G
import spam.blocker.R
import spam.blocker.ui.M
import spam.blocker.ui.setting.api.ApiHeader
import spam.blocker.ui.setting.api.ApiList
import spam.blocker.ui.setting.api.ApiQueryPresets
import spam.blocker.ui.setting.api.ApiReportPresets
import spam.blocker.ui.setting.api.ApiSummary
import spam.blocker.ui.setting.bot.BotHeader
import spam.blocker.ui.setting.bot.BotList
import spam.blocker.ui.setting.bot.BotSummary
import spam.blocker.ui.setting.misc.About
import spam.blocker.ui.setting.misc.BackupRestore
import spam.blocker.ui.setting.misc.FAQ
import spam.blocker.ui.setting.misc.Language
import spam.blocker.ui.setting.misc.Theme
import spam.blocker.ui.setting.misc.VersionSummary
import spam.blocker.ui.widgets.AnimatedVisibleV
import spam.blocker.ui.widgets.BalloonQuestionMark
import spam.blocker.ui.widgets.GreyIcon16
import spam.blocker.ui.widgets.NormalColumnScrollbar
import spam.blocker.ui.widgets.RowVCenter
import spam.blocker.ui.widgets.RowVCenterSpaced
import spam.blocker.ui.widgets.Section
import spam.blocker.ui.widgets.Str
import spam.blocker.util.Lambda
import spam.blocker.util.spf

const val SettingRowMinHeight = 48

@Composable
fun SettingScreen() {
    val ctx = LocalContext.current
    val spfSections = spf.SettingSections(ctx)
    val scrollState = rememberScrollState()

    NormalColumnScrollbar(state = scrollState) {
        Column(
            modifier = M
                .verticalScroll(scrollState)
                .padding(top = 8.dp)
        ) {

                // Instant Query
                Section(
                    title = Str(R.string.instant_query),
                    horizontalPadding = 8,
                    isCollapsed = remember { mutableStateOf(spfSections.isInstantQueryCollapsed) },
                    onToggle = {
                        spfSections.isInstantQueryCollapsed = it
                    },
                    content = {
                        Column {
                            // Api Query list
                            LaunchedEffect(true) { G.apiQueryVM.reloadDb(ctx) }
                            ApiHeader(G.apiQueryVM, ApiQueryPresets)
                            AnimatedVisibleV(!G.apiQueryVM.listCollapsed.value) {
                                ApiList(G.apiQueryVM)
                            }
                        }
                    },
                    contentCollapsed = {
                        ApiSummary(G.apiQueryVM)
                    }
                )

                // Report Number
                Section(
                    title = Str(R.string.report_number),
                    horizontalPadding = 8,
                    isCollapsed = remember { mutableStateOf(spfSections.isReportNumberCollapsed) },
                    onToggle = {
                        spfSections.isReportNumberCollapsed = it
                    },
                    content = {
                        Column {
                            // Api Report list
                            LaunchedEffect(true) { G.apiReportVM.reloadDb(ctx) }
                            ApiHeader(G.apiReportVM, ApiReportPresets)
                            AnimatedVisibleV(!G.apiReportVM.listCollapsed.value) {
                                ApiList(G.apiReportVM)
                            }
                        }
                    },
                    contentCollapsed = {
                        ApiSummary(G.apiReportVM)
                    }
                )

                // Automation
                Section(
                    title = Str(R.string.automation),
                    horizontalPadding = 8,
                    isCollapsed = remember { mutableStateOf(spfSections.isAutomationCollapsed) },
                    content = {
                        Column {
                            // Bot list
                            LaunchedEffect(true) { G.botVM.reload(ctx) }
                            BotHeader(G.botVM)
                            AnimatedVisibleV(!G.botVM.listCollapsed.value) {
                                BotList()
                            }
                        }
                    },
                    contentCollapsed = {
                        BotSummary(G.botVM)
                    },
                    onToggle = {
                        spfSections.isAutomationCollapsed = it
                    }
                )

                // Miscellaneous
                Section(
                    title = Str(R.string.miscellaneous),
                    horizontalPadding = 8,
                    isCollapsed = remember { mutableStateOf(spfSections.isMiscCollapsed) },
                    onToggle = {
                        spfSections.isMiscCollapsed = it
                    },
                    content = {
                        Column {
                            Language()
                            Theme()
                            BackupRestore()
                            SettingRow {
                                RowVCenterSpaced(8) {
                                    FAQ()
                                    About()
                                }
                            }
                        }
                    },
                    contentCollapsed = {
                        VersionSummary()
                    }
                )
            }
        }
    }



@Composable
fun QuickSettingHeader(
    labelId: Int,
    modifier: Modifier = Modifier,
    isFirst: Boolean = false,
) {
    Text(
        text = stringResource(id = labelId),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .padding(top = if (isFirst) 4.dp else 12.dp, bottom = 4.dp)
            .semantics { heading() },
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
fun SettingRow(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    RowVCenter(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = SettingRowMinHeight.dp)
            .padding(vertical = 2.dp)
    ) {
        content()
    }
}

@Composable
fun SettingLabel(
    labelId: Int,
    modifier: Modifier = Modifier,
    color: Color? = null,
) {
    Text(
        text = stringResource(id = labelId),
        modifier = modifier,
        fontSize = 15.sp,
        lineHeight = 18.sp,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        fontWeight = FontWeight.Medium,
        color = color ?: MaterialTheme.colorScheme.onSurface,
    )
}

// This is used in SettingScreen
@Composable
fun LabeledRow(
    labelId: Int?,
    modifier: Modifier = Modifier,
    color: Color? = null,

    // Padding indentation for labels in Section Group
    paddingHorizontal: Int = 0,

    // Show the question icon or not
    helpTooltip: String? = null,

    // Show a down arrow to indicate the content below is collapsed
    // - null: it's not collapsable
    // - true/false: if it's collapsed or not
    isCollapsed: Boolean? = false,
    toggleCollapse: Lambda? = null,

    // Items on the right side, e.g.: "New" button
    content: @Composable RowScope.() -> Unit,
) {

    SettingRow(
        modifier = modifier
            .let { if (toggleCollapse != null) it.clickable(onClick = toggleCollapse) else it }
            .padding(horizontal = paddingHorizontal.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.weight(1f, fill = false),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                if (labelId != null) {
                    SettingLabel(
                        labelId,
                        modifier = Modifier.weight(1f, fill = false),
                        color = color,
                    )
                }

                // collapsed indicator
                if (isCollapsed == true) {
                    GreyIcon16(
                        iconId = R.drawable.ic_dropdown_arrow,
                    )
                }

                // balloon tooltip
                helpTooltip?.let {
                    BalloonQuestionMark(it)
                }
            }

            // rest content
            RowVCenter(
                modifier = Modifier.padding(start = 8.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                content()
            }
        }
    }
}

