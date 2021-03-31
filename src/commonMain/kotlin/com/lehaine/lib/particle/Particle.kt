package com.lehaine.lib.particle

import com.soywiz.klock.TimeSpan
import com.soywiz.korge.view.fast.FastSprite
import com.soywiz.korim.bitmap.BmpSlice

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
}