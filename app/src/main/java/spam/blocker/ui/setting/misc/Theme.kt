package spam.blocker.ui.setting.misc

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import spam.blocker.G
import spam.blocker.R
import spam.blocker.ui.setting.LabeledRow
import spam.blocker.ui.widgets.ColorPickerButton
import spam.blocker.ui.widgets.FlowRowSpaced
import spam.blocker.ui.widgets.PopupDialog
import spam.blocker.ui.widgets.Str
import spam.blocker.ui.widgets.StrokeButton


import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import spam.blocker.ui.theme.validateColorContrast


@Composable
fun EditThemeDialog(trigger: MutableState<Boolean>) {
    val ctx = LocalContext.current
    val errorMessage = remember { mutableStateOf<String?>(null) }

    PopupDialog(trigger, transparentBackground = true) {
        Column {
            errorMessage.value?.let { error ->
                Text(
                    text = error,
                    color = G.palette.error,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }
            FlowRowSpaced(10, vSpace = 20) {
                G.palette.allColors.forEach { color ->

                    ColorPickerButton(
                        color = color.state.value,
                        text = Str(color.labelId),
                        clearLabel = Str(R.string.reset),
                        clearColor = color.default
                    ) { newColor ->
                        if (newColor == null) {
                            errorMessage.value = null
                            color.reset()
                        } else {
                            if (validateColorContrast(color.key, newColor, G.palette)) {
                                errorMessage.value = null
                                color.update(newColor)
                            } else {
                                val warning = ctx.getString(R.string.insufficient_contrast)
                                errorMessage.value = warning
                                Toast.makeText(ctx, warning, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Theme() {
    LabeledRow(R.string.theme) {
        val trigger = remember { mutableStateOf(false) }
        EditThemeDialog(trigger)

        StrokeButton(
            Str(R.string.customize),
            color = G.palette.infoBlue
        ) {
            trigger.value = true
        }
    }
}