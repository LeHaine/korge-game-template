package com.lehaine.lib.component

import com.soywiz.kds.iterators.fastForEach
import com.soywiz.korma.geom.vector.VectorPath

interface CollisionComponent<T> : Component {
    var shape: VectorPath
    var collisionEntities: List<T>
    var onCollisionEnter: ((T) -> Unit)?
    var onCollisionUpdate: ((T) -> Unit)?
    var onCollisionExit: ((T) -> Unit)?
    fun checkForCollision()
}

class CollisionComponentDefault<T : CollisionComponent<T>>(
    override var shape: VectorPath,
    override var collisionEntities: List<T>,
    override var onCollisionEnter: ((T) -> Unit)? = null,
    override var onCollisionExit: ((T) -> Unit)? = null,
    override var onCollisionUpdate: ((T) -> Unit)? = null,
) :
    CollisionComponent<T> {
    private val collisionState = mutableMapOf<T, Boolean>()



    override fun checkForCollision() {
        collisionEntities.fastForEach {
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


    private fun collidesWith(shape: VectorPath) {
        if  (VectorPath.intersects(leftPath, getGlobalMatrix(left, lmat), rightPath, getGlobalMatrix(right, rmat))) return true

    }


}