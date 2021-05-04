package com.lehaine.game

import GameModule
import com.lehaine.game.entity.Debugger
import com.lehaine.game.entity.debugger
import com.lehaine.kiwi.component.Entity
import com.lehaine.kiwi.component.GameComponent
import com.lehaine.kiwi.korge.InputController
import com.lehaine.kiwi.korge.addFixedInterpUpdater
import com.lehaine.kiwi.korge.container
import com.lehaine.kiwi.korge.view.CameraContainer
import com.lehaine.kiwi.korge.view.Layers
import com.lehaine.kiwi.korge.view.cameraContainer
import com.lehaine.kiwi.korge.view.layers
import com.lehaine.kiwi.korge.view.ldtk.ldtkMapView
import com.lehaine.kiwi.korge.view.ldtk.toLDtkLevel
import com.soywiz.kds.iterators.fastForEach
import com.soywiz.klock.timesPerSecond
import com.soywiz.korev.GameButton
import com.soywiz.korev.Key
import com.soywiz.korge.input.keys
import com.soywiz.korge.scene.Scene
import com.soywiz.korge.view.*
import com.soywiz.korge.view.fast.*
import com.soywiz.korio.async.launchImmediately
import com.soywiz.korma.geom.Rectangle


class Game(private val world: World, private val levelIdx: Int = 0) : Scene(), GameComponent {

    lateinit var camera: CameraContainer
    lateinit var fx: Fx
    override lateinit var level: GameLevel
    var debugger: Debugger? = null

    override val entities: ArrayList<Entity> = arrayListOf()
    override val staticEntities: ArrayList<Entity> = arrayListOf()

    lateinit var controller: InputController<GameInput>
    lateinit var content: Layers

    override var fixedProgressionRatio: Double = 1.0

    override suspend fun Container.sceneInit() {
        controller = InputController(views)
        createControllerBindings()

        val worldLevel = world.allLevels[levelIdx]
        val ldtkLevel = worldLevel.toLDtkLevel()
        level = GameLevel(worldLevel)

        camera = cameraContainer(
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
        }

        fx = Fx(level, content)
        addUpdater { dt ->
            fx.update(dt)
            entities.fastForEach {
                it.update(dt)
            }
            entities.fastForEach {
                it.postUpdate(dt)
            }
        }


        addFixedInterpUpdater(30.timesPerSecond,
            interpolate = { ratio ->
                fixedProgressionRatio = ratio
            },
            updatable = {
                entities.fastForEach {
                    it.fixedUpdate()
                }
            }
        )
        keys {
            down(Key.ESCAPE) {
                stage?.views?.debugViews = false
                stage?.gameWindow?.run {
                    debug = false
                    close()
                }
            }
            justDown(Key.F1) {
                if (debugger == null) {
                    content.apply {
                        debugger(LAYER_FRONT, this@Game)
                    }
                }
            }
            down(Key.R) {
                launchImmediately {
                    sceneContainer.changeTo<Game>(world, levelIdx)
                }
            }
        }
    }

    private fun createControllerBindings() {
        controller.addAxis(
            GameInput.Horizontal,
            positiveKeys = listOf(Key.D, Key.RIGHT),
            positiveButtons = listOf(GameButton.LX),
            negativeKeys = listOf(Key.A, Key.LEFT),
            negativeButtons = listOf(GameButton.LX)
        )

        controller.addAxis(
            GameInput.Vertical,
            positiveKeys = listOf(Key.S, Key.DOWN),
            positiveButtons = listOf(GameButton.LY),
            negativeKeys = listOf(Key.W, Key.UP),
            negativeButtons = listOf(GameButton.LY)
        )
    }
}