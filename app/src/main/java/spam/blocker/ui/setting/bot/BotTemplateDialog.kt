package spam.blocker.ui.setting.bot

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import spam.blocker.G
import spam.blocker.R
import spam.blocker.service.bot.BotTemplates
import spam.blocker.service.bot.WorkflowTemplate
import spam.blocker.ui.theme.AppTheme
import spam.blocker.ui.widgets.OutlineCard
import spam.blocker.ui.widgets.PopupDialog
import spam.blocker.ui.widgets.PopupSize
import spam.blocker.ui.widgets.RowVCenterSpaced
import spam.blocker.ui.widgets.Str
import spam.blocker.ui.widgets.StrokeButton
import spam.blocker.util.spf

@Composable
fun BotTemplateDialog(
    trigger: MutableState<Boolean>,
    onSelectTemplate: (WorkflowTemplate) -> Unit,
) {
    if (!trigger.value) return

    val C = G.palette
    val templates = remember { BotTemplates.getPredefinedTemplates() }

    PopupDialog(
        trigger = trigger,
        popupSize = PopupSize(maxWidthPercentage = 0.9f, minWidthDp = 340, maxWidthDp = 600),
        content = {
            Column(modifier = Modifier.padding(4.dp)) {
                Text(
                    text = stringResource(R.string.bot_templates),
                    color = C.teal200,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                templates.forEach { template ->
                    OutlineCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            RowVCenterSpaced(8) {
                                Text(
                                    text = stringResource(template.titleRes),
                                    color = C.textGrey,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                StrokeButton(
                                    label = Str(R.string.apply),
                                    color = C.teal200,
                                ) {
                                    trigger.value = false
                                    onSelectTemplate(template)
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(template.descriptionRes),
                                color = C.textGrey.copy(alpha = 0.8f),
                                fontSize = 12.sp,
                            )
                        }
                    }
                }
            }
        }
    )
}

@Preview(name = "BotTemplateDialog Compact", widthDp = 320, fontScale = 1.3f, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun BotTemplateDialogPreview() {
    val ctx = LocalContext.current
    G.palette = spf.Palette(ctx)
    val trigger = remember { mutableStateOf(true) }

    AppTheme {
        BotTemplateDialog(trigger = trigger, onSelectTemplate = {})
    }
}
