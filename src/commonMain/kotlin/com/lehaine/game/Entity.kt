package com.lehaine.game

import com.lehaine.kiwi.component.BaseGameEntity
import com.lehaine.game.component.PixelGameLevelComponent
import com.soywiz.korge.view.Container

open class Entity(override val level: PixelGameLevelComponent<LevelMark>, container: Container) :
    BaseGameEntity(level, container) {
    val fx get() = level.fx
    val hero get() = level.hero
}