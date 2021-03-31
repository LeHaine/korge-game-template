package com.lehaine.lib.particle

import com.soywiz.klock.TimeSpan
import com.soywiz.korge.view.fast.*
import com.soywiz.korim.bitmap.BmpSlice
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class Particle(tex: BmpSlice) : FastSprite(tex) {
    var index: Int = 0
    var xDelta: Double = 0.0
    var yDelta: Double = 0.0

    var alphaDelta: Double = 0.0
    var scaleDelta: Double = 0.0
    var scaleDeltaX: Double = 0.0
    var scaleDeltaY: Double = 0.0
    var scaleFriction: Double = 0.0
    var scaleMultiplier: Double = 0.0
    var scaleXMultiplier: Double = 0.0
    var scaleYMultiplier: Double = 0.0
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
    var fadeOutSpeed: Double = 0.1
    var life: TimeSpan = TimeSpan.NIL
        set(value) {
            field = value
            remainingLife = value
        }
    var remainingLife: TimeSpan = TimeSpan.NIL

    var killed = false

    val alive get() = remainingLife.milliseconds > 0

    var onStart: (() -> Unit)? = null
    var onUpdate: ((Particle) -> Unit)? = null
    var onKill: (() -> Unit)? = null

    var colorR: Double = 1.0
    var colorG: Double = 1.0
    var colorB: Double = 1.0
    var colorA: Double = 1.0

    var colorRdelta: Double = 0.0
    var colorGdelta: Double = 0.0
    var colorBdelta: Double = 0.0
    var colorAdelta: Double = 0.0

    var timeStamp: Double = 0.0

    fun moveAwayFrom(x: Double, y: Double, speed: Double) {
        val angle = atan2(y - this.y, x - this.x)
        xDelta = -cos(angle) * speed
        yDelta = -sin(angle) * speed
    }

    override fun toString(): String {
        return "Particle(index=$index, x:${x}, y:${y}, xDelta=$xDelta, yDelta=$yDelta, alphaDelta=$alphaDelta, scaleX=${scaleX}, scaleY=${scaleY}, scaleDelta=$scaleDelta, scaleDeltaX=$scaleDeltaX, scaleDeltaY=$scaleDeltaY, scaleFriction=$scaleFriction, scaleMultiplier=$scaleMultiplier, scaleXMultiplier=$scaleXMultiplier, scaleYMultiplier=$scaleYMultiplier, rotationDelta=$rotationDelta, rotationFriction=$rotationFriction, frictionX=$frictionX, frictionY=$frictionY, gravityX=$gravityX, gravityY=$gravityY, fadeOutSpeed=$fadeOutSpeed, life=$life, remainingLife=$remainingLife, killed=$killed, onStart=$onStart, onUpdate=$onUpdate, onKill=$onKill, colorR=$colorR, colorG=$colorG, colorB=$colorB, colorA=$colorA, colorRdelta=$colorRdelta, colorGdelta=$colorGdelta, colorBdelta=$colorBdelta, colorAdelta=$colorAdelta, alpha=$alpha, timeStamp=$timeStamp)"
    }
}