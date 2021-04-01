package com.lehaine.pixelheist.entity

import com.lehaine.lib.cd
import com.lehaine.lib.random
import com.lehaine.lib.stateMachine
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
import com.soywiz.korma.geom.degrees
import com.soywiz.korma.geom.radians

inline fun Container.hero(
    data: World.EntityHero,
    level: GameLevel,
    callback: @ViewDslMarker Hero.() -> Unit = {}
): Hero = Hero(data, level).addTo(this, callback)

class Hero(data: World.EntityHero, level: GameLevel) :
    Entity(data.cx, data.cy, level, data.pivotX.toDouble(), data.pivotY.toDouble()) {

    private val moveSpeed = 0.03

    private val runningLeft get() = input.keys[Key.A]
    private val runningRight get() = input.keys[Key.D]
    private val jumping
        get() = input.keys.justPressed(Key.SPACE) && cd.has("onGroundRecently")
    private val jumpingExtra get() = input.keys.pressing(Key.SPACE) && cd.has("jumpExtra")
    private val jumpingForce get() = cd.has("jumpForce") && input.keys.pressing(Key.SPACE)

    private var heldItem: Item? = null
    private var lastMobJumpedOn: Mob? = null

    private sealed class HeroMovementState {
        object Idle : HeroMovementState()
        object Run : HeroMovementState()
        object Jump : HeroMovementState()
        object JumpExtra : HeroMovementState()
        object Fall : HeroMovementState()
        object JumpOnMob : HeroMovementState()
    }

    private val movementFsm = stateMachine<HeroMovementState> {
        state(HeroMovementState.JumpOnMob) {
            reason { lastMobJumpedOn != null }
            begin {
                lastMobJumpedOn?.stretchX = 2.0
                lastMobJumpedOn?.let {
                    fx.bloodSplatter(it.px, it.py - it.enHeight)
                }
                level.camera.bump(y = 0.7)
                lastMobJumpedOn?.stun()
            }
            update {
                dy -= 0.7
                lastMobJumpedOn = null
            }
        }
        state(HeroMovementState.Jump) {
            reason { jumping }
            update {
                move()
                jump()
            }
        }
        state(HeroMovementState.JumpExtra) {
            reason { jumpingExtra || jumpingForce }
            update {
                move()
                if (jumpingExtra) jumpExtra()
                if (jumpingForce) jumpForce()
            }
        }
        state(HeroMovementState.Fall) {
            reason { dy > 0.01 }
            update { move() }
        }
        state(HeroMovementState.Run) {
            reason { runningLeft || runningRight }
            begin { sprite.playAnimationLooped(assets.heroRun) }
            update { move() }
        }
        state(HeroMovementState.Idle) {
            reason { true }
            begin { sprite.playAnimationLooped(assets.heroIdle) }
        }
    }

    private sealed class HeroItemState {
        object ThrowItem : HeroItemState()
        object HoldItem : HeroItemState()
        object NoItem : HeroItemState()
    }

    private val itemFsm = stateMachine<HeroItemState> {

        state(HeroItemState.ThrowItem) {
            reason { input.keys.justPressed(Key.E) && heldItem != null }
            update {
                heldItem?.let {
                    it.dx = (1.2..1.5).random() * dir + dx
                    it.dy = -(0.3..0.35).random() + dy
                }
                heldItem = null
                cd(ITEM_THREW, 250.milliseconds)
            }

        }
        state(HeroItemState.HoldItem) {
            reason { heldItem != null }
            update {
                heldItem?.toGridPosition(cx, cy - 1, xr, yr)
            }
        }

        state(HeroItemState.NoItem) {
            reason { true }
        }
    }

    companion object {
        /** Cooldown flags **/
        private const val ON_GROUND_RECENTLY = "onGroundRecently"
        private const val AIR_CONTROL = "airControl"
        private const val JUMP_FORCE = "jumpForce"
        private const val JUMP_EXTRA = "jumpExtra"
        private const val ITEM_THREW = "itemThrew"
    }


    override fun onEntityCollision(entity: Entity) {
        super.onEntityCollision(entity)
        if (lastMobJumpedOn == null && entity is Mob) {
            val angle = angleTo(entity).radians
            if (angle.degrees in (0.0..180.0) && dy > 0) {
                lastMobJumpedOn = entity
            }
        }
    }

    override fun onEntityColliding(entity: Entity) {
        super.onEntityColliding(entity)
        if (heldItem == null && !cd.has(ITEM_THREW) && entity is Item) {
            heldItem = entity
        }
    }

    override fun update(dt: TimeSpan) {
        super.update(dt)
        if (onGround) {
            cd(ON_GROUND_RECENTLY, 150.milliseconds)
            cd(AIR_CONTROL, 10.seconds)
        }
        movementFsm.update(dt)
        itemFsm.update(dt)

//        debugLabel.text =
//            "${movementFsm.currentState!!.type::class.simpleName}\n${itemFsm.currentState!!.type::class.simpleName}"
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