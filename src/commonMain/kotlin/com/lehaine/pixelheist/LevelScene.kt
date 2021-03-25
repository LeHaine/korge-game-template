package com.lehaine.pixelheist

import GameModule
import com.lehaine.lib.fpsLabel
import com.lehaine.lib.ldtk.ldtkMapView
import com.lehaine.lib.ldtk.toLDtkLevel
import com.lehaine.pixelheist.entity.Hero
import com.lehaine.pixelheist.entity.hero
import com.lehaine.pixelheist.entity.mob
import com.soywiz.kds.iterators.fastForEach
import com.soywiz.korge.scene.Scene
import com.soywiz.korge.view.Container
import com.soywiz.korge.view.alignLeftToLeftOf
import com.soywiz.korge.view.alignTopToTopOf
import com.soywiz.korge.view.camera.cameraContainer


class LevelScene(val assets: Assets, val world: World, val levelIdx: Int = 0) : Scene() {

    override suspend fun Container.sceneInit() {
        val worldLevel = world.allLevels[levelIdx]
        val ldtkLevel = worldLevel.toLDtkLevel()
        val gameLevel = GameLevel(worldLevel)

        lateinit var hero: Hero
        cameraContainer(GameModule.size.width.toDouble(), GameModule.size.height.toDouble(), clip = true) {
            ldtkMapView(ldtkLevel)

            entityContainer {
                worldLevel.layerEntities.allMob.fastForEach {
                    mob(it, assets, gameLevel)
                }

                hero = hero(
                    worldLevel.layerEntities.allHero[0],
                    assets,
                    gameLevel
                )
            }
        }.follow(hero)


        fpsLabel {
            alignTopToTopOf(this)
            alignLeftToLeftOf(this)
        }
    }
}