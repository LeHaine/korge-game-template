package com.lehaine.pixelheist.entity

import com.lehaine.lib.registerState
import com.lehaine.pixelheist.*
import com.soywiz.korge.view.ViewDslMarker
import kotlin.math.abs


inline fun EntityContainer.mob(
    data: World.EntityMob,
    assets: Assets,
    level: GameLevel,
    callback: @ViewDslMarker Mob.() -> Unit = {}
): Mob = Mob(data, assets, level).addEntityTo(this, callback)

class Mob(
    data: World.EntityMob, assets: Assets, level: GameLevel,
) : Entity(data.cx, data.cy, assets, level, data.pivotX.toDouble(), data.pivotY.toDouble()) {

    val runSpeed = 0.03

    init {
        sprite.apply {
            registerState(assets.mobRun) { abs(dx) >= 0.01 }
            registerState(assets.mobIdle) { true }
        }
    }

}