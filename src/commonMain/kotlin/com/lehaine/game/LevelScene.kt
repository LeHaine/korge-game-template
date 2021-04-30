package com.lehaine.game

import GameModule
import com.lehaine.game.entity.debugger
import com.lehaine.kiwi.korge.container
import com.lehaine.kiwi.korge.view.Layers
import com.lehaine.kiwi.korge.view.cameraContainer
import com.lehaine.kiwi.korge.view.layers
import com.lehaine.kiwi.korge.view.ldtk.ldtkMapView
import com.lehaine.kiwi.korge.view.ldtk.toLDtkLevel
import com.soywiz.kds.iterators.fastForEach
import com.soywiz.klock.milliseconds
import com.soywiz.korev.Key
import com.soywiz.korge.input.keys
import com.soywiz.korge.scene.Scene
import com.soywiz.korge.view.*
import com.soywiz.korge.view.fast.*
import com.soywiz.korio.async.launchImmediately
import com.soywiz.korma.geom.Rectangle


class LevelScene(private val world: World, private val levelIdx: Int = 0) : Scene() {

    override suspend fun Container.sceneInit() {
        val worldLevel = world.allLevels[levelIdx]
        val ldtkLevel = worldLevel.toLDtkLevel()
        val gameLevel = GameLevel(worldLevel)

        lateinit var fx: Fx
        lateinit var content: Layers

        cameraContainer(
            GameModule.size.width.toDouble(), GameModule.size.height.toDouble(),
            deadZone = 10,
            viewBounds = Rectangle(0, 0, worldLevel.pxWidth, worldLevel.pxHeight),
            clampToViewBounds = true,
            clip = true
        ) {
            content = layers {
                ldtkMapView(ldtkLevel, LAYER_BG)

                container(LAYER_MAIN) EntityContainer@{
                    name = "EntityContainer"

                    // instantiate and add entities to game level list here
                }
            }
        }.apply {
            // follow newly created entity or do something with camera
        }.also {
            gameLevel._camera = it
        }

        fx = Fx(gameLevel, content).also { gameLevel._fx = it }
        addUpdater { dt ->
            val tmod = if (dt == 0.milliseconds) 0.0 else (dt / 16.666666.milliseconds)
            fx.update(dt)
            gameLevel.entities.fastForEach {
                it.tmod = tmod
                it.update(dt)
            }
            gameLevel.entities.fastForEach {
                it.postUpdate(dt)
            }

        }

        keys {
            down(Key.ESCAPE) {
                stage?.views?.debugViews = false
                stage?.gameWindow?.run {
                    debug = false
                    close()
                }
            }
            justDown(Key.F1) {
                if (gameLevel.debugger == null) {
                    content.apply { debugger(LAYER_FRONT, gameLevel) }
                }
            }
            down(Key.R) {
                launchImmediately {
                    sceneContainer.changeTo<LevelScene>(world, levelIdx)
                }
            }
        }
    }
}