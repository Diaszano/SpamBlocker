package spam.blocker.ui.theme

import androidx.compose.ui.graphics.Color
import spam.blocker.util.spf
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

fun linearizeComponent(component: Float): Double {
    return if (component <= 0.04045f) {
        component.toDouble() / 12.92
    } else {
        ((component.toDouble() + 0.055) / 1.055).pow(2.4)
    }
}

fun relativeLuminance(color: Color): Double {
    val r = linearizeComponent(color.red)
    val g = linearizeComponent(color.green)
    val b = linearizeComponent(color.blue)
    return 0.2126 * r + 0.7152 * g + 0.0722 * b
}

fun contrastRatio(c1: Color, c2: Color): Double {
    val l1 = relativeLuminance(c1)
    val l2 = relativeLuminance(c2)
    val lighter = max(l1, l2)
    val darker = min(l1, l2)
    return (lighter + 0.05) / (darker + 0.05)
}

fun readableOnAll(foreground: Color, backgrounds: List<Color>, minimumRatio: Double = 4.5): Boolean {
    return backgrounds.all { bg -> contrastRatio(foreground, bg) >= minimumRatio }
}

fun badgeContentColor(containerColor: Color): Color {
    val whiteContrast = contrastRatio(Color.White, containerColor)
    val darkText = Black111111
    val darkContrast = contrastRatio(darkText, containerColor)
    return if (whiteContrast >= 4.5 || whiteContrast > darkContrast) Color.White else darkText
}

fun validateColorContrast(
    key: String,
    candidateColor: Color,
    palette: spf.Palette
): Boolean {
    val currentBg = palette.background
    val currentDialogBg = palette.dialogBg
    val currentText = palette.textGrey

    return when (key) {
        "background" -> {
            contrastRatio(currentText, candidateColor) >= 4.5
        }
        "dialogBg" -> {
            contrastRatio(currentText, candidateColor) >= 4.5
        }
        "text" -> {
            readableOnAll(candidateColor, listOf(currentBg, currentDialogBg), 4.5)
        }
        else -> {
            readableOnAll(candidateColor, listOf(currentBg, currentDialogBg), 3.0)
        }
    }
}
