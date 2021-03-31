package com.lehaine.pixelheist

import com.lehaine.lib.getByPrefix
import com.lehaine.lib.particle.ParticleSimulator
import com.lehaine.lib.random
import com.soywiz.klock.TimeSpan
import com.soywiz.klock.seconds
import com.soywiz.korge.view.fast.FastSpriteContainer
import com.soywiz.korge.view.fast.alpha
import com.soywiz.korim.bitmap.BmpSlice

class Fx(val level: GameLevel, private val particleContainer: FastSpriteContainer) {

    private val particleSimulator = ParticleSimulator(2048)


    private fun alloc(slice: BmpSlice, x: Double, y: Double) = particleSimulator.alloc(particleContainer, slice, x, y)


    fun update(dt: TimeSpan) {
        particleSimulator.simulate(dt)
    }


    fun dots(x: Double, y: Double) {
        for (i in 0..80) {
            val p = alloc(Assets.tiles.getByPrefix("fxDot"), x, y)
            p.moveAwayFrom(x, y, (1..3).random())
            p.alpha = (0.4f..1f).random()
            p.friction = (0.8..0.9).random()
            p.gravityY = (0.0..0.02).random()
            p.life = (2..3).random().seconds
        }
    }
}