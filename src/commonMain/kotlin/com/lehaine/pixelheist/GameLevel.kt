package com.lehaine.pixelheist

class GameLevel(val level: World.WorldLevel) {
    val collisionLayers = intArrayOf(1)

    val width get() = level.layerCollisions.cWidth
    val height get() = level.layerCollisions.cHeight

    private val collisionLayer = level.layerCollisions

    fun isValid(cx: Int, cy: Int) = collisionLayer.isCoordValid(cx, cy)
    fun getCoordId(cx: Int, cy: Int) = collisionLayer.getCoordId(cx, cy)

    fun hasCollision(cx: Int, cy: Int): Boolean {
        return if (isValid(cx, cy)) {
            collisionLayers.contains(collisionLayer.getInt(cx, cy))
        } else {
            true
        }
    }
}