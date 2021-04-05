package com.lehaine.pixelheist

import com.lehaine.kiwi.component.BaseGameEntity
import com.lehaine.pixelheist.component.PixelGameLevelComponent
import com.soywiz.korge.view.Container

/**
 * A very base Entity that only uses has a [GridPositionComponent] and a [PixelGameLevelComponent].
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
open class Entity(override val level: PixelGameLevelComponent<LevelMark>, container: Container) :
    BaseGameEntity(level, container) {
    val fx get() = level.fx
}