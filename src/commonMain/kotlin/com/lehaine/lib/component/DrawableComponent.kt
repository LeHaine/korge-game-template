package com.lehaine.lib.component

import com.lehaine.lib.EnhancedSprite
import com.soywiz.korge.view.Container
import com.soywiz.korge.view.addTo

interface DrawableComponent : Component {
    val sprite: EnhancedSprite
    var stretchX: Double
    var stretchY: Double

    var scaleX: Double
    var scaleY: Double

    fun syncPosition(x:Double, y:Double) {
        sprite.x = x
        sprite.y = y
    }

    companion object {
        operator fun invoke(container: Container): DrawableComponent {
            return DrawableComponentDefault(container)
        }
    }
}


private class DrawableComponentDefault(container: Container) : DrawableComponent {
    override val sprite = EnhancedSprite().addTo(container)

    private var _stretchX = 1.0
    private var _stretchY = 1.0

    override var stretchX: Double
        get() = _stretchX
        set(value) {
            _stretchX = value
            _stretchY = 2 - value
        }
    override var stretchY: Double
        get() = _stretchY
        set(value) {
            _stretchX = 2 - value
            _stretchY = value
        }

    override var scaleX = 1.0
    override var scaleY = 1.0
}