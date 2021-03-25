package com.lehaine.pixelheist

import com.soywiz.klock.milliseconds
import com.soywiz.korge.view.SpriteAnimation
import com.soywiz.korge.view.getSpriteAnimation
import com.soywiz.korim.atlas.Atlas
import com.soywiz.korim.atlas.readAtlas
import com.soywiz.korio.file.std.resourcesVfs

class Assets {

    lateinit var tiles: Atlas

    lateinit var heroRun: SpriteAnimation
    lateinit var heroIdle: SpriteAnimation

    lateinit var mobRun: SpriteAnimation
    lateinit var mobIdle: SpriteAnimation

    suspend fun init() {
        tiles = resourcesVfs["tiles.atlas.json"].readAtlas()

        heroRun = tiles.getSpriteAnimation("heroRun", 150.milliseconds)
        heroIdle = tiles.getSpriteAnimation("heroIdle", 450.milliseconds)

        mobRun = tiles.getSpriteAnimation("mobRun", 150.milliseconds)
        mobIdle = tiles.getSpriteAnimation("mobIdle", 450.milliseconds)
    }
}