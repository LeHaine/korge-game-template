package com.lehaine.game

import com.soywiz.korim.atlas.Atlas
import com.soywiz.korim.atlas.readAtlas
import com.soywiz.korim.font.Font
import com.soywiz.korim.font.TtfFont
import com.soywiz.korio.file.std.resourcesVfs

object Assets {

    lateinit var tiles: Atlas

    lateinit var pixelFont: Font

    suspend fun init() {
        tiles = resourcesVfs["tiles.atlas.json"].readAtlas()
        pixelFont = TtfFont(resourcesVfs["m5x7.ttf"].readAll())

        // define animations and other assets here
    }
}