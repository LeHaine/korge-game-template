package com.lehaine.pixelheist.entity

import com.lehaine.lib.cd
import com.lehaine.lib.stateMachine
import com.lehaine.pixelheist.*
import com.soywiz.klock.TimeSpan
import com.soywiz.klock.milliseconds
import com.soywiz.korge.view.ViewDslMarker
import kotlin.math.abs


inline fun EntityContainer.mob(
    data: World.EntityMob,
    assets: Assets,
    level: GameLevel,
    callback: @ViewDslMarker Mob.() -> Unit = {}
): Mob = Mob(data, assets, level).addEntityTo(this, callback)

class Mob(
    data: World.EntityMob, assets: Assets, level: GameLevel,
) : Entity(data.cx, data.cy, assets, level, data.pivotX.toDouble(), data.pivotY.toDouble()) {


    private val moveSpeed = 0.02

    private sealed class MobState {
        object Idle : MobState()
        object Patrol : MobState()
    }

    private val movementFsm = stateMachine<MobState> {
        state(MobState.Patrol) {
            reason { !cd.has("lock") }
            begin { sprite.playAnimationLooped(assets.mobRun) }
            update {
                dx += moveSpeed * dir * tmod

                if ((level.hasMark(cx, cy, LevelMark.PLATFORM_END_LEFT) && dir == -1 && xr < 0.5)
                    || (level.hasMark(
                        cx,
                        cy,
                        LevelMark.PLATFORM_END_RIGHT
                    ) && dir == 1 && xr > 0.5 && !level.hasMark(cx, cy, LevelMark.SMALL_STEP, dir))
                ) {
                    dir *= -1
                    cd("lock", 500.milliseconds)
                }
            }
        }
        state(MobState.Idle) {
            reason { true }
            begin { sprite.playAnimationLooped(assets.mobIdle) }
        }

        stateChanged {
            debugLabel.text = it::class.simpleName ?: ""
        }
    }

    override fun update(dt: TimeSpan) {
        super.update(dt)
        movementFsm.update(dt)
    }
}