package com.lehaine.game

import com.lehaine.kiwi.getByPrefix
import com.lehaine.kiwi.getRandomByPrefix
import com.lehaine.kiwi.particle.Particle
import com.lehaine.kiwi.particle.ParticleSimulator
import com.lehaine.kiwi.random
import com.lehaine.kiwi.randomd
import com.soywiz.klock.TimeSpan
import com.soywiz.klock.milliseconds
import com.soywiz.klock.seconds
import com.soywiz.korge.view.fast.*
import com.soywiz.korim.bitmap.BmpSlice
import com.soywiz.korim.color.RGBA
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class Fx(val level: GameLevel, private val particleContainer: FastSpriteContainer) {

    private val particleSimulator = ParticleSimulator(2048)
    private var frame = 0

    private fun alloc(slice: BmpSlice, x: Double, y: Double) = particleSimulator.alloc(particleContainer, slice, x, y)

    fun update(dt: TimeSpan) {
        particleSimulator.simulate(dt)
        frame++
    }

    fun itemTeleported(x: Double, y: Double, color: RGBA) {
        create(20) {
            val p = alloc(Assets.tiles.getByPrefix("fxDot"), x, y)
            p.color = color
            p.alphaDelta = 0.05
            p.alpha = (0.8f..1f).random()
            p.scaleX = (0.2..0.4).random()
            p.scaleY = (0.2..0.4).random()
            p.xDelta = cos(PI / 8 * it) * 2
            p.yDelta = sin(PI / 8 * it) * 2
            p.scaleDelta = 0.05
            p.life = 100.milliseconds
        }
    }


    fun swipe(x: Double, y: Double, dir: Int) {
        val p = alloc(
            Assets.tiles.getRandomByPrefix("fxSwipe"),
            x + dir + (0..8).random(), y + (0..4).random()
        )
        p.scaleX = (0.8..1.2).random()
        p.scaleY = (0.7..0.9).random()
        p.rotationDelta = (0.2..0.3).random() * dir
        p.rotationFriction = 0.8
        if (dir == 1) {
            p.rotation = -1 + (0.0..0.5).random()
        } else {
            p.rotation = PI + 1 - (0.0..0.5).random()
        }
        p.life = (0.03..0.06).random().seconds

    }

    fun gutsSplatter(x: Double, y: Double, dir: Int) {
        create(50) {
            val p = alloc(Assets.tiles.getRandomByPrefix("fxGib"), x, y)
            p.color = RGBA((111..255).random(), 0, 0, (0..255).random())
            p.xDelta = dir * (3..7).randomd()
            p.yDelta = (-1..0).randomd()
            p.gravityY = (0.07..0.1).random()
            p.rotation = (0.0..PI * 2).random()
            p.friction = (0.92..0.96).random()
            p.rotation = (0.0..PI * 2).random()
            p.scale(0.7)
            p.life = (3..10).random().seconds
            p.onUpdate = ::bloodPhysics
        }
    }

    fun bloodSplatter(x: Double, y: Double) {
        create(10) {
            val p = alloc(Assets.tiles.getByPrefix("fxDot"), x, y)
            p.color = RGBA((111..255).random(), 0, 0, (0..255).random())
            p.xDelta = sin(x + frame * 0.03) * 0.5
            p.gravityY = (0.1..0.2).random()
            p.friction = (0.85..0.96).random()
            p.rotation = (0.0..PI * 2).random()
            p.life = (1..3).random().seconds
            p.delay = (0.0..0.1).random().seconds
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
                particle.scaleY *= (1.0..1.25).random()
            }
            if (particle.isColliding(offsetY = -5) || particle.isColliding(offsetY = 5)) {
                particle.scaleX *= (1.0..1.25).random()
            }
        }
    }

    private fun create(num: Int, createParticle: (index: Int) -> Unit) {
        for (i in 0..num) {
            createParticle(i)
        }
    }

    private fun Particle.isColliding(offsetX: Int = 0, offsetY: Int = 0) =
        level.hasCollision(((x + offsetX) / GRID_SIZE).toInt(), ((y + offsetY) / GRID_SIZE).toInt())
}