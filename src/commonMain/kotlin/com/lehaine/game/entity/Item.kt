package com.lehaine.game.entity

import com.lehaine.kiwi.component.*
import com.lehaine.kiwi.getByPrefix
import com.lehaine.game.*
import com.lehaine.game.component.PixelGameLevelComponent
import com.soywiz.klock.TimeSpan
import com.soywiz.korge.view.Container
import com.soywiz.korim.color.Colors


inline fun Container.item(
    data: World.EntityItem,
    level: PixelGameLevelComponent<LevelMark>,
    callback: Item.() -> Unit = {}
): Item {
    val container = Container()
    return Item(
        data,
        PlatformerDynamicComponentDefault(
            level,
            data.cx,
            data.cy,
            data.pivotX.toDouble(),
            data.pivotY.toDouble(),
            gridCellSize = GRID_SIZE
        ), level, SpriteComponent(container, data.pivotX.toDouble(), data.pivotY.toDouble()), container
    ).addTo(this).also(callback)
}

class Item(
    data: World.EntityItem,
    private val platformerDynamic: PlatformerDynamicComponent,
    level: PixelGameLevelComponent<LevelMark>,
    private val spriteComponent: SpriteComponent,
    container: Container
) : Entity(level, container),
    PlatformerDynamicComponent by platformerDynamic,
    SpriteComponent by spriteComponent {

    init {
        when (data.type) {
            World.ItemType.Ruby -> sprite.bitmap = Assets.tiles.getByPrefix("ruby")
        }
        sync()
        addRectShape()
        addCollision()
    }

    fun teleport() {
        fx.itemTeleported(px, py, Colors.RED)
        destroy()
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