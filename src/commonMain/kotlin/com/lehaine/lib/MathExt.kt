package com.lehaine.lib

import kotlin.math.sqrt
import kotlin.random.Random

fun ClosedFloatingPointRange<Double>.random() = Random.nextDouble(start, endInclusive)
fun ClosedFloatingPointRange<Float>.random() = Random.nextDouble(start.toDouble(), endInclusive.toDouble()).toFloat()
fun IntRange.random(): Double {
    return random(Random).toDouble()
}


fun distSqr(ax: Double, ay: Double, bx: Double, by: Double) =
    (ax - bx) * (ax - bx) + (ay - by) * (ay - by)

fun dist(ax: Double, ay: Double, bx: Double, by: Double) =
    sqrt(distSqr(ax, ay, bx, by))


