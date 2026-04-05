package com.biprangshu.spendwise.ui.components.confetti

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class Particle(
    var x: Float,
    var y: Float,
    var velocityX: Float,
    var velocityY: Float,
    var rotation: Float,
    var rotationSpeed: Float,
    var color: Color,
    var size: Float,
    var alpha: Float = 1f,
    var lifetime: Float = 1f,  // 0 to 1, decreases over time
    var shape: ParticleShape = ParticleShape.RECTANGLE
) {

    fun update(deltaTime: Float, gravity: Float = 980f, friction: Float = 0.99f) {
        // Apply gravity
        velocityY += gravity * deltaTime
        
        // Apply friction
        velocityX *= friction
        velocityY *= friction
        
        // Update position
        x += velocityX * deltaTime
        y += velocityY * deltaTime
        
        // Update rotation
        rotation += rotationSpeed * deltaTime
        
        // Update lifetime and alpha
        lifetime -= deltaTime * 0.3f  // Particles last ~3 seconds
        alpha = (lifetime * 1.5f).coerceIn(0f, 1f)
    }


    fun isAlive(): Boolean = lifetime > 0 && alpha > 0.01f

    companion object {
        private val CONFETTI_COLORS = listOf(
            Color(0xFFFF6B6B),  // Red
            Color(0xFF4ECDC4),  // Teal
            Color(0xFFFFE66D),  // Yellow
            Color(0xFF95E1D3),  // Mint
            Color(0xFFF38181),  // Coral
            Color(0xFF7C73E6),  // Purple
            Color(0xFF45B7D1),  // Sky blue
            Color(0xFFFF9F43),  // Orange
            Color(0xFF26DE81),  // Green
            Color(0xFFFC5C65),  // Pink
        )


        fun createRandom(
            ejectPoint: Offset,
            ejectAngle: Float,  // Base angle in degrees (0 = right, 90 = up)
            angleSpread: Float = 40f,  // Spread in degrees
            force: Float = 800f,
            forceVariance: Float = 200f
        ): Particle {
            val random = Random.Default
            
            // Calculate random angle within spread
            val angle = Math.toRadians(
                (ejectAngle + random.nextFloat() * angleSpread - angleSpread / 2).toDouble()
            )
            
            // Calculate velocity with variance
            val speed = force + (random.nextFloat() - 0.5f) * forceVariance * 2
            val vx = cos(angle).toFloat() * speed
            val vy = -sin(angle).toFloat() * speed  // Negative because screen Y is inverted
            
            return Particle(
                x = ejectPoint.x + (random.nextFloat() - 0.5f) * 20,
                y = ejectPoint.y + (random.nextFloat() - 0.5f) * 20,
                velocityX = vx,
                velocityY = vy,
                rotation = random.nextFloat() * 360f,
                rotationSpeed = (random.nextFloat() - 0.5f) * 720f,  // -360 to 360 degrees/sec
                color = CONFETTI_COLORS.random(),
                size = 8f + random.nextFloat() * 8f,  // 8-16 dp
                shape = ParticleShape.entries.random()
            )
        }
    }
}

/**
 * Shape types for confetti particles
 */
enum class ParticleShape {
    RECTANGLE,
    CIRCLE,
    TRIANGLE
}
