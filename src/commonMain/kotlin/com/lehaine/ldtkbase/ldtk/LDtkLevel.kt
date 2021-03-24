package com.lehaine.ldtkbase.ldtk

import com.lehaine.ldtk.Level
import com.soywiz.korge.view.tiles.TileSet
import com.soywiz.korim.bitmap.Bitmap
import com.soywiz.korim.bitmap.BitmapSlice

data class LDtkLevel(
    val level: Level,
    val tileSets: Map<Int, TileSet>,
    val bgImage: BitmapSlice<Bitmap>? = null,
    val bgImageInfo: Level.LevelBgImage? = null
)