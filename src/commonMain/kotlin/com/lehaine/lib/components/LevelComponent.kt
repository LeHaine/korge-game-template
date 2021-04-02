package com.lehaine.lib.components

import com.lehaine.pixelheist.LevelMark

interface LevelComponent : Component {
    fun hasCollision(cx: Int, cy: Int): Boolean
    fun hasMark(cx: Int, cy: Int, mark: LevelMark, dir: Int): Boolean
    fun setMark(cx: Int, cy: Int, mark: LevelMark, dir: Int)
    fun setMarks(cx: Int, cy: Int, marks: List<LevelMark>)
    fun isValid(cx: Int, cy: Int): Boolean
    fun getCoordId(cx: Int, cy: Int): Boolean
}