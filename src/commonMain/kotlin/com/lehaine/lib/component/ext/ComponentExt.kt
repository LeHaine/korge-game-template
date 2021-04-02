package com.lehaine.lib.component.ext


import com.lehaine.lib.castRay
import com.lehaine.lib.component.GridPositionComponent
import com.lehaine.lib.dist
import kotlin.math.atan2

fun GridPositionComponent.castRayTo(tcx: Int, tcy: Int, canRayPass: (Int, Int) -> Boolean) =
    castRay(cx, cy, tcx, tcy, canRayPass)

fun GridPositionComponent.toGridPosition(cx: Int, cy: Int, xr: Double = 0.5, yr: Double = 1.0) {
    this.cx = cx
    this.cy = cy
    this.xr = xr
    this.yr = yr
}

fun GridPositionComponent.dirTo(targetGridPosition: GridPositionComponent) =
    if (targetGridPosition.centerX > centerX) 1 else -1

fun GridPositionComponent.distGridTo(tcx: Int, tcy: Int, txr: Double = 0.5, tyr: Double = 0.5) =
    dist(cx + xr, cy + yr, tcx + txr, tcy + tyr)

fun GridPositionComponent.distGridTo(targetGridPosition: GridPositionComponent) =
    distGridTo(targetGridPosition.cx, targetGridPosition.cy, targetGridPosition.xr, targetGridPosition.yr)

fun GridPositionComponent.distPxTo(x: Double, y: Double) = dist(px, py, x, y)
fun GridPositionComponent.distPxTo(targetGridPosition: GridPositionComponent) =
    distPxTo(targetGridPosition.px, targetGridPosition.py)

fun GridPositionComponent.angleTo(x: Double, y: Double) = atan2(y - py, x - px)
fun GridPositionComponent.angleTo(targetGridPosition: GridPositionComponent) =
    angleTo(targetGridPosition.centerX, targetGridPosition.centerY)
