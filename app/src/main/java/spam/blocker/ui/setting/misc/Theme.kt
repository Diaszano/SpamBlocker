package spam.blocker.ui.setting.misc

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import spam.blocker.G
import spam.blocker.R
import spam.blocker.ui.isLight
import spam.blocker.ui.setting.LabeledRow
import spam.blocker.ui.theme.ThemeAppearance
import spam.blocker.ui.theme.ThemeChoice
import spam.blocker.ui.theme.ThemeFamily
import spam.blocker.ui.theme.isDark
import spam.blocker.ui.theme.presetColors
import spam.blocker.ui.widgets.ColorPickerButton
import spam.blocker.ui.widgets.ComboBox
import spam.blocker.ui.widgets.FlowRowSpaced
import spam.blocker.ui.widgets.LabelItem
import spam.blocker.ui.widgets.PopupDialog
import spam.blocker.ui.widgets.Str
import spam.blocker.ui.widgets.StrokeButton
import spam.blocker.util.spf

@Composable
fun EditThemeDialog(trigger: MutableState<Boolean>) {
    PopupDialog(trigger, transparentBackground = true) {
        FlowRowSpaced(10, vSpace = 20) {
            G.palette.allColors.forEach { color ->

                ColorPickerButton(
                    color = color.state.value,
                    text = Str(color.labelId),
                    clearLabel = Str(R.string.reset),
                    clearColor = color.default
                ) { newColor ->
                    if (newColor == null)
                        color.reset()
                    else
                        color.update(newColor)
                }
            }
        }
    }
}

@Composable
fun Theme() {
    val ctx = LocalContext.current
    val systemIsDark = isSystemInDarkTheme()
    val choice = G.currentThemeChoice.value
    val themeSpf = remember { spf.Theme(ctx) }

    val familyItems = remember(systemIsDark, ctx) {
        listOf(
            LabelItem(
                id = ThemeFamily.TRUECALLER.name,
                label = ctx.getString(R.string.theme_truecaller),
                onClick = {
                    val current = G.currentThemeChoice.value
                    val newChoice = ThemeChoice(ThemeFamily.TRUECALLER, current.appearance)
                    themeSpf.themeFamily = newChoice.family.name
                    G.currentThemeChoice.value = newChoice
                    val dark = newChoice.isDark(systemIsDark, !G.palette.background.isLight())
                    G.palette.applyPreset(presetColors(newChoice.family, dark))
                }
            ),
            LabelItem(
                id = ThemeFamily.CATPPUCCIN.name,
                label = ctx.getString(R.string.theme_catppuccin),
                onClick = {
                    val current = G.currentThemeChoice.value
                    val newChoice = ThemeChoice(ThemeFamily.CATPPUCCIN, current.appearance)
                    themeSpf.themeFamily = newChoice.family.name
                    G.currentThemeChoice.value = newChoice
                    val dark = newChoice.isDark(systemIsDark, !G.palette.background.isLight())
                    G.palette.applyPreset(presetColors(newChoice.family, dark))
                }
            ),
            LabelItem(
                id = ThemeFamily.CUSTOM.name,
                label = ctx.getString(R.string.custom),
                onClick = {
                    val current = G.currentThemeChoice.value
                    val newChoice = ThemeChoice(ThemeFamily.CUSTOM, current.appearance)
                    themeSpf.themeFamily = newChoice.family.name
                    G.currentThemeChoice.value = newChoice
                    G.palette.restoreCustom()
                }
            )
        )
    }

    val appearanceItems = remember(systemIsDark, ctx) {
        listOf(
            LabelItem(
                id = ThemeAppearance.SYSTEM.name,
                label = ctx.getString(R.string.appearance_system),
                onClick = {
                    val current = G.currentThemeChoice.value
                    val newChoice = ThemeChoice(current.family, ThemeAppearance.SYSTEM)
                    themeSpf.themeAppearance = newChoice.appearance.name
                    G.currentThemeChoice.value = newChoice
                    val dark = newChoice.isDark(systemIsDark, !G.palette.background.isLight())
                    G.palette.applyPreset(presetColors(newChoice.family, dark))
                }
            ),
            LabelItem(
                id = ThemeAppearance.LIGHT.name,
                label = ctx.getString(R.string.appearance_light),
                onClick = {
                    val current = G.currentThemeChoice.value
                    val newChoice = ThemeChoice(current.family, ThemeAppearance.LIGHT)
                    themeSpf.themeAppearance = newChoice.appearance.name
                    G.currentThemeChoice.value = newChoice
                    val dark = newChoice.isDark(systemIsDark, !G.palette.background.isLight())
                    G.palette.applyPreset(presetColors(newChoice.family, dark))
                }
            ),
            LabelItem(
                id = ThemeAppearance.DARK.name,
                label = ctx.getString(R.string.appearance_dark),
                onClick = {
                    val current = G.currentThemeChoice.value
                    val newChoice = ThemeChoice(current.family, ThemeAppearance.DARK)
                    themeSpf.themeAppearance = newChoice.appearance.name
                    G.currentThemeChoice.value = newChoice
                    val dark = newChoice.isDark(systemIsDark, !G.palette.background.isLight())
                    G.palette.applyPreset(presetColors(newChoice.family, dark))
                }
            )
        )
    }

    val selectedFamilyIndex = when (choice.family) {
        ThemeFamily.TRUECALLER -> 0
        ThemeFamily.CATPPUCCIN -> 1
        ThemeFamily.CUSTOM -> 2
    }

    val selectedAppearanceIndex = when (choice.appearance) {
        ThemeAppearance.SYSTEM -> 0
        ThemeAppearance.LIGHT -> 1
        ThemeAppearance.DARK -> 2
    }

    LabeledRow(R.string.theme) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ComboBox(
                items = familyItems,
                selected = selectedFamilyIndex,
            )

            if (choice.family == ThemeFamily.CUSTOM) {
                val trigger = remember { mutableStateOf(false) }
                EditThemeDialog(trigger)

                StrokeButton(
                    Str(R.string.customize),
                    color = G.palette.infoBlue
                ) {
                    trigger.value = true
                }
            } else {
                ComboBox(
                    items = appearanceItems,
                    selected = selectedAppearanceIndex,
                )
            }
        }
    }
}