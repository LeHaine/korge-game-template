package com.lehaine.pixelheist.entity

import com.lehaine.lib.getByPrefix
import com.lehaine.pixelheist.*
import com.soywiz.korge.view.ViewDslMarker


inline fun EntityContainer.item(
    data: World.EntityItem,
    assets: Assets,
    level: GameLevel,
    callback: @ViewDslMarker Item.() -> Unit = {}
): Item = Item(data, assets, level).addEntityTo(this, callback)

class Item(
    data: World.EntityItem, assets: Assets, level: GameLevel,
) : Entity(data.cx, data.cy, assets, level, data.pivotX.toDouble(), data.pivotY.toDouble()) {

    init {
        when(data.type) {
            World.ItemType.Ruby -> sprite.bitmap = assets.tiles.getByPrefix("ruby")
        }
    }
}