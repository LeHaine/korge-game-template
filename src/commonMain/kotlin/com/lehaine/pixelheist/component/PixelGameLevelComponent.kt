package com.lehaine.pixelheist.component

import com.lehaine.kiwi.CameraContainer
import com.lehaine.kiwi.component.GameLevelComponent
import com.lehaine.pixelheist.Fx

/**
 * Add any extra references to this LevelComponent such as Hero reference for easier access in other entities.
 */
interface PixelGameLevelComponent<LevelMark> : GameLevelComponent<LevelMark> {
    val fx: Fx
    val camera: CameraContainer
    val levelWidth: Int
    val levelHeight: Int
}