package com.lehaine.pixelheist.component

import com.lehaine.lib.component.GridPositionComponent
import com.soywiz.kds.iterators.fastForEach
import com.soywiz.korge.view.Container
import com.soywiz.korge.view.View
import com.soywiz.korge.view.addUpdater
import com.soywiz.korge.view.collidesWithShape

class Entity(private val position: GridPositionComponent) : Container(),
    GridPositionComponent by position {

    override var width: Double
        get() = position.width
        set(value) {
            position.height = height
        }
    override var height: Double
        get() = position.height
        set(value) {
            position.width = value
        }

    var destroyed = false

    private var onDestroyedCallback: ((Entity) -> Unit)? = null

    private var initEntityCollisionChecks = false
    private var collisionFilter: () -> List<Entity> = { emptyList() }
        set(value) {
            field = value
            addEntityCollisionChecks()
        }

    fun update(tmod: Double) {
        position.updateComponent(tmod)
    }

    fun destroy() {
        destroyed = true
        removeFromParent()
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

        val collisionState = mutableMapOf<View, Boolean>()
        addUpdater {
            collisionFilter().fastForEach {
                if (this != it) {
                    if (this.collidesWithShape(it)) {
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