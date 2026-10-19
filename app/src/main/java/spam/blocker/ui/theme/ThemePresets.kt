package spam.blocker.ui.theme

import androidx.compose.ui.graphics.Color

val truecallerLightColors = mapOf(
    "text" to Color(0xFF1E293B),
    "info" to Color(0xFF0284C7),
    "warning" to Color(0xFFC2410C),
    "success" to Color(0xFF16A34A),
    "error" to Color(0xFFDC2626),
    "priority" to Color(0xFF7E22CE),
    "regexFlags" to Color(0xFF9333EA),
    "enabled" to Color(0xFF0066CC),
    "disabled" to Color(0xFF64748B),
    "dialogBg" to Color(0xFFFFFFFF),
    "background" to Color(0xFFF7F9FC),
)

val truecallerDarkColors = mapOf(
    "text" to Color(0xFFF8FAFC),
    "info" to Color(0xFF60A5FA),
    "warning" to Color(0xFFFB923C),
    "success" to Color(0xFF4ADE80),
    "error" to Color(0xFFF87171),
    "priority" to Color(0xFFC084FC),
    "regexFlags" to Color(0xFFE879F9),
    "enabled" to Color(0xFF38BDF8),
    "disabled" to Color(0xFF94A3B8),
    "dialogBg" to Color(0xFF1E293B),
    "background" to Color(0xFF0F172A),
)

val catppuccinLatteColors = mapOf(
    "text" to Color(0xFF4C4F69),
    "info" to Color(0xFF1E66F5),
    "warning" to Color(0xFFFE640B),
    "success" to Color(0xFF40A02B),
    "error" to Color(0xFFD20F39),
    "priority" to Color(0xFF8839EF),
    "regexFlags" to Color(0xFFEA76CB),
    "enabled" to Color(0xFF179299),
    "disabled" to Color(0xFF6C6F85),
    "dialogBg" to Color(0xFFCCD0DA),
    "background" to Color(0xFFEFF1F5),
)

val catppuccinMacchiatoColors = mapOf(
    "text" to Color(0xFFCAD3F5),
    "info" to Color(0xFF8AADF4),
    "warning" to Color(0xFFF5A97F),
    "success" to Color(0xFFA6DA95),
    "error" to Color(0xFFED8796),
    "priority" to Color(0xFFC6A0F6),
    "regexFlags" to Color(0xFFF5BDE6),
    "enabled" to Color(0xFF8BD5CA),
    "disabled" to Color(0xFFA5ADCB),
    "dialogBg" to Color(0xFF363A4F),
    "background" to Color(0xFF24273A),
)

fun presetColors(family: ThemeFamily, dark: Boolean): Map<String, Color> {
    return when (family) {
        ThemeFamily.TRUECALLER -> if (dark) truecallerDarkColors else truecallerLightColors
        ThemeFamily.CATPPUCCIN -> if (dark) catppuccinMacchiatoColors else catppuccinLatteColors
        ThemeFamily.CUSTOM -> emptyMap()
    }
}
