package spam.blocker.ui.theme

const val KEY_THEME_FAMILY = "theme_family"
const val KEY_THEME_APPEARANCE = "theme_appearance"

enum class ThemeFamily {
    TRUECALLER,
    CATPPUCCIN,
    CUSTOM,
}

enum class ThemeAppearance {
    SYSTEM,
    LIGHT,
    DARK,
}

data class ThemeChoice(
    val family: ThemeFamily,
    val appearance: ThemeAppearance,
)

fun resolveThemeChoice(
    savedFamily: String?,
    savedAppearance: String?,
    hasSavedCustomColors: Boolean,
): ThemeChoice {
    val family = savedFamily?.let { str ->
        ThemeFamily.entries.firstOrNull { it.name.equals(str, ignoreCase = true) }
    } ?: if (hasSavedCustomColors) ThemeFamily.CUSTOM else ThemeFamily.TRUECALLER

    val appearance = savedAppearance?.let { str ->
        ThemeAppearance.entries.firstOrNull { it.name.equals(str, ignoreCase = true) }
    } ?: ThemeAppearance.SYSTEM

    return ThemeChoice(family, appearance)
}

fun ThemeChoice.isDark(
    systemIsDark: Boolean,
    customBackgroundIsDark: Boolean,
): Boolean {
    return when (family) {
        ThemeFamily.CUSTOM -> customBackgroundIsDark
        ThemeFamily.TRUECALLER, ThemeFamily.CATPPUCCIN -> when (appearance) {
            ThemeAppearance.SYSTEM -> systemIsDark
            ThemeAppearance.LIGHT -> false
            ThemeAppearance.DARK -> true
        }
    }
}
