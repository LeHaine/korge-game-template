package com.lehaine.lib.component

import com.soywiz.korma.geom.Rectangle
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.pow

interface DynamicComponent : GridPositionComponent {
    var gravityX: Double
    var gravityY: Double
    var gravityMultiplier: Double
    var deltaX: Double
    var deltaY: Double
    var frictionX: Double
    var frictionY: Double

    fun checkXCollision(tmod: Double)
    fun checkYCollision(tmod: Double)

    companion object {
        operator fun invoke(): GridPositionComponent {
            return DynamicComponentDefault()
        }
    }
}

open class DynamicComponentDefault : DynamicComponent {
    override var gravityX: Double = 0.0
    override var gravityY: Double = 0.0
    override var gravityMultiplier: Double = 1.0
    override var deltaX: Double = 0.0
    override var deltaY: Double = 0.0
    override var frictionX: Double = 0.82
    override var frictionY: Double = 0.82

    override var cx: Int = 0
    override var cy: Int = 0
    override var xr: Double = 0.5
    override var yr: Double = 0.5

    override var gridCellSize: Int = 16
    override var width: Int = 16
    override var height: Int = 16

    override var anchorX: Double = 0.5
    override var anchorY: Double = 0.5

    override val px get() = (cx + xr) * gridCellSize
    override val py get() = (cy + yr) * gridCellSize
    override val centerX get() = px + (0.5 - anchorX) * gridCellSize
    override val centerY get() = py + (0.5 - anchorY) * gridCellSize

    private var _bounds = Rectangle()
    override val bounds: Rectangle
        get() = _bounds.apply {
            top = py - anchorY * width
            right = px + (1 - px) * width
            bottom = py + (1 - anchorY) * height
            left = px - anchorX * height
        }

    override fun updateComponent(tmod: Double) {
        updateX(tmod)
        updateY(tmod)
    }

    override fun updateX(tmod: Double) {
        var steps = ceil(abs(deltaX * tmod))
        val step = deltaX * tmod / steps
        while (steps > 0) {
            xr += step

            checkXCollision(tmod)
            while (xr > 1) {
                xr--
                cx++
            }
            while (xr < 0) {
                xr++
                cx--
            }
            steps--
        }

        deltaX *= frictionX.pow(tmod)
        if (abs(deltaX) <= 0.0005 * tmod) {
            deltaX = 0.0
        }
    }

    open override fun checkXCollision(tmod: Double) {}

    protected open fun calculateDeltaXGravity(tmod: Double): Double {
        return 0.0
    }

    override fun updateY(tmod: Double) {
        deltaY += calculateDeltaYGravity(tmod)
        var steps = ceil(abs(deltaY * tmod))
        val step = deltaY * tmod / steps
        while (steps > 0) {
            yr += step
            checkYCollision(tmod)
            while (yr > 1) {
                yr--
                cy++
            }
            while (yr < 0) {
                yr++
                cy--
            }
            steps--
        }
        deltaY *= frictionY.pow(tmod)
        if (abs(deltaY) <= 0.0005 * tmod) {
            deltaY = 0.0
        }
    }

    protected open fun calculateDeltaYGravity(tmod: Double): Double {
        return 0.0
    }

    open override fun checkYCollision(tmod: Double) {}
}


class PlatformerDynamicComponent(private val levelComponent: LevelComponent) :
    DynamicComponentDefault() {
    var hasGravity = true
    private val gravityPulling get() = !onGround && hasGravity
    val onGround get() = deltaX == 0.0 && levelComponent.hasCollision(cx, cy + 1)

    override fun checkXCollision(tmod: Double) {
        if (levelComponent.hasCollision(cx + 1, cy) && xr >= 0.7) {
            xr = 0.7
            deltaX *= 0.5.pow(tmod)
        }

        if (levelComponent.hasCollision(cx - 1, cy) && xr <= 0.3) {
            xr = 0.3
            deltaY *= 0.5.pow(tmod)
        }
    }

    override fun calculateDeltaXGravity(tmod: Double): Double {
        return 0.0
    }

    override fun checkYCollision(tmod: Double) {
        val heightCoordDiff = floor(height / gridCellSize.toDouble())
        if (levelComponent.hasCollision(cx, cy - 1) && yr <= heightCoordDiff) {
            yr = heightCoordDiff
            deltaX = 0.0
        }
        if (levelComponent.hasCollision(cx, cy + 1) && yr >= 1) {
            deltaY = 0.0
            yr = 1.0
        }
    }

    override fun calculateDeltaYGravity(tmod: Double): Double {
        return if (gravityPulling) {
            gravityMultiplier * gravityY * tmod
        } else {
            0.0
        }
    }
}