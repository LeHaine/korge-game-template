package com.lehaine.pixelheist.component

import com.lehaine.lib.component.GridPositionComponent
import com.soywiz.kds.iterators.fastForEach
import com.soywiz.korge.view.*
import com.soywiz.korma.geom.vector.rect
import kotlin.collections.set

open class Entity(private val position: GridPositionComponent) : Container(),
    GridPositionComponent by position {

    var destroyed = false

    private var onDestroyedCallback: ((Entity) -> Unit)? = null

    private var initEntityCollisionChecks = false
    private var collisionFilter: () -> List<Entity> = { emptyList() }
        set(value) {
            field = value
            addEntityCollisionChecks()
        }

    init {
//        position(cx, cy)
//        sync()

        hitShape {
            rect(enWidth * 0.5, enHeight * 0.5, enWidth, enHeight)
        }

    }

    fun update(tmod: Double) {
        position.updateComponent(tmod)
    }

    fun postUpdate(tmod: Double) {
        syncPosition()
    }

    private fun syncPosition() {
        x = px
        y = py
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