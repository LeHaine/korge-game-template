package com.lehaine.pixelheist

import com.lehaine.pixelheist.entity.Hero
import com.soywiz.kmem.clamp

class GameLevel(val level: World.WorldLevel) {
    var _hero: Hero? = null
    val hero get() = _hero!!
    val entities: ArrayList<Entity> = arrayListOf()

    val width get() = level.layerCollisions.cWidth
    val height get() = level.layerCollisions.cHeight

    private val marks = mutableMapOf<LevelMark, MutableMap<Int, Int>>()
    private val collisionLayers = intArrayOf(1)
    private val collisionLayer = level.layerCollisions

    init {
        createLevelMarks()
    }

    fun isValid(cx: Int, cy: Int) = collisionLayer.isCoordValid(cx, cy)
    fun getCoordId(cx: Int, cy: Int) = collisionLayer.getCoordId(cx, cy)

    fun hasCollision(cx: Int, cy: Int): Boolean {
        return if (isValid(cx, cy)) {
            collisionLayers.contains(collisionLayer.getInt(cx, cy))
        } else {
            true
        }
    }

    fun hasMark(cx: Int, cy: Int, mark: LevelMark, dir: Int = 0): Boolean {
        return marks[mark]?.get(getCoordId(cx, cy)) == dir && isValid(cx, cy)
    }

    fun setMarks(cx: Int, cy: Int, marks: List<LevelMark>) {
        marks.forEach {
            setMark(cx, cy, it)
        }
    }

    fun setMark(cx: Int, cy: Int, mark: LevelMark, dir: Int = 0) {
        if (isValid(cx, cy) && !hasMark(cx, cy, mark)) {
            if (!marks.contains(mark)) {
                marks[mark] = mutableMapOf()
            }

            marks[mark]?.set(getCoordId(cx, cy), dir.clamp(-1, 1))
        }
    }

    private fun createLevelMarks() {
        for (cy in 0 until height) {
            for (cx in 0 until width) {
                // no collision at current pos or north but has collision south.
                if (!hasCollision(cx, cy) && hasCollision(cx, cy + 1) && !hasCollision(cx, cy - 1)) {
                    // if collision to the east of current pos and no collision to the northeast
                    if (hasCollision(cx + 1, cy) && !hasCollision(cx + 1, cy - 1)) {
                        setMark(cx, cy, LevelMark.SMALL_STEP, 1);
                    }

                    // if collision to the west of current pos and no collision to the northwest
                    if (hasCollision(cx - 1, cy) && !hasCollision(cx - 1, cy - 1)) {
                        setMark(cx, cy, LevelMark.SMALL_STEP, -1);
                    }
                }

                if (!hasCollision(cx, cy) && hasCollision(cx, cy + 1)) {
                    if (hasCollision(cx + 1, cy) ||
                        (!hasCollision(cx + 1, cy + 1) && !hasCollision(cx + 1, cy + 2))
                    ) {
                        setMarks(cx, cy, listOf(LevelMark.PLATFORM_END, LevelMark.PLATFORM_END_RIGHT))
                    }
                    if (hasCollision(cx - 1, cy) ||
                        (!hasCollision(cx - 1, cy + 1) && !hasCollision(cx - 1, cy + 2))
                    ) {
                        setMarks(cx, cy, listOf(LevelMark.PLATFORM_END, LevelMark.PLATFORM_END_LEFT))
                    }
                }
            }
        }
    }
}

enum class LevelMark {
    PLATFORM_END,
    PLATFORM_END_RIGHT,
    PLATFORM_END_LEFT,
    SMALL_STEP
}