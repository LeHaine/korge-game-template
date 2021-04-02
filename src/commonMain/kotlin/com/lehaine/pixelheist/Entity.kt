package com.lehaine.pixelheist

import com.lehaine.lib.component.UpdatableComponent
import com.lehaine.lib.cooldown
import com.soywiz.kds.iterators.fastForEach
import com.soywiz.klock.TimeSpan
import com.soywiz.korge.view.Container
import com.soywiz.korge.view.addTo
import com.soywiz.korge.view.addUpdater
import com.soywiz.korge.view.collidesWithShape
import com.soywiz.korio.lang.Closeable
import kotlin.collections.set

open class Entity(val container: Container) : UpdatableComponent {
    var destroyed = false

    private var onDestroyedCallback: ((Entity) -> Unit)? = null

    private var initEntityCollisionChecks = false
    private var collisionFilter: () -> List<Entity> = { emptyList() }
        set(value) {
            field = value
            addEntityCollisionChecks()
        }

    val input get() = container.stage?.views?.input!!

    init {
//        container.hitShape {
//            rect(width * 0.5, height * 0.5, width, height)
//        }
    }

    override var tmod: Double = 1.0

    override fun update(dt: TimeSpan) {
    }

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
            collisionFilter().fastForEach {
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

val Entity.cooldown get() = container.cooldown
val Entity.cd get() = cooldown

fun Entity.cooldown(name: String, time: TimeSpan, callback: () -> Unit = {}): Closeable =
    cooldown.timeout(name, time, callback)

fun Entity.cd(name: String, time: TimeSpan, callback: () -> Unit = {}): Closeable =
    cooldown(name, time, callback)
