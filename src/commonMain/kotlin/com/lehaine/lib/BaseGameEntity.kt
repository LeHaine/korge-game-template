package com.lehaine.lib

import com.lehaine.lib.component.GameLevelComponent
import com.lehaine.lib.component.GridPositionComponent
import com.lehaine.lib.component.UpdatableComponent
import com.lehaine.lib.component.ext.castRayTo
import com.soywiz.kds.iterators.fastForEach
import com.soywiz.klock.TimeSpan
import com.soywiz.korge.view.*
import com.soywiz.korio.lang.Closeable
import com.soywiz.korma.geom.vector.VectorBuilder
import com.soywiz.korma.geom.vector.rect

abstract class BaseGameEntity(open val level: GameLevelComponent<*>, val container: Container) :
    UpdatableComponent {
    var destroyed = false

    var onDestroyedCallback: ((BaseGameEntity) -> Unit)? = null

    val input get() = container.stage!!.views.input

    override var tmod: Double = 1.0

    override fun update(dt: TimeSpan) {}

    override fun postUpdate(dt: TimeSpan) {}

    fun destroy() {
        destroyed = true
        container.removeFromParent()
        onDestroyedCallback?.invoke(this)
    }

    fun onDestroy(action: (BaseGameEntity) -> Unit) {
        onDestroyedCallback = action
    }

    open fun onCollisionEnter(entity: BaseGameEntity) {}

    open fun onCollisionUpdate(entity: BaseGameEntity) {}

    open fun onCollisionExit(entity: BaseGameEntity) {}
}

fun <T : BaseGameEntity> T.addTo(parent: Container): T {
    container.addTo(parent)
    return this
}

fun <T> T.castRayTo(position: GridPositionComponent) where T : BaseGameEntity, T : GridPositionComponent =
    castRayTo(position, canRayPass)

val <T> T.canRayPass: (Int, Int) -> Boolean where T : BaseGameEntity, T : GridPositionComponent
    get() = { cx, cy ->
        !level.hasCollision(cx, cy) || this.cx == cx && this.cy == cy
    }

fun <T> T.sync() where T : BaseGameEntity, T : GridPositionComponent {
    container.x = px
    container.y = py
}

inline fun <T> T.addShape(crossinline block: (VectorBuilder.() -> Unit)) where T : BaseGameEntity, T : GridPositionComponent {
    container.hitShape(block)
}

fun <T> T.addRectShape() where T : BaseGameEntity, T : GridPositionComponent {
    addShape { rect(width * 0.5, height * 0.5, width, height) }
}

fun <T> T.addCollision() where T : BaseGameEntity, T : GridPositionComponent {
    val collisionState = mutableMapOf<BaseGameEntity, Boolean>()
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

val BaseGameEntity.cooldown get() = container.cooldown
val BaseGameEntity.cd get() = cooldown

fun BaseGameEntity.cooldown(name: String, time: TimeSpan, callback: () -> Unit = {}): Closeable =
    cooldown.timeout(name, time, callback)

fun BaseGameEntity.cd(name: String, time: TimeSpan, callback: () -> Unit = {}): Closeable =
    cooldown(name, time, callback)