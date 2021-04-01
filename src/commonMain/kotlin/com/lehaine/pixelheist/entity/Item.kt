package com.lehaine.pixelheist.entity

import com.lehaine.lib.getByPrefix
import com.lehaine.pixelheist.Entity
import com.lehaine.pixelheist.GameLevel
import com.lehaine.pixelheist.World
import com.soywiz.korge.view.Container
import com.soywiz.korge.view.ViewDslMarker
import com.soywiz.korge.view.addTo
import com.soywiz.korim.color.Colors


inline fun Container.item(
    data: World.EntityItem,
    level: GameLevel,
    callback: @ViewDslMarker Item.() -> Unit = {}
): Item = Item(data, level).addTo(this, callback)

class Item(
    data: World.EntityItem, level: GameLevel,
) : Entity(data.cx, data.cy, level, data.pivotX.toDouble(), data.pivotY.toDouble()) {

    init {
        when (data.type) {
            World.ItemType.Ruby -> sprite.bitmap = assets.tiles.getByPrefix("ruby")
        }
    }

    fun teleport() {
        fx.itemTeleported(x, y, Colors.RED)
        destroy()
    }
}