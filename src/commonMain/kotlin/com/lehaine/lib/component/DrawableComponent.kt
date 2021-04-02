package com.lehaine.lib.component

import com.lehaine.lib.EnhancedSprite
import com.lehaine.lib.enhancedSprite
import com.soywiz.korge.view.Container

interface DrawableComponent {
    var dir: Int
    val sprite: EnhancedSprite

    companion object {
        operator fun invoke(container: Container, anchorX: Double, anchorY: Double): DrawableComponent {
            return DrawableComponentDefault(container, anchorX, anchorY)
        }
    }
}

class DrawableComponentDefault(container: Container, anchorX: Double, anchorY: Double) : DrawableComponent {
    override var dir = 0
    override val sprite: EnhancedSprite = container.enhancedSprite {
        smoothing = false
        this.anchorX = anchorX
        this.anchorY = anchorY
    }
}