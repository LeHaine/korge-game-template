package com.lehaine.pixelheist.entity

import com.lehaine.lib.component.PlatformerDynamicComponent
import com.lehaine.lib.component.PlatformerDynamicComponentDefault
import com.lehaine.lib.component.SpriteComponent
import com.lehaine.lib.getByPrefix
import com.lehaine.pixelheist.*
import com.lehaine.pixelheist.component.GameLevelComponent
import com.soywiz.klock.TimeSpan
import com.soywiz.korge.view.Container
import com.soywiz.korim.color.Colors


inline fun Container.item(
    data: World.EntityItem,
    level: GameLevelComponent<LevelMark>,
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
            data.pivotY.toDouble()
        ), level, SpriteComponent(container, data.pivotX.toDouble(), data.pivotY.toDouble()), container
    ).addTo(this).also(callback)
}

class Item(
    data: World.EntityItem,
    private val platformerDynamic: PlatformerDynamicComponent,
    level: GameLevelComponent<LevelMark>,
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
    }

    fun teleport() {
        fx.itemTeleported(px, py, Colors.RED)
        destroy()
    }

    override fun update(dt: TimeSpan) {
        super.update(dt)
        updateGridPosition(tmod)
        updateStretchAndScale()
    }

    override fun postUpdate(dt: TimeSpan) {
        super.postUpdate(dt)
        sync()
    }
}