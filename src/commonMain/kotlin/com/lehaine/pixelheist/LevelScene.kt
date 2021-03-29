package com.lehaine.pixelheist

import GameModule
import com.lehaine.lib.cameraContainer
import com.lehaine.lib.ldtk.ldtkMapView
import com.lehaine.lib.ldtk.toLDtkLevel
import com.lehaine.pixelheist.entity.*
import com.soywiz.kds.iterators.fastForEach
import com.soywiz.klock.milliseconds
import com.soywiz.korev.Key
import com.soywiz.korge.input.keys
import com.soywiz.korge.scene.Scene
import com.soywiz.korge.view.Container
import com.soywiz.korge.view.container
import com.soywiz.korio.async.launchImmediately
import com.soywiz.korma.geom.Rectangle


class LevelScene(private val world: World, private val levelIdx: Int = 0) : Scene() {

    override suspend fun Container.sceneInit() {
        val worldLevel = world.allLevels[levelIdx]
        val ldtkLevel = worldLevel.toLDtkLevel()
        val gameLevel = GameLevel(worldLevel)

        lateinit var hero: Hero

        val entities = arrayListOf<Entity>()
        val mobs = arrayListOf<Mob>()
        val items = arrayListOf<Item>()

        fun removeEntity(entity: Entity) {
            entities.remove(entity)
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
                        entities += mob(entityMob, gameLevel) {
                            collisionFilter { entities }
                            onDestroy { removeEntity(it) }
                        }.also {
                            entities += it
                            mobs += it
                        }
                    }
                }

                container ItemContainer@{
                    name = "ItemContainer"
                    worldLevel.layerEntities.allItem.fastForEach { entityItem ->
                        item(entityItem, gameLevel) {
                            onDestroy { removeEntity(it) }
                        }.also {
                            entities += it
                            items += it
                        }
                    }
                }

                container PortalContainer@{
                    name = "PortalContainer"
                    worldLevel.layerEntities.allPortal.fastForEach { entityPortal ->
                        entities += portal(entityPortal, gameLevel) {
                            collisionFilter { items }
                            onDestroy { removeEntity(it) }
                        }
                    }
                }

                hero = hero(
                    worldLevel.layerEntities.allHero[0],
                    gameLevel
                ) {
                    collisionFilter { entities }
                }.also {
                    entities += it
                    gameLevel._hero = it
                }
            }
        }.apply {
            follow(hero, true)
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

            down(Key.K) {
                cam.shake(250.milliseconds)
            }

            down(Key.Z) {
                cam.cameraZoom += 0.5
            }
            down(Key.X) {
                cam.cameraZoom -= 0.5

            }
        }
    }
}