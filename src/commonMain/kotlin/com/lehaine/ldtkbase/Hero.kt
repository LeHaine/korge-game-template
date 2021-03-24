package com.lehaine.ldtkbase

import com.soywiz.klock.TimeSpan
import com.soywiz.klock.milliseconds
import com.soywiz.kmem.clamp
import com.soywiz.korev.Key
import com.soywiz.korge.view.*
import com.soywiz.korim.atlas.Atlas
import kotlin.math.abs
import kotlin.math.pow

inline fun Container.hero(
    x: Double = 0.0, y: Double = 0.0, atlas: Atlas, callback: @ViewDslMarker Hero.() -> Unit = {}
): Hero = Hero(x, y, atlas).addTo(this, callback)


class Hero(x: Double, y: Double, atlas: Atlas) : Container() {

    val sprite = sprite {
        smoothing = false
        anchorX = 0.5
        anchorY = 1.0
    }

    init {
        this.x = x
        this.y = y


        addUpdater { update(it) }
    }


    val animations = Animations(atlas)
    val runSpeed = 0.1

    var dx = 0.0
    var dy = 0.0

    private fun update(dt: TimeSpan) {
        if (stage == null) return
        val input = stage?.views?.input!!
        val scale = if (dt == 0.milliseconds) 0.0 else (dt / 16.666666.milliseconds)
        if (input.keys[Key.D]) {
            dx += runSpeed
            sprite.scaleX = 1.0
        }
        if (input.keys[Key.A]) {
            dx -= runSpeed
            sprite.scaleX = -1.0
        }

        dx = dx.clamp(-1.5, +1.5)
        dy = dy.clamp(-1.5, +1.5)
        x += dx * scale
        dx *= 0.9.pow(scale)
        dy *= 0.9.pow(scale)
        if (abs(dx) <= 0.08) {
            dx = 0.0
        }

        if (dx == 0.0) {
            sprite.playAnimationLooped(animations.idle)
        } else {
            sprite.playAnimationLooped(animations.run)
        }
    }


    class Animations(atlas: Atlas) {
        val run = atlas.getSpriteAnimation("heroRun", 150.milliseconds)
        val idle = atlas.getSpriteAnimation("heroIdle", 450.milliseconds)
    }

}