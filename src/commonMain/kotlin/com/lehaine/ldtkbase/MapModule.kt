package com.lehaine.ldtkbase

import com.soywiz.korge.scene.Module
import com.soywiz.korim.color.Colors
import com.soywiz.korinject.AsyncInjector
import com.soywiz.korma.geom.Size
import com.soywiz.korma.geom.SizeInt

object MapModule : Module() {
    override val mainScene = LevelScene::class

    override val windowSize: SizeInt = SizeInt(Size(1920, 1080))
    override val size: SizeInt = SizeInt(Size(480, 270))
    override val bgcolor = Colors["#2b2b2b"]

    override suspend fun AsyncInjector.configure() {
        mapInstance(World().apply { loadAsync() })
        mapInstance(0) // load first level
        mapPrototype { LevelScene(get(), get()) }
    }
}