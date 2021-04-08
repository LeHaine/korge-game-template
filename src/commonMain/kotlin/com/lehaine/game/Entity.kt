package com.lehaine.game

import com.lehaine.game.component.GenericGameLevelComponent
import com.lehaine.kiwi.component.*
import com.soywiz.klock.TimeSpan

/**
 * An example [Entity] that extends the [SpriteLevelEntity] class.
 *
 * Create more entities that extend this class by adding different components.
 *
 * Example:
 * ```
 *  class Hero(
 *      level: GenericGameLevelComponent<LevelMark>,
 *      spriteComponent: SpriteComponent,
 *      platformerDynamic: PlatformerDynamicComponent
 *  ) : GameEntity(level, spriteComponent, platformerDynamic),
 *      PlatformerDynamicComponent by platformerDynamic,
 *      SpriteComponent by spriteComponent {}
 * ```
 */
open class GameEntity(
    override val level: GenericGameLevelComponent<LevelMark>,
    spriteComponent: SpriteComponent = SpriteComponentDefault(anchorX = 0.5, anchorY = 1.0),
    position: LevelDynamicComponent = LevelDynamicComponentDefault(
        levelComponent = level,
        anchorX = 0.5,
        anchorY = 1.0
    )
) : SpriteLevelEntity(level, spriteComponent, position) {

    val fx get() = level.fx
    val camera get() = level.camera

    // TODO maybe add a component or something to handle creating inputs
    val input get() = container.stage!!.views.input

    private val affects = hashMapOf<Affect, TimeSpan>()

    override fun update(dt: TimeSpan) {
        super.update(dt)
        updateAffects(dt)
    }

    fun hasAffect(affect: Affect) = affects.containsKey(affect)

    fun addAffect(affect: Affect, duration: TimeSpan, addToCurrentDuration: Boolean = false) {
        if (affects.containsKey(affect)) {
            if (addToCurrentDuration) {
                affects[affect] = affects[affect]?.plus(duration) ?: duration
                return
            }
        }
        affects[affect] = duration
        onAffectStart(affect)
    }

    fun removeAffect(affect: Affect) {
        affects.remove(affect)
        onAffectEnd(affect)
    }

    open fun onAffectStart(affect: Affect) {}
    open fun onAffectUpdate(affect: Affect) {}
    open fun onAffectEnd(affect: Affect) {}

    private fun updateAffects(dt: TimeSpan) {
        affects.keys.forEach {
            var remainingTime = affects[it] ?: TimeSpan.ZERO
            remainingTime -= dt
            if (remainingTime <= TimeSpan.ZERO) {
                removeAffect(it)
            } else {
                affects[it] = remainingTime
                onAffectUpdate(it)
            }
        }
    }
}