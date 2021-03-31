package com.lehaine.pixelheist

import com.lehaine.lib.getByPrefix
import com.lehaine.lib.particle.Particle
import com.lehaine.lib.particle.ParticleSimulator
import com.lehaine.lib.random
import com.lehaine.lib.randomD
import com.soywiz.klock.TimeSpan
import com.soywiz.klock.seconds
import com.soywiz.korge.view.fast.*
import com.soywiz.korim.bitmap.BmpSlice
import com.soywiz.korim.color.RGBA
import kotlin.math.sin

class Fx(val level: GameLevel, private val particleContainer: FastSpriteContainer) {

    private val particleSimulator = ParticleSimulator(2048)
    private var frame = 0


    private fun alloc(slice: BmpSlice, x: Double, y: Double) = particleSimulator.alloc(particleContainer, slice, x, y)


    fun update(dt: TimeSpan) {
        particleSimulator.simulate(dt)
        frame++
    }


    fun dots(x: Double, y: Double) {
        for (i in 0..80) {
            val p = alloc(Assets.tiles.getByPrefix("fxDot"), x, y)
            p.moveAwayFrom(x, y, (1..3).randomD())
            p.alpha = (0.4f..1f).random()
            p.friction = (0.8..0.9).random()
            p.gravityY = (0.0..0.02).random()
            p.life = (2..3).random().seconds
            val grav = p.gravityY
            p.onUpdate = {
                if (it.isColliding()) {
                    it.gravityY = 0.0
                } else {
                    it.gravityY = grav
                }

            }
        }
    }

    fun bloodSplatter(x: Double, y: Double) {
        for (i in 0..2) {
            val p = alloc(Assets.tiles.getByPrefix("fxDot"), x, y)
            p.color = RGBA((111..255).random(), 0, 0, (0..255).random())
            p.xDelta = sin(x + frame * 0.03) * 0.02
            p.gravityY = (0.1..0.2).random()
            p.friction = (0.85..0.96).random()
            p.life = (1..3).random().seconds
            p.onUpdate = ::bloodPhysics
        }
    }

    private fun bloodPhysics(particle: Particle) {
        if (particle.isColliding() && particle.data0 != 1) {
            particle.data0 = 1
            particle.xDelta *= 0.4
            particle.yDelta = 0.0
            particle.gravityY = (0.0..0.001).random()
            particle.friction = (0.5..0.7).random()
            particle.scaleDeltaY = (0.0..0.001).random()
            particle.rotation = 0.0
            particle.rotationDelta = 0.0
            if (particle.isColliding(-5) || particle.isColliding(5)) {
                particle.scaleY *= (2..3).random()
            }
            if (particle.isColliding(offsetY = -5) || particle.isColliding(offsetY = 5)) {
                particle.scaleX *= (2..3).random()
            }
        }
    }

    private fun Particle.isColliding(offsetX: Int = 0, offsetY: Int = 0) =
        level.hasCollision(((x + offsetX) / GRID_SIZE).toInt(), ((y + offsetY) / GRID_SIZE).toInt())
}