package spam.blocker.ui.widgets

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.collapse
import androidx.compose.ui.semantics.expand
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import spam.blocker.G
import spam.blocker.R
import spam.blocker.ui.M
import spam.blocker.ui.slightDiff

@Composable
fun Section(
    title: String?,
    titleColor: Color = G.palette.textGrey,
    horizontalPadding : Int = 0,
    bgColor: Color = G.palette.background,
    isCollapsed: MutableState<Boolean>? = null, // null == non-foldable, e.g. sections in api dialogs
    onToggle: ((Boolean) -> Unit)? = null,
    contentCollapsed: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = M
            .padding(top = 8.dp, bottom = 8.dp)
    ) {
        // the rectangle section border
        Box(
            modifier = M
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding.dp)
                .border(0.5.dp, bgColor.slightDiff(), shape = RoundedCornerShape(4.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .wrapContentHeight()
        ) {
            // Show expanded content
            AnimatedVisibleV(isCollapsed?.value != true) {
                content()
            }
            // Show folded summary
            AnimatedVisibleV(isCollapsed?.value == true) {
                Column(
                    modifier = M.fillMaxWidth().clickable(
                        role = Role.Button,
                        onClickLabel = stringResource(R.string.expand),
                    ) {
                        isCollapsed!!.value = !isCollapsed.value
                        onToggle?.invoke(isCollapsed.value)
                    }.heightIn(min = 48.dp).padding(4.dp)
                ) {
                    contentCollapsed?.let { it() }
                }
            }
        }

        // the section title
        if (title != null) {
            val state = if (isCollapsed?.value == true) R.string.section_collapsed else R.string.section_expanded
            val stateText = stringResource(state)
            val actionLabel = if (isCollapsed != null) {
                stringResource(if (isCollapsed.value) R.string.expand else R.string.collapse)
            } else null
            val chevronRotation by animateFloatAsState(
                targetValue = if (isCollapsed?.value == true) -90f else 0f,
                label = "sectionChevron"
            )

            Box(
                modifier = M
                    .fillMaxWidth()
                    .offset(y = (-8).dp)
                    .heightIn(min = 48.dp)
                    .let {
                        if (isCollapsed != null) { // is foldable
                            it.clickable(
                                role = Role.Button,
                                onClickLabel = actionLabel,
                            ) {
                                isCollapsed.value = !isCollapsed.value
                                onToggle?.invoke(isCollapsed.value)
                            }
                        } else it
                    }
                    .semantics {
                        heading()
                        if (isCollapsed != null) {
                            stateDescription = stateText
                            collapse {
                                if (!isCollapsed.value) {
                                    isCollapsed.value = true
                                    onToggle?.invoke(true)
                                    true
                                } else false
                            }
                            expand {
                                if (isCollapsed.value) {
                                    isCollapsed.value = false
                                    onToggle?.invoke(false)
                                    true
                                } else false
                            }
                        }
                    }
            ) {
                Box(
                    modifier = M
                        .wrapContentWidth()
                        .offset(x = 20.dp)
                        .background(bgColor)
                ) {
                    RowVCenterSpaced(4, modifier = Modifier.padding(horizontal = 10.dp)) {
                        Text(
                            text = title,
                            fontSize = 13.sp,
                            color = titleColor,
                            lineHeight = 16.sp,
                        )
                        if (isCollapsed != null) {
                            ResIcon(
                                iconId = R.drawable.ic_dropdown_arrow,
                                color = titleColor,
                                modifier = Modifier
                                    .size(14.dp)
                                    .rotate(chevronRotation)
                            )
                        }
                    }
                }
            }
        }
    }
}
