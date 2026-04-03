package com.biprangshu.spendwise.ui.components.confetti

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.geometry.Offset
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.random.Random

/**
 * Controller for managing confetti particle system.
 * Based on the Buckwheat app's ConfettiController implementation.
 */
class ConfettiController {
    
    private val _particles = mutableStateListOf<Particle>()
    val particles: List<Particle> get() = _particles

    private val _isActive = MutableStateFlow(false)
    val isActive: StateFlow<Boolean> = _isActive.asStateFlow()

    /**
     * Spawn confetti particles from multiple points (celebration burst)
     * 
     * @param screenWidth Width of the screen
     * @param screenHeight Height of the screen
     * @param particleCount Number of particles to spawn
     */
    fun spawnCelebration(
        screenWidth: Float,
        screenHeight: Float,
        particleCount: Int = 100
    ) {
        _isActive.value = true
        
        val random = Random.Default
        
        // Spawn from multiple points along the top
        repeat(particleCount) {
            val spawnX = random.nextFloat() * screenWidth
            val spawnY = -20f  // Just above screen
            
            _particles.add(
                Particle.createRandom(
                    ejectPoint = Offset(spawnX, spawnY),
                    ejectAngle = 270f + (random.nextFloat() - 0.5f) * 60f,  // Downward with spread
                    angleSpread = 30f,
                    force = 200f + random.nextFloat() * 300f,
                    forceVariance = 100f
                )
            )
        }
    }

    /**
     * Spawn confetti from a specific point (like Buckwheat's FillCircleStub)
     * 
     * @param ejectPoint Point to spawn particles from
     * @param ejectAngle Base ejection angle in degrees
     * @param angleSpread Spread of ejection angle
     * @param force Base ejection force
     * @param particleCount Number of particles
     */
    fun spawn(
        ejectPoint: Offset,
        ejectAngle: Float = 90f,  // Default upward
        angleSpread: Float = 80f,
        force: Float = 600f,
        particleCount: IntRange = 30..60
    ) {
        _isActive.value = true
        
        val count = particleCount.random()
        repeat(count) {
            _particles.add(
                Particle.createRandom(
                    ejectPoint = ejectPoint,
                    ejectAngle = ejectAngle,
                    angleSpread = angleSpread,
                    force = force,
                    forceVariance = force * 0.3f
                )
            )
        }
    }

    /**
     * Update all particles for one frame
     * 
     * @param deltaTime Time since last frame in seconds
     * @param screenHeight Height of screen (for cleanup)
     */
    fun update(deltaTime: Float, screenHeight: Float) {
        if (_particles.isEmpty()) {
            _isActive.value = false
            return
        }

        // Update each particle
        val iterator = _particles.iterator()
        while (iterator.hasNext()) {
            val particle = iterator.next()
            particle.update(deltaTime)
            
            // Remove dead particles or those that have fallen off screen
            if (!particle.isAlive() || particle.y > screenHeight + 100) {
                iterator.remove()
            }
        }

        // Check if all particles are done
        if (_particles.isEmpty()) {
            _isActive.value = false
        }
    }

    /**
     * Clear all particles immediately
     */
    fun clear() {
        _particles.clear()
        _isActive.value = false
    }

    /**
     * Check if there are any active particles
     */
    fun hasParticles(): Boolean = _particles.isNotEmpty()
}
