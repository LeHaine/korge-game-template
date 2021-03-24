package com.lehaine.lib.ldtk

import com.lehaine.ldtk.*
import com.soywiz.kds.iterators.fastForEachReverse
import com.soywiz.korge.view.*
import com.soywiz.korim.color.Colors

inline fun Container.ldtkMapView(
    ldtkLevel: LDtkLevel,
    renderIntGridLayer: Boolean = false,
    debugEntities: Boolean = false,
    callback: LDtkMapView.() -> Unit = {}
) = LDtkMapView(ldtkLevel, renderIntGridLayer, debugEntities).addTo(this, callback)


class LDtkMapView(
    val ldtkLevel: LDtkLevel,
    val renderIntGridLayers: Boolean = false,
    val debugEntities: Boolean = false
) : Container() {

    init {
        val level = ldtkLevel.level
        val bgImage = ldtkLevel.bgImage
        val tileSets = ldtkLevel.tileSets

        require(level.isLoaded()) { "Level is not loaded! Please make sure level is loaded before creating an LDtkMapView" }

        if (bgImage != null) {
            image(texture = bgImage) {
                ldtkLevel.level.bgImageInfos?.let {
                    xy(it.topLeftX, it.topLeftY)
                    scale(it.scaleX, it.scaleY)
                }
            }
        }
        level.allUntypedLayers.fastForEachReverse { layer ->
            val view: View = when (layer) {
                is LayerTiles -> ldtkLayer(layer, tileSets[layer.tileset.json.uid])
                is LayerAutoLayer -> ldtkLayer(layer, tileSets[layer.tileset.json.uid])
                is LayerIntGridAutoLayer -> ldtkLayer(layer, tileSets[layer.tileset.json.uid])
                is LayerIntGrid -> {
                    if (renderIntGridLayers) {
                        ldtkLayer(layer, null)
                    }
                    dummyView()
                }
                is LayerEntities -> {
                    if (debugEntities) {
                        layer.entities.forEach { entity ->
                            level.project.defs?.let { defs ->
                                defs.entities.find { it.uid == entity.json.defUid }?.let {
                                    solidRect(entity.width, entity.height, Colors[it.color])
                                        .xy(
                                            entity.pixelX - entity.width * it.pivotX,
                                            entity.pixelY - entity.height * it.pivotY
                                        )
                                }
                            }
                        }
                    }
                    dummyView()
                }
                else -> dummyView()
            }
            view.visible(true)
                .name(layer.identifier.takeIf { it.isNotEmpty() })
                .xy(layer.pxTotalOffsetX, layer.pxTotalOffsetY)
        }
    }
}