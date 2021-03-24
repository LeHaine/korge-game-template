package com.lehaine.ldtkbase

import com.lehaine.ldtkbase.ldtk.ldtkMapView
import com.lehaine.ldtkbase.ldtk.toLDtkLevel
import com.soywiz.korev.Key
import com.soywiz.korge.input.keys
import com.soywiz.korge.scene.Scene
import com.soywiz.korge.view.Container
import com.soywiz.korge.view.alignLeftToLeftOf
import com.soywiz.korge.view.alignTopToTopOf
import com.soywiz.korge.view.camera.cameraContainer
import com.soywiz.korim.atlas.readAtlas
import com.soywiz.korio.async.launchImmediately
import com.soywiz.korio.file.std.resourcesVfs


class LevelScene(val world: World, val levelIdx: Int = 0) : Scene() {

    override suspend fun Container.sceneInit() {
        val worldLevel = world.allLevels[levelIdx]
        val ldtkLevel = worldLevel.toLDtkLevel()
        val atlas = resourcesVfs["tiles.atlas.json"].readAtlas()

        lateinit var hero: Hero
        cameraContainer(480.0, 270.0, clip = true) {
            ldtkMapView(ldtkLevel)
            val playerData = worldLevel.layerEntities.allPlayer[0]
            hero = hero(
                playerData.pixelX.toDouble(),
                playerData.pixelY.toDouble(),
                atlas
            )
        }.follow(hero)


        fpsLabel {
            alignTopToTopOf(this)
            alignLeftToLeftOf(this)
        }

        keys {
            down(Key.N) {
                launchImmediately {
                    if (levelIdx < world.allUntypedLevels.lastIndex) {
                        sceneContainer.changeTo<LevelScene>(world, levelIdx + 1)
                    }
                }
            }
            down(Key.B) {
                launchImmediately {
                    if (levelIdx > 0) {
                        sceneContainer.changeTo<LevelScene>(world, levelIdx - 1)
                    }
                }
            }
            down(Key.ESCAPE) {
                stage?.gameWindow?.close()
            }
        }
    }
}