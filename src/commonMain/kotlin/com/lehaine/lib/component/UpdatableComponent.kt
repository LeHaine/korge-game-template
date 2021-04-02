package com.lehaine.lib.component

import com.soywiz.klock.TimeSpan

interface UpdatableComponent {
    var tmod: Double
    fun update(dt: TimeSpan)
    fun postUpdate(dt: TimeSpan)
}