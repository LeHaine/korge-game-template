package com.lehaine.pixelheist.entity

import com.lehaine.lib.cd
import com.lehaine.lib.stateMachine
import com.lehaine.pixelheist.Assets
import com.lehaine.pixelheist.Entity
import com.lehaine.pixelheist.GameLevel
import com.lehaine.pixelheist.World
import com.soywiz.klock.TimeSpan
import com.soywiz.klock.milliseconds
import com.soywiz.klock.seconds
import com.soywiz.korev.Key
import com.soywiz.korge.view.Container
import com.soywiz.korge.view.ViewDslMarker
import com.soywiz.korge.view.addTo

inline fun Container.hero(
    data: World.EntityHero,
    assets: Assets,
    level: GameLevel,
    callback: @ViewDslMarker Hero.() -> Unit = {}
): Hero = Hero(data, assets, level).addTo(this, callback)

class Hero(data: World.EntityHero, assets: Assets, level: GameLevel, anchorX: Double = 0.5, anchorY: Double = 1.0) :
    Entity(data.cx, data.cy, assets, level, data.pivotX.toDouble(), data.pivotY.toDouble()) {

    private val moveSpeed = 0.03

    private val runningLeft get() = input.keys[Key.A]
    private val runningRight get() = input.keys[Key.D]
    private val jumping
        get() = input.keys.justPressed(Key.SPACE) && cd.has("onGroundRecently")
    private val jumpingExtra get() = input.keys.pressing(Key.SPACE) && cd.has("jumpExtra")
    private val jumpingForce get() = cd.has("jumpForce") && input.keys.pressing(Key.SPACE)

    private sealed class HeroState {
        object Idle : HeroState()
        object Run : HeroState()
        object Jump : HeroState()
        object JumpExtra : HeroState()
        object Fall : HeroState()
    }

    private val movementFsm = stateMachine<HeroState> {
        state(HeroState.Jump) {
            reason { jumping }
            update {
                move()
                jump()
            }
        }
        state(HeroState.JumpExtra) {
            reason { jumpingExtra || jumpingForce }
            update {
                move()
                if (jumpingExtra) jumpExtra()
                if (jumpingForce) jumpForce()
            }
        }
        state(HeroState.Fall) {
            reason { dy > 0.01 }
            update { move() }
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

    companion object {
        private const val ON_GROUND_RECENTLY = "onGroundRecently"
        private const val AIR_CONTROL = "airControl"
        private const val JUMP_FORCE = "jumpForce"
        private const val JUMP_EXTRA = "jumpExtra"
    }

    override fun onEntityCollision(entity: Entity) {
        super.onEntityCollision(entity)
        println("colliding with ${entity::class.simpleName}")
    }

    override fun onEntityCollisionExit(entity: Entity) {
        println("collision exit with ${entity::class.simpleName}")
    }

    override fun update(dt: TimeSpan) {
        super.update(dt)
        if (onGround) {
            cd(ON_GROUND_RECENTLY, 150.milliseconds)
            cd(AIR_CONTROL, 10.seconds)
        }
        movementFsm.update(dt)
    }

    private fun move() {
        if (runningRight) {
            dx += moveSpeed
            dir = 1
        }
        if (runningLeft) {
            dx -= moveSpeed
            dir = -1
        }
    }

    private fun jump() {
        dy = -0.35
        cd(JUMP_FORCE, 100.milliseconds)
        cd(JUMP_EXTRA, 100.milliseconds)
    }

    private fun jumpExtra() {
        dy -= 0.04 * tmod
    }

    private fun jumpForce() {
        dy -= 0.05 * cd.ratio(JUMP_FORCE) * tmod
    }

}