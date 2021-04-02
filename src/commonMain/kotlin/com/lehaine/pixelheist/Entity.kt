package com.lehaine.pixelheist

import com.lehaine.lib.component.GridPositionComponent
import com.lehaine.lib.component.UpdatableComponent
import com.lehaine.lib.component.ext.castRayTo
import com.lehaine.lib.cooldown
import com.lehaine.pixelheist.component.GameLevelComponent
import com.soywiz.kds.iterators.fastForEach
import com.soywiz.klock.TimeSpan
import com.soywiz.korge.view.Container
import com.soywiz.korge.view.addTo
import com.soywiz.korge.view.addUpdater
import com.soywiz.korge.view.collidesWithShape
import com.soywiz.korio.lang.Closeable
import kotlin.collections.set

open class Entity(private val level: GameLevelComponent<LevelMark>, val container: Container) : UpdatableComponent,
    GameLevelComponent<LevelMark> by level {
    var destroyed = false

    private var onDestroyedCallback: ((Entity) -> Unit)? = null

    private var initEntityCollisionChecks = false

    val input get() = container.stage?.views?.input!!

    init {
//        if (this is GridPositionComponent) {
//            container.hitShape {
//                rect(width * 0.5, height * 0.5, width, height)
//            }
//            addEntityCollisionChecks()
//        }
    }

    override var tmod: Double = 1.0

    override fun update(dt: TimeSpan) {}
    override fun postUpdate(dt: TimeSpan) {}

    open fun destroy() {
        destroyed = true
        container.removeFromParent()
        onDestroyedCallback?.invoke(this)
    }

    fun onDestroy(action: (Entity) -> Unit) {
        onDestroyedCallback = action
    }

    protected open fun onEntityCollision(entity: Entity) {}

    protected open fun onEntityColliding(entity: Entity) {}

    protected open fun onEntityCollisionExit(entity: Entity) {}

    private fun addEntityCollisionChecks() {
        if (initEntityCollisionChecks) return

        val collisionState = mutableMapOf<Entity, Boolean>()
        container.addUpdater {
            entities.fastForEach {
                if (this != it) {
                    if (this.collidesWithShape(it.container)) {
                        if (collisionState[it] == true) {
                            onEntityColliding(it)
                        } else {
                            // we only need to call it once
                            onEntityCollision(it)
                            collisionState[it] = true
                        }
                    } else if (collisionState[it] == true) {
                        onEntityCollisionExit(it)
                        collisionState[it] = false
                    }
                }
            }
        }
    }

}


fun <T : Entity> T.addTo(parent: Container): T {
    container.addTo(parent)
    return this
}

fun <T> T.castRayTo(position: GridPositionComponent) where T : Entity, T : GridPositionComponent =
    castRayTo(position, canRayPass)

val <T> T.canRayPass: (Int, Int) -> Boolean where T : Entity, T : GridPositionComponent
    get() = { cx, cy ->
        !hasCollision(cx, cy) || this.cx == cx && this.cy == cy
    }

fun <T> T.sync() where T : Entity, T : GridPositionComponent {
    container.x = px
    container.y = py
}

val Entity.cooldown get() = container.cooldown
val Entity.cd get() = cooldown

fun Entity.cooldown(name: String, time: TimeSpan, callback: () -> Unit = {}): Closeable =
    cooldown.timeout(name, time, callback)

fun Entity.cd(name: String, time: TimeSpan, callback: () -> Unit = {}): Closeable =
    cooldown(name, time, callback)
