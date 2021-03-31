package com.lehaine.lib.particle

import com.soywiz.kds.iterators.fastForEach
import com.soywiz.klock.DateTime
import com.soywiz.klock.TimeSpan
import com.soywiz.klock.milliseconds
import com.soywiz.klock.seconds
import com.soywiz.korge.view.fast.*
import com.soywiz.korim.bitmap.Bitmaps
import com.soywiz.korim.bitmap.BmpSlice
import com.soywiz.korim.color.Colors
import kotlin.math.pow

class ParticleSimulator(maxParticles: Int) {

    val particles = List(maxParticles) { init(Particle(Bitmaps.white).apply { index = it }) }

    private var numAlloc = 0


    fun alloc(fastSpriteContainer: FastSpriteContainer, bmpSlice: BmpSlice, x: Double, y: Double): Particle {
        return if (numAlloc < particles.size - 1) {
            val particle = reset(particles[numAlloc], fastSpriteContainer, bmpSlice).also {
                it.x = x
                it.y = y
            }
            numAlloc++
            particle
        } else {
            var best: Particle? = null
            particles.fastForEach {
                val b = best
                if (b == null || it.timeStamp < b.timeStamp) {
                    best = it
                }
            }
            best?.onKill?.invoke()
            best?.let {
                reset(it, fastSpriteContainer, bmpSlice)
                it.x = x
                it.y = y
            }
            best!!
        }
    }

    private fun init(particle: Particle): Particle {
        with(particle) {
            scale(1f)
            rotationRadiansf = 0f
            color = Colors.WHITE
            visible = true
            alpha = 1f

            xDelta = 0.0
            yDelta = 0.0
            alphaDelta = 0.0
            scaleDelta = 0.0
            scaleDeltaX = 0.0
            scaleDeltaY = 0.0
            scaleFriction = 1.0
            scaleMultiplier = 1.0
            scaleXMultiplier = 1.0
            scaleYMultiplier = 1.0
            rotationDelta = 0.0
            rotationFriction = 1.0
            frictionX = 1.0
            frictionY = 1.0
            gravityX = 0.0
            gravityY = 0.0
            fadeOutSpeed = 0.1
            life = 1.seconds

            colorR = 1.0
            colorG = 1.0
            colorB = 1.0
            colorA = 1.0
            colorRdelta = 0.0
            colorGdelta = 0.0
            colorBdelta = 0.0
            colorAdelta = 0.0

            onStart = null
            onUpdate = null
            onKill = null

            timeStamp = DateTime.nowUnix()
            killed = false
        }
        return particle
    }

    private fun reset(particle: Particle, fastSpriteContainer: FastSpriteContainer, bmpSlice: BmpSlice): Particle {
        val result = init(particle)
        result.tex = bmpSlice
        if (result.container != fastSpriteContainer) {
            result.container?.delete(result)
            fastSpriteContainer.addChild(result)
        }

        return result
    }

    private fun kill(particle: Particle) {
        particle.alpha = 0f
        particle.life = TimeSpan.ZERO
        particle.killed = true
        particle.visible = false
    }

    private fun advance(particle: Particle, dt: TimeSpan) {
        particle.delay -= dt
        if (particle.killed || particle.delay > 0.milliseconds) return

        particle.onStart?.invoke()
        particle.onStart = null

        val tmod = if (dt == 0.milliseconds) 0.0 else (dt / 16.666666.milliseconds)
        with(particle) {
            // gravity
            xDelta += gravityX * tmod
            yDelta += gravityY * tmod

            // movement
            x += xDelta * tmod
            y += yDelta * tmod

            // friction
            if (frictionX == frictionY) {
                val frictTmod = frictionX.fastPow(tmod)
                xDelta *= frictTmod
                yDelta *= frictTmod
            } else {
                xDelta *= frictionX.fastPow(tmod)
                yDelta *= frictionY.fastPow(tmod)
            }

            rotation += rotationDelta * tmod
            rotationDelta *= rotationFriction.fastPow(tmod)
            scaleX += (scaleDelta + scaleDeltaX) * tmod
            scaleY += (scaleDelta + scaleDeltaY) * tmod
            // TODO fix
//            val scaleMulTmod = scaleMultiplier.fastPow(tmod)
//            scaleX *= scaleMulTmod
//            scaleX *= scaleXMultiplier.fastPow(tmod)
//            scaleY *= scaleMulTmod
//            scaleY *= scaleYMultiplier.fastPow(tmod)
            val scaleFrictPow = scaleFriction.fastPow(tmod)
            scaleDelta *= scaleFrictPow
            scaleDeltaX *= scaleFrictPow
            scaleDeltaY *= scaleFrictPow

            colorR += particle.colorRdelta * tmod
            colorG += particle.colorGdelta * tmod
            colorB += particle.colorBdelta * tmod
            colorA += particle.colorAdelta * tmod
            alpha -= alphaDelta.toFloat()

            remainingLife -= dt
            if (remainingLife <= 0.milliseconds) {
                alpha -= (fadeOutSpeed * tmod).toFloat()
            }

            if (remainingLife <= 0.milliseconds && alpha <= 0) {
                onKill?.invoke()
                kill(particle)
            } else {
                onUpdate?.invoke(particle)
            }
        }
    }

    fun simulate(dt: TimeSpan) {
        for (i in 0..numAlloc) {
            val particle = particles[i]
            advance(particle, dt)
        }
    }
}


private fun Double.fastPow(power: Double): Double {
    if (power == 1.0 || this == 0.0 || this == 1.0) {
        return this
    }
    return pow(power)
}