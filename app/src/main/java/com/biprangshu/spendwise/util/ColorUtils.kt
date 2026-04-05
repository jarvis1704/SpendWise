package com.biprangshu.spendwise.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils as AndroidColorUtils


fun combineColors(colorA: Color, colorB: Color, ratio: Float = 0.5f): Color {
    val clampedRatio = ratio.coerceIn(0f, 1f)
    val blended = AndroidColorUtils.blendARGB(colorA.toArgb(), colorB.toArgb(), clampedRatio)
    return Color(blended)
}

/**
 * Blends colors from a list based on position.
 * Interpolates between adjacent colors.
 *
 * @param colors List of colors to blend between
 * @param position Position in the gradient (0.0 to 1.0)
 * @return Interpolated color
 */
fun combineColors(colors: List<Color>, position: Float): Color {
    if (colors.isEmpty()) return Color.Transparent
    if (colors.size == 1) return colors.first()
    
    val clampedPosition = position.coerceIn(0f, 1f)
    val scaledPosition = clampedPosition * (colors.size - 1)
    val index = scaledPosition.toInt().coerceIn(0, colors.size - 2)
    val localRatio = scaledPosition - index
    
    return combineColors(colors[index], colors[index + 1], localRatio)
}

/**
 * Harmonizes a design color with a source color (typically the theme primary).
 * Creates a color that feels cohesive with the app's color scheme.
 *
 * @param designColor The color to harmonize
 * @param sourceColor The source color to harmonize with (usually primary)
 * @return Harmonized color
 */
@Composable
fun harmonize(designColor: Color, sourceColor: Color): Color {
    // Simple harmonization: blend the design color slightly toward the source
    return combineColors(designColor, sourceColor, 0.15f)
}

/**
 * Data class for a harmonized color palette.
 */
data class HarmonizedColorPalette(
    val main: Color,
    val onMain: Color,
    val container: Color,
    val onContainer: Color,
    val surface: Color,
    val onSurface: Color
)

/**
 * Creates a full palette from a single color.
 *
 * @param color Base color
 * @param isDark Whether the theme is dark
 * @return Full color palette
 */
fun toPalette(color: Color, isDark: Boolean): HarmonizedColorPalette {
    return if (isDark) {
        HarmonizedColorPalette(
            main = color.copy(alpha = 0.8f),
            onMain = Color.White,
            container = combineColors(color, Color.Black, 0.6f),
            onContainer = color.copy(alpha = 0.9f),
            surface = combineColors(color, Color.Black, 0.85f),
            onSurface = Color.White.copy(alpha = 0.87f)
        )
    } else {
        HarmonizedColorPalette(
            main = color,
            onMain = Color.White,
            container = combineColors(color, Color.White, 0.85f),
            onContainer = combineColors(color, Color.Black, 0.3f),
            surface = combineColors(color, Color.White, 0.95f),
            onSurface = Color.Black.copy(alpha = 0.87f)
        )
    }
}

/**
 * Clamps a float value to a range.
 */
fun Float.clamp(min: Float, max: Float): Float = this.coerceIn(min, max)

/**
 * Budget health colors for visual feedback.
 */
object BudgetHealthColors {
    val colorGood = Color(0xFF34A853)      // Green - under budget
    val colorNotGood = Color(0xFFFBBC04)   // Yellow - near limit
    val colorBad = Color(0xFFEA4335)       // Red - over budget
    
    /**
     * Gets the appropriate color based on budget percentage spent.
     * @param percentSpent Percentage of budget spent (0.0 to 1.0+)
     * @return Color representing budget health
     */
    fun getHealthColor(percentSpent: Float): Color {
        return when {
            percentSpent <= 0.5f -> colorGood
            percentSpent <= 0.8f -> combineColors(colorGood, colorNotGood, (percentSpent - 0.5f) / 0.3f)
            percentSpent <= 1.0f -> combineColors(colorNotGood, colorBad, (percentSpent - 0.8f) / 0.2f)
            else -> colorBad
        }
    }
    
    /**
     * Gets the appropriate color based on remaining budget percentage.
     * @param percentRemaining Percentage of budget remaining (0.0 to 1.0)
     * @return Color representing budget health
     */
    fun getHealthColorFromRemaining(percentRemaining: Float): Color {
        return getHealthColor(1f - percentRemaining)
    }
}
