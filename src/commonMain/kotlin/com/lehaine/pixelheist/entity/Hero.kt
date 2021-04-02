package com.lehaine.pixelheist.entity

import com.lehaine.lib.component.GridPositionComponent
import com.lehaine.lib.component.PlatformerDynamicComponent
import com.lehaine.lib.component.PlatformerDynamicComponentDefault
import com.lehaine.lib.component.SpriteComponent
import com.lehaine.lib.component.ext.angleTo
import com.lehaine.lib.component.ext.dirTo
import com.lehaine.lib.component.ext.toGridPosition
import com.lehaine.lib.random
import com.lehaine.lib.stateMachine
import com.lehaine.pixelheist.*
import com.lehaine.pixelheist.component.GameLevelComponent
import com.soywiz.klock.TimeSpan
import com.soywiz.klock.milliseconds
import com.soywiz.klock.seconds
import com.soywiz.korev.Key
import com.soywiz.korge.view.Container
import com.soywiz.korma.geom.degrees
import com.soywiz.korma.geom.radians

inline fun Container.hero(
    data: World.EntityHero,
    level: GameLevelComponent<LevelMark>,
    callback: Hero.() -> Unit = {}
): Hero {
    val container = Container()
    return Hero(
        PlatformerDynamicComponentDefault(
            level,
            data.cx,
            data.cy,
            data.pivotX.toDouble(),
            data.pivotY.toDouble()
        ), level, SpriteComponent(container, data.pivotX.toDouble(), data.pivotY.toDouble()), container
    ).addTo(this).also(callback)
}

class Hero(
    private val platformerDynamic: PlatformerDynamicComponent,
    private val level: GameLevelComponent<LevelMark>,
    private val spriteComponent: SpriteComponent,
    container: Container
) : Entity(level, container),
    PlatformerDynamicComponent by platformerDynamic,
    SpriteComponent by spriteComponent {

    private val moveSpeed = 0.03

    private val runningLeft get() = input.keys[Key.A]
    private val runningRight get() = input.keys[Key.D]
    private val jumping
        get() = input.keys.justPressed(Key.SPACE) && cd.has("onGroundRecently")
    private val jumpingExtra get() = input.keys.pressing(Key.SPACE) && cd.has("jumpExtra")
    private val jumpingForce get() = cd.has("jumpForce") && input.keys.pressing(Key.SPACE)

    private var forceDropItem = false
    private var heldItem: Item? = null
    private var lastMobJumpedOn: Mob? = null

    private sealed class HeroMovementState {
        object Idle : HeroMovementState()
        object Run : HeroMovementState()
        object Jump : HeroMovementState()
        object JumpExtra : HeroMovementState()
        object Fall : HeroMovementState()
        object JumpOnMob : HeroMovementState()
        object Stunned : HeroMovementState()
    }

    private val movementFsm = stateMachine<HeroMovementState> {
        state(HeroMovementState.Stunned) {
            reason { cd.has(STUNNED) }
        }
        state(HeroMovementState.JumpOnMob) {
            reason { lastMobJumpedOn != null }
            begin {
                level.camera.bump(y = 0.7)
                lastMobJumpedOn?.stun()
            }
            update {
                velocityY -= 0.7
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
            reason { velocityY > 0.01 }
            update { move() }
        }
        state(HeroMovementState.Run) {
            reason { runningLeft || runningRight }
            begin { sprite.playAnimationLooped(Assets.heroRun) }
            update { move() }
        }
        state(HeroMovementState.Idle) {
            reason { true }
            begin { sprite.playAnimationLooped(Assets.heroIdle) }
        }
    }

    private sealed class HeroItemState {
        object ThrowItem : HeroItemState()
        object HoldItem : HeroItemState()
        object DropItem : HeroItemState()
        object NoItem : HeroItemState()
    }

    private val itemFsm = stateMachine<HeroItemState> {
        state(HeroItemState.DropItem) {
            reason { heldItem != null && forceDropItem }
            begin {
                forceDropItem = false
                heldItem?.let {
                    it.velocityX = (0.6..0.75).random() * -dir
                    it.velocityY = -(0.15..0.2).random()
                }
                heldItem = null
                cd(ITEM_THREW, 250.milliseconds)
            }

        }
        state(HeroItemState.ThrowItem) {
            reason { input.keys.justPressed(Key.E) && heldItem != null }
            update {
                heldItem?.let {
                    it.velocityX = (1.2..1.5).random() * dir + velocityX
                    it.velocityY = -(0.3..0.35).random() + velocityY
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
        private const val STUNNED = "stunned"
        private const val MOB_JUMP_LOCK = "mobJumpLock"
        private const val HIT_IMMUNE = "hitImmune"
    }

    init {
        sync()
    }

    override fun onEntityCollision(entity: Entity) {
        super.onEntityCollision(entity)
        if (lastMobJumpedOn == null && !cd.has(MOB_JUMP_LOCK)
            && entity is Mob && entity.canStun
        ) {
            val angle = angleTo(entity).radians
            if (angle.degrees in (0.0..180.0) && velocityY > 0) {
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

        updateGridPosition(tmod)
        updateStretchAndScale()
        println("$cx,$cy --- $px,$py")
        println(movementFsm.currentState!!.type::class.simpleName)

//        debugLabel.text =
//            "${movementFsm.currentState!!.type::class.simpleName}\n${itemFsm.currentState!!.type::class.simpleName}"
    }


    override fun postUpdate(dt: TimeSpan) {
        super.postUpdate(dt)
        sync()
    }

    fun <T> hit(from: T) where T : Entity, T : GridPositionComponent {
        if (cd.has(HIT_IMMUNE)) return
        val hitDir = dirTo(from)
        velocityX = -hitDir * 0.25
        velocityY - 0.3
        forceDropItem = true
        cd(STUNNED, 500.milliseconds)
        cd(MOB_JUMP_LOCK, 500.milliseconds)
        cd(HIT_IMMUNE, 1.seconds)
    }

    private fun move() {
        if (runningRight) {
            velocityX += moveSpeed
            dir = 1
        }
        if (runningLeft) {
            velocityX -= moveSpeed
            dir = -1
        }
    }

    private fun jump() {
        velocityY = -0.35
        cd(JUMP_FORCE, 100.milliseconds)
        cd(JUMP_EXTRA, 100.milliseconds)
    }

    private fun jumpExtra() {
        velocityY -= 0.04 * tmod
    }

    private fun jumpForce() {
        velocityY -= 0.05 * cd.ratio(JUMP_FORCE) * tmod
    }

}