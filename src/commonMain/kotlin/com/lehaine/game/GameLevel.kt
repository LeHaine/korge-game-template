package com.lehaine.game

import com.lehaine.game.component.GenericGameLevelComponent
import com.lehaine.game.entity.Debugger
import com.lehaine.kiwi.component.Entity
import com.lehaine.kiwi.korge.view.CameraContainer
import com.soywiz.kmem.clamp

class GameLevel(val level: World.WorldLevel) : GenericGameLevelComponent<LevelMark> {
    override val levelWidth get() = level.layerCollisions.cWidth
    override val levelHeight get() = level.layerCollisions.cHeight

    private val marks = mutableMapOf<LevelMark, MutableMap<Int, Int>>()

    // a list of collision layers indices from LDtk world
    private val collisionLayers = intArrayOf(1)
    private val collisionLayer = level.layerCollisions

    init {
        createLevelMarks()
    }

    override fun isValid(cx: Int, cy: Int) = collisionLayer.isCoordValid(cx, cy)
    override fun getCoordId(cx: Int, cy: Int) = collisionLayer.getCoordId(cx, cy)

    override fun hasCollision(cx: Int, cy: Int): Boolean {
        return if (isValid(cx, cy)) {
            collisionLayers.contains(collisionLayer.getInt(cx, cy))
        } else {
            true
        }
    }

    override fun hasMark(cx: Int, cy: Int, mark: LevelMark, dir: Int): Boolean {
        return marks[mark]?.get(getCoordId(cx, cy)) == dir && isValid(cx, cy)
    }

    override fun setMarks(cx: Int, cy: Int, marks: List<LevelMark>) {
        marks.forEach {
            setMark(cx, cy, it)
        }
    }

    override fun setMark(cx: Int, cy: Int, mark: LevelMark, dir: Int) {
        if (isValid(cx, cy) && !hasMark(cx, cy, mark)) {
            if (!marks.contains(mark)) {
                marks[mark] = mutableMapOf()
            }

            marks[mark]?.set(getCoordId(cx, cy), dir.clamp(-1, 1))
        }
    }

    // set level marks at start of level creation to react to certain tiles
    private fun createLevelMarks() {

    }

}

enum class LevelMark {
    PLATFORM_END,
    PLATFORM_END_RIGHT,
    PLATFORM_END_LEFT,
    SMALL_STEP
}