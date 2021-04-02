package com.lehaine.lib.component

import com.lehaine.lib.EnhancedSprite
import com.lehaine.lib.enhancedSprite
import com.soywiz.korge.view.Container

interface SpriteComponent : DrawableComponent, ScaleAndStretchComponent {
    companion object {
        operator fun invoke(container: Container, anchorX: Double, anchorY: Double): SpriteComponent {
            return SpriteComponentDefault(container, anchorX, anchorY)
        }
    }
}

class SpriteComponentDefault(container: Container, anchorX: Double, anchorY: Double) : SpriteComponent {
    override var dir = 0
    override val sprite: EnhancedSprite = container.enhancedSprite {
        smoothing = false
        this.anchorX = anchorX
        this.anchorY = anchorY
    }
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

    override fun updateStretchAndScale() {
        _stretchX += (1 - _stretchX) * 0.2
        _stretchY += (1 - _stretchY) * 0.2
    }
}