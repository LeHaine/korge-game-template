package com.lehaine.game.entity

import com.lehaine.game.LevelMark
import com.lehaine.game.component.GenericGameLevelComponent
import com.lehaine.game.follow
import com.lehaine.kiwi.component.*
import com.lehaine.kiwi.component.ext.toPixelPosition
import com.lehaine.kiwi.korge.view.Layers
import com.soywiz.klock.TimeSpan
import com.soywiz.korev.Key
import com.soywiz.korge.view.Circle
import com.soywiz.korge.view.Container
import com.soywiz.korge.view.addTo
import com.soywiz.korge.view.anchor
import com.soywiz.korim.color.Colors
import com.soywiz.korma.geom.Anchor

inline fun Container.debugger(
    level: GenericGameLevelComponent<LevelMark>, callback: Debugger.() -> Unit = {}
): Debugger = Debugger(
    level = level,
    position = DynamicComponentDefault(
        anchorX = 0.5,
        anchorY = 0.5,
    )
).apply { toPixelPosition(px, py) }.addTo(this).also {
    level.entities += it
    level.debugger = it
}.also(callback)

inline fun Layers.debugger(
    layer: Int,
    level: GenericGameLevelComponent<LevelMark>,
    callback: Debugger.() -> Unit = {}
): Debugger = Debugger(
    level = level,
    position = DynamicComponentDefault(
        anchorX = 0.5,
        anchorY = 0.5,
    )
).addToLayer(this, layer).also {
    level.entities += it
    level.debugger = it
}.also(callback)

class Debugger(
    val level: GenericGameLevelComponent<LevelMark>,
    position: DynamicComponent
) : Entity(position),
    DynamicComponent by position {

    private val input get() = container.stage?.views?.input!!

    private val prevCamTarget = level.camera.following
    private val prevCameraZoom = level.camera.cameraZoom

    private val circle = Circle(4.0, fill = Colors.RED).apply {
        anchor(Anchor.MIDDLE_CENTER)
    }.addTo(container)

    private var initialized = false

    init {
        frictionX = 0.86
        frictionY = 0.86
        width = 8.0
        height = 8.0

        toPixelPosition(
            level.camera.cameraX, level.camera.cameraY
        )

        level.camera.follow(this)
    }

    private var moveSpeedX: Double = 0.0
    private var moveSpeedY: Double = 0.0

    override fun update(dt: TimeSpan) {
        super.update(dt)

        moveSpeedX = 0.0
        moveSpeedY = 0.0

        val speedMultiplier = if (input.keys.pressing(Key.LEFT_SHIFT)) 3 else 1
        val speed = 0.05 * speedMultiplier

        if (input.keys.pressing(Key.A)) {
            moveSpeedX = -speed
        }
        if (input.keys.pressing(Key.D)) {
            moveSpeedX = speed
        }
        if (input.keys.pressing(Key.W)) {
            moveSpeedY = -speed
        }
        if (input.keys.pressing(Key.S)) {
            moveSpeedY = speed
        }
        moveSpeedX *= speedMultiplier
        moveSpeedY *= speedMultiplier

        if (input.keys.pressing(Key.PAGE_UP)) {
            level.camera.cameraZoom += 2 * dt.seconds
        }
        if (input.keys.pressing(Key.PAGE_DOWN)) {
            level.camera.cameraZoom -= 2 * dt.seconds
        }

        if (input.keys.justPressed(Key.F1) && initialized) {
            destroy()
        }

        if (level.hasCollision(cx, cy)) {
            circle.fill = Colors.YELLOW
        } else {
            circle.fill = Colors.RED
        }

        initialized = true
    }

    override fun fixedUpdate() {
        super.fixedUpdate()
        velocityX += moveSpeedX
        velocityY += moveSpeedY
    }

    override fun destroy() {
        super.destroy()
        level.camera.follow(prevCamTarget)
        level.camera.cameraZoom = prevCameraZoom
        level.entities.remove(this)
        level.debugger = null
    }
}