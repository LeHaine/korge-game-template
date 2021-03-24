package com.lehaine.pixelheist

import com.soywiz.klock.TimeSpan
import com.soywiz.klock.milliseconds
import com.soywiz.korev.Key
import com.soywiz.korge.view.Container
import com.soywiz.korge.view.ViewDslMarker
import com.soywiz.korge.view.addTo
import com.soywiz.korge.view.getSpriteAnimation
import com.soywiz.korim.atlas.Atlas

inline fun Container.hero(
    cx: Int = 0,
    cy: Int = 0,
    atlas: Atlas,
    level: GameLevel,
    callback: @ViewDslMarker Hero.() -> Unit = {}
): Hero = Hero(cx, cy, atlas, level).addTo(this, callback)

class Hero(cx: Int, cy: Int, atlas: Atlas, level: GameLevel, anchorX: Double = 0.5, anchorY: Double = 1.0) :
    Entity(cx, cy, level, anchorX, anchorY) {


    val animations = Animations(atlas)
    val runSpeed = 0.03

    override fun update(dt: TimeSpan) {
        super.update(dt)
        val input = stage?.views?.input!!
        if (input.keys[Key.D]) {
            dx += runSpeed
            dir = 1
        }
        if (input.keys[Key.A]) {
            dx -= runSpeed
            dir = -1
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