package com.lehaine.pixelheist.entity

import com.lehaine.kiwi.component.*
import com.lehaine.pixelheist.*
import com.lehaine.pixelheist.component.PixelGameLevelComponent
import com.soywiz.klock.TimeSpan
import com.soywiz.korge.view.Container


inline fun Container.portal(
    data: World.EntityPortal,
    level: PixelGameLevelComponent<LevelMark>,
    callback: Portal.() -> Unit = {}
): Portal {
    val container = Container()
    return Portal(
        GridPositionComponent(
            data.cx,
            data.cy,
            data.pivotX.toDouble(),
            data.pivotY.toDouble(),
            gridCellSize = GRID_SIZE
        ), level, SpriteComponent(container, data.pivotX.toDouble(), data.pivotY.toDouble()), container
    ).addTo(this).also(callback)
}

class Portal(
    private val gridPositionComponent: GridPositionComponent,
    level: PixelGameLevelComponent<LevelMark>,
    private val spriteComponent: SpriteComponent,
    container: Container
) : Entity(level, container),
    GridPositionComponent by gridPositionComponent,
    SpriteComponent by spriteComponent {

    init {
        sprite.playAnimationLooped(Assets.portalIdle)
        sync()
        addRectShape()
        addCollision()
    }

    override fun onCollisionEnter(entity: BaseGameEntity) {
        super.onCollisionEnter(entity)
        if (entity is Item) {
            entity.teleport()
        }
    }

    override fun update(dt: TimeSpan) {
        super.update(dt)
        updateGridPosition(tmod)
    }

    override fun postUpdate(dt: TimeSpan) {
        super.postUpdate(dt)
        sync()
        updateSprite()
    }
}