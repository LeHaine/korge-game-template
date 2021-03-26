package com.lehaine.pixelheist.entity

import com.lehaine.lib.getByPrefix
import com.lehaine.pixelheist.Assets
import com.lehaine.pixelheist.Entity
import com.lehaine.pixelheist.GameLevel
import com.lehaine.pixelheist.World
import com.soywiz.korge.view.Container
import com.soywiz.korge.view.ViewDslMarker
import com.soywiz.korge.view.addTo


inline fun Container.item(
    data: World.EntityItem,
    assets: Assets,
    level: GameLevel,
    callback: @ViewDslMarker Item.() -> Unit = {}
): Item = Item(data, assets, level).addTo(this, callback)

class Item(
    data: World.EntityItem, assets: Assets, level: GameLevel,
) : Entity(data.cx, data.cy, assets, level, data.pivotX.toDouble(), data.pivotY.toDouble()) {

    init {
        when (data.type) {
            World.ItemType.Ruby -> sprite.bitmap = assets.tiles.getByPrefix("ruby")
        }
    }
}