package com.lehaine.pixelheist

import com.soywiz.korim.atlas.Atlas
import com.soywiz.korim.atlas.readAtlas
import com.soywiz.korio.file.std.resourcesVfs

class Assets {

    lateinit var tiles: Atlas
        private set

    suspend fun init() {
        tiles = resourcesVfs["tiles.atlas.json"].readAtlas()
    }
}