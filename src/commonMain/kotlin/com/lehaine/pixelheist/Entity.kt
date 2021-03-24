package com.lehaine.pixelheist

import com.soywiz.klock.TimeSpan
import com.soywiz.klock.milliseconds
import com.soywiz.korge.view.Container
import com.soywiz.korge.view.addUpdater
import com.soywiz.korge.view.anchor
import com.soywiz.korge.view.sprite
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.pow

open class Entity(cx: Int, cy: Int, val level: GameLevel, anchorX: Double = 0.5, anchorY: Double = 1.0) :
    Container() {

    var cx = 0
    var cy = 0
    var xr = 0.5
    var yr = 1.0

    var dx = 0.0
    var dy = 0.0

    var frictX = 0.82
    var frictY = 0.82

    var dir = 1
        set(value) {
            field = when {
                value > 1 -> {
                    1
                }
                value < -1 -> {
                    -1
                }
                else -> {
                    value
                }
            }
        }

    var tmod = 1.0
        private set

    val sprite = sprite {
        smoothing = false
        this.anchorX = anchorX
        this.anchorY = anchorY
    }

    init {
        toGridPosition(cx, cy)

        addUpdater {
            if (stage == null) return@addUpdater
            update(it)
            postUpdate(it)
        }
    }

    fun anchor(ax: Double, ay: Double): Entity {
        sprite.anchor(ax, ay)
        return this
    }

    fun toGridPosition(cx: Int, cy: Int, xr: Double = 0.5, yr: Double = 1.0) {
        this.cx = cx
        this.cy = cy
        this.xr = xr
        this.yr = yr
    }

    protected open fun update(dt: TimeSpan) {
        tmod = if (dt == 0.milliseconds) 0.0 else (dt / 16.666666.milliseconds)

        performXSteps(tmod)
    }

    protected open fun postUpdate(dt: TimeSpan) {
        x = (cx + xr) * GRID_SIZE
        y = (cy + yr) * GRID_SIZE
        sprite.scaleX = dir.toDouble()
    }

    private fun performXSteps(tmod: Double) {
        var steps = ceil(abs(dx * tmod))
        val step = dx * tmod / steps
        while (steps > 0) {
            xr += step

            performXCollisionCheck()
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

        dx *= frictX.pow(tmod)
        if (abs(dx) <= 0.0005 * tmod) {
            dx = 0.0
        }
    }

    private fun performXCollisionCheck() {
        if (level.hasCollision(cx + 1, cy) && xr >= 0.7) {
            xr = 0.7
            dx *= 0.5.pow(tmod)
            //  onCollision(1, 0);
        }

        if (level.hasCollision(cx - 1, cy) && xr <= 0.3) {
            xr = 0.3
            dx *= 0.5.pow(tmod)
            //  onCollision(-1, 0);
        }
    }

}