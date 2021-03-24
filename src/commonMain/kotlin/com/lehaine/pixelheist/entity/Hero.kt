package com.lehaine.pixelheist.entity

import com.lehaine.lib.cd
import com.lehaine.pixelheist.Entity
import com.lehaine.pixelheist.GameLevel
import com.soywiz.klock.TimeSpan
import com.soywiz.klock.milliseconds
import com.soywiz.klock.seconds
import com.soywiz.korev.Key
import com.soywiz.korge.view.Container
import com.soywiz.korge.view.ViewDslMarker
import com.soywiz.korge.view.addTo
import com.soywiz.korge.view.getSpriteAnimation
import com.soywiz.korim.atlas.Atlas
import kotlin.math.pow

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
        if (onGround) {
            cd("onGroundRecently", 150.milliseconds)
            cd("airControl", 10.seconds)
        }
        val input = stage?.views?.input!!
        if (input.keys[Key.D]) {
            dx += runSpeed
            dir = 1
        }
        if (input.keys[Key.A]) {
            dx -= runSpeed
            dir = -1
        }

        if (input.keys.justPressed(Key.SPACE) && onGround) {
            dy = -0.35
            cd("jumpForce", 100.milliseconds)
            cd("jumpExtra", 100.milliseconds)
        } else if (input.keys.pressing(Key.SPACE) && cd.has("jumpExtra")) {
            dy -= 0.04.pow(tmod)
        }

        if (cd.has("jumpForce") && input.keys.pressing(Key.SPACE)) {
            println(cd.ratio("jumpForce"))
            dy -= 0.05 * cd.ratio("jumpForce") * tmod
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