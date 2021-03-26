package com.lehaine.lib

import kotlin.random.Random

fun ClosedFloatingPointRange<Double>.random() = Random.nextDouble(start, endInclusive)
