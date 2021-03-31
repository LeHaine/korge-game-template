package com.lehaine.lib.particle

import com.soywiz.klock.TimeSpan
import com.soywiz.klock.seconds
import com.soywiz.korge.view.fast.FastSprite
import com.soywiz.korge.view.fast.x
import com.soywiz.korge.view.fast.y
import com.soywiz.korim.bitmap.BmpSlice
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class Particle(tex: BmpSlice) : FastSprite(tex) {
    var index: Int = 0
    var xDelta: Double = 0.0
    var yDelta: Double = 0.0

    var scaleDelta: Double = 0.0
    var scaleDeltaX: Double = 0.0
    var scaleDeltaY: Double = 0.0
    var scaleFriction: Double = 1.0
    var scaleMultiplier: Double = 1.0
    var scaleXMultiplier: Double = 1.0
    var scaleYMultiplier: Double = 1.0
    var rotationDelta: Double = 0.0
    var rotationFriction: Double = 1.0
    var friction
        get() = (frictionX + frictionY) * 0.5
        set(value) {
            frictionX = value
            frictionY = value
        }

    var frictionX: Double = 1.0
    var frictionY: Double = 1.0
    var gravityX: Double = 0.0
    var gravityY: Double = 0.0

    /**
     * The speed to fade out the particle after [remainingLife] is 0
     */
    var fadeOutSpeed: Double = 0.1

    /**
     * Total particle life
     */
    var life: TimeSpan = 1.seconds
        set(value) {
            field = value
            remainingLife = value
        }

    /**
     * Life remaining before being killed
     */
    var remainingLife: TimeSpan = TimeSpan.NIL

    /**
     * Time to delay the particle from starting updates
     */
    var delay: TimeSpan = TimeSpan.ZERO

    var killed = false

    val alive get() = remainingLife.milliseconds > 0

    var onStart: (() -> Unit)? = null
    var onUpdate: ((Particle) -> Unit)? = null
    var onKill: (() -> Unit)? = null

    var colorRdelta: Double = 0.0
    var colorGdelta: Double = 0.0
    var colorBdelta: Double = 0.0
    var alphaDelta: Double = 0.0

    var timeStamp: Double = 0.0

    var data0 = 0
    var data1 = 0
    var data2 = 0
    var data3 = 0

    fun moveAwayFrom(x: Double, y: Double, speed: Double) {
        val angle = atan2(y - this.y, x - this.x)
        xDelta = -cos(angle) * speed
        yDelta = -sin(angle) * speed
    }

    override fun toString(): String {
        return "Particle(index=$index, xDelta=$xDelta, yDelta=$yDelta, scaleDelta=$scaleDelta, scaleDeltaX=$scaleDeltaX, scaleDeltaY=$scaleDeltaY, scaleFriction=$scaleFriction, scaleMultiplier=$scaleMultiplier, scaleXMultiplier=$scaleXMultiplier, scaleYMultiplier=$scaleYMultiplier, rotationDelta=$rotationDelta, rotationFriction=$rotationFriction, frictionX=$frictionX, frictionY=$frictionY, gravityX=$gravityX, gravityY=$gravityY, fadeOutSpeed=$fadeOutSpeed, life=$life, remainingLife=$remainingLife, delay=$delay, killed=$killed, onStart=$onStart, onUpdate=$onUpdate, onKill=$onKill, colorRdelta=$colorRdelta, colorGdelta=$colorGdelta, colorBdelta=$colorBdelta, alphaDelta=$alphaDelta, timeStamp=$timeStamp, data0=$data0, data1=$data1, data2=$data2, data3=$data3)"
    }


}