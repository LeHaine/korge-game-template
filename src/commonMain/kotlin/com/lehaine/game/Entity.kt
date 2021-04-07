package com.lehaine.game

import com.lehaine.kiwi.component.BaseGameEntity
import com.lehaine.game.component.GenericGameLevelComponent
import com.soywiz.klock.TimeSpan
import com.soywiz.korge.view.Container

/**
 * A very base Entity that only uses has a [GridPositionComponent] and a [GenericGameLevelComponent].
 *
 * Create more entities that extend this class by adding different components.
 *
 * Example:
 * ```
 * class Hero(private val spriteComponent SpriteComponent,
 *      level:PixelGameLevelComponent<LevelMark>,
 *      private val platformerDynamic: PlatformerDynamicComponent,
 *      container: Container
 * ) : Entity(level, container),
 *     PlatformerDynamicComponent by platformerDynamic,
 *     SpriteComponent by spriteComponent
 * ```
 */
open class Entity(override val level: GenericGameLevelComponent<LevelMark>, container: Container) :
    BaseGameEntity(level, container) {
    val fx get() = level.fx

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