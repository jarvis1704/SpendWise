package com.biprangshu.spendwise.ui.components.confetti

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.geometry.Offset
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.random.Random


class ConfettiController {
    
    private val _particles = mutableStateListOf<Particle>()
    val particles: List<Particle> get() = _particles

    private val _isActive = MutableStateFlow(false)
    val isActive: StateFlow<Boolean> = _isActive.asStateFlow()


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


    fun clear() {
        _particles.clear()
        _isActive.value = false
    }


    fun hasParticles(): Boolean = _particles.isNotEmpty()
}
