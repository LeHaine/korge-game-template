package com.lehaine.pixelheist.entity

import com.lehaine.lib.cd
import com.lehaine.lib.stateMachine
import com.lehaine.pixelheist.*
import com.soywiz.klock.TimeSpan
import com.soywiz.klock.milliseconds
import com.soywiz.korge.view.ViewDslMarker


inline fun EntityContainer.mob(
    data: World.EntityMob,
    assets: Assets,
    level: GameLevel,
    callback: @ViewDslMarker Mob.() -> Unit = {}
): Mob = Mob(data, assets, level).addEntityTo(this, callback)

class Mob(
    data: World.EntityMob, assets: Assets, level: GameLevel,
) : Entity(data.cx, data.cy, assets, level, data.pivotX.toDouble(), data.pivotY.toDouble()) {

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

    private sealed class MobState {
        object Idle : MobState()
        object Patrol : MobState()
        object HopSmallStep : MobState()
    }

    private val movementFsm = stateMachine<MobState> {
        state(MobState.HopSmallStep) {
            reason { hasSmallStep }
            update { hopSmallStep() }
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
    }

    override fun update(dt: TimeSpan) {
        super.update(dt)
        movementFsm.update(dt)
    }

    private fun autoPatrol() {
        dx += moveSpeed * dir * tmod

        if ((hasPlatformLeft || hasPlatformRight) && !hasSmallStep) {
            cd(LOCK, 500.milliseconds)
            dir *= -1
        }
    }


    private fun hopSmallStep() {
        dy = -0.25
        dx = dir * 0.1
        xr = if (dir == 1) 0.7 else 0.3
    }
}