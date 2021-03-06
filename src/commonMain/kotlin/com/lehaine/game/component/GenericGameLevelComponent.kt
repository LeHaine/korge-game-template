package com.lehaine.game.component

import com.lehaine.game.Fx
import com.lehaine.game.entity.Debugger
import com.lehaine.kiwi.component.LevelComponent
import com.lehaine.kiwi.korge.view.CameraContainer

/**
 * Add any extra references to this LevelComponent such as Hero reference for easier access in other entities.
 */
interface GenericGameLevelComponent<LevelMark> : LevelComponent<LevelMark> {
    val levelWidth: Int
    val levelHeight: Int
}