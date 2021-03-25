package com.lehaine.pixelheist.entity

import com.lehaine.lib.stateMachine
import com.lehaine.pixelheist.*
import com.soywiz.klock.TimeSpan
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


    private sealed class MobState {
        object Idle : MobState()
        object Run : MobState()
    }

    private val movementFsm = stateMachine<MobState> {
        state(MobState.Run) {
            reason { abs(dx) >= 0.01 }
            begin { sprite.playAnimationLooped(assets.mobRun) }
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