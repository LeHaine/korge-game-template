package com.lehaine.pixelheist.component

import com.lehaine.lib.CameraContainer
import com.lehaine.lib.component.LevelComponent
import com.lehaine.pixelheist.Entity
import com.lehaine.pixelheist.Fx
import com.lehaine.pixelheist.entity.Hero

interface GameLevelComponent<LevelMark> : LevelComponent<LevelMark> {
    val fx: Fx
    val hero: Hero
    val entities: ArrayList<Entity>
    val camera: CameraContainer
    val levelWidth: Int
    val levelHeight: Int
}