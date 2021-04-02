package com.lehaine.lib.component

import com.soywiz.korge.debug.uiCollapsibleSection
import com.soywiz.korge.debug.uiEditableValue
import com.soywiz.korma.geom.Rectangle
import com.soywiz.korui.UiContainer

interface GridPositionComponent : Component {
    var cx: Int
    var cy: Int
    var xr: Double
    var yr: Double

    var gridCellSize: Int
    var gridPosWidth: Double
    var gridPosHeight: Double

    var anchorX: Double
    var anchorY: Double

    val px: Double
    val py: Double
    val centerX: Double
    val centerY: Double
    val bounds: Rectangle

    override fun updateComponent(tmod: Double) {
        updateX(tmod)
        updateY(tmod)
    }

    fun updateX(tmod: Double) {
        while (xr > 1) {
            xr--
            cx++
        }
        while (xr < 0) {
            xr++
            cx--
        }
    }

    fun updateY(tmod: Double) {
        while (yr > 1) {
            yr--
            cy++
        }
        while (yr < 0) {
            yr++
            cy--
        }
    }


    override fun createDebugInfo(container: UiContainer) {
        container.uiCollapsibleSection("Grid Position") {
            uiEditableValue(this@GridPositionComponent::gridCellSize, name = "Grid Cell Size", min = 1)
            uiEditableValue(
                listOf(this@GridPositionComponent::cx, this@GridPositionComponent::cy),
                name = "(cx, cy)",
                min = 0,
                max = 10000
            )
            uiEditableValue(
                listOf(this@GridPositionComponent::xr, this@GridPositionComponent::yr),
                name = "(xr, yr)",
                min = 0.0,
                max = 1.0
            )
            uiEditableValue(
                listOf(this@GridPositionComponent::gridPosWidth, this@GridPositionComponent::gridPosHeight),
                name = "Size (w, h)"
            )
        }
        super.createDebugInfo(container)
    }

    companion object {
        operator fun invoke(): GridPositionComponent {
            return GridPositionComponentDefault()
        }
    }
}

open class GridPositionComponentDefault(
    override var cx: Int = 0,
    override var cy: Int = 0,
    override var xr: Double = 0.5,
    override var yr: Double = 0.5
) : GridPositionComponent {


    override var gridCellSize: Int = 16
    override var gridPosWidth: Double = 16.0
    override var gridPosHeight: Double = 16.0

    override var anchorX: Double = 0.5
    override var anchorY: Double = 0.5

    override val px get() = (cx + xr) * gridCellSize
    override val py get() = (cy + yr) * gridCellSize
    override val centerX get() = px + (0.5 - anchorX) * gridCellSize
    override val centerY get() = py + (0.5 - anchorY) * gridCellSize

    private var _bounds = Rectangle()
    override val bounds: Rectangle
        get() = _bounds.apply {
            top = py - anchorY * width
            right = px + (1 - px) * width
            bottom = py + (1 - anchorY) * height
            left = px - anchorX * height
        }
}