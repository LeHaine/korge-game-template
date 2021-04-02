package com.lehaine.pixelheist.entity

import com.lehaine.lib.component.GridPositionComponent
import com.lehaine.lib.component.PlatformerDynamicComponent
import com.lehaine.lib.component.PlatformerDynamicComponentDefault
import com.lehaine.lib.component.SpriteComponent
import com.lehaine.lib.component.ext.dirTo
import com.lehaine.lib.component.ext.distGridTo
import com.lehaine.lib.getByPrefix
import com.lehaine.lib.stateMachine
import com.lehaine.pixelheist.*
import com.lehaine.pixelheist.component.GameLevelComponent
import com.soywiz.klock.TimeSpan
import com.soywiz.klock.milliseconds
import com.soywiz.klock.seconds
import com.soywiz.korge.view.Container


inline fun Container.mob(
    data: World.EntityMob,
    level: GameLevelComponent<LevelMark>,
    callback: Mob.() -> Unit = {}
): Mob {
    val container = Container()
    return Mob(
        PlatformerDynamicComponentDefault(
            level,
            data.cx,
            data.cy,
            data.pivotX.toDouble(),
            data.pivotY.toDouble()
        ), level, SpriteComponent(container, data.pivotX.toDouble(), data.pivotY.toDouble()), container
    ).addTo(this).also(callback)
}

class Mob(
    private val platformerDynamic: PlatformerDynamicComponent,
    level: GameLevelComponent<LevelMark>,
    private val spriteComponent: SpriteComponent,
    container: Container
) : Entity(level, container),
    PlatformerDynamicComponent by platformerDynamic,
    SpriteComponent by spriteComponent {


    private val moveSpeed = 0.015

    private val hasPlatformLeft get() = hasMark(cx, cy, LevelMark.PLATFORM_END_LEFT) && dir == -1 && xr < 0.5
    private val hasPlatformRight get() = hasMark(cx, cy, LevelMark.PLATFORM_END_RIGHT) && dir == 1 && xr > 0.5
    private val hasSmallStep
        get() = hasMark(
            cx,
            cy,
            LevelMark.SMALL_STEP,
            dir
        ) && ((dir == 1 && xr >= 0.5) || (dir == -1 && xr <= 0.5))

    private val canSeeHero: Boolean get() = castRayTo(hero) && distGridTo(hero) <= 15
    private val nearHero: Boolean get() = canSeeHero && distGridTo(hero) <= 1

    private var attackRange = 2 // grid cells
    private var aggroTarget: GridPositionComponent? = null
    private var attack = false

    val canStun get() = !cd.has(STUN_IMMUNE)

    private sealed class MobState {
        object Idle : MobState()
        object Patrol : MobState()
        object HopSmallStep : MobState()
        object Alert : MobState()
        object ChaseTarget : MobState()
        object LostTarget : MobState()
        object SearchingTarget : MobState()
        object PrepareAttack : MobState()
        object Attack : MobState()
        object Stunned : MobState()
    }

    private val movementFsm = stateMachine<MobState> {
        state(MobState.Stunned) {
            reason { cd.has(STUNNED) }
            begin {
                attack = false
                cd(ATTACK, 250.milliseconds)
                cd(STUN_IMMUNE, 500.milliseconds)
            }
        }
        state(MobState.Attack) {
            reason { attack && !cd.has(ATTACK) }
            begin {
                cd(ATTACK, 500.milliseconds)
                fx.swipe(centerX, centerY, dir)
                if (distGridTo(hero) <= attackRange) {
                    hero.hit(this@Mob)
                }
                attack = false
            }
        }
        state(MobState.PrepareAttack) {
            reason {
                (aggroTarget != null && nearHero || cd.has(PREPARE_ATTACK))
                        && !attack && !cd.has(ATTACK)
            }
            begin {
                cd(PREPARE_ATTACK, 500.milliseconds) {
                    attack = true
                }
                sprite.playOverlap(Assets.tiles.getByPrefix("mobIdle"), 500.milliseconds)
                velocityX = 0.0
            }
            update {
                stretchY = 1.25
            }
            end {
                cd.remove(PREPARE_ATTACK)
            }
        }
        state(MobState.HopSmallStep) {
            reason { hasSmallStep }
            update { hopSmallStep() }
        }
        state(MobState.LostTarget) {
            reason { aggroTarget != null && !canSeeHero && !cd.has(KEEP_AGGRO) }
            begin {
                cd(LOST_TARGET, 5.seconds)
                aggroTarget = null
            }

        }
        state(MobState.ChaseTarget) {
            reason { aggroTarget != null }
            update {
                chaseTarget()
                cd(KEEP_AGGRO, 3.seconds)
            }
        }
        state(MobState.Alert) {
            reason { aggroTarget == null && canSeeHero && dir == dirTo(level.hero) }
            begin {
                aggroTarget = level.hero
                stretchX = 0.6
                dir = dirTo(level.hero)
            }
        }
        state(MobState.SearchingTarget) {
            reason { cd.has(LOST_TARGET) }
            update { search() }

        }
        state(MobState.Patrol) {
            reason { !cd.has(LOCK) }
            begin { sprite.playAnimationLooped(Assets.mobRun) }
            update { autoPatrol() }
        }
        state(MobState.Idle) {
            reason { true }
            begin { sprite.playAnimationLooped(Assets.mobIdle) }
        }
        stateChanged {
            //debugLabel.text = it::class.simpleName ?: ""
        }
    }

    companion object {
        private const val LOCK = "lock"
        private const val TURN = "turn"
        private const val SEARCH = "search"
        private const val KEEP_AGGRO = "keepAggro"
        private const val LOST_TARGET = "lostTarget"
        private const val ATTACK = "attack"
        private const val PREPARE_ATTACK = "prepareAttack"
        private const val STUNNED = "stunned"
        private const val STUN_IMMUNE = "stunImmune"
    }

    init {
        sync()
    }

    override fun update(dt: TimeSpan) {
        super.update(dt)
        movementFsm.update(dt)
        updateGridPosition(tmod)
    }

    override fun postUpdate(dt: TimeSpan) {
        super.postUpdate(dt)
        sync()
        updateSprite()
    }

    fun stun() {
        stretchX = 2.0
        fx.bloodSplatter(centerX, bounds.top)
        cd(STUNNED, 100.milliseconds)
    }

    private fun autoPatrol() {
        velocityX += moveSpeed * dir * tmod

        if ((hasPlatformLeft || hasPlatformRight) && !hasSmallStep) {
            cd(LOCK, 500.milliseconds)
            stretchX = 0.85
            dir *= -1
        }
    }

    private fun chaseTarget() {
        aggroTarget?.let {
            dir = dirTo(it)
            velocityX += moveSpeed * 1.5 * dir * tmod
        }
    }

    private fun search() {
        if (!cd.has(TURN)) {
            cd(TURN, (500..900).random().milliseconds)
            cd(SEARCH, (100..400).random().milliseconds)
            dir *= -1
        }

        if (cd.has(SEARCH)) {
            velocityX += moveSpeed * 2 * dir * tmod
        }
    }

    private fun hopSmallStep() {
        velocityY = -0.25
        velocityX = dir * 0.1
        xr = if (dir == 1) 0.7 else 0.3
    }
}