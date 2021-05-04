package com.lehaine.game.entity

import com.lehaine.game.Game
import com.lehaine.game.GameInput
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
    game: Game, callback: Debugger.() -> Unit = {}
): Debugger = Debugger(
    game = game,
    position = DynamicComponentDefault(
        anchorX = 0.5,
        anchorY = 0.5,
    )
).apply { toPixelPosition(px, py) }.addTo(this).addToGame().also {
    game.debugger = it
}.also(callback)

inline fun Layers.debugger(
    layer: Int,
    game: Game,
    callback: Debugger.() -> Unit = {}
): Debugger = Debugger(
    game = game,
    position = DynamicComponentDefault(
        anchorX = 0.5,
        anchorY = 0.5,
    )
).addToLayer(this, layer).addToGame().also {
    game.debugger = it
}.also(callback)

class Debugger(
    override val game: Game,
    position: DynamicComponent
) : Entity(game, position),
    DynamicComponent by position {

    private val prevCamTarget = game.camera.following
    private val prevCameraZoom = game.camera.cameraZoom

    private val circle = Circle(4.0, fill = Colors.RED).apply {
        anchor(Anchor.MIDDLE_CENTER)
    }.addTo(container)

    private var initialized = false

    private val input = game.controller.createAccess("debugger", true)

    init {
        frictionX = 0.86
        frictionY = 0.86
        width = 8.0
        height = 8.0

        toPixelPosition(
            game.camera.cameraX, game.camera.cameraY
        )

        game.camera.follow(this)
    }

    private var moveSpeedX: Double = 0.0
    private var moveSpeedY: Double = 0.0

    override fun update(dt: TimeSpan) {
        super.update(dt)

        moveSpeedX = 0.0
        moveSpeedY = 0.0

        val speedMultiplier = if (input.keyDown(Key.LEFT_SHIFT)) 3 else 1
        val speed = 0.05 * speedMultiplier

        moveSpeedX = speed * input.strength(GameInput.Horizontal)
        moveSpeedY = speed * input.strength(GameInput.Vertical)

        if (input.keyDown(Key.PAGE_UP)) {
            game.camera.cameraZoom += 2 * dt.seconds
        }
        if (input.keyDown(Key.PAGE_DOWN)) {
            game.camera.cameraZoom -= 2 * dt.seconds
        }

        if (input.keyPressed(Key.F1) && initialized) {
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
        game.camera.follow(prevCamTarget)
        game.camera.cameraZoom = prevCameraZoom
        game.entities.remove(this)
        game.debugger = null
    }
}