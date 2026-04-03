package com.biprangshu.spendwise.ui.components.confetti

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.onSizeChanged
import kotlin.math.cos
import kotlin.math.sin

/**
 * Confetti overlay composable that renders particles using Canvas.
 * Based on the Buckwheat app's ConfettiBox implementation.
 *
 * @param controller The ConfettiController managing particles
 * @param modifier Modifier for the overlay
 * @param onAnimationComplete Callback when all particles have finished
 */
@Composable
fun ConfettiOverlay(
    controller: ConfettiController,
    modifier: Modifier = Modifier,
    onAnimationComplete: () -> Unit = {}
) {
    var screenSize by remember { mutableStateOf(Size.Zero) }
    var lastFrameTime by remember { mutableLongStateOf(0L) }

    // Animation loop
    LaunchedEffect(controller) {
        while (true) {
            withFrameMillis { frameTimeMillis ->
                if (controller.hasParticles()) {
                    val deltaTime = if (lastFrameTime > 0) {
                        ((frameTimeMillis - lastFrameTime) / 1000f).coerceAtMost(0.1f)
                    } else {
                        0.016f  // ~60fps default
                    }
                    lastFrameTime = frameTimeMillis
                    controller.update(deltaTime, screenSize.height)
                } else {
                    lastFrameTime = 0L
                }
            }
        }
    }

    // Check for animation complete
    LaunchedEffect(controller.isActive) {
        controller.isActive.collect { active ->
            if (!active && controller.particles.isEmpty()) {
                onAnimationComplete()
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { size ->
                screenSize = Size(size.width.toFloat(), size.height.toFloat())
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            controller.particles.forEach { particle ->
                drawParticle(particle)
            }
        }
    }
}

/**
 * Extension function to draw a single particle on Canvas
 */
private fun DrawScope.drawParticle(particle: Particle) {
    val color = particle.color.copy(alpha = particle.alpha)

    rotate(
        degrees = particle.rotation,
        pivot = Offset(particle.x, particle.y)
    ) {
        when (particle.shape) {
            ParticleShape.RECTANGLE -> {
                drawRect(
                    color = color,
                    topLeft = Offset(
                        particle.x - particle.size / 2,
                        particle.y - particle.size / 4
                    ),
                    size = Size(particle.size, particle.size / 2)
                )
            }

            ParticleShape.CIRCLE -> {
                drawCircle(
                    color = color,
                    radius = particle.size / 2,
                    center = Offset(particle.x, particle.y)
                )
            }

            ParticleShape.TRIANGLE -> {
                val path = Path().apply {
                    val halfSize = particle.size / 2
                    // Equilateral triangle
                    moveTo(particle.x, particle.y - halfSize)  // Top
                    lineTo(
                        particle.x - halfSize * cos(Math.toRadians(30.0)).toFloat(),
                        particle.y + halfSize * sin(Math.toRadians(30.0)).toFloat()
                    )  // Bottom left
                    lineTo(
                        particle.x + halfSize * cos(Math.toRadians(30.0)).toFloat(),
                        particle.y + halfSize * sin(Math.toRadians(30.0)).toFloat()
                    )  // Bottom right
                    close()
                }
                drawPath(path = path, color = color)
            }
        }
    }
}

/**
 * Spawns celebration confetti from the top of the screen.
 * Call this when user achieves a milestone.
 */
@Composable
fun rememberConfettiController(): ConfettiController {
    return remember { ConfettiController() }
}
