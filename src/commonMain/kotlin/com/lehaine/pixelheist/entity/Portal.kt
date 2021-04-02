package com.lehaine.pixelheist.entity

import com.lehaine.pixelheist.Assets
import com.lehaine.pixelheist.GameLevel
import com.lehaine.pixelheist.World
import com.soywiz.korge.view.Container
import com.soywiz.korge.view.ViewDslMarker

inline fun Container.portal(
    data: World.EntityPortal,
    level: GameLevel,
    callback: @ViewDslMarker Portal.() -> Unit = {}
): Portal = Portal(data, level).addTo(this, callback)

class Portal(
    data: World.EntityPortal, level: GameLevel,
) : Entity(data.cx, data.cy, level, data.pivotX.toDouble(), data.pivotY.toDouble()) {

    init {
        sprite.playAnimationLooped(Assets.portalIdle)
    }

    override fun onEntityCollision(entity: Entity) {
        super.onEntityCollision(entity)
        if (entity is Item) {
            entity.teleport()
        }
    }
}