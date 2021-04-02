package com.lehaine.pixelheist.components

interface LevelCollisionComponent : Component {
    fun hasCollision(cx: Int, cy: Int): Boolean
}