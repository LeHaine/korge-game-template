package com.lehaine.pixelheist.entity

import com.lehaine.lib.cd
import com.lehaine.lib.registerState
import com.lehaine.pixelheist.*
import com.soywiz.klock.TimeSpan
import com.soywiz.klock.milliseconds
import com.soywiz.klock.seconds
import com.soywiz.korev.Key
import com.soywiz.korge.view.ViewDslMarker
import kotlin.math.abs

inline fun EntityContainer.hero(
    data: World.EntityHero,
    assets: Assets,
    level: GameLevel,
    callback: @ViewDslMarker Hero.() -> Unit = {}
): Hero = Hero(data, assets, level).addEntityTo(this, callback)

class Hero(data: World.EntityHero, assets: Assets, level: GameLevel, anchorX: Double = 0.5, anchorY: Double = 1.0) :
    Entity(data.cx, data.cy, level, data.pivotX.toDouble(), data.pivotY.toDouble()) {

    val runSpeed = 0.03

    init {
        sprite.apply {
            registerState(assets.heroRun) { abs(dx) >= 0.01 }
            registerState(assets.heroIdle) { true }
        }
    }

    override fun update(dt: TimeSpan) {
        super.update(dt)
        if (onGround) {
            cd("onGroundRecently", 150.milliseconds)
            cd("airControl", 10.seconds)
        }
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

}