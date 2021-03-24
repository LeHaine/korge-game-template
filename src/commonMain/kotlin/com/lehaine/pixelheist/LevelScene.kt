package com.lehaine.pixelheist

import GameModule
import com.lehaine.lib.fpsLabel
import com.lehaine.lib.ldtk.ldtkMapView
import com.lehaine.lib.ldtk.toLDtkLevel
import com.lehaine.pixelheist.entity.Hero
import com.lehaine.pixelheist.entity.hero
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


class LevelScene(val assets: Assets, val world: World, val levelIdx: Int = 0) : Scene() {

    override suspend fun Container.sceneInit() {
        val worldLevel = world.allLevels[levelIdx]
        val ldtkLevel = worldLevel.toLDtkLevel()
        val gameLevel = GameLevel(worldLevel)

        lateinit var hero: Hero
        cameraContainer(GameModule.size.width.toDouble(), GameModule.size.height.toDouble(), clip = true) {
            ldtkMapView(ldtkLevel)
            val playerData = worldLevel.layerEntities.allPlayer[0]
            hero = hero(
                playerData.cx,
                playerData.cy,
                assets,
                gameLevel
            ) {
                anchor(playerData.pivotX.toDouble(), playerData.pivotY.toDouble())
            }
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