package com.biprangshu.spendwise.util

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import kotlin.math.PI
import kotlin.math.sin


class WavyShape(
    private val period: Dp,
    private val amplitude: Dp,
    private val shift: Float = 0f
) : Shape {

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val periodPx = with(density) { period.toPx() }
        val amplitudePx = with(density) { amplitude.toPx() }
        
        val path = Path().apply {
            // Start at top-left
            moveTo(0f, 0f)
            
            // Line to top-right (start of wave)
            lineTo(size.width - amplitudePx, 0f)
            
            // Create wavy right edge
            var y = 0f
            val shiftOffset = shift * periodPx
            
            while (y < size.height) {
                val nextY = (y + periodPx / 2).coerceAtMost(size.height)
                val waveX = size.width - amplitudePx + 
                    amplitudePx * sin(2 * PI * (y + shiftOffset) / periodPx).toFloat()
                val nextWaveX = size.width - amplitudePx + 
                    amplitudePx * sin(2 * PI * (nextY + shiftOffset) / periodPx).toFloat()
                
                // Use quadratic bezier for smooth waves
                quadraticBezierTo(
                    waveX, y + (nextY - y) / 2,
                    nextWaveX, nextY
                )
                y = nextY
            }
            
            // Close the path
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
        
        return Outline.Generic(path)
    }
}

/**
 * A Shape that fills from left to right with a wavy edge.
 * The wave is on the right side, creating a "liquid fill" effect.
 *
 * @param period The wavelength of the wave
 * @param amplitude The height of the wave peaks
 * @param shift Animation offset (0.0 to 1.0) for animating the wave
 */
class WavyFillShape(
    private val period: Dp,
    private val amplitude: Dp,
    private val shift: Float = 0f
) : Shape {

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val periodPx = with(density) { period.toPx() }
        val amplitudePx = with(density) { amplitude.toPx() }
        
        val path = Path().apply {
            // Start at bottom-left
            moveTo(0f, size.height)
            
            // Line to top-left
            lineTo(0f, 0f)
            
            // Create wavy top edge (horizontal wave)
            var x = 0f
            val shiftOffset = shift * periodPx
            
            while (x < size.width) {
                val nextX = (x + periodPx / 2).coerceAtMost(size.width)
                val waveY = amplitudePx + 
                    amplitudePx * sin(2 * PI * (x + shiftOffset) / periodPx).toFloat()
                val nextWaveY = amplitudePx + 
                    amplitudePx * sin(2 * PI * (nextX + shiftOffset) / periodPx).toFloat()
                
                quadraticBezierTo(
                    x + (nextX - x) / 2, waveY,
                    nextX, nextWaveY
                )
                x = nextX
            }
            
            // Line to bottom-right and close
            lineTo(size.width, size.height)
            close()
        }
        
        return Outline.Generic(path)
    }
}
