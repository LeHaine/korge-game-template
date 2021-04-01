package com.lehaine.pixelheist.entity

import com.lehaine.lib.cd
import com.lehaine.lib.getByPrefix
import com.lehaine.lib.stateMachine
import com.lehaine.pixelheist.*
import com.soywiz.klock.TimeSpan
import com.soywiz.klock.milliseconds
import com.soywiz.klock.seconds
import com.soywiz.korge.view.Container
import com.soywiz.korge.view.ViewDslMarker
import com.soywiz.korge.view.addTo


inline fun Container.mob(
    data: World.EntityMob,
    level: GameLevel,
    callback: @ViewDslMarker Mob.() -> Unit = {}
): Mob = Mob(data, level).addTo(this, callback)

class Mob(
    data: World.EntityMob, level: GameLevel,
) : Entity(data.cx, data.cy, level, data.pivotX.toDouble(), data.pivotY.toDouble()) {

    private val moveSpeed = 0.015

    private val hasPlatformLeft get() = level.hasMark(cx, cy, LevelMark.PLATFORM_END_LEFT) && dir == -1 && xr < 0.5
    private val hasPlatformRight get() = level.hasMark(cx, cy, LevelMark.PLATFORM_END_RIGHT) && dir == 1 && xr > 0.5
    private val hasSmallStep
        get() = level.hasMark(
            cx,
            cy,
            LevelMark.SMALL_STEP,
            dir
        ) && ((dir == 1 && xr >= 0.5) || (dir == -1 && xr <= 0.5))

    private val canSeeHero: Boolean get() = castRayTo(hero) && distGridTo(hero) <= 15
    private val nearHero: Boolean get() = canSeeHero && distGridTo(hero) <= 1

    private var aggroTarget: Entity? = null
    private var attack = false

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
    }

    private val movementFsm = stateMachine<MobState> {
        state(MobState.Attack) {
            reason { attack && !cd.has(ATTACK) }
            begin {
                cd("attack", 500.milliseconds)
                fx.swipe(centerX, centerY, dir)
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
                dx = 0.0
            }
            update {
                stretchY = 1.25
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
            begin { sprite.playAnimationLooped(assets.mobRun) }
            update { autoPatrol() }
        }
        state(MobState.Idle) {
            reason { true }
            begin { sprite.playAnimationLooped(assets.mobIdle) }
        }
        stateChanged {
            debugLabel.text = it::class.simpleName ?: ""
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
    }

    override fun update(dt: TimeSpan) {
        super.update(dt)
        movementFsm.update(dt)
    }

    private fun autoPatrol() {
        dx += moveSpeed * dir * tmod

        if ((hasPlatformLeft || hasPlatformRight) && !hasSmallStep) {
            cd(LOCK, 500.milliseconds)
            stretchX = 0.85
            dir *= -1
        }
    }

    private fun chaseTarget() {
        aggroTarget?.let {
            dir = dirTo(it)
            dx += moveSpeed * 1.5 * dir * tmod
        }
    }

    private fun search() {
        if (!cd.has(TURN)) {
            cd(TURN, (500..900).random().milliseconds)
            cd(SEARCH, (100..400).random().milliseconds)
            dir *= -1
        }

        if (cd.has(SEARCH)) {
            dx += moveSpeed * 2 * dir * tmod
        }
    }

    private fun hopSmallStep() {
        dy = -0.25
        dx = dir * 0.1
        xr = if (dir == 1) 0.7 else 0.3
    }
}