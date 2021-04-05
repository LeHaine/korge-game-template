package com.lehaine.game

import GameModule
import com.lehaine.kiwi.cameraContainer
import com.lehaine.kiwi.ldtk.ldtkMapView
import com.lehaine.kiwi.ldtk.toLDtkLevel
import com.lehaine.game.entity.*
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

        lateinit var hero: Hero
        lateinit var fx: Fx

        val mobs = arrayListOf<Mob>()
        val items = arrayListOf<Item>()

        fun removeEntity(entity: Entity) {
            gameLevel.entities.remove(entity)
            if (entity is Mob) {
                mobs.remove(entity)
            } else if (entity is Item) {
                items.remove(entity)
            }
        }

        val cam = cameraContainer(
            GameModule.size.width.toDouble(), GameModule.size.height.toDouble(),
            deadZone = 10,
            viewBounds = Rectangle(0, 0, worldLevel.pxWidth, worldLevel.pxHeight),
            clampToViewBounds = true,
            clip = true
        ) {
            ldtkMapView(ldtkLevel)

            container EntityContainer@{
                name = "EntityContainer"

                container MobContainer@{
                    name = "MobContainer"
                    worldLevel.layerEntities.allMob.fastForEach { entityMob ->
                        mob(entityMob, gameLevel) {
                            onDestroy { removeEntity(it as Entity) }
                        }.also {
                            gameLevel.entities += it
                            mobs += it
                        }
                    }
                }

                container ItemContainer@{
                    name = "ItemContainer"
                    worldLevel.layerEntities.allItem.fastForEach { entityItem ->
                        item(entityItem, gameLevel) {
                            onDestroy { removeEntity(it as Entity) }
                        }.also {
                            gameLevel.entities += it
                            items += it
                        }
                    }
                }

                container PortalContainer@{
                    name = "PortalContainer"
                    worldLevel.layerEntities.allPortal.fastForEach { entityPortal ->
                        portal(entityPortal, gameLevel) {
                            onDestroy { removeEntity(it as Entity) }
                        }.also {
                            gameLevel.entities += it

                        }
                    }
                }

                hero = hero(
                    worldLevel.layerEntities.allHero[0],
                    gameLevel
                ).also {
                    gameLevel.entities += it
                    gameLevel._hero = it
                }
            }

            val particleContainer = fastSpriteContainer(useRotation = true, smoothing = false)
            fx = Fx(gameLevel, particleContainer).also { gameLevel._fx = it }
        }.apply {
            follow(hero, true)
        }.also {
            gameLevel._camera = it
        }

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
            down(Key.R) {
                launchImmediately {
                    sceneContainer.changeTo<LevelScene>(world, levelIdx)
                }
            }

            down(Key.PAGE_UP) {
                cam.cameraZoom += 0.1
            }
            down(Key.PAGE_DOWN) {
                cam.cameraZoom -= 0.1
            }
        }
    }
}