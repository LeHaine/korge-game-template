package com.lehaine.pixelheist.component

import com.lehaine.lib.CameraContainer
import com.lehaine.lib.component.GameLevelComponent
import com.lehaine.pixelheist.Fx
import com.lehaine.pixelheist.entity.Hero

interface PixelGameLevelComponent<LevelMark> : GameLevelComponent<LevelMark> {
    val fx: Fx
    val hero: Hero
    val camera: CameraContainer
    val levelWidth: Int
    val levelHeight: Int
}