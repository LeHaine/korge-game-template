package com.lehaine.pixelheist.entity

import com.lehaine.lib.cd
import com.lehaine.lib.stateMachine
import com.lehaine.pixelheist.*
import com.soywiz.klock.TimeSpan
import com.soywiz.klock.milliseconds
import com.soywiz.klock.seconds
import com.soywiz.korev.Key
import com.soywiz.korge.view.ViewDslMarker

inline fun EntityContainer.hero(
    data: World.EntityHero,
    assets: Assets,
    level: GameLevel,
    callback: @ViewDslMarker Hero.() -> Unit = {}
): Hero = Hero(data, assets, level).addEntityTo(this, callback)

class Hero(data: World.EntityHero, assets: Assets, level: GameLevel, anchorX: Double = 0.5, anchorY: Double = 1.0) :
    Entity(data.cx, data.cy, assets, level, data.pivotX.toDouble(), data.pivotY.toDouble()) {

    val runSpeed = 0.03

    private val runningLeft get() = input.keys[Key.A]
    private val runningRight get() = input.keys[Key.D]
    private val jumping
        get() = input.keys.justPressed(Key.SPACE) && cd.has("onGroundRecently")
    private val jumpingExtra get() = input.keys.pressing(Key.SPACE) && cd.has("jumpExtra")
    private val jumpingForce get() = cd.has("jumpForce") && input.keys.pressing(Key.SPACE)

    sealed class HeroState {
        object Idle : HeroState()
        object Run : HeroState()
        object Jump : HeroState()
        object Fall : HeroState()
    }

    private val movementFsm = stateMachine<HeroState> {
        state(HeroState.Fall) {
            reason { dy > 0.01 }
            update { move() }
        }
        state(HeroState.Jump) {
            reason { jumping || jumpingExtra || jumpingForce }
            update {
                move()
                jump()
            }
        }
        state(HeroState.Run) {
            reason { runningLeft || runningRight }
            begin { sprite.playAnimationLooped(assets.heroRun) }
            update { move() }
        }
        state(HeroState.Idle) {
            reason { true }
            begin { sprite.playAnimationLooped(assets.heroIdle) }
        }
        stateChanged {
            debugLabel.text = it::class.simpleName ?: ""
        }
    }

    override fun update(dt: TimeSpan) {
        super.update(dt)
        movementFsm.update(dt)

        if (onGround) {
            cd("onGroundRecently", 150.milliseconds)
            cd("airControl", 10.seconds)
        }
    }

    private fun move() {
        if (runningRight) {
            dx += runSpeed
            dir = 1
        }
        if (runningLeft) {
            dx -= runSpeed
            dir = -1
        }
    }

    private fun jump() {
        if (jumping) {
            dy = -0.35
            cd("jumpForce", 100.milliseconds)
            cd("jumpExtra", 100.milliseconds)
        } else if (jumpingExtra) {
            dy -= 0.04 * tmod
        }

        if (jumpingForce) {
            dy -= 0.05 * cd.ratio("jumpForce") * tmod
        }
    }
}