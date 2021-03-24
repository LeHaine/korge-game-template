package com.lehaine.lib

import com.soywiz.klock.TimeSpan
import com.soywiz.korge.view.*
import kotlin.math.roundToInt


inline fun Container.fpsLabel(
    block: @ViewDslMarker Text.() -> Unit = {}
): FpsLabel = FpsLabel().addTo(this, block)

class FpsLabel : Text("FPS: ...") {

    var accumulator = 0

    init {
        smoothing = false
        addUpdater { dt -> update(dt) }
    }

    private fun update(dt: TimeSpan) {
        accumulator += dt.millisecondsInt
        if (accumulator > 200) {
            text = "FPS: ${(1 / dt.seconds).roundToInt()}"
            accumulator = 0
        }
    }
}