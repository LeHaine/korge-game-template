package com.lehaine.lib.ldtk

import com.lehaine.ldtk.*
import com.soywiz.kds.iterators.fastForEach
import com.soywiz.korge.view.tiles.TileSet
import com.soywiz.korim.bitmap.sliceWithSize
import com.soywiz.korim.format.readBitmap
import com.soywiz.korio.file.std.resourcesVfs


suspend fun Level.toLDtkLevel(): LDtkLevel {
    if (!isLoaded()) {
        loadAsync()
    }
    val tileSets = loadTileSets()
    val bgImage = if (hasBgImage) resourcesVfs[bgImageInfos!!.relFilePath].readBitmap() else null
    val slice = bgImage?.let {
        val cropRect = bgImageInfos!!.cropRect
        it.sliceWithSize(cropRect.x.toInt(), cropRect.y.toInt(), cropRect.w.toInt(), cropRect.h.toInt())
    }
    return LDtkLevel(this, tileSets, slice)
}

suspend fun Level.loadTileSets(): Map<Int, TileSet> {
    if (!isLoaded()) {
        loadAsync()
    }

    val tileSets = mutableMapOf<Int, TileSet>()

    allUntypedLayers.fastForEach { layer ->
        when (layer.type) {
            LayerType.IntGrid -> {
                if (layer is LayerIntGridAutoLayer) {
                    if (!tileSets.containsKey(layer.tileset.json.uid)) {
                        tileSets[layer.tileset.json.uid] = layer.tileset.toTileSet()
                    }
                }
            }
            LayerType.Tiles -> {
                layer as LayerTiles
                if (!tileSets.containsKey(layer.tileset.json.uid)) {
                    tileSets[layer.tileset.json.uid] = layer.tileset.toTileSet()
                }
            }
            LayerType.AutoLayer -> {
                layer as LayerAutoLayer
                if (!tileSets.containsKey(layer.tileset.json.uid)) {
                    tileSets[layer.tileset.json.uid] = layer.tileset.toTileSet()
                }
            }
            else -> {
                // nothing
            }
        }
    }
    return tileSets
}

suspend fun Tileset.toTileSet(): TileSet {
    val bmp = resourcesVfs[relPath].readBitmap().toBMP32IfRequired()
    val columns = bmp.width / tileGridSize
    val rows = bmp.height / tileGridSize
    val slices = TileSet.extractBitmaps(bmp, tileGridSize, tileGridSize, columns, columns * rows, 0, 0)
    return TileSet.fromBitmaps(tileGridSize, tileGridSize, slices, 1)
}