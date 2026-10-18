package spam.blocker.ui.setting

import androidx.compose.foundation.clickable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import spam.blocker.R
import spam.blocker.ui.widgets.BalloonQuestionMark
import spam.blocker.ui.widgets.GreyIcon16
import spam.blocker.ui.widgets.SwitchBox

@Composable
fun SettingItem(
    labelId: Int,
    modifier: Modifier = Modifier,
    helpTooltip: String? = null,
    value: String? = null,
    onValueClick: (() -> Unit)? = null,
    checked: Boolean? = null,
    onCheckedChange: ((Boolean) -> Unit)? = null,
    expanded: Boolean? = null,
    onExpand: (() -> Unit)? = null,
) {
    require(checked == null || expanded == null) { "A setting row can be a switch or a disclosure" }
    val label = stringResource(labelId)
    val expansionState = expanded?.let { stringResource(if (it) R.string.section_expanded else R.string.section_collapsed) }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .let {
                when {
                    checked != null && onCheckedChange != null -> it.toggleable(checked, role = Role.Switch, onValueChange = onCheckedChange)
                    expanded != null && onExpand != null -> it.clickable(role = Role.Button, onClick = onExpand)
                    else -> it
                }
            }
            .let { if (expansionState != null) it.semantics { stateDescription = expansionState } else it }
            .heightIn(min = 48.dp)
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SettingLabel(labelId, Modifier.weight(1f))
        helpTooltip?.let { BalloonQuestionMark(it) }
        value?.let {
            Box(
                modifier = Modifier
                    .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                    .let { target ->
                        if (onValueClick != null) target.clickable(onClick = onValueClick)
                            .semantics { contentDescription = "$label: $value" }
                        else target
                    }
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = it, modifier = Modifier.widthIn(max = 112.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (checked != null) {
            SwitchBox(checked = checked, onCheckedChange = null)
        } else if (expanded != null) {
            GreyIcon16(if (expanded) R.drawable.ic_dropdown_arrow else R.drawable.ic_right_arrow)
        }
    }
}
