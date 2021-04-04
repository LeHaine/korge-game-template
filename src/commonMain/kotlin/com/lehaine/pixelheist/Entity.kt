package com.lehaine.pixelheist

import com.lehaine.lib.component.GridPositionComponent
import com.lehaine.lib.component.UpdatableComponent
import com.lehaine.lib.component.ext.castRayTo
import com.lehaine.lib.cooldown
import com.lehaine.pixelheist.component.GameLevelComponent
import com.soywiz.kds.iterators.fastForEach
import com.soywiz.klock.TimeSpan
import com.soywiz.korge.view.*
import com.soywiz.korio.lang.Closeable
import com.soywiz.korma.geom.vector.VectorBuilder
import com.soywiz.korma.geom.vector.rect
import kotlin.collections.set

open class Entity(val level: GameLevelComponent<LevelMark>, val container: Container) : UpdatableComponent {
    var destroyed = false

    private var onDestroyedCallback: ((Entity) -> Unit)? = null

    val fx get() = level.fx
    val hero get() = level.hero

    val input get() = container.stage!!.views.input

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

    open fun onCollisionEnter(entity: Entity) {}

    open fun onCollisionUpdate(entity: Entity) {}

    open fun onCollisionExit(entity: Entity) {}
}

fun <T : Entity> T.addTo(parent: Container): T {
    container.addTo(parent)
    return this
}

fun <T> T.castRayTo(position: GridPositionComponent) where T : Entity, T : GridPositionComponent =
    castRayTo(position, canRayPass)

val <T> T.canRayPass: (Int, Int) -> Boolean where T : Entity, T : GridPositionComponent
    get() = { cx, cy ->
        !level.hasCollision(cx, cy) || this.cx == cx && this.cy == cy
    }

fun <T> T.sync() where T : Entity, T : GridPositionComponent {
    container.x = px
    container.y = py
}

inline fun <T> T.addShape(crossinline block: (VectorBuilder.() -> Unit)) where T : Entity, T : GridPositionComponent {
    container.hitShape(block)
}

fun <T> T.addRectShape() where T : Entity, T : GridPositionComponent {
    addShape { rect(width * 0.5, height * 0.5, width, height) }
}

fun <T> T.addCollision() where T : Entity, T : GridPositionComponent {
    val collisionState = mutableMapOf<Entity, Boolean>()
    container.addUpdater {
        level.entities.fastForEach {
            if (this@addCollision != it) {
                if (collidesWithShape(it.container)) {
                    if (collisionState[it] == true) {
                        onCollisionUpdate(it)
                    } else {
                        // we only need to call it once
                        onCollisionEnter(it)
                        collisionState[it] = true
                    }
                } else if (collisionState[it] == true) {
                    onCollisionExit(it)
                    collisionState[it] = false
                }
            }
        }
    }
}

val Entity.cooldown get() = container.cooldown
val Entity.cd get() = cooldown

fun Entity.cooldown(name: String, time: TimeSpan, callback: () -> Unit = {}): Closeable =
    cooldown.timeout(name, time, callback)

fun Entity.cd(name: String, time: TimeSpan, callback: () -> Unit = {}): Closeable =
    cooldown(name, time, callback)
