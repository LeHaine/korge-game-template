package com.lehaine.lib.component

import com.soywiz.korui.UiContainer

interface LevelComponent<LevelMark> : Component {
    fun hasCollision(cx: Int, cy: Int): Boolean
    fun hasMark(cx: Int, cy: Int, mark: LevelMark, dir: Int = 0): Boolean
    fun setMark(cx: Int, cy: Int, mark: LevelMark, dir: Int = 0)
    fun setMarks(cx: Int, cy: Int, marks: List<LevelMark>)
    fun isValid(cx: Int, cy: Int): Boolean
    fun getCoordId(cx: Int, cy: Int): Int
}