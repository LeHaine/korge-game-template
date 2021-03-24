package com.lehaine.pixelheist.entity

import com.lehaine.lib.cd
import com.lehaine.lib.registerState
import com.lehaine.pixelheist.Assets
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
import kotlin.math.abs

inline fun Container.hero(
    cx: Int = 0,
    cy: Int = 0,
    assets: Assets,
    level: GameLevel,
    callback: @ViewDslMarker Hero.() -> Unit = {}
): Hero = Hero(cx, cy, assets, level).addTo(this, callback)

class Hero(cx: Int, cy: Int, assets: Assets, level: GameLevel, anchorX: Double = 0.5, anchorY: Double = 1.0) :
    Entity(cx, cy, level, anchorX, anchorY) {

    val animations = Animations(assets)
    val runSpeed = 0.03

    init {
        sprite.apply {
            registerState(animations.run) { abs(dx) >= 0.01 }
            registerState(animations.idle) { true }
        }
    }

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
            dy -= 0.04 * tmod
        }

        if (cd.has("jumpForce") && input.keys.pressing(Key.SPACE)) {
            dy -= 0.05 * cd.ratio("jumpForce") * tmod
        }
    }

    class Animations(assets: Assets) {
        val run = assets.tiles.getSpriteAnimation("heroRun", 150.milliseconds)
        val idle = assets.tiles.getSpriteAnimation("heroIdle", 450.milliseconds)
    }

}