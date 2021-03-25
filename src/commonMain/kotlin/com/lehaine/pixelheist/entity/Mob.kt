package com.lehaine.pixelheist.entity

import com.lehaine.lib.registerState
import com.lehaine.pixelheist.Assets
import com.lehaine.pixelheist.Entity
import com.lehaine.pixelheist.GameLevel
import com.lehaine.pixelheist.World
import com.soywiz.korge.view.Container
import com.soywiz.korge.view.ViewDslMarker
import com.soywiz.korge.view.addTo
import kotlin.math.abs


inline fun Container.mob(
    data: World.EntityMob,
    assets: Assets,
    level: GameLevel,
    callback: @ViewDslMarker Mob.() -> Unit = {}
): Mob = Mob(data, assets, level).addTo(this, callback)

class Mob(
    data: World.EntityMob, assets: Assets, level: GameLevel,
) : Entity(data.cx, data.cy, level, data.pivotX.toDouble(), data.pivotY.toDouble()) {

    val runSpeed = 0.03

    init {
        sprite.apply {
            registerState(assets.mobRun) { abs(dx) >= 0.01 }
            registerState(assets.mobIdle) { true }
        }
    }

}